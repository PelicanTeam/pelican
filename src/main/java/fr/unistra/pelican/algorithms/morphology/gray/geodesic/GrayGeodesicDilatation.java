package fr.unistra.pelican.algorithms.morphology.gray.geodesic;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.arithmetic.Minimum;
import fr.unistra.pelican.algorithms.morphology.gray.GrayDilation;

/**
 * Perform a gray geodesic dilatation with a structuring element and a mask.
 * Work on an int precision.
 * 
 * @author Erchan Aptoula, Benjamin Perret
 */
public class GrayGeodesicDilatation extends Algorithm {
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
	public GrayGeodesicDilatation() {

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
		outputImage = GrayDilation.exec(inputImage, se);
		outputImage = Minimum.exec(outputImage, mask);
	}
	
	/**
	 * Perform a gray geodesic dilatation with a structuring element and a mask.
	 * Work on an int precision.
	 * 
	 * @param inputImage Input Image
	 * @param mask Input mask
	 * @param se Input structuring element
	 * @return result...
	 */
	public static Image exec(Image inputImage,Image mask, BooleanImage se) {
		return (Image) new GrayGeodesicDilatation().process(inputImage,mask, se);
	}

}
