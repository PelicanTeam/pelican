package fr.unistra.pelican.algorithms.applied.remotesensing;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;

/** 
 * This class adds two bands to the image, one for NDVI and one for IBS
 *
 * @author ?, Jonathan Weber
 */


public class EnhanceWithNDVIAndIBS extends Algorithm {
	
	/**
	 * Image to be processed
	 */
	public Image inputImage;
	
	/**
	 * Resulting picture
	 */
	public Image outputImage;

	/**
  	 * Constructor
  	 *
  	 */
	public EnhanceWithNDVIAndIBS() {		
		
		super();		
		super.inputs = "inputImage";		
		super.outputs = "outputImage";		
		
	}
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		int xDim = inputImage.getXDim();
		int yDim = inputImage.getYDim();
		int bDim = inputImage.getBDim() + 2;
		
		outputImage = new ByteImage(xDim, yDim, 1, 1, bDim);
		
		// Recopie et ajout des bandes
		// NDVI = (NIR - red) / (NIR + red)
		// Math.sqrt((Math.pow(rouge,2) + Math.pow(pir,2)) / 2);
		for(int x = 0; x < xDim; x++)
			for(int y = 0; y < yDim; y++) {
				for(int b = 0; b < 4; b++)
					outputImage.setPixelXYBByte(x, y, b, 
							inputImage.getPixelXYBByte(x, y, b));
				
				double rouge = inputImage.getPixelXYBByte(x, y, 2);
				double pir = inputImage.getPixelXYBByte(x, y, 3);
				
				double ndvi = ((pir - rouge) / (rouge + pir) + 1.0) * 128.0;
				
				outputImage.setPixelXYBByte(x, y, 4, (int)ndvi);
				
				double ibs = Math.sqrt((Math.pow(rouge,2) + Math.pow(pir,2)) / 2);
				
				outputImage.setPixelXYBByte(x, y, 5, (int)ibs);				
			}
	}
	
	/**
	 * This method adds two bands to the image, one for NDVI and one for IBS
	 * @param inputImage Satellite picture
	 * @return Image Satellite picture with NDVI and IBS bands
	 */
	public static Image exec(Image inputImage) {
		return (Image)new EnhanceWithNDVIAndIBS().process(inputImage);
	}
}
