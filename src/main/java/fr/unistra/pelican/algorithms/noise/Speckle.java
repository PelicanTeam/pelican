package fr.unistra.pelican.algorithms.noise;

import java.util.Random;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;

/**
 * Adds uncorrelated speckle noise to a given image
 * 
 * Output type is same as input type.
 * 
 * @author ?, Benjamin Perret
 */
public class Speckle extends Algorithm {
	/**
	 * Input image
	 */
	public Image inputImage;

	/**
	 * Noisy result
	 */
	public Image outputImage;

	/**
	 * Constant for noise type Salt
	 */
	public static final int SALT = 0;

	/**
	 * Constant for noise type Pepper
	 */
	public static final int PEPPER = 1;

	/**
	 * Constant for noise type Salt & Pepper
	 */
	public static final int SALT_PEPPER = 2;

	/**
	 * Probability of corruption for a single pixel in [0;1]
	 */
	public double prop;

	/**
	 * Type of noise
	 * @see fr.unistra.pelican.algorithms.noise.Speckle#PEPPER
	 * @see fr.unistra.pelican.algorithms.noise.Speckle#SALT
	 * @see fr.unistra.pelican.algorithms.noise.Speckle#SALT_PEPPER
	 */
	public int type;

	/**
	 * Constructor
	 * 
	 */
	public Speckle() {

		super();
		super.inputs = "inputImage,prop,type";
		super.outputs = "outputImage";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = inputImage.copyImage(true);

		Random rand = new Random();

		if (prop > 1.0 || prop <= 0.0)
			throw new AlgorithmException(
					"The noise propability must be in ]0,1[");

		if (type != SALT && type != PEPPER && type != SALT_PEPPER)
			throw new AlgorithmException("Invalid noise type");

		int xDim = inputImage.getXDim();
		int yDim = inputImage.getYDim();
		int tDim = inputImage.getTDim();
		int zDim = inputImage.getZDim();
		int bDim = inputImage.getBDim();

		for (int b = 0; b < bDim; b++)
			for (int t = 0; t < tDim; t++)
				for (int z = 0; z < zDim; z++)
					for (int x = 0; x < xDim; x++)
						for (int y = 0; y < yDim; y++) {
							if (prop < rand.nextDouble())
								continue;

							switch (type) {
							case SALT:
								outputImage.setPixelXYZTBDouble(x, y, z, t, b,
										1.0);
								break;
							case PEPPER:
								outputImage.setPixelXYZTBDouble(x, y, z, t, b,
										0.0);
								break;
							default:
								if (rand.nextDouble() > 0.5)
									outputImage.setPixelXYZTBDouble(x, y, z, t,
											b, 0.0);
								else
									outputImage.setPixelXYZTBDouble(x, y, z, t,
											b, 1.0);
							}
						}
	}

	/**
	 * Adds uncorrelated speckle noise to a given image
	 * 
	 * Output type is same as input type.
	 *  
	 * @param inputImage Input image
	 * @param prop Probability of corruption of a single pixel
	 * @param type Type of noise to apply
	 * @return Noisy version of input image
	 */
	public static<T extends Image> T exec(T inputImage, double prop, int type)
	{
		return (T) new Speckle().process(inputImage,prop,type);
	}
	
}
