package fr.unistra.pelican.algorithms.logical;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;

/**
 *	Computes a comparison beetwen two binary images
 *
 *	Mask management (by witz) :
 *		- pixels masked in inputImage2 are ignored for computation : 
 *			masked pixel value is the same in input and in output.
 *		- inputImage1 mask becomes the outputImage mask
 * 
 *	@author Lefevre
 */
public class CompareImage extends Algorithm {
	
	/**
	 * First input image
	 */
	public Image inputImage1;

	/**
	 * Second input image
	 */
	public Image inputImage2;

	/**
	 * The operator of comparison
	 */
	public int compOperator;

	/**
	 * if a > b
	 */
	public final static int SUP = 0;

	/**
	 * if a >= b
	 */
	public final static int GEQ = 1;

	/**
	 * if a == b
	 */
	public final static int EQ = 2;

	/**
	 * if a != b
	 */
	public final static int NEQ = 3; 

	/**
	 * if a <= b
	 */
	public final static int LEQ = 4;

	/**
	 * if a < b
	 */
	public final static int INF = 5;

	/**
	 * Ouput image
	 */
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public CompareImage() {

		super();
		super.inputs = "inputImage1,inputImage2,compOperator";
		super.outputs = "outputImage";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException { 

		outputImage = new BooleanImage(inputImage1, false);
		outputImage.setMask( inputImage1.getMask() );

		boolean tmp = false;
		int size = inputImage1.size();

		for (int i = 0; i < size; ++i) {
			switch (compOperator) {
			case SUP:
				if ( inputImage2.isPresent(i) ) 
					tmp = (inputImage1.getPixelDouble(i) > inputImage2.getPixelDouble(i));
				else tmp = inputImage1.getPixelBoolean(i);
				break;
			case GEQ:
				if ( inputImage2.isPresent(i) ) 
					tmp = (inputImage1.getPixelDouble(i) >= inputImage2.getPixelDouble(i));
				else tmp = inputImage1.getPixelBoolean(i);
				break;
			case EQ:
				if ( inputImage2.isPresent(i) ) 
					tmp = (inputImage1.getPixelDouble(i) == inputImage2.getPixelDouble(i));
				else tmp = inputImage1.getPixelBoolean(i);
				break;
			case NEQ:
				if ( inputImage2.isPresent(i) ) 
					tmp = (inputImage1.getPixelDouble(i) != inputImage2.getPixelDouble(i));
				else tmp = inputImage1.getPixelBoolean(i);
				break;
			case LEQ:
				if ( inputImage2.isPresent(i) ) 
					tmp = (inputImage1.getPixelDouble(i) <= inputImage2.getPixelDouble(i));
				else tmp = inputImage1.getPixelBoolean(i);
				break;
			case INF:
				if ( inputImage2.isPresent(i) ) 
					tmp = (inputImage1.getPixelDouble(i) < inputImage2.getPixelDouble(i));
				else tmp = inputImage1.getPixelBoolean(i);
				break;
			}
			outputImage.setPixelBoolean( i,tmp );
		}
	}
	
	/**
	 * Computes a comparison beetwen two binary images
	 * @param inputImage1 First input image
	 * @param inputImage2 Second input image
	 * @param compOperator The operator of comparison
	 * @return Output image
	 */
	public static Image exec(Image inputImage1, Image inputImage2, int compOperator) {
		return (Image) new CompareImage().process(inputImage1,inputImage2,compOperator);
	}
}
