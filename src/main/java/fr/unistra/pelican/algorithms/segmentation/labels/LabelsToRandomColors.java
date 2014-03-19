package fr.unistra.pelican.algorithms.segmentation.labels;

import java.util.Random;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.InvalidNumberOfParametersException;
import fr.unistra.pelican.InvalidTypeOfParameterException;

/**
 * Convert a labelization to a RGB image, each region has a random color.
 * 
 * @author Sébastien Derivaux, Jonathan Weber
 */
public class LabelsToRandomColors extends Algorithm {

	/**
	 * First input parameter
	 */
	public Image input;

	/**
	 * Option parameter
	 */
	public boolean background = false;

	/**
	 * Output parameter
	 */
	public Image result;

	/**
	 * Constructor
	 * 
	 */
	public LabelsToRandomColors() {
		super.inputs = "input";
		super.options = "background";
		super.outputs = "result";

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		Random random = new Random();
		int xDim = input.getXDim();
		int yDim = input.getYDim();
		int zDim = input.getZDim();
		int tDim = input.getTDim();

		result = new ByteImage(xDim, yDim, zDim, tDim, 3);
		result.setColor(true);

		byte[] color = new byte[3];
		for (int t = 0; t < tDim; t++)
			for (int z = 0; z < zDim; z++)
				for (int y = 0; y < yDim; y++)
					for (int x = 0; x < xDim; x++) {
						if (background && input.getPixelInt(x, y, z, t, 0) == 0)
							for (int c = 0; c < 3; c++)
								result.setPixelByte(x, y, z, t, c, 0);
						else {
							random.setSeed(input.getPixelInt(x, y, z, t, 0) * 131);
							random.nextBytes(color);
							result.setPixelByte(x, y, z, t, 0, color[0]+128);
							result.setPixelByte(x, y, z, t, 1, color[1]+128);
							result.setPixelByte(x, y, z, t, 2, color[2]+128);
						}
					}
	}

	/**
	 * Return color from label
	 * 
	 * @param label
	 *          the label
	 * @return color
	 */
	public static byte[] getColor(int label) {
		Random random = new Random((long) label);
		byte[] color = new byte[3];
		random.nextBytes(color);
		return color;
	}

	/**
	 * Static fonction that use this algorithm.
	 * 
	 * Each algorithm can have one or more of theses static fonction. It's more
	 * convenient for coding.
	 * 
	 * @param image
	 * @return result
	 * @throws InvalidTypeOfParameterException
	 * @throws AlgorithmException
	 * @throws InvalidNumberOfParametersException
	 */
	public static Image exec(Image labels, boolean background) {
		return (Image) new LabelsToRandomColors().process(labels, background);
	}

	public static Image exec(Image labels) {
		return (Image) new LabelsToRandomColors().process(labels);
	}

	// public static void main(String[] args) {
	// String file = "samples/watershed.png";
	// if (args.length > 0)
	// file = args[0];
	// new Viewer2D().process(new LabelsToRandomColors()
	// .process(new ConnectedComponentsLabeling()
	// .process(new ImageLoader().process(file))),
	// "Segmentation of " + file);
	// }

}
