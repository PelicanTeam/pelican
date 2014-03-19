package fr.unistra.pelican.algorithms.segmentation.labels;

import java.util.Random;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;


/**
 * Draw labels over an image.
 * 
 * @author Lefevre
 */
public class DrawLabelsOnImage extends Algorithm {

	/**
	 * Input Image
	 */
	public Image inputImage;

	/**
	 * A label image
	 */
	public Image label;

	/**
	 * Colorize the labels
	 */
	public boolean colorize = true;

	/**
	 * Resulting image
	 */
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public DrawLabelsOnImage() {
		super.inputs = "inputImage,label";
		super.outputs = "outputImage";
		super.options = "colorize";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		if (colorize && inputImage.getBDim() != 3) {
			colorize = false;
			System.err
				.println("DrawLabelsOnImage: colorize mode works only with color images... disabled !");
		}

		int xDim = inputImage.getXDim();
		int yDim = inputImage.getYDim();
		int zDim = inputImage.getZDim();
		int tDim = inputImage.getTDim();
		int bDim = inputImage.getBDim();
		outputImage = inputImage.copyImage(true);

		if (colorize) {
			Random random = new Random();
			byte[] color = new byte[3];
			for (int t = 0; t < tDim; t++)
				for (int z = 0; z < zDim; z++)
					for (int y = 0; y < yDim; y++)
						for (int x = 0; x < xDim; x++)
							if (label.getPixelXYZTInt(x, y, z, t) > 0) {
								random.setSeed(label.getPixelInt(x, y, z, t, 0) * 131);
								random.nextBytes(color);
								outputImage.setPixelByte(x, y, z, t, 0, color[0]);
								outputImage.setPixelByte(x, y, z, t, 1, color[1]);
								outputImage.setPixelByte(x, y, z, t, 2, color[2]);
							}
		} else {
			for (int t = 0; t < tDim; t++)
				for (int z = 0; z < zDim; z++)
					for (int y = 0; y < yDim; y++)
						for (int x = 0; x < xDim; x++)
							if (label.getPixelXYZTInt(x, y, z, t) > 0)
								for (int b = 0; b < bDim; b++)
									outputImage.setPixelByte(x, y, z, t, b, label
										.getPixelXYZTInt(x, y, z, t));
		}

	}

	public static Image exec(Image image, Image label) {
		return (Image) new DrawLabelsOnImage().process(image, label);
	}

	public static Image exec(Image image, Image label, boolean colorize) {
		return (Image) new DrawLabelsOnImage().process(image, label, colorize);
	}

}
