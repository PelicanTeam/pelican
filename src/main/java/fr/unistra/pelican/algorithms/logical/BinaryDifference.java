package fr.unistra.pelican.algorithms.logical;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.mask.MaskStack;

/**
 *	Computes the difference beetwen two binary images
 *
 *	Mask management (by witz) :
 *		- computation with an unmasked pixel p1 and a masked pixel p2 results in value p1.
 *		- computation with a masked pixel p1 and an unmasked pixel p2 results in value !p2.
 *		- computation with two masked pixels results in value false.
 *		- the the output image mask is inputImage1 mask ORed with inputImage2 mask
 * 
 * @author Lefevre
 */
public class BinaryDifference extends Algorithm {
	
	/**
	 * First input image
	 */
	public Image inputImage1;

	/**
	 * Second input image
	 */
	public Image inputImage2;

	/**
	 * Output image
	 */
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public BinaryDifference() {

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

		this.outputImage = this.inputImage1.copyImage(false);

		MaskStack mask = new MaskStack( MaskStack.OR );
		mask.push( this.inputImage1.getMask() );
		mask.push( this.inputImage2.getMask() );
		this.outputImage.setMask( mask );

		int size = inputImage1.size();

		boolean tmp, isHere1,isHere2;
		for ( int i = 0 ; i < size; ++i ) { 

			tmp = false;
			isHere1 = this.inputImage1.isPresent(i);
			isHere2 = this.inputImage2.isPresent(i);
			if ( isHere1 && !isHere2 ) tmp = this.inputImage1.getPixelBoolean(i);
			else 
			if ( !isHere1 && isHere2 ) tmp = !this.inputImage2.getPixelBoolean(i);
			else 
			if ( isHere1 && isHere2 ) 
				tmp =  ( this.inputImage1.getPixelBoolean(i) == true )
					&& ( this.inputImage2.getPixelBoolean(i) == false );

			this.outputImage.setPixelBoolean(i, tmp);
		}
	}
	
	/**
	 * Computes the difference beetwen two binary images
	 * @param inputImage1  First input image
	 * @param inputImage2 Second input image
	 * @return Output image
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Image> T  exec(T inputImage1, T inputImage2) {
		return (T) new BinaryDifference().process(inputImage1,inputImage2);
	}

}
