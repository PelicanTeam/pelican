package fr.unistra.pelican.algorithms.applied.video.caption;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.edge.Sobel;
import fr.unistra.pelican.algorithms.geometric.BlockResampling2D;
import fr.unistra.pelican.algorithms.statistics.BlockCount;

/**
 * This class identifies caption blocks in still images or video sequences using
 * edge regularity analysis
 * 
 * S. Lefèvre, N. Vincent, Caption Localisation in Video Sequences by Fusion of
 * Multiple Detectors, IAPR International Conference on Document Analysis and
 * Recognition, Seoul, Korea, September 2005, pages 106-110,
 * doi:10.1109/ICDAR.2005.67
 * 
 * @author Lefevre
 */

public class EdgeRegularityDetector extends Algorithm {

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
	 * The line width, default value is 1 pixel
	 */
	public int widthLine = 1;

	/**
	 * The ratio used in the line analysis process
	 */
	public double ratioLine = 0.15;

	/**
	 * The binary output image
	 */
	public BooleanImage output;

	/**
	 * Default constructor
	 */
	public EdgeRegularityDetector() {
		super.inputs = "input";
		super.options = "width,height,sobelThr,ratio,widthLine,ratioLine";
		super.outputs = "output";
		
	}

	/**
	 * This class identifies caption blocks in still images or video sequences
	 * using edge regularity analysis
	 * 
	 * @param input
	 *            The color input image
	 * @return The binary output image
	 */
	public static BooleanImage exec(Image input) {
		return (BooleanImage) new EdgeRegularityDetector().process(input);
	}

	/**
	 * This class identifies caption blocks in still images or video sequences
	 * using edge regularityanalysis
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
	 * @param widthLine
	 *            The line width
	 * @param ratioLine
	 *            The ratio used in the line analysis process
	 * @return The binary output image
	 */
	public static Image exec(Image input, int width, int height, int sobelThr,
			double ratio, int widthLine, double ratioLine) {
		return (Image) new EdgeRegularityDetector().process(input, width,
				height, sobelThr, ratio, widthLine, ratioLine);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() {
		BooleanImage label = new BooleanImage(input.getXDim() / widthLine,
				input.getYDim() / height, input.getZDim(), input.getTDim(),
				input.getBDim());
		output = new BooleanImage(input.getXDim() / width, input.getYDim()
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
		// Count pixels on limited vertical zones
		try {
			count = (Image) new BlockCount().process(sobel, widthLine, height);
		} catch (PelicanException ex) {
			throw new AlgorithmException(ex.getMessage());
		}
		// Threshold count image
		if (count instanceof IntegerImage)
			for (int l = 0; l < count.size(); l++)
				label.setPixelBoolean(l, count.getPixelInt(l) >= (ratioLine
						* widthLine * height));
		else
			for (int l = 0; l < count.size(); l++)
				label.setPixelBoolean(l, count.getPixelByte(l) >= (ratioLine
						* widthLine * height));
		// Eliminate neighbours
		eliminateNeighbours(label);
		try {
			label = (BooleanImage) BlockResampling2D.exec(label, widthLine,
					height,true);
		} catch (PelicanException ex) {
			throw new AlgorithmException(ex.getMessage());
		}
		// Count pixels
		try {
			count = (Image) new BlockCount().process(label, width, height);
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

	private void eliminateNeighbours(BooleanImage img) {
		int MINX = 0;
		int MAXX = 10;
		int previous;
		int inter;
		for (int b = 0; b < img.getBDim(); b++)
			for (int t = 0; t < img.getTDim(); t++)
				for (int z = 0; z < img.getZDim(); z++)
					for (int y = 0; y < img.getYDim(); y++) {
						previous = -1;
						for (int x = 0; x < img.getXDim(); x++)
							if (img.getPixelBoolean(x, y, z, t, b)) {
								if (previous == -1)
									inter = MINX;
								else
									inter = x - previous;
								previous = x;
								if (inter < MINX || inter > MAXX)
									img.setPixelBoolean(x, y, z, t, b, false);
							}
					}
	}

}
