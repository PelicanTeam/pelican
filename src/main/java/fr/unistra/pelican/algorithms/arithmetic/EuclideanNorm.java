package fr.unistra.pelican.algorithms.arithmetic;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;

/**
 * Compute the euclidian norm on a multiband image. for a pixel, the new image
 * of one band b is : b = sqrt(b1*b1 + b2*b2 + ...)
 * 
 * @author ?, Benjamin Perret
 */
public class EuclideanNorm extends Algorithm {

	/**
	 * Input image 1
	 */
	public Image inputImage;

	/**
	 * Output image
	 */
	public Image outputImage;

	/**
	 * Flag to determine if integer computation is used (i.e. sum of absolute
	 * values)
	 */
	public Boolean integer = false;

	/**
	 * Constructor
	 * 
	 */

	public EuclideanNorm() {
		super.inputs = "inputImage";
		super.outputs = "outputImage";
		super.options = "integer";

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {

		if (inputImage instanceof ByteImage)
			outputImage = new ByteImage(inputImage.getXDim(), inputImage
					.getYDim(), inputImage.getZDim(), inputImage.getTDim(), 1);
		else if (inputImage instanceof IntegerImage)
			outputImage = new IntegerImage(inputImage.getXDim(), inputImage
					.getYDim(), inputImage.getZDim(), inputImage.getTDim(), 1);
		else
			outputImage = new DoubleImage(inputImage.getXDim(), inputImage
					.getYDim(), inputImage.getZDim(), inputImage.getTDim(), 1);

		outputImage.setMask( inputImage.getMask() );
		// Do it for all bands
		int xDim = inputImage.getXDim();
		int yDim = inputImage.getYDim();
		int zDim = inputImage.getZDim();
		int tDim = inputImage.getTDim();
		int bDim = inputImage.getBDim();
		// Standard case
		if (!integer)
			for (int x = 0; x < xDim; x++)
				for (int y = 0; y < yDim; y++)
					for (int z = 0; z < zDim; z++)
						for (int t = 0; t < tDim; t++) {
							double dist = 0.0;
							for (int b = 0; b < bDim; b++) {
								double pixel = inputImage.getPixelXYZTBDouble( x, y, z, t, b );
								dist += pixel * pixel;
							}
							outputImage.setPixelXYZTDouble(x, y, z, t, Math.sqrt(dist));
						}
		else {
			for (int x = 0; x < xDim; x++)
				for (int y = 0; y < yDim; y++)
					for (int z = 0; z < zDim; z++)
						for (int t = 0; t < tDim; t++) {
							double dist = 0.0;
							for (int b = 0; b < bDim; b++) {
								double pixel = inputImage.getPixelXYZTBDouble(
										x, y, z, t, b);
								dist += Math.abs(pixel);
							}
							outputImage.setPixelXYZTDouble(x, y, z, t, dist);
						}
		}
	}

	/**
	 * Compute the euclidian norm on a multiband image. for a pixel, the new
	 * image of one band b is : b = sqrt(b1*b1 + b2*b2 + ...)
	 * 
	 * @param inputImage
	 *            input multi band image
	 * @return euclidian norm of input image
	 */
	public static Image exec(Image inputImage) {
		return (Image) new EuclideanNorm().process(inputImage);
	}

	public static Image exec(Image inputImage, Boolean integer) {
		return (Image) new EuclideanNorm().process(inputImage, integer);
	}

}
