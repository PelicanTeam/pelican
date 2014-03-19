package fr.unistra.pelican.algorithms.morphology.binary.geodesic;

import java.awt.Point;
import java.util.Vector;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.segmentation.flatzones.BooleanConnectedComponentsLabeling;

/**
 * Performs a fast binary reconstruction using Fifo queue
 * 
 * Luc Vincent, "Morphological Grayscale Reconstruction in Image Analysis:
 * Applications and Efficient Algorithms", IEEE Transaction on Image Processing,
 * 2:2, pages 176-201, april 1993
 * 
 * @author Lefevre
 */
public class FastBinaryReconstruction extends Algorithm {

	/**
	 * Constant for 4-connexity
	 */
	public static int CONNEXITY4 = BooleanConnectedComponentsLabeling.CONNEXITY4;

	/**
	 * Constant for 8-connexity
	 */
	public static int CONNEXITY8 = BooleanConnectedComponentsLabeling.CONNEXITY8;

	/**
	 * Marker image
	 */
	public Image marker;

	/**
	 * Mask image
	 */
	public Image mask;

	/**
	 * Chosen connexity
	 */
	public int connexity=CONNEXITY8;

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
	public FastBinaryReconstruction() {
		super.inputs = "marker,mask";
		super.options="connexity";
		super.outputs = "outputImage";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		//outputImage = new BooleanImage(marker, true);
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

					// Initialize the queue with border pixels of the marker
					for (int y = 0; y < yDim - 0; y++)
						for (int x = 0; x < xDim - 0; x++)
							if (outputImage.getPixelBoolean(x, y, z, t, b)
									&& mask.getPixelBoolean(x, y, z, t, b)
									&& isBoundaryPixel(x, y, z, t, b))
								f.add(new Point(x, y));

					// Propagation
					while (!f.isEmpty()) {
						p = f.retrieve();
						checkNeighbours(p.x, p.y, z, t, b, f);
					}

				}
	}

	private void checkNeighbours(int x, int y, int z, int t, int b, Fifo f) {
		if (x > 0)
			checkPixel(x - 1, y, z, t, b, f);
		if (y > 0)
			checkPixel(x, y - 1, z, t, b, f);
		if (x < xDim - 1)
			checkPixel(x + 1, y, z, t, b, f);
		if (y < yDim - 1)
			checkPixel(x, y + 1, z, t, b, f);
		if (connexity == CONNEXITY4)
			return;
		if (x > 0 && y > 0)
			checkPixel(x - 1, y - 1, z, t, b, f);
		if (x > 0 && y < yDim - 1)
			checkPixel(x - 1, y + 1, z, t, b, f);
		if (x < xDim - 1 && y > 0)
			checkPixel(x + 1, y - 1, z, t, b, f);
		if (x < xDim - 1 && y < yDim - 1)
			checkPixel(x + 1, y + 1, z, t, b, f);
	}

	private void checkPixel(int x, int y, int z, int t, int b, Fifo f) {
		if (!outputImage.getPixelBoolean(x, y, z, t, b)
				&& mask.getPixelBoolean(x, y, z, t, b)) {
			outputImage.setPixelBoolean(x, y, z, t, b, true);
			f.add(new Point(x, y));
		}

	}

	private boolean isBoundaryPixel(int x, int y, int z, int t, int b) {
		if (x>0 && !outputImage.getPixelBoolean(x - 1, y, z, t, b))
			return true;
		if (x<xDim-1 && !outputImage.getPixelBoolean(x + 1, y, z, t, b))
			return true;
		if (y>0 && !outputImage.getPixelBoolean(x, y - 1, z, t, b))
			return true;
		if (y<yDim-1 && !outputImage.getPixelBoolean(x - 1, y + 1, z, t, b))
			return true;
		if (connexity == CONNEXITY4)
			return false;
		if (x>0 && y>0 && !outputImage.getPixelBoolean(x - 1, y - 1, z, t, b))
			return true;
		if (x<xDim-1 && y>0 && !outputImage.getPixelBoolean(x + 1, y - 1, z, t, b))
			return true;
		if (x>0 && y<yDim-1 && !outputImage.getPixelBoolean(x - 1, y + 1, z, t, b))
			return true;
		if (x<xDim-1 && y<yDim-1 && !outputImage.getPixelBoolean(x + 1, y + 1, z, t, b))
			return true;
		return false;
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
	 * @param connexity
	 *            chosen connexity
	 * @return reconstructed image
	 */
	public static Image exec(Image marker, Image mask, Integer connexity) {
		return (Image) new FastBinaryReconstruction().process(marker,
				mask, connexity);
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
		return (Image) new FastBinaryReconstruction().process(marker,
				mask);
	}

}
