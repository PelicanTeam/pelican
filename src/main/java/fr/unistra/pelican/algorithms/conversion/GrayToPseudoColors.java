package fr.unistra.pelican.algorithms.conversion;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.histogram.ContrastStretch;

/**
 * Transforms a gray image into a pseudo-colors image using a LUT
 * magenta-blue-cyan-green-yellow-red
 * 
 *	MASK MANAGEMENT (by Régis) : 
 *	- input's mask becomes output's mask.
 *	- no modification on color calculation.
 *
 * @author Lefevre
 * 
 */

public class GrayToPseudoColors extends Algorithm {

	/**
	 * Input parameter.
	 */
	public Image input;

	/**
	 * Output parameter.
	 */
	public Image output;

	/**
	 * (Optional) stretch the input value range
	 */
	public boolean stretch = true;

	/**
	 * Constructor
	 * 
	 */
	public GrayToPseudoColors() {
		super.inputs = "input";
		super.options = "stretch";
		super.outputs = "output";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		Image work;
		if (stretch)
			work = ContrastStretch.exec(input);
		else
			work = input;
		int xdim = input.getXDim();
		int ydim = input.getYDim();
		int zdim = input.getZDim();
		int tdim = input.getTDim();
		int bdim = input.getBDim();

		if (bdim != 1)
			throw new AlgorithmException("The input must be a grayscale image");

		output = new ByteImage(xdim, ydim, zdim, tdim, 3);
		this.output.setMask( this.input.getMask() );
		output.setColor(true);

		for (int t = 0; t < tdim; t++)
			for (int z = 0; z < zdim; z++)
				for (int y = 0; y < ydim; y++)
					for (int x = 0; x < xdim; x++) {
						double p = work.getPixelXYZTDouble(x, y, z, t);
						if (p < 0.2) {
							output.setPixelDouble(x, y, z, t, 0, (0.2 - p) / 0.2);
							output.setPixelDouble(x, y, z, t, 1, 0);
							output.setPixelDouble(x, y, z, t, 2, 1);
						} else if (p < 0.4) {
							output.setPixelDouble(x, y, z, t, 0, 0);
							output.setPixelDouble(x, y, z, t, 1, 1 - (0.4 - p) / 0.2);
							output.setPixelDouble(x, y, z, t, 2, 1);
						} else if (p < 0.6) {
							output.setPixelDouble(x, y, z, t, 0, 0);
							output.setPixelDouble(x, y, z, t, 1, 1);
							output.setPixelDouble(x, y, z, t, 2, (0.6 - p) / 0.2);
						} else if (p < 0.8) {
							output.setPixelDouble(x, y, z, t, 0, 1 - (0.8 - p) / 0.2);
							output.setPixelDouble(x, y, z, t, 1, 1);
							output.setPixelDouble(x, y, z, t, 2, 0);
						} else {
							output.setPixelDouble(x, y, z, t, 0, 1);
							output.setPixelDouble(x, y, z, t, 1, (1.0 - p) / 0.2);
							output.setPixelDouble(x, y, z, t, 2, 0);
						}

					}
	}

	/**
	 * Transforms a gray image into a pseudo-colors image using a LUT
	 * magenta-blue-cyan-green-yellow-red
	 * 
	 * @param input
	 *          Gray image.
	 * @return RGB pseudo-colors image.
	 */
	public static Image exec(Image input) {
		return (Image) new GrayToPseudoColors().process(input);
	}

	public static Image exec(Image input, boolean stretch) {
		return (Image) new GrayToPseudoColors().process(input, stretch);
	}

}