package fr.unistra.pelican.algorithms.segmentation;

import java.awt.Point;
import java.util.ArrayList;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;

/**
 * This class performs a watershed segmentation using the Soille algorithm (with
 * hierarchical queues)
 * 
 * It works by default on Byte resolution. The maximum number of created segment
 * is 2^31-1. It return an IntegerImage, the first segment as label
 * Integer.MIN_VALUE.
 * 
 * @author Aptoula, Derivaux, Weber
 */
public class Watershed extends Algorithm {

	/**
	 * The input image
	 */
	public Image inputImage;

	/**
	 * The resolution considered, 8 by default
	 */
	public int resolution = 8;

	/**
	 * The output image
	 */
	public Image outputImage;

	/**
	 * A constant to represent watershed lines
	 */
	public static final int WSHED = 0;

	/*
	 * Private attributes
	 */
	private static final int INIT = -1;

	private static final int MASK = -2;

	private final Point fictitious = new Point(-1, -1);

	/**
	 * Constructor
	 */
	public Watershed() {
		super.inputs = "inputImage";
		super.options = "resolution";
		super.outputs = "outputImage";

	}

	/**
	 * Performs a watershed segmentation using the Soille algorithm (with
	 * hierarchical queues)
	 * 
	 * @param inputImage
	 *            The input image
	 * @return The output image
	 */
	public static IntegerImage exec(Image inputImage) {
		return (IntegerImage) new Watershed().process(inputImage);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		IntegerImage work = new IntegerImage(inputImage.getXDim(), inputImage
				.getYDim(), 1, 1, 1);
		IntegerImage dist = new IntegerImage(inputImage.getXDim(), inputImage
				.getYDim(), 1, 1, 1);
		IntegerImage workOut = new IntegerImage(inputImage.getXDim(),
				inputImage.getYDim(), 1, 1, 1);
		outputImage = new IntegerImage(inputImage, false);
		for (int z = 0; z < inputImage.getZDim(); z++)
			for (int b = 0; b < inputImage.getBDim(); b++)
				for (int t = 0; t < inputImage.getTDim(); t++) {
					// Create a working Image.
					for (int x = 0; x < inputImage.getXDim(); x++)
						for (int y = 0; y < inputImage.getYDim(); y++)
							// That's a nice hack, isn't it? No Byte to Integer
							// conversion.
							// Work still have values from 0 to 255.
							work.setPixelInt(x, y, 0, 0, 0, inputImage
									.getPixelByte(x, y, z, t, b));

					// Initialise the workout image
					workOut.fill(INIT);
					dist.fill(0);
					int currentLabel = WSHED;
					Fifo fifo = new Fifo();
					Point p;

					// pixel value distribution,
					ArrayList<Point>[] distro = calculateDistro(work);

					// start flooding
					for (int i = 0; i < 256; i++) {

						// geodesic SKIZ of level i - 1 inside level i
						int size = distro[i].size();

						for (int j = 0; j < size; j++) {
							p = distro[i].get(j);

							workOut.setPixelXYInt(p.x, p.y, MASK);

							if (areThereLabelledNeighbours(workOut, p.x, p.y) == true) {
								dist.setPixelXYInt(p.x, p.y, 1);
								fifo.add(p);
							}
						}

						int curDist = 1;
						fifo.add(fictitious);

						do {
							p = fifo.retrieve();

							if (p.x == -1 && p.y == -1) {
								if (fifo.isEmpty() == true)
									break;
								else {
									fifo.add(fictitious);
									curDist++;
									p = fifo.retrieve();
								}
							}

							// labelling p by inspecting its neighbours
							for (int j = p.y - 1; j <= p.y + 1; j++) {
								for (int k = p.x - 1; k <= p.x + 1; k++) {
									if (k < 0 || k >= inputImage.getXDim()
											|| j < 0
											|| j >= inputImage.getYDim())
										continue;

									// if the pixel is
									// already labelled
									if (!(j == p.y && k == p.x)
											&& dist.getPixelXYInt(k, j) < curDist
											&& workOut.getPixelXYInt(k, j) > WSHED) {
										if (workOut.getPixelXYInt(k, j) > 0) {
											if (workOut.getPixelXYInt(p.x, p.y) == MASK
													|| workOut.getPixelXYInt(
															p.x, p.y) == WSHED)
												workOut.setPixelXYInt(p.x, p.y,
														workOut.getPixelXYInt(
																k, j));

											else if (workOut.getPixelXYInt(p.x,
													p.y) != workOut
													.getPixelXYInt(k, j))
												workOut.setPixelXYInt(p.x, p.y,
														WSHED);

										} else if (workOut.getPixelXYInt(p.x,
												p.y) == MASK)
											workOut.setPixelXYInt(p.x, p.y,
													WSHED);

										// if the neighbour is a plateau pixel
									} else if (workOut.getPixelXYInt(k, j) == MASK
											&& dist.getPixelXYInt(k, j) == 0) {
										dist.setPixelXYInt(k, j, curDist + 1);
										fifo.add(new Point(k, j));
									}
								}
							}
						} while (true);

						// check for new minima
						size = distro[i].size();

						// detect and process new minima at level i
						for (int j = 0; j < size; j++) {
							p = distro[i].get(j);

							// reset distance to 0
							dist.setPixelXYInt(p.x, p.y, 0);

							// if p is inside a new minimum
							if (workOut.getPixelXYInt(p.x, p.y) == MASK) {

								// create a new label
								currentLabel++;
								fifo.add(p);
								workOut.setPixelXYInt(p.x, p.y, currentLabel);

								while (fifo.isEmpty() == false) {
									Point q = fifo.retrieve();

									// for every pixel in the 8-neighbourhood of
									// q
									for (int l = q.y - 1; l <= q.y + 1; l++) {
										for (int k = q.x - 1; k <= q.x + 1; k++) {
											if (k < 0
													|| k >= inputImage
															.getXDim()
													|| l < 0
													|| l >= inputImage
															.getYDim())
												continue;

											if (!(k == q.x && l == q.y)
													&& workOut.getPixelXYInt(k,
															l) == MASK) {
												fifo.add(new Point(k, l));
												workOut.setPixelXYInt(k, l,
														currentLabel);
											}
										}
									}
								}
							}
						}

						// Copy the result to the outputImage
						for (int _x = 0; _x < inputImage.getXDim(); _x++)
							for (int _y = 0; _y < inputImage.getYDim(); _y++) {
								// That's a nice hack, isn't it? No Integer to
								// Byte
								// conversion.
								// Values are inside [0,255] if the algo is
								// correct.
								outputImage.setPixelInt(_x, _y, z, t, b,
										workOut.getPixelInt(_x, _y, 0, 0, 0)/*
																			 * +
																			 * Integer.MIN_VALUE
																			 */);
							}

					}
				}
		return;
	}

	@SuppressWarnings("unchecked")
	private ArrayList<Point>[] calculateDistro(IntegerImage img) {
		ArrayList<Point>[] distro = (ArrayList<Point>[])new ArrayList[256];

		for (int i = 0; i < 256; i++)
			distro[i] = new ArrayList<Point>();

		for (int x = 0; x < img.getXDim(); x++) {
			for (int y = 0; y < img.getYDim(); y++)
				distro[img.getPixelXYInt(x, y)].add(new Point(x, y));
		}

		return distro;
	}

	private boolean areThereLabelledNeighbours(IntegerImage img, int x, int y)
			throws AlgorithmException {
		for (int j = y - 1; j <= y + 1; j++) {
			if (j >= img.getYDim() || j < 0)
				continue;

			for (int i = x - 1; i <= x + 1; i++) {
				if (i >= img.getXDim() || i < 0)
					continue;
				// try{
				if (!(i == x && j == y) && img.getPixelXYInt(i, j) >= WSHED)
					return true;

				// }catch(java.lang.ArrayIndexOutOfBoundsException ex){
				// throw new AlgorithmException(
				// "ArrayIndexOutOfBoundsException in
				// areThereLabelledNeighbours"
				// +" with i=" + i + " j=" + j);
				// }
			}
		}

		return false;
	}

	private class Fifo {
		private ArrayList<Object> v;

		Fifo() {
			v = new ArrayList<Object>();
		}

		void add(Object o) {
			v.add(o);
		}

		Point retrieve() {
			Object o = v.remove(0);
			return (Point) o;
		}

		boolean isEmpty() {
			return (v.size() == 0);
		}

		int size() {
			return v.size();
		}
	}

}
