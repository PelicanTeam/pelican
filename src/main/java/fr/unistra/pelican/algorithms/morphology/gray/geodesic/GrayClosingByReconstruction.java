package fr.unistra.pelican.algorithms.morphology.gray.geodesic;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.morphology.gray.GrayDilation;

/**
 * Perform a gray geodesic closing by reconstruction with a structuring element.
 * Work on an int precision.
 * 
 * @author Erchan Aptoula, Benjamin Perret
 */
public class GrayClosingByReconstruction extends Algorithm {

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
	public GrayClosingByReconstruction() {
		super.inputs = "inputImage,se";
		super.outputs = "outputImage";

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = GrayDilation.exec(inputImage, se);
		outputImage = FastGrayReconstruction.exec( outputImage, inputImage, true );
		
	}

	/**
	 * Perform a gray geodesic closing by reconstruction with a structuring
	 * element. Work on an int precision.
	 * 
	 * @param inputImage
	 *            Input Image
	 * @param se
	 *            Input structuring element
	 * @return result...
	 */
	public static Image exec(Image inputImage, BooleanImage se) {
		return (Image) new GrayClosingByReconstruction().process(inputImage, se);
	}

}
