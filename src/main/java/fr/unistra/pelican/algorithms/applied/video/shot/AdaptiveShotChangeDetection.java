package fr.unistra.pelican.algorithms.applied.video.shot;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;

/**
 * This class perform shot change detection using HSV block-based interframe
 * differences in video sequences
 * 
 * S. Lefèvre, N. Vincent, Efficient and Robust Shot Change Detection, Journal
 * of Real Time Image Processing, Springer, Vol. 2, No. 1, october 2007, pages
 * 23-34, doi:10.1007/s11554-007-0033-1.
 * 
 * @author Sébatien Lefèvre
 */
public class AdaptiveShotChangeDetection extends Algorithm {

	/**
	 * The input image sequence
	 */
	public Image input;

	/**
	 * The threshold value used to locate shot change
	 */
	public double threshold;

	/**
	 * The temporal inertia given to the difference measures
	 */
	public double inertia = 0.75;

	/**
	 * The output result containing shot change positions
	 */
	public Integer[] output;

	/**
	 * Default constructor
	 */
	public AdaptiveShotChangeDetection() {
		super.inputs = "input";
		super.options = "threshold,inertia";
		super.outputs = "output";
		
	}

	/**
	 * Performs shot change detection using HSV block-based interframe
	 * differences in video sequences
	 * 
	 * @param input
	 *            The input image sequence
	 * @return The output result containing shot change positions
	 */
	public static Integer[] exec(Image input) {
		return (Integer[]) new AdaptiveShotChangeDetection().process(input);
	}

	/**
	 * Performs shot change detection using HSV block-based interframe
	 * differences in video sequences
	 * 
	 * @param input
	 *            The input image sequence
	 * @param threshold
	 *            The threshold value used to locate shot change
	 * @param inertia
	 *            The temporal inertia given to the difference measures
	 * @return The output result containing shot change positions
	 */
	public static Integer[] exec(Image input, double threshold, double inertia) {
		return (Integer[]) new AdaptiveShotChangeDetection().process(input,
				threshold, inertia);
	}

	public static Integer[] exec(Image input, double threshold) {
		return (Integer[]) new AdaptiveShotChangeDetection().process(input,
				threshold);
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
		Double measures[] = new Double[duration];
		Double thresholds[] = new Double[duration];
		Double derivative[] = new Double[duration];
		Double integral[] = new Double[duration];

		// Compute HSV interframe difference
		measures = HSVBasedInterframeDifference.exec(input);
		// Compute threshold values
		thresholds[0] = measures[0];
		for (int t = 1; t < duration; t++)
			thresholds[t] = inertia * thresholds[t - 1] + (1 - inertia)
					* measures[t - 1];
		// Compute derivative values
		for (int t = 1; t < duration - 1; t++)
			derivative[t - 1] = Math.abs(measures[t] - measures[t - 1]);
		derivative[duration - 2] = 0.0;
		derivative[duration - 1] = 0.0;
		// Computer integral values
		double sum = 0;
		boolean integrate = false;
		for (int t = 0; t < duration; t++) {
			if (measures[t] > thresholds[t])
				integrate = true;
			else
				integrate = false;
			if (integrate)
				sum += derivative[t];
			else
				sum = 0;
			integral[t] = sum;
		}
		// Analyse the computed values
		java.util.Arrays.fill(tmp, 0);
		// Detect progressive transitions
		for (int t = 0; t < duration; t++)
			if (integral[t] > threshold) {
				int t2 = t;
				for (t2 = t; t2 >= 0 && integral[t2] > 0; t2--)
					tmp[t2] = 1;
				for (t2 = t; t2 < duration && integral[t2] > 0; t2++)
					tmp[t2] = 1;
				t = t2;
			}
		// Detect abrupt transitions
		for (int t = 0; t < duration; t++)
			if (derivative[t] > threshold) {
				int t2 = t;
				if (tmp[t] == 1 && tmp[t - 1] == 1)
					for (t2 = t - 1; t2 >= 0 && tmp[t2] == 1; t2--)
						tmp[t2] = 0;
				if (tmp[t] == 1 && tmp[t + 1] == 1)
					for (t2 = t + 1; t2 < duration && tmp[t2] == 1; t2++)
						tmp[t2] = 0;
				tmp[t] = 2;
			}
		output = tmp;
		/*
		 * if (DEBUG) for (int t = 0; t < duration; t++) System.out.println(t +
		 * "\t" + measures[t] + "\t" + thresholds[t] + "\t" + derivative[t] +
		 * "\t" + integral[t] + "\t" + output[t]);
		 */
	}

}
