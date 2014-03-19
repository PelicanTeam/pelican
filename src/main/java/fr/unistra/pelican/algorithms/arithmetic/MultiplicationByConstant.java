package fr.unistra.pelican.algorithms.arithmetic;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;

/**
 * Channel-wise multiplication with a constant. Operates on double precision
 * 
 * @author  Abdullah
 */
public class MultiplicationByConstant extends Algorithm
{
	/**
	 * input image
	 */
	public Image input;
	
	/**
	 * multiplication factors
	 */
	public double[] factors;

	/**
	 * output image
	 */
	public Image output;
	
	/**
	 * Constructor
	 * 
	 */
	public MultiplicationByConstant() {

		super();
		super.inputs = "input,factors";
		super.outputs = "output";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException { 

		output = input.copyImage(false);

		for( int b = 0 ; b < input.getBDim() ; b++ ) { 
		for( int x = 0 ; x < input.getXDim() ; x++ ) { 
		for( int y = 0 ; y < input.getYDim() ; y++ ) { 

					double p = input.getPixelXYBDouble(x,y,b);
					output.setPixelXYBDouble(x,y,b,Math.min(p*Math.abs(factors[b]),1.0));
		}	}	}
	}
	
	/**
	 * Compute the multiplication an image with a array of constants
	 * 
	 * @param input input image
	 * @param factors multiplication factors
	 * @return product
	 * 
	 */
	public static Image exec(Image input, double[] factors) {
		return (Image) new MultiplicationByConstant().process(input, factors);
	}
}
