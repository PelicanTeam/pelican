package fr.unistra.pelican.algorithms.histogram;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;

/**
 * Computes the normalized histogram of a monoband dataset
 * 
 * @author Sebastien lefevre
 * 
 * TODO : compute the appropriate histogram size
 */
public class Histogram extends Algorithm {

	
	/**
	 * Input parameter.
	 */
	public Image input;

	/**
	 * Optionnal output parameter which indicates if the histogram will be
	 * normalized.
	 */
	public Boolean normalized=false;

	/**
	 * Output parameter.
	 */
	public double[] output;

	/**
	 * Constructor
	 * 
	 */
	public Histogram() {

		super();
		super.inputs = "input";
		super.options = "normalized";
		super.outputs = "output";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		// TODO : compute the appopriate histogram size
		int size = 256;
		output = new double[size];
		for (int i = 0; i < size; i++)
			output[i] = 0.0;
		int nbPixels=0;
		for (int x = 0; x < input.getXDim(); x++)
			for (int y = 0; y < input.getYDim(); y++)
				for (int z = 0; z < input.getZDim(); z++)
					for (int t = 0; t < input.getTDim(); t++)
						if(this.input.isPresentXYZT( x,y,z,t ))
						{
							output[input.getPixelXYZTByte(x, y, z, t)]++;
							nbPixels++;
						}
		if (normalized)
			for (int i = 0; i < size; i++)
				output[i] /= nbPixels;
	}

	/**
	 * Static fonction that use this algorithm.
	 * 
	 * Computes the normalized histogram of a monoband dataset.
	 * 
	 * @param image
	 *            Image to be converted in a histogram.
	 * @param normalized
	 *            Indicates if the histogram will be normalized.
	 * @return result The normalized histogram
	 */
	public static double[] exec(Image input, boolean normalized) {
		return (double[]) new Histogram().process(input, normalized);
	}

	/**
	 * Static fonction that use this algorithm.
	 * 
	 * Computes the normalized histogram of a monoband dataset.
	 * 
	 * @param image
	 *            Image to be converted in a histogram.
	 * @return result The normalized histogram
	 */
	public static double[] exec(Image input) {
		return (double[]) new Histogram().process(input);
	}

}
