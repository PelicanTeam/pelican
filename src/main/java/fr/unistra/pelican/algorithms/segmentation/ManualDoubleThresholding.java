package fr.unistra.pelican.algorithms.segmentation;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;

/**
 * This class performs an image binarization relying on a double given thresholding
 * value.
 * 
 * Works on all formats.
 * 
 * @author Jonathan Weber
 */
public class ManualDoubleThresholding extends Algorithm {

	/**
	 * The input image
	 */
	public Image inputImage;

	/**
	 * The minimum threshold value
	 */
	public Number minThreshold;

	/**
	 * The maximum threshold value
	 */
	public Number maxThreshold;
	
	/**
	 * The output image
	 */
	public BooleanImage outputImage;

	/**
	 * Constructor
	 */
	public ManualDoubleThresholding() {
		super.inputs = "inputImage,minThreshold,maxThreshold";
		super.outputs = "outputImage";
	}

	/**
	 * Performs an image binarization relying on a double given thresholding value
	 * 
	 * @param image
	 *            The input image
	 * @param minThreshold
	 *            The minimum threshold value
	 * @param maxThreshold
	 *            The maximum threshold value
	 * @return The output image
	 */
	public static BooleanImage exec(Image image, Number minThreshold, Number maxThreshold) {
		return (BooleanImage) new ManualDoubleThresholding()
				.process(image, minThreshold, maxThreshold);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = new BooleanImage(inputImage, false);

		if (minThreshold instanceof Integer && inputImage instanceof ByteImage)
			for (int i = 0; i < inputImage.size(); i++)
				if (inputImage.getPixelByte(i) >= (Integer) minThreshold  && inputImage.getPixelByte(i) <= (Integer) maxThreshold)
					outputImage.setPixelBoolean(i, true);
				else
					outputImage.setPixelBoolean(i, false);
		else if (minThreshold instanceof Integer)
			for (int i = 0; i < inputImage.size(); i++)
				if (inputImage.getPixelInt(i) >= (Integer) minThreshold && inputImage.getPixelInt(i) <= (Integer) maxThreshold)
					outputImage.setPixelBoolean(i, true);
				else
					outputImage.setPixelBoolean(i, false);
		else if (minThreshold instanceof Double)
			for (int i = 0; i < inputImage.size(); i++)
				if (inputImage.getPixelDouble(i) >= (Double) minThreshold && inputImage.getPixelDouble(i) <= (Double) maxThreshold)
					outputImage.setPixelBoolean(i, true);
				else
					outputImage.setPixelBoolean(i, false);
		else
			throw new AlgorithmException("Binarisation error with parameters"
					+ inputImage + " and " + minThreshold + " and " + maxThreshold);
	}

}
