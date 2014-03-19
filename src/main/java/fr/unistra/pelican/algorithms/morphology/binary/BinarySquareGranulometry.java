package fr.unistra.pelican.algorithms.morphology.binary;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 * Performs a binary granulometry with a square shaped flat structuring element.
 * Optional flag allow the use of line structuring elements.
 * 
 * @author Aptoula, Lefevre
 */

public class BinarySquareGranulometry extends Algorithm {

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
	 * Indicates if horizontal line structuring element should be used
	 */
	public boolean horizontalOnly = false;

	/**
	 * Indicates if vertical line structuring element should be used
	 */
	public boolean verticalOnly = false;

	/**
	 * Indicates if pattern spectrum should be computed instead of standard
	 * granulometry
	 */
	public boolean diff = false;

	/**
	 * Resulting granulometry
	 */
	public Double[] output;

	/**
	 * Constructor
	 * 
	 */
	public BinarySquareGranulometry() {
		super.inputs = "input,length,flag";
		super.outputs = "output";
		super.options = "diff,horizontalOnly,verticalOnly";
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
		double original2 = input.volume();

		// every size
		for (int i = 0; i < length; i++) {
			int side = i * 2 + 1;
			BooleanImage seHor = FlatStructuringElement2D
					.createHorizontalLineFlatStructuringElement(side);
			BooleanImage seVer = FlatStructuringElement2D
					.createVerticalLineFlatStructuringElement(side);

			// schnell Hans, schnell!!!
			Image tmp = input;
			if (!verticalOnly)
				tmp = BinaryErosion.exec(tmp, seHor, flag);
			if (!horizontalOnly)
				tmp = BinaryErosion.exec(tmp, seVer, flag);
			if (!verticalOnly)
				tmp = BinaryDilation.exec(tmp, seHor, flag);
			if (!horizontalOnly)
				tmp = BinaryDilation.exec(tmp, seVer, flag);

			for (int b = 0; b < input.getBDim(); b++)
				output[b * length + i] = moment(tmp, b, MOMENTX, MOMENTY, 0)
						/ original2;
		}
		if (diff) {
			Double[] output2 = new Double[output.length];
			for (int i = 0; i < length; i++)
				for (int b = 0; b < input.getBDim(); b++) {
					if (i == 0)
						output2[b * length + i] = 1.0 - output[b * length + i];
					else
						output2[b * length + i] = output[b * length + i - 1]
								- output[b * length + i];
				}
			output = output2;
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
	 * This method performs a binary granulometry with a square shaped flat
	 * structuring element.
	 * 
	 * @param image
	 *            image to be processed
	 * @param length
	 *            size of the square struturing element
	 * @return granulometry array
	 */
	public static Double[] exec(Image input, Integer length, Boolean flag) {
		return (Double[]) new BinarySquareGranulometry().process(input, length,
				flag);
	}

	public static Double[] exec(Image input, Integer length, Boolean flag,
			boolean diff, boolean horizontalOnly, boolean verticalOnly) {
		return (Double[]) new BinarySquareGranulometry().process(input, length,
				flag, diff, horizontalOnly, verticalOnly);
	}

}
