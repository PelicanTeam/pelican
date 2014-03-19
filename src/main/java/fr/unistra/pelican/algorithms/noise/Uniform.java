package fr.unistra.pelican.algorithms.noise;

import java.util.Random;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.statistics.Correlator;

/**
 * Adds uniformly distributed noise to a given image (3 bands max)
 * 
 * @author ?, Benjamin Perret
 * 
 * @see fr.unistra.pelican.algorithms.noise.MultivariateNormalNoise
 */
public class Uniform extends Algorithm {
	/**
	 * Input image
	 */
	public Image inputImage;

	/**
	 * Output image
	 */
	public Image outputImage;

	/**
	 * Noise deviation (same in each band)  in [0;1]
	 * 	!!! Not equal to standard deviation
	 */
	public double sdev;

	/**
	 * Inter band correlation in [0;1]
	 */
	public double corr;

	/**
	 * Constructor
	 * 
	 */
	public Uniform() {

		super();
		super.inputs = "inputImage,sdev,corr";
		super.outputs = "outputImage";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = inputImage.copyImage(false);

		Random rand = new Random();

		double s = sdev * sdev;
		double r = s * corr;

		double[][] sigma = { { s, r, r }, { r, s, r }, { r, r, s } };

		if (sdev > 1.0 || sdev <= 0.0)
			throw new AlgorithmException(
					"The standard deviation must be in ]0,1]");

		if (corr > 1.0 || corr < 0.0)
			throw new AlgorithmException(
					"The correlation degree must be in [0,1]");

		if (inputImage.getBDim() != 3 && corr > 0)
			throw new AlgorithmException(
					"For now correlated noise is available only to 3 channel input, sorry");

		int xDim = inputImage.getXDim();
		int yDim = inputImage.getYDim();
		int tDim = inputImage.getTDim();
		int zDim = inputImage.getZDim();

		for (int t = 0; t < tDim; t++)
			for (int z = 0; z < zDim; z++)
				for (int x = 0; x < xDim; x++)
					for (int y = 0; y < yDim; y++) {
						double[] d = inputImage.getVectorPixelXYZTDouble(x, y,
								z, t);
						double[] v = null;

						if (corr > 0) {
							double[] noise = new double[3];
							noise[0] = rand.nextDouble();
							noise[1] = rand.nextDouble();
							noise[2] = rand.nextDouble();

							try {
								v = (double[]) new Correlator().process(noise, sigma);
							} catch (PelicanException e) {
								e.printStackTrace();
							}
						} else {
							v = new double[inputImage.getBDim()];

							for (int i = 0; i < inputImage.getBDim(); i++)
								v[i] = sdev * (rand.nextDouble() - 0.5) * 2;
						}

						for (int i = 0; i < inputImage.getBDim(); i++) {
							d[i] += v[i];

							if (d[i] < 0.0)
								d[i] = 0.0;
							else if (d[i] > 1.0)
								d[i] = 1.0;
						}

						outputImage.setVectorPixelXYZTDouble(x, y, z, t, d);
					}
	}

	/**
	 * Adds uniformly distributed noise to a given image (3 bands max)
	 * 
	 * @param inputImage Input image	
	 * @param sdev Deviation between [0;1], should not be considered as standard deviation
	 * @param corr Correlation between bands (identical between each bands)
	 * @return Noisy image
	 */
	public static Image exec(Image inputImage, double sdev, double corr)
	{
		return (Image) new Uniform().process(inputImage,sdev,corr);
	}
	
}
