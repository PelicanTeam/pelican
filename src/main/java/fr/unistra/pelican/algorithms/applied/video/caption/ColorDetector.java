package fr.unistra.pelican.algorithms.applied.video.caption;

import java.util.Arrays;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;

/**
 * This class identifies caption blocks in still images or video sequences using
 * color analysis
 * 
 * S. Lefèvre, N. Vincent, Caption Localisation in Video Sequences by Fusion of
 * Multiple Detectors, IAPR International Conference on Document Analysis and
 * Recognition, Seoul, Korea, September 2005, pages 106-110,
 * doi:10.1109/ICDAR.2005.67
 * 
 * @author Lefevre
 */

public class ColorDetector extends Algorithm {

	/**
	 * The input image
	 */
	public Image input;

	/**
	 * The block width, default value is 8 pixels
	 */
	public int width = 8;

	/**
	 * The block height, default value is 8 pixels
	 */
	public int height = 8;

	/**
	 * The quantification value, i.e. number of histogram bins for each colour,
	 * default value is 8 bins
	 */
	public int quantification = 8;

	/**
	 * The ratio used in the block labeling process
	 */
	public double ratio = 0.5;

	/**
	 * The binary output image
	 */
	public BooleanImage output;

	/**
	 * Default constructor
	 */
	public ColorDetector() {
		super.inputs = "input";
		super.outputs = "output";
		super.options = "width,height,quantification,ratio";
	}

	/**
	 * This class identifies caption blocks in still images or video sequences
	 * using color analysis
	 * 
	 * @param input
	 *            The color input image
	 * @return The binary output image
	 */
	public static BooleanImage exec(Image input) {
		return (BooleanImage) new ColorDetector().process(input);
	}

	/**
	 * This class identifies caption blocks in still images or video sequences
	 * using color analysis
	 * 
	 * @param input
	 *            The color input image
	 * @param width
	 *            The block width
	 * @param height
	 *            The block height
	 * @param quantification
	 *            The quantification value, i.e. number of histogram bins for
	 *            each colour
	 * @param ratio
	 *            The ratio used in the labeling process
	 * @return The binary output image
	 */
	public static Image exec(Image input, int width, int height,
			int quantification, double ratio) {
		return (Image) new ColorDetector().process(input, width, height,
				quantification, ratio);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() {
		BooleanImage label = new BooleanImage(input.getXDim() / width, input
				.getYDim()
				/ height, input.getZDim(), input.getTDim(), 1);
		label.copyAttributes(input);
		label.setColor(false);

		ratio *= width * height;
		int histo[][] = new int[input.getBDim()][quantification];
		boolean ok;
		for (int z = 0; z < input.getZDim(); z++)
			for (int t = 0; t < input.getTDim(); t++)
				for (int x = 0; x < label.getXDim(); x++)
					for (int y = 0; y < label.getYDim(); y++) {
						for (int b = 0; b < input.getBDim(); b++)
							Arrays.fill(histo[b], 0);
						for (int xb = 0; xb < width; xb++)
							for (int yb = 0; yb < height; yb++)
								for (int b = 0; b < input.getBDim(); b++)
									histo[b][(input.getPixelByte(
											x * width + xb, y * height + yb, z,
											t, b) * quantification) / 256]++;
						ok = true;
						for (int k = 0; k < quantification && ok; k++)
							for (int b = 0; b < input.getBDim(); b++)
								if (histo[b][k] > ratio)
									ok = false;
						label.setPixelBoolean(x, y, z, t, 0, ok);
					}
		output = label;
	}

}
