package fr.unistra.pelican.algorithms.conversion;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;

/**
 * This class realizes the transformation of a tristumulus double valued HSL
 * image with pixels in [0,1], into a byte valued RGB image.
 * 
 *	MASK MANAGEMENT (by Régis) : 
 *	- input's mask becomes output's mask.
 *	- no modification on color calculation.
 * 
 * @author Erchan Aptoula
 * 
 */

public class HSLToRGB extends Algorithm {

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
	public HSLToRGB() {

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
					"The input must be a tristumulus HSV image");

		output = new ByteImage(xdim, ydim, zdim, tdim, bdim);
		this.output.setMask( this.input.getMask() );
		output.setColor(true);

		for (int x = 0; x < xdim; x++) {
			for (int y = 0; y < ydim; y++) {
				for (int z = 0; z < zdim; z++) {
					for (int t = 0; t < tdim; t++) {
						double H = input.getPixelXYZTBDouble(x, y, z, t, 0);
						double S = input.getPixelXYZTBDouble(x, y, z, t, 1);
						double L = input.getPixelXYZTBDouble(x, y, z, t, 2);

						double R, G, B;

						if (S >= 0.0 && S <= 0.0) { // doubles are tricky in
							// equality tests..
							R = G = B = L; // hue undefined

						} else if (L >= 0.0 && L <= 0.0) {
							R = G = B = 0.0; // hue and saturation undefined

						} else {
							double tmp1, tmp2;

							if (L < 0.5)
								tmp2 = L * (1 + S);
							else
								tmp2 = L + S - (L * S);

							tmp1 = 2 * L - tmp2;

							R = Hue_2_RGB(tmp1, tmp2, H + 1.0 / 3.0);
							G = Hue_2_RGB(tmp1, tmp2, H);
							B = Hue_2_RGB(tmp1, tmp2, H - 1.0 / 3.0);

						}

						output.setPixelXYZTBByte(x, y, z, t, 0, (int) Math
								.round(R * 255));
						output.setPixelXYZTBByte(x, y, z, t, 1, (int) Math
								.round(G * 255));
						output.setPixelXYZTBByte(x, y, z, t, 2, (int) Math
								.round(B * 255));
					}
				}
			}
		}
	}

	private double Hue_2_RGB(double v1, double v2, double v3) {
		if (v3 < 0.0)
			v3 += 1;
		if (v3 > 1.0)
			v3 -= 1;
		if (6 * v3 < 1.0)
			return v1 + (v2 - v1) * 6 * v3;
		if (2 * v3 < 1.0)
			return v2;
		if (3 * v3 < 2.0)
			return v1 + (v2 - v1) * ((2.0 / 3.0) - v3) * 6;

		return v1;
	}

	/**
	 * Realizes the transformation of a tristumulus double valued HSL
	 * image with pixels in [0,1], into a byte valued RGB image.
	 * 
	 * @param input
	 *            tristumulus double valued HSL image with pixels in [0,1].
	 * @return Byte valued RGB image.
	 */
	public static Image exec(Image input) {
		return (Image) new HSLToRGB().process(input);
	}
}