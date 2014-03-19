package fr.unistra.pelican.algorithms.conversion;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;

/**
 * This class realizes the transformation of a tristumulus RGB image into a
 * double valued YIQ image with pixel values eventually outside of the [0,1]
 * interval. Consequently scaling is necessary before any visualisation attempt
 * of the result.
 * 
 *	MASK MANAGEMENT (by Régis) : 
 *	- input's mask becomes output's mask.
 *	- no modification on color calculation.
 * 
 * @author Erchan Aptoula
 * 
 */

public class RGBToYIQ extends Algorithm {

	/**
	 * Input parameter
	 */
	public Image input;

	/**
	 * Output parameter
	 */
	public Image output;

	/**
	 * Constructor
	 * 
	 */
	public RGBToYIQ() {

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

						double Y = 0.299 * rN + 0.587 * gN + 0.114 * bN;
						double I = 0.596 * rN - 0.275 * gN - 0.321 * bN;
						double Q = 0.212 * rN - 0.523 * gN + 0.311 * bN;

						output.setPixelXYZTBDouble(x, y, z, t, 0, Y);
						output.setPixelXYZTBDouble(x, y, z, t, 1, I);
						output.setPixelXYZTBDouble(x, y, z, t, 2, Q);
					}
				}
			}
		}
	}

	/**
	 * This class realizes the transformation of a tristumulus RGB image into a
	 * double valued YIQ image with pixel values eventually outside of the [0,1]
	 * interval. Consequently scaling is necessary before any visualisation
	 * attempt of the result.
	 * 
	 * @param input
	 *            Tristumulus RGB image.
	 * @return A double valued YIQ image.
	 */
	public static Image exec(Image input) {
		return (Image) new RGBToYIQ().process(input);
	}
}