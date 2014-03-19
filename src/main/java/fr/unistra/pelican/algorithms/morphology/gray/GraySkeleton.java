package fr.unistra.pelican.algorithms.morphology.gray;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.arithmetic.Addition;
import fr.unistra.pelican.algorithms.arithmetic.Difference;

/**
 * This class computes the skeleton of a grayscale image using the iterative
 * algorithm of Lanteuejoul 1980
 * 
 * @author Erchan Aptoula
 */
public class GraySkeleton extends Algorithm {

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
	public GraySkeleton() {
		super.inputs = "inputImage,se";
		super.outputs = "outputImage";
		
	}

	/**
	 * Computes the skeleton of a grayscale image using the iterative algorithm
	 * of Lanteuejoul 1980
	 * 
	 * @param inputImage
	 *            The input image
	 * @param se
	 *            The flat structuring element used in the morphological
	 *            operation
	 * @return The output image
	 */
	public static Image exec(Image inputImage, BooleanImage se) {
		return (Image) new GraySkeleton().process(inputImage, se);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() {
		outputImage = inputImage.copyImage(false);
		int bDim = inputImage.getBDim();
		int[] erosionNbr = new int[bDim];
		// find the number of erosions needed in order to fully eliminate
		// the input
		for (int b = 0; b < bDim; b++) {
			erosionNbr[b] = 0;
			Image band = inputImage.getImage4D(b, Image.B);
			// Modification by SL to remove the call to ByteImage
			Image tmp1 = band;
			Image tmp2 = null;
			do {
				tmp2=tmp1;
				tmp1 = GrayErosion.exec(tmp1, se);
				erosionNbr[b]++;
			} while (tmp1.equals(tmp2) == false);
			/*
			ByteImage tmp = (ByteImage) band;
			do {
				tmp = (ByteImage) GrayErosion.exec(tmp, se);
				erosionNbr[b]++;
			} while (tmp.isEmpty() == false);
			*/
			System.err.println(erosionNbr[b]);
			Image accumulator = band.copyImage(false);
			accumulator.fill(0.0);
			Image eroded = band;
			for (int i = 0; i < erosionNbr[b]; i++) {
				System.err.println(i);
				eroded = GrayErosion.exec(eroded, se);
				Image opened = GrayOpening.exec(eroded, se);
				Image diff = Difference.exec(eroded, opened);
				accumulator = Addition.exec(accumulator, diff);
			}
			outputImage.setImage4D(accumulator, b, Image.B);
		}

	}

}
