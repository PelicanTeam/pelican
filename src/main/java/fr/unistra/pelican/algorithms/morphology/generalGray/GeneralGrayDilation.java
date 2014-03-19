package fr.unistra.pelican.algorithms.morphology.generalGray;

import java.awt.Point;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.morphology.GrayStructuringElement;

/**
 * This class performs a gray dilation with a 2-D gray structuring element
 * 
 * @author
 */
public class GeneralGrayDilation extends Algorithm {

	/**
	 * The input image
	 */
	public Image inputImage;

	/**
	 * The gray structuring element used in the morphological operation
	 */
	public GrayStructuringElement se;

	/**
	 * The output image
	 */
	public Image outputImage;

	/**
	 * Default constructor
	 */
	public GeneralGrayDilation() {
		super.inputs = "inputImage,se";
		super.outputs = "outputImage";
		
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

			outputImage.setPixelDouble( x,y,z,t,b, getMaxGray( x,y,z,t,b,points ) );
		}
	}
	/*
	 * Return the max value under a flat structuring element.
	 */
	private double getMaxGray(int x,int y,int z,int t,int b,Point[] points)
	{	
		double max =  Double.NEGATIVE_INFINITY;
		boolean flag = false;
		
		for (int i = 0; i < points.length; i++) {
			int valX = x - se.getCenter().x + points[i].x;
			int valY = y - se.getCenter().y + points[i].y;
			
			if ( valX >= 0 && valX < inputImage.getXDim() 
				&& valY >= 0 && valY < inputImage.getYDim() 
				&& inputImage.isPresent( valX,valY,z,t,b ) ) { 

				double value = inputImage.getPixelDouble(valX,valY,z,t,b);
            	double valueSE = se.getPixelXYDouble(points[i].x, points[i].y);
            	double diff = value+valueSE;
            	
                if(max < diff)
                	max = diff;

				flag = true;
			}
		}
		// FIXME: Strange, if nothing is under the se, what is the right way?
		return (flag == true) ? max : inputImage.getPixelDouble(x, y, z, t, b);
	}

	/**
	 * Performs a gray dilation with a 2-D functional structuring element
	 * 
	 * @param image
	 *            The input image
	 * @param se
	 *            The gray structuring element used in the morphological
	 *            operation
	 * @return The output image
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Image>  T  exec(T image, GrayStructuringElement se) {
		return (T) new GeneralGrayDilation().process(image, se);
	}
	
}
