package fr.unistra.pelican.algorithms.logical;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.util.mask.MaskStack;

/**
 *	Computes the logical AND of two images. The outputImage is of the lowest argument precision
 *
 *	Mask management (by witz) :
 *		- computation with an unmasked pixel p and a masked pixel p' results in value p.
 *		- computation with two masked pixels results in value false.
 *		- the the output image mask is inputImage1 mask ANDed with inputImage2 mask
 * 
 * @author Lefevre
 */
public class AND extends Algorithm {
	
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
	public AND() {

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

		int size = this.inputImage1.size();

		boolean isHere1,isHere2;
		if ( this.inputImage1 instanceof BooleanImage 
		  || this.inputImage2 instanceof BooleanImage ) { 

			this.outputImage = new BooleanImage( this.inputImage1, false );
			for ( int i = 0; i < size; ++i ) { 

				isHere1 = this.inputImage1.isPresent(i);
				isHere2 = this.inputImage2.isPresent(i);
				boolean tmp = false;
				if ( isHere1 && !isHere2 ) tmp = this.inputImage1.getPixelBoolean(i);
				else 
				if ( !isHere1 && isHere2 ) tmp = this.inputImage2.getPixelBoolean(i);
				else 
				if ( isHere1 && isHere2 ) 
					tmp = this.inputImage1.getPixelBoolean(i) 
						& this.inputImage2.getPixelBoolean(i);

				this.outputImage.setPixelBoolean( i,tmp );
			}
		} else if ( this.inputImage1 instanceof ByteImage
				||  this.inputImage2 instanceof ByteImage) { 

			this.outputImage = new ByteImage(inputImage1, false);

			for ( int i = 0; i < size; ++i ) { 

				isHere1 = this.inputImage1.isPresent(i);
				isHere2 = this.inputImage2.isPresent(i);
				int tmp = 0;
				if ( isHere1 && !isHere2 ) tmp = this.inputImage1.getPixelByte(i);
				else 
				if ( !isHere1 && isHere2 ) tmp = this.inputImage2.getPixelByte(i);
				else 
				if ( isHere1 && isHere2 ) 
					tmp = this.inputImage1.getPixelByte(i)
						& this.inputImage2.getPixelByte(i);
				this.outputImage.setPixelByte( i,tmp );
			}
		} else if ( this.inputImage1 instanceof IntegerImage
				 || this.inputImage2 instanceof IntegerImage) {
			this.outputImage = new IntegerImage( this.inputImage1, false) ;

			for (int i = 0; i < size; ++i) {

				isHere1 = this.inputImage1.isPresent(i);
				isHere2 = this.inputImage2.isPresent(i);
				int tmp = 0;
				if ( isHere1 && !isHere2 ) tmp = this.inputImage1.getPixelInt(i);
				else 
				if ( !isHere1 && isHere2 ) tmp = this.inputImage2.getPixelInt(i);
				else 
				if ( isHere1 && isHere2 ) 
					tmp = this.inputImage1.getPixelInt(i)
						& this.inputImage2.getPixelInt(i);
				this.outputImage.setPixelInt( i,tmp );
			}
		} else throw new AlgorithmException( "AND cannot be applied to floating point data" );

		MaskStack mask = new MaskStack( MaskStack.AND );
		mask.push( this.inputImage1.getMask() );
		mask.push( this.inputImage2.getMask() );
		this.outputImage.setMask( mask );
	}
	
	/**
	 * Computes the logical AND of two images. The outputImage is of the lowest argument precision
	 * @param inputImage1 First input image
	 * @param inputImage2 Second input image
	 * @return Output image
	 */
	public static Image exec(Image inputImage1, Image inputImage2) {
		return (Image) new AND().process(inputImage1,inputImage2);
	}
}
