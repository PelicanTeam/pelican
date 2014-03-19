package fr.unistra.pelican.algorithms.morphology.gray.granulometry;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.morphology.gray.GrayClosing;
import fr.unistra.pelican.algorithms.morphology.gray.GrayOpening;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 * This class computes a normalized granulometric curve on 4 directions
 * (0,45,90,135) with line shaped SEs.
 * 
 * Example : given 25 as length, 26 values for each orientation are computed.,
 * 13 with openings and 13 with closings.
 * 
 * The line lengths for size k are of 2xk + 1 pixels.
 * 
 * @author Erchan Aptoula
 */
public class Granulometry extends Algorithm {

	/**
	 * The input image
	 */
	public Image input;

	/**
	 * The length of the granulometric curve for each orientation
	 */
	public int length;

	/**
	 * The output granulometric curve
	 */
	public double[] curve;

	/**
	 * Default constructor
	 */
	public Granulometry() {
		super.inputs = "input,length";
		super.outputs = "curve";

	}

	/**
	 * Computes a normalized granulometric curve on 4 directions (0,45,90,135)
	 * with line shaped SEs.
	 * 
	 * @param input
	 *          The input image
	 * @param length
	 *          The length of the granulometric curve for each orientation
	 * @return The output granulometric curve
	 */
	public static double[] exec(Image input, int length) {
		return (double[]) new Granulometry().process(input, length);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() {
		int size = (length + 1) * 4 * input.getBDim(); // length of SEs
		// increases in steps of 2
		curve = new double[size];
		double[] originalVolumes = new double[input.getBDim()];
		for (int b = 0; b < input.getBDim(); b++)
			originalVolumes[b] = volume(input, b);
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
				curve[b * 4 * (length + 1) + (length + 1) / 2 - 1 - i / 2] = volume(
					tmp, b)
					/ originalVolumes[b];
			// opening
			tmp = GrayOpening.exec(input, se);
			// place it...carefully..
			for (int b = 0; b < input.getBDim(); b++)
				curve[b * 4 * (length + 1) + (length + 1) / 2 + i / 2] = volume(tmp, b)
					/ originalVolumes[b];
			// left diagonal line________________________
			se = FlatStructuringElement2D
				.createLeftDiagonalLineFlatStructuringElement(i * 2 + 1);
			// closing
			tmp = GrayClosing.exec(input, se);
			// place it...carefully..
			for (int b = 0; b < input.getBDim(); b++)
				curve[b * 4 * (length + 1) + (length + 1) + (length + 1) / 2 - 1 - i
					/ 2] = volume(tmp, b) / originalVolumes[b];
			// opening
			tmp = GrayOpening.exec(input, se);
			// place it...carefully..
			for (int b = 0; b < input.getBDim(); b++)
				curve[b * 4 * (length + 1) + (length + 1) + (length + 1) / 2 + i / 2] = volume(
					tmp, b)
					/ originalVolumes[b];
			// horizontal line___________________________
			se = FlatStructuringElement2D
				.createHorizontalLineFlatStructuringElement(i * 2 + 1);
			// closing
			tmp = GrayClosing.exec(input, se);
			// place it...carefully..
			for (int b = 0; b < input.getBDim(); b++)
				curve[b * 4 * (length + 1) + 2 * (length + 1) + (length + 1) / 2 - 1
					- i / 2] = volume(tmp, b) / originalVolumes[b];
			// opening
			tmp = GrayOpening.exec(input, se);
			// place it...carefully..
			for (int b = 0; b < input.getBDim(); b++)
				curve[b * 4 * (length + 1) + 2 * (length + 1) + (length + 1) / 2 + i
					/ 2] = volume(tmp, b) / originalVolumes[b];
			// right diagonal line_______________________
			se = FlatStructuringElement2D
				.createRightDiagonalLineFlatStructuringElement(i * 2 + 1);
			// closing
			tmp = GrayClosing.exec(input, se);
			// place it...carefully..
			for (int b = 0; b < input.getBDim(); b++)
				curve[b * 4 * (length + 1) + 3 * (length + 1) + (length + 1) / 2 - 1
					- i / 2] = volume(tmp, b) / originalVolumes[b];
			// opening
			tmp = GrayOpening.exec(input, se);
			// place it...carefully..
			for (int b = 0; b < input.getBDim(); b++)
				curve[b * 4 * (length + 1) + 3 * (length + 1) + (length + 1) / 2 + i
					/ 2] = volume(tmp, b) / originalVolumes[b];
		}
	}

	private double volume( Image img, int channel )
	{
		double d = 0.0;
		for ( int x = 0 ; x < img.getXDim() ; x++ ) {
			for ( int y = 0 ; y < img.getYDim() ; y++ ) { 

				if ( input.isPresentXYB( x,y,channel ) )
					d += img.getPixelXYBDouble( x,y, channel );
		}	}
		return d;
	}

}
