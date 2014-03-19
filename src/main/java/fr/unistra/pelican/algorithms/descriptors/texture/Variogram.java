package fr.unistra.pelican.algorithms.descriptors.texture;

import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.Descriptor;
import fr.unistra.pelican.util.data.DoubleArrayData;



/**
 *	Variogram on 4 directions
 *
 *	@author Erchan Aptoula
 *	@author Régis Witz (recovery, mask support and framework adaptation, normalization)
 */
public class Variogram extends Descriptor {

	public static final int HOR = 0;
	public static final int LDG = 1;
	public static final int VER = 2;
	public static final int RDG = 3;

	/**	Length of the granulometric curve */
	public int length = 25;

	/**	First input parameter. */
	public Image input;

	/**	Output parameter. */
	public DoubleArrayData output;

	/**	Constructor */
	public Variogram() { 

		super();
		super.inputs = "input";
		super.options = "length";
		super.outputs = "output";
		
	}

	public static DoubleArrayData exec( Image input ) { 
		return ( DoubleArrayData ) new Variogram().process( input );
	}
	public static DoubleArrayData exec( Image input, int length ) { 
		return ( DoubleArrayData ) new Variogram().process( input,length );
	}

	/**	@see fr.unistra.pelican.Algorithm#launch() */
	@SuppressWarnings("unchecked")
	public void launch() throws AlgorithmException {

		int size = this.length * 4;
		Double[] values = new Double[ size ];
		for ( int i = 0 ; i < size ; i++ ) values[i] = new Double(0);

		// every size
		for ( int i = 1; i <= this.length; i++ ) { 

			// vertical_____________________________
			double d = variogramOperator( this.input,i,Variogram.VER );
			values[ i-1 ] = d;

			// left diagonal line________________________
			d = variogramOperator( this.input,i,Variogram.LDG );
			values[ this.length+i-1 ] = d;

			// horizontal line___________________________
			d = variogramOperator( this.input,i,Variogram.HOR );
			values[ 2*this.length+i-1 ] = d;

			// right diagonal line_______________________
			d = variogramOperator( this.input,i,Variogram.RDG );
			values[ 3*this.length+i-1 ] = d;
		}

		// don't forget to normalize..
		values = fr.unistra.pelican.util.Tools.vectorNormalize( values );

		this.output = new DoubleArrayData();
		this.output.setDescriptor( ( Class ) this.getClass() );
		this.output.setValues( values );

	}

	private double variogramOperator( Image img, int l, int direction ) { 

		double res = 0.0;
		double syc = 0.0;
		int tmpx = 0;
		int tmpy = 0;


		// M00 in alpha kuvvetini al..tum goruntu icin kesisim icn degil..
		// zira islemde tum pikselleri modulo biciminde kullaniyoruz...

		switch ( direction ) {
		case HOR:
			for ( int x = 0 ; x < img.getXDim() ; x++ ) {
				for ( int y = 0 ; y < img.getYDim() ; y++ ) { 

					if ( !img.isPresentXY( x,y ) ) continue;

					tmpx = x + l;

					if ( !img.isPresentXY( tmpx,y ) ) continue;

					double diff = diff( img, x,y, tmpx,y );
					res += diff * diff;
					syc++;
				}
			}
			break;
		case VER:
			for ( int x = 0 ; x < img.getXDim() ; x++ ) {
				for ( int y = 0 ; y < img.getYDim() ; y++ ) { 

					if ( !img.isPresentXY( x,y ) ) continue;

					tmpy = y + l;

					if ( !img.isPresentXY( x,tmpy ) ) continue;

					double diff = diff( img, x,y, x,tmpy );
					res += diff * diff;
					syc++;
				}
			}
			break;
		case LDG:
			for ( int x = 0 ; x < img.getXDim() ; x++ ) {
				for ( int y = 0 ; y < img.getYDim() ; y++ ) { 

					if ( !img.isPresentXY( x,y ) ) continue;

					tmpx = x - l;
					tmpy = y + l;

					if ( !img.isPresentXY( tmpx,tmpy ) ) continue;

					double diff = diff( img, x,y, tmpx,tmpy );
					res += diff * diff;
					syc++;
				}
			}
			break;
		case RDG:
			for ( int x = 0 ; x < img.getXDim() ; x++ ) {
				for ( int y = 0 ; y < img.getYDim() ; y++ ) { 

					if ( !img.isPresentXY( x,y ) ) continue;

					tmpx = x + l;
					tmpy = y + l;

					if ( !img.isPresentXY( tmpx,tmpy ) ) continue;

					double diff = diff( img, x,y, tmpx,tmpy );
					res += diff * diff;
					syc++;
				}
			}
			break;
		default: throw new AlgorithmException( "Unsupported direction" );
		}

		return res / ( 2*syc );
	}

	private double diff( Image img, int x, int y, int x2, int y2 ) { 

		if ( img.getBDim() == 1 ) { 

			double d = img.getPixelXYDouble( x,y );
			double d2 = img.getPixelXYDouble( x2,y2 );
			double sonuc = ( d-d2 );
			return sonuc;

		} else { 

			double tmp = 0.0;
			for ( int b = 0; b < img.getBDim(); b++ ) { 

				double diff = img.getPixelXYBDouble( x,y,b ) - img.getPixelXYBDouble( x2,y2,b );
				tmp += diff*diff;
			}
			return Math.sqrt(tmp);
		}
	}



	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#help()
	 */
	public String help() {
		return "4 directional normalized covariance.\n"
				+ "fr.unistra.pelican.Image inputImage\n"
				+ "Integer size for each side\n" + "\n"
				+ "double array output curve\n" + "\n";
	}



}
