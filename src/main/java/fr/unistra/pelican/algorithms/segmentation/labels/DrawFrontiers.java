package fr.unistra.pelican.algorithms.segmentation.labels;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;

/**
 * Draw a frontier (in white).
 * 
 * @author Lefevre
 */
public class DrawFrontiers extends Algorithm {

	/**
	 * Input Image
	 */
	public Image inputImage;

	/**
	 * Flag to determine if frontiers are present in the input image
	 */
	public Boolean frontiers;

	/**
	 * Resulting image
	 */
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public DrawFrontiers() {
		super.inputs = "inputImage,frontiers";
		super.outputs = "outputImage";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */

	public void launch() throws AlgorithmException {
		outputImage = new BooleanImage(inputImage, false);
		if (frontiers)
			for (int p = 0; p < inputImage.size(); p++)
				outputImage.setPixelBoolean(p, inputImage.getPixelInt(p) == 0);
		else {
			int val, val2;
			for (int z = 0; z < inputImage.getZDim(); z++)
				for (int t = 0; t < inputImage.getTDim(); t++)
					for (int b = 0; b < inputImage.getBDim(); b++) {
						for (int y = 0; y < inputImage.getYDim(); y++)
							for (int x = 0; x < inputImage.getXDim(); x++) {
								val = inputImage.getPixelInt(x, y, z, t, b);
								if (val == -1)
									continue;
								val = inputImage.getPixelInt(x, y, z, t, b);
								if (val == -1)
									continue;
								if (x > 0 && y > 0) {
									val2 = inputImage.getPixelInt(x - 1, y - 1,
											z, t, b);
									if (val2 != -1 && val2 != val) {
										outputImage.setPixelBoolean(x, y, z, t,
												b, true);
										continue;
									}
								}
								if (x < inputImage.getXDim() - 1 && y > 0) {
									val2 = inputImage.getPixelInt(x + 1, y - 1,
											z, t, b);
									if (val2 != -1 && val2 != val) {
										outputImage.setPixelBoolean(x, y, z, t,
												b, true);
										continue;
									}
								}
								if (x > 0 && y < inputImage.getYDim() - 1) {
									val2 = inputImage.getPixelInt(x - 1, y + 1,
											z, t, b);
									if (val2 != -1 && val2 != val) {
										outputImage.setPixelBoolean(x, y, z, t,
												b, true);
										continue;
									}
								}
								if (x < inputImage.getXDim() - 1
										&& y < inputImage.getYDim() - 1) {
									val2 = inputImage.getPixelInt(x + 1, y + 1,
											z, t, b);
									if (val2 != -1 && val2 != val) {
										outputImage.setPixelBoolean(x, y, z, t,
												b, true);
										continue;
									}
								}
							}
					}

		}

	}

	/*
	 * Draw some frontiers on an image.
	 */
	public static Image exec(Image image, Boolean frontiers) {
		return (Image) new DrawFrontiers().process(image, frontiers);
	}

}
