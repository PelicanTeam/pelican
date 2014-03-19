package fr.unistra.pelican.algorithms.logical;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;

/**
 *	This class realizes a sub-quantization of the input ByteImage.
 *
 *	Mask management (by witz) :
 *		- computation of all pixels is done as usual
 *		- the input image mask becomes the output image mask
 *
 * TODO: Add integerImage sub-quantization
 * 
 * @author Jonathan Weber
 */
public class Quantization extends Algorithm {

	public static final int LEVELSTEP = 0;
	public static final int REALQUANTIZ=1;
	
	/**
	 * Input image
	 */
	public Image input;
	
	/**
	 * Number of bits to keep..at most 8 for bytes
	 */
	public int bit;
	
	/**
	 * The quantization mode :
	 * 		- LEVELSTEP : result intensity scale = 2^8
	 * 		- REALQUANTIZ : result intensity scale = 2^(nb bits)
	 * Default is LEVELSTEP (for better vizualization)
	 */
	public int mode=LEVELSTEP;

	/**
	 * Output image
	 */
	public Image output;


	/**
	 * Constructor
	 * 
	 */
	public Quantization() {

		super();
		super.inputs = "input,bit";
		super.options = "mode";
		super.outputs = "output";
		
	}

	public void launch() throws AlgorithmException {
		output = input.copyImage(false);
		output.setMask(input.getMask());
		if (bit >= 1 && bit <= 8) 
		{
			int shift = 8 - bit;
			if(mode==LEVELSTEP)
			{
				for (int i = input.size(); --i >= 0;) 
				{
					output.setPixelByte(i, (input.getPixelByte(i) >> shift) << shift);
				}
			} else
			{
				for (int i = input.size(); --i >= 0;) 
				{
					output.setPixelByte(i, input.getPixelByte(i) >> shift);
				}
			}
		} else
			throw new AlgorithmException("The bit number must be in [1,8]");
	}

	/**
	 * Realizes a sub-quantization of the input Byte or Integer imag
	 * @param inputImage1 Input image
	 * @param bit  Number of bits to keep..at most 8 for bytes and 32 for ints
	 * @return Output image
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Image> T exec(T inputImage1, int bit) {
		return (T) new Quantization().process(inputImage1,bit);
	}
	
	/**
	 * Realizes a sub-quantization of the input Byte or Integer imag
	 * @param inputImage1 Input image
	 * @param bit  Number of bits to keep..at most 8 for bytes and 32 for ints
	 * @param mode The quantization mode
	 * @return Output image
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Image> T exec(T inputImage1, int bit, int mode) {
		return (T) new Quantization().process(inputImage1,bit,mode);
	}
}
