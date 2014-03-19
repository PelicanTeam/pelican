package fr.unistra.pelican.algorithms.spatial;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.arithmetic.Equal;
import fr.unistra.pelican.algorithms.conversion.AverageChannels;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.io.ImageSave;
import fr.unistra.pelican.algorithms.morphology.gray.GrayDilation;
import fr.unistra.pelican.algorithms.morphology.gray.GrayErosion;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 * The Kramer-Bruckner filter
 * 
 * @author Lefevre
 */
public class KramerBrucknerFilter extends Algorithm {

	/**
	 * Input image
	 */
	public Image input;

	/**
	 * Size of the filter
	 */
	public int size;

	/**
	 * Output image
	 */
	public Image output;

	/**
	 * Number of iteration, -1 for iteration until convergence
	 */
	public int iterations = 1;

	/**
	 * Structuring element
	 */
	private BooleanImage kernel;

	/**
	 * Constructor
	 * 
	 */
	public KramerBrucknerFilter() {
		super.inputs = "input,size";
		super.options = "iterations";
		super.outputs = "output";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		output = input.copyImage(true);
		kernel = FlatStructuringElement2D
				.createSquareFlatStructuringElement(size);
		boolean convergence = false;
		if (iterations == -1)
			convergence = true;
		for (int i = 0; i < iterations || convergence; i++) {
			Image tmp = output.copyImage(true);
			Image min = GrayErosion.exec(output, kernel);
			Image max = GrayDilation.exec(output, kernel);
			for (int p = 0; p < output.size(); p++) {
				if (output.getPixelDouble(p) - min.getPixelDouble(p) < max
						.getPixelDouble(p)
						- output.getPixelDouble(p))
					output.setPixelDouble(p, min.getPixelDouble(p));
				else
					output.setPixelDouble(p, max.getPixelDouble(p));
			}
			if (convergence) {
				System.err.print(".");
				convergence = !Equal.exec(output, tmp);
			}
		}
	}

	/**
	 * The Kramer-Bruckner filter : each pixel is replaced by the closest
	 * between local minimum and maximum
	 * 
	 * @param input
	 *            Input image
	 * @param size
	 *            Size of the filter
	 * @return Output image
	 */
	public static Image exec(Image input, int size) {
		return (Image) new KramerBrucknerFilter().process(input, size);
	}

	public static Image exec(Image input, int size, int iterations) {
		return (Image) new KramerBrucknerFilter().process(input, size,
				iterations);
	}

	public static void main(String args[]) {
		String respath="/home/lefevre/kb";
		Image image = ImageLoader.exec("/home/lefevre/kb0.png");
		int size=3;
		ImageSave.exec(KramerBrucknerFilter.exec(image, size, 0), respath+"0.png");
		ImageSave.exec(KramerBrucknerFilter.exec(image, size, 1), respath+"1.png");
		ImageSave.exec(KramerBrucknerFilter.exec(image, size, 2), respath+"2.png");
		ImageSave.exec(KramerBrucknerFilter.exec(image, size, 5), respath+"5.png");
		ImageSave.exec(KramerBrucknerFilter.exec(image, size, 10), respath+"10.png");
		ImageSave.exec(KramerBrucknerFilter.exec(image, size, 20), respath+"20.png");
		ImageSave.exec(KramerBrucknerFilter.exec(image, size, 50), respath+"50.png");
		ImageSave.exec(KramerBrucknerFilter.exec(image, size, -1), respath+"inf.png");
	}


}
