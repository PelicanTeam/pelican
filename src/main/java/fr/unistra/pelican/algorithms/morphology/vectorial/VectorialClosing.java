package fr.unistra.pelican.algorithms.morphology.vectorial;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.util.vectorial.orders.VectorialOrdering;

/**
 * This class performs a vectorial closing (dilation then erosion) with a given structuring
 * element and ordering. Works on double precision.
 * 
 * @author Abdullah
 * 
 */
public class VectorialClosing extends Algorithm
{
	/**
	 * the input image
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
	 * This method performs a vectorial closing with the given structuring element and ordering
	 * @param inputImage the input image
	 * @param se the structuring element
	 * @param vo the vector ordering
	 * @return the output image 
	 */
	public static Image exec(Image inputImage,BooleanImage se,VectorialOrdering vo) {
		return (Image) new VectorialClosing().process(inputImage,se,vo);
	}

	/**
	 * Constructor
	 * 
	 */
	public VectorialClosing() {

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
			outputImage = (Image) new VectorialDilation().process(inputImage,se,vo);
			outputImage = (Image) new VectorialErosion().process(outputImage,se,vo);

		} catch (PelicanException e) {
			e.printStackTrace();
		}
	}

}
