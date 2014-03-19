package fr.unistra.pelican.algorithms.morphology.vectorial;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.vectorial.orders.VectorialOrdering;

/**
 * This class applies a median filter with the given SE, on double precision
 * 
 * @author Abdullah
 * 
 */
public class VectorialMedian extends Algorithm
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
	 * the vectorial ordering
	 */
	public VectorialOrdering vo;
	
	/**
	 * This class applies a median filter with the given SE, on double precision
	 * @param inputImage the input image
	 * @param se the structuring element
	 * @param vo the vectorial ordering
	 * @return the output image
	 */
	public static Image exec(Image inputImage, BooleanImage se,VectorialOrdering vo) {
		return (Image) new VectorialMedian().process(inputImage,se,vo);
	}

	/**
	 * Constructor
	 * 
	 */
	public VectorialMedian() {

		super();
		super.inputs = "inputImage,se,vo";
		super.outputs = "outputImage";
		
	}

	private double[] getMedianVector(int x, int y, int z, int t) {
		double[][] vectorArray = new double[se.getSum()][];
		int tmp = 0;

		for (int i = 0; i < se.getXDim(); i++) {
			for (int j = 0; j < se.getYDim(); j++) {
				int valX = x - se.getCenter().x + i;
				int valY = y - se.getCenter().y + j;

				if (	se.getPixelXYBoolean(i,j) 
					 && valX >= 0 && valX < inputImage.getXDim() 
					 && valY >= 0 && valY < inputImage.getYDim() 
					 && inputImage.isPresentXYZT( valX,valY,z,t ) ) 
					vectorArray[tmp++] = inputImage.getVectorPixelXYZTDouble( valX,valY,z,t );
			}
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

		return vo.rank(vectorArray, tmp / 2);
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

		for ( int t = 0 ; t < tDim ; t++ ) 
		for ( int z = 0 ; z < zDim ; z++ ) 
		for ( int x = 0 ; x < xDim ; x++ ) 
		for ( int y = 0 ; y < yDim ; y++ ) 
			if ( inputImage.isPresentXYZT( x,y,z,t ) )
				outputImage.setVectorPixelXYZTDouble( x,y,z,t, getMedianVector( x,y,z,t ) );
	}

}
