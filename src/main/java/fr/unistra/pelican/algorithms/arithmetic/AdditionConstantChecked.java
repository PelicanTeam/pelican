package fr.unistra.pelican.algorithms.arithmetic;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;


/** 
 * Compute the addition beetwen an image and a constant.
 * The outputImage format is the same as inputImage1.
 * A check is done to keep pixels values in image type natural bounds.
 * 
 * If x is a image of type t, c a scalar, and t takes value in [a,b], 
 * result z at pixel p is then
 * z(p)=min( b , max ( a , x(p) + c ) )
 * 
 * @author Benjamin Perret, Jonathan Weber
 */
public class AdditionConstantChecked extends Algorithm{
	
	/**
	 * First input image.
	 */
	public Image inputImage1;
	
	/**
	 * Shift to apply to pixels values.
	 */
	public Number constant;
	
	/**
	 * The precision used (byte or double)
	 */
	public int mode=DOUBLEMODE;
	
	/**
	 * Algorithm result: addition of image one and the constant.
	 */
	public Image outputImage;

	
	public static final int DOUBLEMODE=0;
	public static final int BYTEMODE=1;
	public static final int INTMODE=2;
	
	/**
  	 * Constructor
  	 *
  	 */
	public AdditionConstantChecked() {		
		
		super();		
		super.inputs = "inputImage1,constant";
		super.options = "mode";
		super.outputs = "outputImage";		
	
	}
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException{		
		outputImage = inputImage1.newInstance(inputImage1.getXDim(), inputImage1.getYDim(), inputImage1.getZDim(), inputImage1.getTDim(), inputImage1.getBDim());
		outputImage.setMask( inputImage1.getMask() );
		int size = inputImage1.size();
		if(mode==DOUBLEMODE)
		{
			double dconstant=constant.doubleValue();
			for(int i = 0; i < size; ++i)
				outputImage.setPixelDouble(i, 
						Math.max(0.0,Math.min(1.0, inputImage1.getPixelDouble(i) + dconstant)));
		} else if(mode==BYTEMODE)
		{
			int iconstant=constant.intValue();
			for(int i = 0; i < size; ++i)
				outputImage.setPixelByte(i, 
						Math.max(0,Math.min(255, inputImage1.getPixelByte(i) + iconstant)));
		} else if(mode==INTMODE)
		{
			int iconstant=constant.intValue();
			for(int i = 0; i < size; ++i)
				outputImage.setPixelInt(i, 
						Math.max(Integer.MIN_VALUE,Math.min(Integer.MAX_VALUE, inputImage1.getPixelInt(i) + iconstant)));
		} else
		{
			System.err.println("This mode is not managed !");
		}
	}
	
	/** 
	 * Compute the addition beetwen an image and a constant.
	 * The outputImage format is the same as inputImage1.
	 * A check is done to keep pixels values in image type natural bounds.
	 * 
	 * If x is a image of type t, c a scalar, and t takes value in [a,b], 
	 * result z at pixel p is then
	 * z(p)=min( b , max ( a , x(p) + c ) )
	 * 
	 * @param inputImage1
	 *            Input image.
	 * @param constant
	 *            Scalar to add.
	 * @return outputImage which is the addition between image inputImage1 and inputConstant.
	 */
	public static <T extends Image> T exec(T inputImage1, double constant) {
		return (T) new AdditionConstantChecked().process(inputImage1,constant,DOUBLEMODE);
	}
	
	/** 
	 * Compute the addition beetwen an image and a constant.
	 * The outputImage format is the same as inputImage1.
	 * A check is done to keep pixels values in image type natural bounds.
	 * 
	 * If x is a image of type t, c a scalar, and t takes value in [a,b], 
	 * result z at pixel p is then
	 * z(p)=min( b , max ( a , x(p) + c ) )
	 * 
	 * @param inputImage1
	 *            Input image.
	 * @param constant
	 *            Scalar to add.
	 * @return outputImage which is the addition between image inputImage1 and inputConstant.
	 */
	public static <T extends Image> T exec(T inputImage1, int constant) {
		return (T) new AdditionConstantChecked().process(inputImage1,constant,BYTEMODE);
	}
	
	/** 
	 * Compute the addition beetwen an image and a constant.
	 * The outputImage format is the same as inputImage1.
	 * A check is done to keep pixels values in image type natural bounds.
	 * 
	 * If x is a image of type t, c a scalar, and t takes value in [a,b], 
	 * result z at pixel p is then
	 * z(p)=min( b , max ( a , x(p) + c ) )
	 * 
	 * @param inputImage1
	 *            Input image.
	 * @param constant
	 *            Scalar to add.
	 * @param mode
	 * 			  byte or int
	 * @return outputImage which is the addition between image inputImage1 and inputConstant.
	 */
	public static <T extends Image> T exec(T inputImage1, int constant, int mode) {
		return (T) new AdditionConstantChecked().process(inputImage1,constant,mode);
	}
}
