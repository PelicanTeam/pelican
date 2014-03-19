package fr.unistra.pelican.algorithms.applied.video.caption;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.edge.Sobel;
import fr.unistra.pelican.algorithms.statistics.BlockCount;

/**
 * This class identifies caption blocks in still images or video sequences using
 * edge density analysis
 * 
 * S. Lefèvre, N. Vincent, Caption Localisation in Video Sequences by Fusion of
 * Multiple Detectors, IAPR International Conference on Document Analysis and
 * Recognition, Seoul, Korea, September 2005, pages 106-110,
 * doi:10.1109/ICDAR.2005.67
 * 
 * @author Lefevre
 */

public class EdgeDensityDetector extends Algorithm {

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
	 * The threshold compared to sobel values to label edge pixels
	 */
	public int sobelThr = 75;

	/**
	 * The ratio used in the block labeling process
	 */
	public double ratio = 0.15;

	/**
	 * The binary output image
	 */
	public BooleanImage output;

	/**
	 * Default constructor
	 */
	public EdgeDensityDetector() {
		super.inputs = "input";
		super.options = "width,height,sobelThr,ratio";
		super.outputs = "output";
		
	}

	/**
	 * This class identifies caption blocks in still images or video sequences
	 * using edge density analysis
	 * 
	 * @param input
	 *            The color input image
	 * @return The binary output image
	 */
	public static BooleanImage exec(Image input) {
		return (BooleanImage) new EdgeDensityDetector().process(input);
	}

	/**
	 * This class identifies caption blocks in still images or video sequences
	 * using edge density analysis
	 * 
	 * @param input
	 *            The color input image
	 * @param width
	 *            The block width
	 * @param height
	 *            The block height
	 * @param sobelThr
	 *            The threshold compared to sobel values to label edge pixels
	 * @param ratio
	 *            The ratio used in the labeling process
	 * @return The binary output image
	 */
	public static Image exec(Image input, int width, int height, int sobelThr,
			double ratio) {
		return (Image) new EdgeDensityDetector().process(input, width, height,
				sobelThr, ratio);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() {
		BooleanImage label = new BooleanImage(input.getXDim() / width, input
				.getYDim()
				/ height, input.getZDim(), input.getTDim(), input.getBDim());
		label.copyAttributes(input);
		Image sobel, count;
		// Sobel computation
		try {
			sobel = (Image) new Sobel().process(input, Sobel.NORM);
		} catch (PelicanException ex) {
			throw new AlgorithmException(ex.getMessage());
		}
		// Sobel thresholded
		for (int l = 0; l < sobel.size(); l++)
			sobel.setPixelBoolean(l, sobel.getPixelByte(l) >= sobelThr);
		// Count pixels
		try {
			count = (Image) new BlockCount().process(sobel, width, height);
		} catch (PelicanException ex) {
			throw new AlgorithmException(ex.getMessage());
		}
		// Threshold count image
		if (count instanceof IntegerImage)
			for (int l = 0; l < count.size(); l++)
				label.setPixelBoolean(l,
						count.getPixelInt(l) >= (ratio * width * height));
		else
			for (int l = 0; l < count.size(); l++)
				label.setPixelBoolean(l, count.getPixelByte(l) >= (ratio
						* width * height));
		output = label;
	}

}
