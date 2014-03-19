package fr.unistra.pelican.algorithms.morphology.gray;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
/**
 * This class performs a morphological center
 * 
 * @author 
 */
public class GrayCenter extends Algorithm {

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
	public GrayCenter() {
		super.inputs = "inputImage,se";
		super.outputs = "outputImage";
	}

	/**
	 * Applies a morphological center
	 * 
	 * @param input
	 *            The input image
	 * @param se
	 *            The flat structuring element used in the morphological
	 *            operation
	 * @return The output image
	 */
	public static Image exec(Image inputImage, BooleanImage se) {
		return (Image) new GrayCenter().process(inputImage, se);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() {
		
		
		outputImage = inputImage.copyImage(false);
		Image gfg = GrayOpening.exec(inputImage, se);
		gfg = GrayClosing.exec(gfg, se);
		gfg = GrayOpening.exec(gfg, se);
		Image fgf = GrayClosing.exec(inputImage, se);
		fgf = GrayOpening.exec(fgf, se);
		fgf = GrayClosing.exec(fgf, se);
		for (int b = 0; b < inputImage.getBDim(); b++) {
			for (int x = 0; x < inputImage.getXDim(); x++) {
				for (int y = 0; y < inputImage.getYDim(); y++) {

					if ( !inputImage.isPresentXYB( x,y,b ) ) continue;

					double d = gfg.getPixelXYBDouble(x, y, b);
					double e = fgf.getPixelXYBDouble(x, y, b);
					double o = inputImage.getPixelXYBDouble(x, y, b);
					double s = Math.min(Math.max(o, Math.min(d, e)), Math.max(
							d, e));
					outputImage.setPixelXYBDouble(x, y, b, s);
				}
			}
		}
	}

}
