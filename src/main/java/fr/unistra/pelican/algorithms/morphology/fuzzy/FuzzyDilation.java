package fr.unistra.pelican.algorithms.morphology.fuzzy;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.morphology.GrayStructuringElement;
import fr.unistra.pelican.util.morphology.fuzzyNorms.FuzzyTNorm;

/**
 * Fuzzy dilation as defined by Bloch and Maitre
 * Works with double image with values in [0;1]
 * @author Benjamin Perret
 * 
 */
public class FuzzyDilation extends Algorithm {

	/**
	 * Input image
	 */
	public Image inputImage;

	/**
	 * Structuring function
	 */
	public GrayStructuringElement se;

	/**
	 * TNorm
	 */
	public FuzzyTNorm td;

	/**
	 * Dilated image
	 */
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public FuzzyDilation() {

		super();
		super.inputs = "inputImage,se,td";
		super.outputs = "outputImage";
		
	}

	/**
	 * Return the min value under a flat structuring element.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param t
	 * @param b
	 * @return
	 */
	private double getMinGray( int x, int y, int z, int t, int b ) { 

		double max = Double.MIN_VALUE;
		boolean flag = false;
		int col = se.getXDim();
		int row = se.getYDim();
		for ( int i = 0 ; i < col ; i++ )
			for ( int j = 0 ; j < row ; j++ ) { 

				int valX = x - se.getCenter().x + i;
				int valY = y - se.getCenter().y + j;
				if (	valX >= 0 && valX < inputImage.getXDim() 
					 && valY >= 0 && valY < inputImage.getYDim()
					 && inputImage.isPresent( valX,valY,z,t,b ) ) { 

					double value = td.tDistance( se.getPixelXYDouble( col-i-1, row-j-1), 
												 inputImage.getPixelDouble( valX,valY,z,t,b ));
					if (max < value) max = value;
					flag = true;
				}
			}

		// FIXME: Strange, if nothing is under the se, what is the right way?
		return (flag == true) ? max : inputImage.getPixelDouble(x, y, z, t, b);
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
		int bDim = inputImage.getBDim();
		int zDim = inputImage.getZDim();

		boolean isHere;
		for ( int b = 0 ; b < bDim ; b++ )
		for ( int t = 0 ; t < tDim ; t++ )
		for ( int z = 0 ; z < zDim ; z++ )
		for ( int y = 0 ; y < yDim ; y++ ) 
		for ( int x = 0 ; x < xDim ; x++ ) { 

			isHere = this.inputImage.isPresent( x,y,z,t,b );
			if  ( !isHere ) continue; 

			outputImage.setPixelDouble( x,y,z,t,b, getMinGray( x,y,z,t,b ) );
		}
	}
	
	/**
	 * Fuzzy dilation as defined by Bloch and Maitre
	 * Works with double image with values in [0;1]
	 * 
	 * @param inputImage Input image
	 * @param se Functional Structuring Element
	 * @param s TNorm
	 * @return dilated image image
	 */
	public static Image exec(Image inputImage, GrayStructuringElement se, FuzzyTNorm s)
	{
		return (Image) new FuzzyDilation().process(inputImage,se,s);
	}

}
