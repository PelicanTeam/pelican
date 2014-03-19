package fr.unistra.pelican.algorithms.spatial;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;

/**
 * Performs a Gaussian Smoothing (by the use of Gaussian masks)
 * 
 * @author Dany DAMAJ
 */
public class GaussianSmoothing extends Algorithm {

	/**
	 * Input image
	 */
	public Image input;

	/**
	 * Size of the mask
	 */
	public Integer size;

	/**
	 * Standard deviation of the gaussian
	 */
	public Float sig;

	/**
	 * Output image
	 */
	public Image output;

	/**
	 * Gaussian mask
	 */
	public float gaussianMask[][];

	/**
	 * ???
	 */
	public int xDim, yDim;

	/**
	 * Constructor
	 * 
	 */
	public GaussianSmoothing() {
		super();
		super.inputs = "input,size,sig";
		super.outputs = "output";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		computeGaussianMask();

		xDim = input.getXDim();
		yDim = input.getYDim();
		int tDim = input.getTDim();
		int bDim = input.getBDim();
		int zDim = input.getZDim();

		output = input.copyImage(false);

		for (int b = 0; b < bDim; b++)
			for (int t = 0; t < tDim; t++)
				for (int z = 0; z < zDim; z++)
					for (int x = 0/* (size - 1) / 2 */; x < xDim /*- (size-1)/2*/; x++)
						for (int y = 0/* (size - 1) / 2 */; y < yDim /*-  (size - 1) / 2*/; y++) {
							double tmp = applyGaussianMask(x, y, z, t, b);
							output.setPixelDouble(x, y, z, t, b, tmp);
						}
	}

	/*
	 * 
	 */
	private double applyGaussianMask(int x, int y, int z, int t, int b) {
		double res = 0.0;

		if (x >= (size - 1) / 2 && x < xDim - (size - 1) / 2
				&& y >= (size - 1) / 2 && y < yDim - (size - 1) / 2) { // normal
			// case
			for (int i = 0; i < size; i++)
				for (int j = 0; j < size; j++)
					res += gaussianMask[i][j]
							* input.getPixelDouble(x + i - (size - 1) / 2, y
									+ j - (size - 1) / 2, z, t, b);
		} else { // the mask overflows the image
			float real_sum = 0.0f;
			for (int i = 0; i < size; i++)
				for (int j = 0; j < size; j++)
					if (x + i - (size - 1) / 2 >= 0
							&& x + i - (size - 1) / 2 < xDim
							&& y + j - (size - 1) / 2 >= 0
							&& y + j - (size - 1) / 2 < yDim)
						real_sum += gaussianMask[i][j];

			for (int i = 0; i < size; i++)
				for (int j = 0; j < size; j++)
					if (x + i - (size - 1) / 2 >= 0
							&& x + i - (size - 1) / 2 < xDim
							&& y + j - (size - 1) / 2 >= 0
							&& y + j - (size - 1) / 2 < yDim)
						res += gaussianMask[i][j]
								* input.getPixelDouble(x + i - (size - 1) / 2,
										y + j - (size - 1) / 2, z, t, b)
								/ real_sum;

		}

		return res;
	}

	/*
	 * 
	 */
	public void computeGaussianMask(int sizeDesired, float sigma) {
		size = sizeDesired;
		sig = sigma;
		computeGaussianMask();
	}

	/*
	 * 
	 */
	private void computeGaussianMask() {
		gaussianMask = new float[size][size];
		float sum = 0.0f;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				gaussianMask[i][j] = G(i - (size - 1) / 2, j - (size - 1) / 2);
				sum += gaussianMask[i][j];
			}
		}

		// normalisation
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				gaussianMask[i][j] /= sum;
			}
		}
	}

	/*
	 * 
	 */
	private float G(int i, int j) {
		return (float) (Math.pow(Math.E, -((double) (i * i + j * j))
				/ (2.0 * sig * sig)) / (2.0 * Math.PI * sig * sig));
	}
	
	/**
	 * Performs a Gaussian Smoothing (by the use of Gaussian masks)
	 * @param input Input image
	 * @param sigma Standard deviation of the gaussian
	 * @return Ouput image
	 */
	public static Image exec(Image image, int maskSize, float sigma) {
		return (Image) new GaussianSmoothing().process(image,maskSize,sigma);
	}
}
