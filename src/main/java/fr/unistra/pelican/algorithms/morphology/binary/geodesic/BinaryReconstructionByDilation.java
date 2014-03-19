package fr.unistra.pelican.algorithms.morphology.binary.geodesic;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.arithmetic.Equal;

/**
 * Performs a binary reconstruction by dilation with a structuring element and a
 * mask.
 * 
 * @author ?, Jonathan Weber
 */
public class BinaryReconstructionByDilation extends Algorithm {
	
	
	/**
	 * Image to be processed
	 */
	public Image inputImage;
	
	/**
	 * Mask
	 */
	public Image mask;
	
	/**
	 * Structuring Element used for the geodesic dilation
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
	 * Set if a trace must be printed in the console
	 */
	public static boolean trace = false;

	/**
	 * Constructor
	 * 
	 */
	public BinaryReconstructionByDilation() {
		super.inputs = "inputImage,mask,se";
		super.options = "option,trace";
		super.outputs = "outputImage";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		BooleanImage tmp = new BooleanImage(inputImage, true);
			int iteration = 0;

			do {
				outputImage = tmp;
				tmp = (BooleanImage) new BinaryGeodesicDilation().process(outputImage, mask, se,
						option);
				if (trace)
					System.out.println("iteration : " + ++iteration);
			} while (!Equal.exec(outputImage, tmp));
	}

	/**
	 * This method Performs a binary reconstruction by dilation with a structuring element and a
	 * mask.
	 * @param input image to process
	 * @param mask mask to use for geodsic dilation
	 * @param se structuring element to use for geodesic dilation
	 * @return recontructed image
	 */
	public static BooleanImage exec(Image input,Image mask, BooleanImage se)
	{
		return (BooleanImage) new BinaryReconstructionByDilation().process(input, mask, se);
	}

}
