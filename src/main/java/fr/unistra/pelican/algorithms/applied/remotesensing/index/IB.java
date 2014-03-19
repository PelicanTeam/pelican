package fr.unistra.pelican.algorithms.applied.remotesensing.index;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;

/**
 * This class performs Brightness Index on Satellite picture.
 * @author Jonathan Weber, Lefevre
 */

public class IB extends Algorithm {

	
	/**
	 * Image to be computed
	 */
	public Image inputImage;

	/**
	 * index of the red band
	 */
	public int R;
	
	/**
	 * index of infrared band
	 */
	public int IR;

	/**
	 * Resulting picture
	 */
	public Image outputImage;

	
	/**
  	 * Constructor
  	 *
  	 */
	public IB() {		
		super.inputs = "inputImage,R,IR";		
		super.outputs = "outputImage";		
	}
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException 
	{
		Image r=inputImage.getImage4D(R, Image.B);
		Image ir=inputImage.getImage4D(IR, Image.B);
		outputImage=r.copyImage(false);
		for (int p=0;p<outputImage.size();p++)
			outputImage.setPixelDouble(p, 0.5*Math.sqrt(r.getPixelDouble(p)*r.getPixelDouble(p)+ir.getPixelDouble(p)*ir.getPixelDouble(p)));
	}
	
	/**
	 * This method applies the BI on a satellite picture
	 * @param inputImage Satellite picture
	 * @return BI band
	 */
	public static Image exec(Image inputImage,Integer R, Integer IR)
	{
		return (Image) new IB().process(inputImage,R,IR);
	}

}
