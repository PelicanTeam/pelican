package fr.unistra.pelican.algorithms.morphology.binary;

import java.awt.Point;
import java.util.Stack;
import java.util.Vector;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;

/**
 * Filters 8-connected component from a boolean image with a size criterion
 * (number of pixels)
 * 
 * @author Benjamin Perret, Jonathan Weber
 * 
 */
public class BinaryAreaOpening extends Algorithm {
	/**
	 * Input image
	 */
	public Image inputImage;

	/**
	 * Threshold for connected component size
	 */
	public int thresh;

	/**
	 * Mask image to remember what has been explored
	 */
	private BooleanImage mask;

	/**
	 * X dimension of input image
	 */
	private int dimX;

	/**
	 * Y dimension of input image
	 */
	private int dimY;

	/**
	 * Output Image
	 */
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public BinaryAreaOpening() {
		super.inputs = "inputImage,thresh";
		super.outputs = "outputImage";
		
	}

	/**
	 * Erase the connected component containing s if its size if lower than the
	 * threshold
	 * 
	 * @param s
	 *            Seed of connected component
	 */
	private void filter(Stack<Point> s) {
		Vector<Point> vp = new Vector<Point>();
		int size = 0;
		/**
		 * Build a vector of every points of the connected component
		 */
		while (!s.isEmpty()) {
			Point p = s.pop();
			if (p.x >= 0 && p.x < dimX && p.y >= 0 && p.y < dimY
					&& inputImage.isPresentXY( p.x,p.y )
					&& mask.getPixelXYBoolean(p.x, p.y))// &&
														// inputImage.getPixelXYBoolean(p.x,p.y)
														// )
			{
				mask.setPixelXYBoolean(p.x, p.y, false);
				vp.add(p);
				size++;
				s.push(new Point(p.x + 1, p.y - 1));
				s.push(new Point(p.x + 1, p.y));
				s.push(new Point(p.x + 1, p.y + 1));

				s.push(new Point(p.x - 1, p.y + 1));
				s.push(new Point(p.x - 1, p.y));
				s.push(new Point(p.x - 1, p.y - 1));
				s.push(new Point(p.x, p.y - 1));
				s.push(new Point(p.x, p.y + 1));
			}
		}
		/**
		 * Erase it if under threshold
		 */
		if (size < thresh)
			for (Point p : vp)
				outputImage.setPixelXYBoolean(p.x, p.y, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = inputImage.copyImage(true);
		mask = new BooleanImage(inputImage,true); //inputImage.copyImage(true);
		// mask.fill(1.0);
		dimX = inputImage.getXDim();
		dimY = inputImage.getYDim();
		for (int i = 0; i < dimX; i++)
			for (int j = 0; j < dimY; j++) {
				if ( inputImage.isPresentXY(i,j) && mask.getPixelXYBoolean(i,j) ) {
					Stack<Point> s = new Stack<Point>();
					s.push(new Point(i, j));
					filter(s);
				}

			}

	}

	/**
	 * This method  filters 8-connected component from a boolean image with a size criterion
	 * (number of pixels)
	 * @param inputImage image to be processed
	 * @param thresh Threshold for connected component size
	 * @return filtered picture
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Image> T  exec(T inputImage, Integer thresh)
	{
		return (T) new BinaryAreaOpening().process(inputImage, thresh);
	}
}
