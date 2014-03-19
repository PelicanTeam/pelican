package fr.unistra.pelican.algorithms.statistics;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;

/**
 * This class computes the peak signal to noise ratio between two multiband images
 * 
 * TODO: check the definition of PSNR
 * 
 * @author Abdullah
 */
public class PSNR extends Algorithm
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
	 * the resulting PSNR
	 */
	public Double output;
	
	/**
	 * This class computes the peak signal to noise ratio between two multiband images
	 * @param original the original image
	 * @param filtered the filtered image
	 * @return the PSNR value
	 */
	public static Double exec(Image original,Image filtered)
	{
		return (Double) new PSNR().process(original,filtered);
	}

	/**
	 * Constructor
	 * 
	 */
	public PSNR() {

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
		double mse = 0.0;

		try {
			mse = (Double) new MSE().process(original, filtered);
		} catch (PelicanException e) {
			e.printStackTrace();
		}

		output = new Double(20 * Math.log10(255 / Math.sqrt(mse)));
	}
}
