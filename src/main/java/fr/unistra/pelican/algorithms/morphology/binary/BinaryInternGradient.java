package fr.unistra.pelican.algorithms.morphology.binary;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.logical.BinaryDifference;

/**
 * Performs a binary intern gradient with a flat structuring element.
 * 
 * @author ?, Jonathan Weber
 */
public class BinaryInternGradient extends Algorithm {
	/**
	 * Image to be processed
	 */
	public Image inputImage;

	/**
	 * Structuring element to use
	 */
	public BooleanImage se;

	/**
	 * Resulting picture
	 */
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public BinaryInternGradient() {
		super.inputs = "inputImage,se";
		super.outputs = "outputImage";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = (Image)new  BinaryDifference().process(
				inputImage,new BinaryErosion().process(inputImage, se));
	}

	/**
	 * This method performs a binary intern gradient with a flat structuring element.
	 * @param image image to be processed
	 * @param se structuring element to use
	 * @return intern gradient picture
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Image> T  exec(T inputImage, BooleanImage se) {
		return (T) new BinaryInternGradient().process(inputImage,se);
	}

}
