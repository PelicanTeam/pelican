package fr.unistra.pelican.algorithms.morphology.gray.geodesic;

import java.awt.Point;
import java.util.Vector;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.segmentation.flatzones.BooleanConnectedComponentsLabeling;

/**
 * Performs a fast gray reconstruction using Fifo queue
 * 
 * Luc Vincent, "Morphological Grayscale Reconstruction in Image Analysis:
 * Applications and Efficient Algorithms", IEEE Transaction on Image Processing,
 * 2:2, pages 176-201, april 1993
 * 
 * @author Lefevre
 */
public class FastGrayReconstruction extends Algorithm {

	/**
	 * Constant for 4-connexity
	 */
	public static int CONNEXITY4 = BooleanConnectedComponentsLabeling.CONNEXITY4;

	/**
	 * Constant for 8-connexity
	 */
	public static int CONNEXITY8 = BooleanConnectedComponentsLabeling.CONNEXITY8;

	/**
	 * marker image
	 */
	public Image marker;

	/**
	 * mask image
	 */
	public Image mask;

	/**
	 * Chosen connexity
	 */
	public int connexity = CONNEXITY8;

	/**
	 * Flag to perform inverse reconstruction
	 */
	public boolean inverse = false;

	/**
	 * Resulting picture
	 */
	public Image outputImage;

	private int xDim;
	private int yDim;
	private int zDim;
	private int tDim;
	private int bDim;

	/**
	 * Constructor
	 * 
	 */
	public FastGrayReconstruction() {
		super.inputs = "marker,mask";
		super.options = "connexity,inverse";
		super.outputs = "outputImage";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = marker.copyImage(true);

		xDim = marker.getXDim();
		yDim = marker.getYDim();
		zDim = marker.getZDim();
		tDim = marker.getTDim();
		bDim = marker.getBDim();
		Fifo f = new Fifo();
		Point p = new Point();

		for (int b = 0; b < bDim; b++)
			for (int t = 0; t < tDim; t++)
				for (int z = 0; z < zDim; z++) {

					// Scan in raster order
					for (int y = 0; y < yDim; y++)
						for (int x = 0; x < xDim; x++)
							if (!inverse)
								outputImage.setPixelByte(x, y, z, t, b, Math
										.min(maxForward(x, y, z, t, b), mask
												.getPixelByte(x, y, z, t, b)));
							else
								outputImage.setPixelByte(x, y, z, t, b, Math
										.max(minForward(x, y, z, t, b), mask
												.getPixelByte(x, y, z, t, b)));

					// Scan in anti-raster order
					for (int y = yDim - 1; y >= 0; y--)
						for (int x = xDim - 1; x >= 0; x--) {
							if (!inverse)
								outputImage.setPixelByte(x, y, z, t, b, Math
										.min(maxBackward(x, y, z, t, b), mask
												.getPixelByte(x, y, z, t, b)));
							else
								outputImage.setPixelByte(x, y, z, t, b, Math
										.max(minBackward(x, y, z, t, b), mask
												.getPixelByte(x, y, z, t, b)));
							if (updateFifo(x, y, z, t, b, inverse))
								f.add(new Point(x, y));
						}

					// Propagation
					while (!f.isEmpty()) {
						p = f.retrieve();
						checkNeighbours(p.x, p.y, z, t, b, f, outputImage
								.getPixelByte(p.x, p.y, z, t, b), inverse);
					}
				}
	}

	private int minForward(int x, int y, int z, int t, int b) {
		int val = outputImage.getPixelByte(x, y, z, t, b);
		if (x > 0)
			val = Math.min(val, outputImage.getPixelByte(x - 1, y, z, t, b));
		if (y > 0)
			val = Math.min(val, outputImage.getPixelByte(x, y - 1, z, t, b));
		if (connexity == CONNEXITY4)
			return val;
		if (x > 0 && y > 0)
			val = Math
					.min(val, outputImage.getPixelByte(x - 1, y - 1, z, t, b));
		if (x < xDim - 1 && y > 0)
			val = Math
					.min(val, outputImage.getPixelByte(x + 1, y - 1, z, t, b));
		return val;
	}

	private int minBackward(int x, int y, int z, int t, int b) {
		int val = outputImage.getPixelByte(x, y, z, t, b);
		if (x < xDim - 1)
			val = Math.min(val, outputImage.getPixelByte(x + 1, y, z, t, b));
		if (y < yDim - 1)
			val = Math.min(val, outputImage.getPixelByte(x, y + 1, z, t, b));
		if (connexity == CONNEXITY4)
			return val;
		if (x < xDim - 1 && y < yDim - 1)
			val = Math
					.min(val, outputImage.getPixelByte(x + 1, y + 1, z, t, b));
		if (x > 0 && y < yDim - 1)
			val = Math
					.min(val, outputImage.getPixelByte(x - 1, y + 1, z, t, b));
		return val;
	}

	private int maxForward(int x, int y, int z, int t, int b) {
		int val = outputImage.getPixelByte(x, y, z, t, b);
		if (x > 0)
			val = Math.max(val, outputImage.getPixelByte(x - 1, y, z, t, b));
		if (y > 0)
			val = Math.max(val, outputImage.getPixelByte(x, y - 1, z, t, b));
		if (connexity == CONNEXITY4)
			return val;
		if (x > 0 && y > 0)
			val = Math
					.max(val, outputImage.getPixelByte(x - 1, y - 1, z, t, b));
		if (x < xDim - 1 && y > 0)
			val = Math
					.max(val, outputImage.getPixelByte(x + 1, y - 1, z, t, b));
		return val;
	}

	private int maxBackward(int x, int y, int z, int t, int b) {
		int val = outputImage.getPixelByte(x, y, z, t, b);
		if (x < xDim - 1)
			val = Math.max(val, outputImage.getPixelByte(x + 1, y, z, t, b));
		if (y < yDim - 1)
			val = Math.max(val, outputImage.getPixelByte(x, y + 1, z, t, b));
		if (connexity == CONNEXITY4)
			return val;
		if (x < xDim - 1 && y < yDim - 1)
			val = Math
					.max(val, outputImage.getPixelByte(x + 1, y + 1, z, t, b));
		if (x > 0 && y < yDim - 1)
			val = Math
					.max(val, outputImage.getPixelByte(x - 1, y + 1, z, t, b));
		return val;
	}

	private boolean checkFifo(int x, int y, int z, int t, int b, int val,
			boolean inverse) {
		if (!inverse)
			return (outputImage.getPixelByte(x, y, z, t, b) < val && outputImage
					.getPixelByte(x, y, z, t, b) < mask.getPixelByte(x, y, z,
					t, b));
		else
			return (outputImage.getPixelByte(x, y, z, t, b) > val && outputImage
					.getPixelByte(x, y, z, t, b) > mask.getPixelByte(x, y, z,
					t, b));
	}

	private boolean updateFifo(int x, int y, int z, int t, int b,
			boolean inverse) {
		int val = outputImage.getPixelByte(x, y, z, t, b);
		if (x < xDim - 1 && checkFifo(x + 1, y, z, t, b, val, inverse))
			return true;
		if (y < yDim - 1 && checkFifo(x, y + 1, z, t, b, val, inverse))
			return true;
		if (connexity == CONNEXITY4)
			return false;
		if (x < xDim - 1 && y < yDim - 1
				&& checkFifo(x + 1, y + 1, z, t, b, val, inverse))
			return true;
		if (x > 0 && y < yDim - 1
				&& checkFifo(x - 1, y + 1, z, t, b, val, inverse))
			return true;
		return false;
	}

	private void checkNeighbours(int x, int y, int z, int t, int b, Fifo f,
			int val, boolean inverse) {
		if (x > 0)
			checkPixel(x - 1, y, z, t, b, f, val, inverse);
		if (y > 0)
			checkPixel(x, y - 1, z, t, b, f, val, inverse);
		if (x < xDim - 1)
			checkPixel(x + 1, y, z, t, b, f, val, inverse);
		if (y < yDim - 1)
			checkPixel(x, y + 1, z, t, b, f, val, inverse);
		if (connexity == CONNEXITY4)
			return;
		if (x > 0 && y > 0)
			checkPixel(x - 1, y - 1, z, t, b, f, val, inverse);
		if (x > 0 && y < yDim - 1)
			checkPixel(x - 1, y + 1, z, t, b, f, val, inverse);
		if (x < xDim - 1 && y > 0)
			checkPixel(x + 1, y - 1, z, t, b, f, val, inverse);
		if (x < xDim - 1 && y < yDim - 1)
			checkPixel(x + 1, y + 1, z, t, b, f, val, inverse);
	}

	private void checkPixel(int x, int y, int z, int t, int b, Fifo f, int val,
			boolean inverse) {
		if (!inverse
				&& outputImage.getPixelByte(x, y, z, t, b) < val
				&& mask.getPixelByte(x, y, z, t, b) != outputImage
						.getPixelByte(x, y, z, t, b)) {
			outputImage.setPixelByte(x, y, z, t, b, Math.min(val, mask
					.getPixelByte(x, y, z, t, b)));
			f.add(new Point(x, y));
		} else if (inverse
				&& outputImage.getPixelByte(x, y, z, t, b) > val
				&& mask.getPixelByte(x, y, z, t, b) != outputImage
						.getPixelByte(x, y, z, t, b)) {
			outputImage.setPixelByte(x, y, z, t, b, Math.max(val, mask
					.getPixelByte(x, y, z, t, b)));
			f.add(new Point(x, y));
		}

	}

	private class Fifo {
		private Vector<Object> v;

		Fifo() {
			v = new Vector<Object>();
		}

		void add(Object o) {
			v.add(o);
		}

		Point retrieve() {
			Object o = v.firstElement();
			v.remove(0);

			return (Point) o;
		}

		boolean isEmpty() {
			return (v.size() == 0);
		}
	}

	/**
	 * Performs a fast binary reconstruction using Fifo queue
	 * 
	 * @param marker
	 *            marker image
	 * @param mask
	 *            mask image
	 * @return reconstructed image
	 */
	public static Image exec(Image marker, Image mask) {
		return (Image) new FastGrayReconstruction().process(marker, mask);
	}

	/**
	 * Performs a fast binary reconstruction using Fifo queue
	 * 
	 * @param marker
	 *            marker image
	 * @param mask
	 *            mask image
	 * @param connexity
	 *            chosen connexity
	 * @return reconstructed image
	 */
	public static Image exec(Image marker, Image mask, int connexity) {
		return (Image) new FastGrayReconstruction().process(marker, mask,
				connexity);
	}

	/**
	 * Performs a fast binary reconstruction using Fifo queue
	 * 
	 * @param marker
	 *            marker image
	 * @param mask
	 *            mask image
	 * @param connexity
	 *            chosen connexity
	 * @param flag
	 *            flag to perform inverse reconstruction
	 * @return reconstructed image
	 */
	public static Image exec(Image marker, Image mask, int connexity,
			boolean flag) {
		return (Image) new FastGrayReconstruction().process(marker, mask,
				connexity, flag);
	}

	/**
	 * Performs a fast binary reconstruction using Fifo queue
	 * 
	 * @param marker
	 *            marker image
	 * @param mask
	 *            mask image
	 * @param inverse
	 *            flag to perform inverse reconstruction
	 * @return reconstructed image
	 */
	public static Image exec(Image marker, Image mask, boolean inverse) {
		return (Image) new FastGrayReconstruction().process(marker, mask,null,
				inverse);
	}

}
