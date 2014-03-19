package fr.unistra.pelican.algorithms.statistics;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;

/**
 * This class computes the signal to noise ratio between two multiband images
 * 
 * @author Abdullah
 */
public class SNR extends Algorithm
{
	/**
	 * the original image
	 */
	public Image original;

	/**
	 * the filtered image
	 */
	public Image filtered;

	/**
	 * the resulting SNR
	 */
	public Double output;
	
	/**
	 * This method computes the signal to noise ratio between two multiband images
	 * @param original The original image
	 * @param filtered the filtered image
	 * @return the SNR
	 */
	public static Double exec(Image original,Image filtered)
	{
		return (Double) new SNR().process(original,filtered);
	}

	/**
	 * Constructor
	 * 
	 */
	public SNR() {

		super();
		super.inputs = "original,filtered";
		super.outputs = "output";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		double rms1 = 0.0, rms2 = 0.0;

		try {
			rms1 = (Double) new RMS().process(original);
			rms2 = (Double) new RMS().process(filtered);
		} catch (PelicanException e) {
			e.printStackTrace();
		}

		output = new Double(20 * Math.log10(rms1 / rms2));
	}
}
