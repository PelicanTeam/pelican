package fr.unistra.pelican.algorithms.histogram;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;

/**
 * 
 * Produce a saturation weighted histogram using the 8 RGB cube edge colours +
 * grey.
 * 
 * @author Erchan Aptoula
 * 
 */
public class QuantizedColourHistogram extends Algorithm {

	/**
	 * Input parameter
	 */
	public Image input;

	/**
	 * Optionnal input parameter.
	 */
	public boolean normalized;

	/**
	 * Output parameter.
	 */
	public double[] output;

	/**
	 * Constructor
	 * 
	 */
	public QuantizedColourHistogram() {

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
		int size = 6 + 5; // 6 hues and 5 achromat?c levels

		output = new double[size];

		double saturationSum = 0.0;
		double achromaticSum = 0.0;

		for (int x = 0; x < input.getXDim(); x++) {
			for (int y = 0; y < input.getYDim(); y++) {
				double sat = input.getPixelXYBDouble(x, y, 1);
				double hue = input.getPixelXYBDouble(x, y, 0);
				double lum = input.getPixelXYBDouble(x, y, 2);

				double coeff = 1 / (1 + Math.exp(-10 * (sat - 0.5)));
				saturationSum += coeff;

				achromaticSum += 1 - coeff;

				// determine the hue..
				// red
				if (hue >= 330.0 / 360.0 || hue < 30.0 / 360.0)
					output[0] += coeff;

				// yellow
				else if (hue >= 30.0 / 360.0 && hue < 90.0 / 360.0)
					output[1] += coeff;

				// green
				else if (hue >= 90.0 / 360.0 && hue < 150.0 / 360.0)
					output[2] += coeff;

				// cyan
				else if (hue >= 150.0 / 360.0 && hue < 210.0 / 360.0)
					output[3] += coeff;

				// blue
				else if (hue >= 210.0 / 360.0 && hue < 270.0 / 360.0)
					output[4] += coeff;

				// magenta
				else
					output[5] += coeff;

				// and now white grey and black
				// depending on the luminosity

				// black
				if (lum < 0.20)
					output[6] += 1 - coeff;

				// grey - 1
				else if (lum >= 0.20 && lum < 0.40)
					output[7] += 1 - coeff;

				// grey - 2
				else if (lum >= 0.40 && lum < 0.60)
					output[8] += 1 - coeff;

				// grey - 3
				else if (lum >= 0.60 && lum < 0.80)
					output[9] += 1 - coeff;

				// white
				else
					output[10] += 1 - coeff;

			}
		}

		if (normalized == true) {
			for (int i = 0; i < 6; i++)
				output[i] = output[i] / saturationSum;

			for (int i = 6; i < 11; i++)
				output[i] = output[i] / achromaticSum;
		}
	}

	/**
	 * Produce a saturation weighted histogram using the 8 RGB cube edge colours +
	 * grey.
	 * 
	 * @param image
	 *            Image to be histogrammed
	 * @param normalized
	 *            The histogram will be normalized or not.
	 * @return result The saturation weighted histogram
	 */
	public static double[] exec(Image input, boolean normalized) {
		return (double[]) new QuantizedColourHistogram().process(input,
				normalized);
	}

	/**
	 * Produce a saturation weighted histogram using the 8 RGB cube edge colours +
	 * grey.
	 * 
	 * @param image
	 *            Image to be histogrammed
	 * @return result The saturation weighted histogram
	 */
	public static double[] exec(Image input) {
		return (double[]) new QuantizedColourHistogram().process(input);
	}

}
