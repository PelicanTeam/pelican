package fr.unistra.pelican.algorithms.applied.remotesensing.index;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.arithmetic.NormalizedDifference;

/**
 * This class compute a Normalized Difference of Water Index band.
 * @author Jonathan Weber
 *
 */

public class NDWI extends Algorithm {

	
	/**
	 * Image to be computed
	 */
	public Image inputImage;

	
	/**
	 * index of near-infrared band
	 */
	public int NIR;
	
	/**
	 * index of short-wave infrared band
	 */
	public int SWIR;
	
	/**
	 * Resulting picture
	 */
	public Image outputImage;

	
	/**
  	 * Constructor
  	 *
  	 */
	public NDWI() {		
		
		super();		
		super.inputs = "inputImage,NIR,SWIR";		
		super.outputs = "outputImage";		
		
	}
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException 
	{
		outputImage=(Image) new NormalizedDifference().process(inputImage.getImage4D(NIR, Image.B), inputImage.getImage4D(SWIR, Image.B));
	}
	
	/**
	 * This method computes a NDWI band.
	 * @param inputImage Satellite picture
	 * @param NIR index of near-infrared band
	 * @param SWIR index of short-wave infrared band
	 * @return Image NDWI band
	 */
	public static Image exec(Image inputImage, Integer NIR, Integer SWIR)
	{
		return (Image) new NDWI().process(inputImage,NIR,SWIR);
	}
}
