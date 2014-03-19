package fr.unistra.pelican.algorithms.segmentation.weka;

import weka.clusterers.SimpleKMeans;
import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.InvalidNumberOfParametersException;
import fr.unistra.pelican.InvalidTypeOfParameterException;
import fr.unistra.pelican.algorithms.histogram.ContrastStretch;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.segmentation.labels.DrawFrontiersOnImage;
import fr.unistra.pelican.algorithms.segmentation.labels.FrontiersFromSegmentation;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToColorByMeanValue;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

/**
 * Perform a segmentation using a Weka algorithm. Each band represents a
 * attribute.
 * 
 * @author Sï¿œbastien Derivaux
 */
public class WekaSegmentationKmeans extends Algorithm {

	// Inputs parameters
	public Image inputImage;

	public int nbClusters;

	// Outputs parameters
	public Image outputImage;

	/**
	 * an optional subsampling percentage
	 */
	public double subsampling = 100.0;

	/**
	 * Constructor
	 * 
	 */
	public WekaSegmentationKmeans() {

		super.options = "subsampling";
		super.inputs = "inputImage,nbClusters";
		super.outputs = "outputImage";

	}

	public static Image exec(Image inputImage, int nbClusters) {
		return (Image) new WekaSegmentationKmeans().process(inputImage, nbClusters);
	}

	public static Image exec(Image inputImage, int nbClusters, double subsampling) {
		return (Image) new WekaSegmentationKmeans().process(inputImage, nbClusters,
			subsampling);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		SimpleKMeans clusterer = new SimpleKMeans();
		try {
			clusterer.setNumClusters(nbClusters);
			clusterer.setSeed((int) System.currentTimeMillis());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			outputImage = (Image) new WekaSegmentation().process(inputImage,
				clusterer, subsampling);
		} catch (InvalidTypeOfParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidNumberOfParametersException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		String file = "samples/lenna.png";
		if (args.length > 0)
			file = args[0];

		try {
			// Load the image
			Image source = (Image) new ImageLoader().process(file);
			new Viewer2D().process(source, "Image " + file);

			Image work = (Image) new WekaSegmentationKmeans().process(source, 3);

			Image frontiers = (Image) new FrontiersFromSegmentation().process(work);

			// View it
			new Viewer2D().process(new ContrastStretch().process(work),
				"Clusters from " + file);
			new Viewer2D().process(new LabelsToColorByMeanValue().process(work,
				source), "Mean of clusters from " + file);
			new Viewer2D().process(new DrawFrontiersOnImage().process(source,
				frontiers), "Frontiers of " + file);

		} catch (InvalidTypeOfParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidNumberOfParametersException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
