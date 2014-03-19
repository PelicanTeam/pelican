package fr.unistra.pelican.algorithms.spatial;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.Tools;
import fr.unistra.pelican.util.morphology.GrayStructuringElement;

/**
 * Standard gaussian filter..applied with a 2D kernel. Not optimized.
 * 
 * TODO : use two 1D kernels.
 * 
 * @author Abdullah
 */
public class GaussianFilter extends Algorithm {
	
	/**
	 * Input image
	 */
	public Image input;
	
	/**
	 * Standard deviation of the gaussian
	 */
	public double sigma;

	/**
	 * Ouput image
	 */
	public Image output;

	
	/**
	 * Size of the structuring element
	 */
	public int size = 5;

	/**
	 * Structuring element used for the filter
	 */
	public GrayStructuringElement kernel;

	/**
	 * Constructor
	 * 
	 */
	public GaussianFilter() {
		super.inputs = "input,sigma";
		super.options="size";
		super.outputs = "output";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		kernel = GrayStructuringElement.createSquareFlatStructuringElement(size);

		for (int x = 0; x < kernel.getXDim(); x++) {
			for (int y = 0; y < kernel.getYDim(); y++) {
				int _x = x - kernel.getCenter().y;
				int _y = y - kernel.getCenter().x;

				kernel.setPixelXYDouble(y, x, Tools.Gaussian2D(_x, _y, sigma));
			}
		}

		output = (Image) new Convolution().process(input, kernel);
	}
	
	/**
	 * Standard gaussian filter..applied with a 2D kernel.
	 * @param input Input image
	 * @param sigma Standard deviation of the gaussian
	 * @return Ouput image
	 */
	public static Image exec(Image input, double sigma) {
		return (Image) new GaussianFilter().process(input,sigma);
	}
	
	public static Image exec(Image input, double sigma,int size) {
		return (Image) new GaussianFilter().process(input,sigma,size);
	}

}
