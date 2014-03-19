package fr.unistra.pelican.algorithms.descriptors.texture;

import java.awt.Point;

import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Descriptor;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.morphology.gray.GrayErosion;
import fr.unistra.pelican.util.data.DoubleArrayData;

/**
 *	This class computes normalised covariance curve on 4 directions
 *	(0,45,90,135).
 *
 *	example : Given 25 as number of different lengths, 25 values are computed for
 *	each orientation, with pixel distances ranging from 1 to 2 x 25, in
 *	increments of 2. The user is encouraged to employ different SE forms.
 *
 *	All pixels beyond image borders are ignored.
 *
 *	MASK SUPPORT NOTE : put a msk wich B-dim different of that of input, and you'll
 *	end just screwed up. 
 *
 *	@author Erchan Aptoula
 *	@author Régis Witz (mask support, normalization and framework adaptation)
 */
public class Covariance extends Descriptor { 

	/**	The length of the covariance curve for each orientation. */
	public int length = 25;

	/**	Input image. */
	public Image input;

	/**	Output covariance curve. */
	public DoubleArrayData output;

	/**	Constructor */
	public Covariance() { 

		super();
		super.inputs = "input";
		super.options = "length";
		super.outputs = "output";
	}

	public static DoubleArrayData exec( Image input ) { 
		return ( DoubleArrayData ) new Covariance().process( input );
	}

	/**
	 * Computes normalised covariance curve on 4 directions (0,45,90,135)
	 * 
	 * @param input
	 *            The input image
	 * @param length
	 *            The length of the covariance curve for each orientation
	 * @return The output covariance curve
	 */
	public static DoubleArrayData exec( Image input, int length ) { 
		return ( DoubleArrayData ) new Covariance().process( input, length );
	}

	/**	@see fr.unistra.pelican.Algorithm#launch() */
	@SuppressWarnings("unchecked")
	public void launch() { 

		int bdim = this.input.getBDim();
		int size = this.length * 4 * bdim;
		Double[] curve = new Double[ size ];
		for ( int i = 1 ; i <= this.length ; i++ ) 
		for ( int n = 0 ; n < 4 ; n++ ) 
		for ( int b = 0 ; b < bdim ; b++ ) 
			curve[ i-1 + n*this.length + b*4*length ] = new Double(0);

		Double[] originalVolumes = new Double[ bdim ];
		for ( int b = 0 ; b < bdim ; b++ ) originalVolumes[b] = this.volume( this.input, b );

		int n;
		// every size
		for ( int i = 1 ; i <= this.length ; i++ ) { 

			int side = i*2 + 1;

			// vertical line_____________________________
			n = 0;
			BooleanImage se = new BooleanImage( side,side, 1,1,1 );
			se.setCenter( new Point( i,i ) );
			se.setPixelXYBoolean( 0,i, true );
			se.setPixelXYBoolean( side-1,i, true );

			Image tmp = GrayErosion.exec( this.input,se );
			for ( int b = 0 ; b < bdim ; b++ )
				curve[ b + n*bdim + ( i-1 )*4*bdim ] = 
					this.volume( tmp,b ) / ( originalVolumes[b] * 4 * this.length );

			// left diagonal line________________________
			n = 1;
			se = new BooleanImage( side,side, 1,1,1 );
			se.setCenter( new Point( i,i ) );
			se.setPixelXYBoolean( 0,0, true );
			se.setPixelXYBoolean( side-1,side-1, true );

			tmp = GrayErosion.exec( this.input,se );
			for ( int b = 0 ; b < bdim ; b++ )
				curve[ i-1 + n*this.length + b*4*this.length ] = 
					this.volume( tmp,b ) / ( originalVolumes[b] * 4 * this.length );

			// horizontal line___________________________
			n = 2;
			se = new BooleanImage( side,side, 1,1,1 );
			se.setCenter( new Point( i,i ) );
			se.setPixelXYBoolean( i,0, true );
			se.setPixelXYBoolean( i,side-1, true );

			tmp = GrayErosion.exec( this.input,se );
			for ( int b = 0 ; b < bdim ; b++ )
				curve[ i-1 + n*this.length + b*4*this.length ] = 
					this.volume( tmp,b ) / ( originalVolumes[b] * 4 * this.length );

			// right diagonal line_______________________
			n = 3;
			se = new BooleanImage( side,side, 1,1,1 );
			se.setCenter( new Point( i,i ) );
			se.setPixelXYBoolean( side-1,0, true );
			se.setPixelXYBoolean( 0,side-1, true );

			tmp = GrayErosion.exec( this.input,se );
			for ( int b = 0 ; b < bdim ; b++ )
				curve[ i-1 + n*this.length + b*4*this.length ] = 
					this.volume( tmp, b ) / ( originalVolumes[b] * 4 * this.length );
		}

		this.output = new DoubleArrayData();
		this.output.setDescriptor( ( Class ) this.getClass() );
		this.output.setValues( curve );
	}

	private Double volume( Image img, int channel ) { 

		Double d = new Double( 0.0 );
		for ( int x = 0 ; x < img.getXDim() ; x++ ) {
			for ( int y = 0 ; y < img.getYDim() ; y++ ) { 

				if ( this.input.isPresentXYB( x,y,channel ) )
					d += img.getPixelXYBDouble( x,y, channel );
		}	}
		return d;
	}



}
