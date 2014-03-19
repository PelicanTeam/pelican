package fr.unistra.pelican.algorithms.segmentation.weka;

import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
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
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.io.SamplesLoader;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToColorByMeanValue;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

/**
 * Perform a classification using a Weka algorithm. Each band from input
 * represents a attribute. Each band from samples represent a class exemples.
 * @author Sï¿œbastien Derivaux
 */
public class WekaClassification extends Algorithm {

	// Inputs parameters
	public Image inputImage;

	public Classifier classifier;

	public Image samples;

	public boolean stats=false;

	// Outputs parameters
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public WekaClassification() {
		super.inputs = "inputImage,classifier,samples";
		super.options="stats";
				super.outputs = "outputImage";
	}

	public static Image exec(Image inputImage, Classifier classifier,Image samples) {
		return (Image) new WekaClassification().process(inputImage, classifier,samples);
	}

	public static Image exec(Image inputImage, Classifier classifier,Image samples,boolean stats) {
		return (Image) new WekaClassification().process(inputImage, classifier,samples,stats);
	}

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = new IntegerImage(inputImage.getXDim(), inputImage
				.getYDim(), 1, 1, 1);

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

			// Resample or not resample to uniformize class distribution??
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
			Evaluation eval = new Evaluation(dataset);
			eval.crossValidateModel(classifier, dataset, 10, new Random());
			if(stats) {
			System.out.println(eval.toMatrixString());
			System.out.println(eval.toClassDetailsString());
			System.out.println(eval.toSummaryString());
			}
			classifier.buildClassifier(dataset);

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
				int label = -1;
				try {
					label = (int) classifier.classifyInstance(instance);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				outputImage.setPixelXYInt(x, y, label);
			}
	}

	public static void main(String[] args) {
		String file = "samples/remotesensing1.png";
		String samplesPath = "samples/remotesensing1";
		if (args.length > 0)
			file = args[0];
			Image source = (Image) new ImageLoader().process(file);
			new Viewer2D().process(source, "Image " + file);
			Image samples = (Image) new SamplesLoader().process(samplesPath);
			new Viewer2D().process(samples, "Samples of" + file);
			MultilayerPerceptron classifier = new MultilayerPerceptron();
			Image work = (Image) new WekaClassification()
					.process(source, classifier, samples);
			// View It!
			new Viewer2D().process(new LabelsToColorByMeanValue().process(work,
					source), "Classification for " + file);
	}
}
