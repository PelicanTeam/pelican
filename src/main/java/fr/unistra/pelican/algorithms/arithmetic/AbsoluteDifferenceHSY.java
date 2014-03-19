package fr.unistra.pelican.algorithms.arithmetic;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.InvalidParameterException;
import fr.unistra.pelican.util.mask.MaskStack;

/**
 * Compute the absolute difference between two images
 * (inputImage1 - inputImage2) in the HSY space.
 * The outputImage format is the same as inputImage1.
 *
 *	Mask management (by witz) :
 *		- computation occurs as usual.
 *		- the the output image mask is inputImage1 mask ANDed with inputImage2 mask
 * 
 * @author  ?, Benjamin Perret
 */
public class AbsoluteDifferenceHSY extends Algorithm {

	/**
	 * First input image in HSY space.
	 */
	public Image inputImage1;

	/**
	 * Second input image in HSY space.
	 */
	public Image inputImage2;

	/**
	 * Algorithm result: absolute difference between input image one and two.
	 */
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public AbsoluteDifferenceHSY() {

		super();
		super.inputs = "inputImage1,inputImage2";
		super.outputs = "outputImage";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		if(!Image.haveSameDimensions(inputImage1, inputImage2))
			throw(new InvalidParameterException("The two images must have the same dimensions"));
		outputImage = inputImage1.copyImage(false);
		MaskStack mask = new MaskStack( MaskStack.AND );
		mask.push( inputImage1.getMask() );
		mask.push( inputImage2.getMask() );
		outputImage.setMask( mask );

		for (int b = 0; b < inputImage1.getBDim(); b++) {
			for (int t = 0; t < inputImage1.getTDim(); t++) {
				for (int z = 0; z < inputImage1.getZDim(); z++) {
					for (int x = 0; x < inputImage1.getXDim(); x++) {
						for (int y = 0; y < inputImage1.getYDim(); y++) {
							if (b > 0)
								outputImage.setPixelXYZTBDouble(x, y, z, t, b,
										Math.abs(inputImage1
												.getPixelXYZTBDouble(x, y, z,
														t, b)
												- inputImage2
														.getPixelXYZTBDouble(x,
																y, z, t, b)));
							else {
								double abs = Math.abs(inputImage1
										.getPixelXYZTBDouble(x, y, z, t, b)
										- inputImage2.getPixelXYZTBDouble(x, y,
												z, t, b));

								if (abs > 0.5)
									abs = 1.0 - abs;

								abs *= 2.0; // get into [0,1]

								outputImage.setPixelXYZTBDouble(x, y, z, t, b,
										abs);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Compute the absolute difference between two images
	 * (inputImage1 and inputImage2) in the HSY space.
	 * 
	 * @param inputImage1
	 *            First of the two subtracted images.
	 * @param inputImage2
	 *            Second of the two subtracted images.
	 * @return outputImage which is the absolute difference between two images
	 *         in the HSY space.
	 */
	public static Image exec(Image inputImage1, int inputImage2) {
		return (Image) new AbsoluteDifferenceHSY().process(inputImage1,
				inputImage2);
	}
}
