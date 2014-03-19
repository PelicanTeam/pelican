package fr.unistra.pelican.algorithms.histogram;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;

/**
 * Stretch the contrast of an image so that the minimal intensity pixel (in
 * double format) for each band is 0.0 and the maximal intensity is 1.0.
 * 
 * Optionally, the stretch can be performed independently on each slice or frame
 * 
 * @author Lefevre
 * 
 */
public class ContrastStretchEachBands extends Algorithm {

	/**
	 * Input image
	 */
	public Image inputImage;

	/**
	 * Output image
	 */
	public Image outputImage;

	/**
	 * Optional dimension to stretch: Image.Z, Image.T, Image.B
	 */
	public int dimension = Image.B;

	/**
	 * Constructor
	 */
	public ContrastStretchEachBands() {
		super.inputs = "inputImage";
		super.outputs = "outputImage";
		super.options = "dimension";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = inputImage.copyImage(false);
		int size = 0;
		double min;
		double max;
		double dist;
		// Do it for all bands
		int zDim = outputImage.getZDim();
		int tDim = outputImage.getTDim();
		int bDim = outputImage.getBDim();
		switch (dimension) {
		case Image.Z:
			size = zDim;
			break;
		case Image.T:
			size = tDim;
			break;
		case Image.B:
			size = bDim;
			break;
		}
		for (int i = 0; i < size; i++) {
			// Get current 4D image
			Image tmp = inputImage.getImage4D(i, dimension);
			// Compute min and max
			min = tmp.minimumDouble();
			max = tmp.maximumDouble();
			dist = max - min;
			// Modify the pixels
			if (max != min) {
				for (int p = 0; p < tmp.size(); p++) {
					double val = tmp.getPixelDouble(p);
					val = (val - min) / dist;
					tmp.setPixelDouble(p, val);
				}
				// Set current 4D image
				outputImage.setImage4D(tmp, i, dimension);
			}
		}
	}

	/**
	 * Stretch the contrast of an image so that the minimal intensity pixel (in
	 * double format) for each band is 0.0 and the maximal intensity is 1.0.
	 * 
	 * Optionally, the stretch can be performed independently on each slice or
	 * frame
	 * 
	 * @param inputImage
	 *          image to be stretched
	 * @return the stretched image
	 */
	public static Image exec(Image inputImage) {
		return (Image) new ContrastStretchEachBands().process(inputImage);
	}

	/**
	 * Stretch the contrast of an image so that the minimal intensity pixel (in
	 * double format) for each band is 0.0 and the maximal intensity is 1.0.
	 * 
	 * Optionally, the stretch can be performed independently on each slice or
	 * frame
	 * 
	 * @param inputImage
	 *          image to be stretched
	 * @param dimension
	 *          the dimension considered : Image.Z, Image.T, Image.B
	 * @return the stretched image
	 */
	public static Image exec(Image inputImage, int dimension) {
		return (Image) new ContrastStretchEachBands()
			.process(inputImage, dimension);
	}

}
