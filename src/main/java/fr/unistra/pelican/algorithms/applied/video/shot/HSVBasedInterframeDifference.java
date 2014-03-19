package fr.unistra.pelican.algorithms.applied.video.shot;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.conversion.RGBToHSV;
import fr.unistra.pelican.algorithms.geometric.BlockResampling2D;

/**
 * This class computes HSV block-based interframe differences in image sequences
 * for shot change detection
 * 
 * S. Lefèvre, N. Vincent, Efficient and Robust Shot Change Detection, Journal
 * of Real Time Image Processing, Springer, Vol. 2, No. 1, october 2007, pages
 * 23-34, doi:10.1007/s11554-007-0033-1.
 * 
 * @author Sébatien Lefèvre
 */
public class HSVBasedInterframeDifference extends Algorithm {

	/**
	 * The input image sequence
	 */
	public Image input;

	/**
	 * The saturation threshold, hue values are not considered when saturation
	 * is below
	 */
	public double saturationThr = 0.25;

	/**
	 * The weight of hue versus saturation
	 */
	public double hueWeight = 0.5;

	/**
	 * The subsampling level used to analyse the image sequence
	 */
	public int subsampling = 8;

	/**
	 * The array of difference values
	 */
	public Double[] output;

	/**
	 * Default constructor
	 */
	public HSVBasedInterframeDifference() {
		super.inputs = "input";
		super.options = "saturationThr,hueWeight,subsampling";
		super.outputs = "output";
		
	}

	/**
	 * Computes HSV block-based interframe differences in image sequences for
	 * shot change detection
	 * 
	 * @param input
	 *            The input image sequence
	 * @return The array of difference values
	 */
	public static Double[] exec(Image input) {
		return (Double[]) new HSVBasedInterframeDifference().process(input);
	}

	/**
	 * Computes HSV block-based interframe differences in image sequences for
	 * shot change detection
	 * 
	 * @param input
	 *            The input image sequence
	 * @param saturationThr
	 *            The saturation threshold
	 * @param hueWeight
	 *            The weight of hue versus saturation
	 * @param subsampling
	 *            The subsampling level used to analyse the image sequence
	 * @return The array of difference values
	 */
	public static Double[] exec(Image input, double saturationThr,
			double hueWeight, int subsampling) {
		return (Double[]) new HSVBasedInterframeDifference().process(input,
				saturationThr, hueWeight, subsampling);
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
		output = new Double[duration];
		Image img1;
		Image img2;
		double alpha;
		double diff;
		double hue, sat;
		double sat1, sat2;
		double sum;
		// Sequence scanning
		for (int t = 1; t < duration; t++) {
			img1 = input.getImage4D(t - 1, Image.T);// getColorByteChannelZT(0,t-1);
			img2 = input.getImage4D(t, Image.T);// getColorByteChannelZT(0,t);
			if (subsampling > 1) {
				img1 = (Image) new BlockResampling2D().process(img1, subsampling,
						subsampling,false);
				img2 = (Image) new BlockResampling2D().process(img2, subsampling,
						subsampling,false);
			}
			if (input.isColor()) {
				img1 = (Image) new RGBToHSV().process(img1);
				img2 = (Image) new RGBToHSV().process(img2);
			}
			sum = 0;
			for (int x = 0; x < img1.getXDim(); x++)
				for (int y = 0; y < img1.getYDim(); y++) {
					// Compute the hue difference
					hue = (img1.getPixelXYBDouble(x, y, 2)
							- img2.getPixelXYBDouble(x, y, 2) + 1) % 1;
					if (hue > 0.5)
						hue = 1 - hue;
					hue *= 2;
					// Compute the sat difference
					sat1 = img1.getPixelXYBDouble(x, y, 1);
					sat2 = img2.getPixelXYBDouble(x, y, 1);
					sat = Math.abs(sat1 - sat2);
					// Test the sat level to compute weights
					if (sat1 > saturationThr && sat2 > saturationThr)
						alpha = hueWeight;
					else
						alpha = 0;
					// Difference between the two pixels
					diff = alpha * hue + (1 - alpha) * sat;
					sum += diff;
				}
			sum /= (img1.getXDim() * img1.getYDim());
			output[t - 1] = sum * 100;
		}
		output[duration - 1] = 0.0;
	}

}
