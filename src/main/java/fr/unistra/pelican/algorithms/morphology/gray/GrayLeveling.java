package fr.unistra.pelican.algorithms.morphology.gray;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.Point4D;
import fr.unistra.pelican.util.morphology.FlatStructuringElement3D;

/**
 * This class performs a standard morphological lambda-leveling
 * 
 * (warning actually just X,Y and T dim are taken into account)
 * 
 * TODO : Put in ND
 * 
 * 
 * @author Abdullah
 */
public class GrayLeveling extends Algorithm {

	/**
	 * The input image
	 */
	public Image input;

	/**
	 * The marker image
	 */
	public Image marker;

	/**
	 * The lambda parameter
	 */
	public int lambda;

	/**
	 * The output image
	 */
	public Image output;

	
	private boolean DEBUG = false;

	/**
	 * Default constructor
	 */
	public GrayLeveling() {
		super.inputs = "input,marker,lambda";
		super.outputs = "output";
		
	}

	/**
	 * Performs a standard morphological lambda-leveling
	 * @param input The input image
	 * @param marker The marker image
	 * @param lambda The lambda parameter
	 * @return The output image
	 */
	public static Image exec (Image input, Image marker, int lambda) {
		return (Image) new GrayLeveling().process(input,marker,lambda);
	}
	
	/*
	 * (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		// checking only the immediate 10-neighborhood
		BooleanImage se = FlatStructuringElement3D.createTemporal10ConnectivityFlatStructuringElement();
		Point4D[] points = se.foreground();
		
		if (lambda < 0 || lambda > 254)
			throw new AlgorithmException("Lambda value out of bounds!");
		
		Image tmp = null;
		
		output = marker.copyImage(true);
		
		int i = 0;
		
		do {
			tmp = output;
			output = level(output, points, se);
			if (DEBUG) System.err.println("Iteration " + (i++));
		} while (output.equals(tmp) == false);
	}

	private Image level(Image img, Point4D[] points, BooleanImage se)
	{
		Image tmp = img.copyImage(true);
		
		for (int t = 0; t < img.getTDim(); t++){
			for (int y = 0; y < img.getYDim(); y++) { 
				for (int x = 0; x < img.getXDim(); x++) {
					for (int b = 0; b < input.getBDim(); b++) {
				

						if ( !input.isPresentXYTB( x, y, t, b ) ) continue;

						int g = tmp.getPixelXYTBByte(x, y, t, b);
						int f = input.getPixelXYTBByte(x, y, t, b);
					
						if (g > f + lambda)
							tmp.setPixelXYTBByte(x, y, t, b, getLowerLevelledGray(tmp,
								x, y, t, b, points, se));
						else if (f > g + lambda)
							tmp.setPixelXYTBByte(x, y, t, b, getUpperLevelledGray(tmp,
								x, y, t, b, points, se));
						else
							; // f == g do nothing
					}
				}
			}
		}
		return tmp;
	}

	private int getLowerLevelledGray(Image img, int x, int y, int t, int b, Point4D[] points, BooleanImage se)
	{
		double min = 255;
		
		for (int i = 0; i < points.length; i++) {
			int _x = x - se.getCenter().x + points[i].x;
			int _y = y - se.getCenter().y + points[i].y;
			int _t = t - se.getCenter().t + points[i].t;
			
			if (	_x >= 0 && _x < input.getXDim() 
					&& _y >= 0 && _y < input.getYDim()
					&& _t >= 0 && _t < input.getTDim()
					&& input.isPresentXYTB( _x,_y,_t,b ) ) { 

				int p = img.getPixelXYTBByte(_x, _y, _t, b);
				if (min > p)
					min = p;
			}
		}
		return (int) Math.max(min, input.getPixelXYTBByte(x, y, t, b));
	}

	private int getUpperLevelledGray(Image img, int x, int y, int t, int b,
			Point4D[] points, BooleanImage se) {
		double max = 0;
		for (int i = 0; i < points.length; i++) {
			int _x = x - se.getCenter().x + points[i].x;
			int _y = y - se.getCenter().y + points[i].y;
			int _t = t - se.getCenter().t + points[i].t;
			if (	_x >= 0 && _x < input.getXDim() 
					&& _y >= 0 && _y < input.getYDim()
					&& _t >= 0 && _t < input.getTDim()
					&& input.isPresentXYTB( _x, _y, _t, b ) ) { 

				int p = img.getPixelXYTBByte(_x, _y, _t, b);
				if (max < p)
					max = p;
			}
		}
		return (int) Math.min(max, input.getPixelXYTBByte(x, y, t, b));
	}

}
