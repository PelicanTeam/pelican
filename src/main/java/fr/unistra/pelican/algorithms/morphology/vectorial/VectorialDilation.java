package fr.unistra.pelican.algorithms.morphology.vectorial;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.Point4D;
import fr.unistra.pelican.util.vectorial.orders.VectorialOrdering;

/**
 * This class performs a vectorial dilation with the given structuring element and vector ordering on a double
 * precision
 * 
 * @author Abdullah
 * 
 */
public class VectorialDilation extends Algorithm
{
	/**
	 * the input image
	 */
	public Image inputImage;

	/**
	 * the output image
	 */
	public Image outputImage;

	/**
	 * the structuring element
	 */
	public BooleanImage se;

	/**
	 * the vector ordering
	 */
	public VectorialOrdering vo;
	
	/**
	 * This method performs a vectorial dilation with the given structuring element and ordering
	 * @param inputImage the input image
	 * @param se the structuring element
	 * @param vo the vector ordering
	 * @return the output image 
	 */
	public static Image exec(Image inputImage,BooleanImage se,VectorialOrdering vo) {
		return (Image) new VectorialDilation().process(inputImage,se,vo);
	}

	/**
	 * Constructor
	 * 
	 */
	public VectorialDilation() {

		super();
		super.inputs = "inputImage,se,vo";
		super.outputs = "outputImage";
		
	}

	private double[] getMaxVector(int x, int y, int z, int t, Point4D[] points) {
		double[][] vectorArray = new double[se.getSum()][];
		int tmp = 0;

		for (int i = 0; i < points.length; i++) {
			int valX = x - se.getCenter().x + points[i].x;
			int valY = y - se.getCenter().y + points[i].y;

			if (	valX >= 0 && valX < inputImage.getXDim() 
				 && valY >= 0 && valY < inputImage.getYDim() 
				 && inputImage.isPresentXYZT( valX,valY,z,t ) )
				vectorArray[tmp++] = inputImage.getVectorPixelXYZTDouble( valX,valY,z,t );
		}

		// empty spots will occur only in image borders...
		// if the fill policy changes...this must change too.
		if (tmp == 0)
			return inputImage.getVectorPixelXYZTDouble(x, y, z, t);

		else if (tmp < vectorArray.length) {
			double[][] tmpArray = new double[tmp][];

			for (int i = 0; i < tmp; i++)
				tmpArray[i] = vectorArray[i];

			vectorArray = tmpArray;

		}
		return vo.max(vectorArray);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = inputImage.copyImage(false);

		int xDim = inputImage.getXDim();
		int yDim = inputImage.getYDim();
		int tDim = inputImage.getTDim();
		int zDim = inputImage.getZDim();

		Point4D[] points = se.foreground();

		for ( int t = 0 ; t < tDim ; t++ )
		for ( int z = 0 ; z < zDim ; z++ )
		for ( int x = 0 ; x < xDim ; x++ )
		for ( int y = 0 ; y < yDim ; y++ )
			if ( inputImage.isPresentXYZT( x,y,z,t ) )
				outputImage.setVectorPixelXYZTDouble( x,y,z,t, getMaxVector( x,y,z,t, points ) );
	}

}
