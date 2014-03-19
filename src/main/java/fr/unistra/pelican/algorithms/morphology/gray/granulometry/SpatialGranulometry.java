package fr.unistra.pelican.algorithms.morphology.gray.granulometry;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.morphology.gray.GrayClosing;
import fr.unistra.pelican.algorithms.morphology.gray.GrayOpening;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 * This class computes a normalized spatial granulometric curve on 4 directions
 * (0,45,90,135) with line shaped SEs.
 * 
 * Example : given 25 as length, 26 values for each orientation are computed.,
 * 13 with openings and 13 with closings.
 * 
 * The line lengths for size k are of 2xk + 1 pixels.
 * 
 * @author Erchan Aptoula
 */
public class SpatialGranulometry extends Algorithm {

	/**
	 * The input image
	 */
	public Image input;

	/**
	 * The length of the granulometric curve for each orientation
	 */
	public int length;

	/**
	 * The moment order for X dimension
	 */
	public int momentX;

	/**
	 * The moment order for Y dimension
	 */
	public int momentY;

	/**
	 * Flag to consider scale invariance or not
	 */
	public boolean scaleInvariance = false;

	/**
	 * The output granulometric curve
	 */
	public double[] curve;

	/**
	 * Default constructor
	 */

	public SpatialGranulometry() {
		super.inputs = "input,length,momentX,momentY";
		super.options = "scaleInvariance";
		super.outputs = "curve";
		
	}

	/**
	 * Computes normalised spatial granulometric curve on 4 directions (0,45,90,135)
	 * 
	 * @param input
	 *            The input image
	 * @param length
	 *            The length of the granulometric curve for each orientation
	 * @param momentX
	 *            The moment order for X dimension
	 * @param momentY
	 *            The moment order for Y dimension
	 * @return The output granulometric curve
	 */
	public static double[] exec(Image input, int length, int momentX,
			int momentY) {
		return (double[]) new SpatialGranulometry().process(input, length, momentX,
				momentY);
	}

	/**
	 * Computes normalised spatial granulometric curve on 4 directions (0,45,90,135)
	 * 
	 * @param input
	 *            The input image
	 * @param length
	 *            The length of the granulometric curve for each orientation
	 * @param momentX
	 *            The moment order for X dimension
	 * @param momentY
	 *            The moment order for Y dimension
	 * @param scaleInvariance
	 *            Flag to consider scale invariance or not
	 * @return The output granulometric curve
	 */
	public static double[] exec(Image input, int length, int momentX,
			int momentY, boolean scaleInvariance) {
		return (double[]) new SpatialGranulometry().process(input, length, momentX,
				momentY, scaleInvariance);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() {
		int size = (length + 1) * 4 * input.getBDim(); // length of SEs
		// increases in
		// steps of 2
		// System.err.println("size " + size);
		curve = new double[size];
		double[] originalVolumes = new double[input.getBDim()];
		for (int b = 0; b < input.getBDim(); b++)
			originalVolumes[b] = moment(input, b, momentX, momentY);
		// every size
		for (int i = 0; i < length; i += 2) {
			// System.err.println("size : " + i);
			// vertical line_____________________________
			BooleanImage se = FlatStructuringElement2D
					.createVerticalLineFlatStructuringElement(i * 2 + 1);
			// closing
			Image tmp = GrayClosing.exec(input, se);
			// place it...carefully..
			for (int b = 0; b < input.getBDim(); b++)
				curve[b * 4 * (length + 1) + (length + 1) / 2 - 1 - i / 2] = moment(
						tmp, b, momentX, momentY)
						/ originalVolumes[b];
			// opening
			tmp = GrayOpening.exec(input, se);
			// place it...carefully..
			for (int b = 0; b < input.getBDim(); b++)
				curve[b * 4 * (length + 1) + (length + 1) / 2 + i / 2] = moment(
						tmp, b, momentX, momentY)
						/ originalVolumes[b];
			// left diagonal line________________________
			se = FlatStructuringElement2D
					.createLeftDiagonalLineFlatStructuringElement(i * 2 + 1);
			// closing
			tmp = GrayClosing.exec(input, se);
			// place it...carefully..
			for (int b = 0; b < input.getBDim(); b++)
				curve[b * 4 * (length + 1) + (length + 1) + (length + 1) / 2
						- 1 - i / 2] = moment(tmp, b, momentX, momentY)
						/ originalVolumes[b];
			// opening
			tmp = GrayOpening.exec(input, se);
			// place it...carefully..
			for (int b = 0; b < input.getBDim(); b++)
				curve[b * 4 * (length + 1) + (length + 1) + (length + 1) / 2
						+ i / 2] = moment(tmp, b, momentX, momentY)
						/ originalVolumes[b];
			// horizontal line___________________________
			se = FlatStructuringElement2D
					.createHorizontalLineFlatStructuringElement(i * 2 + 1);
			// closing
			tmp = GrayClosing.exec(input, se);
			// place it...carefully..
			for (int b = 0; b < input.getBDim(); b++)
				curve[b * 4 * (length + 1) + 2 * (length + 1) + (length + 1)
						/ 2 - 1 - i / 2] = moment(tmp, b, momentX, momentY)
						/ originalVolumes[b];
			// opening
			tmp = GrayOpening.exec(input, se);
			// place it...carefully..
			for (int b = 0; b < input.getBDim(); b++)
				curve[b * 4 * (length + 1) + 2 * (length + 1) + (length + 1)
						/ 2 + i / 2] = moment(tmp, b, momentX, momentY)
						/ originalVolumes[b];
			// right diagonal line_______________________
			se = FlatStructuringElement2D
					.createRightDiagonalLineFlatStructuringElement(i * 2 + 1);
			// closing
			tmp = GrayClosing.exec(input, se);
			// place it...carefully..
			for (int b = 0; b < input.getBDim(); b++)
				curve[b * 4 * (length + 1) + 3 * (length + 1) + (length + 1)
						/ 2 - 1 - i / 2] = moment(tmp, b, momentX, momentY)
						/ originalVolumes[b];
			// opening
			tmp = GrayOpening.exec(input, se);
			// place it...carefully..
			for (int b = 0; b < input.getBDim(); b++)
				curve[b * 4 * (length + 1) + 3 * (length + 1) + (length + 1)
						/ 2 + i / 2] = moment(tmp, b, momentX, momentY)
						/ originalVolumes[b];
		}
	}

	private double moment(Image img, int channel, int i, int j) {
		double d = 0.0;
		double ort = 0.0;
		int gamma = (i + j) / 2 + 1;
		for (int x = 0; x < img.getXDim(); x++) {
			for (int y = 0; y < img.getYDim(); y++) {

				if ( input.isPresentXYB( x,y,channel ) ) { 

					double tmp = img.getPixelXYBDouble(x, y, channel);
					ort += tmp;
					d += Math.pow(x + 1, i) * Math.pow(y + 1, j) * tmp;
				}
			}
		}
		ort = Math.pow(ort, gamma);
		if ((i > 0 || j > 0) && scaleInvariance == true)
			d = d / ort;
		return d;
	}

}
