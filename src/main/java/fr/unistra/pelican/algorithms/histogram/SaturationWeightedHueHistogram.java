package fr.unistra.pelican.algorithms.histogram;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.Tools;

/**
 * Custom size saturation weighted hue histogram
 * 
 * @author Erchan Aptoula
 * 
 */
public class SaturationWeightedHueHistogram extends Algorithm {

	/**
	 * Input parameter
	 */
	public Image input;

	/**
	 * 
	 * First optionnal input parameter.
	 */
	public int size;

	/**
	 * 
	 * First optionnal input parameter:
	 * The interval of values taken by saturation
	 */
	public int saturationInterval; 

	/**
	 * Output parameter.
	 */
	public double[] output;

	/**
	 * Constructor
	 * 
	 */
	public SaturationWeightedHueHistogram() {

		super();
		super.inputs = "input";
		super.options = "size,saturationInterval";
		super.outputs = "output";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		output = new double[size];

		double sum = 0.0;

		for (int x = 0; x < input.getXDim(); x++) {
			for (int y = 0; y < input.getYDim(); y++) {
				int hue = input.getPixelXYBByte(x, y, 0);
				double sat = input.getPixelXYBByte(x, y, 1);
				//double lum = input.getPixelXYBByte(x, y, 2);

				sat = (double) sat / (double) (saturationInterval - 1);
				// lum = (double)lum / (double)(saturationInterval - 1);

				double tmp = Tools.saturationWeightedHue(sat);

				output[hue] += tmp;
				sum += tmp;
			}
		}

		for (int i = 0; i < size; i++)
			output[i] /= sum;
	}

	/**
	 * Custom size saturation weighted hue histogram.
	 * 
	 * @param image Image to be histogrammed.
	 * @param size 
	 * @param saturationInterval The interval of values taken by saturation
	 * @return The custom size saturation weighted hue histogram
	 */
	public static double[] exec(Image input, Integer size,
			Integer saturationInterval) {
		return (double[]) new SaturationWeightedHueHistogram().process(input,size,saturationInterval);
	}

	/**
	 * Custom size saturation weighted hue histogram.
	 * 
	 * @param image Image to be histogrammed.
	 * @returnThe custom size saturation weighted hue histogram
	 */
	public static double[] exec(Image input) {
		return (double[]) new SaturationWeightedHueHistogram().process(input);
	}
}
