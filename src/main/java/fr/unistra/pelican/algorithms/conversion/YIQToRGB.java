package fr.unistra.pelican.algorithms.conversion;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;

/**
 * This class realizes the transformation of a tristumulus double valued YIQ
 * image into a byte valued RGB image.
 * 
 *	MASK MANAGEMENT (by Régis) : 
 *	- input's mask becomes output's mask.
 *	- no modification on color calculation.
 * 
 * @author Erchan Aptoula
 * 
 */
public class YIQToRGB extends Algorithm {

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
	public YIQToRGB() {

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
					"The input must be a tristumulus YIQ image");

		output = new ByteImage(xdim, ydim, zdim, tdim, bdim);
		this.output.setMask( this.input.getMask() );
		output.setColor(true);

		for (int x = 0; x < xdim; x++) {
			for (int y = 0; y < ydim; y++) {
				for (int z = 0; z < zdim; z++) {
					for (int t = 0; t < tdim; t++) {
						double Y = input.getPixelXYZTBDouble(x, y, z, t, 0);
						double I = input.getPixelXYZTBDouble(x, y, z, t, 1);
						double Q = input.getPixelXYZTBDouble(x, y, z, t, 2);

						// RGB cannot contain ALL colors, so anything outside
						// [0,255] is black
						// for full bijectivity between spaces sRGB is necessary
						int r = (int) Math
								.round((1.0 * Y + 0.956 * I + 0.621 * Q) * 255);
						if (r < 0 || r > 255)
							output.setPixelXYZTBByte(x, y, z, t, 0, 0);
						else
							output.setPixelXYZTBByte(x, y, z, t, 0, r);

						int g = (int) Math
								.round((1.0 * Y - 0.272 * I - 0.647 * Q) * 255);
						if (g < 0 || g > 255)
							output.setPixelXYZTBByte(x, y, z, t, 1, 0);
						else
							output.setPixelXYZTBByte(x, y, z, t, 1, g);

						int b = (int) Math
								.round((1.0 * Y - 1.105 * I + 1.702 * Q) * 255);
						if (b < 0 || b > 255)
							output.setPixelXYZTBByte(x, y, z, t, 2, 0);
						else
							output.setPixelXYZTBByte(x, y, z, t, 2, b);
					}
				}
			}
		}
	}

	/**
	 * This class realizes the transformation of a tristumulus double valued YIQ
	 * image into a byte valued RGB image.
	 * 
	 * @param input
	 *            Tristumulus double valued YIQ image.
	 * @return A byte valued RGB image.
	 */
	public static Image exec(Image input) {
		return (Image) new YIQToRGB().process(input);
	}
}