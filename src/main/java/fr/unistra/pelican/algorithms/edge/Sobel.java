package fr.unistra.pelican.algorithms.edge;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;

/**
 * This class represents the Sobel edge detector scheme applied in 2-D or 3-D as norm,
 * dimensional and orientation gradient.
 * 
 * @author Aptoula, Lefèvre
 */
public class Sobel extends Algorithm {

	/**
	 * Input image
	 */
	public Image input;

	/**
	 * Type of operation (NORM = 0,GRADX = 1,GRADY = 2,ORIEN = 3)
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
	 * Z gradient
	 */
	public static final int GRADZ = 4;

	/**
	 * T gradient
	 */
	public static final int GRADT = 5;

	/**
	 * Dimensions used to compute the Sobel operator
	 */
	public int dimensions = XY;

	/**
	 * Processing 2-D images
	 */
	public static final int XY = 0;

	/**
	 * Processing 3-D images
	 */
	public static final int XYZ = 1;

	/**
	 * Processing 2-D+t image sequences
	 */
	public static final int XYT = 2;

	/**
	 * Output image
	 */
	public Image output;

	private int bdim, tdim, zdim, ydim, xdim;

	/**
	 * Constructor
	 * 
	 */
	public Sobel() {
		super.inputs = "input,operation";
		super.options = "dimensions";
		super.outputs = "output";

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		bdim = input.getBDim();
		tdim = input.getTDim();
		zdim = input.getZDim();
		ydim = input.getYDim();
		xdim = input.getXDim();
		int size = input.size();
		Image gradx, grady, gradz, gradt;

		if (input instanceof BooleanImage)
			throw new AlgorithmException("BooleanImages are not supported");

		switch (dimensions) {
		case XYZ:
			switch (operation) {
			case GRADZ:
				output = convolve3DXYZ(1, 1, 1, 0, 0, new int[][][] {
						{ { -1, -3, -1 }, { -3, -6, -3 }, { -1, -3, -1 } },
						{ { 0, 0, 0 }, { 0, 0, 0 }, { 0, 0, 0 } },
						{ { 1, 3, 1 }, { 3, 6, 3 }, { 1, 3, 1 } } });
				break;
			case GRADY:
				output = convolve3DXYZ(1, 1, 1, 0, 0, new int[][][] {
						{ { -1, -3, -1 }, { 0, 0, 0 }, { 1, 3, 1 } },
						{ { -3, -6, -3 }, { 0, 0, 0 }, { 3, 6, 3 } },
						{ { -1, -3, -1 }, { 0, 0, 0 }, { 1, 3, 1 } } });
				break;
			case GRADX:
				output = convolve3DXYZ(1, 1, 1, 0, 0, new int[][][] {
						{ { -1, 0, 1 }, { -3, 0, 3 }, { -1, 0, 1 } },
						{ { -3, 0, 3 }, { -6, 0, 6 }, { 3, 0, 3 } },
						{ { -1, 0, 1 }, { -3, 0, 3 }, { -1, 0, 1 } } });
				break;
			case ORIEN:
				gradx = convolve3DXYZ(1, 1, 1, 0, 0, new int[][][] {
						{ { -1, 0, 1 }, { -3, 0, 3 }, { -1, 0, 1 } },
						{ { -3, 0, 3 }, { -6, 0, 6 }, { 3, 0, 3 } },
						{ { -1, 0, 1 }, { -3, 0, 3 }, { -1, 0, 1 } } });
				grady = convolve3DXYZ(1, 1, 1, 0, 0, new int[][][] {
						{ { -1, -3, -1 }, { 0, 0, 0 }, { 1, 3, 1 } },
						{ { -3, -6, -3 }, { 0, 0, 0 }, { 3, 6, 3 } },
						{ { -1, -3, -1 }, { 0, 0, 0 }, { 1, 3, 1 } } });
				gradz = convolve3DXYZ(1, 1, 1, 0, 0, new int[][][] {
						{ { -1, -3, -1 }, { -3, -6, -3 }, { -1, -3, -1 } },
						{ { 0, 0, 3 }, { 0, 0, 0 }, { 0, 0, 0 } },
						{ { 1, 3, 1 }, { 3, 6, 3 }, { 1, 3, 1 } } });
				System.err
						.println("cas non traité pour l'instant : orientation XYZ Sobel");
				break;
			default:
			case NORM:
				gradx = convolve3DXYZ(1, 1, 1, 0, 0, new int[][][] {
						{ { -1, 0, 1 }, { -3, 0, 3 }, { -1, 0, 1 } },
						{ { -3, 0, 3 }, { -6, 0, 6 }, { 3, 0, 3 } },
						{ { -1, 0, 1 }, { -3, 0, 3 }, { -1, 0, 1 } } });
				grady = convolve3DXYZ(1, 1, 1, 0, 0, new int[][][] {
						{ { -1, -3, -1 }, { 0, 0, 0 }, { 1, 3, 1 } },
						{ { -3, -6, -3 }, { 0, 0, 0 }, { 3, 6, 3 } },
						{ { -1, -3, -1 }, { 0, 0, 0 }, { 1, 3, 1 } } });
				gradz = convolve3DXYZ(1, 1, 1, 0, 0, new int[][][] {
						{ { -1, -3, -1 }, { -3, -6, -3 }, { -1, -3, -1 } },
						{ { 0, 0, 3 }, { 0, 0, 0 }, { 0, 0, 0 } },
						{ { 1, 3, 1 }, { 3, 6, 3 }, { 1, 3, 1 } } });
				output = new DoubleImage(xdim, ydim, zdim, tdim, bdim);
				output.copyAttributes(input);
				for (int p = 0; p < size; p++)
					output.setPixelDouble(p, Math.sqrt(gradx.getPixelDouble(p)
							* gradx.getPixelDouble(p) + grady.getPixelDouble(p)
							* grady.getPixelDouble(p) + gradz.getPixelDouble(p)
							* gradz.getPixelDouble(p)));
			}
			break;
		case XYT:
			break;
		case XY:
		default:
			switch (operation) {
			case GRADX:
				output = convolve2D(1, 1, 0, 0, 0, new int[][] { { -1, 0, 1 },
						{ -2, 0, 2 }, { -1, 0, 1 } });
				break;
			case GRADY:
				output = convolve2D(1, 1, 0, 0, 0, new int[][] {
						{ -1, -2, -1 }, { 0, 0, 0 }, { 1, 2, 1 } });
				break;
			case ORIEN:
				gradx = convolve2D(1, 1, 0, 0, 0, new int[][] { { -1, 0, 1 },
						{ -2, 0, 2 }, { -1, 0, 1 } });
				grady = convolve2D(1, 1, 0, 0, 0, new int[][] { { -1, -2, -1 },
						{ 0, 0, 0 }, { 1, 2, 1 } });
				output = new DoubleImage(xdim, ydim, zdim, tdim, bdim);
				output.copyAttributes(input);
				for (int p = 0; p < size; p++)
					output.setPixelDouble(p, Math.atan(grady.getPixelDouble(p)
							/ gradx.getPixelDouble(p))
							- 3 * Math.PI / 4);
				break;
			default:
			case NORM:
				gradx = convolve2D(1, 1, 0, 0, 0, new int[][] { { -1, 0, 1 },
						{ -2, 0, 2 }, { -1, 0, 1 } });
				grady = convolve2D(1, 1, 0, 0, 0, new int[][] { { -1, -2, -1 },
						{ 0, 0, 0 }, { 1, 2, 1 } });
				output = new DoubleImage(xdim, ydim, zdim, tdim, bdim);
				output.copyAttributes(input);
				for (int p = 0; p < size; p++)
					output.setPixelDouble(p, Math.sqrt(gradx.getPixelDouble(p)
							* gradx.getPixelDouble(p) + grady.getPixelDouble(p)
							* grady.getPixelDouble(p)));
			}
		}
	}

	Image convolve2D(int dx, int dy, int dz, int dt, int db, int tab[][]) {
		Image result = new DoubleImage(xdim, ydim, zdim, tdim, bdim);
		result.copyAttributes(input);
		for (int b = db; b < bdim - db; b++)
			for (int t = dt; t < tdim - dt; t++)
				for (int z = dz; z < zdim - dz; z++)
					for (int y = dy; y < ydim - dy; y++)
						for (int x = dx; x < xdim - dx; x++) {
							double sum = 0;
							for (int i = 0; i < 3; i++)
								for (int j = 0; j < 3; j++)
									sum += tab[j][i]
											* input.getPixelXYZTBDouble(x + i
													- 1, y + j - 1, z, t, b);
							result.setPixelXYZTBDouble(x, y, z, t, b, sum);
						}
		return result;
	}

	Image convolve3DXYZ(int dx, int dy, int dz, int dt, int db, int tab[][][]) {
		Image result = new DoubleImage(xdim, ydim, zdim, tdim, bdim);
		result.copyAttributes(input);
		for (int b = db; b < bdim - db; b++)
			for (int t = dt; t < tdim - dt; t++)
				for (int z = dz; z < zdim - dz; z++)
					for (int y = dy; y < ydim - dy; y++)
						for (int x = dx; x < xdim - dx; x++) {
							double sum = 0;
							for (int i = 0; i < 3; i++)
								for (int j = 0; j < 3; j++)
									for (int k = 0; k < 3; k++)
										sum += tab[k][j][i]
												* input.getPixelXYZTBDouble(x
														+ i - 1, y + j - 1, z
														+ k - 1, t, b);
							result.setPixelXYZTBDouble(x, y, z, t, b, sum);
						}
		return result;
	}

	Image convolve3DXYT(int dx, int dy, int dz, int dt, int db, int tab[][][]) {
		Image result = new DoubleImage(xdim, ydim, zdim, tdim, bdim);
		result.copyAttributes(input);
		for (int b = db; b < bdim - db; b++)
			for (int t = dt; t < tdim - dt; t++)
				for (int z = dz; z < zdim - dz; z++)
					for (int y = dy; y < ydim - dy; y++)
						for (int x = dx; x < xdim - dx; x++) {
							double sum = 0;
							for (int i = 0; i < 3; i++)
								for (int j = 0; j < 3; j++)
									for (int k = 0; k < 3; k++)
										sum += tab[k][j][i]
												* input.getPixelXYZTBDouble(x
														+ i - 1, y + j - 1, z,
														t + k - 1, b);
							result.setPixelXYZTBDouble(x, y, z, t, b, sum);
						}
		return result;
	}

	public static DoubleImage exec(Image image, int operation, int dimensions) {
		return (DoubleImage) new Sobel().process(image, operation, dimensions);
	}

	/**
	 * Apply the Sobel edge detector scheme
	 * 
	 * @param image
	 *            the input image
	 * @param operation
	 *            type of operation (NORM,GRADX,GRADY,ORIEN)
	 * @return the output image
	 */
	public static DoubleImage exec(Image image, int operation) {
		return (DoubleImage) new Sobel().process(image, operation);
	}

	/**
	 * Apply the Sobel edge detector scheme with the NORM operation
	 * 
	 * @param the
	 *            input image
	 * @return the output image
	 */
	public static DoubleImage exec(Image image) {
		return (DoubleImage) new Sobel().process(image, Sobel.NORM);
	}

}
