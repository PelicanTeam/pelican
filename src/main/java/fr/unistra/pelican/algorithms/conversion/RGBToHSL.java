package fr.unistra.pelican.algorithms.conversion;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;

/**
 * This class realizes the transformation of a tristumulus RGB image into a
 * double valued HSL image with pixels in the interval [0,1]. Thus it is
 * adequate also for visualisation.
 * 
 * ...One of the "worst" colour spaces out there..
 * 
 *	MASK MANAGEMENT (by Régis) : 
 *	- input's mask becomes output's mask.
 *	- no modification on color calculation.
 * 
 * @author Erchan Aptoula
 * 
 */

public class RGBToHSL extends Algorithm {

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
	public RGBToHSL() {

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
					"The input image must be a tristumulus RGB image");

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

						double H, S, L;

						H = S = L = 0.0;

						double min = rN;
						if (gN < min)
							min = gN;
						if (bN < min)
							min = bN;

						double max = rN;
						if (gN > max)
							max = gN;
						if (bN > max)
							max = bN;

						double delta = max - min;

						L = (max + min) * 0.5;

						if (delta >= 0.0 && delta <= 0.0) {
							H = S = 0.0;
						} else {
							if (L < 0.5)
								S = delta / (max + min);
							else
								S = delta / (2 - max - min);
						}

						double _R = (((max - rN) / 6.0) + delta * 0.5) / delta;
						double _G = (((max - gN) / 6.0) + delta * 0.5) / delta;
						double _B = (((max - bN) / 6.0) + delta * 0.5) / delta;

						if (rN == max)
							H = _B - _G;
						else if (gN == max)
							H = 1.0 / 3.0 + _R - _B;
						else if (bN == max)
							H = 2.0 / 3.0 + _G - _R;

						if (H < 0.0)
							H += 1.0;
						if (H > 1.0)
							H -= 1.0;

						output.setPixelXYZTBDouble(x, y, z, t, 0, H);
						output.setPixelXYZTBDouble(x, y, z, t, 1, S);
						output.setPixelXYZTBDouble(x, y, z, t, 2, L);
					}
				}
			}
		}
	}

	/**
	 * This class realizes the transformation of a tristumulus RGB image into a
	 * double valued HSL image with pixels in the interval [0,1]. Thus it is
	 * adequate also for visualisation.
	 * 
	 * @param input
	 *            Tristumulus RGB image.
	 * @return A double valued HSL image.
	 */
	public static Image exec(Image input) {
		return (Image) new RGBToHSL().process(input);
	}

}