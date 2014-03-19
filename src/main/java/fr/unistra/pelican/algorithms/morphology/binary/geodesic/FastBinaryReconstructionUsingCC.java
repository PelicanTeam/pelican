package fr.unistra.pelican.algorithms.morphology.binary.geodesic;

import java.util.ArrayList;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.segmentation.flatzones.BooleanConnectedComponentsLabeling;

/**
 * Performs a fast binary reconstruction using CC labeling.
 * 
 * Comments can be found in : Luc Vincent, "Morphological Grayscale
 * Reconstruction in Image Analysis: Applications and Efficient Algorithms",
 * IEEE Transaction on Image Processing, 2:2, pages 176-201, april 1993
 * 
 * @author Lefevre
 */
public class FastBinaryReconstructionUsingCC extends Algorithm {

	/**
	 * Constant for 4-connexity
	 */
	public static int CONNEXITY4 = BooleanConnectedComponentsLabeling.CONNEXITY4;

	/**
	 * Constant for 8-connexity
	 */
	public static int CONNEXITY8 = BooleanConnectedComponentsLabeling.CONNEXITY8;

	/**
	 * marker image
	 */
	public Image marker;

	/**
	 * mask image
	 */
	public Image mask;

	/**
	 * Chosen connexity
	 */
	public int connexity;

	/**
	 * Resulting picture
	 */
	public BooleanImage outputImage;

	/**
	 * Constructor
	 * 
	 */
	public FastBinaryReconstructionUsingCC() {
		super.inputs = "marker,mask,connexity";
		super.outputs = "outputImage";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {

		// Apply the CC labeling
		BooleanConnectedComponentsLabeling algoCC = new BooleanConnectedComponentsLabeling();
		ArrayList<Object> v = algoCC.processAll(mask, connexity);
		Image imgLabels = (Image) v.get(0);
		int nbLabels = (Integer) v.get(1);

		// Determine the labels of the CC which contain at least a pixel of the
		// marker
		boolean containLabel[] = new boolean[nbLabels];
		int val;
		for (int p = 0; p < imgLabels.size(); p++)
			// For each marker pixel, check if it corresponds to a label of the
			// mask
			if (marker.getPixelBoolean(p)) {
				val = imgLabels.getPixelInt(p);
				if (val > 0 && !containLabel[val])
					containLabel[val] = true;
			}

		// Update the outputImage by keeping only mask pixels which have a
		// validate label
		outputImage = new BooleanImage(marker, false);
		for (int p = 0; p < outputImage.size(); p++)
			outputImage.setPixelBoolean(p,
					(mask.getPixelBoolean(p) && containLabel[imgLabels
							.getPixelInt(p)]));

	}

	/**
	 * Performs a fast binary reconstruction using CC labeling.
	 * 
	 * @param marker
	 *            marker image
	 * @param mask
	 *            mask image 
	 * @param connexity
	 *            chosen connexity
	 * @return reconstructed image
	 */
	public static BooleanImage exec(Image marker, Image mask, Integer connexity) {
		return (BooleanImage) new FastBinaryReconstructionUsingCC().process(
				marker, mask, connexity);
	}

}
