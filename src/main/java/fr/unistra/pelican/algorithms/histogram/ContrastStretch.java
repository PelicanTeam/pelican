package fr.unistra.pelican.algorithms.histogram;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;

/**
 * Stretch the contrast of an image so that the minimal intensity pixel (in
 * double format) is 0.0 and the maximal intensity is 1.0.
 * 
 * It works on image that have initial range outside [0.0;1.0].
 * 
 * This algorithm use the darker and brighter pixel in all bands, frames and
 * temporality (?) for this image and apply the same stretch factor for all
 * pixels.
 * 
 * @author
 * 
 */
public class ContrastStretch extends Algorithm {

	/**
	 * Input parameter.
	 */
	public Image inputImage;

	/**
	 * Output parameter.
	 */
	public Image outputImage;

	/**
	 * Constructor.
	 * 
	 */
	public ContrastStretch() {

		super();
		super.inputs = "inputImage";
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

		// if the range is null, throw an exception
		if (min == max) {
			// throw new AlgorithmException("Range size is null, can't stretch
			// the contrast.");
			outputImage = inputImage.copyImage(true);
			return;
		}

		// Do it now
		double factor = 1.0 / (max - min);
		for (int i = 0; i < inputImage.size(); i++)
			outputImage.setPixelDouble(i, (inputImage.getPixelDouble(i) - min)
					* factor);
	}

	/**
	 * Stretch the contrast of an image so that the minimal intensity pixel (in
	 * double format) is 0.0 and the maximal intensity is 1.0.
	 * 
	 * @param inputImage
	 * 			Image to be stretched.
	 * @return The contrast stretched image.
	 */
	public static Image exec(Image inputImage) {
		return (Image) new ContrastStretch().process(inputImage);
	}

}
