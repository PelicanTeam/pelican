package fr.unistra.pelican.algorithms.segmentation;

import weka.clusterers.SimpleKMeans;
import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.InvalidNumberOfParametersException;
import fr.unistra.pelican.InvalidTypeOfParameterException;
import fr.unistra.pelican.algorithms.histogram.ContrastStretch;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.segmentation.labels.DrawFrontiersOnImage;
import fr.unistra.pelican.algorithms.segmentation.labels.FrontiersFromSegmentation;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToColorByMeanValue;
import fr.unistra.pelican.algorithms.segmentation.weka.WekaSegmentation;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 * Perform a segmentation using a Weka algorithm. Each band represents a
 * attribute. Two additional bands are created to store X and Y spatial
 * positions
 */
public class SpatialColorKMeans extends Algorithm {

	// Inputs parameters
	public Image inputImage;

	public int nbClusters;

	// Outputs parameters
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public SpatialColorKMeans() {
		super.inputs = "inputImage,nbClusters";
		super.outputs = "outputImage";
	}

	public static Image exec(Image inputImage, int nbClusters) {
		return (Image) new SpatialColorKMeans().process(inputImage,nbClusters);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		int dims = 0;
		if (inputImage.getXDim() > 1)
			dims++;
		if (inputImage.getZDim() > 1)
			dims++;
		if (inputImage.getTDim() > 1)
			dims++;
		if (inputImage.getBDim() > 1)
			dims++;
		Image work = inputImage.newInstance(inputImage.getXDim(), inputImage
				.getYDim(), inputImage.getZDim(), inputImage.getTDim(),
				inputImage.getBDim() + dims);
		for (int x = 0; x < work.getXDim(); x++)
			for (int y = 0; y < work.getYDim(); y++)
				for (int z = 0; z < work.getZDim(); z++)
					for (int t = 0; t < work.getTDim(); t++) {
						// Copie initial values
						int b = 0;
						for (b = 0; b < inputImage.getBDim(); b++)
							work.setPixelDouble(x, y, z, t, b, inputImage
									.getPixelDouble(x, y, z, t, b));
						// Add x value
						if (work.getXDim() > 1) {
							work.setPixelDouble(x, y, z, t, b, ((double) x)
									/ work.getXDim());
							b++;
						}
						// Add y value
						if (work.getYDim() > 1) {
							work.setPixelDouble(x, y, z, t, b, ((double) y)
									/ work.getYDim());
							b++;
						}
						// Add z value
						if (work.getZDim() > 1) {
							work.setPixelDouble(x, y, z, t, b, ((double) z)
									/ work.getZDim());
							b++;
						}
						// Add t value
						if (work.getTDim() > 1) {
							work.setPixelDouble(x, y, z, t, b, ((double) t)
									/ work.getTDim());
							b++;
						}
					}
		SimpleKMeans clusterer = new SimpleKMeans();
		try {
			clusterer.setNumClusters(nbClusters);
			clusterer.setSeed((int) System.currentTimeMillis());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			outputImage = (Image) new WekaSegmentation().process(work, clusterer);
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

		BooleanImage se3 = FlatStructuringElement2D
				.createSquareFlatStructuringElement(3);

		try {
			// Load the image
			Image source = (Image) new ImageLoader().process(file);
			new Viewer2D().process(source, "Image " + file);

			Image work = (Image) new SpatialColorKMeans().process(source, 3);

			Image frontiers = (Image) new FrontiersFromSegmentation().process(work);

			// View it
			new Viewer2D().process (new ContrastStretch().process(work), "Clusters from "
					+ file);
			new Viewer2D().process(new LabelsToColorByMeanValue().process(work, source),
					"Mean of clusters from " + file);
			new Viewer2D().process(new DrawFrontiersOnImage().process(source, frontiers),
					"Frontiers of " + file);

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
