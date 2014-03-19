package fr.unistra.pelican.algorithms.conversion;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;

/**
 * This class realizes the transformation of a tristumulus double valued CIE LCH
 * image into a CIE LAB image.
 * 
 *	MASK MANAGEMENT (by Régis) : 
 *	- input's mask becomes output's mask.
 *	- no modification on color calculation.
 * 
 * @author Erchan Aptoula
 * 
 */

public class HCLToLAB extends Algorithm {

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
	public HCLToLAB() {

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
					"The input must be a tristumulus XYZ image");

		output = new DoubleImage(xdim, ydim, zdim, tdim, bdim);
		this.output.setMask( this.input.getMask() );
		output.setColor(true);

		for (int x = 0; x < xdim; x++) {
			for (int y = 0; y < ydim; y++) {
				for (int z = 0; z < zdim; z++) {
					for (int t = 0; t < tdim; t++) {
						double H = input.getPixelXYZTBDouble(x, y, z, t, 0);
						double C = input.getPixelXYZTBDouble(x, y, z, t, 1);
						double L = input.getPixelXYZTBDouble(x, y, z, t, 2);

						double[] lab = convert(H, C, L);

						output.setPixelXYZTBDouble(x, y, z, t, 0, lab[0]);
						output.setPixelXYZTBDouble(x, y, z, t, 1, lab[1]);
						output.setPixelXYZTBDouble(x, y, z, t, 2, lab[2]);
					}
				}
			}
		}
	}

	/**
	 * converts a triplet of hcl into cielab
	 * 
	 * @param h
	 * @param c
	 * @param l
	 * @return the array of lab values
	 */
	public static double[] convert(double h, double c, double l) {
		double[] lab = new double[3];

		lab[0] = l;

		h = h * 2 * Math.PI; // from 0,1 -> degrees -> radians

		lab[1] = Math.cos(h) * c;
		lab[2] = Math.sin(h) * c;

		return lab;
	}

	/**
	 * Realizes the transformation of a tristumulus double valued CIE
	 * LCH image into a CIE LAB image.
	 * 
	 * @param input
	 *            Tristumulus double valued CIE LCH image.
	 * @return CIE LAB image.
	 */
	public static Image exec(Image input) {
		return (Image) new HCLToLAB().process(input);
	}
}