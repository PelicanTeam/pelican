package fr.unistra.pelican.algorithms.morphology.binary.hitormiss;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.arithmetic.Inversion;
import fr.unistra.pelican.algorithms.logical.AND;
import fr.unistra.pelican.algorithms.morphology.binary.BinaryErosion;

/**
 * This class realizes the binary hit or miss transform of the input. The result
 * is a boolean image. The composite structuring element is decomposed to two
 * simple flat structuring elements one for the background and one for the
 * foreground.
 * 
 * @author ?, Jonathan Weber
 */
public class BinaryHitOrMiss extends Algorithm {
	/**
	 * Image to be processed
	 */
	public Image input;

	/**
	 * Structuring Element used with the foreground
	 */
	public BooleanImage seFG;

	/**
	 * Structuring Element used with the background
	 */
	public BooleanImage seBG;

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
	public BooleanImage output;

	/**
	 * Constructor
	 * 
	 */
	public BinaryHitOrMiss() {
		super.inputs = "input,seFG,seBG";
		super.options = "option";
		super.outputs = "output";

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() {

		Image fg = BinaryErosion.exec(input, seFG, option);
		Image bg = BinaryErosion.exec(Inversion.exec(input), seBG, option);

		output = (BooleanImage) AND.exec(fg, bg);
	}

	/**
	 * This method realizes the binary hit or miss transform of the input.
	 * 
	 * @param input
	 *          image to process
	 * @param se
	 *          structuring element to use
	 * @return image with objects that have matched pattern defined by se
	 */
	public static BooleanImage exec(Image input, BooleanImage seFG,
		BooleanImage seBG) {
		return (BooleanImage) new BinaryHitOrMiss().process(input, seFG, seBG);
	}

}