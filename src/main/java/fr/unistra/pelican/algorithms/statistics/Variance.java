package fr.unistra.pelican.algorithms.statistics;

import java.util.Arrays;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;

/**
 * This class computes the variance of each channel of the given image.
 * It accepts a single argument and returns an array of Double.
 * 
 * @author Weber
 */
public class Variance extends Algorithm
{
	/**
	 * The input image
	 */
	public Image original;

	/**
	 * The already pre-computed mean if already computed
	 */
	public Double[] mean=null;
	
	/**
	 * The resulting averages
	 */
	public Double[] output;
	
	/**
	 * This class computes the variance of each channel of the given image
	 * @param original the input image
	 * @return an array containing the variance of each channel
	 */
	public static Double[] exec(Image original)
	{
		return (Double[]) new Variance().process(original);
	}
	
	/**
	 * This class computes the variance of each channel of the given image
	 * @param original the input image
	 * @return an array containing the variance of each channel
	 */
	public static Double[] exec(Image original, Double[] mean)
	{
		return (Double[]) new Variance().process(original,mean);
	}

	/**
	 * Constructor
	 * 
	 */
	public Variance() {

		super();
		super.inputs = "original";
		super.options = "mean";
		super.outputs = "output";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		if(mean==null)
		{
			mean=Average.exec(original);
		}		
		int bDim = original.getBDim();
		output = new Double[bDim];
		Arrays.fill(output, 0.);
		int[] bPixels = new int[bDim];
		Arrays.fill(bPixels, 0);
		double val;
		for(int b=0;b<bDim;b++)
		{
			for(int i=b;i<original.size();i=i+bDim)
			{
				if(original.isInMask(i))
				{
					val = original.getPixelDouble(i)-mean[b];
					output[b] += (val*val);
					bPixels[b]++;
				}
			}
			output[b] = output[b] / bPixels[b];
		}
	}
}