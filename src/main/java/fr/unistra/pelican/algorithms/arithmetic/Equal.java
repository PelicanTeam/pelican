package fr.unistra.pelican.algorithms.arithmetic;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;

/**
 * Are two image equals? Comparison is done at the minimum precision format of
 * the two images.
 * 
 * @author ?, Bnejamin Perret
 */
public class Equal extends Algorithm {

	/**
	 * Input image 1
	 */
	public Image inputImage1;

	/**
	 * Input image 2
	 */
	public Image inputImage2;

	/**
	 * Result of comparision
	 */
	public Boolean result;

	/**
	 * Constructor
	 * 
	 */
	public Equal() {

		super();
		super.inputs = "inputImage1,inputImage2";
		super.outputs = "result";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		result = false;
		// Check format
		if (inputImage1.getXDim() != inputImage2.getXDim()
				|| inputImage1.getYDim() != inputImage2.getYDim()
				|| inputImage1.getZDim() != inputImage2.getZDim()
				|| inputImage1.getTDim() != inputImage2.getTDim()
				|| inputImage1.getBDim() != inputImage2.getBDim())
			return;

		int size = inputImage1.size();

		// Check values
		if (inputImage1 instanceof BooleanImage
				|| inputImage2 instanceof BooleanImage) {
			for (int i = 0; i < size; ++i)
				if (inputImage1.getPixelBoolean(i) != inputImage2
						.getPixelBoolean(i))
					if ( !(!inputImage1.isPresent(i) && !inputImage2.isPresent(i)) ) return;
		} else if (inputImage1 instanceof ByteImage
				|| inputImage2 instanceof ByteImage) {
			for (int i = 0; i < size; ++i)
				if (inputImage1.getPixelByte(i) != inputImage2.getPixelByte(i))
					if ( !(!inputImage1.isPresent(i) && !inputImage2.isPresent(i)) ) return;
		} else if (inputImage1 instanceof IntegerImage
				|| inputImage2 instanceof IntegerImage) {
			for (int i = 0; i < size; ++i)
				if (inputImage1.getPixelInt(i) != inputImage2.getPixelInt(i))
					if ( !(!inputImage1.isPresent(i) && !inputImage2.isPresent(i)) ) return;
		} else if (inputImage1 instanceof DoubleImage
				|| inputImage2 instanceof DoubleImage) {
			for (int i = 0; i < size; ++i)
				if (Math.abs(inputImage1.getPixelDouble(i)
						- inputImage2.getPixelDouble(i)) > 0.0000001)
					if ( !(!inputImage1.isPresent(i) && !inputImage2.isPresent(i)) ) return;
		} else
			throw new AlgorithmException("Strange image format?!");

		result = true;
	}
	
	/**
	 * Are two image equals? Comparison is done at the minimum precision format of
	 * the two images.
	 * 
	 * @param inputImage1 input image 1
	 * @param inputImage2 input image 2
	 * @return result of comparision of image 1 and 2
	 */
	public static boolean exec(Image inputImage1, Image inputImage2) {
		return (Boolean) new Equal().process(inputImage1,
				inputImage2);
	}
}
