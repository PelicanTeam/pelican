package fr.unistra.pelican.algorithms.morphology.generalGray;

import java.awt.Point;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.morphology.GrayStructuringElement;


/**
 * Perform a gray erosion with a functional structuring element.
 * Work on a double precision.
 * 
 * @author Benjamin Perret
 *
 */
public class GeneralGrayErosion extends Algorithm {
	/**
	 * Input image
	 */
	public Image inputImage;

	/**
	 * Input functional structuring element
	 */
	public GrayStructuringElement se;

	/**
	 * Result
	 */
	public Image outputImage;

	/**
	 * Constructor
	 */
	public GeneralGrayErosion()
	{
		super.inputs="inputImage,se";
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
	private double getMinGray( int x, int y, int z, int t, int b, Point[] points ) { 

        double min = Double.MAX_VALUE;
        boolean flag = false;
        for(int i = 0; i < points.length; i++){
              int valX = x - se.getCenter().x + points[i].x;
              int valY = y - se.getCenter().y + points[i].y;
          	
                if( valX >= 0 && valX < inputImage.getXDim() 
                	&& valY >=0 && valY <inputImage.getYDim()
					&& inputImage.isPresent( valX,valY,z,t,b ) ) { 
                	double value = inputImage.getPixelDouble(valX,valY,z,t,b);
                	double valueSE = se.getPixelXYDouble(points[i].x, points[i].y);
                	double diff = value-valueSE;
                    if(min > diff)
                    	min = diff;

                    flag = true;

                }
        }
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

		boolean isHere;
		for ( int b = 0 ; b < bDim ; b++ )
		for ( int t = 0 ; t < tDim ; t++ )
		for ( int z = 0 ; z < zDim ; z++ )
		for ( int y = 0 ; y < yDim ; y++ ) 
		for ( int x = 0 ; x < xDim ; x++ ) { 

			isHere = this.inputImage.isPresent( x,y,z,t,b );
			if  ( !isHere ) continue; 

			outputImage.setPixelDouble( x,y,z,t,b, getMinGray( x,y,z,t,b, points ) );
		}
	}

	

	
	/**
	 * Performs a gray erosion with a 2-D functional structuring element
	 * 
	 * @param image
	 *            The input image
	 * @param se
	 *            The gray structuring element used in the morphological
	 *            operation
	 * @return The output image
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Image>  T exec(T image, GrayStructuringElement se) {
		return (T) new GeneralGrayErosion().process(image, se);
	}
	
}
