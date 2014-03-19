package fr.unistra.pelican.algorithms.histogram;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;

/**
 * Shrinks the contrast of an image so that difference between minimal and
 * maximal intensities within the image are made closer and contained in only a
 * subset of the initial range.
 * 
 * @author lefevre
 * 
 */
public class ContrastShrink extends Algorithm {

	/**
	 * Input parameter.
	 */
	public Image inputImage;

	/**
	 * Ratio of output range vs initial range
	 */
	public Double ratio;

	/**
	 * Output parameter.
	 */
	public Image outputImage;

	/**
	 * Constructor.
	 * 
	 */
	public ContrastShrink() {
		super.inputs = "inputImage,ratio";
		super.outputs = "outputImage";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		// Always work with double.
		// find the extremities
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;

		outputImage = inputImage.copyImage(false);

		for (int i = 0; i < inputImage.size(); i++) {
			double c = inputImage.getPixelDouble(i);
			if (c < min)
				min = c;
			if (c > max)
				max = c;
		}

		// if the range is null, do nothing
		if (min == max) {
			outputImage = inputImage.copyImage(true);
			return;
		}

		// Do it now
		double median = (max + min) / 2;
		for (int i = 0; i < inputImage.size(); i++)
			outputImage.setPixelDouble(i,
					(inputImage.getPixelDouble(i) - median) * ratio + median);
	}

	/**
	 * Shrinks the contrast of an image so that difference between minimal and
	 * maximal intensities within the image are made closer and contained in
	 * only a subset of the initial range. *
	 * 
	 * @param inputImage
	 *            Image to be shrunk.
	 * @param ratio
	 *            shrinking ratio
	 * @return The contrast shrunk image.
	 */
	public static Image exec(Image inputImage, double ratio) {
		return (Image) new ContrastShrink().process(inputImage, ratio);
	}

}
