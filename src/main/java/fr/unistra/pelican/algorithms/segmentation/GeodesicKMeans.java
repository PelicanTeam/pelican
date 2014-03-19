package fr.unistra.pelican.algorithms.segmentation;

import java.awt.Point;
import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.arithmetic.Inversion;
import fr.unistra.pelican.algorithms.conversion.GrayToPseudoColors;
import fr.unistra.pelican.algorithms.segmentation.labels.DrawFrontiersOnImage;
import fr.unistra.pelican.algorithms.segmentation.labels.FrontiersFromSegmentation;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToBinaryMasks;
import fr.unistra.pelican.algorithms.spatial.TopographicTransform;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.HierarchicalQueue;
import fr.unistra.pelican.util.Memory;
import fr.unistra.pelican.util.Point4D;

/**
 * This class is a geodesic adaptation of the K-Means algorithm for iterative
 * image segmentation using distance transforms.
 * 
 * @author Lefevre
 */
public class GeodesicKMeans extends Algorithm {

	/**
	 * Input Image
	 */
	public Image inputImage;

	/**
	 * Number of seeked clusters.
	 */
	public int clusters;

	/**
	 * Output Image
	 */
	public Image outputImage;

	/**
	 * Number of iterations, default 15.
	 */
	public int maxIterations = 15;

	/**
	 * Convergence criterion (distance threshold), default 2
	 */
	public int minDist = 2;

	/**
	 * Flag to compute hue-based distance
	 */
	public boolean hue = false;

	private boolean DEBUG = true;

	/**
	 * Constructor
	 */
	public GeodesicKMeans() {
		super.inputs = "inputImage,clusters";
		super.options = "maxIterations,minDist,hue";
		super.outputs = "outputImage";
	}

	/**
	 * A geodesic adaptation of the K-Means algorithm for iterative image
	 * segmentation using distance transforms.
	 * 
	 * @param inputImage
	 *          The input image
	 * @param clusters
	 *          The number of seeked clusters
	 * @return The output image
	 */
	public static Image exec(Image inputImage, int clusters) {
		return (Image) new GeodesicKMeans().process(inputImage, clusters);
	}

	public static Image exec(Image inputImage, int clusters, int iterations) {
		return (Image) new GeodesicKMeans().process(inputImage, clusters,
			iterations);
	}

	public static Image exec(Image inputImage, int clusters, int iterations,
		int minDist) {
		return (Image) new GeodesicKMeans().process(inputImage, clusters,
			iterations, minDist);
	}

	public static Image exec(Image inputImage, int clusters, int iterations,
		int minDist, boolean hue) {
		return (Image) new GeodesicKMeans().process(inputImage, clusters,
			iterations, minDist, hue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = new IntegerImage(inputImage.getXDim(), inputImage.getYDim(),
			inputImage.getZDim(), inputImage.getTDim(), 1);
//		inputImage = (Image) new AdditionConstantChecked().process(inputImage,
//			1.0 / 255);

		Image labels = null, frontiers = null, distances = null, distances2 = null, globaldistances2 = null, draw = null;
		if (DEBUG) {
			labels = new IntegerImage(inputImage.getXDim(), inputImage.getYDim(),
				inputImage.getZDim(), maxIterations, 1);
			frontiers = new BooleanImage(inputImage.getXDim(), inputImage.getYDim(),
				inputImage.getZDim(), maxIterations, 1);
			distances = new ByteImage(inputImage.getXDim(), inputImage.getYDim(),
				inputImage.getZDim(), maxIterations, 3);
			distances.setColor(true);
			// FIXME : works only for 2D images !
			distances2 = new ByteImage(inputImage.getXDim(), inputImage.getYDim(),
				clusters, 1, 3);
			distances2.setColor(true);
			globaldistances2 = new ByteImage(inputImage.getXDim(), inputImage
				.getYDim(), clusters, maxIterations, 3);
			draw = new ByteImage(inputImage.getXDim(), inputImage.getYDim(),
				inputImage.getZDim(), maxIterations, inputImage.getBDim());
			draw.setColor(true);
		}

		int scale = Math.max(inputImage.getXDim(), inputImage.getYDim());// 500;
		double mem1 = Memory.totalUsedMemoryMB();
		HierarchicalQueue queue = new HierarchicalQueue(scale * scale * 255);
		double mem2 = Memory.totalUsedMemoryMB();
		System.out.println("Allocated memory for queue:" + (int) (mem2 - mem1)
			+ " MB");

		// Initialise cluster centers
		Point4D[] centers = new Point4D[clusters];
		Point4D[] oldCenters = null, oldCenters2 = null;
		for (int c = 0; c < clusters; c++)
			centers[c] = new Point4D((int) (Math.random() * inputImage.getXDim()),
				(int) (Math.random() * inputImage.getYDim()), (int) Math.random()
					* inputImage.getZDim(), (int) (Math.random() * inputImage.getTDim()));

//		 centers[0] = new Point4D(229,95, 0, 0);
//		 centers[1] = new Point4D(341,135, 0, 0);
//		 centers[2] = new Point4D(160,301, 0, 0);
//		 centers[3] = new Point4D(121,269, 0, 0);
//		 centers[4] = new Point4D(87,203, 0, 0);
//		 centers[5] = new Point4D(101,65, 0, 0);

		boolean trueDistance = true;
		boolean borders = false;

		// Repeat the process until convergence
		boolean convergence = false;
		int i = 0;
		long t1 = System.currentTimeMillis();
		for (i = 0; i < maxIterations && !convergence; i++) {

			// Set the centers on the input image
			Image work = inputImage.copyImage(true);
			/*
			 * for (int c = 0; c < clusters; c++) for (int b = 0; b <
			 * inputImage.getBDim(); b++) work.setPixelXYBByte(centers[c].x,
			 * centers[c].y, b, 0);
			 */

			// Perform geodesic computation
			Image im = GeodesicDistanceBasedWatershed.exec(work, centers,
				trueDistance, hue, queue);
			outputImage = im.getImage4D(0, Image.B);

			if (DEBUG) {
				distances.setImage4D(DrawFrontiersOnImage.exec(GrayToPseudoColors
					.exec(im.getImage4D(1, Image.B)), FrontiersFromSegmentation.exec(im
					.getImage4D(0, Image.B))), i, Image.T);
				labels.setImage4D(im.getImage4D(0, Image.B), i, Image.T);
				frontiers.setImage4D((Image) new FrontiersFromSegmentation().process(im
					.getImage4D(0, Image.B)), i, Image.T);
				draw.setImage4D(DrawFrontiersOnImage.exec(inputImage,
					(BooleanImage) frontiers.getImage4D(i, Image.T)), i, Image.T);
			}

			// Compute the center of each region
			// oldCenters = centers.clone();
			if (oldCenters != null)
				oldCenters2 = copy(oldCenters);
			oldCenters = copy(centers);
			// for (int cpt=0;cpt<oldCenters.length;cpt++) {
			// System.err.println(oldCenters[cpt]+" "+centers[cpt]);
			// }

			// centre de gravitÃ© standard (k-means)
			// centers = trim((Point[]) new RegionCenter().process(labels));

			// recherche du central comme le max de la TD au fond
			// Image binary=LabelsToBinaryMasks.exec(outputImage);
			// for (int c=0;c<clusters;c++) {
			// Image cluster=Inversion.exec(binary.getImage4D(c,Image.B));
			// cluster=DistanceTransform.exec(cluster,true);
			// //Viewer2D.exec(GrayToPseudoColors.exec(cluster));
			// int max=0;
			// for(int x=0;x<cluster.getXDim();x++)
			// for(int y=0;y<cluster.getYDim();y++)
			// if (cluster.getPixelXYInt(x, y)>max) {
			// centers[c].x=x;
			// centers[c].y=y;
			// max=cluster.getPixelXYInt(x, y);
			// }
			// }

			Image binary = LabelsToBinaryMasks.exec(outputImage);
			if (binary.getBDim() < clusters) {
				clusters = binary.getBDim();
				System.err.println("Less clusters : " + clusters);
			}
			if (DEBUG)
				distances2.fill(0);
			// Viewer2D.exec(binary,"clusters, # "+i);
			for (int c = 0; c < clusters; c++) {
				Image cluster = Inversion.exec(binary.getImage4D(c, Image.B));
				cluster = TopographicTransform.exec(inputImage, (BooleanImage) cluster,
					trueDistance, borders, hue, queue);
				if (DEBUG)
					distances2.setImage4D(GrayToPseudoColors.exec(cluster), c, Image.Z);
				// Viewer2D.exec(GrayToPseudoColors.exec(cluster));
				int max = 0;
				for (int x = 0; x < cluster.getXDim(); x++)
					for (int y = 0; y < cluster.getYDim(); y++)
						if (cluster.getPixelXYInt(x, y) > max) {
							centers[c].x = x;
							centers[c].y = y;
							max = cluster.getPixelXYInt(x, y);
						}
			}
			// System.err.println("===");
			// for (int cpt=0;cpt<oldCenters.length;cpt++)
			// System.err.println(oldCenters[cpt]+" "+centers[cpt]);

			if (DEBUG)
				globaldistances2.setImage4D(distances2, i, Image.T);
			// Viewer2D.exec(distances2,"distance aux centres, # "+i);

			if (centers.length != clusters)
				System.err.println("Probleme :" + centers.length + " != " + clusters);

			// Evaluate modifications
			System.out.println();
			for (int c = 0; c < clusters; c++)
				System.out.println(i + "/" + c + ":" + oldCenters[c].x + ","
					+ oldCenters[c].y + " =>" + centers[c].x + "," + centers[c].y
					+ " ==>" + centers[c].distance(oldCenters[c]));
			int dist = 0;
			for (int c = 0; c < clusters; c++)
				dist += centers[c].distance(oldCenters[c]);
			System.out.println(i + ": convergence ? " + dist + " <= "
				+ (minDist * clusters));
			if (dist <= minDist * clusters)
				convergence = true;

			// Additional check for oscillation
			if (oldCenters2 != null) {
				dist = 0;
				for (int c = 0; c < clusters; c++)
					dist += centers[c].distance(oldCenters2[c]);
				if (dist <= minDist * clusters)
					convergence = true;
			}
		}

		long t2 = System.currentTimeMillis();
		System.out.println("GeodesicKMeans performed in " + (t2 - t1) + " ms ("
			+ (t2 - t1) / (i) + " ms / iteration)");

		// queue.clear();
		// queue = null;
		// System.gc();

		if (DEBUG) {
			Viewer2D.exec(distances, "distances");
			globaldistances2.setColor(true);
			Viewer2D.exec(globaldistances2, "distances2");
			// Viewer2D.exec(LabelsToRandomColors.exec(labels), "labels");
			// Viewer2D.exec(frontiers, "frontiers");
			// Viewer2D.exec(draw, "draw");
		}

	}

	private Point4D[] copy(Point4D[] tab) {
		Point4D[] res = new Point4D[tab.length];
		for (int t = 0; t < tab.length; t++)
			res[t] = new Point4D(tab[t]);
		return res;
	}

	private Point[] trim(Point[] tab) {
		int nb = 0;
		for (int t = 0; t < tab.length; t++)
			if (tab[t] != null)
				nb++;
		Point[] res = new Point[nb];
		nb = 0;
		for (int t = 0; t < tab.length; t++)
			if (tab[t] != null)
				res[nb++] = tab[t];
		return res;
	}

}
