package fr.unistra.pelican.algorithms.draw;

import java.awt.Color;
import java.awt.Point;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;

/**
 * This class draws a circle on a 2D-picture. Colored or not
 *
 */
public class DrawCircle extends Algorithm {
	
	/**
	 * Image to be processed
	 */
	public Image inputImage;
	
	/**
	 * Center of the circle
	 */
	public Point center;
	
	/**
	 * Size of the circle
	 */
	public Integer size;
	
	/**
	 * Color of the circle
	 */
	public Color color=Color.WHITE;
	
	/**
	 * Resulting picture
	 */
	public Image outputImage;
	
	
	/**
	 * Constructor
	 */
	public DrawCircle(){
		super();
		super.inputs="inputImage,center,size";
		super.options="color";
		super.outputs="outputImage";
	}
	
	public void launch() throws AlgorithmException {
		if (inputImage.getZDim()!=1) throw new AlgorithmException("This is not a 2D picture");
		outputImage=inputImage.copyImage(true);
		for(int k=0; k<inputImage.tdim;k++){
			for(int xsize=-size;xsize<=size;xsize++){
				for(int ysize=-size;ysize<=size;ysize++){
					if(xsize+center.x>=0 && xsize+center.x<inputImage.xdim && ysize+center.y>=0 && ysize+center.y < inputImage.ydim) {
						if(xsize*xsize+ysize*ysize-size*size>=-(size-1) && xsize*xsize+ysize*ysize-size*size<=(size-1)){
							if(inputImage.getBDim()==3)
							{
								outputImage.setPixelXYZTBByte(center.x+xsize,center.y+ysize,0,k,0,color.getRed());
								outputImage.setPixelXYZTBByte(center.x+xsize,center.y+ysize,0,k,1,color.getGreen());
								outputImage.setPixelXYZTBByte(center.x+xsize,center.y+ysize,0,k,2,color.getBlue());
							}
							else
							{
								for(int i=0;i<inputImage.getBDim();i++)	outputImage.setPixelXYZTBByte(center.x+xsize,center.y+ysize,0,k,i,255);
							}
						}
					}
				}
			}
		}

	}
	
	/**
	 * Draws a circle on a 2D-picture
	 * 
	 * @param inputImage image to be processed
	 * @param center center of the circle
	 * @param size size of the circle
	 * @return image with the circle drawn
	 */
	public static Image exec(Image inputImage,Point center,Integer size){
		return (Image) new DrawCircle().process(inputImage,center,size);
	}
	
	/**
	 * Draws a circle on a 2D-picture
	 * @param inputImage image to be processed
	 * @param center center of the circle
	 * @param size size of the circle
	 * @param cOlor color of the circle
	 * @return image with the circle drawn
	 */
	public static Image exec(Image inputImage,Point center,Integer size,Color cOlor){
		return (Image) new DrawCircle().process(inputImage,center,size,cOlor);
	}

}
