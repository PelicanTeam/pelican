package fr.unistra.pelican.algorithms.segmentation.weka;

import weka.classifiers.lazy.IBk;
import weka.core.SelectedTag;
import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.geometric.Padding;
import fr.unistra.pelican.algorithms.geometric.ResamplingByRatio;


/**
 * Perform a soft classification using the 5-KNN Weka algorithm. Use a
 * subsampled image for the classification.
 */
public class WekaSoftAndCoarseClassificationKNN extends Algorithm {

	// Inputs parameters
	/**
	 * The input image
	 */
	public Image inputImage;

	/**
	 * The sample image
	 */
	public Image samples;

	/**
	 * The subsampling ration (<= 1)
	 */
	public double ratio = 0.5;

	/**
	 * The output image
	 */
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public WekaSoftAndCoarseClassificationKNN() {
		super.inputs = "inputImage,samples";
		super.options = "ratio";
		super.outputs = "outputImage";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() {
		IBk classifier = new IBk();
		classifier.setKNN(5);
		classifier.setWindowSize(100);
		classifier.setDistanceWeighting(new SelectedTag(IBk.WEIGHT_INVERSE,
			IBk.TAGS_WEIGHTING));

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
		outputImage = (Image) new WekaSoftClassification().process(reduce,
			classifier, samples);
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

	public static Image exec(Image input, Image labels) {
		return (Image) new WekaSoftAndCoarseClassificationKNN().process(input,
			labels);
	}

	public static Image exec(Image input, Image labels, double ratio) {
		return (Image) new WekaSoftAndCoarseClassificationKNN().process(input,
			labels, ratio);
	}

}
