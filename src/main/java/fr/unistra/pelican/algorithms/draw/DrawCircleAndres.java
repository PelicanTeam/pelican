package fr.unistra.pelican.algorithms.draw;

import java.awt.Color;
import java.awt.Point;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;

/**
 * This class draws a circle on a 2D-picture using Andres algorithm. Colored or not
 *
 */
public class DrawCircleAndres extends Algorithm {
	
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
	public DrawCircleAndres(){
		super();
		super.inputs="inputImage,center,size";
		super.options="color";
		super.outputs="outputImage";
	}
	
	public void launch() throws AlgorithmException {
		outputImage=inputImage.copyImage(true);
		for(int t=0; t<inputImage.tdim;t++){
			Integer x=0;
			Integer y=size;
			Integer d = size -1;			
			while(y>=x){
		        tracerPixel( x+center.x , y+center.y , t );
		        tracerPixel( y+center.x , x+center.y , t );
		        tracerPixel( -x+center.x, y+center.y , t );
		        tracerPixel( -y+center.x, x+center.y , t );
		        tracerPixel( x+center.x , -y+center.y, t );
		        tracerPixel( y+center.x , -x+center.y, t );
		        tracerPixel( -x+center.x, -y+center.y, t );
		        tracerPixel( -y+center.x, -x+center.y, t );
		        
		    	if(d>=2*x){
		    		d= d-1*x-1;
		    		x= x+1;
		    	}else if(d<=2*(size-y)){
		    		d=d+2*y-1;
		    		y=y-1;
		    	}else{
		    		d= d+2*(y-x-1);
		    		x= x+1;
		    		y=y-1;
		    	}
			}
		}
	}

	private void tracerPixel(Integer x, Integer y,Integer t){
		
		if(inputImage.getBDim()==3)
		{
			outputImage.setPixelXYZTBByte(x,y,0,t,0,color.getRed());
			outputImage.setPixelXYZTBByte(x,y,0,t,1,color.getGreen());
			outputImage.setPixelXYZTBByte(x,y,0,t,2,color.getBlue());
		}
		else
		{
			for(int i=0;i<inputImage.getBDim();i++)	outputImage.setPixelXYZTBByte(x,y,0,t,i,255);
		}
	}
	
	/**
	 * Draws a circle on a 2D-picture using Andres algorithm
	 * 
	 * @param inputImage image to be processed
	 * @param center center of the circle
	 * @param size size of the circle
	 * @return image with the circle drawn
	 */
	public static Image exec(Image inputImage,Point center,Integer size){
		return (Image) new DrawCircleAndres().process(inputImage,center,size);
	}
	
	/**
	 * Draws a circle on a 2D-picture using Andres algorithm
	 * @param inputImage image to be processed
	 * @param center center of the circle
	 * @param size size of the circle
	 * @param cOlor color of the circle
	 * @return image with the circle drawn
	 */
	public static Image exec(Image inputImage,Point center,Integer size,Color cOlor){
		return (Image) new DrawCircleAndres().process(inputImage,center,size,cOlor);
	}

}
