package fr.unistra.pelican.algorithms.descriptors.color;

import fr.unistra.pelican.*;
import fr.unistra.pelican.algorithms.conversion.*;
import fr.unistra.pelican.algorithms.descriptors.texture.ConnectedGraySquareGranulometry;
import fr.unistra.pelican.util.Tools;
import fr.unistra.pelican.util.data.*;



/**
 *	Morphological color descriptor based on color specific granulometries.
 *	<p>
 *	HSV 7-3-3 version
 * 
 *	@date 04/11/2007
 * 
 *	@author Erchan Aptoula
 *	@author Régis Witz (mask support and framework adaptation)
 */
public class ColorSpecificGranulometryOld extends Descriptor { 

	/**	First input parameter. */
	public Image input;

	/**	Optional length. */
	public int length = 4;

	/**	Output parameter. */
	public DoubleArrayData output;

	/**	Constructor */
	public ColorSpecificGranulometryOld() { 

		super();
		super.inputs = "input";
		super.options = "length";
		super.outputs = "output";
		
	}

	public static DoubleArrayData exec( Image input ) { 
		return ( DoubleArrayData ) new ColorSpecificGranulometryOld().process( input );
	}

	public static DoubleArrayData exec( Image input, int length ) { 
		return ( DoubleArrayData ) new ColorSpecificGranulometryOld().process( input,length );
	}

	/**	@see fr.unistra.pelican.Algorithm#launch() */
	@SuppressWarnings("unchecked")
	public void launch() throws AlgorithmException { 

		this.input = RGBToHSV.exec( this.input );
		this.input = NonUniformHSVQuantization733.exec( this.input );

		int sizeTotal = 63*this.length;
		Double[] values = new Double[ sizeTotal ];
		for ( int i = 0 ; i < sizeTotal ; i++ ) values[i] = new Double(0);
		
		// a micro-granulometry for every colour
		for ( int i = 0 ; i < 7 ; i++ ) { 
			for ( int j = 0 ; j < 3 ; j++ ) { 
				for ( int k = 0 ; k < 3 ; k++ ) { 

					Image gcc = colourDistanceImage( i,j,k );
					gcc.setMask( this.input.getMask() );

					// anti-granulometry...since the data is dark.
					DoubleArrayData data = ConnectedGraySquareGranulometry.exec( gcc,this.length );
					Double[] granulo = ( Double[] ) data.getValues();

					// place it
					for ( int l = 0 ; l < granulo.length ; l++ ) 
						values[(i * 9 + j * 3 + k) * this.length + l] = granulo[l];
				} // rof k
			} // rof j
		} // rof i

		this.output = new DoubleArrayData();
		this.output.setDescriptor( ( Class ) this.getClass() );
		this.output.setValues( values );
	}

	private Image colourDistanceImage( int hue,int sat,int lum )
	{
		Image output = new ByteImage( this.input.getXDim(),this.input.getYDim(),1,1,1 );

		for( int x = 0 ; x < this.input.getXDim() ; x++ ) { 
			for ( int y = 0 ; y < this.input.getYDim() ; y++ ) { 

				if ( this.input.isPresentXY( x,y ) ) { 

					double[] p = this.input.getVectorPixelXYZTDouble( x,y,0,0 );
					double hueDifference = Tools.hueDifference(hue / 8.0,p[0]);
					double luminanceDifference = Math.abs(lum / 3.0 - p[2]);
					double saturationCoeff = p[1] * (sat / 3.0);

					double distance = hueDifference * saturationCoeff 
									+ luminanceDifference * ( 1.0-saturationCoeff );

					output.setPixelXYDouble(x,y,distance);
				}
			}
		}
		
		return output;
	}

	public static double distance( Data d1, Data d2 ) { 

		Double[] v1 = (Double[]) d1.getValues();
		Double[] v2 = (Double[]) d2.getValues();
		return Tools.correlogramDistance( v1,v2 );
	}



}
