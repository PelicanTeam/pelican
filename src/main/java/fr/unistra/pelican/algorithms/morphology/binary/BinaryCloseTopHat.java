package fr.unistra.pelican.algorithms.morphology.binary;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.logical.BinaryDifference;

/**
 * Perform a binary close top hat (closing_of_inputImage - inputImage) with a
 * flat structuring element.
 * 
 * @author ?, Jonathan Weber
 */
public class BinaryCloseTopHat extends Algorithm {
	/**
	 * Image to be processed
	 */
	public Image inputImage;

	/**
	 * Structuring element to use
	 */
	public BooleanImage se;

	/**
	 * resulting picture
	 */
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public BinaryCloseTopHat() {
		super.inputs = "inputImage,se";
		super.outputs = "outputImage";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = (Image) new BinaryDifference().process(
				new BinaryClosing().process(inputImage, se), inputImage);
	}

	/**
	 * This method perform a binary close top hat (closing_of_inputImage - inputImage) with a
	 * flat structuring element.
	 * @param image image to be processed
	 * @param se structuring element to use
	 * @return top-hat closed picture
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Image> T  exec(T image, BooleanImage se) {
		return (T) new BinaryCloseTopHat().process(image,se);
	}

}
