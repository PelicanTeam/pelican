package fr.unistra.pelican.algorithms.spatial;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;

/**
 * High boost filter with laplacian.
 * 
 * @author lefevre
 */
public class HighBoostFilter extends Algorithm {

	/**
	 * Input image
	 */
	public Image input;

	/**
	 * Ouput image
	 */
	public Image output;

	/**
	 * Constructor
	 * 
	 */
	public HighBoostFilter() {
		super.inputs = "input";
		super.outputs = "output";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		output = convolve2D(1, 1, 0, 0, 0, new int[][] { { -1, -1, -1 },
				{ -1, 9, -1 }, { -1, -1, -1 } });
	}

	/**
	 * High-boost filter
	 * 
	 * @param input
	 *            Input image
	 * @return Ouput image
	 */
	public static Image exec(Image input) {
		return (Image) new HighBoostFilter().process(input);
	}

	Image convolve2D(int dx, int dy, int dz, int dt, int db, int tab[][]) {
		int xdim = input.getXDim();
		int ydim = input.getYDim();
		int zdim = input.getZDim();
		int tdim = input.getTDim();
		int bdim = input.getBDim();
		Image result = input.copyImage(false);
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
							if (sum > 1)
								sum = 1;
							else if(sum<0)
								sum=0;
							result.setPixelXYZTBDouble(x, y, z, t, b, sum);
						}
		return result;
	}

}
