package fr.unistra.pelican.algorithms.morphology.gray;

import java.util.Arrays;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.Point4D;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 * This class computes a gray-scale rank filter
 * 
 * 15/12/2007
 * 
 * @author Abdullah
 */
public class GrayRankFilter extends Algorithm {

	/**
	 * The input image
	 */
	public Image input;

	/**
	 * The flat structuring element
	 */
	public BooleanImage se;

	/**
	 * the desired rank, must be in [1,n]
	 * with n being the cardinality of the SE
	 */
	public int rank;

	/**
	 * The output image
	 */
	public Image output;

	private int xDim;
	private int yDim;
	private int zDim;
	private int tDim;
	private int bDim;

	/**
	 * Default constructor
	 */
	public GrayRankFilter() {
		super.inputs = "input,se,rank";
		super.outputs = "output";
	}

	/**
	 * Gray-scale rank filter
	 * 
	 * @param input The input image
	 * @param se The flat structuring element
	 * @param rank the rank
	 * @return The output image
	 */
	public static Image exec(Image input, BooleanImage se,Integer rank) {
		return (Image) new GrayRankFilter().process(input, se, rank);
	}

	/**
	 * Grayscale rank filter
	 * 
	 * @param image The input image
	 * @param size The size of the analysis window
	 * @param rank the rank
	 * @return The output image
	 */
	public static Image exec(Image image, int size,Integer rank) {
		return (Image) new GrayRankFilter().process(image, FlatStructuringElement2D.createSquareFlatStructuringElement(size),rank);
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
		bDim = input.getBDim();
		zDim = input.getZDim();
		
		Point4D[] points = se.foreground();
		
		if (rank < 1 || rank > points.length)
			throw new AlgorithmException("Invalid rank value");
		
		boolean isHere;
		for ( int b = 0 ; b < bDim ; b++ )
		for ( int t = 0 ; t < tDim ; t++ )
		for ( int z = 0 ; z < zDim ; z++ )
		for ( int y = 0 ; y < yDim ; y++ ) 
		for ( int x = 0 ; x < xDim ; x++ ) { 

			isHere = this.input.isPresent( x,y,z,t,b );
			if  ( isHere ) output.setPixelDouble( x,y,z,t,b, getRankGray( x,y,z,t,b, points ) );
		}
	}

	private double getRankGray(int x, int y, int z, int t, int b, Point4D[] points)
	{
		int tmp = 0;
		int localRank = rank;
		
		double[] array = new double[points.length];
		
		for (int i = 0; i < points.length; i++){
			int valX = x - se.getCenter().x + points[i].x;
			int valY = y - se.getCenter().y + points[i].y;
				
			if (	valX < 0 || valX >= xDim 
				 || valY < 0 || valY >= yDim 
				 || !input.isPresent( valX,valY,z,t,b ) ) continue;
			
			array[tmp++] = input.getPixelXYZTBDouble(valX,valY,z,t,b);
		}
		if (tmp == 0)
			return input.getPixelXYZTBDouble(x,y,z,t,b);
		
		else if (tmp < array.length){
			
			double[] tmpArray = new double[tmp];
			
			for (int i = 0; i < tmp; i++)
				tmpArray[i] = array[i];
			
			array = tmpArray;
			localRank = Math.min(localRank,array.length); 
		}
		
		Arrays.sort(array);
		
		// needs inversion, since the first post-sort element is the smallest,
		// hence corresponds to erosion, hence, the smallest rank.
		// whereas in theory, the smallest rank leads to dilation.
		return array[array.length - localRank];
	}

}