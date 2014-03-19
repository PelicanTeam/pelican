package fr.unistra.pelican.algorithms.conversion;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;

/**
 * This class realizes the transformation of a tristumulus double valued CIE LAB
 * image into a CIE LCH image.
 * 
 *	MASK MANAGEMENT (by Régis) : 
 *	- input's mask becomes output's mask.
 *	- no modification on color calculation.
 * 
 * @author Erchan Aptoula
 * 
 */

public class LABToHCL extends Algorithm {

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
	public LABToHCL() {

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
					"The input must be a tristumulus CIE LAB image");

		output = new DoubleImage(xdim, ydim, zdim, tdim, bdim);
		this.output.setMask( this.input.getMask() );
		output.setColor(true);

		for (int x = 0; x < xdim; x++) {
			for (int y = 0; y < ydim; y++) {
				for (int z = 0; z < zdim; z++) {
					for (int t = 0; t < tdim; t++) {
						double L = input.getPixelXYZTBDouble(x, y, z, t, 0);
						double A = input.getPixelXYZTBDouble(x, y, z, t, 1);
						double B = input.getPixelXYZTBDouble(x, y, z, t, 2);

						double[] hcl = convert(L, A, B);

						output.setPixelXYZTBDouble(x, y, z, t, 0, hcl[0]);
						output.setPixelXYZTBDouble(x, y, z, t, 1, hcl[1]);
						output.setPixelXYZTBDouble(x, y, z, t, 2, hcl[2]);
					}
				}
			}
		}
	}

	/**
	 * converts a triplet of cielab into lch
	 * 
	 * @param l
	 * @param a
	 * @param b
	 * @return the array of lch values
	 */
	private static double[] convert(double l, double a, double b) {
		double[] hcl = new double[3];

		hcl[2] = l;

		hcl[0] = Math.atan2(b, a);

		if (hcl[0] > 0.0)
			hcl[0] = (hcl[0] / Math.PI) * 180.0;
		else
			hcl[0] = 360.0 - (Math.abs(hcl[0]) / Math.PI) * 180.0;

		hcl[1] = Math.sqrt(a * a + b * b);

		hcl[0] = hcl[0] / 360.0;

		return hcl;
	}

	/**
	 * This class realizes the transformation of a tristumulus double valued CIE
	 * LAB image into a CIE LCH image.
	 * 
	 * @param input
	 *            Tristumulus double valued CIE LAB image.
	 * @return CIE LCH image.
	 */
	public static Image exec(Image input) {
		return (Image) new LABToHCL().process(input);
	}
}