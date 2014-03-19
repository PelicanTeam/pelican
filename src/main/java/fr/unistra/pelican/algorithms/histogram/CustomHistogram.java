package fr.unistra.pelican.algorithms.histogram;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;

/**
 * Custom size normalized histogram for monochannel data.
 * 
 * @author Erchan Aptoula
 * 
 */
public class CustomHistogram extends Algorithm {

	/**
	 * First input parameter
	 */
	public Image input;

	/**
	 * First optional parameter
	 */
	public boolean normalized;

	/**
	 * Second optional parameter
	 */
	public int size;

	/**
	 * Output parameter
	 */
	public double[] output;

	/**
	 * Constructor
	 * 
	 */
	public CustomHistogram() {

		super();
		super.inputs = "input";
		super.options = "normalized,size";
		super.outputs = "output";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {

		output = new double[size];

		for (int x = 0; x < input.getXDim(); x++)
			for (int y = 0; y < input.getYDim(); y++)
				for (int z = 0; z < input.getZDim(); z++)
					for (int t = 0; t < input.getTDim(); t++)
						output[input.getPixelXYZTByte(x, y, z, t)]++;

		if (normalized)
			for (int i = 0; i < size; i++)
				output[i] /= input.getXDim() * input.getYDim()
						* input.getZDim() * input.getTDim();
	}

	/**
	 * Custom size normalized histogram for monochannel data.
	 * 
	 * @param input
	 *            The image use to create the histogram.
	 * @return An array of double which is the histogram.
	 */
	public static Image exec(Image input) {
		return (Image) new CustomHistogram().process(input);
	}

}
