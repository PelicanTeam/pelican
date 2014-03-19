package fr.unistra.pelican.algorithms.morphology.gray;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;

/**
 * This class performs a gray OCCO (mean between opening then closing 
 * and closing then opening) with the same 2-D structuring element
 * 
 * The name is taken from: 
 * Soille, Morphological Image Analysis
 * 
 * @author Erchan Aptoula
*/
public class GrayOCCO extends Algorithm {

	/**
	 * The input image
	 */
	public Image inputImage;

	/**
	 * The flat structuring element used in the morphological operation
	 */
	public BooleanImage se;

	/**
	 * The output image
	 */
	public Image outputImage;

	/**
	 * Default constructor
	 */
	public GrayOCCO() {
		super.inputs = "inputImage,se";
		super.outputs = "outputImage";
		
	}

	/**
	 * Performs a gray OCCO (mean between opening then closing 
	 * and closing then opening) with the same 2-D structuring element
	 * 
	 * @param inputImage
	 *            The input image
	 * @param se
	 *            The flat structuring element used in the morphological
	 *            operation
	 * @return The output image
	 */
	public static Image exec(Image inputImage, BooleanImage se) {
		return (Image) new GrayOCCO().process(inputImage, se);
	}

	/*
	 * (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() {
		outputImage = inputImage.copyImage(false);
		Image tmp = inputImage.copyImage(false);
		// opening then closing
		outputImage = GrayOpening.exec(inputImage, se);
		outputImage = GrayClosing.exec(outputImage, se);
		// closing then opening
		tmp = GrayClosing.exec(inputImage, se);
		tmp = GrayOpening.exec(tmp, se);
		// Merge by mean.
		int size = inputImage.size();
		for (int i = 0; i < size; i++) {

			if ( !inputImage.isPresent(i) ) continue;

			double p1 = outputImage.getPixelDouble(i);
			double p2 = tmp.getPixelDouble(i);
			outputImage.setPixelDouble(i, (p1 + p2) / 2.0);
			}
	}

}
