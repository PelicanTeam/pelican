package fr.unistra.pelican.algorithms.morphology.gray.geodesic;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.arithmetic.Difference;

/**
 * Perform a gray geodesic opening by reconstruction with a structuring element.
 * Work on an int precision.
 * 
 * @author Erchan Aptoula, Benjamin Perret
 */
public class GrayOpenTopHatByReconstruction extends Algorithm {
	
	/**
	 * Input image
	 */
	public Image inputImage;

	/**
	 * Input structuring element
	 */
	public BooleanImage se;

	/**
	 * Result
	 */
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public GrayOpenTopHatByReconstruction() {

		super();
		super.inputs = "inputImage,se";
		super.outputs = "outputImage";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = inputImage.copyImage(false);

		try {
			Image tmp = (Image) new GrayOpeningByReconstruction().process(inputImage, se);
			outputImage = (Image) new Difference().process(inputImage, tmp);

		} catch (PelicanException e) { e.printStackTrace(); }
	}

	/**
	 * Perform a gray geodesic opening by reconstruction with a structuring element.
	 * Work on an int precision.
	 *
	 * @param inputImage Input Image
	 * @param se Input structuring element
	 * @return result...
	 */
	public static Image exec(Image inputImage, BooleanImage se) {
		return (Image) new GrayOpenTopHatByReconstruction().process(inputImage, se);
	}
}
