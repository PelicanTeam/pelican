package fr.unistra.pelican.algorithms.arithmetic;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;

/**
 * Divison by a constant or an array of constants for multichannel data
 * 
 * 
 * @author E.A., Benjamin Perret
 * 
 */
public class Division extends Algorithm
{
	/**
	 * Input image
	 */
	public Image input;

	/**
	 * Input dividers
	 */
	public double[] dividerArray = null;

	/**
	 * Result of division
	 */
	public Image output;

	/**
	 * Constructor
	 * 
	 */
	public Division() {

		super();
		super.inputs = "input,dividerArray";
		super.outputs = "output";
	
}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		output = input.copyImage(false);
		output.setMask( input.getMask() );

		for (int b = 0; b < input.getBDim(); b++) {
			double div = dividerArray[b];

			for (int x = 0; x < input.getXDim(); x++) {
				for (int y = 0; y < input.getYDim(); y++) {

					int tmp = (int) Math.floor( input.getPixelXYBByte(x, y, b) / div );
					output.setPixelXYBByte(x, y, b, tmp);
				}
			}
		}
	}
	
	/**
	 * Divison by a constant or an array of constants for multichannel data
	 *
	 * @param input Input image
	 * @param dividerArray Array of dividers
	 * @return Divided image
	 */
	public static Image exec(Image input, double [] dividerArray) {
		return (Image) new Division().process(input,
				dividerArray);
	}
}
