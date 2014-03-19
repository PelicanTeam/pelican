package fr.unistra.pelican.algorithms.segmentation;

import fr.unistra.pelican.util.Point3D;
import fr.unistra.pelican.util.Point4D;

import java.util.Vector;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;

/**
 * This class performs a watershed segmentation in N dimensions (XYZT) (using
 * the Soille algorithm (with hierarchical queues)
 * 
 * It works by default on Byte resolution. The maximum number of created segment
 * is 2^31-1. It return an IntegerImage, the first segment as label
 * Integer.MIN_VALUE.
 * 
 * @author Aptoula, Derivaux, Lefevre
 */
public class WatershedND extends Algorithm {

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

	private static final int INQUEUE = -3;

	private final Point4D fictitious = new Point4D(-1, -1, -1, -1);
	private int levels;

	/**
	 * Constructor
	 */
	public WatershedND() {
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
	public static Image exec(Image inputImage) {
		return (Image) new WatershedND().process(inputImage);
	}

	public static Image exec(Image inputImage, int resolution) {
		return (Image) new WatershedND().process(inputImage, resolution);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		levels = (int) Math.pow(2, resolution);
		IntegerImage work = new IntegerImage(inputImage.getXDim(), inputImage
				.getYDim(), inputImage.getZDim(), inputImage.getTDim(), 1);
		IntegerImage dist = new IntegerImage(inputImage.getXDim(), inputImage
				.getYDim(), inputImage.getZDim(), inputImage.getTDim(), 1);
		IntegerImage workOut = new IntegerImage(inputImage.getXDim(),
				inputImage.getYDim(), inputImage.getZDim(), inputImage
						.getTDim(), 1);
		outputImage = new IntegerImage(inputImage, false);
		for (int b = 0; b < inputImage.getBDim(); b++) {
			// Create a working Image.
			for (int x = 0; x < inputImage.getXDim(); x++)
				for (int y = 0; y < inputImage.getYDim(); y++)
					for (int z = 0; z < inputImage.getZDim(); z++)
						for (int t = 0; t < inputImage.getTDim(); t++)
							// That's a nice hack, isn't it? No Byte to Integer
							// conversion.
							// Work still have values from 0 to 255.
							work.setPixelInt(x, y, z, t, 0, inputImage
									.getPixelByte(x, y, z, t, b));

			// Initialise the workout image
			workOut.fill(INIT);
			dist.fill(0);
			int currentLabel = WSHED;
			int x, y, z;
			Fifo fifo = new Fifo();
			Point4D p;

			// pixel value distribution,
			Vector[] distro = calculateDistro(work);

			// start flooding
			for (int i = 0; i < levels; i++) {

				System.out.println(i + "/" + (levels-1));

				// geodesic SKIZ of level i - 1 inside level i
				int size = distro[i].size();

				for (int j = 0; j < size; j++) {
					p = (Point4D) distro[i].elementAt(j);

					workOut.setPixelXYZTInt(p.x, p.y, p.z, p.t, MASK);

					if (areThereLabelledNeighbours(workOut, p.x, p.y, p.z, p.t) == true) {
						dist.setPixelXYZTInt(p.x, p.y, p.z, p.t, 1);
						fifo.add(p);
					}
				}

				int curDist = 1;
				fifo.add(fictitious);

				do {
					p = (Point4D) fifo.retrieve();

					if (p.x == -1 && p.y == -1 && p.z == -1 && p.t == -1) {
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
						for (int k = p.x - 1; k <= p.x + 1; k++)
							for (int l = p.z - 1; l <= p.z + 1; l++)
								for (int m = p.t - 1; m <= p.t + 1; m++) {
									if (k < 0 || k >= inputImage.getXDim()
											|| j < 0
											|| j >= inputImage.getYDim()
											|| l < 0
											|| l >= inputImage.getZDim()
											|| m < 0
											|| m >= inputImage.getTDim())
										continue;

									// if the pixel is
									// already labelled
									if (!(j == p.y && k == p.x && l == p.z && m == p.t)
											&& dist.getPixelXYZTInt(k, j, l, m) < curDist
											&& workOut.getPixelXYZTInt(k, j, l,
													m) > WSHED) {
										if (workOut.getPixelXYZTInt(k, j, l, m) > 0) {
											if (workOut.getPixelXYZTInt(p.x,
													p.y, p.z, p.t) == MASK
													|| workOut.getPixelXYZTInt(
															p.x, p.y, p.z, p.t) == WSHED)
												workOut
														.setPixelXYZTInt(
																p.x,
																p.y,
																p.z,
																p.t,
																workOut
																		.getPixelXYZTInt(
																				k,
																				j,
																				l,
																				m));

											else if (workOut.getPixelXYZTInt(
													p.x, p.y, p.z, p.t) != workOut
													.getPixelXYZTInt(k, j, l, m))
												workOut.setPixelXYZTInt(p.x,
														p.y, p.z, p.t, WSHED);

										} else if (workOut.getPixelXYZTInt(p.x,
												p.y, p.z, p.t) == MASK)
											workOut.setPixelXYZTInt(p.x, p.y,
													p.z, p.t, WSHED);

										// if the neighbour is a plateau pixel
									} else if (workOut.getPixelXYZTInt(k, j, l,
											m) == MASK
											&& dist.getPixelXYZTInt(k, j, l, m) == 0) {
										dist.setPixelXYZTInt(k, j, l, m,
												curDist + 1);
										fifo.add(new Point4D(k, j, l, m));
									}
								}
					}
				} while (true);

				// check for new minima
				size = distro[i].size();

				// detect and process new minima at level i
				for (int j = 0; j < size; j++) {
					p = (Point4D) distro[i].elementAt(j);

					// reset distance to 0
					dist.setPixelXYZTInt(p.x, p.y, p.z, p.t, 0);

					// if p is inside a new minimum
					if (workOut.getPixelXYZTInt(p.x, p.y, p.z, p.t) == MASK) {

						// create a new label
						currentLabel++;
						fifo.add(p);
						workOut.setPixelXYZTInt(p.x, p.y, p.z, p.t,
								currentLabel);

						while (fifo.isEmpty() == false) {
							Point4D q = fifo.retrieve();

							// for every pixel in the 80-neighbourhood of
							// q
							for (int n = q.t - 1; n <= q.t + 1; n++)
								for (int m = q.z - 1; m <= q.z + 1; m++)
									for (int l = q.y - 1; l <= q.y + 1; l++)
										for (int k = q.x - 1; k <= q.x + 1; k++) {
											if (k < 0
													|| k >= inputImage
															.getXDim()
													|| l < 0
													|| l >= inputImage
															.getYDim()
													|| m < 0
													|| m >= inputImage
															.getZDim()
													|| n < 0
													|| n >= inputImage
															.getTDim()

											)
												continue;

											if (!(k == q.x && l == q.y
													&& m == q.z && n == q.t)
													&& workOut.getPixelXYZTInt(
															k, l, m, n) == MASK) {
												fifo
														.add(new Point4D(k, l,
																m, n));
												workOut.setPixelXYZTInt(k, l,
														m, n, currentLabel);
											}
										}

						}
					}
				}

				// Copy the result to the outputImage
				for (int _x = 0; _x < inputImage.getXDim(); _x++)
					for (int _y = 0; _y < inputImage.getYDim(); _y++)
						for (int _z = 0; _z < inputImage.getZDim(); _z++)
							for (int _t = 0; _t < inputImage.getTDim(); _t++) {
								// That's a nice hack, isn't it? No Integer to
								// Byte
								// conversion.
								// Values are inside [0,255] if the algo is
								// correct.
								outputImage.setPixelInt(_x, _y, _z, _t, b,
										workOut.getPixelInt(_x, _y, _z, _t, 0)/*
																				 * +
																				 * Integer.MIN_VALUE
																				 */);
							}

			}
		}
		return;
	}

	private Vector[] calculateDistro(IntegerImage img) {
		Vector[] distro = new Vector[levels];

		for (int i = 0; i < levels; i++)
			distro[i] = new Vector();

		for (int x = 0; x < img.getXDim(); x++)
			for (int y = 0; y < img.getYDim(); y++)
				for (int z = 0; z < img.getZDim(); z++)
					for (int t = 0; t < img.getTDim(); t++)
						distro[img.getPixelXYZTInt(x, y, z, t)]
								.add(new Point4D(x, y, z, t));

		return distro;
	}

	private boolean areThereLabelledNeighbours(IntegerImage img, int x, int y,
			int z, int t) throws AlgorithmException {
		for (int m = t - 1; m <= t + 1; m++) {
			if (m >= img.getTDim() || m < 0)
				continue;
			for (int k = z - 1; k <= z + 1; k++) {
				if (k >= img.getZDim() || k < 0)
					continue;
				for (int j = y - 1; j <= y + 1; j++) {
					if (j >= img.getYDim() || j < 0)
						continue;

					for (int i = x - 1; i <= x + 1; i++) {
						if (i >= img.getXDim() || i < 0)
							continue;
						// try{
						if (!(i == x && j == y && k == z && m == t)
								&& img.getPixelXYZTInt(i, j, k, m) >= WSHED)
							return true;

						// }catch(java.lang.ArrayIndexOutOfBoundsException ex){
						// throw new AlgorithmException(
						// "ArrayIndexOutOfBoundsException in
						// areThereLabelledNeighbours"
						// +" with i=" + i + " j=" + j);
						// }
					}
				}
			}
		}
		return false;
	}

	private class Fifo {
		private Vector v;

		Fifo() {
			v = new Vector();
		}

		void add(Object o) {
			v.add(o);
		}

		Point4D retrieve() {
			Object o = v.firstElement();
			v.remove(0);

			return (Point4D) o;
		}

		boolean isEmpty() {
			return (v.size() == 0);
		}

		int size() {
			return v.size();
		}
	}

}
