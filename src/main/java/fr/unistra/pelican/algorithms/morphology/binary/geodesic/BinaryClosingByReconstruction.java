package fr.unistra.pelican.algorithms.morphology.binary.geodesic;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.arithmetic.Inversion;
import fr.unistra.pelican.algorithms.morphology.binary.BinaryErosion;

/**
 * Perform a binary geodesic closing by reconstruction with a structuring element.
 * 
 */
public class BinaryClosingByReconstruction extends Algorithm {

	// Inputs parameters
	public Image inputImage;

	public BooleanImage se;

	// Outputs parameters
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public BinaryClosingByReconstruction() {
		super.inputs = "inputImage,se";
		super.outputs = "outputImage";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		Image tmp=Inversion.exec(inputImage);
		outputImage= BinaryErosion.exec(tmp, se);
		outputImage= Inversion.exec(FastBinaryReconstruction.exec(outputImage,
					tmp));
}
	
	@SuppressWarnings("unchecked")
	public static <T extends Image> T exec(T im, BooleanImage se) {
		return (T) new BinaryClosingByReconstruction().process(im,se);
	}

}
