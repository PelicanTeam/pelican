package fr.unistra.pelican.algorithms.statistics;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;

/**
 * This class performs the principal components transformation.. (aka Karhunen Loeve or
 * Hotelling transformation) still in experimental mode...but works all the
 * same. Done according to wiki :
 * http://en.wikipedia.org/wiki/Principal_components_analysis dont forget to
 * scale the doubleImage for visualisation.
 * 
 * @author Abdullah
 * 
 */
public class PCA extends Algorithm
{
	/**
	 * The input image
	 */
	public Image input;

	/**
	 * the output images
	 */
	public Image output;

	/**
	 * the eigenvectors
	 */
	public double[][] eigenvectors = null;

	/**
	 * the eigen values
	 */
	public double[] eigenvalues = null;

	/**
	 * the means
	 */
	public double[] mean = null;
	
	/**
	 * This method performs the Hotelling transform
	 * @param image The input image
	 * @return the eigen images sorted according to their variances in descending order
	 */
	public static Image exec(Image image)
	{
		return (Image)new PCA().process(image);
	}

	/**
	 * Constructor
	 * 
	 */
	public PCA() {

		super();
		super.inputs = "input";
		super.outputs = "output,eigenvectors,eigenvalues,mean";
		
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

		if (bDim == 1) {
			output = input.copyImage(true);
			return;
		}

		// transform the channels into columns of a 2D matrix...
		double[][] pixels = new double[bDim][xDim * yDim];

		for (int b = 0; b < bDim; b++)
			for (int x = 0; x < xDim; x++)
				for (int y = 0; y < yDim; y++)
					pixels[b][y * xDim + x] = input.getPixelXYBDouble(x, y, b);

		// get the mean of each dimension
		mean = new double[bDim];

		for (int i = 0; i < bDim; i++) {
			mean[i] = 0.0;

			for (int j = 0; j < xDim * yDim; j++)
				mean[i] += pixels[i][j];

			mean[i] = mean[i] / size;
		}

		// substract the mean from the input..
		for (int i = 0; i < bDim; i++)
			for (int j = 0; j < size; j++)
				pixels[i][j] = pixels[i][j] - mean[i];

		// covariance matrix...first multiply with transpose

		Matrix M = new Matrix(pixels, bDim, size);
		Matrix Mt = M.transpose();
		M = M.times(Mt);

		System.gc();

		// then normalize
		M = M.times(1 / (double) (size - 1));

		/*
		 * double[][] tmp = M.getArray();
		 * 
		 * for(int i = 0; i < bDim; i++){ System.err.print("DEBUG PCA: cov ");
		 * 
		 * for(int j = 0; j < bDim; j++) System.err.print(tmp[i][j] + " ");
		 * 
		 * System.err.println(" "); }
		 */

		EigenvalueDecomposition ev = new EigenvalueDecomposition(M);
		Matrix V = ev.getV();

		eigenvalues = ev.getRealEigenvalues();
		eigenvectors = V.getArray();

		sort(eigenvalues, eigenvectors, mean, bDim);

		V = new Matrix(eigenvectors);

		M = Mt.times(V);
		M = M.transpose();

		System.gc();

		// the eigen images
		double[][] eigenImages = M.getArray();

		/*
		 * for(int i = 0; i < bDim; i++){ System.err.print("DEBUG PCA :
		 * eigenvectors "); for(int j = 0; j < bDim; j++){
		 * System.err.print(eigenvectors[i][j] + " "); } System.err.println("
		 * "); }
		 */

		// cleanup
		for (int j = 0; j < eigenImages.length; j++) {
			if (eigenvalues[j] < 0) {
				eigenvalues[j] = 0.0;

				for (int i = 0; i < eigenImages[j].length; i++)
					eigenImages[j][i] = 0;
			}
		}

		// prepare output..
		output = new DoubleImage(input, false);
		for (int b = 0; b < bDim; b++) {
			for (int x = 0; x < xDim; x++) {
				for (int y = 0; y < yDim; y++) {
					double d = eigenImages[b][y * xDim + x];
					output.setPixelXYBDouble(x, y, b, d);
				}
			}
		}
	}

	/*
	public static double[] getVariances(double[] vals) {
		double totalVariance = 0.0;
		double[] variances = new double[vals.length];

		// add up the eigen values..(= variances..sort of)
		for (int j = 0; j < vals.length; j++)
			totalVariance += vals[j];

		// variance percentage of each channel
		for (int j = 0; j < vals.length; j++)
			variances[j] = vals[j] / totalVariance;

		return variances;
	}
	*/
	
	private void sort(double[] vals, double[][] eigenvectors, double[] mean,
			int n) {
		for (int i = 0; i < n; i++) {
			int k = i;
			double p = vals[k];

			for (int j = i + 1; j < n; j++) {
				if (vals[j] >= p) {
					k = j;
					p = vals[k];
				}
			}

			if (k != i) {
				vals[k] = vals[i];
				vals[i] = p;
				// p = mean[i];
				// mean[i] = mean[k];
				// mean[k] = p;
				for (int j = 0; j < n; j++) {
					p = eigenvectors[j][i];
					eigenvectors[j][i] = eigenvectors[j][k];
					eigenvectors[j][k] = p;
				}
			}
		}
	}
}
