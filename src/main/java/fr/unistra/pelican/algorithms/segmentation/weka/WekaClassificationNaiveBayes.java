package fr.unistra.pelican.algorithms.segmentation.weka;

import weka.classifiers.bayes.NaiveBayes;
import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.InvalidNumberOfParametersException;
import fr.unistra.pelican.InvalidTypeOfParameterException;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.io.SamplesLoader;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToColorByMeanValue;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

/**
 * Perform a classification using the Naive Bayes Weka algorithm. Each band from
 * input represents a attribute. Each band from samples represent a class
 * exemples.
 * @author Sébastien Derivaux
 */
public class WekaClassificationNaiveBayes extends Algorithm {

	// Inputs parameters
	public Image inputImage;

	public Image samples;

	// Outputs parameters
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public WekaClassificationNaiveBayes() {
		super.inputs = "inputImage,samples";
		super.outputs = "outputImage";
	}

	public static Image exec(Image inputImage,Image samples) {
		return (Image) new WekaClassificationNaiveBayes().process(inputImage,samples);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		NaiveBayes classifier = new NaiveBayes();

		classifier.setUseKernelEstimator(false);
		classifier.setUseSupervisedDiscretization(false);

		try {
			outputImage = (Image) new WekaClassification().process(inputImage, classifier,
					samples);
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

			Image work = (Image) new WekaClassificationNaiveBayes().process(source, samples);

			// View It!
			new Viewer2D().process(new LabelsToColorByMeanValue().process(work, source),
					"Classification for " + file);

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
