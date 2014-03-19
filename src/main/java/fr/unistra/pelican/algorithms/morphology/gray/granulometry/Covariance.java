package fr.unistra.pelican.algorithms.morphology.gray.granulometry;

import java.awt.Point;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.morphology.gray.GrayErosion;

/**
 * This class computes normalised covariance curve on 4 directions
 * (0,45,90,135).
 * 
 * example : Given 25 as number of different lengths, 25 values are computed for
 * each orientation, with pixel distances ranging from 1 to 2 x 25, in
 * increments of 2. The user is encouraged to employ different SE forms.
 * 
 * All pixels beyond image borders are ignored.
 * 
 * @author Erchan Aptoula
 */
public class Covariance extends Algorithm {

	/**
	 * The input image
	 */
	public Image input;

	/**
	 * The length of the covariance curve for each orientation
	 */
	public int length;

	/**
	 * The output covariance curve
	 */
	public double[] curve;

	/**
	 * Default constructor
	 */
	public Covariance() {
		super.inputs = "input,length";
		super.outputs = "curve";

	}

	/**
	 * Computes normalised covariance curve on 4 directions (0,45,90,135)
	 * 
	 * @param input
	 *          The input image
	 * @param length
	 *          The length of the covariance curve for each orientation
	 * @return The output covariance curve
	 */
	public static double[] exec(Image input, int length) {
		return (double[]) new Covariance().process(input, length);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() { 

		int size = length * 4 * input.getBDim();
		curve = new double[size];
		double[] originalVolumes = new double[input.getBDim()];

		for ( int b = 0 ; b < input.getBDim() ; b++ ) originalVolumes[b] = volume(input, b);

		// every size
		for ( int i = 1 ; i <= length ; i++ ) { 

			int side = i * 2 + 1;

			// vertical line_____________________________
			BooleanImage se = new BooleanImage( side,side, 1,1,1 );
			se.setCenter( new Point( i,i ) );
			se.setPixelXYBoolean( 0,i, true );
			se.setPixelXYBoolean( side-1,i, true );

			Image tmp = GrayErosion.exec( input,se );

			// place it...carefully..
			for ( int b = 0 ; b < input.getBDim() ; b++ )
				curve[ b*4*length + i-1 ] = volume(tmp, b) / originalVolumes[b];

			// left diagonal line________________________
			se = new BooleanImage( side,side, 1,1,1 );
			se.setCenter( new Point( i,i ) );
			se.setPixelXYBoolean( 0,0, true );
			se.setPixelXYBoolean( side-1,side-1, true );

			tmp = GrayErosion.exec( input,se );

			// place it...carefully..
			for (int b = 0; b < input.getBDim(); b++)
				curve[ b*4*length + length + i-1] = volume(tmp,b) / originalVolumes[b];

			// horizontal line___________________________
			se = new BooleanImage( side,side, 1,1,1 );
			se.setCenter( new Point( i,i ) );
			se.setPixelXYBoolean( i,0, true );
			se.setPixelXYBoolean( i,side-1, true );

			tmp = GrayErosion.exec( input,se );

			// place it...carefully..	
			for (int b = 0; b < input.getBDim(); b++)
				curve[ b*4*length + 2*length + i-1 ] = volume(tmp,b) / originalVolumes[b];
			
			// right diagonal line_______________________
			se = new BooleanImage( side,side, 1,1,1 );
			se.setCenter( new Point( i,i ) );
			se.setPixelXYBoolean( side-1,0, true );
			se.setPixelXYBoolean( 0,side-1, true );
			
			tmp = GrayErosion.exec( input,se );

			// place it...carefully..
			for (int b = 0; b < input.getBDim(); b++)
				curve[ b*4*length + 3*length + i-1 ] = volume(tmp, b) / originalVolumes[b];
		}
	}

	private double volume( Image img, int channel )
	{
		double d = 0.0;
		for ( int x = 0 ; x < img.getXDim() ; x++ ) {
			for ( int y = 0 ; y < img.getYDim() ; y++ ) { 

				if ( input.isPresentXYB( x,y,channel ) )
					d += img.getPixelXYBDouble( x,y, channel );
		}	}
		return d;
	}

}
