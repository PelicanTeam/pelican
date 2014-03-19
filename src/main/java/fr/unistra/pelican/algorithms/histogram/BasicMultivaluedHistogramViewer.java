package fr.unistra.pelican.algorithms.histogram;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;

/**
 * Transforms a multivalued histogram into an Image in order to visualize it.
 * 
 * @author Sebastien lefevre
 * 
 */
public class BasicMultivaluedHistogramViewer extends Algorithm {

	/**
	 * Input parameter.
	 */
	public double[][] input;

	/**
	 * Output parameter.
	 */
	public Image output;

	/**
	 * Constructor.
	 * 
	 */
	public BasicMultivaluedHistogramViewer() {

		super();
		super.inputs = "input";
		super.outputs = "output";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		int height = 256;
		double max[] = new double[input.length];
		// Process each channel
		for (int b = 0; b < input.length; b++) {
			max[b] = input[b][0];
			// Search for the maximum
			for (int i = 1; i < input[0].length; i++)
				if (max[b] < input[b][i])
					max[b] = input[b][i];
		}
		// Image initialisation
		BooleanImage bi = new BooleanImage(input[0].length, height, 1, 1,
				input.length);
		bi.fill(false);
		// Image generation
		for (int b = 0; b < input.length; b++)
			for (int i = 0; i < input[b].length; i++)
				for (int j = height - 1; j >= 0
						&& j >= 255 - (int) (input[b][i] * 255) / max[b]; j--)
					bi.setPixelXYBBoolean(i, j, b, true);
		output = bi;
	}
	
	/**
	 * Transforms a multivalued histogram into an Image in order to visualize it.
	 * 
	 * @param input
	 *            Multivalued histogram to be transformed.
	 * @return A visualisable image from a monochannel histogram.
	 */
	public static Image exec(double[] input) {
		return (Image) new BasicMultivaluedHistogramViewer().process(input);
	}

}
