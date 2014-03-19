package fr.unistra.pelican.algorithms.morphology.gray.geodesic;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.morphology.gray.GrayErosion;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 * Perform a gray geodesic opening by reconstruction with a structuring element.
 * Option to preserve input graylevels in the reconstructed image as suggested
 * in Salembier and Wilkinson, IEEE SPM 2009
 * 
 */
public class GrayOpeningByReconstruction extends Algorithm {

	/**
	 * Input Image
	 */
	public Image inputImage;

	/**
	 * Input structuring element
	 */
	public BooleanImage se;

	/**
	 * Flag to preserve graylevels of the input image in the reconstructed image
	 */
	public boolean preserve = false;

	/**
	 * Result
	 */
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public GrayOpeningByReconstruction() {
		super.inputs = "inputImage,se";
		super.options = "preserve";
		super.outputs = "outputImage";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = GrayErosion.exec(inputImage, se);
		if (!preserve)
			// Standard reconstruction
			outputImage = FastGrayReconstruction.exec(outputImage, inputImage);
		else {
			// Enhanced mask creation as described in Salembier and Wilkinson,
			// IEEE SPM 2009
			// TODO: optimize !!!
			Image erosion = outputImage;
			Image recons = FastGrayReconstruction.exec(outputImage, inputImage);
			Image maxima = GrayRegionalMaxima.exec(erosion);
			for (int p = 0; p < outputImage.size(); p++)
				if ((erosion.getPixelByte(p) == recons.getPixelByte(p))
						&& (maxima.getPixelByte(p) != 0))
					outputImage.setPixelByte(p, inputImage.getPixelByte(p));
				else
					outputImage.setPixelByte(p, 0);
			// Final reconstruction
			outputImage = FastGrayReconstruction.exec(outputImage, inputImage);

		}
	}

	/**
	 * Perform a gray geodesic opening by reconstruction with a structuring
	 * element.
	 * 
	 * @param inputImage
	 *            Input Image
	 * @param se
	 *            Input structuring element
	 * @return result...
	 */
	public static Image exec(Image inputImage, BooleanImage se) {
		return (Image) new GrayOpeningByReconstruction()
				.process(inputImage, se);
	}

	/**
	 * Perform a gray geodesic opening by reconstruction with a structuring
	 * element.
	 * 
	 * @param inputImage
	 *            Input Image
	 * @param se
	 *            Input structuring element
	 * @param preserve
	 *            Flag to preserve graylevels of the input image in the
	 *            reconstructed image
	 * 
	 * @return result...
	 */
	public static Image exec(Image inputImage, BooleanImage se, boolean preserve) {
		return (Image) new GrayOpeningByReconstruction().process(inputImage,
				se, preserve);
	}

	public static void main(String args[]) {
		Image input = ImageLoader.exec("samples/blood1.png");
		Viewer2D.exec(input);
		BooleanImage se = FlatStructuringElement2D
				.createCircleFlatStructuringElement(5);
		Viewer2D.exec(GrayOpeningByReconstruction.exec(input, se));
		Viewer2D.exec(GrayOpeningByReconstruction.exec(input, se, true));

	}

}
