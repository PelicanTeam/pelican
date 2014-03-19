package fr.unistra.pelican.algorithms.statistics;

import java.util.Arrays;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;

/**
 * This class computes the average of each channel of the given image.
 * It accepts a single argument and returns an array of Double.
 * 
 * @author Abdullah, Weber
 */
public class Average extends Algorithm
{
	/**
	 * The input image
	 */
	public Image original;

	/**
	 * The resulting averages
	 */
	public Double[] output;
	
	/**
	 * This class computes the average of each channel of the given image
	 * @param original the input image
	 * @return an array containing the average of each channel
	 */
	public static Double[] exec(Image original)
	{
		return (Double[]) new Average().process(original);
	}

	/**
	 * Constructor
	 * 
	 */
	public Average() {

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
		int bDim = original.getBDim();
		output = new Double[bDim];	
		Arrays.fill(output, 0.);
		int[] bPixels = new int[bDim];
		Arrays.fill(bPixels, 0);
		for(int b=0;b<bDim;b++)
		{
			for(int i=b;i<original.size();i=i+bDim)
			{
				if(original.isInMask(i))
				{
					output[b] += original.getPixelDouble(i);
					bPixels[b]++;
				}
			}
			output[b] = output[b] / bPixels[b];
		}
	}
}
