package fr.unistra.pelican.algorithms.conversion;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;

/**
 * This class realizes the transformation of a tristumulus RGB image into a
 * double valued CMY image.
 * 
 *	MASK MANAGEMENT (by Régis) : 
 *	- input's mask becomes output's mask.
 *	- no modification on color calculation.
 * 
 * @author Erchan Aptoula
 * 
 */

public class RGBToCMY extends Algorithm {

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
	public RGBToCMY() {

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
					"The input must be a tristumulus RGB image");

		output = new DoubleImage(xdim, ydim, zdim, tdim, bdim);
		this.output.setMask( this.input.getMask() );
		output.setColor(true);

		for (int x = 0; x < xdim; x++) {
			for (int y = 0; y < ydim; y++) {
				for (int z = 0; z < zdim; z++) {
					for (int t = 0; t < tdim; t++) {
						int R = input.getPixelXYZTBByte(x, y, z, t, 0);
						int G = input.getPixelXYZTBByte(x, y, z, t, 1);
						int B = input.getPixelXYZTBByte(x, y, z, t, 2);

						// normalise to [0,1]
						double rN = R * 0.003921;
						double gN = G * 0.003921;
						double bN = B * 0.003921;

						output.setPixelXYZTBDouble(x, y, z, t, 0, 1 - rN);
						output.setPixelXYZTBDouble(x, y, z, t, 1, 1 - gN);
						output.setPixelXYZTBDouble(x, y, z, t, 2, 1 - bN);
					}
				}
			}
		}
	}

	/**
	 * This class realizes the transformation of a tristumulus RGB image into a
	 * double valued CMY image.
	 * 
	 * @param input
	 *            Tristumulus RGB image.
	 * @return Double valued CMY image.
	 */
	public static Image exec(Image input) {
		return (Image) new RGBToCMY().process(input);
	}
}