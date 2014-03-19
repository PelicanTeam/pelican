package fr.unistra.pelican.algorithms.conversion;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;

/**
 * This class realizes the transformation of a tristumulus RGB image into a
 * graylevel image using the formula : g = 0.299 * R + 0.587 * G + 0.114 * B
 * 
 * 
 *	MASK MANAGEMENT (by Régis) : 
 *	- input's mask becomes output's mask.
 *	- no modification on color calculation.
 * 
 * @author Sebastien Lefevre
 * 
 */

public class RGBToGray extends Algorithm {

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
	public RGBToGray() {
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

		output = new ByteImage(xdim, ydim, zdim, tdim, 1);
		this.output.setMask( this.input.getMask() );
		output.setColor(false);

		for (int x = 0; x < xdim; x++) {
			for (int y = 0; y < ydim; y++) {
				for (int z = 0; z < zdim; z++) {
					for (int t = 0; t < tdim; t++) {
						double R = input.getPixelXYZTBDouble(x, y, z, t, 0);
						double G = input.getPixelXYZTBDouble(x, y, z, t, 1);
						double B = input.getPixelXYZTBDouble(x, y, z, t, 2);
						double g = 0.299 * R + 0.587 * G + 0.114 * B;
						output.setPixelXYZTBDouble(x, y, z, t, 0, g);
					}
				}
			}
		}
	}

	/**
	 * This class realizes the transformation of a tristumulus RGB image into a
	 * graylevel image using the formula : g = 0.299 * R + 0.587 * G + 0.114 * B
	 * 
	 * @param input Tristumulus RGB image
	 * @return Graylevel image
	 */
	public static Image exec(Image input) {
		return (Image) new RGBToGray().process(input);
	}
}