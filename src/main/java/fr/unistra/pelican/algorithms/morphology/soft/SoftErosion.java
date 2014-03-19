package fr.unistra.pelican.algorithms.morphology.soft;




import java.awt.Point;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.SortedBag;
import fr.unistra.pelican.util.morphology.GrayIntStructuringElement;


/**
 * Perform a soft erosion with a flat structuring element and a weight map.
 * Flat SE and weight map are represented with a GrayIntStructuringElement.
 * The flat structuring element is encoded as the value greater than 0.
 * Greater value than 0 gives the weight of the pixel.
 * 
 * The threshold is used for rank operation.
 * 
 * Example: consider image {1,5,2} and SE {1,2,3} with threshold 2
 * We want to compute the value of pixel at the center:
 * We take the 2th lower element of the sorted bag composed of one 1, two 5 and three 2: {1,2,2,2,5,5} 
 * that is 2!
 * 
 * If all weights are set to 1, this is equivalent to a simple rank filter.
 * 
 * Work on double precision.
 * @author Benjamin Perret
 */
public class SoftErosion extends Algorithm {
	/**
	 * Input image
	 */
	public Image inputImage;
	
	/**
	 * Rank order
	 */
	public int threshold;
	
	/**
	 * Gray Structuring Element, value are treated as integer
	 */
	public GrayIntStructuringElement se;

	/**
	 * Result
	 */
	public Image outputImage;

	/**
	 * Default constructor
	 */
	public SoftErosion()
	{
		super.inputs="inputImage,se,threshold";
		super.outputs="outputImage";
		
	}
	
	/** Return the min value under a flat structuring element.
	 * @param x
	 * @param y
	 * @param z
	 * @param t
	 * @param b
	 * @return
	 */
	private double getMinGray(int x,int y, int z,int t,int b, Point [] points)
{
        double min = 0;
        boolean flag = false;
       
        SortedBag sb=new SortedBag();
        
        for (int i = 0; i < points.length; i++) {
			int valX = x - se.getCenter().x + points[i].x;
			int valY = y - se.getCenter().y + points[i].y;
        	if(		valX>=0 && valX < inputImage.getXDim() 
        		 && valY>=0 && valY < inputImage.getYDim() 
    			 && inputImage.isPresent( valX,valY,z,t,b ) ) {

       			sb.add( inputImage.getPixelDouble( valX,valY,z,t,b), 
       					se.getPixelXYInt(points[i].x,points[i].y));
       			flag = true;
   			}     
        }
       
      
         min=sb.getElementAt(Math.max(sb.size()-threshold, 0)).doubleValue();
        // FIXME: Strange, if nothing is under the se, what is the right way?
        return (flag == true)? min : inputImage.getPixelDouble(x,y,z,t,b);
    }
	
		
	
	
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = inputImage.copyImage(false);
		
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
				outputImage.setPixelDouble(x,y,z,t,b,getMinGray(x,y,z,t,b,points));
	}


	public static void main(String [] args)
	{
		int size=31;
		GrayIntStructuringElement se= new GrayIntStructuringElement(size,size,new Point(size/2,size/2));
		se.fill(0.0);
		for (int i =0;i<size;i++)
		{
			se.setPixelXYInt(i,size/2,size/2-Math.abs(size/2-i));
			se.setPixelXYInt(size/2,i,size/2-Math.abs(size/2-i));
		}
		
		Viewer2D.exec(se,"SE");
		
		
		Image im=ImageLoader.exec("samples/detection_test.png");
		Viewer2D.exec(im,"Original");
		Viewer2D.exec(SoftErosion.exec(im, se, 1),"Erosion cross 31*31, threshold=1, normal erosion");
		Viewer2D.exec(SoftErosion.exec(im, se, 20),"Erosion cross 31*31, threshold=20");
		Viewer2D.exec(SoftErosion.exec(im, se, 80),"Erosion cross 31*31, threshold=40");
		
		
		
	}
	
	
	
	/**
	 * Perform a soft erosion with a flat structuring element and a weight map.
	 * Flat SE and weight map are represented with a GrayIntStructuringElement.
	 * The flat structuring element is encoded as the value greater than 0.
	 * Greater value than 0 gives the weight of the pixel.
	 * 
	 * The threshold is used for rank operation.
	 * 
	 * Example: consider image {1,5,2} and SE {1,2,3} with threshold 2
	 * We want to compute the value of pixel at the center:
	 * We take the 2th lower element of the sorted bag composed of one 1, two 5 and three 2: {1,2,2,2,5,5} 
	 * that is 2!
	 * 
	 * If all weights are set to 1, this is equivalent to a simple rank filter.
	 * 
	 * Work on double precision.
	 * @param image
	 *            The input image
	 * @param se
	 *            The gray structuring element with weighted position (int precision)
	 * @param thresold
	 * 			  Rank order threshold
	 * @return The output image
	 */
	public static Image exec(Image image, GrayIntStructuringElement se, int threshold) {
		return (Image) new SoftErosion().process(image, se,threshold);
	}
}