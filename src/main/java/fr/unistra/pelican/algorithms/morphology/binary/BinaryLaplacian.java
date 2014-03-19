package fr.unistra.pelican.algorithms.morphology.binary;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.logical.BinaryDifference;

/**
 * This class performs a binary laplacian with a flat structuring element.
 * 
 * @author ?, Jonathan Weber
 */
public class BinaryLaplacian extends Algorithm {
	
	/**
	 * Image to be processed
	 */
	public Image inputImage;

	
	/**
	 * structuring element to use
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
	public BinaryLaplacian() {
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
					new BinaryExternGradient().process(inputImage, se),
					new BinaryInternGradient().process(inputImage, se));
	}

	/**
	 * This method performs a binary laplacian with a flat structuring element.
	 * @param image image to be processed
	 * @param se structuring element
	 * @return binary laplacian
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Image> T  exec(T inputImage, BooleanImage se) {
		return (T) new BinaryLaplacian().process(inputImage,se);
	}
	
	
	
}
