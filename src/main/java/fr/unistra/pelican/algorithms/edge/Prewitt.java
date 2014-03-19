package fr.unistra.pelican.algorithms.edge;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;

/**
 * This class represents the Prewitt edge detector scheme, applied as norm,
 * horizontal,vertical and orientation gradient.
 * 
 * @author Abdullah
 */
public class Prewitt extends Algorithm {
	
	/**
	 * Input image
	 */
	public Image input;

	/**
	 * Type of operation (NORM,GRADX,GRADY,ORIEN)
	 */
	public int operation;

	/**
	 * Gradient norm
	 */
	public static final int NORM = 0;

	/**
	 * X gradient
	 */
	public static final int GRADX = 1;

	/**
	 * Y gradient
	 */
	public static final int GRADY = 2;

	/**
	 * Orientation gradient 
	 */
	public static final int ORIEN = 3;

	/**
	 * Output image
	 */
	public Image output;

	/**
	 * Constructor
	 * 
	 */
	public Prewitt() {

		super();
		super.inputs = "input,operation";
		super.outputs = "output";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		int bdim = input.getBDim();
		int tdim = input.getTDim();
		int zdim = input.getZDim();
		int ydim = input.getYDim();
		int xdim = input.getXDim();

		output = new DoubleImage(xdim, ydim, zdim, tdim, bdim);
		output.copyAttributes(input);

		if (input instanceof BooleanImage)
			throw new AlgorithmException("BooleanImages are not supported");

		switch (operation) {
		case GRADY:
			for (int b = 0; b < bdim; b++) {
				for (int t = 0; t < tdim; t++) {
					for (int z = 0; z < zdim; z++) {
						for (int y = 1; y < ydim - 1; y++) {
							for (int x = 1; x < xdim - 1; x++) {
								double tmp = input.getPixelXYZTBDouble(x - 1,
										y - 1, z, t, b)
										+ input.getPixelXYZTBDouble(x, y - 1,
												z, t, b)
										+ input.getPixelXYZTBDouble(x + 1,
												y - 1, z, t, b);
								tmp = input.getPixelXYZTBDouble(x - 1, y + 1,
										z, t, b)
										+ input.getPixelXYZTBDouble(x, y + 1,
												z, t, b)
										+ input.getPixelXYZTBDouble(x + 1,
												y + 1, z, t, b) - tmp;
								output.setPixelXYZTBDouble(x, y, z, t, b, tmp);
							}
						}
					}
				}
			}
			break;
		case GRADX:
			for (int b = 0; b < bdim; b++) {
				for (int t = 0; t < tdim; t++) {
					for (int z = 0; z < zdim; z++) {
						for (int y = 1; y < ydim - 1; y++) {
							for (int x = 1; x < xdim - 1; x++) {
								double tmp = input.getPixelXYZTBDouble(x - 1,
										y - 1, z, t, b)
										+ input.getPixelXYZTBDouble(x - 1, y,
												z, t, b)
										+ input.getPixelXYZTBDouble(x - 1,
												y + 1, z, t, b);
								tmp = input.getPixelXYZTBDouble(x + 1, y - 1,
										z, t, b)
										+ input.getPixelXYZTBDouble(x + 1, y,
												z, t, b)
										+ input.getPixelXYZTBDouble(x + 1,
												y + 1, z, t, b) - tmp;
								output.setPixelXYZTBDouble(x, y, z, t, b, tmp);
							}
						}
					}
				}
			}
			break;
		case NORM:
			for (int b = 0; b < bdim; b++) {
				for (int t = 0; t < tdim; t++) {
					for (int z = 0; z < zdim; z++) {
						for (int y = 1; y < ydim - 1; y++) {
							for (int x = 1; x < xdim - 1; x++) {
								double tmpy = input.getPixelXYZTBDouble(x - 1,
										y - 1, z, t, b)
										+ input.getPixelXYZTBDouble(x, y - 1,
												z, t, b)
										+ input.getPixelXYZTBDouble(x + 1,
												y - 1, z, t, b);
								tmpy = input.getPixelXYZTBDouble(x - 1, y + 1,
										z, t, b)
										+ input.getPixelXYZTBDouble(x, y + 1,
												z, t, b)
										+ input.getPixelXYZTBDouble(x + 1,
												y + 1, z, t, b) - tmpy;
								double tmpx = input.getPixelXYZTBDouble(x - 1,
										y - 1, z, t, b)
										+ input.getPixelXYZTBDouble(x - 1, y,
												z, t, b)
										+ input.getPixelXYZTBDouble(x - 1,
												y + 1, z, t, b);
								tmpx = input.getPixelXYZTBDouble(x + 1, y - 1,
										z, t, b)
										+ input.getPixelXYZTBDouble(x + 1, y,
												z, t, b)
										+ input.getPixelXYZTBDouble(x + 1,
												y + 1, z, t, b) - tmpx;
								double norm = Math.sqrt(tmpx * tmpx + tmpy
										* tmpy);
								output.setPixelXYZTBDouble(x, y, z, t, b, norm);
							}
						}
					}
				}
			}
			break;
		default:
			for (int b = 0; b < bdim; b++) {
				for (int t = 0; t < tdim; t++) {
					for (int z = 0; z < zdim; z++) {
						for (int y = 1; y < ydim - 1; y++) {
							for (int x = 1; x < xdim - 1; x++) {
								double tmpy = input.getPixelXYZTBDouble(x - 1,
										y - 1, z, t, b)
										+ input.getPixelXYZTBDouble(x, y - 1,
												z, t, b)
										+ input.getPixelXYZTBDouble(x + 1,
												y - 1, z, t, b);
								tmpy = input.getPixelXYZTBDouble(x - 1, y + 1,
										z, t, b)
										+ 2
										* input.getPixelXYZTBDouble(x, y + 1,
												z, t, b)
										+ input.getPixelXYZTBDouble(x + 1,
												y + 1, z, t, b) - tmpy;
								double tmpx = input.getPixelXYZTBDouble(x - 1,
										y - 1, z, t, b)
										+ input.getPixelXYZTBDouble(x - 1, y,
												z, t, b)
										+ input.getPixelXYZTBDouble(x - 1,
												y + 1, z, t, b);
								tmpx = input.getPixelXYZTBDouble(x + 1, y - 1,
										z, t, b)
										+ 2
										* input.getPixelXYZTBDouble(x + 1, y,
												z, t, b)
										+ input.getPixelXYZTBDouble(x + 1,
												y + 1, z, t, b) - tmpx;
								double norm = Math.atan(tmpy / tmpx) - 3
										* Math.PI / 4;
								output.setPixelXYZTBDouble(x, y, z, t, b, norm);
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Apply the Prewitt edge detector scheme
	 * @param image the input image
	 * @param operation type of operation (NORM,GRADX,GRADY,ORIEN)
	 * @return the output image
	 */
	public static DoubleImage exec(Image image, int operation) {
		return (DoubleImage)new Prewitt().process(image,operation);
	}
}
