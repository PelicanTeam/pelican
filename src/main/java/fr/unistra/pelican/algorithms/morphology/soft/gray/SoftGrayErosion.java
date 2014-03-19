package fr.unistra.pelican.algorithms.morphology.soft.gray;




import java.awt.Point;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.SortedBag;
import fr.unistra.pelican.util.morphology.GrayIntStructuringElement;
import fr.unistra.pelican.util.morphology.GrayStructuringElement;


/**
 * Perform a soft erosion with a gray structuring element and a weight map.
 * 
 * The threshold is used for rank operation.
 * 
 * Example: consider image {1,5,2}, SE {0,2,0} and weight map {1,2,1} with threshold 2
 * We want to compute the value of pixel at the center:
 * We take the 2th lower element of the sorted bag composed of one (1-0), two (5-2) and one (2-0): {1,2,3,3} 
 * that is 2!
 * 
 * Work on double precision.
 * 
 * @author Benjamin Perret
 */
public class SoftGrayErosion extends Algorithm {
	/**
	 * Input Image
	 */
	private Image inputImage;
	
	/**
	 * Rank Order
	 */
	private int seuil;
	
	/**
	 * Weight map
	 */
	private GrayIntStructuringElement se;
	
	/**
	 * Gray Structuring Element
	 */
	private GrayStructuringElement se2;
	
	/**
	 * Result
	 */
	private DoubleImage outputImage;

	
				
	
	private double getMinGray(int x,int y, int z,int t,int b, Point [] points)
	{
	        //double min = 0;
	        boolean flag = false;
	       
	        SortedBag sb=new SortedBag();
	        
	        for (int i = 0; i < points.length; i++) {

				int valX = x - se.getCenter().x + points[i].x;
				int valY = y - se.getCenter().y + points[i].y;
	        	if( 	valX >= 0 && valX < inputImage.getXDim() 
	        		 && valY >= 0 && valY < inputImage.getYDim() 
	        		 && inputImage.isPresent( valX,valY,z,t,b ) ) { 

	        		sb.add( 
	        				inputImage.getPixelDouble( valX,valY,z,t,b )
	        				- se2.getPixelXYDouble(points[i].x, points[i].y), 
	       					se.getPixelXYInt(points[i].x,points[i].y) );
	       			flag = true;
	       		}
	        }

	        // FIXME: Strange, if nothing is under the se, what is the right way?
	         return (flag == true)? sb.getElementAt(Math.min(seuil-1, sb.size()-1)).doubleValue() : 
	        	 inputImage.getPixelDouble(x,y,z,t,b);
	    }
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = new DoubleImage(inputImage,false);
		
		int xDim = inputImage.getXDim();
		int yDim = inputImage.getYDim();
		int tDim = inputImage.getTDim();
		int bDim = inputImage.getBDim();
		int zDim = inputImage.getZDim();
		
		int size = 0;
		for(int i = 0; i < se.getXDim(); i++)
	            for(int j = 0 ; j < se.getYDim(); j++)
	            	if(se.isValue(i,j) == true) size++;
		
		Point[] points = new Point[size];
		
		int k = 0;
		for(int i = 0; i < se.getXDim(); i++){
	            for(int j = 0 ; j < se.getYDim(); j++){
	            	if(se.isValue(i,j) == true) points[k++] = new Point(i,j);
	            }
		}
		
		for ( int b = 0 ; b < bDim ; b++ )
			for ( int t = 0 ; t < tDim ; t++ )
			for ( int z = 0 ; z < zDim ; z++ )
			for ( int x = 0 ; x < xDim ; x++ )
			for ( int y = 0 ; y < yDim ; y++ )
				if ( inputImage.isPresent( x,y,z,t,b ) ) 
					outputImage.setPixelDouble( x,y,z,t,b, getMinGray( x,y,z,t,b, points ) );

		
	}

	/**
	 * Perform a soft erosion with a gray structuring element and a weight map.
	 * 
	 * The threshold is used for rank operation.
	 * 
	 * Example: consider image {1,5,2}, SE {0,2,0} and weight map {1,2,1} with threshold 2
	 * We want to compute the value of pixel at the center:
	 * We take the 2th lower element of the sorted bag composed of one (1-0), two (5-2) and one (2-0): {1,2,3,3} 
	 * that is 2!
	 * 
	 * @param image
	 *            The input image
	 * @param se
	 *            The gray structuring element 
	 *            
	 * @param se2
	 *            The weight map 
	 *              
	 * @param thresold
	 * 			  Rank order threshold
	 * @return The output image
	 */
	public static Image exec(Image image, GrayStructuringElement se,GrayStructuringElement se2, int threshold) {
		return (Image) new SoftGrayErosion().process(image, se,se2,threshold);
	}
	
	
}