package fr.unistra.pelican.algorithms.statistics;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.Tools;

/**
 * This class computes the mean square error between two multiband images
 * 
 * @author Abdullah
 */
public class MSE extends Algorithm
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
	 * the resultign error
	 */
	public Double output;
	
	/**
	 * This method computes the mean square error between two multiband images
	 * @param original the original image
	 * @param filtered the filtered image
	 * @return the mean squared error
	 */
	public static Double exec(Image original,Image filtered)
	{
		return (Double) new MSE().process(original,filtered);
	}

	/**
	 * Constructor
	 * 
	 */
	public MSE() {

		super();
		super.inputs = "original,filtered";
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

		double sum = 0.0;

		for (int x = 0; x < xDim; x++) {
			for (int y = 0; y < yDim; y++) {
				double[] p1 = original.getVectorPixelXYZTDouble(x, y, 0, 0);
				double[] p2 = filtered.getVectorPixelXYZTDouble(x, y, 0, 0);

				double norm = Tools.euclideanNorm(Tools.VectorDifference(p1, p2));
				sum += norm * norm;
			}
		}

		output = new Double(sum / (xDim * yDim));
	}
}
