package fr.unistra.pelican.algorithms.conversion;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;

/**
 * This class realizes the transformation of a tristumulus RGB image into a
 * double valued YUV image with pixel values eventually outside of the [0,1]
 * interval. Consequently scaling is necessary before any visualisation attempt
 * of the result.
 * 
 *	MASK MANAGEMENT (by Régis) : 
 *	- input's mask becomes output's mask.
 *	- no modification on color calculation.
 * 
 * @author Erchan Aptoula
 * 
 */

public class RGBToYUV extends Algorithm {

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
	public RGBToYUV() {

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

		for (int x = 0; x < xdim; x++) {
			for (int y = 0; y < ydim; y++) {
				for (int z = 0; z < zdim; z++) {
					for (int t = 0; t < tdim; t++) {
						int R = input.getPixelXYZTBByte(x, y, z, t, 0);
						int G = input.getPixelXYZTBByte(x, y, z, t, 1);
						int B = input.getPixelXYZTBByte(x, y, z, t, 2);

						// normalise to [0,1]
						double rN = R * 0.003921;
						double gN = G * 0.003921;
						double bN = B * 0.003921;

						double Y = 0.299 * rN + 0.587 * gN + 0.114 * bN;
						double U = -0.147 * rN - 0.289 * gN + 0.436 * bN;
						double V = 0.615 * rN - 0.515 * gN - 0.100 * bN;

						output.setPixelXYZTBDouble(x, y, z, t, 0, Y);
						output.setPixelXYZTBDouble(x, y, z, t, 1, U);
						output.setPixelXYZTBDouble(x, y, z, t, 2, V);
					}
				}
			}
		}
	}

	/**
	 * Scales each band of the resulting yuv image according to the value
	 * intervals Y in [0,1], U in [-0.436,0.436], V in [-0.615,0.615] and
	 * returns a valid byteImage
	 * 
	 * @return resulting ByteImage
	 */
	private static Image scaleToByte(Image yuv) {
		ByteImage bimg = new ByteImage(yuv, false);

		double f = 255.0;
		double g = 255.0 / 0.872;
		double h = 255.0 / 1.23;

		for (int x = 0; x < yuv.getXDim(); x++) {
			for (int y = 0; y < yuv.getYDim(); y++) {
				double d = yuv.getPixelXYBDouble(x, y, 0);
				// Y
				bimg.setPixelXYBByte(x, y, 0, (int) Math.round(d * f));

				// U
				d = yuv.getPixelXYBDouble(x, y, 1);
				d = (d + 0.436) * g;
				bimg.setPixelXYBByte(x, y, 1, (int) Math.round(d));

				// V
				d = yuv.getPixelXYBDouble(x, y, 2);
				d = (d + 0.615) * h;
				bimg.setPixelXYBByte(x, y, 2, (int) Math.round(d));
			}
		}

		return bimg;
	}

	/**
	 * This class realizes the transformation of a tristumulus RGB image into a
	 * double valued YUV image with pixel values eventually outside of the [0,1]
	 * interval. Consequently scaling is necessary before any visualisation
	 * attempt of the result.
	 * 
	 * @param input
	 *            Tristumulus RGB image.
	 * @return A double valued YUV image.
	 */
	public static Image exec(Image input) {
		return (Image) new RGBToYUV().process(input);
	}
}