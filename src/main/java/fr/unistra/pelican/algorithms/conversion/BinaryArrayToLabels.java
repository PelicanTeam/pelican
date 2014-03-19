package fr.unistra.pelican.algorithms.conversion;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;

/**
 *	This class combines several binary images into a single label image. A boolean
 *	flag is used to switch between normal and accumulate mode
 *
 *	The outputImage format is IntegerImage
 *
 *	MASK MANAGEMENT (by Régis) : absent pixels in each input image count as if at false.
 * 
 * @author Lefevre
 */
public class BinaryArrayToLabels extends Algorithm {

	/**
	 * The input array of binary images.
	 */
	public Image[] inputImage;

	/**
	 * The flag to use accumulate mode
	 */
	public boolean accumulate;

	/**
	 * The output label image
	 */
	public Image outputImage;

	/**
	 * Constructor
	 */
	public BinaryArrayToLabels() { 

		super.inputs = "inputImage";
		super.options = "accumulate";
		super.outputs = "outputImage";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException { 

		// generate output
		outputImage = new IntegerImage(inputImage[0], false);
		int size = inputImage[0].size();
		int length = inputImage.length;
		int val;

		fr.unistra.pelican.util.mask.MaskStack[] masks = 
			new fr.unistra.pelican.util.mask.MaskStack[length];
		for ( int t = 0 ; t < length ; t++ ) masks[t] = this.inputImage[t].getMask();

		// accumulate mode
		if (accumulate == true)
			for (int p = 0; p < size; p++) {
				val = 0;
				for (int t = 0; t < length; t++)
					if (	inputImage[t].getPixelBoolean(p) 
						&&	masks[t] != null && !masks[t].empty() && inputImage[t].isPresent( p ) )
						val++;
				outputImage.setPixelInt(p, val);
			}
		else
			for (int p = 0; p < size; p++) {
				val = -1;
				for (int t = 0; t < length; t++)
					if (	inputImage[t].getPixelBoolean(p) 
						&&	masks[t] != null && !masks[t].empty() && inputImage[t].isPresent( p ) )
						if (val == -1)
							val = t;
						else
							val = length;
				outputImage.setPixelInt(p, val + 1);
			}

	}

	/**
	 * Combines several binary images into a single label image
	 * 
	 * @param inputImage
	 *            The input array of binary images
	 * @param accumulate
	 *            The flag to use accumulate mode
	 * @return The output label image
	 */
	public static IntegerImage exec(Image[] inputImage, boolean accumulate) {
		return (IntegerImage) new BinaryArrayToLabels().process(inputImage,
				accumulate);
	}

	/**
	 * Combines several binary images into a single label image
	 * 
	 * @param inputImage
	 *            The input array of binary images
	 * @return The output label image
	 */
	public static IntegerImage exec(Image[] inputImage) {
		return (IntegerImage) new BinaryArrayToLabels()
				.process((Object) inputImage);
	}
}
