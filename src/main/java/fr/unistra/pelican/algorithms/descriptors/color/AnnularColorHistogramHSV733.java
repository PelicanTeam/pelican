package fr.unistra.pelican.algorithms.descriptors.color;

import java.awt.Point;
import java.util.Vector;

import fr.unistra.pelican.*;
import fr.unistra.pelican.algorithms.conversion.GrayToRGB;
import fr.unistra.pelican.algorithms.conversion.NonUniformHSVQuantization733;
import fr.unistra.pelican.algorithms.conversion.RGBToHSV;
import fr.unistra.pelican.util.data.DataArrayData;
import fr.unistra.pelican.util.data.DoubleArrayData;
import fr.unistra.pelican.util.data.HistogramData;

/**
 *	Color distribution entropy in HSV 7-3-3
 *	[Rao et al. - Spatial color histograms for content-based Image retrieval (1999)]
 *	05/11/2007
 * 
 *	@author Erchan Aptoula
 *	@author Régis Witz (recovery, mask management and framework adaptation)
 *
 *	@TODO : ANORMALIZE !! 
 */
public class AnnularColorHistogramHSV733 extends Descriptor {


	/**	First input parameter. */
	public Image input;

	/**	Output parameter. */
	public DataArrayData output;
	/**	Constructor */
	public AnnularColorHistogramHSV733() { 

		super();
		super.inputs = "input";
		super.outputs = "output";
		
	}

	/**	Produces an annular color histogram.
	 *	@param input Input image.
	 *	@return The histogram of <tt>input</tt> (an array of doubles).
	 */
	public static HistogramData exec( Image input ) { 
		return ( HistogramData ) new AnnularColorHistogramHSV733().process( input );
	}


	/**	@see fr.unistra.pelican.Algorithm#launch() */
	@SuppressWarnings("unchecked")
	public void launch() throws AlgorithmException { 

		if ( this.input.getBDim() == 1  ) this.input = GrayToRGB.exec( this.input );
		Image img = RGBToHSV.exec( this.input );
		img = NonUniformHSVQuantization733.exec(img);

		int colorNumber = 7 * 3 * 3;
		int N = 4;

		Vector<Point>[] pixelsPerColour = new Vector[ colorNumber ];
		Point[] centroids = new Point[ colorNumber ]; // centroids for each color
		double[] radius = new double[ colorNumber ];

		Double[][] values = new Double[ colorNumber ][ N ];	// histogram and entropy for each color
		for ( int c = 0 ; c < colorNumber ; c++ ) 
			for ( int k = 0 ; k < N ; k++ ) 
				values[c][k] = new Double(0);

		for ( int c = 0 ; c < colorNumber ; c++ ) 
			pixelsPerColour[c] = new Vector<Point>();

		for ( int x = 0 ; x < img.getXDim() ; x++ ) { 
			for ( int y = 0 ; y < img.getYDim() ; y++ ) { 

				if ( !this.input.isPresentXY( x,y ) ) continue;

				int[] p = img.getVectorPixelXYZTByte( x,y,0,0 );
				int index = p[0]*9 + p[1]*3 + p[2];
				// the pixels of colour p
				pixelsPerColour[index].add( new Point( x,y ) );
			}
		}

		DoubleArrayData[] data = new DoubleArrayData[ colorNumber ];
		// now, compute the annular entropies of each color
		for ( int c = 0; c < colorNumber; c++ ) { 

			data[c] = new DoubleArrayData();
			data[c].setDescriptor( ( Class ) this.getClass() );
			data[c].setValues( values[c] );

			if ( pixelsPerColour[c].size() == 0 ) continue;

			// compute the centroid
			centroids[c] = new Point( 0,0 );
			for ( Point p : pixelsPerColour[c] ) { 

				centroids[c].x += p.x;
				centroids[c].y += p.y;
			}

			centroids[c].x /= pixelsPerColour[c].size();
			centroids[c].y /= pixelsPerColour[c].size();

			// get the radius of this colour
			for ( Point p : pixelsPerColour[c] ) { 

				double distance = ( p.x-centroids[c].x ) * ( p.x-centroids[c].x )
								+ ( p.y-centroids[c].y ) * ( p.y-centroids[c].y );
				radius[c] = Math.max( radius[c], Math.sqrt(distance) );
			}

			// get the occurrence probability for each distance and then compute
			// the accumulative entropy
			// the radius of each color cluster is divided in N
			double[] occurrenceProbability = new double[N];
			for ( Point p : pixelsPerColour[c] ) { 

				double distance = Math.sqrt(  ( p.x-centroids[c].x ) * ( p.x-centroids[c].x ) 
											+ ( p.y-centroids[c].y ) * ( p.y-centroids[c].y ) );
				// check how many colours are within this radius
				for ( int k = 0 ; k < N ; k++ ) 
					if ( distance <= ( k+1 )*radius[c] / N ) occurrenceProbability[k]++;
			}

			values[c][0] = new Double( occurrenceProbability[0] );
			for ( int k = 1 ; k < N ; k++ ) 
				values[c][k] = new Double( occurrenceProbability[k]-occurrenceProbability[ k-1 ] );

			for ( int k = 0 ; k < N ; k++ ) values[c][k] /= pixelsPerColour[c].size();

		}

		this.output = new DataArrayData();
		this.output.setDescriptor( ( Class ) this.getClass() );
		this.output.setValues( data );

	}



}
