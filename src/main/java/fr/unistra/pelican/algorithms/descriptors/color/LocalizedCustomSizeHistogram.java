package fr.unistra.pelican.algorithms.descriptors.color;

import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Descriptor;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.conversion.GrayToRGB;
import fr.unistra.pelican.algorithms.conversion.NonUniformHSVQuantization733;
import fr.unistra.pelican.algorithms.conversion.RGBToHSV;
import fr.unistra.pelican.algorithms.geometric.Subdivide;
import fr.unistra.pelican.util.data.Data;
import fr.unistra.pelican.util.data.DataArrayData;
import fr.unistra.pelican.util.data.HistogramData;

/**
 *	Produces a localized custom size histogram.
 * 
 *	@author Erchan Aptoula
 *	@author Régis Witz (mask support, normalization and framework adaptation)
 */
public class LocalizedCustomSizeHistogram extends Descriptor { 

	/**	First input parameter. */
	public Image input;

	/**	Output parameter. */
	public DataArrayData output;

	/**	Optional parameter which the number of bins for each band. */
	public int[] size = { 7,3,3 };

	/**	Constructor */
	public LocalizedCustomSizeHistogram() { 

		super();
		super.inputs = "input";
		super.options = "size";
		super.outputs = "output";
		
	}

	/**	Produces a localized custom size histogram.
	 *	@param input Input image.
	 *	@return The histogram of <tt>input</tt> (an array of doubles).
	 */
	public static HistogramData exec( Image input ) { 
		return ( HistogramData ) new LocalizedCustomSizeHistogram().process( input );
	}

	/**	Produces a localized custom size histogram.
	 *	@param input Input image.
	 *	@param size The number of bins for each band.
	 *	@return The histogram of <tt>input</tt> (an array of doubles).
	 */
	public static HistogramData exec( Image input, int[] size ) { 
		return ( HistogramData ) new LocalizedCustomSizeHistogram().process( input, size );
	}

	/**	@see fr.unistra.pelican.Algorithm#launch() */
	@SuppressWarnings("unchecked")
	public void launch() throws AlgorithmException { 

		int sizeTotal = this.size[0] * this.size[1] * this.size[2];
		int _xDim = (int) Math.ceil( this.input.getXDim()*0.25 );
		int _yDim = (int) Math.ceil( this.input.getYDim()*0.25 );
		int xOffset = 0, yOffset = 0;

		if ( this.input.getBDim() != 3 ) this.input = GrayToRGB.exec( this.input );
		this.input = RGBToHSV.exec( this.input );
		this.input = NonUniformHSVQuantization733.exec( this.input );

		// divide into 5 pieces...
		Image[] inputs = Subdivide.exec( this.input,Subdivide.Map5 );
		HistogramData[] data = new HistogramData[ inputs.length ];

		for ( int k = 0 ; k < inputs.length ; k++ ) { 

			Double[] values = new Double[ sizeTotal ];
			for ( int i = 0 ; i < sizeTotal ; i++ ) values[i] = new Double(0);
			int nbpix = 0;

			switch ( k ) { 

				//	BEWARE ! theses indices are totally dependant of Subdivide's implementation.
				//	if Subdivide changes, a masked instance of the current class will presumabely 
				//	produce incorrect results -if not "out of bounds" Exceptions.
				case 0 : 	// NORTH offsets
					xOffset = 0;
					yOffset = 0;
					break;
				case 1 : 	// SOUTH offsets
					xOffset = 0;
					yOffset = this.input.getYDim()-_yDim;
					break;
				case 2 : 	// WEST offsets
					xOffset = 0;
					yOffset = _yDim;
					break;
				case 3 : 	// CENTER offsets
					xOffset = _xDim;
					yOffset = _yDim;
					break;
				case 4 : 	// EAST offsets
					xOffset = this.input.getXDim()-_xDim;
					yOffset = _yDim;
					break;
			}
			for ( int x = 0 ; x < inputs[k].getXDim() ; x++ ) { 
				for ( int y = 0 ; y < inputs[k].getYDim() ; y++ ) { 

					// the day Subdivide supports masking, update this test.
					if ( this.input.isPresentXYZT( x+xOffset, y+yOffset, 0,0 ) ) { 

						int[] p = inputs[k].getVectorPixelXYZTByte( x,y, 0,0 );
						values[ p[0] * this.size[1] * this.size[2] 
						      + p[1] * this.size[2] 
						      + p[2] ]++;
						nbpix ++;
					}
				}
			}

			if ( nbpix != 0 ) 
				for ( Double value : values ) value /= nbpix;

			data[k] = new HistogramData();
			data[k].setDescriptor( ( Class ) this.getClass() );
			data[k].setValues( values );
		}

		this.output = new DataArrayData();
		this.output.setDescriptor( ( Class ) this.getClass() );
		this.output.setValues( data );
	}

	public static double distance( Data d1, Data d2 ) { 

		Data[] v1 = ( Data[] ) d1.getValues();
		Data[] v2 = ( Data[] ) d2.getValues();
		int length = v1.length;
		if ( length != v2.length ) 
			throw new fr.unistra.pelican.PelicanException( "Incompatible sizes !" );

		double distance = 0.;
		int size = 7*3*3;
		for ( int i = 0 ; i < length ; i++ ) { 

			Double[] w1 = ( Double[] ) v1[i].getValues();
			Double[] w2 = ( Double[] ) v2[i].getValues();
			if ( w1.length != w2.length ) 
				throw new fr.unistra.pelican.PelicanException( "Incompatible sizes !" );

			double tmp = 0.;
			for ( int j = 0 ; j < size ; j++ ) 
				tmp += Math.min( w1[j],w2[j] );
			distance += 1.-tmp;
		}
		return distance / 5.;

//		Double[] v1 = ( Double[] ) d1.getValues();
//		Double[] v2 = ( Double[] ) d2.getValues();
//		if ( v1.length != v2.length ) 
//			throw new fr.unistra.pelican.PelicanException( "Incompatible sizes !" );
//
//		double distance = 0.0;
//		int size = 7 * 3 * 3;
//		for( int i = 0 ; i < 5 ; i++ ) {
//		
//			double tmp = 0.0;
//			for( int j = i*size ; j < ( i+1 )*size ; j++ ) tmp += Math.min(v1[j],v2[j]);
//			distance += 1.0 - tmp;
//		}
//		return distance / 5.0;
	}



}
