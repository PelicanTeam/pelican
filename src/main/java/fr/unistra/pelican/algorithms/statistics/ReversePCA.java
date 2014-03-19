package fr.unistra.pelican.algorithms.statistics;

import Jama.Matrix;
import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;

/**
 * This class reverses a performed PCA, given a subset of the eigenImages, the
 * eigenvectors, and the mean values of each dimension.
 * 
 * @author Abdullah
 * 
 */
public class ReversePCA extends Algorithm
{
	/**
	 * The input images
	 */
	public Image input;

	/**
	 * The eigenvectors
	 */
	public double[][] eigenvectors = null;

	/**
	 * the means
	 */
	public double[] mean = null;

	/**
	 * the output images
	 */
	public Image output;
	
	/**
	 * This class reverses a performed PCA, given a subset of the eigenImages, the eigenvectors, and the mean values of each dimension.
	 * @param input The input image
	 * @param eigenvectors the eigenvectors
	 * @param mean the mean
	 * @return the reversed images
	 */
	public static Image exec(Image input,double[] eigenvectors,double[] mean)
	{
		return (Image) new ReversePCA().process(input,eigenvectors,mean);
	}

	/**
	 * Constructor
	 * 
	 */
	public ReversePCA() {

		super();
		super.inputs = "input,eigenvectors,mean";
		super.outputs = "output";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		int bDim = input.getBDim();
		int xDim = input.getXDim();
		int yDim = input.getYDim();

		int size = xDim * yDim;

		output = input.copyImage(false);

		// transform the channels into columns of a 2D matrix...
		double[][] pixels = new double[bDim][size];

		for (int b = 0; b < bDim; b++)
			for (int x = 0; x < xDim; x++)
				for (int y = 0; y < yDim; y++)
					pixels[b][y * xDim + x] = input.getPixelXYBDouble(x, y, b);

		Matrix E = new Matrix(pixels, bDim, size);
		Matrix V = new Matrix(eigenvectors, eigenvectors.length, bDim);

		Matrix M = V.times(E);

		double[][] sonuc = M.getArray();

		// prepare output
		for (int b = 0; b < bDim; b++)
			for (int x = 0; x < xDim; x++)
				for (int y = 0; y < yDim; y++) {
					double d = sonuc[b][y * xDim + x];
					output.setPixelXYBDouble(x, y, b, d + mean[b]);
					// if(d+mean[b] - 1.0 > 0.00001) System.err.println(x + " "
					// + y + " " + (d + mean[b]));
					// if(d+mean[b] > 1.0) System.err.println(x + " " + y + " "
					// + (d + mean[b]));
					// if(d+mean[b] < 0.0) System.err.println(x + " TERS " + y +
					// " TERS =========" + (d + mean[b]));
				}
	}
}
