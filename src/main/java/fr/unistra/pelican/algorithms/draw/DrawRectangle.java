package fr.unistra.pelican.algorithms.draw;


import java.awt.Color;
import java.awt.Point;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;

/**
 * This class draws a rectangle on a 2-D picture. Colored or not
 *
 */

public class DrawRectangle extends Algorithm {

	/**
	 * Image to be processed
	 */
	public Image inputImage;
	
	/**
	 * location of the left-top corner  
	 */
	public Point leftTop;
	
	/**
	 * location of the right-bottom corner  
	 */
	public Point rightBottom;
	
	/**
	 * thickness of the rectangle edges
	 */
	public int thickness=1;
	
	/**
	 * Color of the square
	 */
	public Color color=Color.WHITE;
	
	/**
	 * Unsafe Mode
	 */
	public boolean unsafe=false;
	
	/**
	 * Resulting picture
	 */
	public Image outputImage;
	
	/**
  	 * Constructor
  	 *
  	 */
	public DrawRectangle() {		
		
		super();		
		super.inputs = "inputImage,leftTop,rightBottom";
		super.options = "thickness,color,unsafe";
		super.outputs = "outputImage";		
		
	}
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		if(unsafe)
			outputImage = inputImage;
		else
			outputImage = inputImage.copyImage(true);
		
		for(int y=leftTop.y;y<=rightBottom.y;y++)
			for(int x=leftTop.x;x<=rightBottom.x;x++)
			{
				if(y<leftTop.y+thickness||y>rightBottom.y-thickness||x<leftTop.x+thickness||x>rightBottom.x-thickness)
					if(x>=0&&x<outputImage.xdim&&y>=0&&y<outputImage.ydim)
					{
						outputImage.setPixelXYZTBByte(x,y,0,0,0,color.getRed());
						outputImage.setPixelXYZTBByte(x,y,0,0,1,color.getGreen());
						outputImage.setPixelXYZTBByte(x,y,0,0,2,color.getBlue());
					}
			}
	}
	
	/**
	 * This method draw a white rectangle on a picture
	 * @param InputImage image to be processed
	 * @param leftTop left-top corner location
	 * @param rightBottom right-bottom corner location
	 * @return image with the rectangle drawn
	 */
	public static Image exec(Image InputImage, Point leftTop, Point rightBottom)
	{
		return (Image) new DrawRectangle().process(InputImage,leftTop,rightBottom);
	}
	
	/**
	 * This method draw a white rectangle on a picture
	 * @param InputImage image to be processed
	 * @param leftTop left-top corner location
	 * @param rightBottom right-bottom corner location
	 * @param thickness thickness of the rectangle edges
	 * @return image with the rectangle drawn
	 */
	public static Image exec(Image InputImage, Point leftTop, Point rightBottom, int thickness)
	{
		return (Image) new DrawRectangle().process(InputImage,leftTop,rightBottom, thickness);
	}
	
		
	/**
	 * This method draw a colored rectangle on a color picture
	 * @param InputImage image to be processed
	 * @param leftTop left-top corner location
	 * @param rightBottom right-bottom corner location
	 * @param cOlor color of the rectangle
	 * @return image with the rectangle drawn
	 */
	public static Image exec(Image InputImage, Point leftTop, Point rightBottom, int thickness, Color cOlor)
	{
		return (Image) new DrawRectangle().process(InputImage,leftTop,rightBottom,thickness, cOlor);
	}
	
	/**
	 * This method draw a colored rectangle on a color picture
	 * @param InputImage image to be processed
	 * @param leftTop left-top corner location
	 * @param rightBottom right-bottom corner location
	 * @param cOlor color of the rectangle
	 * @param unsafe unsafe mode draw on inputimage directly
	 * @return image with the rectangle drawn
	 */
	public static Image exec(Image InputImage, Point leftTop, Point rightBottom, int thickness, Color cOlor, boolean unsafe)
	{
		return (Image) new DrawRectangle().process(InputImage,leftTop,rightBottom,thickness, cOlor, unsafe);
	}

}

