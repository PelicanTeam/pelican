package fr.unistra.pelican.algorithms.morphology.vectorial.gradient;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.BasinTools;
import fr.unistra.pelican.util.Point4D;
import fr.unistra.pelican.util.Tools;

/**
 * This class computes a hue gradient according to "Morphological operations on the unit
 * circle" - Hanbury & Serra 2001 + a little saturation weighting
 * 
 * The input is supposed to be in a polar colour space
 * 
 * @author Abdullah
 * 
 */
public class HueGradient extends Algorithm
{
	/**
	 * the input image
	 */
	public Image input;

	/**
	 * the ouput image
	 */
	public Image output;

	/**
	 * slope of the weighting sigmoid
	 */
	private double egim = 10.0;

	/**
	 * offset of the weighting sigmoid
	 */
	private double esik = 0.35;

	/**
	 * the structuring element
	 */
	public BooleanImage se;
	
	/**
	 * A morphological hue gradient with a saturation based sigmoid weighting
	 * 
	 * @param image the input image in a polar colour space
	 * @param se the structuring element
	 * @return the output image
	 */
	public static Image exec(Image image,BooleanImage se)
	{
		return (Image) new HueGradient().process(image,se);
	}

	/**
	 * Constructor
	 * 
	 */
	public HueGradient() {

		super();
		super.inputs = "input,se";
		super.outputs = "output";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		int xDim = input.getXDim();
		int yDim = input.getYDim();

		output = new DoubleImage(xDim, yDim, 1, 1, 1);

		Point4D[] points = se.foreground();

		for (int x = 0; x < xDim; x++)
			for (int y = 0; y < yDim; y++)
				if ( input.isPresentXY( x,y ) )
					output.setPixelXYDouble(x, y, getGradient(x, y, points));

	}

	private double getGradient(int x, int y, Point4D[] points) {
		double centerH = input.getPixelXYBDouble(x, y, 0);
		double centerS = input.getPixelXYBDouble(x, y, 1);

		double maxDist = 0.0;
		double minDist = Double.MAX_VALUE;

		for (int i = 0; i < points.length; i++) {
			int valX = x - se.getCenter().x + points[i].x;
			int valY = y - se.getCenter().y + points[i].y;

			if (	valX >= 0 && valX < input.getXDim() 
				 && valY >= 0 && valY < input.getYDim() 
				 && input.isPresentXYB( valX,valY,0 ) 
				 && input.isPresentXYB( valX,valY,1 ) ) { 

				double pH = input.getPixelXYBDouble(valX, valY, 0);
				double pS = input.getPixelXYBDouble(valX, valY, 1);

				double coeff = 1 / ((1 + Math.exp(-1 * egim * (pS - esik))) * (1 + Math
						.exp(-1 * egim * (centerS - esik))));

				double tmp = coeff
						* BasinTools.hueDiffBoost(Tools
								.hueDistance(pH, centerH));

				if (tmp < minDist)
					minDist = tmp;
				if (tmp > maxDist)
					maxDist = tmp;
			}
		}

		return maxDist - minDist;
	}
}
