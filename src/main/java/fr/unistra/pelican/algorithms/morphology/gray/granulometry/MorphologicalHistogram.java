package fr.unistra.pelican.algorithms.morphology.gray.granulometry;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.morphology.gray.GrayClosing;
import fr.unistra.pelican.algorithms.morphology.gray.GrayOpening;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 * This class computes a morphological histogram, i.e. a 2-D feature combining
 * granulometry and histogram features
 * 
 * S. Lefèvre, Extending morphological signatures for visual pattern
 * recognition, IAPR International Workshop on Pattern Recognition in
 * Information Systems (PRIS), Madeira, June 2007
 * 
 * @author Lefevre
 */
public class MorphologicalHistogram extends Algorithm {

	/**
	 * The input image
	 */
	public Image input;

	/**
	 * The mode of the granulometric curve: OPEN, CLOSE, BOTH
	 */
	public int mode = 2;

	/**
	 * The length of the spatial feature (granulometry)
	 */
	public int lengthSpatial = 25;

	/**
	 * The length of the intensity feature (histogram)
	 */
	public int lengthIntensity = 256;

	/**
	 * The step of the spatial feature (granulometry)
	 */
	public int stepSpatial = 2;

	/**
	 * The step of the intensity feature (histogram)
	 */
	public int stepIntensity = 10;

	/**
	 * Flag to determine if a normalisation is applied on the data
	 */
	public boolean normalised = false;

	/**
	 * The output morphological histogram 2-D curve
	 */
	public double[][] curve;

	/**
	 * Constant representing the OPEN mode
	 */
	public static int OPEN = 0;

	/**
	 * Constant representing the CLOSE mode
	 */
	public static int CLOSE = 1;

	/**
	 * Constant representing the BOTH mode
	 */
	public static int BOTH = 2;

	/**
	 * Default constructor
	 */
	public MorphologicalHistogram() {
		super.inputs = "input";
		super.options = "mode,lengthSpatial,lengthIntensity,stepSpatial,stepIntensity,normalised";
		super.outputs = "curve";
		
	}

	/**
	 * Computes a morphological histogram, i.e. a 2-D feature combining
	 * granulometry and histogram features
	 * 
	 * @param input
	 *            The input image
	 * @return The output morphological histogram 2-D curve
	 */
	public static double[][] exec(Image input) {
		return (double[][]) new MorphologicalHistogram().process(input);
	}

	/**
	 * Computes a morphological histogram, i.e. a 2-D feature combining
	 * granulometry and histogram features
	 * 
	 * @param input
	 *            The input image
	 * @param mode
	 *            The mode of the granulometric curve: OPEN, CLOSE, BOTH
	 * @param lengthSpatial
	 *            The length of the spatial feature (granulometry)
	 * @param lengthIntensity
	 *            The length of the intensity feature (histogram)
	 * @param stepSpatial
	 *            The step of the spatial feature (granulometry)
	 * @param stepIntensity
	 *            The step of the intensity feature (histogram)
	 * @param normalised
	 *            Flag to determine if a normalisation is applied on the data
	 * @return The output morphological histogram 2-D curve
	 */
	public static double[][] exec(Image input, int mode, int lengthSpatial,
			int lengthIntensity, int stepSpatial, int stepIntensity,
			boolean normalised) {
		return (double[][]) new MorphologicalHistogram().process(input, mode,
				lengthSpatial, lengthIntensity, stepSpatial, stepIntensity,
				normalised);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() { 
		int singleSizeSpatial = (int) Math.ceil((double) lengthSpatial
				/ stepSpatial);
		int sizeSpatial = singleSizeSpatial;
		if (mode == 2)
			sizeSpatial = 2 * sizeSpatial - 1;
		int sizeIntensity = (int) Math.ceil((double) lengthIntensity
				/ stepIntensity);
		curve = new double[sizeSpatial][sizeIntensity];
		double total = input.size();
		Image img = null;
		// Opening only
		if (mode == OPEN) {
			histogram(input, 0);
			for (int i = 1; i < sizeSpatial; i++) {
				img = GrayOpening.exec( input, FlatStructuringElement2D
							.createSquareFlatStructuringElement( 2 * ( i*stepSpatial ) +1 ) );
				histogram(img, i);
			}
		}
		// Closing only
		else if (mode == CLOSE) {
			histogram(input, 0);
			for (int i = 1; i < sizeSpatial; i++) {
				img = GrayClosing.exec( input, FlatStructuringElement2D
							.createSquareFlatStructuringElement( 2 * ( i*stepSpatial ) +1 ) );
				histogram(img, i);
			}
		}
		// Both
		else {
			histogram(input, singleSizeSpatial - 1);
			for (int i = 1; i < singleSizeSpatial; i++) {
				img = GrayClosing.exec( input, FlatStructuringElement2D
							.createSquareFlatStructuringElement( 2 * ( i*stepSpatial ) +1 ) );
				histogram(img, singleSizeSpatial - 1 - i);
			}
			for (int i = 1; i < singleSizeSpatial; i++) {
				img = GrayClosing.exec( input, FlatStructuringElement2D
							.createSquareFlatStructuringElement( 2 * ( i*stepSpatial ) +1 ) );
				histogram(img, singleSizeSpatial - 1 + i);
			}
		}
		if (normalised)
			for (int i = 0; i < sizeSpatial; i++)
				for (int j = 0; j < sizeIntensity; j++)
					curve[i][j] /= total;

	}

	private void histogram(Image img, int i) {
		for (int j = 0; j < img.size(); j++)
			curve[i][img.getPixelByte(j) / stepIntensity]++;
	}

}
