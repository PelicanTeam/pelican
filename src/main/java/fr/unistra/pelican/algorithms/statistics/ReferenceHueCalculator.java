package fr.unistra.pelican.algorithms.statistics;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.Tools;

/**
 * This class computes the reference hue of an image as the average of saturation weighted
 * hue values The input is supposed to be a HSY (or similar: HSV,HSL) colour
 * image
 * 
 * 22/10/2006
 * 
 * @author Abdullah
 * 
 */
public class ReferenceHueCalculator extends Algorithm
{
	/**
	 * The input image 
	 */
	public Image input;

	/**
	 * the reference hue
	 */
	public Double output;
	
	/**
	 * This class computes the reference hue of an image as the average of saturation weighted hue values
	 * @param original The original image
	 * @return the reference hue
	 */
	public static Double exec(Image input)
	{
		return (Double) new ReferenceHueCalculator().process(input);
	}

	/**
	 * Constructor
	 * 
	 */
	public ReferenceHueCalculator() {

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
		double avg = 0.0;

		double slope = 10.0;
		double offset = 0.5;

		// first compute the classical average...using non-weighted hues..
		for (int x = 0; x < input.getXDim(); x++) {
			for (int y = 0; y < input.getYDim(); y++) {
				double p = input.getPixelXYBDouble(x, y, 0);
				avg = Tools.hueAverage(avg, p);
			}
		}

		// now...once more for every hue compute it using the weighted
		// distances..
		for (int x = 0; x < input.getXDim(); x++) {
			for (int y = 0; y < input.getYDim(); y++) {
				double[] p = input.getVectorPixelXYZTDouble(x, y, 0, 0);

				double beta = 0.0;

				if (p[2] <= 0.5)
					beta = 2 / (1 + Math.exp(-10 * (p[2] - 0.5)));
				else
					beta = 2 / (1 + Math.exp(10 * (p[2] - 0.5)));

				double alpha = 1 / (1 + Math.exp(-1 * slope * (p[1] - offset)))
						* beta;

				double coeff = alpha;

				// non weighted distance
				double distOld = Tools.hueDistance(p[0], avg); // \in [0,0.5]
				double distNew = distOld * coeff;
				double fark = distOld - distNew; // fark > 0

				// modify so as the new distance replaces the old
				if (p[0] <= avg)
					p[0] = Tools.hueAddition(p[0], fark);
				else
					p[0] = Tools.hueDifference(p[0], fark);

				// now we can compute the average.
				avg = Tools.hueAverage(avg, p[0]);
			}
		}

		output = avg;
		// System.err.println("Reference Hue 1 : " + avg);
	}

}
