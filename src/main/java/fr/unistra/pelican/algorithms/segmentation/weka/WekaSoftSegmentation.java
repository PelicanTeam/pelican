package fr.unistra.pelican.algorithms.segmentation.weka;

import weka.clusterers.Clusterer;
import weka.clusterers.EM;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.Resample;
import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.InvalidNumberOfParametersException;
import fr.unistra.pelican.InvalidTypeOfParameterException;
import fr.unistra.pelican.algorithms.histogram.ContrastStretch;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

/**
 * Perform a soft segmentation using a Weka algorithm. Each band represents a
 * attribute.
 * @author Sébastien Derivaux
 */
public class WekaSoftSegmentation extends Algorithm {

	// Inputs parameters
	public Image inputImage;

	public Clusterer clusterer;

	// Outputs parameters
	public Image outputImage;

	public final int MAX_LEARNING = 5000;

	/**
	 * Constructor
	 * 
	 */
	public WekaSoftSegmentation() {

		super();
		super.inputs = "inputImage,clusterer";
		super.outputs = "outputImage";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		int xDim = inputImage.getXDim();
		int yDim = inputImage.getYDim();
		int bDim = inputImage.getBDim();

		// Creation of the datas for Wek.
		// Create attributes.
		FastVector attributes = new FastVector(bDim);
		for (int i = 0; i < bDim; i++)
			attributes.addElement(new weka.core.Attribute("bande" + i));

		Instances dataset = new Instances("dataset", attributes, 0);

		for (int x = 0; x < xDim; x++)
			for (int y = 0; y < yDim; y++) {
				Instance instance = new Instance(dataset.numAttributes());
				for (int b = 0; b < bDim; b++)
					instance.setValue(b, inputImage.getPixelXYBDouble(x, y, b));
				instance.setDataset(dataset);
				dataset.add(instance);
			}

		// Learn the classification
		try {
			Instances learningSet = dataset;
			if (learningSet.numInstances() > MAX_LEARNING) {
				Resample filter = new Resample();
				filter.setRandomSeed((int) System.currentTimeMillis());
				filter.setSampleSizePercent((double) MAX_LEARNING * 100.0
						/ (double) learningSet.numInstances());
				filter.setInputFormat(learningSet);
				learningSet = Filter.useFilter(learningSet, filter);
				System.out
						.println("INFO : WekaSoftSegmentation : numInstances = "
								+ learningSet.numInstances());
			}
			clusterer.buildClusterer(learningSet);

			outputImage = new DoubleImage(inputImage.getXDim(), inputImage
					.getYDim(), 1, 1, clusterer.numberOfClusters());

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (int x = 0; x < xDim; x++)
			for (int y = 0; y < yDim; y++) {
				Instance instance = new Instance(dataset.numAttributes());
				for (int b = 0; b < bDim; b++)
					instance.setValue(b, inputImage.getPixelXYBDouble(x, y, b));
				instance.setDataset(dataset);
				double distrib[] = null;
				try {
					distrib = clusterer.distributionForInstance(instance);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				for (int b = 0; b < distrib.length; b++)
					outputImage.setPixelXYBDouble(x, y, b, distrib[b]);
			}
	}

	public static void main(String[] args) {
		String file = "samples/remotesensing1.png";
		if (args.length > 0)
			file = args[0];

		try {
			// Load the image
			Image source = (Image) new ImageLoader().process(file);
			new Viewer2D().process(source, "Image " + file);

			EM clusterer = new EM();
			try {
				clusterer.setNumClusters(3);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			clusterer.setSeed((int) System.currentTimeMillis());
			Image work = (Image) new WekaSoftSegmentation().process(source, clusterer);

			// View it
			new Viewer2D().process(new ContrastStretch().process(work),
					"Soft clusters from " + file);

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
