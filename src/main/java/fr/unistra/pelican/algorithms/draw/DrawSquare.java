package fr.unistra.pelican.algorithms.draw;

import java.awt.Color;
import java.awt.Point;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;

/**
 * This class draws a square on a 2-D picture. Colored or not
 *
 */

public class DrawSquare extends Algorithm {

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
	 * Thickness of the square edge
	 */
	public Integer thickness=1;
	
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
	public DrawSquare() {		
		
		super();		
		super.inputs = "inputImage,location,size";
		super.options = "thickness,color";
		super.outputs = "outputImage";		
		
	}
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = inputImage.copyImage(true);
		for(int xsize=0;xsize<=size;xsize++){
			for(int ysize=0;ysize<=size;ysize++){
				if(location.x+xsize<inputImage.getXDim()&&location.x+xsize>=0&&location.y+ysize<inputImage.getYDim()&&location.y+ysize>=0){
					if((xsize>=0&&xsize<thickness)||(ysize>=0&&ysize<thickness)||(xsize<=size&&xsize>size-thickness)||(ysize<=size&&ysize>size-thickness))
						for(int k=0;k<inputImage.getTDim();k++){
							if(inputImage.getBDim()==3)
							{
								outputImage.setPixelXYZTBByte(location.x+xsize,location.y+ysize,0,k,0,color.getRed());
								outputImage.setPixelXYZTBByte(location.x+xsize,location.y+ysize,0,k,1,color.getGreen());
								outputImage.setPixelXYZTBByte(location.x+xsize,location.y+ysize,0,k,2,color.getBlue());
							}
							else
							{
								for(int i=0;i<inputImage.getBDim();i++)	outputImage.setPixelXYZTBByte(location.x+xsize,location.y+ysize,0,0,i,255);
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
	public static Image exec(Image InputImage, Point point, Integer size)
	{
		return (Image) new DrawSquare().process(InputImage,point,size);
	}
	
	/**
	 * This method draw a white square on a picture
	 * @param InputImage image to be processed
	 * @param Location square location
	 * @param Size size of the square
	 * @param thickness thickness of the square edge
	 * @return image with the square drawn
	 */
	public static Image exec(Image InputImage, Point point, Integer size,Integer thickness)
	{
		return (Image) new DrawSquare().process(InputImage,point,size,thickness);
	}
	
		
	/**
	 * This method draw a colored square on a color picture
	 * @param InputImage image to be processed
	 * @param Locations square location
	 * @param Size size of the square
	 * @param thickness thickness of the square edge
	 * @param cOlor color of the square
	 * @return image with the square drawn
	 */
	public static Image exec(Image InputImage, Point point, Integer size, Integer thickness, Color cOlor)
	{
		return (Image) new DrawSquare().process(InputImage,point,size,thickness,cOlor);
	}

}
