package fr.unistra.pelican.algorithms.conversion;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;

/**
 * This class realizes the transformation of a tristumulus double valued CIE XYZ
 * image into a CIE LUV image.
 * 
 *	MASK MANAGEMENT (by Régis) : 
 *	- input's mask becomes output's mask.
 *	- no modification on color calculation.
 * 
 * @author Erchan Aptoula
 * 
 */
public class XYZToLUV extends Algorithm {

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
	public XYZToLUV() {

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

		double Un = 0.197839; // calculated like Up and Vn for the white
		// Xn,Yn,Zn
		double Vn = 0.468342;

		for (int x = 0; x < xdim; x++) {
			for (int y = 0; y < ydim; y++) {
				for (int z = 0; z < zdim; z++) {
					for (int t = 0; t < tdim; t++) {
						double X = input.getPixelXYZTBDouble(x, y, z, t, 0);
						double Y = input.getPixelXYZTBDouble(x, y, z, t, 1);
						double Z = input.getPixelXYZTBDouble(x, y, z, t, 2);

						double Up = 4 * X / (X + 15 * Y + 3 * Z);
						double Vp = 9 * Y / (X + 15 * Y + 3 * Z);

						double L = 116 * Math.pow(Y, 0.333333) - 16;
						double U = 13 * L * (Up - Un);
						double V = 13 * L * (Vp - Vn);

						output.setPixelXYZTBDouble(x, y, z, t, 0, L);
						output.setPixelXYZTBDouble(x, y, z, t, 1, U);
						output.setPixelXYZTBDouble(x, y, z, t, 2, V);
					}
				}
			}
		}
	}

	/**
	 * This class realizes the transformation of a tristumulus double valued CIE
	 * XYZ image into a CIE LUV image.
	 * 
	 * @param input
	 *            Tristumulus double valued CIE XYZ image.
	 * @return A CIE LUV image.
	 */
	public static Image exec(Image input) {
		return (Image) new XYZToLUV().process(input);
	}
}