package fr.unistra.pelican.algorithms.segmentation;

import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.conversion.BinaryMasksToLabels;
import fr.unistra.pelican.algorithms.conversion.ProcessChannels;
import fr.unistra.pelican.algorithms.logical.CompareConstant;
import fr.unistra.pelican.algorithms.segmentation.flatzones.BooleanConnectedComponentsLabelingND;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToRandomColors;
import fr.unistra.pelican.algorithms.segmentation.labels.RegionSize;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.Point4D;

/**
 * This class is a N-D version of an extension of the marker-based watershed
 * segmentation to multiple reliefs and markers.
 * 
 * S. Lefevre, Knowledge from markers in watershed segmentation, IAPR
 * International Conference on Computer Analysis of Images and Patterns (CAIP),
 * Vienna, August 2007, Springer-Verlag Lecture Notes in Computer Sciences,
 * Volume 4673, pages 579-586, doi:10.1007/978-3-540-74272-2_72.
 * 
 * @author Lefevre
 */
public class MarkerBasedMultiWatershedND extends Algorithm {

	/**
	 * The input image
	 */
	public Image inputImage;

	/**
	 * The output image
	 */
	public Image outputImage;

	/**
	 * (optional) The mask image
	 */
	public Image mask = null;

	/**
	 * (optional) 4-connexity watershed
	 */
	public boolean connexity4 = false;

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

	private boolean cpu = true;
	private IntegerImage labels = null;
	private int xdim, ydim, zdim, tdim;

	/**
	 * Constructor
	 */
	public MarkerBasedMultiWatershedND() {
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
		return (Image) new MarkerBasedMultiWatershedND().process(inputImage);
	}

	public static Image exec(Image inputImage, Image mask) {
		return (Image) new MarkerBasedMultiWatershedND().process(inputImage, mask);
	}

	public static Image exec(Image inputImage, boolean connexity4) {
		return (Image) new MarkerBasedMultiWatershedND().process(inputImage, null,
			connexity4);
	}

	public static Image exec(Image inputImage, int minSize) {
		return (Image) new MarkerBasedMultiWatershedND().process(inputImage, null,
			null, minSize);
	}

	public static Image exec(Image inputImage, Image mask, boolean connexity4) {
		return (Image) new MarkerBasedMultiWatershedND().process(inputImage, mask,
			connexity4);
	}

	public static Image exec(Image inputImage, Image mask, boolean connexity4,
		int minSize) {
		return (Image) new MarkerBasedMultiWatershedND().process(inputImage, mask,
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
			mask = new BooleanImage(inputImage.getXDim(), inputImage.getYDim(),
				inputImage.getZDim(), inputImage.getTDim(), 1);
			mask.fill(1);
		}
		xdim = inputImage.getXDim();
		ydim = inputImage.getYDim();
		zdim = inputImage.getZDim();
		tdim = inputImage.getTDim();
		int bdim = inputImage.getBDim();
		IntegerImage input = new IntegerImage(inputImage, false);
		IntegerImage output = new IntegerImage(inputImage.getXDim(), inputImage
			.getYDim(), inputImage.getZDim(), inputImage.getTDim(), 2);
		outputImage = new IntegerImage(inputImage.getXDim(), inputImage.getYDim(),
			inputImage.getZDim(), inputImage.getTDim(), 1);
		for (int p = 0; p < inputImage.size(); p++)
			input.setPixelInt(p, inputImage.getPixelByte(p));
		HierarchicalQueue queue = new HierarchicalQueue(GRAY_LEVELS);
		int currentLabel = 1;
		output.fill(NULL);

		// Identify the markers
		long t1 = 0, t2 = 0;
		if (cpu)
			t1 = System.currentTimeMillis();
		BooleanImage markers = CompareConstant.exec(input, 0, CompareConstant.EQ);
		Image classes = BinaryMasksToLabels.exec(markers);
		labels = BooleanConnectedComponentsLabelingND.exec(ProcessChannels.exec(markers,
			ProcessChannels.MAXIMUM),
			connexity4 ? BooleanConnectedComponentsLabelingND.CONNEXITY4
				: BooleanConnectedComponentsLabelingND.CONNEXITY8);
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
		for (int t = 0; t < tdim; t++)
			for (int z = 0; z < zdim; z++)
				for (int y = 0; y < ydim; y++)
					for (int x = 0; x < xdim; x++) {
						int p = labels.getPixelXYZTInt(x, y, z, t);
						int c = classes.getPixelXYZTInt(x, y, z, t) - 1;
						if (!mask.getPixelXYZTBoolean(x, y, z, t))
							output.setPixelXYZTInt(x, y, z, t, IGNORE);
						else if (p != NULL) {
							output.setPixelInt(x, y, z, t, 0, p);
							output.setPixelInt(x, y, z, t, 1, c);
							if (bord(x, y, z, t, p))
								queue.add(new Point4D(x, y, z, t), NULL);
						}
					}

		// for (int t = 0; t < inputImage.getTDim(); t++)
		// for (int z = 0; z < inputImage.getZDim(); z++)
		// for (int y = 0; y < inputImage.getYDim(); y++)
		// for (int x = 0; x < inputImage.getXDim(); x++) {
		// int p = input.getPixelXYZTInt(x, y, z, t);
		// int v = output.getPixelXYZTInt(x, y, z, t);
		// if (!mask.getPixelXYZTBoolean(x, y, z, t))
		// output.setPixelXYZTInt(x, y, z, t, IGNORE);
		// else if (p == NULL && v == NULL) {
		// marker(input, output, x, y, z, t, queue,
		// currentLabel);
		// currentLabel++;
		// }
		// }

		if (cpu) {
			t2 = System.currentTimeMillis();
			System.err.println("Marker step: " + ((t2 - t1)) + " ms");
			t1 = System.currentTimeMillis();
		}

		// Perform the flooding
		// System.err.println("Number of markers : " + currentLabel+ " queue length
		// = " + queue.length());
		while (!queue.isEmpty()) {
			Point4D p = queue.get();
			int label = output.getPixelInt(p.x, p.y, p.z, p.t, 0);
			int band = output.getPixelInt(p.x, p.y, p.z, p.t, 1);
			// get the non labelled 80-neighbours of (x,y,z,t)
			Point4D[] neighbours = getNonLabelledNeighbours(output, p.x, p.y, p.z,
				p.t);
			for (int i = 0; i < neighbours.length; i++) {
				// give him the label of p
				output.setPixelInt(neighbours[i].x, neighbours[i].y, neighbours[i].z,
					neighbours[i].t, 0, label);
				output.setPixelInt(neighbours[i].x, neighbours[i].y, neighbours[i].z,
					neighbours[i].t, 1, band);
				// get his gray level IN THE APPROPRIATE BAND
				int val = input.getPixelInt(neighbours[i].x, neighbours[i].y,
					neighbours[i].z, neighbours[i].t, band);
				// add him to the appropriate queue
				queue.add(neighbours[i], val);
			}
		}

		if (cpu) {
			t2 = System.currentTimeMillis();
			System.err.println("Segmentation step: " + ((t2 - t1)) + " ms");
		}

		// Copy the result to the outputImage
		for (int _t = 0; _t < inputImage.getTDim(); _t++)
			for (int _z = 0; _z < inputImage.getZDim(); _z++)
				for (int _y = 0; _y < inputImage.getYDim(); _y++)
					for (int _x = 0; _x < inputImage.getXDim(); _x++) {
						outputImage.setPixelInt(_x, _y, _z, _t, 0, output.getPixelInt(_x,
							_y, _z, _t, 0)/*
														 * + Integer.MIN_VALUE
														 */);
					}
	}

	private boolean bord(int x, int y, int z, int t, int p) {
		boolean bord = false;
		for (int tt = -1; tt <= 1; tt++)
			for (int zz = -1; zz <= 1; zz++)
				for (int yy = -1; yy <= 1; yy++)
					for (int xx = -1; xx <= 1; xx++)
						if (x + xx >= 0 && x + xx < xdim && y + yy >= 0 && y + yy < ydim
							&& z + zz >= 0 && z + zz < zdim && t + tt >= 0 && t + tt < tdim
							&& mask.getPixelXYZTBoolean(x + xx, y + yy, z + zz, t + tt)
							&& labels.getPixelXYZTInt(x + xx, y + yy, z + zz, t + tt) != p)
							if (xx != 0 || yy != 0 || zz != 0 || tt != 00)
								if (!connexity4
									|| (Math.abs(xx) + Math.abs(yy) + Math.abs(zz) + Math.abs(tt)) == 1)
									bord = true;
		return bord;
	}

	private void marker(IntegerImage input, IntegerImage output, int x, int y,
		int z, int t, HierarchicalQueue queue, int label) {
		LinkedList fifo = new LinkedList();

		fifo.add(new Point4D(x, y, z, t));

		while (fifo.size() > 0) {
			Point4D p = (Point4D) fifo.removeFirst();

			queue.add(p, NULL);
			output.setPixelXYZTInt(p.x, p.y, p.z, p.t, label);

			for (int m = p.t - 1; m <= p.t + 1; m++)
				for (int l = p.z - 1; l <= p.z + 1; l++)
					for (int k = p.x - 1; k <= p.x + 1; k++)
						for (int j = p.y - 1; j <= p.y + 1; j++) {
							if (k < 0 || k >= input.getXDim() || j < 0
								|| j >= input.getYDim() || l < 0 || l >= input.getZDim()
								|| m < 0 || m >= input.getTDim())
								continue;
							if (connexity4 && j != 0 && k != 0 && l != 0 && m != 0)
								continue;
							if (!mask.getPixelXYZTBoolean(k, j, l, m))
								continue;

							if (!(k == p.x && j == p.y && l == p.z && m == p.t)
								&& input.getPixelXYZTInt(k, j, l, m) == NULL
								&& output.getPixelXYZTInt(k, j, l, m) == NULL) {
								int size = fifo.size();
								boolean b = false;

								for (int i = 0; i < size; i++) {
									Point4D v = (Point4D) fifo.get(i);
									if (v.x == k && v.y == j && v.z == l && v.t == m)
										b = true;
								}
								if (b == false)
									fifo.add(new Point4D(k, j, l, m));
							}
						}

		}
	}

	private Point4D[] getNonLabelledNeighbours(IntegerImage output, int x, int y,
		int z, int t) {
		ArrayList<Point4D> neighbours = new ArrayList<Point4D>();

		for (int l = t - 1; l <= t + 1; l++)
			for (int k = z - 1; k <= z + 1; k++)
				for (int j = y - 1; j <= y + 1; j++)
					for (int i = x - 1; i <= x + 1; i++) {
						if (i < 0 || i >= output.getXDim() || j < 0
							|| j >= output.getYDim() || k < 0 || k >= output.getZDim()
							|| l < 0 || l >= output.getTDim())
							continue;
						if (connexity4
							&& (Math.abs(i) + Math.abs(j) + Math.abs(k) + Math.abs(l)) != 1)
							continue;
						if (!mask.getPixelXYZTBoolean(i, j, k, l))
							continue;
						int u = output.getPixelInt(i, j, k, l, 0);

						if (!(i == x && j == y && k == z && l == t) && u == NULL)
							neighbours.add(new Point4D(i, j, k, l));

					}

		return neighbours.toArray(new Point4D[] {});
	}

	private class HierarchicalQueue {
		private LinkedList[] queue;

		private int current;

		private int length;

		HierarchicalQueue(int size) {
			queue = new LinkedList[size];

			for (int i = 0; i < size; i++)
				queue[i] = new LinkedList();

			current = 0;
			length = 0;
		}

		void add(Point4D p, int val) {
			if (val >= current)
				queue[val].add(p);
			else
				queue[current].add(p);
			length++;
		}

		Point4D get() {
			length--;
			if (queue[current].size() >= 2)
				return (Point4D) queue[current].removeFirst();
			else if (queue[current].size() == 1) {
				Point4D p = (Point4D) queue[current].removeFirst();

				while (current < 255 && queue[current].size() == 0)
					current++;

				return p;

			} else
				return null;
		}

		int length() {
			return length;
		}

		boolean isEmpty() {
			int sum = 0;

			for (int i = current; i < queue.length; i++)
				sum += queue[i].size();

			return (sum == 0);
		}
	}

}
