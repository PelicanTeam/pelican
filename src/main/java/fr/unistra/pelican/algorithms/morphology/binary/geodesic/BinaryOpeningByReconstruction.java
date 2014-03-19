package fr.unistra.pelican.algorithms.morphology.binary.geodesic;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.morphology.binary.BinaryErosion;

/**
 * Perform a binary geodesic opening by reconstruction with a structuring element.
 * 
 */
public class BinaryOpeningByReconstruction extends Algorithm {

	// Inputs parameters
	public Image inputImage;

	public BooleanImage se;

	// Outputs parameters
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public BinaryOpeningByReconstruction() {
		super.inputs = "inputImage,se";
		super.outputs = "outputImage";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = BinaryErosion.exec(inputImage, se);
		outputImage = FastBinaryReconstruction.exec(outputImage,
					inputImage);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Image> T exec(T im, BooleanImage se) {
		return (T) new BinaryOpeningByReconstruction().process(im,se);
	}

}
