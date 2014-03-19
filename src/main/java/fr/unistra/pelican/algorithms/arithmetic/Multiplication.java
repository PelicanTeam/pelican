package fr.unistra.pelican.algorithms.arithmetic;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.mask.MaskStack;

/**
 * Compute the multiplication of two images, inputImage1 * inputImage2.
 * 
 * The outputImage format is the same as inputImage1. 
 * 
 * No check is done on value.
 * 
 * @author  ?, Benjamin Perret
 */
public class Multiplication extends Algorithm {

	/**
	 * First input image
	 */
	public Image inputImage1;

	/**
	 * Second input image
	 */
	public Image inputImage2;

	/**
	 * Multiplication of inputs one and two
	 */
	public Image outputImage;
	/**
	 * Constructor
	 * 
	 */
	public Multiplication() {

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
		outputImage = inputImage1.copyImage(false);
		MaskStack mask = new MaskStack( MaskStack.OR );
		mask.push( inputImage1.getMask() );
		mask.push( inputImage2.getMask() );
		outputImage.setMask( mask );
		int size = inputImage1.size();

		boolean isHere1, isHere2;
		for ( int i = 0 ; i < size ; ++i ) {  

			isHere1 = inputImage1.isPresent(i);
			isHere2 = inputImage2.isPresent(i);
			double pixel1 = inputImage1.getPixelDouble(i);
			double pixel2 = inputImage2.getPixelDouble(i);
			if ( isHere1 && isHere2 ) outputImage.setPixelDouble(i, pixel1*pixel2 );
			else if ( isHere1 && !isHere2 ) outputImage.setPixelDouble( i, pixel1 );
			else if ( !isHere1 && isHere2 ) outputImage.setPixelDouble( i, pixel2 );
		}
	}
	
	/**
	 * Compute the multiplication of two images, inputImage1 * inputImage2.
	 * 
	 * The outputImage format is the same as inputImage1. 
	 * 
	 * No check is done on value.
	 * 
	 * @param inputImage1 First input image
	 * @param inputImage2 Second input image
	 * @return multiplication of inputs one and two
	 * 
	 */
	public static Image exec(Image inputImage1, Image inputImage2) {
		return (Image)new Multiplication().process(inputImage1, inputImage2);
	}
}
