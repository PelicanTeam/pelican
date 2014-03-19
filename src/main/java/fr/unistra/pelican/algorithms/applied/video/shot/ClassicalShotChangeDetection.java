package fr.unistra.pelican.algorithms.applied.video.shot;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;

/**
 * This class perform shot change detection using classical interframe
 * differences in video sequences
 * 
 * S. Lefèvre, N. Vincent, Efficient and Robust Shot Change Detection, Journal
 * of Real Time Image Processing, Springer, Vol. 2, No. 1, october 2007, pages
 * 23-34, doi:10.1007/s11554-007-0033-1.
 * 
 * @author Sébatien Lefèvre
 */
public class ClassicalShotChangeDetection extends Algorithm {

	/**
	 * Constant to represent the pixel-based measure
	 */
	public final static int PIXEL = 1;

	/**
	 * Constant to represent the histogram-based measure
	 */
	public final static int HISTOGRAM = 2;

	/**
	 * The input image
	 */
	public Image input;

	/**
	 * The method to be used : either PIXEL or HISTOGRAM
	 */
	public int method;

	/**
	 * The threshold value used to locate shot change
	 */
	public double threshold;

	/**
	 * The output result containing shot change positions
	 */
	public Integer[] output;

	/**
	 * Constructor
	 */
	public ClassicalShotChangeDetection() {
		super.inputs = "input";
		super.options = "method,threshold";
		super.outputs = "output";
		
	}

	/**
	 * Performs shot change detection using classical interframe
	 * differences in video sequences
	 * 
	 * @param input
	 *            The input image sequence
	 * @return The output result containing shot change positions
	 */
	public static Integer[] exec(Image input) {
		return (Integer[]) new ClassicalShotChangeDetection().process(input);
	}

	/**
	 * Performs shot change detection using classical interframe
	 * differences in video sequences
	 * 
	 * @param input
	 *            The input image sequence
	 * @param method
	 *            The method to be used : either PIXEL or HISTOGRAM
	 * @param threshold
	 *            The threshold value used to locate shot change
	 * @return The output result containing shot change positions
	 */
	public static Integer[] exec(Image input, int method, double threshold) {
		return (Integer[]) new ClassicalShotChangeDetection().process(input,
				method, threshold);
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
		Integer tmp[] = new Integer[duration];
		Double measures[] = null;
		// compute measures
		if (method == PIXEL)
			measures = (Double[]) new PixelBasedInterframeDifference()
					.process(input);
		else if (method == HISTOGRAM)
			measures = (Double[]) new HistogramBasedInterframeDifference()
					.process(input);

		java.util.Arrays.fill(tmp, 0);
		for (int t = 0; t < duration - 1; t++)
			if (measures[t] > threshold) {
				int t2 = t;
				for (t2 = t - 1; t2 >= 0 && measures[t2] > threshold; t2--)
					tmp[t2] = 1;
				for (t2 = t + 1; t2 < duration - 1 && measures[t2] > threshold; t2++)
					tmp[t2] = 1;
				if (((t > 0) && (tmp[t - 1] == 100.0))
						|| ((t < duration - 2) && (tmp[t + 1] == 100.0)))
					tmp[t] = 1;
				else
					tmp[t] = 2;
				t = t2;
			}
		output = tmp;
	}

}
