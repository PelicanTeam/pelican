package fr.unistra.pelican.algorithms.arithmetic;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.mask.MaskStack;

/**
 * Compute the difference beetwen two image, inputImage1 - inputImage2. The
 * outputImage format is the same as inputImage1. Check is done on values
 * @deprecated Use class fr.unistra.pelican.algorithms.arithmetic.Difference instead
 * 
 * @author ?, Benjamin Perret
 */
public class DifferenceChecked extends Algorithm {

	/**
	 * Input image 1
	 */
	public Image inputImage1;

	/**
	 * Input image 2
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
	public DifferenceChecked() {
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
			outputImage.setPixelDouble(i, Math.max( 0.0, val ));
		}

	}
	/**
	 * Compute the difference beetwen two image, inputImage1 - inputImage2. The
	 * outputImage format is the same as inputImage1. Check is done on values
	 * @deprecated Use class fr.unistra.pelican.algorithms.arithmetic.Difference instead
	 * 
	 * @param inputImage1  Input Image 1
	 * @param inputImage2  Input Image 2
	 * @return Difference between  image 1 and 2.
	 */
	public static Image exec(Image inputImage1, Image inputImage2) {
		return (Image) new Difference().process(inputImage1,inputImage2);
	}
}
