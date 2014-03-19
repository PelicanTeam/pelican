package fr.unistra.pelican.algorithms.morphology.gray.geodesic;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.arithmetic.AdditionConstantChecked;
import fr.unistra.pelican.algorithms.arithmetic.Difference;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 * Dynamic opening (enhanced HMax contrast operator) using morphological
 * reconstruction algorithm, following the implementation given by Salembier and
 * Wilkinson (IEEE SPM 2009).
 * 
 * Original article: M. Grimaud, “A new measure of contrast: The dynamics,” in
 * Proc. SPIE Visual Communications and Image Processing’92, vol. SPIE 1769, S.
 * Gader and E. R. Dougherty, Eds. San Diego, CA: SPIE, July 1992, pp. 292–305.
 * 
 * @author Lefevre
 * 
 */
public class GrayDynamicOpening extends Algorithm {

	/**
	 * The input image
	 */
	public Image input;

	/**
	 * The contrast threshold
	 */
	public int h;

	/**
	 * The output image
	 */
	public Image output;

	/**
	 * Constructor
	 */
	public GrayDynamicOpening() {
		super.inputs = "input,h";
		super.outputs = "output";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		output = input.copyImage(false);
		Image diff = AdditionConstantChecked.exec(input, -h);
		Image recons = FastGrayReconstruction.exec(diff, input);
		Image maxima = GrayRegionalMaxima.exec(diff);
		for (int p = 0; p < output.size(); p++)
			if ((diff.getPixelByte(p) == recons.getPixelByte(p))
					&& (maxima.getPixelByte(p) != 0))
				output.setPixelByte(p, input.getPixelByte(p));
			else
				output.setPixelByte(p, 0);
		// Final reconstruction
		output = FastGrayReconstruction.exec(output, input);
	}

	/**
	 * Dynamic opening contrast operator using morphological reconstruction
	 * algorithm. *
	 * 
	 * @param input
	 *            The input image
	 * @param h
	 *            The contrast threshold
	 * @return The output image
	 */
	public static Image exec(Image input, int h) {
		return (Image) new GrayDynamicOpening().process(input, h);
	}

	 public static void main(String args[]) {
	 Image input = ImageLoader.exec("samples/blood1.png");
	 Viewer2D.exec(input);
	 BooleanImage se = FlatStructuringElement2D
	 .createCircleFlatStructuringElement(5);
	 Viewer2D.exec(GrayHMax.exec(input, 100));
	 Viewer2D.exec(GrayDynamicOpening.exec(input, 100));
	 }
		

}