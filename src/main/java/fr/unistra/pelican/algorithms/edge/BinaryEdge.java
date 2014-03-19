package fr.unistra.pelican.algorithms.edge;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;

/**
 * This class performs an edge detection on binary images.
 * 
 * @author Lefevre
 */
public class BinaryEdge extends Algorithm {

	/**
	 * Input image
	 */
	public BooleanImage inputImage;

	/**
	 * Output image
	 */
	public BooleanImage outputImage;

	/**
	 * (Optional) the type of connexity
	 */
	public boolean lowConnexity = false;

	/**
	 * Constructor
	 */
	public BinaryEdge() {
		super.inputs = "inputImage";
		super.outputs = "outputImage";
		super.options = "lowConnexity";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = new BooleanImage(inputImage, false);
		int xdim = inputImage.getXDim();
		int ydim = inputImage.getYDim();
		int zdim = inputImage.getZDim();
		int tdim = inputImage.getTDim();
		int bdim = inputImage.getBDim();
		boolean bord;
		for (int b = 0; b < bdim; b++) {
			for (int t = 0; t < tdim; t++)
				for (int z = 0; z < zdim; z++)
					for (int y = 0; y < ydim; y++)
						for (int x = 0; x < xdim; x++)
							if (inputImage.getPixelBoolean(x, y, z, t, b)) {
								bord = false;
								for (int tt = -1; tt <= 1; tt++)
									for (int zz = -1; zz <= 1; zz++)
										for (int yy = -1; yy <= 1; yy++)
											for (int xx = -1; xx <= 1; xx++)
												if (x + xx >= 0
													&& x + xx < xdim
													&& y + yy >= 0
													&& y + yy < ydim
													&& z + zz >= 0
													&& z + zz < zdim
													&& t + tt >= 0
													&& t + tt < tdim
													&& inputImage.getPixelBoolean(x + xx, y + yy, z + zz,
														t + tt, b) == false)
													if (xx != 0 || yy != 0 || zz != 0 || tt != 00)
														if (!lowConnexity
															|| (Math.abs(xx) + Math.abs(yy) + Math.abs(zz) + Math
																.abs(tt)) == 1)
															bord = true;
								if (bord)
									outputImage.setPixelBoolean(x, y, z, t, b, true);
							}
		}
	}

	/**
	 * Performs an edge detection on binary images
	 * 
	 * @param image
	 *          the input image
	 * @return the output image
	 */
	public static BooleanImage exec(BooleanImage image) {
		return (BooleanImage) new BinaryEdge().process(image);
	}

	/**
	 * 
	 * @param image the input image
	 * @param lowConnexity the type of connexity
	 * @return the output image
	 */public static BooleanImage exec(BooleanImage image,boolean lowConnexity) {
		return (BooleanImage) new BinaryEdge().process(image,lowConnexity);
	}

}
