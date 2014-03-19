package fr.unistra.pelican.algorithms.morphology.binary.geodesic;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.arithmetic.Minimum;
import fr.unistra.pelican.algorithms.morphology.binary.BinaryDilation;

/**
 * This class performs a binary geodesic dilation with a structuring element and a mask.
 * 
 * @author ?, Jonathan Weber
 */
public class BinaryGeodesicDilation extends Algorithm {
	/**
	 * Image to be processed
	 */
	public Image inputImage;
	
	/**
	 * Mask
	 */
	public Image mask;
	
	/**
	 * Structuring Element used for the geodesin dilation
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
	public BinaryGeodesicDilation() {
		super.inputs = "inputImage,mask,se";
		super.options="option";
		super.outputs = "outputImage";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = new BooleanImage(inputImage, false);

		try {
			outputImage = BinaryDilation.exec(inputImage, se, option);
			outputImage = Minimum.exec(outputImage, mask);
		} catch (PelicanException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method performs a binary geodesic dilation with a structuring element and a mask.
	 * @param input image to be processed
	 * @param mask mask for the geodesic operation
	 * @param se structuring element
	 * @return geodesicly dilated image
	 */
	public static BooleanImage exec(Image input, Image mask, BooleanImage se)
	{
		return (BooleanImage) new BinaryGeodesicDilation().process(input,mask,se);
	}
}
