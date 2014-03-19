package fr.unistra.pelican.algorithms.conversion;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;

/**
 * This class realizes the transformation of a tristumulus double valued CIE LAB
 * image into a CIE XYZ image.
 * 
 *	MASK MANAGEMENT (by Régis) : 
 *	- input's mask becomes output's mask.
 *	- no modification on color calculation.
 * 
 * @author Erchan Aptoula
 * 
 */

public class LABToXYZ extends Algorithm {

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
	public LABToXYZ() {

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

						double[] xyz = convert(L, A, B);

						output.setPixelXYZTBDouble(x, y, z, t, 0, xyz[0]);
						output.setPixelXYZTBDouble(x, y, z, t, 1, xyz[1]);
						output.setPixelXYZTBDouble(x, y, z, t, 2, xyz[2]);
					}
				}
			}
		}
	}

	/**
	 * converts a triplet of cielab into xyz
	 * 
	 * @param l
	 * @param a
	 * @param b
	 * @return the array of xyz values
	 */
	public static double[] convert(double L, double A, double B) {
		// THE white point
		double Xn = 0.950456;
		double Yn = 1.0;
		double Zn = 1.088754;

		double delta = 6.0 / 29.0;

		double fy = (L + 16.0) / 116.0;
		double fx = fy + A / 500.0;
		double fz = fy - B / 200.0;

		double[] xyz = new double[3];

		if (fy > delta)
			xyz[1] = Yn * Math.pow(fy, 3);
		else
			xyz[1] = (fy - 16.0 / 116.0) * 3 * delta * delta * Yn;

		if (fx > delta)
			xyz[0] = Xn * Math.pow(fx, 3);
		else
			xyz[0] = (fx - 16.0 / 116.0) * 3 * delta * delta * Xn;

		if (fz > delta)
			xyz[2] = Zn * Math.pow(fz, 3);
		else
			xyz[2] = (fz - 16.0 / 116.0) * 3 * delta * delta * Zn;

		return xyz;
	}

	/**
	 * Realizes the transformation of a tristumulus double valued CIE
	 * LAB image into a CIE XYZ image.
	 * 
	 * @param input
	 *            Tristumulus double valued CIE LAB image.
	 * @return CIE XYZ image.
	 */
	public static Image exec(Image input) {
		return (Image) new LABToXYZ().process(input);
	}
}