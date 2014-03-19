package fr.unistra.pelican.algorithms.morphology.binary;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 * Performs a binary opening (erosion then dilation) with a flat structuring
 * element.
 * 
 * @author ?, Jonathan Weber
 */
public class BinaryOpening extends Algorithm {
	/**
	 * Image to be processed
	 */
	public Image inputImage;

	/**
	 * Structuring Element used for the opening
	 */
	public BooleanImage se;

	/**
	 * option for considering out-of-image pixels
	 */
	public Integer option = IGNORE;

	/**
	 * Constant for ignoring out-of-image pixels
	 */
	public static final int IGNORE = 0;

	/**
	 * Constant for setting to white out-of-image pixels
	 */
	public static final int WHITE = 1;

	/**
	 * Constant for setting to black out-of-image pixels
	 */
	public static final int BLACK = 2;

	/**
	 * Resulting picture
	 */
	public Image outputImage;


	/**
	 * Constructor
	 * 
	 */
	public BinaryOpening() {
		super.inputs = "inputImage,se";
		super.options = "option";
		super.outputs = "outputImage";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = (Image) new BinaryErosion().process(inputImage, se, option);
		outputImage = (Image) new BinaryDilation().process(outputImage, FlatStructuringElement2D.reflect(se), option);
	}

	/**
	 * Performs a binary opening with a flat structuring element
	 * @param image Image to be processed
	 * @param se Structuring element
	 * @return opened picture
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Image> T  exec(T image, BooleanImage se) {
		return (T) new BinaryOpening().process(image,se);
	}

	/**
	 * Performs a binary opening with a flat structuring element
	 * @param image Image to be processed
	 * @param se Structuring element
	 * @param option how to consider out-of-image pixels
	 * @return opened picture
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Image> T  exec(T image, BooleanImage se,
			Integer option) {
		return (T) new BinaryOpening().process(image,se,option);
	}
}