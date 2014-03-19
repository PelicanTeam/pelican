package fr.unistra.pelican.algorithms.edge;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;

/**
 * This class represents the Roberts edge detector scheme, applied as gradient norm,
 * upper diagonal gradient, lower diagonal gradient
 * 
 * @author Abdullah
 */
public class Roberts extends Algorithm {
	
	/**
	 * Input image
	 */
	public Image input;

	/**
	 * Type of operation (NORM,GRADUD,GRADLD)
	 */
	public int operation;

	/**
	 * Gradient norm
	 */
	public static final int NORM = 0;

	/**
	 * Upper diagonal gradient
	 */
	public static final int GRADUD = 1;

	/**
	 * Lower diagonal gradient
	 */
	public static final int GRADLD = 2;

	/**
	 * Output image
	 */
	public Image output;

	/**
	 * Constructor
	 * 
	 */
	public Roberts() {

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
		case GRADUD:
			for (int b = 0; b < bdim; b++) {
				for (int t = 0; t < tdim; t++) {
					for (int z = 0; z < zdim; z++) {
						for (int y = 1; y < ydim - 1; y++) {
							for (int x = 1; x < xdim - 1; x++) {
								double tmp = input.getPixelXYZTBDouble(x + 1,
										y, z, t, b)
										- input.getPixelXYZTBDouble(x, y + 1,
												z, t, b);
								output.setPixelXYZTBDouble(x, y, z, t, b, tmp);
							}
						}
					}
				}
			}
			break;
		case GRADLD:
			for (int b = 0; b < bdim; b++) {
				for (int t = 0; t < tdim; t++) {
					for (int z = 0; z < zdim; z++) {
						for (int y = 1; y < ydim - 1; y++) {
							for (int x = 1; x < xdim - 1; x++) {
								double tmp = input.getPixelXYZTBDouble(x, y, z,
										t, b)
										- input.getPixelXYZTBDouble(x + 1,
												y + 1, z, t, b);
								output.setPixelXYZTBDouble(x, y, z, t, b, tmp);
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
								double tmp1 = input.getPixelXYZTBDouble(x + 1,
										y, z, t, b)
										- input.getPixelXYZTBDouble(x, y + 1,
												z, t, b);
								double tmp2 = input.getPixelXYZTBDouble(x, y,
										z, t, b)
										- input.getPixelXYZTBDouble(x + 1,
												y + 1, z, t, b);
								double norm = Math.sqrt(tmp1 * tmp1 + tmp2
										+ tmp2);
								output.setPixelXYZTBDouble(x, y, z, t, b, norm);
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Apply the Roberts edge detector scheme
	 * @param image the input image
	 * @param operation type of operation (NORM,GRADUD,GRADLD)
	 * @return the output image
	 */
	public static Image exec(Image image, int operation) {
		return (Image)new Roberts().process(image,operation);
	}
}
