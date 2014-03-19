package fr.unistra.pelican.algorithms.arithmetic;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;

/**
 * Invert an image (default complement function). 
 * Work on all format.
 * 
 * Details on standard complement function can be found in class Tools
 * @see fr.unistra.pelican.util.Tools
 *  
 * @author  ?, Benjamin Perret
 */
public class Inversion extends Algorithm {
	
	/**
	 * Input image 
	 */
	public Image inputImage;

	/**
	 * Complement of input image
	 */
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public Inversion() {

		super();
		super.inputs = "inputImage";
		super.outputs = "outputImage";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = inputImage.copyImage(false);
		if (inputImage instanceof ByteImage) {
			for (int i = 0; i < inputImage.size(); i++)
				outputImage.setPixelByte(i,255 - inputImage.getPixelByte(i));

		} else if (inputImage instanceof BooleanImage) {
			BooleanImage inputBool = (BooleanImage) inputImage;
			BooleanImage outputBool = inputBool.getComplement();
			outputImage = outputBool;
		} else {
			for (int i = 0; i < inputImage.size(); i++)
				outputImage.setPixelDouble(i,1.0 - inputImage.getPixelDouble(i));
		}
	}
	
	/**
	 * Invert an image (default complement function). 
	 * Work on all format.
	 * 
	 * Details on standard complement function can be found in class Tools
	 * @see fr.unistra.pelican.util.Tools
	 * 
	 * @param inputImage Input Image
	 * @return Complement of input
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Image> T exec(T inputImage) {
		return (T)new Inversion().process(inputImage);
	}
}
