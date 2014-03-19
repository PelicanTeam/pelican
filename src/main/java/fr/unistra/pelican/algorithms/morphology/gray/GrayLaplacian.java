package fr.unistra.pelican.algorithms.morphology.gray;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.arithmetic.AbsoluteDifference;

/**
 * This class performs a gray laplacian (extern - intern gradient)
 * with a 2-D flat structuring element
 * 
 * @author
 */
public class GrayLaplacian extends Algorithm {

	/**
	 * The input image
	 */
	public Image inputImage;

	/**
	 * The flat structuring element used in the morphological operation
	 */
	public BooleanImage se;

	/**
	 * The output image
	 */
	public Image outputImage;

	/**
	 * Default constructor
	 */
	public GrayLaplacian() {
		super.inputs = "inputImage,se";
		super.outputs = "outputImage";
	}

	/**
	 * Performs a gray laplacian (extern - intern gradient) with a 2-D flat
	 * structuring element
	 * 
	 * @param inputImage
	 *            The input image
	 * @param se
	 *            The flat structuring element used in the morphological
	 *            operation
	 * @return The output image
	 */
	public static Image exec(Image inputImage, BooleanImage se) {
		return (Image) new GrayLaplacian().process(inputImage, se);
	}

	/*
	 * (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() {
		outputImage = AbsoluteDifference.exec(GrayExternGradient.exec(inputImage,se),GrayInternGradient.exec(inputImage, se));
	}
	
}
