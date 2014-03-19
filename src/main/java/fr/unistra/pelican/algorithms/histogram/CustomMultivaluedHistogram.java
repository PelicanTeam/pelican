package fr.unistra.pelican.algorithms.histogram;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;

/**
 * 
 * Produces normalized histogram with a custom number of bins
 * 
 * @author Erchan Aptoula
 * 
 */
public class CustomMultivaluedHistogram extends Algorithm {

	/**
	 * first input parameter.
	 */
	public Image input;

	/**
	 * Output parameter.
	 */
	public double[][] output;

	/**
	 * Second input parameter which is the number of bins of each band.
	 */
	public int size;

	/**
	 * Third input parameter indicates if the histogram will be normalized.
	 */
	public boolean normalized;

	/**
	 * Constructor
	 * 
	 */
	public CustomMultivaluedHistogram() {

		super();
		super.inputs = "input,normalized,size";
		super.outputs = "output";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
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
 * Produces normalized histogram with a custom number of bins
 * 
 * @param input
 * 				Image to be converted in a histogram.
 * @param normalized
 * 				True if the histogram will be normalized.
 * @param size
 * 				The number of bins of each band.
 * @return An array of double which is the histogram.
 */
	public static double[][] exec(Image input, boolean normalized, int size) {
		return (double[][]) new CustomMultivaluedHistogram().process(input, normalized, size);
	}
}
