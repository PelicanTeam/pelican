package fr.unistra.pelican.algorithms.morphology.gray.geodesic;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.arithmetic.AdditionConstantChecked;
import fr.unistra.pelican.algorithms.arithmetic.Difference;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 * HMax contrast operator using morphological reconstruction algorithm.
 * 
 * @author Lefevre
 * 
 */
public class GrayHMax extends Algorithm {

	/**
	 * The input image
	 */
	public Image input;

	/**
	 * The contrast threshold
	 */
	public int h;

	/**
	 * The output image
	 */
	public Image output;

	/**
	 * Constructor
	 */
	public GrayHMax() {
		super.inputs = "input,h";
		super.outputs = "output";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		output = AdditionConstantChecked.exec(input, -h);
		output = FastGrayReconstruction.exec(output, input);
	}

	/**
	 * HMax contrast operator using morphological reconstruction algorithm. *
	 * 
	 * @param input
	 *            The input image
	 * @param h
	 *            The contrast threshold
	 * @return The output image
	 */
	public static Image exec(Image input, int h) {
		return (Image) new GrayHMax().process(input, h);
	}

	public static void main(String args[]) {
		Image input = ImageLoader.exec("samples/blood1.png");
		Viewer2D.exec(input);
		BooleanImage se = FlatStructuringElement2D
				.createCircleFlatStructuringElement(5);
		Viewer2D.exec(GrayHMax.exec(input, 100));
		// Viewer2D.exec(GrayOpeningByReconstruction.exec(input, se, true));

	}

}