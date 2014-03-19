package fr.unistra.pelican.algorithms.segmentation.weka;

import weka.clusterers.EM;
import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.InvalidNumberOfParametersException;
import fr.unistra.pelican.InvalidTypeOfParameterException;


/**
 * Perform a segmentation using a Weka algorithm. Each band represents a
 * attribute.
 * @author Sebastien Derivaux
 */
public class WekaSegmentationEM extends Algorithm {

	// Inputs parameters
	public Image inputImage;

	public int nbClusters;

	// Outputs parameters
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public WekaSegmentationEM() {
		super.inputs = "inputImage,nbClusters";
		super.outputs = "outputImage";
	}

	public static Image exec(Image inputImage, int nbClusters) {
		return (Image) new WekaSegmentationEM().process(inputImage,nbClusters);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		EM clusterer = new EM();
		try {
			clusterer.setNumClusters(nbClusters);
			clusterer.setSeed((int) System.currentTimeMillis());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			outputImage = (Image) new WekaSegmentation().process(inputImage, clusterer);
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
/*
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

			Image work =(Image) new  WekaSegmentationEM().process(source, 3);

			Image frontiers =(Image) new  FrontiersFromSegmentation().process(work);

			// View it
			new Viewer2D().process(new ContrastStretch().process(work), "Clusters from "
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
	}*/
}
