package fr.unistra.pelican.algorithms.morphology.vectorial.geodesic;

import java.awt.Point;
import java.util.Vector;
import java.util.Arrays;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.InvalidParameterException;
import fr.unistra.pelican.algorithms.segmentation.flatzones.BooleanConnectedComponentsLabeling;
import fr.unistra.pelican.util.vectorial.orders.VectorialOrdering;

/**
 * Performs a fast vectorial reconstruction using Fifo queue
 * 
 * Adaptation to the vectorial case of the following algorithm for grayscale
 * images: Luc Vincent, "Morphological Grayscale Reconstruction in Image
 * Analysis: Applications and Efficient Algorithms", IEEE Transaction on Image
 * Processing, 2:2, pages 176-201, april 1993
 * 
 * @author Lefevre
 * 
 * FIXME: the algorithm works only with vector-preserving orderings
 */
public class FastVectorialReconstruction extends Algorithm {

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
	 * The vectorial ordering
	 */
	public VectorialOrdering vo;

	/**
	 * Chosen connexity
	 */
	public int connexity = CONNEXITY8;

	/**
	 * Resulting picture
	 */
	public Image outputImage;

	private int xDim;
	private int yDim;
	private int zDim;
	private int tDim;
	private int bDim;
	private double[][] tab = new double[2][];

	/**
	 * Constructor
	 * 
	 */
	public FastVectorialReconstruction() {
		super.inputs = "marker,mask,vo";
		super.options = "connexity";
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

		// Check if marker and mask have same size
		if (marker.size() != mask.size())
			throw new InvalidParameterException(
				"Marker and Mask do not have the same size");

		for (int t = 0; t < tDim; t++)
			for (int z = 0; z < zDim; z++) {

				// Scan in raster order
				for (int y = 0; y < yDim; y++)
					for (int x = 0; x < xDim; x++) {
						outputImage.setVectorPixelXYZTDouble(x, y, z, t, min(maxForward(x,
							y, z, t), mask.getVectorPixelXYZTDouble(x, y, z, t)));
					}
				// Scan in anti-raster order
				for (int y = yDim - 1; y >= 0; y--)
					for (int x = xDim - 1; x >= 0; x--) {
						outputImage.setVectorPixelXYZTDouble(x, y, z, t, min(maxBackward(x,
							y, z, t), mask.getVectorPixelXYZTDouble(x, y, z, t)));
						if (updateFifo(x, y, z, t))
							f.add(new Point(x, y));
					}

				if (f.isEmpty())
					System.err
						.println("Problem: FIFO empty in FastVectorialReconstruction");
				// Propagation
				while (!f.isEmpty()) {
					//System.out.println(f.size());
					p = f.retrieve();
					checkNeighbours(p.x, p.y, z, t, f, outputImage
						.getVectorPixelXYZTDouble(p.x, p.y, z, t));
				}

			}
	}

	private double[] maxForward(int x, int y, int z, int t) {
		double val[] = outputImage.getVectorPixelXYZTDouble(x, y, z, t);
		if (x > 0)
			val = max(val, outputImage.getVectorPixelXYZTDouble(x - 1, y, z, t));
		if (y > 0)
			val = max(val, outputImage.getVectorPixelXYZTDouble(x, y - 1, z, t));
		if (connexity == CONNEXITY4)
			return val;
		if (x > 0 && y > 0)
			val = max(val, outputImage.getVectorPixelXYZTDouble(x - 1, y - 1, z, t));
		if (x < xDim - 1 && y > 0)
			val = max(val, outputImage.getVectorPixelXYZTDouble(x + 1, y - 1, z, t));
		return val;
	}

	private double[] minForward(int x, int y, int z, int t) {
		double val[] = outputImage.getVectorPixelXYZTDouble(x, y, z, t);
		if (x > 0)
			val = min(val, outputImage.getVectorPixelXYZTDouble(x - 1, y, z, t));
		if (y > 0)
			val = min(val, outputImage.getVectorPixelXYZTDouble(x, y - 1, z, t));
		if (connexity == CONNEXITY4)
			return val;
		if (x > 0 && y > 0)
			val = min(val, outputImage.getVectorPixelXYZTDouble(x - 1, y - 1, z, t));
		if (x < xDim - 1 && y > 0)
			val = min(val, outputImage.getVectorPixelXYZTDouble(x + 1, y - 1, z, t));
		return val;
	}

	private double[] maxBackward(int x, int y, int z, int t) {
		double val[] = outputImage.getVectorPixelXYZTDouble(x, y, z, t);
		if (x < xDim - 1)
			val = max(val, outputImage.getVectorPixelXYZTDouble(x + 1, y, z, t));
		if (y < yDim - 1)
			val = max(val, outputImage.getVectorPixelXYZTDouble(x, y + 1, z, t));
		if (connexity == CONNEXITY4)
			return val;
		if (x < xDim - 1 && y < yDim - 1)
			val = max(val, outputImage.getVectorPixelXYZTDouble(x + 1, y + 1, z, t));
		if (x > 0 && y < yDim - 1)
			val = max(val, outputImage.getVectorPixelXYZTDouble(x - 1, y + 1, z, t));
		return val;
	}

	private double[] minBackward(int x, int y, int z, int t) {
		double val[] = outputImage.getVectorPixelXYZTDouble(x, y, z, t);
		if (x < xDim - 1)
			val = min(val, outputImage.getVectorPixelXYZTDouble(x + 1, y, z, t));
		if (y < yDim - 1)
			val = min(val, outputImage.getVectorPixelXYZTDouble(x, y + 1, z, t));
		if (connexity == CONNEXITY4)
			return val;
		if (x < xDim - 1 && y < yDim - 1)
			val = min(val, outputImage.getVectorPixelXYZTDouble(x + 1, y + 1, z, t));
		if (x > 0 && y < yDim - 1)
			val = min(val, outputImage.getVectorPixelXYZTDouble(x - 1, y + 1, z, t));
		return val;
	}

	private boolean inf(double[] a, double[] b) {
		// return Arrays.equals(a,vo.min(new double[][]{a,b}));
		tab[0] = a;
		tab[1] = b;
		return !Arrays.equals(a, vo.max(tab));
		// return (Arrays.equals(a, vo.min(tab))&&Arrays.equals(b,vo.max(tab)));
	}

	private boolean sup(double[] a, double[] b) {
		// return Arrays.equals(a,vo.max(new double[][]{a,b}));
		tab[0] = a;
		tab[1] = b;
		return !Arrays.equals(a, vo.min(tab));
		// return (Arrays.equals(a, vo.max(tab))&&Arrays.equals(b,vo.min(tab)));

	}

	private double[] min(double[] a, double[] b) {
		// return vo.min(new double[][]{a,b});
		tab[0] = a;
		tab[1] = b;
		return vo.min(tab);
	}

	private double[] max(double[] a, double[] b) {
		// return vo.max(new double[][]{a,b});
		tab[0] = a;
		tab[1] = b;
		return vo.max(tab);
	}

	private boolean checkFifo(int x, int y, int z, int t, double[] val) {
		return (inf(outputImage.getVectorPixelXYZTDouble(x, y, z, t), val) && inf(
			outputImage.getVectorPixelXYZTDouble(x, y, z, t), mask
				.getVectorPixelXYZTDouble(x, y, z, t)));
	}

	private boolean updateFifo(int x, int y, int z, int t) {
		double val[] = outputImage.getVectorPixelXYZTDouble(x, y, z, t);
		if (x < xDim - 1 && checkFifo(x + 1, y, z, t, val))
			return true;
		if (y < yDim - 1 && checkFifo(x, y + 1, z, t, val))
			return true;
		if (connexity == CONNEXITY4)
			return false;
		if (x < xDim - 1 && y < yDim - 1 && checkFifo(x + 1, y + 1, z, t, val))
			return true;
		if (x > 0 && y < yDim - 1 && checkFifo(x - 1, y + 1, z, t, val))
			return true;
		return false;
	}

	private void checkNeighbours(int x, int y, int z, int t, Fifo f, double[] val) {
		if (x > 0)
			checkPixel(x - 1, y, z, t, f, val);
		if (y > 0)
			checkPixel(x, y - 1, z, t, f, val);
		if (x < xDim - 1)
			checkPixel(x + 1, y, z, t, f, val);
		if (y < yDim - 1)
			checkPixel(x, y + 1, z, t, f, val);
		if (connexity == CONNEXITY4)
			return;
		if (x > 0 && y > 0)
			checkPixel(x - 1, y - 1, z, t, f, val);
		if (x > 0 && y < yDim - 1)
			checkPixel(x - 1, y + 1, z, t, f, val);
		if (x < xDim - 1 && y > 0)
			checkPixel(x + 1, y - 1, z, t, f, val);
		if (x < xDim - 1 && y < yDim - 1)
			checkPixel(x + 1, y + 1, z, t, f, val);
	}

	// FIXME: seems not to work with vectorial images
	private void checkPixel(int x, int y, int z, int t, Fifo f, double[] val) {
		if (inf(outputImage.getVectorPixelXYZTDouble(x, y, z, t), val)
			&& !Arrays.equals(mask.getVectorPixelXYZTDouble(x, y, z, t), outputImage
				.getVectorPixelXYZTDouble(x, y, z, t))) {
			outputImage.setVectorPixelXYZTDouble(x, y, z, t, min(val, mask
				.getVectorPixelXYZTDouble(x, y, z, t)));
			// FIXME: is this test necessary ???
			//if(!f.contains(new Point(x,y)))			
				f.add(new Point(x, y));
		}

	}

	private class Fifo {
		private Vector<Point> v;

		Fifo() {
			v = new Vector<Point>();
		}

		boolean contains(Point o) {
			return v.contains(o);
		}

		void add(Point o) {
			v.add(o);
		}

		Point retrieve() {
			Point o = v.firstElement();
			v.remove(0);

			return o;
		}

		boolean isEmpty() {
			return (v.size() == 0);
		}

		int size() {
			return v.size();
		}
	}

	/**
	 * Performs a fast vectorial reconstruction using Fifo queue
	 * 
	 * @param marker
	 *          marker image
	 * @param mask
	 *          mask image
	 * @param vo
	 *          vectorial ordering
	 * @return reconstructed image
	 */
	public static Image exec(Image marker, Image mask, VectorialOrdering vo) {
		return (Image) new FastVectorialReconstruction().process(marker, mask, vo);
	}

	/**
	 * Performs a fast vectorial reconstruction using Fifo queue
	 * 
	 * @param marker
	 *          marker image
	 * @param mask
	 *          mask image
	 * @param vo
	 *          vectorial ordering
	 * @param connexity
	 *          chosen connexity
	 * @return reconstructed image
	 */
	public static Image exec(Image marker, Image mask, VectorialOrdering vo,
		int connexity) {
		return (Image) new FastVectorialReconstruction().process(marker, mask, vo,
			connexity);
	}
}
