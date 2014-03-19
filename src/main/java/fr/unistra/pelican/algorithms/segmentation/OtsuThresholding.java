package fr.unistra.pelican.algorithms.segmentation;

import java.util.Arrays;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.histogram.Histogram;

/**
 * Performs an Otsu threshold...(minimizes the interclass variance)
 * 
 * 22/10/2006
 * 
 */
public class OtsuThresholding extends Algorithm {
	public Image input;

	public BooleanImage output;
	public Integer threshold;

	/**
	 * Constructor
	 * 
	 */
	public OtsuThresholding() {
		super.inputs = "input";
		super.outputs = "output,threshold";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		output = new BooleanImage(input, false);

		double[] variances = new double[254]; // i. nin esik olmasi icin +1
		Arrays.fill(variances, 0.0);

		double[] histo = Histogram.exec(input, false);

		// for every threshold compute the interclass variances
		for (int i = 1; i < 255; i++) {

			// number of pixels in each class
			double n1 = 0.0;
			double n2 = 0.0;

			// mean of each class
			double mean1 = 0.0;
			double mean2 = 0.0;

			for (int j = 0; j < i; j++) {
				n1 += histo[j];
				mean1 += histo[j] * j;
			}

			for (int j = i; j < 256; j++) {
				n2 += histo[j];
				mean2 += histo[j] * j;
			}

			if (n1 == 0 || n2 == 0)
				continue;

			mean1 = mean1 / n1;
			mean2 = mean2 / n2;

			// inner class variance..the official and the simplified versions
			// variances[i - 1] = n1 * (mean1 - mean) * (mean1 - mean) + n2 *
			// (mean2 - mean) * (mean2 - mean);
			variances[i - 1] = n1 * n2 * (mean1 - mean2) * (mean1 - mean2);
		}

		// get the maximum
		int max = 0;
		for (int i = 1; i < variances.length; i++) {
			if (variances[i] > variances[max])
				max = i;
		}

		threshold = new Integer(max+1);
		output = (BooleanImage) new ManualThresholding().process(input, max + 1);
		System.err.println("Otsu threshold : " + (max + 1));
	}

	public static BooleanImage exec(Image i) {
		return (BooleanImage) new OtsuThresholding().process(i);
	}
	
}
