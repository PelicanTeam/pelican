package fr.unistra.pelican.algorithms.morphology.gray.geodesic;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.arithmetic.Maximum;
import fr.unistra.pelican.algorithms.morphology.gray.GrayErosion;

/**
 * Perform a gray geodesic erosion with a structuring element and a mask. Work
 * on an int precision.
 * 
 * @author Erchan Aptoula, Benjamin Perret
 */
public class GrayGeodesicErosion extends Algorithm {
	
	/**
	 * Input Image
	 */
	public Image inputImage;

	/**
	 * Input Mask
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
	public GrayGeodesicErosion() {

		super();
		super.inputs = "inputImage,mask,se";
		super.outputs = "outputImage";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = inputImage.copyImage(false);

		try {
			outputImage = (Image) new GrayErosion().process(inputImage, se);
			outputImage = (Image) new Maximum().process(outputImage, mask);

		} catch (PelicanException e) { e.printStackTrace(); }
	}
	
	/**
	 * Perform a gray geodesic erosion with a structuring element and a mask. Work
	 * on an int precision.
	 *  
	 * @param inputImage Input Image
	 * @param mask Input mask
	 * @param se Input structuring element
	 * @return result...
	 */
	public static Image exec(Image inputImage,Image mask, BooleanImage se) {
		return (Image) new GrayGeodesicErosion().process(inputImage,mask, se);
	}

}
