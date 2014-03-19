package fr.unistra.pelican.algorithms.segmentation;

import java.awt.Point;
import java.util.LinkedList;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.logical.CompareConstant;
import fr.unistra.pelican.algorithms.segmentation.flatzones.BooleanConnectedComponentsLabeling;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToRandomColors;
import fr.unistra.pelican.algorithms.segmentation.labels.RegionSize;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

/**
 * This class performs a marker-based watershed segmentation using the Soille
 * algorithm (with hierarchical queues) and the 0 value for markers.
 * 
 * It works on Byte resolution. The maximum number of created segment is 2^31-1.
 * It return an IntegerImage, the first segment as label Integer.MIN_VALUE.
 * 
 * @author Aptoula, Lefevre
 */
public class MarkerBasedWatershed extends Algorithm {

	/**
	 * The input image
	 */
	public Image inputImage;

	/**
	 * The output image
	 */
	public Image outputImage;

	/**
	 * (optional) 4-connexity watershed
	 */
	public boolean connexity4 = false;

	/**
	 * The optionnal mask image
	 */
	public Image mask = null;

	/**
	 * (optional) The minimum size of markers (number of pixels)
	 */
	public int minSize = 0;

	/*
	 * Private attributes
	 */
	private final int IGNORE = -1;
	private final int NULL = 0;
	private final int GRAY_LEVELS = 256;

	private boolean cpu = false;
	private IntegerImage labels = null;
	private int xdim, ydim;

	/**
	 * Constructor
	 */
	public MarkerBasedWatershed() {
		super.inputs = "inputImage";
		super.outputs = "outputImage";
		super.options = "mask,connexity4,minSize";
	}

	/**
	 * Performs a marker-based watershed segmentation using the Soille algorithm
	 * (with hierarchical queues) and the 0 value for markers
	 * 
	 * @param inputImage
	 *          The input image
	 * @return The output image
	 */
	public static Image exec(Image inputImage) {
		return (Image) new MarkerBasedWatershed().process(inputImage);
	}

	public static Image exec(Image inputImage, Image mask) {
		return (Image) new MarkerBasedWatershed().process(inputImage, mask);
	}

	public static Image exec(Image inputImage, boolean connexity4) {
		return (Image) new MarkerBasedWatershed().process(inputImage, null,
			connexity4);
	}

	public static Image exec(Image inputImage, int minSize) {
		return (Image) new MarkerBasedWatershed().process(inputImage, null, null,
			minSize);
	}

	public static Image exec(Image inputImage, Image mask, boolean connexity4) {
		return (Image) new MarkerBasedWatershed().process(inputImage, mask,
			connexity4);
	}

	public static Image exec(Image inputImage, Image mask, boolean connexity4,
		int minSize) {
		return (Image) new MarkerBasedWatershed().process(inputImage, mask,
			connexity4, minSize);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {

		// Initialisations
		if (mask == null) {
			mask = new BooleanImage(inputImage);
			mask.fill(1);
		}
		xdim = inputImage.getXDim();
		ydim = inputImage.getYDim();
		IntegerImage input = new IntegerImage(xdim, inputImage.getYDim(), 1, 1, 1);
		IntegerImage output = new IntegerImage(xdim, inputImage.getYDim(), 1, 1, 1);
		outputImage = new IntegerImage(xdim, ydim, inputImage.getZDim(), inputImage
			.getTDim(), inputImage.getBDim());
		for (int z = 0; z < inputImage.getZDim(); z++)
			for (int b = 0; b < inputImage.getBDim(); b++)
				for (int t = 0; t < inputImage.getTDim(); t++) {
					// Create a working Image.
					for (int x = 0; x < xdim; x++)
						for (int y = 0; y < ydim; y++)
							// That's a nice hack, isn't it? No Byte to Integer
							// conversion.
							// Work still have values from 0 to 255.
							input.setPixelInt(x, y, 0, 0, 0, inputImage.getPixelByte(x, y, z,
								t, b));
					HierarchicalQueue queue = new HierarchicalQueue(GRAY_LEVELS);
					int currentLabel = 1;
					output.fill(NULL);

					// Identify the markers
					long t1 = 0, t2 = 0;
					if (cpu)
						t1 = System.currentTimeMillis();
					labels = BooleanConnectedComponentsLabeling.exec(CompareConstant.exec(input,
						0, CompareConstant.EQ),
						connexity4 ? BooleanConnectedComponentsLabeling.CONNEXITY4
							: BooleanConnectedComponentsLabeling.CONNEXITY8);
					if (cpu) {
						t2 = System.currentTimeMillis();
						System.err.println("Labeling step: " + ((t2 - t1)) + " ms");
						t1 = System.currentTimeMillis();
					}

					// Optional processing: remove irrelevant markers
					if (minSize > 0) {
						int[] areas = RegionSize.exec(labels);
						int removeLabels = 0;
						for (int a = 0; a < areas.length; a++)
							if (areas[a] < minSize)
								removeLabels++;
						for (int p = 0; p < labels.size(); p++)
							if (areas[labels.getPixelInt(p)] < minSize)
								labels.setPixelInt(p, NULL);
						labels.setProperty("nbRegions", areas.length - removeLabels);
					}

					// Put the marker pixels in the queue
					currentLabel = (Integer) labels.getProperty("nbRegions");
					for (int y = 0; y < ydim; y++)
						for (int x = 0; x < xdim; x++) {
							int p = labels.getPixelXYInt(x, y);
							if (!mask.getPixelXYBoolean(x, y))
								output.setPixelXYInt(x, y, IGNORE);
							else if (p != NULL) {
								output.setPixelXYInt(x, y, p);
								if (bord(x, y, p))
									queue.add(new Point(x, y), NULL);
							}
						}

					if (cpu) {
						t2 = System.currentTimeMillis();
						System.err.println("Marker step: " + ((t2 - t1)) + " ms");
						t1 = System.currentTimeMillis();
					}

					// for (int x = 0; x < inputImage.getXDim(); x++) {
					// for (int y = 0; y < inputImage.getYDim(); y++) {
					// int p = input.getPixelXYInt(x, y);
					// int v = output.getPixelXYInt(x, y);
					// if(!mask.getPixelXYBoolean(x, y))
					// output.setPixelXYInt(x, y, IGNORE);
					// else if (p == NULL && v == NULL) {
					// marker(input, output, x, y, queue, currentLabel);
					// currentLabel++;
					// }
					// }
					// }

					// Perform the flooding
					while (!queue.isEmpty()) {
						Point p = queue.get();
						int label = output.getPixelXYInt(p.x, p.y);
						// get the non labelled 8-neighbours of (x,y)
						Point[] neighbours = getNonLabelledNeighbours(output, p.x, p.y);
						for (int i = 0; i < neighbours.length; i++) {
							// give him the label of p
							output.setPixelXYInt(neighbours[i].x, neighbours[i].y, label);
							// get his gray level
							int val = input.getPixelXYInt(neighbours[i].x, neighbours[i].y);
							// add him to the appropriate queue
							queue.add(neighbours[i], val);
						}
					}
					if (cpu) {
						t2 = System.currentTimeMillis();
						System.err.println("Segmentation step: " + ((t2 - t1)) + " ms");
					}

					// Copy the result to the outputImage
					for (int _x = 0; _x < xdim; _x++)
						for (int _y = 0; _y < ydim; _y++) {
							outputImage.setPixelInt(_x, _y, z, t, b, output.getPixelXYInt(_x,
								_y)/*
										 * + Integer.MIN_VALUE
										 */);
						}

				}

		return;
	}

	private boolean bord(int x, int y, int p) {
		boolean bord = false;
		if (x > 0 && mask.getPixelXYBoolean(x - 1, y)
			&& labels.getPixelXYInt(x - 1, y) != p)
			bord = true;
		else if (x < xdim - 1 && mask.getPixelXYBoolean(x + 1, y)
			&& labels.getPixelXYInt(x + 1, y) != p)
			bord = true;
		else if (y > 0 && mask.getPixelXYBoolean(x, y - 1)
			&& labels.getPixelXYInt(x, y - 1) != p)
			bord = true;
		else if (y < ydim - 1 && mask.getPixelXYBoolean(x, y + 1)
			&& labels.getPixelXYInt(x, y + 1) != p)
			bord = true;
		// Si 4-connexité, on a passé tous les tests, c'est un pixel de
		// bord
		if (connexity4)
			return bord;
		if (x > 0 && y > 0 && mask.getPixelXYBoolean(x - 1, y - 1)
			&& labels.getPixelXYInt(x - 1, y - 1) != p)
			bord = true;
		if (x < xdim - 1 && y > 0 && mask.getPixelXYBoolean(x + 1, y - 1)
			&& labels.getPixelXYInt(x + 1, y - 1) != p)
			bord = true;
		if (x > 0 && y < ydim - 1 && mask.getPixelXYBoolean(x - 1, y + 1)
			&& labels.getPixelXYInt(x - 1, y + 1) != p)
			bord = true;
		if (x < xdim - 1 && y < ydim - 1 && mask.getPixelXYBoolean(x + 1, y + 1)
			&& labels.getPixelXYInt(x + 1, y + 1) != p)
			bord = true;
		// on a passé tous les tests en connexité 8, c'est un pixel de
		// bord
		return bord;
	}

	private void marker(IntegerImage input, IntegerImage output, int x, int y,
		HierarchicalQueue queue, int label) {
		LinkedList fifo = new LinkedList();

		fifo.add(new Point(x, y));

		while (fifo.size() > 0) {
			Point p = (Point) fifo.removeFirst();

			queue.add(p, NULL);
			output.setPixelXYInt(p.x, p.y, label);

			for (int j = p.y - 1; j <= p.y + 1; j++) {
				for (int k = p.x - 1; k <= p.x + 1; k++) {
					if (k < 0 || k >= input.getXDim() || j < 0 || j >= input.getYDim())
						continue;
					if (connexity4 && j != 0 && k != 0)
						continue;
					if (!mask.getPixelXYBoolean(k, j))
						continue;

					if (!(k == p.x && j == p.y) && input.getPixelXYInt(k, j) == NULL
						&& output.getPixelXYInt(k, j) == NULL) {
						int size = fifo.size();
						boolean b = false;

						for (int i = 0; i < size; i++) {
							Point v = (Point) fifo.get(i);
							if (v.x == k && v.y == j)
								b = true;
						}
						if (b == false)
							fifo.add(new Point(k, j));
					}
				}
			}
		}
	}

	private Point[] getNonLabelledNeighbours(IntegerImage output, int x, int y) {
		Point[] neighbours = new Point[8];

		int cnt = 0;

		for (int j = y - 1; j <= y + 1; j++) {
			for (int i = x - 1; i <= x + 1; i++) {
				if (i < 0 || i >= output.getXDim() || j < 0 || j >= output.getYDim())
					continue;
				if (connexity4 && i != 0 && j != 0)
					continue;
				if (!mask.getPixelXYBoolean(i, j))
					continue;
				int z = output.getPixelXYInt(i, j);

				if (!(i == x && j == y) && z == NULL)
					neighbours[cnt++] = new Point(i, j);

			}
		}

		if (cnt < 8) {
			Point[] tmp = new Point[cnt];

			for (int i = 0; i < cnt; i++)
				tmp[i] = neighbours[i];

			neighbours = tmp;
		}

		return neighbours;
	}

	private class HierarchicalQueue {
		private LinkedList[] queue;

		private int current;

		HierarchicalQueue(int size) {
			queue = new LinkedList[size];

			for (int i = 0; i < size; i++)
				queue[i] = new LinkedList();

			current = 0;
		}

		void add(Point p, int val) {
			if (val >= current)
				queue[val].add(p);
			else
				queue[current].add(p);
		}

		Point get() {
			if (queue[current].size() >= 2)
				return (Point) queue[current].removeFirst();
			else if (queue[current].size() == 1) {
				Point p = (Point) queue[current].removeFirst();

				while (current < 255 && queue[current].size() == 0)
					current++;

				return p;

			} else
				return null;
		}

		boolean isEmpty() {
			int sum = 0;

			for (int i = current; i < queue.length; i++)
				sum += queue[i].size();

			return (sum == 0);
		}
	}

}
