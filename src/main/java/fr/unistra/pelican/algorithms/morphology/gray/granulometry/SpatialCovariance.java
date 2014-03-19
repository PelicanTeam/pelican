package fr.unistra.pelican.algorithms.morphology.gray.granulometry;

import java.awt.Point;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.morphology.gray.GrayOpening;

/**
 * This class computes normalised spatial covariance curve on 4 directions
 * (0,45,90,135).
 * 
 * example : Given 25 as number of different lengths, 25 values are computed for
 * each orientation, with pixel distances ranging from 1 to 2 x 25, in
 * increments of 2. The user is encouraged to employ different SE forms.
 * 
 * All pixels beyond image borders are ignored.
 * 
 * @author Erchan Aptoula
 */
public class SpatialCovariance extends Algorithm {

	/**
	 * The input image
	 */
	public Image input;

	/**
	 * The length of the covariance curve for each orientation
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
	 * The output covariance curve
	 */
	public double[] curve;

	/**
	 * Default constructor
	 */

	public SpatialCovariance() {
		super.inputs = "input,length,momentX,momentY";
		super.options = "scaleInvariance";
		super.outputs = "curve";
		
	}

	/**
	 * Computes normalised spatial covariance curve on 4 directions (0,45,90,135)
	 * 
	 * @param input
	 *            The input image
	 * @param length
	 *            The length of the covariance curve for each orientation
	 * @param momentX
	 *            The moment order for X dimension
	 * @param momentY
	 *            The moment order for Y dimension
	 * @return The output covariance curve
	 */
	public static double[] exec(Image input, int length, int momentX,
			int momentY) {
		return (double[]) new SpatialCovariance().process(input, length, momentX,
				momentY);
	}

	/**
	 * Computes normalised spatial covariance curve on 4 directions (0,45,90,135)
	 * 
	 * @param input
	 *            The input image
	 * @param length
	 *            The length of the covariance curve for each orientation
	 * @param momentX
	 *            The moment order for X dimension
	 * @param momentY
	 *            The moment order for Y dimension
	 * @param scaleInvariance
	 *            Flag to consider scale invariance or not
	 * @return The output covariance curve
	 */
	public static double[] exec(Image input, int length, int momentX,
			int momentY, boolean scaleInvariance) {
		return (double[]) new SpatialCovariance().process(input, length, momentX,
				momentY, scaleInvariance);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() {
		int size = length * 4 * input.getBDim();
		curve = new double[size];
		double[] originalVolumes = new double[input.getBDim()];
		for (int b = 0; b < input.getBDim(); b++)
			originalVolumes[b] = moment(input, b, momentX, momentY);
		// every size
		for (int i = 1; i <= length; i++) {
			int side = i * 2 + 1;
			// vertical line_____________________________
			BooleanImage se = new BooleanImage(side, side,1,1,1);
			se.setCenter(new Point(i, i));
			se.setPixelXYBoolean(0, i, true);
			se.setPixelXYBoolean(side - 1, i, true);
			// Opening
			Image tmp = GrayOpening.exec(input, se);
			// place it...carefully..
			for (int b = 0; b < input.getBDim(); b++)
				curve[b * 4 * length + i - 1] = moment(tmp, b, momentX, momentY)
						/ originalVolumes[b];
			// left diagonal line________________________
			se = new BooleanImage(side, side,1,1,1);
			se.setCenter(new Point(i, i));
			se.setPixelXYBoolean(0, 0, true);
			se.setPixelXYBoolean(side - 1, side - 1, true);
			// Opening
			tmp = GrayOpening.exec(input, se);
			// place it...carefully..
			for (int b = 0; b < input.getBDim(); b++)
				curve[b * 4 * length + length + i - 1] = moment(tmp, b,
						momentX, momentY)
						/ originalVolumes[b];
			// horizontal line___________________________
			se = new BooleanImage(side, side,1,1,1);
			se.setCenter( new Point(i, i));
			se.setPixelXYBoolean(i, 0, true);
			se.setPixelXYBoolean(i, side - 1, true);
			// Opening
			tmp = GrayOpening.exec(input, se);
			// place it...carefully..
			for (int b = 0; b < input.getBDim(); b++)
				curve[b * 4 * length + 2 * length + i - 1] = moment(tmp, b,
						momentX, momentY)
						/ originalVolumes[b];
			// right diagonal line_______________________
			se = new BooleanImage(side, side,1,1,1);
			se.setCenter( new Point(i, i));
			se.setPixelXYBoolean(side - 1, 0, true);
			se.setPixelXYBoolean(0, side - 1, true);
			// Opening
			tmp = GrayOpening.exec(input, se);
			// place it...carefully..
			for (int b = 0; b < input.getBDim(); b++)
				curve[b * 4 * length + 3 * length + i - 1] = moment(tmp, b,
						momentX, momentY)
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
