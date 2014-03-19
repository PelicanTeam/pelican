package fr.unistra.pelican.algorithms.conversion;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;

/**
 * This class realizes the transformation of a tristumulus double valued I1I2I3
 * image into a byte valued RGB image.
 * 
 *	MASK MANAGEMENT (by Régis) : 
 *	- input's mask becomes output's mask.
 *	- no modification on color calculation.
 * 
 * @author Erchan Aptoula
 * 
 */

public class I1I2I3ToRGB extends Algorithm {

	/**
	 * Input parameter.
	 */
	public Image input;

	/**
	 * Output parameter.
	 */
	public Image output;

	/**
	 * Constructor
	 * 
	 */
	public I1I2I3ToRGB() {

		super();
		super.inputs = "input";
		super.outputs = "output";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		int xdim = input.getXDim();
		int ydim = input.getYDim();
		int zdim = input.getZDim();
		int tdim = input.getTDim();
		int bdim = input.getBDim();

		if (bdim != 3)
			throw new AlgorithmException(
					"The input must be a tristumulus YIQ image");

		output = new ByteImage(xdim, ydim, zdim, tdim, bdim);
		this.output.setMask( this.input.getMask() );
		output.setColor(true);

		for (int x = 0; x < xdim; x++) {
			for (int y = 0; y < ydim; y++) {
				for (int z = 0; z < zdim; z++) {
					for (int t = 0; t < tdim; t++) {
						double I1 = input.getPixelXYZTBDouble(x, y, z, t, 0);
						double I2 = input.getPixelXYZTBDouble(x, y, z, t, 1);
						double I3 = input.getPixelXYZTBDouble(x, y, z, t, 2);

						I2 = I2 - 0.5;
						I3 = I3 - 0.5;

						double R = I1 + I2 - (2.0 / 3.0) * I3;

						double G = I1 + (4.0 / 3.0) * I3;

						double B = I1 - I2 - (2.0 / 3.0) * I3;

						output.setPixelXYZTBByte(x, y, z, t, 0, (int) Math
								.round(R * 255.0));
						output.setPixelXYZTBByte(x, y, z, t, 1, (int) Math
								.round(G * 255.0));
						output.setPixelXYZTBByte(x, y, z, t, 2, (int) Math
								.round(B * 255.0));
					}
				}
			}
		}
	}

	/**
	 * Realizes the transformation of a tristumulus double valued
	 * I1I2I3 image into a byte valued RGB image.
	 * 
	 * @param input
	 *            Tristumulus double valued I1I2I3 image.
	 * @return Byte valued RGB image.
	 */
	public static Image exec(Image input) {
		return (Image) new I1I2I3ToRGB().process(input);
	}
}