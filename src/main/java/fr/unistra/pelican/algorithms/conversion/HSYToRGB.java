package fr.unistra.pelican.algorithms.conversion;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;

/**
 * This class realizes the transformation of a tristumulus double valued HSY
 * image with pixels in [0,1], into a byte valued RGB image.
 * 
 *	MASK MANAGEMENT (by Régis) : 
 *	- input's mask becomes output's mask.
 *	- no modification on color calculation.
 * 
 * @author Erchan Aptoula
 * 
 */

public class HSYToRGB extends Algorithm {

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
	public HSYToRGB() {

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
					"The input must be a tristumulus HSY image");

		output = new ByteImage(xdim, ydim, zdim, tdim, bdim);
		this.output.setMask( this.input.getMask() );
		output.setColor(true);

		for (int x = 0; x < xdim; x++) {
			for (int y = 0; y < ydim; y++) {
				for (int z = 0; z < zdim; z++) {
					for (int t = 0; t < tdim; t++) {
						double H = input.getPixelXYZTBDouble(x, y, z, t, 0);
						double S = input.getPixelXYZTBDouble(x, y, z, t, 1);
						double Y = input.getPixelXYZTBDouble(x, y, z, t, 2);

						int[] rgb = convert(H, S, Y);

						output.setPixelXYZTBByte(x, y, z, t, 0, rgb[0]);
						output.setPixelXYZTBByte(x, y, z, t, 1, rgb[1]);
						output.setPixelXYZTBByte(x, y, z, t, 2, rgb[2]);
					}
				}
			}
		}
	}

	/**
	 * converts a triplet of hsy into rgb
	 * 
	 * @param h
	 * @param s
	 * @param y
	 * @return the array of rgb values
	 */
	public static int[] convert(double H, double S, double Y) {
		int[] rgb = new int[3];

		double R, G, B;
		R = G = B = 0.0;

		// H to radians
		H = H * Math.PI * 2.0;

		// chroma
		int k = (int) Math.floor(H / (Math.PI / 3.0));
		double Hstar = H - k * (Math.PI / 3.0);
		double C = (Math.sqrt(3.0) * S)
				/ (2.0 * Math.sin(2.0 * Math.PI / 3.0 - Hstar));

		double C1 = C * Math.cos(H);
		double C2 = -1 * C * Math.sin(H);

		R = 1.0 * Y + 0.701 * C1 + 0.27308667732669306 * C2;
		G = 1.0 * Y - 0.299 * C1 - 0.3042635918629327 * C2;
		B = 1.0 * Y - 0.299 * C1 + 0.8504369465163188 * C2;

		rgb[0] = (int) Math.round(R * 255);
		if (rgb[0] > 255)
			rgb[0] = 255;
		else if (rgb[0] < 0)
			rgb[0] = 0;

		rgb[1] = (int) Math.round(G * 255);
		if (rgb[1] > 255)
			rgb[1] = 255;
		else if (rgb[1] < 0)
			rgb[1] = 0;

		rgb[2] = (int) Math.round(B * 255);
		if (rgb[2] > 255)
			rgb[2] = 255;
		else if (rgb[2] < 0)
			rgb[2] = 0;

		return rgb;
	}

	/**
	 * Realizes the transformation of a tristumulus double valued HSY
	 * image with pixels in [0,1], into a byte valued RGB image.
	 * 
	 * @param input
	 *            Tristumulus double valued HSY image with pixels in [0,1].
	 * @return Byte valued RGB image.
	 */
	public static Image exec(Image input) {
		return (Image) new HSYToRGB().process(input);
	}
}