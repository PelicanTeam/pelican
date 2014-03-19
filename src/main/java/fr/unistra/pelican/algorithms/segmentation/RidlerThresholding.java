package fr.unistra.pelican.algorithms.segmentation;


import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.histogram.Histogram;

/**
 * This class performs an image binarization relying on a recursive analysis of
 * the histogram. Since it has been elaborated to monoband images. quality of
 * the results on multiband images is not ensured. Works on all formats.
 * 
 * T.W. Ridler, S. Calvard, Picture thresholding using an iterative selection
 * method, IEEE Transactions on System, Man and Cybernetics, Volume 8, August
 * 1978, pages 629-632.
 * 
 * @author Lefevre
 */
public class RidlerThresholding extends Algorithm {

	/**
	 * The input image
	 */
	public Image inputImage;

	/**
	 * A flag to consider random initialization (default) or not
	 */
	public boolean random = true;

	/**
	 * The output image
	 */
	public BooleanImage outputImage;

	/**
	 * Constructor
	 */
	public RidlerThresholding() {
		super.inputs = "inputImage";
		super.options = "random";
		super.outputs = "outputImage";
	}

	/**
	 * Performs an image binarization relying on a recursive analysis of the
	 * histogram
	 * 
	 * @param image
	 *            The input image
	 * @param random
	 *            A flag to consider random initialization (default) or not
	 * @return The output image
	 */
	public static BooleanImage exec(Image image, boolean random) {
		return (BooleanImage) new RidlerThresholding().process(image, random);
	}

	public static BooleanImage exec(Image image) {
		return (BooleanImage) new RidlerThresholding().process(image);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		double hist[];
		try {
			hist = Histogram.exec(inputImage, true);
		} catch (PelicanException e) {
			throw new AlgorithmException(e.getMessage());
		}
		int val;
		if (random)
			val = (int) (255.0 * Math.random());
		else
			val = 128;
		int old = 512;
		double m1, m2;
		double s1, s2;
		while (Math.abs(val - old) > 10) {
			m1 = 0;
			m2 = 0;
			s1 = 0;
			s2 = 0;
			for (int i = 0; i < val; i++) {
				m1 += i * hist[i];
				s1 += hist[i];
			}
			for (int i = val; i < hist.length; i++) {
				m2 += i * hist[i];
				s2 += hist[i];
			}
			if (s1 != 0)
				m1 /= s1;
			if (s2 != 0)
				m2 /= s2;
			old = val;
			val = (int) (m1 + m2) / 2;
			System.out.println(val);
		}

		try {
			outputImage = (BooleanImage) new ManualThresholding().process(
					inputImage, val);
		} catch (PelicanException e) {
			throw new AlgorithmException(e.getMessage());
		}

	}

}
