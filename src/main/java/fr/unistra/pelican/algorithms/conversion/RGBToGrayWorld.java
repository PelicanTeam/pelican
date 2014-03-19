package fr.unistra.pelican.algorithms.conversion;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;

/**
 * This class realizes the transformation of a tristumulus RGB image into a
 * double valued GrayWorld RGB doubleimage.
 * 
 *	MASK MANAGEMENT (by Régis) : 
 *	- input's mask becomes output's mask.
 *	- no modification on color calculation.
 * 
 * @author Erchan Aptoula
 * 
 */

public class RGBToGrayWorld extends Algorithm {

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
	public RGBToGrayWorld() {

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

		double[] avgs = new double[3];
		avgs[0] = avgs[1] = avgs[2] = 0.0;

		for (int x = 0; x < xdim; x++) {
			for (int y = 0; y < ydim; y++) {
				avgs[0] += input.getPixelXYBDouble(x, y, 0);
				avgs[1] += input.getPixelXYBDouble(x, y, 1);
				avgs[2] += input.getPixelXYBDouble(x, y, 2);
			}
		}

		avgs[0] = avgs[0] / (xdim * ydim);
		avgs[1] = avgs[1] / (xdim * ydim);
		avgs[2] = avgs[2] / (xdim * ydim);

		for (int x = 0; x < xdim; x++) {
			for (int y = 0; y < ydim; y++) {
				double R = input.getPixelXYBDouble(x, y, 0);
				double G = input.getPixelXYBDouble(x, y, 1);
				double B = input.getPixelXYBDouble(x, y, 2);

				double rN = R / avgs[0];
				double gN = G / avgs[1];
				double bN = B / avgs[2];

				output.setPixelXYBDouble(x, y, 0, rN);
				output.setPixelXYBDouble(x, y, 1, gN);
				output.setPixelXYBDouble(x, y, 2, bN);
			}
		}
	}

	/**
	 * This class realizes the transformation of a tristumulus RGB image into a
	 * double valued GrayWorld RGB doubleimage.
	 * 
	 * @param input
	 *            Tristumulus RGB image.
	 * @return A double valued GrayWorld RGB doubleimage.
	 */
	public static Image exec(Image input) {
		return (Image) new RGBToGrayWorld().process(input);
	}
}