package fr.unistra.pelican.algorithms.statistics;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;

/**
 * This class computes the normalized mean square error between two hue bands
 * The input arguments are considered all in a polar colour space
 * 
 * @author Abdullah
 */
public class HueNMSE extends Algorithm{
	/**
	 * the original image
	 */
	public Image original;

	/**
	 * the filtered image
	 */
	public Image filtered;

	/**
	 * the reference hue
	 */
	public double refHue;

	/**
	 * the resulting error
	 */
	public Double output;
	
	/**
	 * This class computes the normalized mean square error between two hue bands
	 * @param original the original image
	 * @param filtered the filtered image
	 * @param refHue the reference hue
	 * @return the normalised mean squared hue error
	 */
	public static Double exec(Image original,Image filtered,Double refHue)
	{
		return (Double) new HueNMSE().process(original,filtered,refHue);
	}

	/**
	 * Constructor
	 * 
	 */
	public HueNMSE() {

		super();
		super.inputs = "original,filtered,refHue";
		super.outputs = "output";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		int bDim = original.getBDim();
		int xDim = original.getXDim();
		int yDim = original.getYDim();

		int bDim2 = filtered.getBDim();
		int xDim2 = filtered.getXDim();
		int yDim2 = filtered.getYDim();

		if (bDim != bDim2 || xDim != xDim2 || yDim != yDim2)
			throw new AlgorithmException(
					"The input images must have same dimensions");

		double sum1 = 0.0;
		double sum2 = 0.0;

		for (int x = 0; x < xDim; x++) {
			for (int y = 0; y < yDim; y++) {
				double[] p1 = original.getVectorPixelXYZTDouble(x, y, 0, 0);
				double[] p2 = filtered.getVectorPixelXYZTDouble(x, y, 0, 0);

				double abs1 = Math.abs(refHue - p1[0]);
				if (abs1 > 0.5)
					abs1 = 1.0 - abs1;

				double abs2 = Math.abs(refHue - p2[0]);
				if (abs2 > 0.5)
					abs2 = 1.0 - abs2;

				double norm = abs1 - abs2;
				sum1 += norm * norm;

				norm = abs1;
				sum2 += norm * norm;
			}
		}

		output = new Double(sum1 / sum2);
	}
}
