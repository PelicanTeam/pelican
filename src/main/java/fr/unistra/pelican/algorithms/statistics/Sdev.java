package fr.unistra.pelican.algorithms.statistics;

import java.util.Arrays;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;

/**
 * This class computes the standard deviation of each channel
 * 
 * @author Abdullah
 */
public class Sdev extends Algorithm
{
	/**
	 * The original image
	 */
	public Image original;

	/**
	 * the resulting standard deviations
	 */
	public Double[] output;
	
	/**
	 * This method computes the standard deviation of each channel of the given image
	 * @param original The original image
	 * @return the standard deviations of each channel
	 */
	public static Double[] exec(Image original)
	{
		return (Double[]) new Sdev().process(original);
	}

	/**
	 * Constructor
	 * 
	 */
	public Sdev() {

		super();
		super.inputs = "original";
		super.outputs = "output";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		try {

			int bdim = original.getBDim();
			int xdim = original.getXDim();
			int ydim = original.getYDim();

			output = new Double[bdim];
			Arrays.fill(output, 0.0);

			Double[] averages = (Double[]) new Average().process(original);

			for (int b = 0; b < bdim; b++) {
				for (int x = 0; x < xdim; x++) {
					for (int y = 0; y < ydim; y++) {
						double tmp = averages[b]
								- original.getPixelXYBDouble(x, y, b);
						output[b] += tmp * tmp;
					}
				}
				output[b] = Math.sqrt(output[b] / (xdim * ydim));
			}
		} catch (PelicanException ex) {
			ex.printStackTrace();
		}
	}
}
