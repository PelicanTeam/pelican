package fr.unistra.pelican.algorithms.applied.video.caption;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.statistics.BlockCount;

/**
 * This class identifies caption blocks in still images or video sequences using
 * texture analysis
 * 
 * S. Lefèvre, N. Vincent, Caption Localisation in Video Sequences by Fusion of
 * Multiple Detectors, IAPR International Conference on Document Analysis and
 * Recognition, Seoul, Korea, September 2005, pages 106-110,
 * doi:10.1109/ICDAR.2005.67
 * 
 * @author Lefevre
 */

public class TextureDetector extends Algorithm {

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
	 * The threshold compared to haar values to label edge pixels
	 */
	public int haarThr = 15;

	/**
	 * The ratio used in the block labeling process
	 */
	public double ratio = 0.2;

	/**
	 * The binary output image
	 */
	public BooleanImage output;

	/**
	 * Default constructor
	 */
	public TextureDetector() {
		super.inputs = "input";
		super.options = "width,height,haarThr,ratio";
		super.outputs = "output";
		
	}

	/**
	 * This class identifies caption blocks in still images or video sequences
	 * using texture analysis
	 * 
	 * @param input
	 *            The color input image
	 * @return The binary output image
	 */
	public static BooleanImage exec(Image input) {
		return (BooleanImage) new TextureDetector().process(input);
	}

	/**
	 * This class identifies caption blocks in still images or video sequences
	 * using texture analysis
	 * 
	 * @param input
	 *            The color input image
	 * @param width
	 *            The block width
	 * @param height
	 *            The block height
	 * @param haarThr
	 *            The threshold compared to haar values to label edge pixels
	 * @param ratio
	 *            The ratio used in the labeling process
	 * @return The binary output image
	 */
	public static Image exec(Image input, int width, int height, int haarThr,
			double ratio) {
		return (Image) new TextureDetector().process(input, width, height,
				haarThr, ratio);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		output = new BooleanImage(input.getXDim() / width, input.getYDim()
				/ height, input.getZDim(), input.getTDim(), input.getBDim());
		output.copyAttributes(input);
		Image count, haar;
		// Haar image computation
		haar = computeHaar();
		// Haar thresholded
		for (int l = 0; l < haar.size(); l++)
			haar.setPixelBoolean(l, haar.getPixelInt(l) >= haarThr);
		// Count pixels
		try {
			count = (Image) new BlockCount().process(haar, width, height);
		} catch (PelicanException ex) {
			throw new AlgorithmException(ex.getMessage());
		}
		// Threshold count image
		if (count instanceof IntegerImage)
			for (int l = 0; l < count.size(); l++)
				output.setPixelBoolean(l, count.getPixelInt(l) >= (ratio
						* width * height));
		else
			for (int l = 0; l < count.size(); l++)
				output.setPixelBoolean(l, count.getPixelByte(l) >= (ratio
						* width * height));
	}

	private Image computeHaar() {
		Image haar = new IntegerImage(input.getXDim(), input.getYDim(), input
				.getZDim(), input.getTDim(), input.getBDim());
		int val;
		for (int b = 0; b < haar.getBDim(); b++)
			for (int t = 0; t < haar.getTDim(); t++)
				for (int z = 0; z < haar.getZDim(); z++)
					for (int x = 0; x < haar.getXDim(); x += 2)
						for (int y = 0; y < haar.getYDim(); y += 2) {
							val = input.getPixelByte(x, y, z, t, b);
							val = val * 3;
							val = val - input.getPixelByte(x + 1, y, z, t, b);
							val = val - input.getPixelByte(x, y + 1, z, t, b);
							val = val
									- input.getPixelByte(x + 1, y + 1, z, t, b);
							val = Math.abs(val) / 4;
							haar.setPixelInt(x, y, z, t, b, val);
							haar.setPixelInt(x + 1, y, z, t, b, val);
							haar.setPixelInt(x, y + 1, z, t, b, val);
							haar.setPixelInt(x + 1, y + 1, z, t, b, val);
						}
		return haar;
	}

}
