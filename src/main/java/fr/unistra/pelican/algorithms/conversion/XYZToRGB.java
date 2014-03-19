package fr.unistra.pelican.algorithms.conversion;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;

/**
 * This class realizes the transformation of a tristumulus double valued CIE XYZ
 * image into a byte valued RGB image.
 * 
 *	MASK MANAGEMENT (by Régis) : 
 *	- input's mask becomes output's mask.
 *	- no modification on color calculation.
 * 
 * @author Erchan Aptoula
 * 
 */

public class XYZToRGB extends Algorithm {

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
	public XYZToRGB() {

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
						double X = input.getPixelXYZTBDouble(x, y, z, t, 0);
						double Y = input.getPixelXYZTBDouble(x, y, z, t, 1);
						double Z = input.getPixelXYZTBDouble(x, y, z, t, 2);

						int[] rgb = convert(X, Y, Z);

						output.setPixelXYZTBByte(x, y, z, t, 0, rgb[0]);
						output.setPixelXYZTBByte(x, y, z, t, 1, rgb[1]);
						output.setPixelXYZTBByte(x, y, z, t, 2, rgb[2]);
					}
				}
			}
		}
	}

	/**
	 * converts a triplet of xyz into rgb
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return the array of rgb values
	 */
	public static int[] convert(double X, double Y, double Z) {
		int[] rgb = new int[3];

		// RGB cannot contain ALL colors, so anything outside [0,255] is black
		// for full bijectivity between spaces sRGB is necessary
		rgb[0] = (int) Math
				.round((3.240479 * X - 1.53715 * Y - 0.498535 * Z) * 255);
		if (rgb[0] < 0)
			rgb[0] = 0;
		else if (rgb[0] > 255)
			rgb[0] = 255;

		rgb[1] = (int) Math
				.round((-0.969256 * X + 1.875992 * Y + 0.041556 * Z) * 255);
		if (rgb[1] < 0)
			rgb[1] = 0;
		else if (rgb[1] > 255)
			rgb[1] = 255;

		rgb[2] = (int) Math
				.round((0.055648 * X - 0.204043 * Y + 1.057311 * Z) * 255);
		if (rgb[2] < 0)
			rgb[2] = 0;
		else if (rgb[2] > 255)
			rgb[2] = 255;

		return rgb;
	}

	/**
	 * This class realizes the transformation of a tristumulus double valued CIE
	 * XYZ image into a byte valued RGB image.
	 * 
	 * @param input
	 *            Tristumulus double valued CIE XYZ image.
	 * @return A byte valued RGB image.
	 */
	public static Image exec(Image input) {
		return (Image) new XYZToRGB().process(input);
	}
}