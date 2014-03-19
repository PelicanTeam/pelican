package fr.unistra.pelican.algorithms.statistics;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.Tools;

/**
 * This class computes the relative normalized mean square error between two multiband images
 * 
 * @author Abdullah
 */
public class RelativeNMSE extends Algorithm
{
	/**
	 * the original image
	 */
	public Image original;

	/**
	 * the filtered image
	 */
	public Image filtered;

	/**
	 * the noisy image
	 */
	public Image noisy;

	/**
	 * the resulting relative NMSE
	 */
	public Double output;
	
	/**
	 * This class computes the relative normalized mean square error between two multiband images
	 * @param original the original image
	 * @param filtered the filtered image
	 * @param noisy the noisy image
	 * @return the relative normalized mean square error
	 */
	public static Double exec(Image original,Image filtered,Image noisy)
	{
		return (Double) new RelativeNMSE().process(original,filtered,noisy);
	}

	/**
	 * Constructor
	 * 
	 */
	public RelativeNMSE() {

		super();
		super.inputs = "original,filtered,noisy";
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
				double[] po = original.getVectorPixelXYZTDouble(x, y, 0, 0);
				double[] pf = filtered.getVectorPixelXYZTDouble(x, y, 0, 0);
				double[] pn = noisy.getVectorPixelXYZTDouble(x, y, 0, 0);

				double norm = Tools.euclideanNorm(Tools.VectorDifference(po, pf));
				sum1 += norm * norm;

				norm = Tools.euclideanNorm(Tools.VectorDifference(po, pn));
				sum2 += norm * norm;
			}
		}

		output = new Double(sum1 / sum2);
	}
}
