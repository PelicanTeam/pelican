package fr.unistra.pelican.algorithms.conversion;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.Tools;

/**
 * Given a colour image, it returns an image representing the Euclidean distance
 * of all pixels with respect to a given colour
 * 
 *	MASK MANAGEMENT (by Régis) : 
 *	- input's mask becomes output's mask.
 *	- no modification on color calculation.
 * 
 * @author Erchan Aptoula
 * 
 */

public class ColourDistanceImage extends Algorithm {

	/**
	 * First Input parameter.
	 */
	public Image input;

	/**
	 * Second Input parameter.
	 */
	public int c1;

	/**
	 * Third Input parameter.
	 */
	public int c2;

	/**
	 * Fourth Input parameter.
	 */
	public int c3;

	/**
	 * Output parameter.
	 */
	public Image output;

	/**
	 * Constructor
	 * 
	 */
	public ColourDistanceImage() {

		super();
		super.inputs = "input,c1,c2,c3";
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

		double[] refColor = { c1 / 255.0, c2 / 255.0, c3 / 255.0 };

		for (int x = 0; x < xdim; x++) {
			for (int y = 0; y < ydim; y++) {
				for (int z = 0; z < zdim; z++) {
					for (int t = 0; t < tdim; t++) { 

						double[] p = input.getVectorPixelXYZTDouble(x, y, 0, 0);
						double distance = Tools.euclideanDistance(refColor,p);
						output.setPixelXYZTDouble(x, y, z, t, distance/3.0);
					}
				}
			}
		}
	}

	/**
	 * Given a colour image, it returns an image representing the Euclidean
	 * distance of all pixels with respect to a given colour
	 * 
	 * @param input
	 *            Colour image
	 * @param c1
	 *            Color 1
	 * @param c2
	 *            Color 2
	 * @param c3
	 *            Color 3
	 * @return Image representing the Euclidean distance of all pixels with
	 *         respect to a given colour
	 */
	public static Image exec(Image input, int c1, int c2, int c3) {
		return (Image) new ColourDistanceImage().process(input, c1, c2, c3);
	}
}