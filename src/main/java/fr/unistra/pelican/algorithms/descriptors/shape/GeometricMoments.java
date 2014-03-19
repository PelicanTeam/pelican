package fr.unistra.pelican.algorithms.descriptors.shape;

import fr.unistra.pelican.Descriptor;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.conversion.RGBToGray;
import fr.unistra.pelican.util.data.DoubleArrayData;

/**
 * Computes a series of geometric moments for its input
 * that are supposed to describe the shape of its content.
 * 
 * They are computed using the grey-level version of the input.
 * 
 * 
 *	@author Erchan Aptoula
 *	@author Régis Witz (mask support, normalization and framework adaptation)
 */
public class GeometricMoments extends Descriptor { 

	/**	First input parameter. */
	public Image input;

	/**	Output parameter. */
	public DoubleArrayData output;

	/**	Constructor */
	public GeometricMoments() { 

		super();
		super.inputs = "input";
		super.outputs = "output";
		
	}

	public static DoubleArrayData exec( Image input ) { 
		return ( DoubleArrayData ) new GeometricMoments().process( input );
	}

	/**	@see fr.unistra.pelican.Algorithm#launch() */
	@SuppressWarnings("unchecked")
	public void launch() { 

		if ( this.input.getBDim() != 1 ) input = RGBToGray.exec(input);
		int sizeTotal = 7;
		Double[] values = new Double[ sizeTotal ];	// moments of order : 00, 01, 10, 11, 02, 20, 22
		for ( int i = 0 ; i < sizeTotal ; i++ ) values[i] = new Double(0);

		values[0] = moment(0,0);
		values[1] = moment(0,1);
		values[2] = moment(1,0);
		values[3] = moment(1,1);
		values[4] = moment(0,2);
		values[5] = moment(2,0);
		values[6] = moment(2,2);

		normalize( values );

		this.output = new DoubleArrayData();
		this.output.setDescriptor( ( Class ) this.getClass() );
		this.output.setValues( values );
	}

	private double moment( int i, int j )
	{
		int n = 0;
		double d = 0.0;
		for ( int x = 0 ; x < this.input.getXDim() ; x++ ) { 
			for ( int y = 0 ; y < this.input.getYDim() ; y++ ) { 

				if ( this.input.isPresentXY( x,y ) ) { 

					d += Math.pow(x,i) * Math.pow(y,j) * this.input.getPixelXYDouble(x,y);
					n++;
				}
			}
		}
		if ( n == 0 ) return 0.0;
		return d/ ( ( Math.pow( this.input.getXDim(),i+1 )*Math.pow( this.input.getYDim(),j+1 ) ) / 
				Math.pow( Math.sqrt(n), (double)i/2+(double)j/2+1 ) ) ;
	}

//	public static double distance( Data d1, Data d2 ) { 
//
//		Double[] v1 = (Double[]) d1.getValues();
//		Double[] v2 = (Double[]) d2.getValues();
//		return Tools.euclideanDistance( v1,v2 );
//	}

	/**	Vector normalization.
	 *	@param vector wich is normalized at end if possible.
	 *	@return 0, or -1 si <tt>vector</tt>'s norm equals to zero.
	 */
	private static int normalize( Double[] vector ) { 

		double norm = 0;
		for ( int i = 0 ; i < vector.length ; i++ ) norm += vector[i]*vector[i];
		norm = Math.sqrt( norm );
		if ( norm == 0 ) return -1;
		if ( norm != 1 ) 
			for ( int i = 0 ; i < vector.length ; i++ ) vector[i] = vector[i]/norm;
		return 0;
	}



}
