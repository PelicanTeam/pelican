package fr.unistra.pelican.algorithms.histogram;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
/**
 * Impose new minima and maxima to pixel values, 
 * values lower than minima are set to minima 
 * and values higher than maxima are set to maxima.
 * Values between are not changed.
 * 
 * Only work in Byte Precision
 * TODO : other precision
 * 
 * @author Jonathan Weber
 *
 */
public class HistogramDoubleThresholding extends Algorithm {

	
	/**
	 * Image input
	 */
	public Image input;
	
	/**
	 * Minimum pixel value
	 */
	public int min;
	
	/**
	 * Maximum pixel value
	 */
	public int max;
	
	
	/**
	 * Image output
	 */
	public Image output;

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		output = input.copyImage(true);
		for(int i=0;i<output.size();i++)
		{
			if(output.getPixelByte(i)<min)
				output.setPixelByte(i, min);
			else if (output.getPixelByte(i)>max)
				output.setPixelByte(i, max);
		}
	}
	
	/**
	 * Constructor
	 * 
	 */
	public HistogramDoubleThresholding() {

		super();
		super.inputs = "input,min,max";
		super.options = "";
		super.outputs = "output";
		
	}
	
	/**
	 * Impose new minima and maxima to pixel values, 
	 * values lower than minima are set to minima 
	 * and values higher than maxima are set to maxima.
	 * Values between are not changed.
	 * 
	 * Only work in Byte Precision
	 * @param input Image to compute
	 * @param min 	Minimum pixel value
	 * @param max 	Maximum pixel value
	 * @return image thresholded
	 */
	public static Image exec(Image input, int min, int max) {
		return (Image) new HistogramDoubleThresholding().process(input,min,max);
	}

}
