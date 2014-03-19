package fr.unistra.pelican.algorithms.segmentation.weka;

import weka.classifiers.lazy.IBk;
import weka.core.SelectedTag;
import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
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
public class WekaSoftClassification5NN extends Algorithm {

	// Inputs parameters
	public Image inputImage;

	public Image samples;

	// Outputs parameters
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public WekaSoftClassification5NN() {

		super();
		super.inputs = "inputImage,samples";
		super.outputs = "outputImage";
		
	}

	public static Image exec(Image inputImage, Image samples) {
		return (Image) new WekaClassification5NN().process(inputImage,samples);
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		IBk classifier = new IBk();
		classifier.setKNN(5);
		classifier.setWindowSize(500);
		classifier.setDistanceWeighting(new SelectedTag(IBk.WEIGHT_INVERSE,
				IBk.TAGS_WEIGHTING));
		classifier
				.setNearestNeighbourSearchAlgorithm(new weka.core.neighboursearch.CoverTree());

		try {
			outputImage = (Image) new WekaSoftClassification().process(inputImage,
					classifier, samples);
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

			Image work = (Image) new WekaSoftClassification5NN().process(source, samples);

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
