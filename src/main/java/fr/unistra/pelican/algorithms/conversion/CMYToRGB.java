package fr.unistra.pelican.algorithms.conversion;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;

/**
 *	This class realizes the transformation of a tristumulus double valued CMY
 *	image into a byte valued RGB image.
 *
 *	MASK MANAGEMENT (by RÃ©gis) : 
 *	- input's mask becomes output's mask.
 *	- no modification on color calculation.
 * 
 * @author Erchan Aptoula
 * 
 */

public class CMYToRGB extends Algorithm {

	/**
	 * Input parameter.
	 */
	public Image input;

	/**
	 * output parameter.
	 */
	public Image output;

	/**
	 * Constructor
	 * 
	 */
	public CMYToRGB() {

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
		output.setColor(true);

		for (int x = 0; x < xdim; x++) {
			for (int y = 0; y < ydim; y++) {
				for (int z = 0; z < zdim; z++) {
					for (int t = 0; t < tdim; t++) {
						double C = input.getPixelXYZTBDouble(x, y, z, t, 0);
						double M = input.getPixelXYZTBDouble(x, y, z, t, 1);
						double Y = input.getPixelXYZTBDouble(x, y, z, t, 2);

						output.setPixelXYZTBByte(x, y, z, t, 0, (int) Math
								.round((1 - C) * 255.0));
						output.setPixelXYZTBByte(x, y, z, t, 1, (int) Math
								.round((1 - M) * 255.0));
						output.setPixelXYZTBByte(x, y, z, t, 2, (int) Math
								.round((1 - Y) * 255.0));
					}
				}
			}
		}

		this.output.setMask( this.input.getMask() );
	}

	/**
	 * Realizes the transformation of a tristumulus double valued CMY image into
	 * a byte valued RGB image.
	 * 
	 * @param input Tristumulus double valued CMY image
	 * @return Byte valued RGB image.
	 */
	public static Image exec(Image input) {
		return (Image) new CMYToRGB().process(input);
	}
}