package fr.unistra.pelican.algorithms.segmentation;

import java.awt.Point;
import java.util.LinkedList;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.conversion.BinaryMasksToLabels;
import fr.unistra.pelican.algorithms.conversion.ProcessChannels;
import fr.unistra.pelican.algorithms.logical.CompareConstant;
import fr.unistra.pelican.algorithms.segmentation.flatzones.BooleanConnectedComponentsLabeling;


/**
 * This class is an extension of the marker-based watershed segmentation to
 * multiple reliefs and markers.
 * 
 * S. Lefevre, Knowledge from markers in watershed segmentation, IAPR
 * International Conference on Computer Analysis of Images and Patterns (CAIP),
 * Vienna, August 2007, Springer-Verlag Lecture Notes in Computer Sciences,
 * Volume 4673, pages 579-586, doi:10.1007/978-3-540-74272-2_72.
 * 
 * @author Lefevre
 */
public class MarkerBasedMultiWatershed extends Algorithm {

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

	/*
	 * Private attributes
	 */
	private final int IGNORE = -1;
	private final int NULL = 0;
	private final int GRAY_LEVELS = 256;

	private boolean cpu = true;
	private IntegerImage labels = null;
	private int xdim, ydim;

	/**
	 * Constructor
	 */
	public MarkerBasedMultiWatershed() {
		super.inputs = "inputImage";
		super.outputs = "outputImage";
		super.options = "mask,connexity4";
	}

	/**
	 * Extension of the marker-based watershed segmentation to multiple reliefs
	 * and markers
	 * 
	 * @param inputImage
	 *          The input image
	 * @return The output image
	 */
	public static IntegerImage exec(Image inputImage) {
		return (IntegerImage) new MarkerBasedMultiWatershed().process(inputImage);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		if (mask == null) {
			mask = new BooleanImage(inputImage.getXDim(), inputImage.getYDim(),
				inputImage.getZDim(), inputImage.getTDim(), 1);
			mask.fill(1);
		}
		xdim = inputImage.getXDim();
		ydim = inputImage.getYDim();
		IntegerImage input = new IntegerImage(inputImage.getXDim(), inputImage
			.getYDim(), 1, 1, inputImage.getBDim());
		IntegerImage output = new IntegerImage(inputImage.getXDim(), inputImage
			.getYDim(), 1, 1, 2);
		outputImage = new IntegerImage(inputImage.getXDim(), inputImage.getYDim(),
			inputImage.getZDim(), inputImage.getTDim(), 1/* inputImage.getBDim() */);
		for (int z = 0; z < inputImage.getZDim(); z++)
			// Temporarily disable the B dim as we use it for the different
			// markers
			for (int t = 0; t < inputImage.getTDim(); t++) {
				// Create a working Image.
				for (int x = 0; x < inputImage.getXDim(); x++)
					for (int y = 0; y < inputImage.getYDim(); y++)
						for (int b = 0; b < inputImage.getBDim(); b++)
							// That's a nice hack, isn't it? No Byte to Integer
							// conversion.
							// Work still have values from 0 to 255.
							input.setPixelInt(x, y, 0, 0, b, inputImage.getPixelByte(x, y, z,
								t, b));
				HierarchicalQueue queue = new HierarchicalQueue(GRAY_LEVELS);
				int currentLabel = 1;
				output.fill(NULL);

				// Initialise the workout image
				// initialize output image and fill up the queue with marker
				// pixels
				long t1 = 0, t2 = 0;
				if (cpu)
					t1 = System.currentTimeMillis();
				BooleanImage markers = CompareConstant.exec(input, 0,
					CompareConstant.EQ);
				Image classes = BinaryMasksToLabels.exec(markers);
				labels = BooleanConnectedComponentsLabeling.exec(ProcessChannels.exec(markers,
					ProcessChannels.MAXIMUM),
					connexity4 ? BooleanConnectedComponentsLabeling.CONNEXITY4
						: BooleanConnectedComponentsLabeling.CONNEXITY8);
				currentLabel = (Integer) labels.getProperty("nbRegions");
				if (cpu) {
					t2 = System.currentTimeMillis();
					System.err.println("Labeling step: " + ((t2 - t1)) + " ms");
					t1 = System.currentTimeMillis();
				}
				for (int y = 0; y < ydim; y++)
					for (int x = 0; x < xdim; x++) {
						int p = labels.getPixelXYInt(x, y);
						int c = classes.getPixelXYInt(x, y)-1;
						if (!mask.getPixelXYBoolean(x, y))
							output.setPixelXYBInt(x, y, 0, IGNORE);
						else if (p != NULL) {
							output.setPixelXYBInt(x, y, 0, p);
							output.setPixelXYBInt(x, y, 1, c);
							if (bord(x, y, p))
								queue.add(new Point(x, y), NULL);
						}
					}
				if (cpu) {
					t2 = System.currentTimeMillis();
					System.err.println("Marker step: " + ((t2 - t1)) + " ms");
					t1 = System.currentTimeMillis();
				}
				// for (int x = 0; x < inputImage.getXDim(); x++)
				// for (int y = 0; y < inputImage.getYDim(); y++)
				// for (int b = 0; b < inputImage.getBDim(); b++) {
				// int p = input.getPixelXYBInt(x, y, b);
				// int v = output.getPixelXYBInt(x, y, 0);
				// if (!mask.getPixelXYBoolean(x, y))
				// output.setPixelXYBInt(x, y, 0,IGNORE);
				// else if (p == NULL && v == NULL) {
				// marker(input, output, x, y, b, queue, currentLabel);
				// currentLabel++;
				// }
				// }
				// System.err.println("Number of markers : " + (currentLabel - 1));
//				Viewer2D.exec(LabelsToRandomColors.exec(output.getImage4D(0,Image.B)));
//				Viewer2D.exec(LabelsToRandomColors.exec(output.getImage4D(1,Image.B)));

				while (!queue.isEmpty()) {
					Point p = queue.get();
					int label = output.getPixelXYBInt(p.x, p.y, 0);
					int band = output.getPixelXYBInt(p.x, p.y, 1);
					// get the non labelled 8-neighbours of (x,y)
					Point[] neighbours = getNonLabelledNeighbours(output, p.x, p.y);
					for (int i = 0; i < neighbours.length; i++) {
						// give him the label of p
						output.setPixelXYBInt(neighbours[i].x, neighbours[i].y, 0, label);
						output.setPixelXYBInt(neighbours[i].x, neighbours[i].y, 1, band);
						// get his gray level IN THE APPROPRIATE BAND
						int val = input.getPixelXYBInt(neighbours[i].x, neighbours[i].y,
							band);
						// add him to the appropriate queue
						queue.add(neighbours[i], val);
					}
				}
				if (cpu) {
					t2 = System.currentTimeMillis();
					System.err.println("Segmentation step: " + ((t2 - t1)) + " ms");
				}

				// Copy the result to the outputImage
				for (int _x = 0; _x < inputImage.getXDim(); _x++)
					for (int _y = 0; _y < inputImage.getYDim(); _y++) {
						outputImage.setPixelInt(_x, _y, z, t, 0, output.getPixelInt(_x, _y,
							0, 0, 0)/*
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
		// Si 4-connexitï¿œ, on a passï¿œ tous les tests, c'est un pixel de
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
		// on a passï¿œ tous les tests en connexitï¿œ 8, c'est un pixel de
		// bord
		return bord;
	}

	private void marker(IntegerImage input, IntegerImage output, int x, int y,
		int m, HierarchicalQueue queue, int label) {
		LinkedList<Point> fifo = new LinkedList<Point>();

		fifo.add(new Point(x, y));

		while (fifo.size() > 0) {
			Point p = (Point) fifo.removeFirst();

			queue.add(p, NULL);
			output.setPixelXYBInt(p.x, p.y, 0, label);
			output.setPixelXYBInt(p.x, p.y, 1, m);

			for (int j = p.y - 1; j <= p.y + 1; j++) {
				for (int k = p.x - 1; k <= p.x + 1; k++) {
					if (k < 0 || k >= input.getXDim() || j < 0 || j >= input.getYDim())
						continue;
					if (connexity4 && j != 0 && k != 0)
						continue;
					if (!mask.getPixelXYBoolean(k, j))
						continue;

					if (!(k == p.x && j == p.y) && input.getPixelXYBInt(k, j, m) == NULL
						&& output.getPixelXYBInt(k, j, 0) == NULL) {
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
				int z = output.getPixelXYBInt(i, j, 0);

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
		private LinkedList<Point>[] queue;

		private int current;

		HierarchicalQueue(int size) {
			queue = new LinkedList[size];

			for (int i = 0; i < size; i++)
				queue[i] = new LinkedList<Point>();

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
