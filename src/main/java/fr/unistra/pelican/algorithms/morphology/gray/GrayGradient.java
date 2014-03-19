package fr.unistra.pelican.algorithms.morphology.gray;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.arithmetic.Difference;

/**
 * This class performs a gray gradient (dilation - erosion) with a 2-D flat
 * structuring element
 * 
 * @author
 */
public class GrayGradient extends Algorithm {

	/**
	 * The input image
	 */
	public Image inputImage;

	/**
	 * The flat structuring element used in the morphological operation
	 */
	public BooleanImage se;
	
	/**
	 * A mask to limit the computing to a specified area
	 */
	public BooleanImage mask=null;

	/**
	 * The output image
	 */
	public Image outputImage;

	/**
	 * Default constructor
	 */
	public GrayGradient() {
		super.inputs = "inputImage,se";
		super.options = "mask";
		super.outputs = "outputImage";	
	}

	/**
	 * Performs a gray gradient (dilation - erosion) with a 2-D flat
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
		return (Image) new GrayGradient().process(inputImage, se);
	}
	
	/**
	 * Performs a gray gradient (dilation - erosion) with a 2-D flat
	 * structuring element
	 * 
	 * @param inputImage
	 *            The input image
	 * @param se
	 *            The flat structuring element used in the morphological
	 *            operation
	 * @param mask
	 * 			Mask used to only compute a part of the image 
	 * @return The output image
	 */
	public static Image exec(Image inputImage, BooleanImage se, BooleanImage mask) {
		return (Image) new GrayGradient().process(inputImage, se, mask);
	}


	/*
	 * (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() {
		outputImage = Difference.exec(
				GrayDilation.exec(inputImage,se, mask),
				GrayErosion.exec(inputImage, se, mask)
				);
	}
	
}
