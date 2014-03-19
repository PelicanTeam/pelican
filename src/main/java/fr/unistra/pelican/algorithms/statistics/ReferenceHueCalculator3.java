package fr.unistra.pelican.algorithms.statistics;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;

/**
 * This class computes the reference hue of an image as the maximum of its saturation
 * weighted histogram Something more evolved would take into account the
 * surroundings of the histogram bins...like the largest CC etc.
 * 
 * The input is supposed to be a HSY (or similar: HSV,HSL) colour image
 * 
 * 22/10/2006
 * 
 * @author Abdullah
 * 
 */
public class ReferenceHueCalculator3 extends Algorithm
{
	/**
	 * The input image
	 */
	public Image input;

	/**
	 * the output hue
	 */
	public Double output;
	
	/**
	 * This class computes the reference hue of an image as the average of saturation weighted hue values
	 * @param original The original image
	 * @return the reference hue
	 */
	public static Double exec(Image input)
	{
		return (Double) new ReferenceHueCalculator3().process(input);
	}

	/**
	 * Constructor
	 * 
	 */
	public ReferenceHueCalculator3() {

		super();
		super.inputs = "input";
		super.outputs = "output";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		double A = 0.0;
		double B = 0.0;

		for (int x = 0; x < input.getXDim(); x++) {
			for (int y = 0; y < input.getYDim(); y++) {
				double[] p = input.getVectorPixelXYZTDouble(x, y, 0, 0);

				A += p[1] * Math.cos(p[0] * 2 * Math.PI);
				B += p[1] * Math.sin(p[0] * 2 * Math.PI);
			}
		}

		if (A > 0 && B > 0)
			output = Math.atan(B / A);
		else if (A < 0)
			output = Math.atan(B / A) + Math.PI;
		else if (B < 0 && A > 0)
			output = Math.atan(B / A) + 2 * Math.PI;
		else if (B > 0 && A == 0)
			output = Math.PI / 2.0;
		else if (B < 0 && A == 0)
			output = 3 * Math.PI / 2.0;

		output = output / (2 * Math.PI);

		// System.err.println("Reference Hue 3 : " + output);
	}

}
