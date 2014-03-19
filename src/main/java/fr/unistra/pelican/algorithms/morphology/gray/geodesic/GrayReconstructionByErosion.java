package fr.unistra.pelican.algorithms.morphology.gray.geodesic;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.InvalidNumberOfParametersException;
import fr.unistra.pelican.InvalidTypeOfParameterException;
import fr.unistra.pelican.algorithms.arithmetic.Equal;

/**
 * Perform a gray reconstruction by erosion with a structuring element and a
 * mask. Work on int precision. Output format is the same as mask.
 * 
 * @author Erchan Aptoula, Benjamin Perret
 */
public class GrayReconstructionByErosion extends Algorithm {
	
	/**
	 * Input Image
	 */
	public Image inputImage;

	/**
	 * Input mask
	 */
	public Image mask;

	/**
	 * Input structuring element
	 */
	public BooleanImage se;

	/**
	 * Result
	 */
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public GrayReconstructionByErosion() {
		super.inputs = "inputImage,mask,se";
		super.outputs = "outputImage";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		// outputImage = mask.copyImage(true);
		Image tmp = inputImage.copyImage(true);
		try {

			do {
				outputImage = tmp;
				tmp = (Image) new GrayGeodesicErosion().process(outputImage, mask, se);
			}
			while (!(Boolean)new Equal().process(outputImage, tmp));
		} 
		catch (InvalidTypeOfParameterException e) { e.printStackTrace(); } 
		catch (InvalidNumberOfParametersException e) { e.printStackTrace(); }
	}

	/**
	 * Perform a gray reconstruction by erosion with a structuring element and a
	 * mask. Work on int precision. Output format is the same as mask.
	 * 
	 * @param inputImage Input Image
	 * @param mask Input mask
	 * @param se Input structuring element
	 * @return result...
	 */
	public static Image exec(Image inputImage,Image mask, BooleanImage se) {
		return (Image) new GrayReconstructionByErosion().process(inputImage,mask, se);
	}

}
