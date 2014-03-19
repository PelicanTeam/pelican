package fr.unistra.pelican.algorithms.morphology.gray;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.arithmetic.AbsoluteDifference;
import fr.unistra.pelican.algorithms.arithmetic.Maximum;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 * This class performs corner detection using Robert Laganiere's method, 1998
 * and the improvement proposed by Shih et al, 2004.
 * 
 * @author Erchan Aptoula
 */
public class GrayCornerDetection extends Algorithm {

	/**
	 * The input image
	 */
	public Image input;

	/**
	 * The output image
	 */
	public Image output;

	/**
	 * Default constructor
	 */
	public GrayCornerDetection() {
		super.inputs = "input";
		super.outputs = "output";
		
	}

	/**
	 * Performs corner detection using Robert Laganiere's method, 1998 and the
	 * improvement proposed by Shih et al, 2004.
	 * 
	 * @param input
	 *            The input image
	 * @return The output image
	 */
	public static Image exec(Image input) {
		return (Image) new GrayCornerDetection().process(input);
	}

	/*
	 * (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		output = input.copyImage(false);
		BooleanImage square = FlatStructuringElement2D
				.createSquareFlatStructuringElement(5);
		BooleanImage cross = FlatStructuringElement2D
				.createCrossFlatStructuringElement(2);
		BooleanImage losange = FlatStructuringElement2D
				.createSquareFlatStructuringElement(5);
		losange.setPixelXYBoolean(0, 0, false);
		losange.setPixelXYBoolean(0, 1, false);
		losange.setPixelXYBoolean(0, 3, false);
		losange.setPixelXYBoolean(0, 4, false);
		losange.setPixelXYBoolean(1, 0, false);
		losange.setPixelXYBoolean(1, 4, false);
		losange.setPixelXYBoolean(3, 0, false);
		losange.setPixelXYBoolean(3, 4, false);
		losange.setPixelXYBoolean(4, 0, false);
		losange.setPixelXYBoolean(4, 1, false);
		losange.setPixelXYBoolean(4, 3, false);
		losange.setPixelXYBoolean(4, 4, false);
		BooleanImage bigX = FlatStructuringElement2D
				.createSquareFlatStructuringElement(5);
		bigX.setPixelXYBoolean(0, 1, false);
		bigX.setPixelXYBoolean(0, 2, false);
		bigX.setPixelXYBoolean(0, 3, false);
		bigX.setPixelXYBoolean(1, 0, false);
		bigX.setPixelXYBoolean(1, 2, false);
		bigX.setPixelXYBoolean(1, 4, false);
		bigX.setPixelXYBoolean(2, 0, false);
		bigX.setPixelXYBoolean(2, 1, false);
		bigX.setPixelXYBoolean(2, 3, false);
		bigX.setPixelXYBoolean(2, 4, false);
		bigX.setPixelXYBoolean(3, 0, false);
		bigX.setPixelXYBoolean(3, 2, false);
		bigX.setPixelXYBoolean(3, 4, false);
		bigX.setPixelXYBoolean(4, 1, false);
		bigX.setPixelXYBoolean(4, 2, false);
		bigX.setPixelXYBoolean(4, 3, false);
		Image first = GrayDilation.exec(input, cross);
		first = GrayErosion.exec(first, losange);
		first = AbsoluteDifference.exec(input, first);
		Image second = GrayDilation.exec(input, bigX);
		second = GrayErosion.exec(second, square);
		second = AbsoluteDifference.exec(input, second);
		output = Maximum.exec(first, second);
	}
}
