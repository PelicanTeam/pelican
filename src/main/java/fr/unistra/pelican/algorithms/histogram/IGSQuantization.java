package fr.unistra.pelican.algorithms.histogram;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;

/**
 *	This class realizes an IGS sub-quantization of the input ByteImage.
 *
 *	Mask management (by witz) :
 *		- computation of all pixels is done as usual
 *		- the input image mask becomes the output image mask
 *
 * TODO: Add integerImage sub-quantization
 * 
 * @author Jonathan Weber
 */
public class IGSQuantization extends Algorithm {
	
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
	public IGSQuantization() {

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
			int shift = 8-bit;
			int pseudoRandomCode = 0;
			if(mode==LEVELSTEP)
			{
				for (int i = input.size(); --i >= 0;) 
				{
					int value = input.getPixelByte(i);
					output.setPixelByte(i, ((value+pseudoRandomCode) >> shift) << shift);
					pseudoRandomCode = value - ((value>> shift)<<shift);
				}
			}
			else
			{
				for (int i = input.size(); --i >= 0;) 
				{
					int value = input.getPixelByte(i);
					output.setPixelByte(i, value+pseudoRandomCode >> shift);
					pseudoRandomCode = value - ((value>> shift)<<shift);
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
	public static Image exec(Image inputImage1, int bit) {
		return (Image) new IGSQuantization().process(inputImage1,bit);
	}
	
	/**
	 * Realizes a sub-quantization of the input Byte or Integer imag
	 * @param inputImage1 Input image
	 * @param bit  Number of bits to keep..at most 8 for bytes and 32 for ints
	 * @param mode Quantization mode
	 * @return Output image
	 */
	public static Image exec(Image inputImage1, int bit, int mode) {
		return (Image) new IGSQuantization().process(inputImage1,bit,mode);
	}
}