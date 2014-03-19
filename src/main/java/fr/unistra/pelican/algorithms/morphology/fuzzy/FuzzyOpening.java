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
 * Fuzzy opening as defined by Bloch and Maitre
 * Works with double image with values in [0;1]
 * @author Benjamin Perret
 */
public class FuzzyOpening extends Algorithm {

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
	 * Result of opening
	 */
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public FuzzyOpening() {

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
		t=n;
		s=n;
		outputImage = (Image) new FuzzyErosion().process(inputImage, se, s, c);
		outputImage = (Image) new FuzzyDilation().process(outputImage, se, t);

	}
/*
	public static void main(String[] args) {
		GrayStructuringElement se = new GrayStructuringElement(9, 9, new Point(
				4, 4));
		double vals[] = GaussianMask.createLinearMask(9, 1.0);
		double vals2[] = { 0.0, 1.0, 0.0, 1.0, 1.0, 1.0, 0.0, 1.0, 0.0 };
		se.setPixels(vals);

		FuzzyStandardNorm t = new FuzzyStandardNorm();
		FuzzyComplement c = new FuzzyStandardComplement();
		Image im = (Image) new ImageLoader().process("img1-12.fits");
		new Viewer2D().process(im, "op");
		im = (Image) new FuzzyOpening().process(im, se, t, t, c);

		new Viewer2D().process(im, "la");
		new ImageSave().process(im, "res.jpg");

	}*/
	
	/**
	 * Fuzzy opening as defined by Bloch and Maitre
	 * Works with double image with values in [0;1]
	 * @param image Input image
	 * @param se Structuring function
	 * @param n Fuzzy Norm
	 * @param c Complement function
	 * @return Closed image
	 */
	public static Image exec(Image inputImage, GrayStructuringElement se, FuzzyNorm n, FuzzyComplement c)
	{
		return (Image) new FuzzyOpening().process(inputImage,se,n,c);
	}

}
