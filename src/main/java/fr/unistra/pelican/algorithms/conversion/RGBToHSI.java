package fr.unistra.pelican.algorithms.conversion;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;

/**
 * This class realizes the transformation of a tristumulus RGB image into a
 * double valued HSI image with pixels in the interval [0,1]. Thus it is
 * adequate also for visualisation.
 * 
 *	MASK MANAGEMENT (by Régis) : 
 *	- input's mask becomes output's mask.
 *	- no modification on color calculation.
 * 
 * @author Erchan Aptoula
 * 
 */

public class RGBToHSI extends Algorithm {

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
	public RGBToHSI() {

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

						double H, S, I;

						double min = rN;

						if (gN < min)
							min = gN;
						if (bN < min)
							min = bN;

						S = 1 - 3 * min / (rN + gN + bN);
						I = (rN + gN + bN) / 3;

						double tmp = Math.sqrt((rN - gN) * (rN - gN)
								+ (rN - bN) * (gN - bN));

						double tmp2 = 0.5 * (rN - gN + rN - bN) / tmp;

						// nasty bug..dont forget the precision problems of
						// Math.*;
						if (tmp2 > 1.0)
							tmp2 = 1.0;
						else if (tmp2 < -1.0)
							tmp2 = -1.0;

						double theta = Math.acos(tmp2);

						if (bN <= gN)
							H = theta;
						else
							H = 2 * Math.PI - theta;

						H = H / (2 * Math.PI);

						output.setPixelXYZTBDouble(x, y, z, t, 0, H);
						output.setPixelXYZTBDouble(x, y, z, t, 1, S);
						output.setPixelXYZTBDouble(x, y, z, t, 2, I);
					}
				}
			}
		}
	}

	/**
	 * This class realizes the transformation of a tristumulus RGB image into a
	 * double valued HSI image with pixels in the interval [0,1]. Thus it is
	 * adequate also for visualisation.
	 * 
	 * @param input
	 *            Tristumulus RGB image.
	 * @return A double valued HSI image.
	 */
	public static Image exec(Image input) {
		return (Image) new RGBToHSI().process(input);
	}
}