package fr.unistra.pelican.algorithms.morphology.binary;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.logical.BinaryDifference;

/**
 * Perform a binary open top hat (inputImage - opening_of_inputImage) with a
 * flat structuring element.
 * 
 * @author ?, Jonathan Weber
 */
public class BinaryOpenTopHat extends Algorithm {
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
	public BooleanImage outputImage;
	/**
	 * Constructor
	 * 
	 */
	public BinaryOpenTopHat() {
		super.inputs = "inputImage,se";
		super.outputs = "outputImage";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = (BooleanImage) new BinaryDifference().process(
				inputImage,new BinaryOpening().process(inputImage, se));
	}

	/**
	 * This method perform a binary open top hat (inputImage - opening_of_inputImage) with a
	 * flat structuring element.
	 * @param image image to be processed
	 * @param se structuring element to use
	 * @return top-hat opened picture
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Image> T  exec(T image, BooleanImage se) {
		return (T) new BinaryOpenTopHat().process(image,se);
	}

}
