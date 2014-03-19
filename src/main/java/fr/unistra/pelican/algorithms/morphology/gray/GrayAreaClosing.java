package fr.unistra.pelican.algorithms.morphology.gray;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.arithmetic.Inversion;

/**
 * Perform an area closing on grayscale images by stack decomposition
 * 
 * @author Lefevre
 * 
 */
public class GrayAreaClosing extends Algorithm {
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
	public GrayAreaClosing() {
		super.inputs = "inputImage,thresh";
		super.outputs = "outputImage";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage=Inversion.exec(GrayAreaOpening.exec(Inversion.exec(inputImage),thresh));
	}

	/**
	 * Perform an area closing on grayscale images by stack decomposition
	 * @param inputImage image to be processed
	 * @param thresh Threshold for connected component size
	 * @return filtered picture
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Image> T  exec(T inputImage, Integer thresh)
	{
		return (T) new GrayAreaClosing().process(inputImage, thresh);
	}
}
