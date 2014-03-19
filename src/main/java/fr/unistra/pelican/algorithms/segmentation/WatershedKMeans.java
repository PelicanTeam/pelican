package fr.unistra.pelican.algorithms.segmentation;

import java.awt.Point;
import java.util.Arrays;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.arithmetic.AdditionConstantChecked;
import fr.unistra.pelican.algorithms.arithmetic.Inversion;
import fr.unistra.pelican.algorithms.conversion.GrayToPseudoColors;
import fr.unistra.pelican.algorithms.morphology.vectorial.gradient.MultispectralEuclideanGradient;
import fr.unistra.pelican.algorithms.segmentation.labels.DrawFrontiersOnImage;
import fr.unistra.pelican.algorithms.segmentation.labels.FrontiersFromSegmentation;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToBinaryMasks;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToRandomColors;
import fr.unistra.pelican.algorithms.segmentation.labels.RegionCenter;
import fr.unistra.pelican.algorithms.spatial.DistanceTransform;
import fr.unistra.pelican.algorithms.spatial.TopographicTransform;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 * This class is a geodesic adaptation of the K-Means algorithm for iterative
 * image segmentation using distance transforms.
 * 
 * @author Lefevre
 */
public class WatershedKMeans extends Algorithm {

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
	 * Constructor
	 */
	public WatershedKMeans() {
		super.inputs = "inputImage,clusters";
		super.options = "maxIterations,minDist";
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
		return (Image) new WatershedKMeans().process(inputImage, clusters);
	}

	public static Image exec(Image inputImage, int clusters,int iterations) {
		return (Image) new WatershedKMeans().process(inputImage, clusters,iterations);
	}

	public static Image exec(Image inputImage, int clusters,int iterations,int minDist) {
		return (Image) new WatershedKMeans().process(inputImage, clusters,iterations,minDist);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = new IntegerImage(inputImage.getXDim(), inputImage
				.getYDim(), inputImage.getZDim(), inputImage.getTDim(), 1);
		inputImage = (Image) new AdditionConstantChecked().process(inputImage,
				1.0 / 255);

		Image labels= new IntegerImage(inputImage.getXDim(), inputImage
			.getYDim(), inputImage.getZDim(), maxIterations, 1);
		Image frontiers= new BooleanImage(inputImage.getXDim(), inputImage
				.getYDim(), inputImage.getZDim(), maxIterations, 1);
		Image draw= new ByteImage(inputImage.getXDim(), inputImage
			.getYDim(), inputImage.getZDim(), maxIterations, inputImage.getBDim());
		draw.setColor(true);
		
		// Initialise cluster centers
		Point[] centers = new Point[clusters];
		Point[] oldCenters;
		for (int c = 0; c < clusters; c++)
			centers[c] = new Point(
					(int) (Math.random() * inputImage.getXDim()), (int) (Math
							.random() * inputImage.getYDim()));

		boolean trueDistance=true;
//		Image grad=MultispectralEuclideanGradient.exec(inputImage,FlatStructuringElement2D.createSquareFlatStructuringElement(3));
		Image grad=MultispectralEuclideanGradient.exec(inputImage,FlatStructuringElement2D.createCrossFlatStructuringElement(1));
		grad=AdditionConstantChecked.exec(grad,1./255);
//		Viewer2D.exec(grad);
		
		// Repeat the process until convergence
		boolean convergence = false;
		for (int i = 0; i < maxIterations && !convergence; i++) {

			// Set the centers on the input image
			Image work = grad.copyImage(true);
			for (int c = 0; c < clusters; c++)
				for (int b = 0; b < inputImage.getBDim(); b++)
					work.setPixelXYBByte(centers[c].x, centers[c].y, b, 0);

			// Perform watershed computation
			Image im = MarkerBasedWatershed.exec(work);

			outputImage=im.getImage4D(0, Image.B);
			
			labels.setImage4D(outputImage, i, Image.T);
			//Viewer2D.exec(LabelsToRandomColors.exec(labels));
			
			frontiers.setImage4D((Image) new FrontiersFromSegmentation()
			.process(outputImage), i, Image.T);
			draw.setImage4D(DrawFrontiersOnImage.exec(inputImage,(BooleanImage) frontiers.getImage4D(i,Image.T)),i,Image.T);

			// Compute the center of each region
			oldCenters = copy(centers);
			
			// centre de gravitÃ© standard (k-means)
			//centers = trim((Point[]) new RegionCenter().process(labels));

			// recherche du central comme le max de la TD au fond
//			Image binary=LabelsToBinaryMasks.exec(outputImage);
//			for (int c=0;c<clusters;c++) {
//				Image cluster=Inversion.exec(binary.getImage4D(c,Image.B));
//				cluster=DistanceTransform.exec(cluster,true);
//				//Viewer2D.exec(GrayToPseudoColors.exec(cluster));
//				int max=0;
//				for(int x=0;x<cluster.getXDim();x++)
//					for(int y=0;y<cluster.getYDim();y++)
//						if (cluster.getPixelXYInt(x, y)>max) {
//							centers[c].x=x;
//							centers[c].y=y;
//							max=cluster.getPixelXYInt(x, y);
//						}
//			}
			
			Image binary=LabelsToBinaryMasks.exec(outputImage);
			if(binary.getBDim()<clusters) {
				clusters=binary.getBDim();
				System.err.println("Less clusters : "+clusters);
			}
			for (int c=0;c<clusters;c++) {
				Image cluster=Inversion.exec(binary.getImage4D(c,Image.B));
				cluster=TopographicTransform.exec(inputImage,(BooleanImage)cluster,trueDistance,true);
				//Viewer2D.exec(GrayToPseudoColors.exec(cluster));
				int max=0;
				for(int x=0;x<cluster.getXDim();x++)
					for(int y=0;y<cluster.getYDim();y++)
						if (cluster.getPixelXYInt(x, y)>max) {
							centers[c].x=x;
							centers[c].y=y;
							max=cluster.getPixelXYInt(x, y);
						}
			}

			/*// recherche du maximum local (simulation d'un watershed) => fonctionne pas !
			Image binary=LabelsToBinaryMasks.exec(outputImage);
			clusters=binary.getBDim();
			for (int c=0;c<clusters;c++) {
				System.out.println(c+"/"+clusters);
				Image cluster=binary.getImage4D(c,Image.B);
				//Viewer2D.exec(cluster);
				int max=0;
				for(int x=0;x<cluster.getXDim();x++)
					for(int y=0;y<cluster.getYDim();y++)
						if(cluster.getPixelXYBoolean(x,y))
						if (grad.getPixelXYByte(x, y)>max) {
							centers[c].x=x;
							centers[c].y=y;
							max=grad.getPixelXYByte(x, y);
						}
			}
			*/
			
			if (centers.length != clusters)
				System.err.println("Problï¿œme :" + centers.length + " != "
						+ clusters);

			// Evaluate modifications
			for (int c = 0; c < clusters; c++)
				System.out.println(i + "/" + c + ":" + oldCenters[c].x + ","
						+ oldCenters[c].y + " =>" + centers[c].x + ","
						+ centers[c].y + " ==>"
						+ centers[c].distance(oldCenters[c]));
			int dist = 0;
			for (int c = 0; c < clusters; c++)
				dist += centers[c].distance(oldCenters[c]);
			System.out.println(i + ": convergence ? "+dist+" <= "+(minDist*clusters));
			if (dist <= minDist * clusters)
				convergence = true;
		}

		new Viewer2D().process(LabelsToRandomColors.exec(labels), "labels");
		new Viewer2D().process(frontiers,"frontiers");
		new Viewer2D().process(draw,"draw");

	}

	private Point[] copy(Point[] tab) {
		Point[] res = new Point[tab.length];
		for (int t = 0; t < tab.length; t++)
			res[t]=new Point(tab[t]);
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
