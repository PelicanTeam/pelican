package fr.unistra.pelican.algorithms.statistics;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.Tools;

/**
 * This class computes the root mean square of a multiband image
 * 
 * @author Abdullah
 */
public class RMS extends Algorithm
{
	/**
	 * the input image
	 */
	public Image input;

	/**
	 * the resulting RMS
	 */
	public Double output;
	
	/**
	 * This class computes the root mean square of a multiband image
	 * @param input the input image
	 * @return the RMS
	 */
	public static Double exec(Image input)
	{
		return (Double) new RMS().process(input);
	}

	/**
	 * Constructor
	 * 
	 */
	public RMS() {

		super();
		super.inputs = "input";
		super.outputs = "output";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException
	{
		int xDim = input.getXDim();
		int yDim = input.getYDim();

		double sum = 0.0;

		for (int x = 0; x < xDim; x++) {
			for (int y = 0; y < yDim; y++) {
				double[] p = input.getVectorPixelXYZTDouble(x, y, 0, 0);
				double norm = Tools.euclideanNorm(p);

				sum += norm * norm;
			}
		}

		sum = Math.sqrt(sum / (xDim * yDim));

		output = new Double(sum);
	}
}
