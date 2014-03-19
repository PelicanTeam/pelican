package fr.unistra.pelican.algorithms.segmentation;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;

/**
 * This class performs an image binarization relying on a given thresholding
 * value.
 * 
 * Works on all formats.
 * 
 * @author Sebastien Derivaux
 */
public class ManualThresholding extends Algorithm {

	/**
	 * The input image
	 */
	public Image inputImage;

	/**
	 * The threshold value
	 */
	public Number threshold;

	/**
	 * The output image
	 */
	public BooleanImage outputImage;

	/**
	 * Constructor
	 */
	public ManualThresholding() {
		super.inputs = "inputImage,threshold";
		super.outputs = "outputImage";
	}

	/**
	 * Performs an image binarization relying on a given thresholding value
	 * 
	 * @param image
	 *            The input image
	 * @param threshold
	 *            The threshold value
	 * @return The output image
	 */
	public static BooleanImage exec(Image image, Number threshold) {
		return (BooleanImage) new ManualThresholding()
				.process(image, threshold);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = new BooleanImage(inputImage, false);
		if (threshold instanceof Integer && inputImage instanceof ByteImage)
			for (int i = 0; i < inputImage.size(); i++)
				if (inputImage.getPixelByte(i) >= (Integer) threshold)
					outputImage.setPixelBoolean(i, true);
				else
					outputImage.setPixelBoolean(i, false);
		else if (threshold instanceof Integer)
			for (int i = 0; i < inputImage.size(); i++)
				if (inputImage.getPixelInt(i) >= (Integer) threshold)
					outputImage.setPixelBoolean(i, true);
				else
					outputImage.setPixelBoolean(i, false);
		else if (threshold instanceof Double)
			for (int i = 0; i < inputImage.size(); i++)
				if (inputImage.getPixelDouble(i) >= (Double) threshold)
					outputImage.setPixelBoolean(i, true);
				else
					outputImage.setPixelBoolean(i, false);
		else
			throw new AlgorithmException("Binarisation error with parameters"
					+ inputImage + " and " + threshold);
	}

}
