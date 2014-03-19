package fr.unistra.pelican.algorithms.logical;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.util.mask.MaskStack;

/**
 * Computes the logical OR of two images. The outputImage is of the lowest
 * argument precision
 * 
 * Mask management (by witz) : - computation with an unmasked pixel p and a
 * masked pixel p' results in value p. - computation with two masked pixels
 * results in value false. - the the output image mask is inputImage1 mask ORed
 * with inputImage2 mask
 * 
 * @author Lefevre
 */
public class OR extends Algorithm {

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
	public OR() {

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
		int size = inputImage1.size();

		boolean isHere1, isHere2;
		if (inputImage1 instanceof BooleanImage
				|| inputImage2 instanceof BooleanImage) {
			outputImage = new BooleanImage(inputImage1, false);

			for (int i = 0; i < size; ++i) {
				boolean tmp = false;
				isHere1 = inputImage1.isPresent(i);
				isHere2 = inputImage2.isPresent(i);
				if (isHere1 && !isHere2)
					tmp = inputImage1.getPixelBoolean(i);
				else if (!isHere1 && isHere2)
					tmp = inputImage2.getPixelBoolean(i);
				else if (isHere1 && isHere2)
					tmp = inputImage1.getPixelBoolean(i)
							|| inputImage2.getPixelBoolean(i);

				outputImage.setPixelBoolean(i, tmp);
			}
		} else if (inputImage1 instanceof ByteImage
				|| inputImage2 instanceof ByteImage) {
			outputImage = new ByteImage(inputImage1, false);

			for (int i = 0; i < size; ++i) {
				int tmp = 0;
				isHere1 = inputImage1.isPresent(i);
				isHere2 = inputImage2.isPresent(i);
				if (isHere1 && !isHere2)
					tmp = inputImage1.getPixelByte(i);
				else if (!isHere1 && isHere2)
					tmp = inputImage2.getPixelByte(i);
				else if (isHere1 && isHere2)
					tmp = inputImage1.getPixelByte(i)
							| inputImage2.getPixelByte(i);

				outputImage.setPixelByte(i, tmp);
			}
		} else if (inputImage1 instanceof IntegerImage
				|| inputImage2 instanceof IntegerImage) {
			outputImage = new IntegerImage(inputImage1, false);

			for (int i = 0; i < size; ++i) {
				int tmp = 0;
				isHere1 = inputImage1.isPresent(i);
				isHere2 = inputImage2.isPresent(i);
				if (isHere1 && !isHere2)
					tmp = inputImage1.getPixelInt(i);
				else if (!isHere1 && isHere2)
					tmp = inputImage2.getPixelInt(i);
				else if (isHere1 && isHere2)
					tmp = inputImage1.getPixelInt(i)
							| inputImage2.getPixelInt(i);

				outputImage.setPixelInt(i, tmp);
			}
		} else
			throw new AlgorithmException(
					"OR cannot be applied to floating point data");

		MaskStack mask = new MaskStack(MaskStack.OR);
		mask.push(inputImage1.getMask());
		mask.push(inputImage2.getMask());
		outputImage.setMask(mask);
	}

	/**
	 * Computes the logical OR of two images. The outputImage is of the lowest
	 * argument precision
	 * 
	 * @param inputImage1
	 *            First input image
	 * @param inputImage2
	 *            Second input image
	 * @return Output image
	 */
	public static Image exec(Image inputImage1, Image inputImage2) {
		return (Image) new OR().process(inputImage1, inputImage2);
	}
}
