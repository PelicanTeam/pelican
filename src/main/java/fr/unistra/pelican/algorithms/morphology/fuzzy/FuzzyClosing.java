package fr.unistra.pelican.algorithms.morphology.fuzzy;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.morphology.GrayStructuringElement;
import fr.unistra.pelican.util.morphology.complements.FuzzyComplement;
import fr.unistra.pelican.util.morphology.fuzzyNorms.FuzzyNorm;
import fr.unistra.pelican.util.morphology.fuzzyNorms.FuzzyTCoNorm;
import fr.unistra.pelican.util.morphology.fuzzyNorms.FuzzyTNorm;

/**
 * Fuzzy closing as defined by Bloch and Maitre
 * Works with double image with values in [0;1]
 * @author Benjamin Perret
 */

public class FuzzyClosing extends Algorithm {
	/**
	 * Input image
	 */
	public Image inputImage;

	/**
	 * Structuring function
	 */
	public GrayStructuringElement se;

	/**
	 * Fuzzy norm
	 */
	public FuzzyNorm n;
	
	private FuzzyTNorm t;

	private FuzzyTCoNorm s;

	/**
	 * Complementing function
	 */
	public FuzzyComplement c;

	/**
	 * Result of closing
	 */
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public FuzzyClosing() {

		super();
		super.inputs = "inputImage,se,n,c";
		super.outputs = "outputImage";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		s=n;
		t=n;
		outputImage = (Image) new FuzzyDilation().process(inputImage, se, t);
		outputImage = (Image) new FuzzyErosion().process(outputImage, se, s, c);

	}
/*
	public static void main(String[] args) {
		GrayStructuringElement se = new GrayStructuringElement(7, 7, new Point(
				3, 3));
		double vals[] = GaussianMask.createLinearMask(11, 0.05);
		double vals2[] = { 0.0, 1.0, 0.0, 1.0, 1.0, 1.0, 0.0, 1.0, 0.0 };
		se.setPixels(vals);

		FuzzyStandardNorm t = new FuzzyStandardNorm();
		FuzzyComplement c = new FuzzyStandardComplement();
		Image im = (Image) new ImageLoader().process("samples/lennaGray256.png");
		new Viewer2D().process(im, "op");
		im = (Image) new FuzzyClosing().process(im, se, t, c);

		new Viewer2D().process(im, "la");
		new ImageSave().process(im, "res.jpg");

	}*/

	/**
	 * Fuzzy closing as defined by Bloch and Maitre
	 * Works with double image with values in [0;1]
	 * @param inputImage Input image
	 * @param se Structuring function
	 * @param n Fuzzy Norm
	 * @param c Complement function
	 * @return Closed image
	 */
	public static Image exec(Image inputImage, GrayStructuringElement se, FuzzyNorm n, FuzzyComplement c)
	{
		return (Image) new FuzzyClosing().process(inputImage,se,n,c);
	}
}
