package fr.unistra.pelican.algorithms.spatial;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.geometric.Crop2D;

/**
 * This class removes all line and columns in the edges of an image where all
 * the pixels are black.
 * 
 * @author Dany DAMAJ
 */
public class CropBlackEdges extends Algorithm {
	
	/**
	 * Input image
	 */
	public Image inputImage;

	/**
	 * Output image
	 */
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public CropBlackEdges() {

		super();
		super.inputs = "inputImage";
		super.outputs = "outputImage";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		// 1st step : top
		boolean stop;
		int topLimit = 0, bottomLimit = inputImage.getYDim() - 1, leftLimit = 0, rightLimit = inputImage
				.getXDim() - 1;

		stop = false;
		while (stop == false) {
			for (int i = 0; i < inputImage.getXDim(); i++)
				if (inputImage.getPixelXYZTBDouble(i, topLimit, 0, 0, 0) != 0)
					stop = true;
			topLimit++;
		}
		topLimit--;

		stop = false;
		while (stop == false) {
			for (int i = 0; i < inputImage.getXDim(); i++)
				if (inputImage.getPixelXYZTBDouble(i, bottomLimit, 0, 0, 0) != 0)
					stop = true;
			bottomLimit--;
		}
		bottomLimit++;

		stop = false;
		while (stop == false) {
			for (int j = 0; j < inputImage.getYDim(); j++)
				if (inputImage.getPixelXYZTBDouble(leftLimit, j, 0, 0, 0) != 0)
					stop = true;
			leftLimit++;
		}
		leftLimit--;

		stop = false;
		while (stop == false) {
			for (int j = 0; j < inputImage.getYDim(); j++)
				if (inputImage.getPixelXYZTBDouble(rightLimit, j, 0, 0, 0) != 0)
					stop = true;
			rightLimit--;
		}
		rightLimit++;

		outputImage = (Image) new Crop2D().process(inputImage, leftLimit, topLimit, rightLimit,
				bottomLimit);
	}

	/**
	 * Removes all line and columns in the edges of an image where all the pixels are black.
	 * @param inputImage Input image
	 * @return Output image
	 */
	public static Image exec(Image inputImage) {
		return (Image) new CropBlackEdges().process(inputImage);
	}
}
