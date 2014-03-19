package fr.unistra.pelican.algorithms.morphology.gray;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;

/**
 * This class performs an iterative contrast mapping until stability
 * 
 * @author Erchan Aptoula
 */
public class GrayIterativeContrastMapping extends Algorithm {

	/**
	 * The input image
	 */
	public Image input;

	/**
	 * The flat structuring element used in the morphological operation
	 */
	public BooleanImage se;

	/**
	 * The number of iterations
	 */
	public int times;

	/**
	 * The type of operation
	 */
	public int type;

	/**
	 * Constant representing dilation-erosion operations
	 */
	public static final int DilEroBased = 0;

	/**
	 * Constant representing open-close operations
	 */
	public static final int OpenCloseBased = 1;

	/**
	 * The output image
	 */
	public Image output;

	/**
	 * Default constructor
	 */
	public GrayIterativeContrastMapping() {
		super.inputs = "input,se,times,type";
		super.outputs = "output";
		
	}

	/**
	 * Performs an iterative contrast mapping until stability
	 * 
	 * @param input
	 *            The input image
	 * @param se
	 *            The flat structuring element used in the morphological operation
	 * @param times
	 *            The number of iterations
	 * @param type
	 *            The type of operation: DilEroBased or OpenCloseBased
	 * @return The output image
	 */
	public static Image exec(Image input, BooleanImage se, int times, int type) {
		return (Image) new GrayIterativeContrastMapping().process(input, se, times,
				type);
	}

	/*
	 * (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() {
		output = input.copyImage(true);
		if (type != DilEroBased && type != OpenCloseBased)
			throw new AlgorithmException("Invalid contrast mapping type");
		for (int i = 0; i < times; i++) {
			output = GrayContrastMapping.exec(output, se,type);
		}
	}

}
