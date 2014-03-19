package fr.unistra.pelican.algorithms.applied.remotesensing.index;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.arithmetic.NormalizedDifference;

/**
 * This class compute an Normalized Difference of Vegetation Index on a satellite picture.
 * Be carefull, the NDVI values usually in [-1,1] are here normalized to [0,1].
 * @author Jonathan Weber
 *
 */

public class NDVI extends Algorithm{

	
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
	public NDVI() {		
		super.inputs = "inputImage,R,IR";		
		super.outputs = "outputImage";		
	}
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException 
	{
		outputImage=(Image) new NormalizedDifference().process(inputImage.getImage4D(IR, Image.B), inputImage.getImage4D(R, Image.B));
	}
	
	/**
	 * This method compute a NDVI band.
	 * @param inputImage Satellite picture
	 * @param R index of red band
	 * @param IR index of infrared band
	 * @return Image NDVI band
	 */
	public static Image exec(Image inputImage, Integer R, Integer IR)
	{
		return (Image) new NDVI().process(inputImage,R,IR);
	}
}
