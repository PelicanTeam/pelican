package fr.unistra.pelican.algorithms.conversion;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;

/**
 * This class realizes the transformation of a tristumulus RGB image into a
 * double valued HSY image with pixels in the interval [0,1]. Thus it is
 * adequate also for visualisation.
 * 
 * http://www.prip.tuwien.ac.at/~hanbury/rgb2hsy.m
 * 
 * this version uses (r+g+b)/3 for brightness
 * 
 *	MASK MANAGEMENT (by Régis) : 
 *	- input's mask becomes output's mask.
 *	- no modification on color calculation.
 * 
 * @author Erchan Aptoula
 * 
 */

public class RGBToHSY2 extends Algorithm {

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
	public RGBToHSY2() {

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

						double[] hsy = convert(R, G, B);

						output.setPixelXYZTBDouble(x, y, z, t, 0, hsy[0]);
						output.setPixelXYZTBDouble(x, y, z, t, 1, hsy[1]);
						output.setPixelXYZTBDouble(x, y, z, t, 2, hsy[2]);
					}
				}
			}
		}
	}

	/**
	 * converts a triplet of rgb in [0,255] into hsy
	 * 
	 * @param r
	 * @param g
	 * @param b
	 * @return the array of hsy values
	 */
	private static double[] convert(int r, int g, int b) {
		// normalise to [0,1]
		double rN = r * 0.003921;
		double gN = g * 0.003921;
		double bN = b * 0.003921;

		double[] hsy = new double[3];

		hsy[0] = hsy[1] = hsy[2] = 0.0;

		// luminance
		hsy[2] = (rN + gN + bN) / 3.0;

		double C1 = rN - 0.5 * gN - 0.5 * bN;
		double C2 = -Math.sqrt(3.0) / 2.0 * gN + Math.sqrt(3.0) / 2.0 * bN;

		// chroma
		double C = Math.sqrt(C1 * C1 + C2 * C2);

		// Hue - attention to double precision
		if (C <= 0.0 && C >= 0.0)
			hsy[0] = 0.0;
		else if (C != 0.0 && C2 <= 0.0)
			hsy[0] = Math.acos(C1 / C);
		else if (C != 0.0 && C2 > 0.0)
			hsy[0] = 2.0 * Math.PI - Math.acos(C1 / C);

		// Saturation
		hsy[1] = Math.max(rN, Math.max(gN, bN))
				- Math.min(rN, Math.min(gN, bN));

		// H to [0,1]
		hsy[0] = hsy[0] / (2.0 * Math.PI);

		return hsy;
	}

//	public static void main(String[] args) {
//		Image img = (Image) new ImageLoader().process("samples/lenna256.png");
//		img = (Image) new RGBToHSY2().process(img);
//		new Viewer2D().process(img, "deneme");
//	}

	/**
	 * This class realizes the transformation of a tristumulus RGB image into a
	 * double valued HSY image with pixels in the interval [0,1]. Thus it is
	 * adequate also for visualisation
	 * 
	 * @param input
	 *            Tristumulus RGB image.
	 * @return A double valued HSY image.
	 */
	public static Image exec(Image input) {
		return (Image) new RGBToHSY2().process(input);
	}
}
