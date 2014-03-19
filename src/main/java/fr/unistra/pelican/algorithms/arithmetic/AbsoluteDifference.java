package fr.unistra.pelican.algorithms.arithmetic;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.InvalidParameterException;
import fr.unistra.pelican.util.mask.MaskStack;

/**
 * Compute the absolute difference between two images
 * abs(inputImage1 - inputImage2).
 * The outputImage format is the same as inputImage1.
 *
 *	Mask management (by witz) :
 *		- computation occurs as if masked pixels were at 0.
 *		- the the output image mask is inputImage1 mask ANDed with inputImage2 mask
 * 
 * @author  ?, Benjamin Perret
 */
public class AbsoluteDifference extends Algorithm {
	
	/**
	 * First input image.
	 */
	public Image inputImage1;

	/**
	 * Second input image.
	 */
	public Image inputImage2;

	/**
	 * Algorithm result: absolute difference between input image one and two.
	 */
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public AbsoluteDifference() {

		super();
		super.inputs = "inputImage1,inputImage2";
		super.outputs = "outputImage";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		if(!Image.haveSameDimensions(inputImage1, inputImage2))
			throw(new InvalidParameterException("The two images must have the same dimensions"));
		outputImage = inputImage1.copyImage(false);

		MaskStack mask = new MaskStack( MaskStack.AND );
		mask.push( inputImage1.getMask() );
		mask.push( inputImage2.getMask() );
		outputImage.setMask( mask );

		int size = inputImage1.size();
		boolean isHere1, isHere2;
		double pixel1, pixel2;
		for ( int i = 0 ; i < size ; ++i ) {  

			isHere1 = inputImage1.isPresent(i);
			isHere2 = inputImage2.isPresent(i);
			if ( isHere1 ) pixel1 = inputImage1.getPixelDouble(i);
			else pixel1 = 0.0;
			if ( isHere2 ) pixel2 = inputImage2.getPixelDouble(i);
			else pixel2 = 0.0;
			outputImage.setPixelDouble( i, Math.abs( pixel1 - pixel2 ) );
		}
	}

	/**
	 * Compute the absolute difference between two images
	 * (inputImage1 and inputImage2) which is the outputImage.
	 * 
	 * @param inputImage1
	 *            First of the two subtracted images.
	 * @param inputImage2
	 *            Second of the two subtracted images.
	 * @return outputImage which is the absolute difference between two images.
	 */
	public static Image exec(Image inputImage1, Image inputImage2) {
		return (Image) new AbsoluteDifference().process(inputImage1,inputImage2);
	}
}
