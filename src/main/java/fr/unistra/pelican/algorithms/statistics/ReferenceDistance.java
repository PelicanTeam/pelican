package fr.unistra.pelican.algorithms.statistics;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.Tools;

/**
 * This class computes the average distance of an image from a given reference hue
 * The input is supposed to be a HSY (or similar: HSV,HSL) colour image
 * 
 * 22/01/2007
 * 
 * @author Abdullah
 * 
 */
public class ReferenceDistance extends Algorithm
{

	/**
	 * The input image
	 */
	public Image input;

	/**
	 * the reference hue
	 */
	public double refHue;

	/**
	 * the output image
	 */
	public double output;
	
	/**
	 * This method computes the average distance of an image from a given reference hue
	 * @param input The input image
	 * @param refHue the reference hue
	 * @return the distance image from this hue
	 */
	public static Image exec(Image input,Double refHue)
	{
		return (Image) new ReferenceDistance().process(input,refHue);
	}

	/**
	 * Constructor
	 * 
	 */
	public ReferenceDistance() {

		super();
		super.inputs = "input,refHue";
		super.outputs = "output";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		double sum = 0.0;

		for (int x = 0; x < input.getXDim(); x++) {
			for (int y = 0; y < input.getYDim(); y++) {
				double p = input.getPixelXYBDouble(x, y, 0);
				sum += Tools.hueDistance(p, refHue);
			}
		}

		output = sum / (input.getXDim() * input.getYDim());
	}

}
