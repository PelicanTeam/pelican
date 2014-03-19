package fr.unistra.pelican.algorithms.morphology.gray.geodesic;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.arithmetic.AdditionConstantChecked;
import fr.unistra.pelican.algorithms.arithmetic.Difference;

/**
 * Morphological maxima extraction using morphological reconstruction algorithm.
 * 
 * @author Aptoula, Lefevre
 * 
 */
public class GrayRegionalMaxima extends Algorithm {

	/**
	 * The input from which maxima should be extracted
	 */
	public Image input;

	/**
	 * The output image containing non-null values for regional maxima pixels
	 */
	public Image output;

	/**
	 * Constructor
	 */
	public GrayRegionalMaxima() {
		super.inputs = "input";
		super.outputs = "output";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		Image inputPlusOne = AdditionConstantChecked.exec(input, 1.0 / 255);
		Image reconstruction = FastGrayReconstruction.exec(input, inputPlusOne);
		output = Difference.exec(inputPlusOne, reconstruction);
	}

	/**
	 * Morphological maxima extraction using morphological reconstruction
	 * algorithm.
	 * 
	 * @param input
	 *            The input from which maxima should be extracted
	 * @return The output image containing non-null values for regional maxima
	 *         pixels
	 */
	public static Image exec(Image input) {
		return (Image) new GrayRegionalMaxima().process(input);
	}

	// public static void main(String[] args) {
	// Image img = ImageLoader.exec("samples/sower.jpg");
	// img = (Image) new AverageChannels().process(img);
	// img = (Image) new GrayRegionalMaxima().process(img);
	// img = (Image) new ManualThresholding().process(img, new Integer(1));
	// Viewer2D.exec(img, "regional maxima");
	// }

}