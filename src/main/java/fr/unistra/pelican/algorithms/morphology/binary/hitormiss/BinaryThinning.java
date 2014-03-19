package fr.unistra.pelican.algorithms.morphology.binary.hitormiss;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.logical.BinaryDifference;
import fr.unistra.pelican.util.morphology.CompositeStructuringElement;

/**
 * This class realizes the binary thinning of the input. The result is a boolean
 * image.
 * 
 * @author weber, lefevre
 */
public class BinaryThinning extends Algorithm {
	/**
	 * Image to be processed
	 */
	public Image input;

	/**
	 * Resulting picture
	 */
	public Image output;

	/**
	 * structuring element to use for the foreground
	 */
	public BooleanImage seFG;

	/**
	 * structuring element to use for the background
	 */
	public BooleanImage seBG;

	/**
	 * Constructor
	 * 
	 */
	public BinaryThinning() {
		super.inputs = "input,seFG,seBG";
		super.outputs = "output";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() {
		Image hitOrMiss = BinaryHitOrMiss.exec(input, seFG, seBG);
		output = BinaryDifference.exec(input, hitOrMiss);
	}

	/**
	 * This method realizes the binary thinning of the input.
	 * 
	 * @param Input
	 *          image to be processed
	 * @param SE
	 *          structuring element to use
	 * @return thinned picture
	 */
	public static BooleanImage exec(Image input, BooleanImage seFG,
		BooleanImage seBG) {
		return (BooleanImage) new BinaryThinning().process(input, seFG, seBG);
	}
}