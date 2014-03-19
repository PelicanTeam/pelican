package fr.unistra.pelican.algorithms.histogram;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;

/**
 *	This class realizes a non uniform sub-quantization of the input ByteImage.
 *  Each band has its quantization parameter
 *
 *	Mask management (by witz) :
 *		- computation of all pixels is done as usual
 *		- the input image mask becomes the output image mask
 *
 * TODO: Add integerImage sub-quantization
 * 
 * @author Jonathan Weber
 */
public class NonUniformColorQuantization extends Algorithm {
	
	public static final int LEVELSTEP = 0;
	public static final int REALQUANTIZ=1;
	
	/**
	 * Input image
	 */
	public Image input;
	
	/**
	 * Number of bits to keep in band 0..at most 8 for bytes
	 */
	public int bit0;

	/**
	 * Number of bits to keep in band 1..at most 8 for bytes
	 */
	public int bit1;
	
	/**
	 * Number of bits to keep in band 2..at most 8 for bytes
	 */
	public int bit2;
	
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
	public NonUniformColorQuantization() {

		super();
		super.inputs = "input,bit0,bit1,bit2";
		super.options = "mode";
		super.outputs = "output";
		
	}

	public void launch() throws AlgorithmException {
		if (bit0 >= 1 && bit0 <= 8 && bit1 >= 1 && bit1 <= 8 && bit2 >= 1 && bit2 <= 8) 
		{			
			output = input.copyImage(false);
			output.setMask(input.getMask());
			int[] tabCorresp0 = new int[256];
			int[] tabCorresp1 = new int[256];
			int[] tabCorresp2 = new int[256];
			int shift0 = 8 - bit0;
			int shift1 = 8 - bit1;
			int shift2 = 8 - bit2;
			if(mode==LEVELSTEP)
			{
				for(int i=0;i<256;i++)
				{
					tabCorresp0[i] = (i >> shift0) << shift0;
					tabCorresp1[i] = (i >> shift1) << shift1;
					tabCorresp2[i] = (i >> shift2) << shift2;
				}
			}
			else
			{
				for(int i=0;i<256;i++)
				{
					tabCorresp0[i] = i >> shift0;
					tabCorresp1[i] = i >> shift1;
					tabCorresp2[i] = i >> shift2;
				}
			}
			int i=0;
			while(i<input.size()) 
			{
				output.setPixelByte(i, tabCorresp0[input.getPixelByte(i)]);
				i++;
				output.setPixelByte(i, tabCorresp1[input.getPixelByte(i)]);
				i++;
				output.setPixelByte(i, tabCorresp2[input.getPixelByte(i)]);
				i++;
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
	public static Image exec(Image inputImage1, int bit0,int bit1,int bit2) {
		return (Image) new NonUniformColorQuantization().process(inputImage1,bit0,bit1,bit2);
	}
}