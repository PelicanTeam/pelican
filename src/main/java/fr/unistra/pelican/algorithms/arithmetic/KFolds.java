package fr.unistra.pelican.algorithms.arithmetic;

import java.util.Random;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.Image;

/**
 * Cut the samples dataset in k folds.
 * A sample image is a boolean image of a bands for each
 * class, if true, a pixel is a sample.
 * 
 * @author Sébastien Derivaux
 */
public class KFolds extends Algorithm {
	// Inputs parameters
	/**
	 * Input Image
	 */
	public Image input;

	/**
	 * nbFolds
	 */
	public int nbFolds;

	// Outputs parameters
	/**
	 * Resulting samples images
	 */
	public Image[] output;

	/**
	 * Constructor
	 * 
	 */
	public KFolds() {

		super();
		super.inputs = "input,nbFolds";
		super.outputs = "output";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() {
		int xDim = input.getXDim();
		int yDim = input.getYDim();
		int bDim = input.getBDim();

		output = new Image[nbFolds];

		for (int i = 0; i < output.length; i++) {
			output[i] = input.copyImage(false);
			output[i].fill(0.0);
		}

		Random r = new Random();

		for (int b = 0; b < bDim; b++)
			for (int x = 0; x < xDim; x++)
				for (int y = 0; y < yDim; y++)
					if (input.getPixelXYBBoolean(x, y, b))
						output[r.nextInt(nbFolds)].setPixelXYBBoolean(x, y, b, true);
	}

	/**
	 * Cut the samples dataset in k folds.
	 * A sample image is a boolean image of a bands for each
	 * class, if true, a pixel is a sample.
	 * 
	 * @param input Sample image
	 * @param nbFolds number of subsamples images needed. 
	 * @return Resulting samples images
	 */
	public static Image[] exec(Image input, int nbFolds) {
		return (Image[])new KFolds().process(input, nbFolds);
	}
}
