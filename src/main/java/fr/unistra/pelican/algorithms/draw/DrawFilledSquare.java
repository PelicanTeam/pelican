package fr.unistra.pelican.algorithms.draw;

import java.awt.Color;
import java.awt.Point;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;

/**
 * This class draws a filled square on a 2-D picture. Colored or not
 *
 */

public class DrawFilledSquare extends Algorithm {

	/**
	 * Image to be processed
	 */
	public Image inputImage;
	
	/**
	 * Size of the square
	 */
	public Integer size;
	
	/**
	 * location of the top-left corner  
	 */
	public Point location;
	
	/**
	 * Color of the square
	 */
	public Color color=Color.WHITE;
	
	/**
	 * Resulting picture
	 */
	public Image outputImage;
	
	/**
  	 * Constructor
  	 *
  	 */
	public DrawFilledSquare() {		
		
		super();		
		super.inputs = "inputImage,location,size";
		super.options = "color";
		super.outputs = "outputImage";		
		
	}
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = inputImage.copyImage(true);
		for(int xsize=0;xsize<size;xsize++){
			for(int ysize=0;ysize<size;ysize++){
				if(location.x+xsize<inputImage.getXDim()&&location.x+xsize>=0&&location.y+ysize<inputImage.getYDim()&&location.y+ysize>=0){
						for(int k=0;k<inputImage.getTDim();k++){
							if(inputImage.getBDim()==3)
							{
								outputImage.setPixelXYZTBByte(location.x+xsize,location.y+ysize,0,k,0,color.getRed());
								outputImage.setPixelXYZTBByte(location.x+xsize,location.y+ysize,0,k,1,color.getGreen());
								outputImage.setPixelXYZTBByte(location.x+xsize,location.y+ysize,0,k,2,color.getBlue());
							}
							else
							{
								// Color.getRed() so you can choose a value even if it is not a color image, usefull for grey-level image
								for(int i=0;i<inputImage.getBDim();i++)	outputImage.setPixelXYZTBByte(location.x+xsize,location.y+ysize,0,0,i,color.getRed());
							}	
						}	
					
				}
			}
		}
	}
	
	/**
	 * This method draw a white square on a picture
	 * @param InputImage image to be processed
	 * @param Location square location
	 * @param Size size of the square
	 * @return image with the square drawn
	 */
	public static Image exec(Image InputImage, Point point, Integer Size)
	{
		return (Image) new DrawFilledSquare().process(InputImage,point,Size);
	}
	
	/**
	 * This method draw a colored square on a color picture
	 * @param InputImage image to be processed
	 * @param Locations square location
	 * @param Size size of the square
	 * @param cOlor color of the square
	 * @return image with the square drawn
	 */
	public static Image exec(Image InputImage, Point point, Integer Size, Color cOlor)
	{
		return (Image) new DrawFilledSquare().process(InputImage,point,Size,cOlor);
	}

}
