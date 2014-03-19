package fr.unistra.pelican.algorithms.morphology.gray;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.morphology.binary.BinaryAreaOpening;
import fr.unistra.pelican.algorithms.segmentation.ManualThresholding;

/**
 * Perform an area opening on grayscale images by stack decomposition
 * 
 * @author Lefevre
 * 
 */
public class GrayAreaOpening extends Algorithm {
	/**
	 * Input image
	 */
	public Image inputImage;

	/**
	 * Threshold for connected component size
	 */
	public int thresh;

	/**
	 * Output Image
	 */
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public GrayAreaOpening() {
		super.inputs = "inputImage,thresh";
		super.outputs = "outputImage";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = inputImage.copyImage(false);
		for (int t=0;t<256;t++) {
			BooleanImage stack=ManualThresholding.exec(inputImage,t/255.);
			stack=BinaryAreaOpening.exec(stack,thresh);
			for (int p=0;p<stack.size();p++)
				if( stack.isPresent(p) && stack.getPixelBoolean(p) )
					outputImage.setPixelByte(p,t);
		}
	}

	/**
	 * Perform an area opening on grayscale images by stack decomposition
	 * @param inputImage image to be processed
	 * @param thresh Threshold for connected component size
	 * @return filtered picture
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Image> T  exec(T inputImage, Integer thresh)
	{
		return (T) new GrayAreaOpening().process(inputImage, thresh);
	}
}
