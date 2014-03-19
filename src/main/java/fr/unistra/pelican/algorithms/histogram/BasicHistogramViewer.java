package fr.unistra.pelican.algorithms.histogram;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;

/**
 * Transforms an histogram into an Image in order to visualize it.
 * 
 * @author Sebastien lefevre
 * 
 */
public class BasicHistogramViewer extends Algorithm {

	/**
	 * Input parameter
	 */
	public double[] input;

	/**
	 * (optional) histogram height
	 */
	public int height=256;
	
	/**
	 * Output parameter
	 */
	public Image output;

	/**
	 * Constructor
	 * 
	 */
	public BasicHistogramViewer() {
		super.inputs = "input";
		super.options="height";
		super.outputs = "output";	
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		double max = input[0];
		// Search for the maximum
		for (int i = 1; i < input.length; i++)
			if (max < input[i])
				max = input[i];
		// Image initialisation
		BooleanImage bi = new BooleanImage(input.length, height, 1, 1, 1);
		bi.fill(false);
		// Image generation
		int height2=height-1;
		for (int i = 0; i < input.length; i++)
			for (int j = height2; j >= 0
					&& j >= height2 - (int) (input[i] * height2 / max); j--)
				bi.setPixelXYBoolean(i, j, true);
		output = bi;
	}

	/**
	 * Transforms an histogram into an Image in order to visualize it.
	 * 
	 * @param input
	 *            Histogram to be transformed.
	 * @return A visualisable image from a monochannel histogram.
	 */
	public static BooleanImage exec(double[] input) {
		return (BooleanImage) new BasicHistogramViewer().process(input);
	}

	public static Image exec(double[] input,int height) {
		return (Image) new BasicHistogramViewer().process(input,height);
	}

}
