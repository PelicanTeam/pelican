package fr.unistra.pelican.algorithms.morphology.gray.geodesic;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;

/**
 * Performs an OCCO by reconstruction
 * 
 * @author Erchan Aptoula, Benjamin Perret
 */
public class GrayOCCOByReconstruction extends Algorithm {
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
	public GrayOCCOByReconstruction() {
		super.inputs = "inputImage,se";
		super.outputs = "outputImage";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException { 

			outputImage = GrayOpeningByReconstruction.exec(inputImage, se);
			outputImage = GrayClosingByReconstruction.exec(outputImage, se);
			// closing then opening
			Image tmp = (Image) new GrayClosingByReconstruction().process(inputImage, se);
			tmp = (Image) new GrayOpeningByReconstruction().process(tmp, se);
			// Merge by mean.
			int size = inputImage.size();
			for ( int i = 0 ; i < size ; i++ ) { 

				if ( !inputImage.isPresent(i) ) continue;

				double p1 = outputImage.getPixelDouble(i);
				double p2 = tmp.getPixelDouble(i);
				outputImage.setPixelDouble(i, (p1 + p2) / 2.0);
			}
	}

	/**
	 * Performs an OCCO by reconstruction
	 *
	 * @param inputImage Input Image
	 * @param se Input structuring element
	 * @return result...
	 */
	public static Image exec(Image inputImage, BooleanImage se) {
		return (Image) new GrayOCCOByReconstruction().process(inputImage, se);
	}
}
