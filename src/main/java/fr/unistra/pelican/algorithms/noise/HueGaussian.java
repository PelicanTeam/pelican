package fr.unistra.pelican.algorithms.noise;

import java.util.Random;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.statistics.Correlator;

/**
 * Adds gaussian distributed noise to a given image's hue band
 * 
 * @author ?, Benjamin Perret
 * @TODO check if keeping multivariate noise is necessary
 */
public class HueGaussian extends Algorithm {
	/**
	 * Input image
	 */
	public Image inputImage;

	/**
	 * Noisy result
	 */
	public Image outputImage;

	/**
	 * Standard deviation of noise in [0;1]
	 */
	public double sdev;
	
	/**
	 * Correlation degree in [0;1]
	 */
	public double corr;

	/**
	 * Constructor
	 * 
	 */
	public HueGaussian() {

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

		for (int t = 0; t < tDim; t++) {
			for (int z = 0; z < zDim; z++) {
				for (int x = 0; x < xDim; x++) {
					for (int y = 0; y < yDim; y++) {
						double[] d = inputImage.getVectorPixelXYZTDouble(x, y,
								z, t);
						double[] v = null;

						if (corr > 0) {
							double[] noise = new double[3];
							noise[0] = rand.nextGaussian();
							noise[1] = rand.nextGaussian();
							noise[2] = rand.nextGaussian();

							try {
								v = (double[]) new Correlator().process(noise, sigma);
							} catch (PelicanException e) {
								e.printStackTrace();
							}
						} else {
							v = new double[inputImage.getBDim()];

							for (int i = 0; i < inputImage.getBDim(); i++)
								v[i] = sdev * rand.nextGaussian();
						}

						// only to the hue band
						// and attention to periodicity
						d[0] += v[0];

						while(d[0]<0.0 || d[0]>1.0) {
						if (d[0] < 0.0)
							d[0] += 1.0;
						else if (d[0] > 1.0)
							d[0] -= 1.0;
						}

						outputImage.setVectorPixelXYZTDouble(x, y, z, t, d);
					}
				}
			}
		}
	}

	/**
	 * Adds gaussian distributed noise to a given image's hue band
	 *  
	 * @param inputImage input image
	 * @param sdev deviation of noise
	 * @param corr correlation between band
	 * @return noisy image
	 */
	public static Image exec(Image inputImage, double sdev, double corr)
	{
		return (Image) new HueGaussian().process(inputImage,sdev,corr);
	}
}
