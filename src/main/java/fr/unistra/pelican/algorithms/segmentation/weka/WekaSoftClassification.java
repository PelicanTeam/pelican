package fr.unistra.pelican.algorithms.segmentation.weka;

import weka.classifiers.Classifier;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.instance.Resample;
import weka.filters.unsupervised.instance.Randomize;
import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.InvalidNumberOfParametersException;
import fr.unistra.pelican.InvalidTypeOfParameterException;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.io.SamplesLoader;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

/**
 * Perform a classification using a Weka algorithm. Each band from input
 * represents a attribute. Each band from samples represent a class exemples.
 * 
 * @author Derivaux, Lefevre
 */
public class WekaSoftClassification extends Algorithm {

	/**
	 * The input image
	 */
	public Image inputImage;

	/**
	 * The classifier in use
	 */
	public Classifier classifier;

	/**
	 * The sample image
	 */
	public Image samples;

	/**
	 * The output image
	 */
	public Image outputImage;

	/**
	 * Constructor
	 */
	public WekaSoftClassification() {
		super.inputs = "inputImage,classifier,samples";
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
		int zDim = inputImage.getZDim();
		int tDim = inputImage.getTDim();
		int bDim = inputImage.getBDim();
		outputImage = new IntegerImage(xDim, yDim, zDim, tDim, samples.getBDim());
		// Creation of the datas for Weka.
		// Create attributes.
		FastVector attributes = new FastVector(bDim);
		for (int i = 0; i < bDim; i++)
			attributes.addElement(new weka.core.Attribute("bande" + i));
		// Add class attribute.
		FastVector classValues = new FastVector(10);
		for (int b = 0; b < samples.getBDim(); b++)
			classValues.addElement("class" + b);
		attributes.addElement(new weka.core.Attribute("label", classValues));
		Instances dataset = new Instances("dataset", attributes, 0);
		dataset.setClassIndex(attributes.size() - 1);

		// Put learning samples in the dataset
		for (int t = 0; t < tDim; t++)
			for (int z = 0; z < zDim; z++)
				for (int y = 0; y < yDim; y++)
					for (int x = 0; x < xDim; x++)
						for (int c = 0; c < samples.getBDim(); c++)
							if (samples.getPixelBoolean(x, y, z, t, c) == true) {
								Instance instance = new Instance(dataset.numAttributes());
								for (int b = 0; b < bDim; b++)
									instance
										.setValue(b, inputImage.getPixelDouble(x, y, z, t, b));
								instance.setDataset(dataset);
								instance.setClassValue((double) c);
								dataset.add(instance);
							}

		// Filter a little the dataset
		try {
			// Radomise presentation
			Filter filter = new Randomize();
			filter.setInputFormat(dataset);
			dataset = Filter.useFilter(dataset, filter);

			// Resample or not resample to uniformise class distribution??
			Resample resample = new Resample();
			resample.setBiasToUniformClass(1.0);
			resample.setSampleSizePercent(100.0);
			resample.setInputFormat(dataset);
			resample.setRandomSeed((int) (Integer.MAX_VALUE * Math.random()));
			dataset = Filter.useFilter(dataset, resample);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Learn the classification
		try {
			classifier.buildClassifier(dataset);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (int t = 0; t < tDim; t++)
			for (int z = 0; z < zDim; z++)
				for (int y = 0; y < yDim; y++)
					for (int x = 0; x < xDim; x++) {
						Instance instance = new Instance(dataset.numAttributes());
						for (int b = 0; b < bDim; b++)
							instance.setValue(b, inputImage.getPixelDouble(x, y, z, t, b));
						instance.setDataset(dataset);
						double[] distrib = null;
						try {
							distrib = classifier.distributionForInstance(instance);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						for (int c = 0; c < distrib.length; c++)
							outputImage.setPixelDouble(x, y, z, t, c, distrib[c]);
					}
	}

	public static void main(String[] args) {
		String file = "samples/remotesensing1.png";
		String samplesPath = "samples/remotesensing1";
		if (args.length > 0)
			file = args[0];

		try {
			// Load the image
			Image source = (Image) new ImageLoader().process(file);
			new Viewer2D().process(source, "Image " + file);

			Image samples = (Image) new SamplesLoader().process(samplesPath);
			new Viewer2D().process(samples, "Samples of" + file);

			MultilayerPerceptron classifier = new MultilayerPerceptron();

			Image work = (Image) new WekaSoftClassification().process(source,
				classifier, samples);

			// View It!
			new Viewer2D().process(work, "Classification for " + file);

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
