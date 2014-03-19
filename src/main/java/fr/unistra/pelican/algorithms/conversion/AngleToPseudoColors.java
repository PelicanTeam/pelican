package fr.unistra.pelican.algorithms.conversion;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.histogram.ContrastStretch;

/**
 * Transforms a gray image into a pseudo-colors image using a LUT
 * magenta-blue-cyan-green-yellow-red-magenta
 * 
 * 
 * @author Lefevre
 * 
 */

public class AngleToPseudoColors extends Algorithm {

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
	public AngleToPseudoColors() {
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
		output.setColor(true);
		double step = 1.0 / 6;

		for (int t = 0; t < tdim; t++)
			for (int z = 0; z < zdim; z++)
				for (int y = 0; y < ydim; y++)
					for (int x = 0; x < xdim; x++) {
						double p = work.getPixelXYZTDouble(x, y, z, t);
						if (p < step) { // 1,0,1 => 0,0,1
							output.setPixelDouble(x, y, z, t, 0, (step - p) / step);
							output.setPixelDouble(x, y, z, t, 1, 0);
							output.setPixelDouble(x, y, z, t, 2, 1);
						} else if (p < 2 * step) { // 0,0,1 => 0,1,1
							output.setPixelDouble(x, y, z, t, 0, 0);
							output.setPixelDouble(x, y, z, t, 1, 1 - (2 * step - p) / step);
							output.setPixelDouble(x, y, z, t, 2, 1);
						} else if (p < 3 * step) { // 0,1,1 => 0,1,0
							output.setPixelDouble(x, y, z, t, 0, 0);
							output.setPixelDouble(x, y, z, t, 1, 1);
							output.setPixelDouble(x, y, z, t, 2, (3 * step - p) / step);
						} else if (p < 4 * step) { // 0,1,0 => 1,1,0
							output.setPixelDouble(x, y, z, t, 0, 1 - (4 * step - p) / step);
							output.setPixelDouble(x, y, z, t, 1, 1);
							output.setPixelDouble(x, y, z, t, 2, 0);
						} else if (p < 5 * step) { // 1,1,0 => 1,0,0
							output.setPixelDouble(x, y, z, t, 0, 1);
							output.setPixelDouble(x, y, z, t, 1, (5 * step - p) / step);
							output.setPixelDouble(x, y, z, t, 2, 0);
						} else { // 1,0,0 => 1,0,1
							output.setPixelDouble(x, y, z, t, 0, 1);
							output.setPixelDouble(x, y, z, t, 1, 0);
							output.setPixelDouble(x, y, z, t, 2, 1 - (1 - p) / step);
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
		return (Image) new AngleToPseudoColors().process(input);
	}

	public static Image exec(Image input, boolean stretch) {
		return (Image) new AngleToPseudoColors().process(input, stretch);
	}

}