package fr.unistra.pelican.algorithms.segmentation.weka;

import weka.classifiers.lazy.IBk;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.filters.Filter;
import weka.filters.supervised.instance.Resample;
import weka.filters.unsupervised.instance.Randomize;
import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.InvalidNumberOfParametersException;
import fr.unistra.pelican.InvalidTypeOfParameterException;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.io.SamplesLoader;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

/**
 * Perform a soft classification using the 5-Nearest Neighbour Weka algorithm.
 * Each band from input represents a attribute. Each band from samples represent
 * a class exemples.
 * @author Sébastien Derivaux
 */
public class WekaSoftClassificationMulti5NN extends Algorithm {

	// Inputs parameters
	public Image inputImage;

	public Image samples;

	// Outputs parameters
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public WekaSoftClassificationMulti5NN() {

		super();
		super.inputs = "inputImage,samples";
		super.outputs = "outputImage";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		int nbClassifiers = 50;

		IBk[] classifier = new IBk[nbClassifiers];

		for (int i = 0; i < classifier.length; i++) {
			classifier[i] = new IBk();
			classifier[i].setKNN(5);
			classifier[i].setWindowSize(500);
			classifier[i]
					.setNearestNeighbourSearchAlgorithm(new weka.core.neighboursearch.CoverTree());

			// classifier.setCrossValidate(true);
			// classifier.setNoNormalization(true);
			classifier[i].setDistanceWeighting(new SelectedTag(
					IBk.WEIGHT_INVERSE, IBk.TAGS_WEIGHTING));
		}
		outputImage = new DoubleImage(inputImage.getXDim(), inputImage
				.getYDim(), 1, 1, samples.getBDim());

		int xDim = inputImage.getXDim();
		int yDim = inputImage.getYDim();
		int bDim = inputImage.getBDim();

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
		for (int x = 0; x < xDim; x++)
			for (int y = 0; y < yDim; y++)
				for (int c = 0; c < samples.getBDim(); c++)
					if (samples.getPixelXYBBoolean(x, y, c) == true) {
						Instance instance = new Instance(dataset
								.numAttributes());
						for (int b = 0; b < bDim; b++)
							instance.setValue(b, inputImage.getPixelXYBDouble(
									x, y, b));
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
			resample.setRandomSeed(123);
			dataset = Filter.useFilter(dataset, resample);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Learn the classification
		try {
			for (int i = 0; i < classifier.length; i++) {
				Resample resample = new Resample();
				resample.setSampleSizePercent(33.0);
				resample.setInputFormat(dataset);
				resample.setRandomSeed(i * 100);
				Instances dataset_r = Filter.useFilter(dataset, resample);

				classifier[i].buildClassifier(dataset_r);
			}
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
				double[] temp = null;
				double[] distrib = new double[dataset.numClasses()];
				try {
					for (int i = 0; i < classifier.length; i++) {
						temp = classifier[i].distributionForInstance(instance);
						for (int j = 0; j < distrib.length; j++)
							distrib[j] += temp[j] / classifier.length;
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				for (int c = 0; c < distrib.length; c++)
					outputImage.setPixelXYBDouble(x, y, c, distrib[c]);
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

			Image samples =(Image) new  SamplesLoader().process(samplesPath);
			new Viewer2D().process(samples, "Samples of" + file);

			Image work =(Image) new  WekaSoftClassificationMulti5NN()
					.process(source, samples);

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
