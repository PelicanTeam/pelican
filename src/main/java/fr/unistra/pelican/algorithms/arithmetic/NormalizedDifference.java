package fr.unistra.pelican.algorithms.arithmetic;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.InvalidParameterException;
import fr.unistra.pelican.util.mask.MaskStack;

/**
 * Compute a normalized difference (image1-image2)/(image1+image2).
 * The domain of results [-1;1] is normalized to [0;1].
 * 
 * outputPixel = ( ( inputPixel1 - inputPixel2 ) / ( inputPixel1 + inputPixel2 ) + 1 ) / 2
 * 
 * Works on double precision.
 * Result is of same type as first input.
 * 
 * @author Jonathan Weber, Benjamin Perret
 *
 */
public class NormalizedDifference extends Algorithm {

	/**
	 * First input image
	 */
	public Image image1;

	/**
	 * Second input image
	 */
	public Image image2;

	/**
	 * Result
	 */
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public NormalizedDifference() {

		super();
		super.inputs = "image1,image2";
		super.outputs = "outputImage";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {

		if(Image.haveSameDimensions(image1, image2))
		{
			outputImage = image1.copyImage(false);
			MaskStack mask = new MaskStack( MaskStack.OR );
			mask.push( image1.getMask() );
			mask.push( image2.getMask() );
			outputImage.setMask( mask );
			boolean isHere1, isHere2;
			for ( int b = 0 ; b < image1.getBDim() ; b++ ) 
			for ( int t = 0 ; t < image1.getTDim() ; t++ ) 
			for ( int z = 0 ; z < image1.getZDim() ; z++ ) 
			for ( int y = 0 ; y < image1.getYDim() ; y++ ) 
			for ( int x = 0 ; x < image1.getXDim() ; x++ ) { 

				isHere1 = image1.isPresent( x,y,z,t,b );
				isHere2 = image2.isPresent( x,y,z,t,b );
				double pixel1 = image1.getPixelDouble( x,y,z,t,b );
				double pixel2 = image2.getPixelDouble( x,y,z,t,b );
				double ND = 0.;
				if ( isHere1 && isHere2 ) 
					ND = (((	pixel1 - pixel2 ) / ( pixel1+pixel2 )) +1. ) /2.;
				else if ( isHere1 && !isHere2 ) ND = pixel1;
				else if ( !isHere1 && isHere2 ) ND = pixel2;
				//For undefined reasons sometimes values are > 1 or < 0 so we checked them.
				if( ND > 1 ) ND = 1;
				else if( ND < 0 ) ND = 0;
				this.outputImage.setPixelDouble( x,y,z,t,b, ND );
			}
		}
		else throw (new InvalidParameterException("The images must have the same dimensions"));
	}

	/*
	 * Static fonction that use this algorithm.
	 * 
	 * @param image
	 * @param number
	 *            of band 1
	 * @param number
	 *            of band 2
	 * @return result
	 * @throws InvalidTypeOfParameterException
	 * @throws AlgorithmException
	 * @throws InvalidNumberOfParametersException
	 
	public Image process(Image image, Number Band1, Number Band2)
			throws InvalidTypeOfParameterException, AlgorithmException,
			InvalidNumberOfParametersException {
		return (Image) new NormalizedDifference().process(image.getImage4D(
				((Number) Band1).intValue(), Image.B), image.getImage4D(
				((Number) Band2).intValue(), Image.B));
	}
*/
	/**
	 * Compute a normalized difference (image1-image2)/(image1+image2).
	 * The domain of results [-1;1] is normalized to [0;1].
	 * 
	 * outputPixel = ( ( inputPixel1 - inputPixel2 ) / ( inputPixel1 + inputPixel2 ) + 1 ) / 2
	 * 
	 * Works on double precision.
	 * Result is of same type as first input.
	 * 
	 * @param image1 First Input Image
	 * @param image2 Second Input Image
	 * @return Normalized Difference of inputs
	 */
	public static Image exec(Image image1, Image image2) {
		return (Image)new NormalizedDifference().process(image1, image2);
	}
}
