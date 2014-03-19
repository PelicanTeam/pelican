package fr.unistra.pelican.algorithms.descriptors.color;

import fr.unistra.pelican.*;
import fr.unistra.pelican.util.data.DataArrayData;
import fr.unistra.pelican.util.data.HistogramData;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;
import fr.unistra.pelican.util.Point4D;
import fr.unistra.pelican.algorithms.conversion.GrayToRGB;
import fr.unistra.pelican.algorithms.conversion.NonUniformHSVQuantization733;
import fr.unistra.pelican.algorithms.conversion.RGBToHSV;


/**
 * Autocorrelogram in HSV (7,3,3)...distances (1,3,5,7)
 * 
 * 7x3x3 = 63 colors
 * 
 * ...A different implementation...
 *
 *	mask support note:
 *	- a pixel "absent" from input doesn't count in histogram (var. histo)
 *	- a pixel "absent" count as it were of a different colour than the center pixel, even
 *	  if it's at "1" on se and is colour is the same.
 * 
 *	@author Erchan Aptoula
 *	@author Régis Witz (mask support, normalization, framework adaptation and distance computing)
 */
public class AutoCorrelogram extends Descriptor { 

	/**	First input parameter. */
	public Image input;

	/**	Output parameter. */
	public DataArrayData output;

	/**	Constructor */
	public AutoCorrelogram() { 

		super();
		super.inputs = "input";
		super.outputs = "output";
		
	}

	public static HistogramData exec( Image input ) { 
		return ( HistogramData ) new AutoCorrelogram().process( input );
	}

	/**	@see fr.unistra.pelican.Algorithm#launch() */
	@SuppressWarnings("unchecked")
	public void launch() throws AlgorithmException { 

		final int NumberOfColors = 7 * 3 * 3;
		final int[] distances = {1,3,5,7};
		Double[][] values = new Double[ distances.length ][ NumberOfColors ];
		for ( int d = 0 ; d < distances.length ; d++ ) 
		for ( int c = 0 ; c < NumberOfColors ; c++ ) 
			values[d][c] = new Double(0);

		if ( this.input.getBDim() != 3 ) this.input = GrayToRGB.exec( this.input );
		this.input = RGBToHSV.exec( this.input );
		this.input = NonUniformHSVQuantization733.exec( this.input );
		
		double[] histo = new double[NumberOfColors];
		
		// for every distance
		for( int d = 0 ; d < distances.length ; d++ ) { 
			
			// construct the corresponding hollow SE
			BooleanImage se = 
				FlatStructuringElement2D.createHollowSquareFlatStructuringElement(distances[d]*2+1);
			Point4D[] sePoints = se.foreground();
			int nbpts = 0;
		
			// scan the entire image surface
			for( int x = 0 ; x < this.input.getXDim() ; x++ ) { 
				for( int y = 0 ; y < this.input.getYDim() ; y++ ) { 

					if ( !this.input.isPresentXY( x,y ) ) continue;

					// our center pixel
					int[] p = this.input.getVectorPixelXYZTByte(x,y,0,0);

					// during the first run, construct the image histo
					// needed for normalization
					if ( d == 0 ) histo[ p[0]*9 + p[1]*3 + p[2] ]++;

					// now get the number of same valued pixels within this distance,
					// set by the SE's shape
					for ( int i = 0 ; i < sePoints.length ; i++ ) { 

						int valX = x - se.getCenter().y + sePoints[i].x;
						int valY = y - se.getCenter().x + sePoints[i].y;

						if ( this.input.isOutOfBounds( valX,valY,0,0,0 ) ) continue;
						if ( !this.input.isPresentXY( valX,valY ) ) continue;

						int[] p2 = this.input.getVectorPixelXYZTByte( valX,valY,0,0 );

						if ( p[0] == p2[0] && p[1] == p2[1] && p[2] == p2[2] ) { 

							values[ d ][ p[0] * 9 + p[1] * 3 + p[2] ]++;
							nbpts++;
						}
					} // rof i

				}	// rof y
			}	// rof x

			// normalize each color seperately
			for( int i = 0 ; i < NumberOfColors;  i++ )
				if ( histo[i] > 0 ) 
					values[d][i] /= nbpts*distances.length;
		}

		HistogramData[] histos = new HistogramData[ distances.length ];
		for( int d = 0 ; d < distances.length ; d++ ) { 

			histos[d] = new HistogramData();
			histos[d].setDescriptor( ( Class ) this.getClass() );
			histos[d].setValues( values[d] );
		}

		this.output = new DataArrayData();
		this.output.setDescriptor( ( Class ) this.getClass() );
		this.output.setValues( histos );
	}



}
