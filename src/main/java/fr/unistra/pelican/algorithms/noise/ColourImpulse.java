package fr.unistra.pelican.algorithms.noise;

import java.util.Random;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;

/**
 * Multichannel impulse noise model (3 bands image)
 * 
 * from : Adaptive fuzzy systems for multichannel signal processing Plataniotis &
 * Venetsanopoulos IEEE 1999
 * 
 * @author ?, Benjamin Perret
 */
public class ColourImpulse extends Algorithm {
	
	/**
	 * Input image
	 */
	public Image input;

	/**
	 * Noisy result
	 */
	public Image output;

	/**
	 * Global noise probability
	 */
	public double globalP;

	/**
	 * Array of channel noise probability
	 */
	public double[] channelP;

	
	private Random rand;

	// part of the unit interval to be used for impulse values
	private final double PEAK = 0.8;

	/**
	 * Constructor
	 * 
	 */
	public ColourImpulse() {

		super();
		super.inputs = "input,globalP,channelP";
		super.outputs = "output";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		output = input.copyImage(true);

		rand = new Random();

		double ps = 0.0;

		if (globalP > 1.0 || globalP < 0.0)
			throw new AlgorithmException(
					"The global noise propability must be in [0,1]");

		for (int i = 0; i < channelP.length; i++) {
			if (channelP[i] > 1.0 || channelP[i] < 0.0)
				throw new AlgorithmException("The " + (i + 1)
						+ ". channel noise propability must be in [0,1]");
			ps += channelP[i];
		}

		if (ps > 1.0)
			throw new AlgorithmException("Invalid channel noise propabilities");

		double p1 = channelP[0] * globalP;
		double p2 = p1 + channelP[1] * globalP;
		double p3 = p2 + channelP[1] * globalP;
		ps = ps * globalP;

		int xDim = input.getXDim();
		int yDim = input.getYDim();
		int bDim = input.getBDim();

		if (channelP.length != bDim)
			throw new AlgorithmException(
					"Incompatible noise propability array length");

		for (int x = 0; x < xDim; x++)
			for (int y = 0; y < yDim; y++) {
				double p = rand.nextDouble();
				double tmp = getImpulse();

				if (globalP < p)
					continue;
				else if (p3 < p) {
					output.setPixelXYBDouble(x, y, 0, tmp);
					output.setPixelXYBDouble(x, y, 1, tmp);
					output.setPixelXYBDouble(x, y, 2, tmp);
				} else if (p2 < p) {
					output.setPixelXYBDouble(x, y, 2, tmp);
				} else if (p1 < p) {
					output.setPixelXYBDouble(x, y, 1, tmp);
				} else {
					output.setPixelXYBDouble(x, y, 0, tmp);
				}
			}
	}

	private double getImpulse() {
		if (rand.nextDouble() < 0.5)
			return rand.nextDouble() * (1.0 - PEAK) + PEAK;
		else
			return rand.nextDouble() * (1.0 - PEAK);
	}
	
	/**
	 * Multichannel impulse noise model (3 bands image)
	 * 
	 * from : Adaptive fuzzy systems for multichannel signal processing Plataniotis &
	 * Venetsanopoulos IEEE 1999
	 * 
	 * @param input Input image
	 * @param globalP Global noise probability
	 * @param channelP Per channel noise probability
	 * @return Noisy version of input image
	 */
	public static Image exec(Image input, double globalP, double [] channelP)
	{
		return (Image) new ColourImpulse().process(input,globalP,channelP);
	}

}
