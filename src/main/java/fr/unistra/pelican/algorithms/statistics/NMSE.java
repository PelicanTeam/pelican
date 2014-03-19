package fr.unistra.pelican.algorithms.statistics;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.Tools;

/**
 * This class computes the normalized mean square error between two multiband images
 * 
 * @author Abdullah
 */
public class NMSE extends Algorithm
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
	 * the computed error
	 */
	public Double output;
	
	/**
	 * This class computes the normalized mean square error between two multiband images
	 * @param original the original image
	 * @param filtered the filtered image
	 * @return the normalised squared error
	 */
	public static Double exec(Image original,Image filtered)
	{
		return (Double) new NMSE().process(original,filtered);
	}

	/**
	 * Constructor
	 * 
	 */
	public NMSE() {

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

		double sum1 = 0.0;
		double sum2 = 0.0;

		for (int x = 0; x < xDim; x++) {
			for (int y = 0; y < yDim; y++) {
				double[] p1 = original.getVectorPixelXYZTDouble(x, y, 0, 0);
				double[] p2 = filtered.getVectorPixelXYZTDouble(x, y, 0, 0);

				double norm = Tools.euclideanNorm(Tools.VectorDifference(p1, p2));
				sum1 += norm * norm;

				norm = Tools.euclideanNorm(p1);
				sum2 += norm * norm;
			}
		}

		output = new Double(sum1 / sum2);
	}
}
