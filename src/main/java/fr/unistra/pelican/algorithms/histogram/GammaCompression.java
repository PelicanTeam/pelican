/**
 * 
 */
package fr.unistra.pelican.algorithms.histogram;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.colour.GammaCompressionModel;
import fr.unistra.pelican.util.colour.GammaCompressionModel.Band;

/**
 * Performs gamma compression to prepare an image with linear dynamic range for display.
 * 
 * Assumes image is either grayscale or RGB.
 * 
 * @author Benjamin Perret
 *
 * @TODO 
 *
 */
public class GammaCompression extends Algorithm {

	
	
	/**
	 * Input image
	 */
	public Image image;
	
	/**
	 * Gamma compressed result
	 */
	public Image result;
	
	/**
	 * Compression model to use
	 */
	public GammaCompressionModel model;
	
	
	
	/**
	 * 
	 */
	private GammaCompression() {
		super();
		super.inputs="image,model";
		super.outputs="result";
		
	}

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	@Override
	public void launch() throws AlgorithmException {
		result=image.copyImage(false);
		if(image.bdim==3)
		{
			for(int i=0;i<image.size();)
			{
				result.setPixelDouble(i, model.compress(image.getPixelDouble(i++), Band.R));
				result.setPixelDouble(i, model.compress(image.getPixelDouble(i++), Band.G));
				result.setPixelDouble(i, model.compress(image.getPixelDouble(i++), Band.B));
			}
		}else{
			for(int i=0;i<image.size();i++)
				result.setPixelDouble(i, model.compress(image.getPixelDouble(i), Band.UNKNOWN));
		}

	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Image> T exec(T image, GammaCompressionModel model)
	{
		return (T)new GammaCompression().process(image,model);
	}

}
