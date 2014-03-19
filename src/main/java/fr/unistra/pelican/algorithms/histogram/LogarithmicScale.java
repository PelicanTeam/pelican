package fr.unistra.pelican.algorithms.histogram;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;

/**
 * Performs a logarithmic scale of the input...special for fourier...
 * 
 * @author
 */
public class LogarithmicScale extends Algorithm {

	/**
	 * Input parameter.
	 */
	public Image inputImage;

	/**
	 * Output parameter.
	 */
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public LogarithmicScale() {

		super();
		super.inputs = "inputImage";
		super.outputs = "outputImage";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = inputImage.copyImage(true);

		for (int i = 0; i < outputImage.size(); i++)
			outputImage
					.setPixelDouble(i, outputImage.getPixelDouble(i) * 10000);

		double max = Double.MIN_VALUE;

		for (int i = 0; i < outputImage.size(); i++) {
			double d = outputImage.getPixelDouble(i);
			if (max < d)
				max = d;
		}

		double c = 255 / Math.log(1 + max);

		for (int i = 0; i < outputImage.size(); i++)
			outputImage.setPixelDouble(i, c
					* Math.log(1 + outputImage.getPixelDouble(i)));
	}
	
	/**
	 * Performs a logarithmic scale of the input.
	 * 
	 * @param inputImage Image to be scaled.
	 * @return the scaled image.
	 */
	public static Image exec(Image inputImage) {
		return (Image) new LogarithmicScale().process(inputImage);
	}
}
