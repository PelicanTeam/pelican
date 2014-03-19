package fr.unistra.pelican.algorithms.histogram;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;

/**
 * Computes marginally the normalized histogram of a mono or multi-valued
 * dataset
 * 
 * @author Sebastien Lefevre
 * 
 * TODO : compute the appropriate histogram size
 */
public class MultivaluedHistogram extends Algorithm {

	/**
	 * First input parameter.
	 */
	public Image input;

	/**
	 * Output parameter.
	 */
	public double[][] output;

	/**
	 * Second input parameter.
	 */
	public Boolean normalized;

	/**
	 * Constructor
	 * 
	 */
	public MultivaluedHistogram() {

		super();
		super.inputs = "input,normalized";
		super.outputs = "output";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		// TODO : compute the appropriate histogram size
		int size = 256;

		output = new double[input.getBDim()][size];

		for (int b = 0; b < input.getBDim(); b++) {
			for (int i = 0; i < size; i++)
				output[b][i] = 0.0;

			for (int x = 0; x < input.getXDim(); x++)
				for (int y = 0; y < input.getYDim(); y++)
					for (int z = 0; z < input.getZDim(); z++)
						for (int t = 0; t < input.getTDim(); t++)
							output[b][input.getPixelXYZTBByte(x, y, z, t, b)]++;

			if (normalized == true) {
				for (int i = 0; i < size; i++)
					output[b][i] /= input.getXDim() * input.getYDim()
							* input.getZDim() * input.getTDim();
			}
		}
	}

	/**
	 * Computes marginally the normalized histogram of a mono or multi-valued
	 * dataset.
	 * 
	 * @param inputImage Image to be computed into histogram.
	 * @param normalized If the histogram will be normalized.
	 * @return The histogram.
	 */
	public static double[][] exec(Image input, Boolean normalized) {
		return  (double[][]) new MultivaluedHistogram().process(input, normalized);
	}
}
