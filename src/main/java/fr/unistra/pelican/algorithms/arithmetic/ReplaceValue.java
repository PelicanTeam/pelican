/**
 * 
 */
package fr.unistra.pelican.algorithms.arithmetic;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;

/**
 * A very simple algorithm to replace a value by a new one.
 * For every pixel p
 * O[p] = newValue if I[p]== oldValue, I[p] otherwise
 * 
 * @author Benjamin Perret
 *
 */
public class ReplaceValue extends Algorithm {

	/**
	 * Input image
	 */
	public Image inputImage;
	
	/**
	 * Value to replace
	 */
	public double oldValue;
	
	/**
	 * New value to replace oldValue
	 */
	public double newValue;
	
	/**
	 * Result
	 */
	public Image outputImage;
	
	/**
	 * Can we use inputImage to store result (no new space allocated)?
	 */
	public boolean inplace=false;
	
	/**
	 * 
	 */
	public ReplaceValue() {
		super.inputs="inputImage,oldValue,newValue";
		super.options="inplace";
		super.outputs="outputImage";
	}

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	@Override
	public void launch() throws AlgorithmException {
		if(inplace)
			outputImage=inputImage;
		else outputImage=inputImage.copyImage(false);

		for(int i=0;i<inputImage.size();i++)
		{
			double v=inputImage.getPixelDouble(i);
			if(v==oldValue)
				outputImage.setPixelDouble(i, newValue);
			else outputImage.setPixelDouble(i, v);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends Image> T exec(T inputImage, double oldValue, double newValue, boolean inplace)
	{
		return (T)(new ReplaceValue().process(inputImage,oldValue,newValue,inplace));
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Image> T exec(T inputImage, double oldValue, double newValue)
	{
		return (T)(new ReplaceValue().process(inputImage,oldValue,newValue));
	}

}
