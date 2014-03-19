package fr.unistra.pelican.algorithms.arithmetic;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;

/**
 * Compute a new image which store for each pixel the distance from this pixel
 * in the input image to the given interval.
 * 
 * @author ?, Benjamin Perret
 * 
 * TODO: Add t-dim and z-dim.
 * TODO: explain in JavaDoc
 */
public class DistanceToByteInterval extends Algorithm {

	/**
	 * Input image
	 */
	public Image inputImage;

	/**
	 * Input intervals
	 */
	public int[][] interval;

	/**
	 * Result
	 */
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public DistanceToByteInterval() {

		super();
		super.inputs = "inputImage,interval";
		super.outputs = "outputImage";
	
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = inputImage.copyImage(false);
		outputImage.setMask( inputImage.getMask() );

		// Do it for all bands
		int xDim = outputImage.getXDim();
		int yDim = outputImage.getYDim();
		int bDim = outputImage.getBDim();
		for (int x = 0; x < xDim; x++)
			for (int y = 0; y < yDim; y++)
				for (int b = 0; b < bDim; b++) {
					int pixel = inputImage.getPixelXYBByte(x, y, b);
					if (pixel >= interval[b][0] && pixel <= interval[b][1])
						outputImage.setPixelXYBByte(x, y, b, 0);
					else if (pixel > interval[b][1])
						outputImage.setPixelXYBByte(x, y, b, pixel
								- interval[b][1]);
					else
						// if(pixel < interval[b][0])
						outputImage.setPixelXYBByte(x, y, b, interval[b][0]
								- pixel);
				}
	}
	
	/**
	 * Compute a new image which store for each pixel the distance from this pixel
	 * in the input image to the given interval.
	 * 
	 * @param inputImage Input image
	 * @param interval Input intervals
	 * @return distance image
	 */
	public static Image exec(Image inputImage, int[][] interval) {
		return (Image) new DistanceToByteInterval().process(inputImage,interval);
	}
}
