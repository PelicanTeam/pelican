package fr.unistra.pelican.algorithms.morphology.vectorial;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.arithmetic.Difference;
import fr.unistra.pelican.util.vectorial.orders.VectorialOrdering;

/**
 * This class performs a vectorial open top hat with a given structuring element. Works on
 * double precision.
 * 
 * @author Abdullah
 * 
 */
public class VectorialOpenTopHat extends Algorithm
{
	/**
	 * The input image
	 */
	public Image inputImage;

	/**
	 * the structuring element
	 */
	public BooleanImage se;

	/**
	 * the vector ordering
	 */
	public VectorialOrdering vo;

	/**
	 * the output image
	 */
	public Image outputImage;
	
	
	/**
	 * This class performs a vectorial open top hat with a given structuring element. Works on double precision.
	 * @param inputImage the input image
	 * @param se the structuring element
	 * @param vo the vectorial ordering
	 * @return the output image
	 */
	public static Image exec(Image inputImage, BooleanImage se,VectorialOrdering vo) {
		return (Image) new VectorialOpenTopHat().process(inputImage,se,vo);
	}

	/**
	 * Constructor
	 * 
	 */
	public VectorialOpenTopHat() {

		super();
		super.inputs = "inputImage,se,vo";
		super.outputs = "outputImage";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = inputImage.copyImage(false);

		try {
			/*
			 * Remove [0;1] constraint in Difference algorithm, i
			 * guess this algorithm cannot produce values out of
			 * range [0;1] if input image is in this range. 
			 * If input image values are not in range [0;1] there is
			 * no reason to constrain result to this range. 
			 *  B. Perret
			 */
			outputImage = (Image) new Difference().process(inputImage, new VectorialOpening()
					.process(inputImage, se, vo),false);
		} catch (PelicanException e) {
			e.printStackTrace();
		}
	}

}
