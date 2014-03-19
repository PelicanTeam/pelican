package fr.unistra.pelican.algorithms.logical;

import fr.unistra.pelican.*;

/**
 *	Computes a comparison beetwen an image and a constant value.
 *
 *	Mask management (by witz) :
 *		- computation of all pixels is done as usual
 *		- the input image mask becomes the output image mask
 * 
 *	@author Lefevre
 */

public class CompareConstant extends Algorithm {

	/**
	 * Input image
	 */
	public Image inputImage1;

	/**
	 * The value to compare
	 */
	public double input2;

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
	public BooleanImage outputImage;

	/**
	 * Constructor
	 * 
	 */
	public CompareConstant() {
		super.inputs = "inputImage1,input2,compOperator";
		super.outputs = "outputImage";

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {

		this.outputImage = new BooleanImage( this.inputImage1, false );
		this.outputImage.setMask( this.inputImage1.getMask() );
		boolean tmp = false;
		int size = this.inputImage1.size();

		if ( this.inputImage1 instanceof ByteImage) { 

			ByteImage image = (ByteImage)this.inputImage1;
			switch ( this.compOperator ) { 
			case SUP:
				for (int i = 0; i < size; ++i) { 

					tmp = (image.getPixelByte(i) > this.input2);
					this.outputImage.setPixelBoolean(i, tmp);
				}
				break;
			case GEQ:
				for (int i = 0; i < size; ++i) { 

					tmp = (image.getPixelByte(i) >= this.input2);
					this.outputImage.setPixelBoolean(i, tmp);
				}
				break;
			case EQ:
				for (int i = 0; i < size; ++i) { 

					tmp = (image.getPixelByte(i) == this.input2);
					this.outputImage.setPixelBoolean(i, tmp);
				}
				break;
			case NEQ:
				for (int i = 0; i < size; ++i) { 

					tmp = (image.getPixelByte(i) != this.input2);
					this.outputImage.setPixelBoolean(i, tmp);
				}
				break;
			case LEQ:
				for (int i = 0; i < size; ++i) { 

					tmp = (image.getPixelByte(i) <= this.input2);
					this.outputImage.setPixelBoolean(i, tmp);
				}
				break;
			case INF:
				for (int i = 0; i < size; ++i) { 

					tmp = (image.getPixelByte(i) < this.input2);
					this.outputImage.setPixelBoolean(i, tmp);
				}
				break;
			}
		}		
		else if (inputImage1 instanceof IntegerImage) {
			IntegerImage image=(IntegerImage)inputImage1;
			switch (compOperator) {
			case SUP:
				for (int i = 0; i < size; ++i) {
					tmp = (image.getPixelInt(i) > input2);
					outputImage.setPixelBoolean(i, tmp);
				}
				break;
			case GEQ:
				for (int i = 0; i < size; ++i) {
					tmp = (image.getPixelInt(i) >= input2);
					outputImage.setPixelBoolean(i, tmp);
				}
				break;
			case EQ:
				for (int i = 0; i < size; ++i) {
					tmp = (image.getPixelInt(i) == input2);
					outputImage.setPixelBoolean(i, tmp);
				}
				break;
			case NEQ:
				for (int i = 0; i < size; ++i) {
					tmp = (image.getPixelInt(i) != input2);
					outputImage.setPixelBoolean(i, tmp);
				}
				break;
			case LEQ:
				for (int i = 0; i < size; ++i) {
					tmp = (image.getPixelInt(i) <= input2);
					outputImage.setPixelBoolean(i, tmp);
				}
				break;
			case INF:
				for (int i = 0; i < size; ++i) {
					tmp = (image.getPixelInt(i) < input2);
					outputImage.setPixelBoolean(i, tmp);
				}
				break;
			}
		}
		else if (inputImage1 instanceof DoubleImage) {
			DoubleImage image=(DoubleImage)inputImage1;
						switch (compOperator) {
			case SUP:
				for (int i = 0; i < size; ++i) {
					tmp = (image.getPixelDouble(i) > input2);
					outputImage.setPixelBoolean(i, tmp);
				}
				break;
			case GEQ:
				for (int i = 0; i < size; ++i) {
					tmp = (image.getPixelDouble(i) >= input2);
					outputImage.setPixelBoolean(i, tmp);
				}
				break;
			case EQ:
				for (int i = 0; i < size; ++i) {
					tmp = (image.getPixelDouble(i) == input2);
					outputImage.setPixelBoolean(i, tmp);
				}
				break;
			case NEQ:
				for (int i = 0; i < size; ++i) {
					tmp = (image.getPixelDouble(i) != input2);
					outputImage.setPixelBoolean(i, tmp);
				}
				break;
			case LEQ:
				for (int i = 0; i < size; ++i) {
					tmp = (image.getPixelDouble(i) <= input2);
					outputImage.setPixelBoolean(i, tmp);
				}
				break;
			case INF:
				for (int i = 0; i < size; ++i) {
					tmp = (image.getPixelDouble(i) < input2);
					outputImage.setPixelBoolean(i, tmp);
				}
				break;
			}
		}
	}

	/**
	 * Computes a comparison beetwen an image and a constant value
	 * 
	 * @param inputImage1
	 *          Input image
	 * @param input2
	 *          The value to compare
	 * @param compOperator
	 *          The operator of comparison
	 * @return Ouput image
	 */
	public static BooleanImage exec(Image inputImage1, double input2, int compOperator) {
		return (BooleanImage) new CompareConstant().process(inputImage1, input2,
			compOperator);
	}
}
