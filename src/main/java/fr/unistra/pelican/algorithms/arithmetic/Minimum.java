package fr.unistra.pelican.algorithms.arithmetic;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.mask.MaskStack;

/**
 * Compute the infimum of two images, for each pixel max(inputImage1,
 * inputImage2). 
 * 
 * Works on double precision. 
 * The outputImage format is the same as inputImage1.
 * 
 * @author  ?, Benjamin Perret
 */
public class Minimum extends Algorithm {

	/**
	 * First input image
	 */
	public Image inputImage1;

	/**
	 * Second input image
	 */
	public Image inputImage2;

	/**
	 * Infimum of inputs one and two
	 */
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public Minimum() {

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
			throw new AlgorithmException("Minimum: input images must have same dimensions!");
		outputImage = inputImage1.copyImage(false);
		int size = inputImage1.size();
		MaskStack mask = new MaskStack( MaskStack.OR );
		mask.push( inputImage1.getMask() );
		mask.push( inputImage2.getMask() );
		outputImage.setMask( mask );

		boolean isHere1, isHere2;
		for ( int i = 0 ; i < size ; ++i ) {  

			isHere1 = inputImage1.isPresent(i);
			isHere2 = inputImage2.isPresent(i);
			double pixel1 = inputImage1.getPixelDouble(i);
			double pixel2 = inputImage2.getPixelDouble(i);
			if ( isHere1 && isHere2 ) 
				outputImage.setPixelDouble(i, pixel1 < pixel2 ? pixel1 : pixel2);
			else if ( isHere1 && !isHere2 ) outputImage.setPixelDouble( i, pixel1 );
			else if ( !isHere1 && isHere2 ) outputImage.setPixelDouble( i, pixel2 );
		}

	}
	
	/**
	 * Compute the infimum of two images, for each pixel max(inputImage1,
	 * inputImage2). 
	 * 
	 * Works on double precision. 
	 * The outputImage format is the same as inputImage1.
	 * 
	 * @param inputImage1 First input image
	 * @param inputImage2 Second input image
	 * @return infimum of inputs one and two
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Image> T exec(T inputImage1, T inputImage2) {
		return (T)new Minimum().process(inputImage1, inputImage2);
	}
}
