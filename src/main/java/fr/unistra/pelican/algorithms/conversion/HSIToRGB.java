package fr.unistra.pelican.algorithms.conversion;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;

/**
 * This class realizes the transformation of a tristumulus double valued HSI
 * image with pixels in [0,1], into a byte valued RGB image.
 * 
 *	MASK MANAGEMENT (by Régis) : 
 *	- input's mask becomes output's mask.
 *	- no modification on color calculation.
 * 
 * @author Erchan Aptoula
 * 
 */

public class HSIToRGB extends Algorithm {

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
	public HSIToRGB() {

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
					"The input must be a tristumulus HSI image");

		output = new ByteImage(xdim, ydim, zdim, tdim, bdim);
		this.output.setMask( this.input.getMask() );
		output.setColor(true);

		for (int x = 0; x < xdim; x++) {
			for (int y = 0; y < ydim; y++) {
				for (int z = 0; z < zdim; z++) {
					for (int t = 0; t < tdim; t++) {
						double H = input.getPixelXYZTBDouble(x, y, z, t, 0);
						double S = input.getPixelXYZTBDouble(x, y, z, t, 1);
						double I = input.getPixelXYZTBDouble(x, y, z, t, 2);

						double R, G, B;

						H = H * 2 * Math.PI;

						int i = (int) Math.floor(H / (2 * Math.PI / 3));

						switch (i) {
						case 0:
							B = I * (1 - S);
							R = I
									* (1 + S * Math.cos(H)
											/ Math.cos(Math.PI / 3 - H));
							G = 3 * I - (R + B);
							break;
						case 1:
							H = H - 2 * Math.PI / 3;
							R = I * (1 - S);
							G = I
									* (1 + S * Math.cos(H)
											/ Math.cos(Math.PI / 3 - H));
							B = 3 * I - (R + G);
							break;
						default:
							H = H - 4 * Math.PI / 3;
							G = I * (1 - S);
							B = I
									* (1 + S * Math.cos(H)
											/ Math.cos(Math.PI / 3 - H));
							R = 3 * I - (G + B);
						}

						output
								.setPixelXYZTBByte(x, y, z, t, 0,
										(int) (R * 255));
						output
								.setPixelXYZTBByte(x, y, z, t, 1,
										(int) (G * 255));
						output
								.setPixelXYZTBByte(x, y, z, t, 2,
										(int) (B * 255));
					}
				}
			}
		}
	}

	/**
	 * Realizes the transformation of a tristumulus double valued HSI image with
	 * pixels in [0,1], into a byte valued RGB image.
	 * 
	 * @param input
	 *            Tristumulus double valued HSI image with pixels in [0,1].
	 * @return Byte valued RGB image.
	 */
	public static Image exec(Image input) {
		return (Image) new HSIToRGB().process(input);
	}
}