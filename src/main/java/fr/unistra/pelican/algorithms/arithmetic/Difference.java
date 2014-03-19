package fr.unistra.pelican.algorithms.arithmetic;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.mask.MaskStack;

/**
 * Compute the difference beetwen two image, inputImage1 - inputImage2. The
 * outputImage format is the same as inputImage1.
 * 
 * By Default: A check is done to keep pixels values in image type natural
 * bounds.
 * 
 * If x and y are images of type t and t takes value in [a,b], result z at pixel
 * p is then z(p) = min ( b , max ( a , x(p) - y(p) ) )
 * 
 * If option safe is set false no check is done and result is: z(p) = x(p) -
 * y(p)
 *
 *	Mask management (by witz) :
 *		- computation occurs as if masked pixels were at 0.
 *		- the the output image mask is inputImage1 mask ANDed with inputImage2 mask
 * 
 * @author ?, Benjamin Perret
 * 
 */
public class Difference extends Algorithm {

	/**
	 * First input image.
	 */
	public Image inputImage1;

	/**
	 * Second input image.
	 */
	public Image inputImage2;

	/**
	 * Option parametre
	 */
	public Boolean safe = true;

	/**
	 * Result
	 */
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public Difference() {
		super.inputs = "inputImage1,inputImage2";
		super.options = "safe";
		super.outputs = "outputImage";

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = inputImage1.copyImage(false);
		MaskStack mask = new MaskStack( MaskStack.AND );
		mask.push( inputImage1.getMask() );
		mask.push( inputImage2.getMask() );
		outputImage.setMask( mask );

		int size = inputImage1.size();
		boolean isHere1, isHere2;
		double val1, val2;
		for (int i = 0; i < size; ++i) { 

			isHere1 = inputImage1.isPresent(i);
			isHere2 = inputImage2.isPresent(i);
			if ( isHere1 ) val1 = inputImage1.getPixelDouble(i);
			else val1 = 0.0;
			if ( isHere2 ) val2 = inputImage2.getPixelDouble(i);
			else val2 = 0.0;
			double val = val1 - val2;
			if (Math.abs(val) < 0.0000001) val = 0;
			outputImage.setPixelDouble( i, safe ? Math.min( 1.0, Math.max(0.0,val)) 
												: val );
		}
	}

	/**
	 * Compute the difference beetwen two image, inputImage1 - inputImage2. The
	 * outputImage format is the same as inputImage1.
	 * 
	 * Is safe is set to true: A check is done to keep pixels values in image
	 * type natural bounds.
	 * 
	 * If x and y are images of type t and t takes value in [a,b], result z at
	 * pixel p is then z(p) = min ( b , max ( a , x(p) - y(p) ) )
	 * 
	 * If option safe is set false: no check is done and result is: z(p) = x(p) -
	 * y(p)
	 * 
	 * @author ?, Benjamin Perret
	 * 
	 * @param image
	 *            Input Image 1.
	 * @param image2
	 *            Input Image 2.
	 * @param safe
	 *            Do we check result?
	 * @return Difference between image 1 and 2.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Image> T exec(T inputImage1, Image inputImage2, boolean safe) {
		return (T) new Difference().process(inputImage1, inputImage2, safe);
	}

	/**
	 * Compute the difference beetwen two image, inputImage1 - inputImage2. The
	 * outputImage format is the same as inputImage1.
	 * 
	 * A check is done to keep pixels values in image type natural bounds.
	 * 
	 * If x and y are images of type t and t takes value in [a,b], result z at
	 * pixel p is then z(p) = min ( b , max ( a , x(p) - y(p) ) )
	 * 
	 * @param inputImage1
	 *            Input Image 1.
	 * 
	 * @param inputImage2
	 *            Input Image 2.
	 * 
	 * @return Difference between image 1 and 2.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Image> T exec(T inputImage1, Image inputImage2) {
		return (T) new Difference().process(inputImage1, inputImage2);
	}

}
