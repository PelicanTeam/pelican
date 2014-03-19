package fr.unistra.pelican.algorithms.statistics;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.conversion.RGBToHSY2;
import fr.unistra.pelican.util.Tools;

/**
 * This class computes the mean squared error of hue It accepts a RGB colour
 * image, its filtered as well as noisy versions.
 * 
 * @author Abdullah
 */
public class HueMSE extends Algorithm {
	/**
	 * The original image
	 */
	public Image original;

	/**
	 * The filtered image
	 */
	public Image filtered;

	/**
	 * The noisy image
	 */
	public Image noisy;

	/**
	 * (Optional) Flag for an input image already coded in HLS or similar space
	 * (hue band first)
	 */
	public boolean inputHLS = false;

	/**
	 * the hue error
	 */
	public Double output;

	/**
	 * This class computes the mean squared error of hue.
	 * 
	 * @param original
	 *          the original image
	 * @param filtered
	 *          the filtered image
	 * @param noisy
	 *          the noisy image
	 * @return the hue mean squared error
	 */
	public static Double exec(Image original, Image filtered, Image noisy) {
		return (Double) new HueMSE().process(original, filtered, noisy);
	}

	/**
	 * This class computes the mean squared error of hue.
	 * 
	 * @param original
	 *          the original image
	 * @param filtered
	 *          the filtered image
	 * @param noisy
	 *          the noisy image
	 * @param inputHLS
	 *          flag for an input image already coded in HLS or similar space (hue
	 *          band first)
	 * @return the hue mean squared error
	 */
	public static Double exec(Image original, Image filtered, Image noisy,
		boolean inputHLS) {
		return (Double) new HueMSE().process(original, filtered, noisy, inputHLS);
	}

	/**
	 * Constructor
	 * 
	 */
	public HueMSE() {
		super.inputs = "original,filtered,noisy";
		super.options = "inputHLS";
		super.outputs = "output";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		int bDim = original.getBDim();
		int xDim = original.getXDim();
		int yDim = original.getYDim();

		int bDim2 = filtered.getBDim();
		int xDim2 = filtered.getXDim();
		int yDim2 = filtered.getYDim();

		if (!inputHLS) {
			original = (Image) new RGBToHSY2().process(original);
			filtered = (Image) new RGBToHSY2().process(filtered);
			noisy = (Image) new RGBToHSY2().process(noisy);
		}

		if (bDim != bDim2 || xDim != xDim2 || yDim != yDim2)
			throw new AlgorithmException("The input images must have same dimensions");

		double sum1 = 0.0;
		double sum2 = 0.0;

		for (int x = 0; x < xDim; x++) {
			for (int y = 0; y < yDim; y++) {
				double ho = original.getPixelXYBDouble(x, y, 0);
				double hf = filtered.getPixelXYBDouble(x, y, 0);
				double hn = noisy.getPixelXYBDouble(x, y, 0);

				sum1 += Tools.hueDistance(ho, hf);
				sum2 += Tools.hueDistance(ho, hn);
			}
		}

		output = new Double(sum1 / sum2);
	}
}
