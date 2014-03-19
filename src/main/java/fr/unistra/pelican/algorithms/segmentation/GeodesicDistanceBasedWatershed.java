package fr.unistra.pelican.algorithms.segmentation;

import java.awt.Point;
import java.util.Arrays;
import java.util.LinkedList;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.arithmetic.AdditionConstantChecked;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.io.SamplesLoader;
import fr.unistra.pelican.algorithms.segmentation.labels.DrawFrontiersOnImage;
import fr.unistra.pelican.algorithms.segmentation.labels.FrontiersFromSegmentation;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.HierarchicalQueue;
import fr.unistra.pelican.util.Point4D;
import fr.unistra.pelican.util.Tools;

/**
 * This class realize a watershed segmentation using the value 0 for markers. It
 * considers an image support per marker. This class work on a byte resolution.
 * The maximum number of created segment is 2^31-1. It return an IntegerImage,
 * the first segment as label Integer.MIN_VALUE.
 * 
 * @author Lefevre
 */
public class GeodesicDistanceBasedWatershed extends Algorithm {

	/*
	 * Input Image
	 */
	public Image inputImage;

	public Point4D[] initialCenters;

	/*
	 * Output Image
	 */
	public Image outputImage;

	public boolean trueDistance = true;

	/**
	 * Flag to compute hue-based distance
	 */
	public boolean hue = false;

	public HierarchicalQueue queue = null;

	private final int NULL = 0;

	private boolean DEBUG = false;
	private boolean CPU = false;

	/**
	 * Constructor
	 * 
	 */
	public GeodesicDistanceBasedWatershed() {
		super.inputs = "inputImage,initialCenters";
		super.outputs = "outputImage";
		super.options = "trueDistance,hue,queue";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		// double TEMP=0;
		long t1, t2;
		t1 = System.currentTimeMillis();

		Point4D[] centers = initialCenters.clone();
		Image input = new IntegerImage(inputImage.getXDim(), inputImage.getYDim(),
			1, 1, inputImage.getBDim());
		IntegerImage output = new IntegerImage(inputImage.getXDim(), inputImage
			.getYDim(), 1, 1, 3);// label, distance, candidate
		outputImage = new IntegerImage(inputImage.getXDim(), inputImage.getYDim(),
			inputImage.getZDim(), inputImage.getTDim(), 2/* inputImage.getBDim() */);
		for (int z = 0; z < inputImage.getZDim(); z++)
			// Temporarily disable the B dim as we may compute mutiband geodesic
			// distance ???
			for (int t = 0; t < inputImage.getTDim(); t++) {
				t2 = System.currentTimeMillis();
				if (CPU)
					System.out.println((t2 - t1) / 1000 + " ms pour phase 0");
				t1 = t2;

				// Create a working Image.
				for (int x = 0; x < inputImage.getXDim(); x++)
					for (int y = 0; y < inputImage.getYDim(); y++)
						for (int b = 0; b < inputImage.getBDim(); b++)
							// That's a nice hack, isn't it? No Byte to Integer
							// conversion.
							// Work still have values from 0 to 255.
							input.setPixelInt(x, y, 0, 0, b, inputImage.getPixelByte(x, y, z,
								t, b));
				input = inputImage.getImage4D(0, Image.T);
				int scale = Math.max(inputImage.getXDim(), inputImage.getYDim());

				if (queue == null)
					queue = new HierarchicalQueue(scale * scale * 255);
				else
					queue.reset();

				int currentLabel = 1;

				output.fill(NULL);

				// Initialise the workout image
				// initialize output image and fill up the queue with marker
				// pixels
				/*
				 * for (int x = 0; x < inputImage.getXDim(); x++) for (int y = 0; y <
				 * inputImage.getYDim(); y++) { boolean ok = true; for (int b = 0; b <
				 * inputImage.getBDim(); b++) if (input.getPixelXYBInt(x, y, b) != NULL)
				 * ok = false; int v = output.getPixelXYBInt(x, y, 0);
				 * 
				 * if (ok && v == NULL) { marker(input, output, x, y, queue,
				 * currentLabel); currentLabel++; } }
				 */

				Arrays.sort(centers);
				for (int i = 0; i < centers.length; i++) {
					Point4D p = centers[i];
					int x = p.x;
					int y = p.y;
					int v = output.getPixelXYBInt(x, y, 0);
					if (v == NULL) {
						marker(input, centers, output, x, y, queue, currentLabel);
						currentLabel++;
					}
				}

				System.err.println("Number of markers : " + (currentLabel - 1));
				int labeled = 0;

				// Viewer2D.exec(output.scaleToVisibleRange(),"tmp");
				int current = 0;
				Point p = null;

				t2 = System.currentTimeMillis();
				if (CPU)
					System.out.println((t2 - t1) / 1000 + " ms pour phase 1");
				t1 = t2;

				while (!queue.isEmpty()) {
					current = queue.getCurrent();
					p = queue.get();
					if (DEBUG)
						System.out.println("GET " + p.getX() + "," + p.getY());

					// Get the label and check if it has not been labeled before
					if (output.getPixelXYBInt(p.x, p.y, 0) != NULL)
						continue;

					if (labeled % (input.size() / input.getBDim() / 10) == 0)
						System.out.print('.');
					// if (labeled % (input.size() / input.getBDim() / 10) == 0) {
					// System.out.println(labeled + " on "
					// + (input.size() / input.getBDim()) + " "
					// + queue.getCurrent() + " / "
					// + queue.getNumber());
					// }

					// Definitely set the label from the candidate
					int label = output.getPixelXYBInt(p.x, p.y, 2);
					output.setPixelXYBInt(p.x, p.y, 0, label);
					labeled++;

					// get the non labelled 8-neighbours of (x,y)
					Point[] neighbours = getNonLabelledNeighbours(output, p.x, p.y);

					for (int i = 0; i < neighbours.length; i++) {

						// get the current distance for this neighbour
						int ndist = output.getPixelXYBInt(neighbours[i].x, neighbours[i].y,
							1);

						double val = 0;
						if (!hue) {
							// compute the geodesic distance between p and its
							// neighbor IN THE APPROPRIATE BAND
							for (int b = 0; b < input.getBDim(); b++) {
								double val1 = input.getPixelXYBDouble(neighbours[i].x,
									neighbours[i].y, b);
								double val2 = input.getPixelXYBDouble(p.x, p.y, b);
								val += (val1 - val2)*(val1-val2);
//								int val1 = input.getPixelXYBByte(neighbours[i].x,
//									neighbours[i].y, b);
//								int val2 = input.getPixelXYBByte(p.x, p.y, b);
//								val += Math.abs(val1 - val2);
//								System.out.println(val1+" "+val2);
							}
//							System.out.println(val);
//							System.out.println(input);
							val/=input.getBDim();
							val=Math.sqrt(val);
							val*=255;
						} else {
//							System.out.println("*");
							// compute hue-base distance
							val = Tools.HSLDistance(input.getVectorPixelXYZDouble(
								neighbours[i].x, neighbours[i].y, 0), input
								.getVectorPixelXYZDouble(p.x, p.y, 0));
							val = Math.ceil(255 * val);
							if (val == 0
								&& Tools.HSLDistance(inputImage.getVectorPixelXYZDouble(
									neighbours[i].x, neighbours[i].y, 0), inputImage
									.getVectorPixelXYZDouble(p.x, p.y, 0)) != 0)
								System.out.println(val);
						}
						if (trueDistance)
							val = scale * val + 1;// val += 1; // pour la distance
						// topographique de Philipp
						int pdist = (int) val + current;// queue.getCurrent();
						// update distance and candidate if necessary
						if (ndist == 0 || pdist < ndist) {
							output.setPixelXYBInt(neighbours[i].x, neighbours[i].y, 1, pdist);
							output.setPixelXYBInt(neighbours[i].x, neighbours[i].y, 2, label);
							// add him to the appropriate queue
							queue.add(neighbours[i], pdist);

						}
						if (DEBUG) {
							if (ndist == 0 || pdist < ndist)
								System.out.println("SET " + neighbours[i].getX() + ","
									+ neighbours[i].getY() + ":" + pdist + "(" + val + "|"
									+ current/* queue.getCurrent() */+ ")");
							else
								System.out.println("NOT " + neighbours[i].getX() + ","
									+ neighbours[i].getY() + ":" + pdist + "/" + ndist + "("
									+ val + "|" + current/* queue.getCurrent() */+ ")");
						}
					}
				}
				t2 = System.currentTimeMillis();
				if (CPU)
					System.out.println((t2 - t1) / 1000 + " ms pour phase 2");

				// System.out.println();

				// Copy the result to the outputImage (label + distance)
				for (int _x = 0; _x < inputImage.getXDim(); _x++)
					for (int _y = 0; _y < inputImage.getYDim(); _y++) {
						outputImage.setPixelInt(_x, _y, z, t, 0, output.getPixelInt(_x, _y,
							0, 0, 0)/*
											 * + Integer.MIN_VALUE
											 */);
						outputImage.setPixelInt(_x, _y, z, t, 1, output.getPixelInt(_x, _y,
							0, 0, 1)/*
											 * + Integer.MIN_VALUE
											 */);
					}

			}

		// System.out.println(TEMP);
		return;
	}

	private void marker(Image input, Point4D[] centers, IntegerImage output,
		int x, int y, HierarchicalQueue queue, int label) {
		LinkedList<Point> fifo = new LinkedList<Point>();

		fifo.add(new Point(x, y));

		while (fifo.size() > 0) {
			Point p = (Point) fifo.removeFirst();

			queue.add(p, NULL);
			if (DEBUG)
				System.out.println("SET " + p.getX() + "," + p.getY() + ":" + NULL);
			// output.setPixelXYBInt(p.x,p.y,0,label);
			output.setPixelXYBInt(p.x, p.y, 1, 1);
			output.setPixelXYBInt(p.x, p.y, 2, label);

			for (int j = p.y - 1; j <= p.y + 1; j++) {
				for (int k = p.x - 1; k <= p.x + 1; k++) {
					if (k < 0 || k >= input.getXDim() || j < 0 || j >= input.getYDim())
						continue;

					boolean ok = (Arrays.binarySearch(centers, new Point4D(k, j, 0, 0)) >= 0);
					/*
					 * boolean ok = true; for (int b = 0; b < input.getBDim(); b++) if
					 * (input.getPixelXYBInt(k, j, 0) != NULL) ok = false;
					 */
					if (!(k == p.x && j == p.y) && ok
						&& output.getPixelXYBInt(k, j, 2) == NULL) {
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

	public static void main(String args[]) {
		// Load image and markers
		String path = "./samples/";
		String file = "spots";
		file = path + file;
		if (args.length > 0)
			file = args[0];
		Image source = (Image) new ImageLoader().process(file + ".png");
		Image samples = (Image) new SamplesLoader().process(file);

		if (source.getXDim() != samples.getXDim()
			|| source.getYDim() != samples.getYDim()
			|| source.getZDim() != samples.getZDim()
			|| source.getTDim() != samples.getTDim()) {
			System.out.println("Attention, taille des images incompatibles");
			return;
		}

		/*
		 * FlatStructuringElement se =
		 * FlatStructuringElement.createSquareFlatStructuringElement(3); source =
		 * GrayGradient.process(source, se);
		 */
		// source = AverageChannels.process(source);
		source = (Image) new AdditionConstantChecked().process(source, 1.0 / 255);

		// Merge image and markers
		for (int t = 0; t < source.getTDim(); t++)
			for (int z = 0; z < source.getZDim(); z++)
				for (int x = 0; x < source.getXDim(); x++)
					for (int y = 0; y < source.getYDim(); y++)
						for (int bb = 0; bb < samples.getBDim(); bb++)
							if (samples.getPixelByte(x, y, z, t, bb) != 0)
								for (int b = 0; b < source.getBDim(); b++)
									source.setPixelByte(x, y, z, t, b, 0);

		new Viewer2D().process(source, "Updated source");

		/*
		 * Image im1=DrawFrontiersOnImage.process(source,
		 * FrontiersFromSegmentation.process(MarkerBasedWatershed.process(source)));
		 * Viewer2D.exec(im1, "classical marker-based watershed");
		 */

		// Image
		// im=((IntegerImage)GeodesicDistanceBasedWatershed.process(source)).scaleToVisibleRange();
		Image geod = (Image) new GeodesicDistanceBasedWatershed().process(source);
		Image frontiers = (Image) new FrontiersFromSegmentation().process(geod
			.getImage4D(0, Image.B));
		new Viewer2D().process(frontiers, "geodesic distance-based watershed");
		new Viewer2D().process(new DrawFrontiersOnImage()
			.process(source, frontiers), "input and segmentation result");
		new Viewer2D().process(((IntegerImage) geod.getImage4D(1, Image.B))
			.scaleToVisibleRange(), "geodesic distance transform");

	}

	/**
	 * See header.
	 */
	public static Image exec(Image input, Point4D[] centers) {
		return (Image) new GeodesicDistanceBasedWatershed().process(input, centers);
	}

	public static Image exec(Image input, Point4D[] centers, boolean trueDistance) {
		return (Image) new GeodesicDistanceBasedWatershed().process(input, centers,
			trueDistance);
	}

	public static Image exec(Image input, Point4D[] centers,
		boolean trueDistance, boolean hue) {
		return (Image) new GeodesicDistanceBasedWatershed().process(input, centers,
			trueDistance, hue);
	}

	public static Image exec(Image input, Point4D[] centers,
		boolean trueDistance, boolean hue, HierarchicalQueue queue) {
		return (Image) new GeodesicDistanceBasedWatershed().process(input, centers,
			trueDistance, hue, queue);
	}

}
