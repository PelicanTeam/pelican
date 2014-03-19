package fr.unistra.pelican.algorithms.applied.video.shot;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.conversion.RGBToGray;

/**
 * This class computes pixel-based interframe differences in image sequences for
 * shot change detection
 * 
 * S. LefÃ©vre, N. Vincent, Efficient and Robust Shot Change Detection, Journal
 * of Real Time Image Processing, Springer, Vol. 2, No. 1, october 2007, pages
 * 23-34, doi:10.1007/s11554-007-0033-1.
 * 
 * @author SÃ©batien LefÃ©vre
 */
public class PixelBasedInterframeDifference extends Algorithm {

	/**
	 * The input image sequence
	 */
	public Image input;

	/**
	 * The array of difference values
	 */
	public Double[] output;

	/**
	 * Default constructor
	 */
	public PixelBasedInterframeDifference() {
		super.inputs = "input";
		super.outputs = "output";
		
	}

	/**
	 * Computes pixel-based interframe differences in image sequences for shot
	 * change detection
	 * 
	 * @param input
	 *            The input image sequence
	 * @return The array of difference values
	 */
	public static Double[] exec(Image input) {
		return (Double[]) new PixelBasedInterframeDifference().process(input);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() {
		// check if the image is a video sequence
		if (input.tdim < 2)
			throw new AlgorithmException(
					"The input image is not a video sequence");
		int duration = input.getTDim();
		ByteImage img1;
		ByteImage img2;
		output = new Double[duration];
		// Sequence scanning
		for (int t = 1; t < duration; t++) {
			img1 = (ByteImage) input.getImage4D(t - 1, Image.T);
			img2 = (ByteImage) input.getImage4D(t, Image.T);
			if (input.isColor()) {
				img1 = (ByteImage) new RGBToGray().process(img1);
				img2 = (ByteImage) new RGBToGray().process(img2);
			}
			output[t - 1] = img1.differenceRatio(img2);
		}
		output[duration - 1] = 0.0;
	}

}
