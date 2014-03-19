package fr.unistra.pelican.algorithms.segmentation.weka;

import weka.classifiers.lazy.IBk;
import weka.core.SelectedTag;
import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;


/**
 * Perform a classification using the k-Nearest Neighbour Weka algorithm. Each
 * band from input represents a attribute. Each band from samples represent a
 * class exemples.
 * 
 * @author Lefevre
 */
public class WekaClassificationKNN extends Algorithm {

	// Inputs parameters
	public Image inputImage;

	public Image samples;

	public int k;
	
	public boolean stats=false;

	// Outputs parameters
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public WekaClassificationKNN() {
		super.inputs = "inputImage,samples,k";
		super.options="false";
		super.outputs = "outputImage";
	}

	public static Image exec(Image inputImage, Image samples,int k) {
		return (Image) new WekaClassificationKNN().process(inputImage, samples,k);
	}

	public static Image exec(Image inputImage, Image samples,int k,boolean stats) {
		return (Image) new WekaClassificationKNN().process(inputImage, samples,k,stats);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		IBk classifier = new IBk();
		classifier.setKNN(k);
		classifier.setWindowSize(100);
		classifier.setDistanceWeighting(new SelectedTag(IBk.WEIGHT_INVERSE,
			IBk.TAGS_WEIGHTING));
		classifier
			.setNearestNeighbourSearchAlgorithm(new weka.core.neighboursearch.CoverTree());

		outputImage = WekaClassification.exec(inputImage,
			classifier, samples,stats);
		
	}
}
