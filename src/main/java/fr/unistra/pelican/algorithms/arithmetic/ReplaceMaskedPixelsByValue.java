/**
 * 
 */
package fr.unistra.pelican.algorithms.arithmetic;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;

/**
 * Set all pixels marked as masked to the given value
 * <p>
 * The inplace option always you to put result in the input (no new space allocated)
 * @author Benjamin Perret
 *
 */
public class ReplaceMaskedPixelsByValue extends Algorithm {

	/**
	 * Input image
	 */
	public Image inputImage;
	
	
	/**
	 * New value
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
	public ReplaceMaskedPixelsByValue() {
		super.inputs="inputImage,newValue";
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
		else outputImage=inputImage.copyImage(true);

		for(int i=0;i<inputImage.size();i++)
		{
			if(!inputImage.isPresent(i))
				outputImage.setPixelDouble(i, newValue);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends Image> T exec(T inputImage, double newValue, boolean inplace)
	{
		return (T)(new ReplaceMaskedPixelsByValue().process(inputImage,newValue,inplace));
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Image> T exec(T inputImage,  double newValue)
	{
		return (T)(new ReplaceMaskedPixelsByValue().process(inputImage,newValue));
	}

}