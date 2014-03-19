package fr.unistra.pelican.algorithms.morphology.gray;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;

/**
 * This class performs a contrast mapping using either erosion/dilation or
 * opening/closing operations
 * 
 * @author Erchan Aptoula
 */
public class GrayContrastMapping extends Algorithm {

	/**
	 * The input image
	 */
	public Image input;

	/**
	 * The flat structuring element used in the morphological operation
	 */
	public BooleanImage se;

	/**
	 * The type of operation
	 */
	public int type = DilEroBased;

	/**
	 * Constant representing dilation-erosion operations
	 */
	public static final int DilEroBased = 0;

	/**
	 * Constant representing open-close operations
	 */
	public static final int OpenCloseBased = 1;

	/**
	 * The output image
	 */
	public Image output;

	/**
	 * Default constructor
	 */
	public GrayContrastMapping() {
		super.inputs = "input,se";
		super.options = "type";
		super.outputs = "output";
		
	}

	/**
	 * Performs a contrast mapping
	 * 
	 * @param input
	 *            The input image
	 * @param se
	 *            The flat structuring element used in the morphological operation
	 * @param type
	 *            The type of operation: DilEroBased or OpenCloseBased
	 * @return The output image
	 */
	public static Image exec(Image input, BooleanImage se, int type) {
		//FIXME : use GrayContrastMapping or GrayIterativeContrastMapping
		return (Image) new GrayContrastMapping().process(input, se, type);
	}

	/**
	 * Performs a contrast mapping using erosion/dilation
	 * 
	 * @param input
	 *            The input image
	 * @param se
	 *            The flat structuring element used in the morphological operation
	 * @return The output image
	 */
	public static Image exec(Image input, BooleanImage se) {
		//FIXME : use GrayContrastMapping or GrayIterativeContrastMapping
		return (Image) new GrayContrastMapping().process(input, se);
	}

	/*
	 * (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() {
		output = input.copyImage(false);
		Image image1 = null;
		Image image2 = null;
		if (type != DilEroBased && type != OpenCloseBased)
			throw new AlgorithmException("Invalid contrast mapping type");
		if (type == DilEroBased) {
			image1 = GrayErosion.exec(input, se);
			image2 = GrayDilation.exec(input, se);
		} else {
			image1 = GrayOpening.exec(input, se);
			image2 = GrayClosing.exec(input, se);
		}
		for (int b = 0; b < input.getBDim(); b++) {
			for (int x = 0; x < input.getXDim(); x++) {
				for (int y = 0; y < input.getYDim(); y++) {

					if ( !input.isPresentXYB( x,y,b ) ) continue;

					double i1 = image1.getPixelXYBDouble(x, y, b);
					double i2 = image2.getPixelXYBDouble(x, y, b);
					double o = input.getPixelXYBDouble(x, y, b);
					if (Math.abs(o - i1) < Math.abs(o - i2))
						output.setPixelXYBDouble(x, y, b, i1);
					else
						output.setPixelXYBDouble(x, y, b, i2);
				}
			}
		}
	}

}
