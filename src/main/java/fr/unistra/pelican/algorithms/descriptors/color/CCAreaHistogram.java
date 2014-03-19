package fr.unistra.pelican.algorithms.descriptors.color;

import java.awt.Point;
import java.util.LinkedList;
import java.util.Vector;

import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Descriptor;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.conversion.GrayToRGB;
import fr.unistra.pelican.algorithms.conversion.NonUniformHSVQuantization733;
import fr.unistra.pelican.algorithms.conversion.RGBToHSV;
import fr.unistra.pelican.util.data.HistogramData;

/**
 *	HSV 7-3-3
 *	A histogram which takes into account only connected components (CC) of
 *	surface superior or equal to x% of the total size;
 * 
 *	@author Erchan Aptoula
 *	@author Régis Witz (recovery, mask support, and framework adaptation)
 */
public class CCAreaHistogram extends Descriptor { 	

	/**	First input parameter. */
	public Image input;

	public Image original;
	
	/**	Output parameter. */
	public HistogramData output;

	/**	Constructor */
	public CCAreaHistogram() { 

		super();
		super.inputs = "input";
		super.outputs = "output";
		
	}

	public static HistogramData exec( Image input ) { 
		return ( HistogramData ) new CCAreaHistogram().process( input );
	}

	/**	@see fr.unistra.pelican.Algorithm#launch() */
	@SuppressWarnings("unchecked")
	public void launch() throws AlgorithmException { 

		if ( this.input.getBDim() == 1 ) this.input = GrayToRGB.exec( this.input );
		this.input = RGBToHSV.exec( this.input );
		this.input = NonUniformHSVQuantization733.exec( this.input );

		int xdim = this.input.getXDim();
		int ydim = this.input.getYDim();
		int bdim = this.input.getBDim();
		
		this.original = this.input;

		int sizeTotal = 7 * 3 * 3;

		Image flags = new BooleanImage( xdim,ydim,1,1,1 );

		Double[] values = new Double[ sizeTotal ];
		for ( int i = 0 ; i < sizeTotal ; i++ ) values[i] = new Double(0);
		int nbpx = 0;

		int onePercent = (int) ( xdim * ydim / 100. ) ;

		for ( int x = 0 ; x < xdim ; x++ ) {
			for ( int y = 0 ; y < ydim ; y++ ) { 

				if ( !this.input.isPresentXY( x,y ) ) continue;

				if ( flags.getPixelXYBoolean( x,y ) == false ) { 

					// if the pixel has not been processed yet, then get the connected component
					Vector<Point> CCpixels = getCCPixels( this.input, flags, x,y );
					int[] p = this.input.getVectorPixelXYZTByte( x,y,0,0 );
					if ( CCpixels.size() > onePercent * 0.5 ) { 

						values[ p[0]*9 + p[1]*3 + p[2] ] += CCpixels.size();
						nbpx += CCpixels.size();

					} else { 

						// o bolumu siyaha boya ve goster
						for ( Point q : CCpixels ) 
							for ( int b = 0 ; b < bdim ; b++ ) 
								this.original.setPixelXYBByte( q.x,q.y,b, 0 );

					}
				}
			}
		}

		if ( nbpx > 0 ) for ( int i = 0 ; i < values.length ; i++ ) values[i] = values[i] / nbpx;

		this.output = new HistogramData();
		this.output.setDescriptor( ( Class ) this.getClass() );
		this.output.setValues( values );
	}

	private Vector<Point> getCCPixels( Image input, Image flags, int x, int y ) { 
	
		int gozekSayisi = 0;
		LinkedList<Point> fifo = new LinkedList<Point>();

		Vector<Point> pixels = new Vector<Point>();

		int[] color = input.getVectorPixelXYZTByte( x,y,0,0 );

		fifo.add( new Point(x,y) );
		flags.setPixelXYBoolean( x,y, true );
		gozekSayisi++;

		pixels.add( new Point(x,y) );

		while ( fifo.size() > 0 ) { 

			Point p = (Point) fifo.removeFirst();
			// simdi komsularina bak
			for ( int _y = p.y - 1 ; _y <= p.y + 1 ; _y++ ) { 
				for ( int _x = p.x - 1 ; _x <= p.x + 1 ; _x++ ) { 

					// within image area ?
					if ( !input.isPresentXY( _x,_y ) ) continue;

					// same color ?
					int[] q = input.getVectorPixelXYZTByte( _x,_y,0,0 );
					if ( q[0] != color[0] || q[1] != color[1] || q[2] != color[2] ) continue;

					// already checked ?
					if ( flags.getPixelXYBoolean( _x,_y ) == true ) continue;

					fifo.add( new Point( _x,_y ) );
					pixels.add( new Point( _x,_y ) );
					flags.setPixelXYBoolean( _x,_y, true );
					gozekSayisi++;
				}
			}
		}

		return pixels;
	}

//	public static double distance( Data d1, Data d2 ) { 
//
//		Double[] v1 = (Double[]) d1.getValues();
//		Double[] v2 = (Double[]) d2.getValues();
//		return Tools.histogramDistance( v1,v2 );
//	}



}
