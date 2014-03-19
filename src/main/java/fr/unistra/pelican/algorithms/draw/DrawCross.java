package fr.unistra.pelican.algorithms.draw;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;

/**
 * This class draw a cross on a 2-D picture. Colored or not.
 * 
 * @author Jonathan Weber
 *
 */

public class DrawCross extends Algorithm {
	/**
	 * Image to be processed
	 */
	public Image inputImage;
	
	/**
	 * Size of the cross
	 */
	public Integer size;
	
		
	/**
	 * Array list of crosses locations
	 */
	public ArrayList<Point> locations;
	
	/**
	 * Color of the cross
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
	public DrawCross() {		
		
		super();		
		super.inputs = "inputImage,locations,size";
		super.options = "color";
		super.outputs = "outputImage";		
		
	}
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = inputImage.copyImage(true);
			for(int nbc=0;nbc<locations.size();nbc++)
				for(int xsize=-size;xsize<=size;xsize++)
					for(int ysize=-size;ysize<=size;ysize++)
						if(locations.get(nbc).x+xsize<inputImage.getXDim()&&locations.get(nbc).x+xsize>0&&locations.get(nbc).y+ysize<inputImage.getYDim()&&locations.get(nbc).y+ysize>0)
							if(xsize==0||ysize==0)
								if(inputImage.getBDim()==3)
								{
									outputImage.setPixelXYZTBByte(locations.get(nbc).x+xsize,locations.get(nbc).y+ysize,0,0,0,color.getRed());
									outputImage.setPixelXYZTBByte(locations.get(nbc).x+xsize,locations.get(nbc).y+ysize,0,0,1,color.getGreen());
									outputImage.setPixelXYZTBByte(locations.get(nbc).x+xsize,locations.get(nbc).y+ysize,0,0,2,color.getBlue());
								}
								else
								{
									for(int i=0;i<inputImage.getBDim();i++)
										outputImage.setPixelXYZTBByte(locations.get(nbc).x+xsize,locations.get(nbc).y+ysize,0,0,i,255);
								}		
	}
	
	/**
	 * This method draw a white cross on a picture
	 * @param InputImage image to be processed
	 * @param Locations Array list of crosses locations
	 * @param Size size of the cross
	 * @return image with the cross drawn
	 */
	public static Image exec(Image InputImage, ArrayList<Point> Locations, Integer Size)
	{
		return (Image) new DrawCross().process(InputImage,Locations,Size);
	}
	
	/**
	 * This method draw a colored cross on a color picture
	 * @param InputImage image to be processed
	 * @param Locations Array list of crosses locations
	 * @param Size size of the cross
	 * @param cOlor color of the cross
	 * @return image with the cross drawn
	 */
	public static Image exec(Image InputImage, ArrayList<Point> Locations, Integer Size, Color cOlor)
	{
		return (Image) new DrawCross().process(InputImage,Locations,Size,cOlor);
	}

}
