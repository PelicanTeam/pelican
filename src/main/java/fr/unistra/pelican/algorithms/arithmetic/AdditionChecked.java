package fr.unistra.pelican.algorithms.arithmetic;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.InvalidParameterException;
import fr.unistra.pelican.util.mask.MaskStack;


/** 
 * Compute the addition beetwen two image, inputImage1 + inputImage2.
 * The outputImage format is the same as inputImage1.
 * A check is done to keep pixels values in image type natural bounds.
 * 
 * If x and y are images of type t and t takes value in [a,b], 
 * result z at pixel p is then
 * z(p) = min ( b , max ( a , x(p) + y(p) ) )
 *
 *	Mask management (by witz) :
 *		- computation occurs as if masked pixels were at 0.
 *		- the the output image mask is inputImage1 mask ORed with inputImage2 mask
 * 
 * @author ?, Benjamin Perret
 */
public class AdditionChecked extends Algorithm{
	
	/**
	 * First input image.
	 */
	public Image inputImage1;
	
	/**
	 * Second input image.
	 */
	public Image inputImage2;
	
	/**
	 * Algorithm result: addition of image one and two.
	 */
	public Image outputImage;

	
	/**
  	 * Constructor
  	 *
  	 */
	public AdditionChecked() {		
		super.inputs = "inputImage1,inputImage2";		
		super.outputs = "outputImage";		
		
	}
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException{	
		if(!Image.haveSameDimensions(inputImage1, inputImage2))
			throw(new InvalidParameterException("The two images must have the same dimensions"));
		outputImage = inputImage1.copyImage(false);
		MaskStack mask = new MaskStack( MaskStack.OR );
		mask.push( inputImage1.getMask() );
		mask.push( inputImage2.getMask() );
		outputImage.setMask( mask );
		int size = inputImage1.size();
		
//		if(inputImage1 instanceof DoubleImage) {
			for(int i = 0; i < size; ++i)
				outputImage.setPixelDouble(i, 
						Math.max(0.0,Math.min(1.0, inputImage1.getPixelDouble(i) + inputImage2.getPixelDouble(i))));
//		}
//		// General path as int
//		else {
//			for(int i = 0; i < size; ++i)
//				outputImage.setPixelInt(i, 
//						inputImage1.getPixelInt(i) - inputImage2.getPixelInt(i));
//		}
	}
	
	/**
	 * Compute the addition beetwen two image, inputImage1 + inputImage2.
	 * The outputImage format is the same as inputImage1.
	 * A check is done to keep pixels values in image type natural bounds.
	 * 
	 * If x and y are images of type t and t takes value in [a,b], 
	 * result z at pixel p is then
	 * z(p)=min( b , max ( a , x(p) + y(p) ) )
	 * 
	 * @param inputImage1
	 *            First of the two additioned images.
	 * @param inputImage2
	 *            Second of the two additioned images.
	 * @return outputImage which is the addition between the two images.
	 */
	public static Image exec(Image inputImage1, Image inputImage2) {
		return (Image) new AdditionChecked().process(inputImage1,inputImage2);
	}
}
