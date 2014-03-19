package fr.unistra.pelican.algorithms.morphology.gray;

import java.util.Arrays;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 * This class applies a median filter with a given structuring element
 * 
 * @author Erchan Aptoula
 */
public class GrayMedian extends Algorithm {

	/**
	 * The input image
	 */
	public Image inputImage;

	/**
	 * The flat structuring element used in the morphological operation
	 */
	public BooleanImage se;

	/**
	 * The output image
	 */
	public Image outputImage;

	/**
	 * Default constructor
	 */
	public GrayMedian() {
		super.inputs = "inputImage,se";
		super.outputs = "outputImage";
	}

	/**
	 * Applies a median filter with a given structuring element
	 * 
	 * @param inputImage
	 *            The input image
	 * @param se
	 *            The flat structuring element used in the morphological
	 *            operation
	 * @return The output image
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Image> T exec(T inputImage, BooleanImage se) {
		return (T) new GrayMedian().process(inputImage, se);
	}

	/**
	 * Applies a median filter with a given size
	 * 
	 * @param input
	 *            The input image
	 * @param size
	 *            The size of the analysis window
	 * @return The output image
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Image> T exec(T image, int size) {
		return (T) new GrayMedian().process(image, FlatStructuringElement2D
				.createSquareFlatStructuringElement(size));
	}

	/*
	 * (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() {
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
			if  ( isHere ) outputImage.setPixelDouble( x,y,z,t,b, getMedianGray( x,y,z,t,b ) );
		}
	}

	/*
	 * Return the max value under a flat structuring element.
	 */
	private double getMedianGray(int x, int y, int z, int t, int b) {
		int tmp = 0;
		double[] array = new double[se.getSum()];
		for (int i = 0; i < se.getXDim(); i++) {
			for (int j = 0; j < se.getYDim(); j++) {
				int valX = x - se.getCenter().x + i;
				int valY = y - se.getCenter().y + j;
				if (	se.getPixelXYBoolean(i,j) 
					 && valX >= 0 && valX < inputImage.getXDim() 
					 && valY >= 0 && valY < inputImage.getYDim() 
					 && inputImage.isPresent( valX,valY,z,t,b ) )
					array[tmp++] = inputImage.getPixelXYZTBDouble( valX,valY,z,t,b );
			}
		}
		if (tmp == 0)
			return inputImage.getPixelXYZTBInt(x, y, z, t, b);
		else if (tmp < array.length) {
			double[] tmpArray = new double[tmp];
			for (int i = 0; i < tmp; i++)
				tmpArray[i] = array[i];
			array = tmpArray;
		}
		Arrays.sort(array);
		return array[tmp / 2];
	}

}
