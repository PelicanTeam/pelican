package fr.unistra.pelican.algorithms.morphology.binary.geodesic;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.arithmetic.Equal;

/**
 * Performs a binary reconstruction by erosion with a flat structuring element
 * and a mask.
 * 
 * @author ?, Jonathan Weber
 */
public class BinaryReconstructionByErosion extends Algorithm {
	/**
	 * Image to be processed
	 */
	public Image inputImage;
	
	/**
	 * Mask
	 */
	public Image mask;
	
	/**
	 * Structuring Element used for the geodesic erosion
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
	public BooleanImage outputImage;
	
	/**
	 * Set if a trace must be printed in the console
	 */
	public static boolean trace = false;
	
	/**
	 * Constructor
	 * 
	 */
	public BinaryReconstructionByErosion() {
		super.inputs = "inputImage,mask,se";
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
				tmp = (BooleanImage) new BinaryGeodesicErosion().process(outputImage, mask, se, option);
				if(trace)
					System.out.println("iteration : " + ++iteration);
			} while (!Equal.exec(outputImage, tmp));
	}

	/**
	 * This method Performs a binary reconstruction by erosion with a structuring element and a
	 * mask.
	 * @param Input image to process
	 * @param Mask mask to use for geodsic erosion
	 * @param SE structuring element to use for geodesic erosion
	 * @return recontructed image
	 */
	public static BooleanImage exec(Image input,Image mask, BooleanImage se)
	{
		return (BooleanImage) new BinaryReconstructionByErosion().process(input, mask, se);
	}
}
