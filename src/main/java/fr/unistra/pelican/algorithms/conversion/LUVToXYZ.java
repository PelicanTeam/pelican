package fr.unistra.pelican.algorithms.conversion;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;

/**
 * This class realizes the transformation of a tristumulus double valued CIE LUV
 * image into a CIE XYZ image.
 * 
 *	MASK MANAGEMENT (by Regis) : 
 *	- input's mask becomes output's mask.
 *	- no modification on color calculation.
 * 
 * @author Erchan Aptoula
 * 
 */
public class LUVToXYZ extends Algorithm {

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
	public LUVToXYZ() {

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
					"The input must be a tristumulus CIE LUV image");

		output = new DoubleImage(xdim, ydim, zdim, tdim, bdim);
		this.output.setMask( this.input.getMask() );
		output.setColor(true);

		// white point
		double Un = 0.197839; // calculated like Up and Vn for the white
		// Xn,Yn,Zn
		double Vn = 0.468342;

		for (int x = 0; x < xdim; x++) {
			for (int y = 0; y < ydim; y++) {
				for (int z = 0; z < zdim; z++) {
					for (int t = 0; t < tdim; t++) {
						double L = input.getPixelXYZTBDouble(x, y, z, t, 0);
						double U = input.getPixelXYZTBDouble(x, y, z, t, 1);
						double V = input.getPixelXYZTBDouble(x, y, z, t, 2);

						double X, Y, Z;

						double Up = U / (13 * L) + Un;
						double Vp = V / (13 * L) + Vn;

						Y = Math.pow((L + 16) / 116, 3);
						X = Up * 9 * Y / (4 * Vp);
						Z = (9 * Y - 15 * Vp * Y - Vp * X) / (3 * Vp);

						output.setPixelXYZTBDouble(x, y, z, t, 0, X);
						output.setPixelXYZTBDouble(x, y, z, t, 1, Y);
						output.setPixelXYZTBDouble(x, y, z, t, 2, Z);
					}
				}
			}
		}
	}

	/**
	 * This class realizes the transformation of a tristumulus double valued CIE
	 * LUV image into a CIE XYZ image.
	 * 
	 * @param input
	 *            Tristumulus double valued CIE LUV image.
	 * @return CIE XYZ image.
	 */
	public static Image exec(Image input) {
		return (Image) new LUVToXYZ().process(input);
	}
}