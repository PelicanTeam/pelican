package fr.unistra.pelican.algorithms.morphology.binary;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 * Performs a binary anti-granulometry with a square shaped flat structuring
 * element.
 * 
 * @author Erhan Aptoula, Jonathan Weber
 */

public class BinarySquareAntiGranulometry extends Algorithm {
	/**
	 * Image to be processed
	 */
	public Image input;

	/**
	 * Size of the square structuring element
	 */
	public Integer length;

	/**
	 * Indicates if structuring element is convex or not
	 */
	public Boolean flag;

	
	
	/**
	 * Resulting anti granulometry
	 */
	public Double[] output;

	/**
	 * Constructor
	 * 
	 */
	public BinarySquareAntiGranulometry() {
		super.inputs = "input,length,flag";
		super.outputs = "output";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		int size = length * input.getBDim(); // size of SEs increases in
												// steps of 2
		output = new Double[size];

		int MOMENTX = 0;
		int MOMENTY = 0;

		// every size
		for (int i = 1; i <= length; i++) {
			int side = i * 2 + 1;
			BooleanImage seHor = FlatStructuringElement2D
					.createHorizontalLineFlatStructuringElement(side);
			BooleanImage seVer = FlatStructuringElement2D
					.createVerticalLineFlatStructuringElement(side);

			// schnell Hans, schnell!!!
			Image tmp = BinaryDilation.exec(input, seHor, flag);
			tmp = BinaryDilation.exec(tmp, seVer, flag);
			tmp = BinaryErosion.exec(tmp, seHor, flag);
			tmp = BinaryErosion.exec(tmp, seVer, flag);

			for (int b = 0; b < input.getBDim(); b++) {
				Image tmp2 = input.copyImage(false);
				tmp2.fill(1.0);
				double original = moment(tmp2, b, MOMENTX, MOMENTY, side / 2);

				if (original <= 0)
					output[b * length + i - 1] = 0.0;
				else
					output[b * length + i - 1] = moment( tmp,b,MOMENTX,MOMENTY,side/2 ) / original;
			}
		}
	}

	private double moment(Image img, int channel, int i, int j, int radius) {
		double d = 0.0;

		for (int x = radius; x < img.getXDim() - radius; x++) {
			for (int y = radius; y < img.getYDim() - radius; y++) {

				if ( img.isPresentXYB( x,y, channel ) )
					d += ( img.getPixelXYBBoolean( x,y, channel ) ) ? 1 : 0;
			}
		}

		return d;
	}

	/**
	 * This method performs a binary anti-granulometry with a square shaped flat structuring
	 * element.
	 * @param image image to be processed
	 * @param length size of the square struturing element
	 * @return Anti granulometry array
	 */
	public static Double[] exec(Image image, Integer length,Boolean flag) {
		return (Double[]) new BinarySquareAntiGranulometry().process(image,length,flag);
	}

}
