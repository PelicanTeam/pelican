package fr.unistra.pelican.algorithms.morphology.vectorial;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.Point4D;
import fr.unistra.pelican.util.vectorial.orders.BinaryVectorialOrdering;

/**
 * This class computes a vectorial rank filter using binary vectorial orderings.
 * 
 * 16/12/2007
 * 
 * @author Abdullah
 */
public class VectorialRankFilter extends Algorithm
{

	/**
	 * The input image
	 */
	public Image input;

	/**
	 * The flat structuring element
	 */
	public BooleanImage se;

	/**
	 * The output image
	 */
	public Image output;
	
	/**
	 * the binary vectorial ordering
	 */
	public BinaryVectorialOrdering vo;
	
	/**
	 * the desired rank, must be in [1,n]
	 * with n being the cardinality of the SE
	 */
	public int rank;
	
	private int xDim;
	private int yDim;
	private int zDim;
	private int tDim;

	/**
	 * Default constructor
	 */
	public VectorialRankFilter() {
		super.inputs = "input,se,vo,rank";
		super.outputs = "output";
		
	}

	/**
	 * Vectorial rank filter
	 * 
	 * @param input The input image
	 * @param se The flat structuring element
	 * @param vo the vectorial ordering
	 * @param rank the rank
	 * @return The output image
	 */
	public static Image exec(Image input, BooleanImage se,BinaryVectorialOrdering vo,Integer rank) {
		return (Image) new VectorialRankFilter().process(input, se, vo,rank);
	}

	/*
	 * (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() {
		output = input.copyImage(false);
		
		xDim = input.getXDim();
		yDim = input.getYDim();
		tDim = input.getTDim();
		zDim = input.getZDim();
		
		Point4D[] points = se.foreground();
		
		if (rank < 1 || rank > points.length)
			throw new AlgorithmException("Invalid rank value");
		
		for ( int t = 0 ; t < tDim ; t++ ) 
		for ( int z = 0 ; z < zDim ; z++ ) 
		for ( int x = 0 ; x < xDim ; x++ ) 
		for ( int y = 0 ; y < yDim ; y++ ) 
			if ( input.isPresentXYZT( x,y,z,t ) )
				output.setVectorPixelXYZTDouble( x,y,z,t, getRankVector( x,y,z,t, points ) );
	}

	private double[] getRankVector(int x,int y,int z,int t,Point4D[] points)
	{
		int tmp = 0;
		int localRank = rank;
		
		double[][] vectorArray = new double[points.length][];
		

		for (int i = 0; i < points.length; i++){
			int valX = x - se.getCenter().x + points[i].x;
			int valY = y - se.getCenter().y + points[i].y;
				
			if (	valX < 0 || valX >= xDim 
				 || valY < 0 || valY >= yDim 
				 || !input.isPresentXYZT( valX,valY,z,t ) ) continue;
			
			vectorArray[tmp++] = input.getVectorPixelXYZTDouble(valX,valY,z,t);
		}


		if (tmp == 0)
			return input.getVectorPixelXYZTDouble(x,y,z,t);

		else if (tmp < vectorArray.length) {
			double[][] tmpArray = new double[tmp][];

			for (int i = 0; i < tmp; i++)
				tmpArray[i] = vectorArray[i];

			vectorArray = tmpArray;
			localRank = Math.min(localRank,vectorArray.length);

		}

		return vo.rank(vectorArray,vectorArray.length - localRank);
	}

}