package fr.unistra.pelican.algorithms.segmentation.weka;

import weka.classifiers.lazy.IBk;
import weka.core.SelectedTag;
import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.geometric.Padding;
import fr.unistra.pelican.algorithms.geometric.ResamplingByRatio;


/**
 * Perform a classification using the k-Nearest Neighbour Weka algorithm. Each
 * band from input represents a attribute. Each band from samples represent a
 * class exemples.
 * 
 * @author Lefevre
 */
public class WekaCoarseClassificationKNN extends Algorithm {

	// Inputs parameters
	public Image inputImage;

	public Image samples;

	public int k;
	
	/**
	 * The subsampling ration (<= 1)
	 */
	public double ratio = 0.5;

	public boolean stats=false;

	// Outputs parameters
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public WekaCoarseClassificationKNN() {
		super.inputs = "inputImage,samples,k";
		super.options="stats,ratio";
		super.outputs = "outputImage";
	}

	public static Image exec(Image inputImage, Image samples,int k) {
		return (Image) new WekaCoarseClassificationKNN().process(inputImage, samples,k);
	}

	public static Image exec(Image inputImage, Image samples,int k,boolean stats) {
		return (Image) new WekaCoarseClassificationKNN().process(inputImage, samples,k,stats);
	}

	public static Image exec(Image inputImage, Image samples,int k,boolean stats,double ratio) {
		return (Image) new WekaCoarseClassificationKNN().process(inputImage, samples,k,stats,ratio);
	}

	public static Image exec(Image inputImage, Image samples,int k,double ratio) {
		return (Image) new WekaCoarseClassificationKNN().process(inputImage, samples,k,null,ratio);
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

		Image reduce = inputImage;
		
		if (ratio < 1) {
			reduce = (Image) new ResamplingByRatio().process(inputImage, inputImage
				.getXDim() > 1 ? ratio : 1, inputImage.getYDim() > 1 ? ratio : 1,
				inputImage.getZDim() > 1 ? ratio : 1, inputImage.getTDim() > 1 ? ratio
					: 1, 1.0, ResamplingByRatio.NEAREST);
			samples = (Image) new ResamplingByRatio().process(samples, inputImage
				.getXDim() > 1 ? ratio : 1, inputImage.getYDim() > 1 ? ratio : 1,
				inputImage.getZDim() > 1 ? ratio : 1, inputImage.getTDim() > 1 ? ratio
					: 1, 1.0, ResamplingByRatio.NEAREST);
		}

		outputImage = WekaClassification.exec(reduce,
			classifier, samples,stats);
		
		if (ratio < 1) {
			outputImage = (Image) new ResamplingByRatio().process(outputImage,
				inputImage.getXDim() > 1 ? 1 / ratio : 1,
				inputImage.getYDim() > 1 ? 1 / ratio : 1,
				inputImage.getZDim() > 1 ? 1 / ratio : 1,
				inputImage.getTDim() > 1 ? 1 / ratio : 1, 1.0,
				ResamplingByRatio.NEAREST);
			outputImage = (Image) new Padding().process(outputImage, inputImage
				.getXDim(), inputImage.getYDim(), inputImage.getZDim(), inputImage
				.getTDim(), outputImage.getBDim(), Padding.BORDERS);
		}
		
	}
}
