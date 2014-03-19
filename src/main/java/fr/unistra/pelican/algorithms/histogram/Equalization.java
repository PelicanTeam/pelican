package fr.unistra.pelican.algorithms.histogram;

import java.util.Arrays;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;

/**
 * Produce a channel independent histogram equalization...no support for Z and T
 * dimensions.
 * 
 * @author
 * 
 */
public class Equalization extends Algorithm {

	/**
	 * Input parameter.
	 */
	public Image input;

	/**
	 * Output parameter.
	 */
	public Image output;

	/**
	 * Constructor
	 * 
	 */
	public Equalization() {

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
		try {
			output = input.copyImage(false);

			double[][] histo = (double[][]) new MultivaluedHistogram().process(
					input, new Boolean(true));
			double[] val = new double[256];

			for (int b = 0; b < input.getBDim(); b++) {
				Arrays.fill(val, 0.0);

				for (int i = 0; i < 256; i++) {
					double tmp = 0.0;

					for (int j = 0; j <= i; j++)
						tmp += histo[b][j];

					val[i] = tmp * 255;
				}

				for (int x = 0; x < input.getXDim(); x++) {
					for (int y = 0; y < input.getYDim(); y++) {
						int index = input.getPixelXYBByte(x, y, b);
						output.setPixelXYBByte(x, y, b, (int) Math
								.round(val[index]));
					}
				}
			}
		} catch (PelicanException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Produce a channel independent histogram equalization...no support for Z
	 * and T dimensions.
	 * 	 
	 * @param input
	 * 			The original image.
	 * @return the channel independent histogram equalization.
	 */
	public static Image exec(Image input) {
		return (Image) new Equalization().process(input);
	}
}
