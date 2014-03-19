package fr.unistra.pelican.algorithms.conversion;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;

/**
 * This class realizes the transformation of a tristumulus double valued CIE LUV
 * image into its polar coordinate version.
 * 
 *	MASK MANAGEMENT (by Regis) : 
 *	- input's mask becomes output's mask.
 *	- no modification on color calculation.
 * 
 * @author Erchan Aptoula
 * 
 */
public class LUVToHLS extends Algorithm {

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
	public LUVToHLS() {

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
						double L = input.getPixelXYZTBDouble(x, y, z, t, 0);
						double U = input.getPixelXYZTBDouble(x, y, z, t, 1);
						double V = input.getPixelXYZTBDouble(x, y, z, t, 2);

						double H = Math.atan(U / V);
						double S = Math.sqrt(U * U + V * V);

						output.setPixelXYZTBDouble(x, y, z, t, 0, H);
						output.setPixelXYZTBDouble(x, y, z, t, 1, L);
						output.setPixelXYZTBDouble(x, y, z, t, 2, S);
					}
				}
			}
		}
	}

	/**
	 * This class realizes the transformation of a tristumulus double valued CIE
	 * LUV image into its polar coordinate version.
	 * 
	 * @param input
	 *            Tristumulus double valued CIE LUV image.
	 * @return image's polar coordinate version
	 */
	public static Image exec(Image input) {
		return (Image) new LUVToHLS().process(input);
	}
}