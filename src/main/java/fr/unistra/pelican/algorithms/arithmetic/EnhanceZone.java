package fr.unistra.pelican.algorithms.arithmetic;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;

/**
 * Enhance a zone of an image with a mask. 
 * outputImage[i] = 
 * 		inputImage[i] * 0.4 if mask[i] == false 
 * 		inputImage[i] otherwise
 * The outputImage format is the same as inputImage1.
 * 
 * @author Sebastien Derivaux, Benjamin Perret
 */
public class EnhanceZone extends Algorithm {

	/**
	 * Input image
	 */
	public Image inputImage;

	/**
	 * Mask of interesting area
	 */
	public Image maskImage;

	/**
	 * Enhanced Image
	 */
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public EnhanceZone() {

		super();
		super.inputs = "inputImage,maskImage";
		super.outputs = "outputImage";

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = inputImage.copyImage(true);
		for (int x = 0; x < inputImage.getXDim(); x++)
			for (int y = 0; y < inputImage.getYDim(); y++) {
				boolean enhance = maskImage.getPixelXYBoolean(x, y);
				if(!enhance && inputImage.isPresentXY(x,y) )
					for (int b = 0; b < inputImage.getBDim(); b++) {
						outputImage.setPixelXYBDouble(x, y, b, inputImage.getPixelXYBDouble(x, y, b) * 0.25);
					}
			}
	}

	/**
	 * Enhance a zone of an image with a mask. 
	 * outputImage[i] = 
	 * 		inputImage[i] * 0.4 if mask[i] == false 
	 * 		inputImage[i] otherwise
	 * The outputImage format is the same as inputImage1.
	 * 
	 * @param inputImage Input Image
	 * @param maskImage Mask of interesting area
	 * @return enhanced image
	 */
	public static Image exec(Image inputImage, Image maskImage) {
		return (Image) new EnhanceZone().process(inputImage,
				maskImage);
	}
}
