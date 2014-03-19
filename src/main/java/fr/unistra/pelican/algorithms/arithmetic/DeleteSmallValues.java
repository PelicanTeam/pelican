package fr.unistra.pelican.algorithms.arithmetic;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;


/** 
 * Set pixels with values under a given threshold to minimum value.
 * 
 * @author ?, Benjamin Perret
 */
public class DeleteSmallValues extends Algorithm{
	
	/**
	 * Input image.
	 */
	public Image inputImage;
	
	/**
	 * Threshold
	 */
	public double threshold;

	/**
	 * Result image
	 */
	public Image outputImage;

	/**
  	 * Constructor
  	 *
  	 */
	public DeleteSmallValues() {		
		
		super();		
		super.inputs = "inputImage,threshold";		
		super.outputs = "outputImage";		
		
	}
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException{		
		outputImage = inputImage.copyImage(false);
		outputImage.setMask( inputImage.getMask() );
		int size = inputImage.size();

		for (int i = 0; i < size; ++i)
			if ( inputImage.isPresent(i) ) 
			{
				if (inputImage.getPixelDouble(i) <= threshold)
				{
					outputImage.setPixelDouble(i, 0.0);
				}
				else
				{
					outputImage.setPixelDouble(i, inputImage.getPixelDouble(i));
				}
			}	
	}
	
	/**
	 * Set pixels with values under a given threshold to minimum value.
	 * 
	 * @param inputImage Input image.
	 * 
	 * @param threshold Threshold.
	 * 
	 * @return Result of the algorithm
	 */
	public static Image exec(Image inputImage, double threshold) {
		return (Image) new DeleteSmallValues().process(inputImage,
				threshold);
	}
}
