package fr.unistra.pelican.algorithms.descriptors.color;

import fr.unistra.pelican.*;
import fr.unistra.pelican.algorithms.conversion.GrayToRGB;
import fr.unistra.pelican.algorithms.conversion.NonUniformHSVQuantization733;
import fr.unistra.pelican.algorithms.conversion.RGBToHSV;
import fr.unistra.pelican.util.Point4D;
import fr.unistra.pelican.util.data.HistogramData;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 * 
 * Color Structure Histogram (HSV 7 3 3)
 * 
 *	@author Erchan Aptoula
 *	@author Régis Witz (mask support, normalization and framework adaptation)
 */
public class ColorStructureHistogramHSV733 extends Descriptor { 

	private BooleanImage se = FlatStructuringElement2D.createSquareFlatStructuringElement(9);

	/**	First input parameter. */
	public Image input;

	/**	Output parameter. */
	public HistogramData output;

	/**	Constructor */
	public ColorStructureHistogramHSV733() { 

		super();
		super.inputs = "input";
		super.options = "se";
		super.outputs = "output";
		
	}

	public static HistogramData exec( Image input ) { 
		return ( HistogramData ) new ColorStructureHistogramHSV733().process( input );
	}

	public static HistogramData exec( Image input, BooleanImage se ) { 
		return ( HistogramData ) new ColorStructureHistogramHSV733().process( input,se );
	}

	/**	@see fr.unistra.pelican.Algorithm#launch() */
	@SuppressWarnings("unchecked")
	public void launch() throws AlgorithmException { 

		int colorNumber = 7 * 3 * 3;
		Double[] values = new Double[ colorNumber ];
		for ( int i = 0 ; i < colorNumber ; i++ ) values[i] = new Double(0);

		if ( this.input.getBDim() != 3 ) this.input = GrayToRGB.exec( this.input );
		this.input = RGBToHSV.exec( this.input );
		this.input = NonUniformHSVQuantization733.exec( this.input );

		Point4D[] points = this.se.foreground();

		int syc = 0;

		for( int x = 0 ; x < this.input.getXDim() ; x++ ) { 
			for( int y = 0 ; y < this.input.getYDim() ; y++ ) { 

				if ( !this.input.isPresentXY( x,y ) ) continue;

				boolean[] flags = new boolean[colorNumber];
				int[] p = this.input.getVectorPixelXYZTByte(x,y,0,0);

				// check what happens under the SE
				for ( int i = 0 ; i < points.length ; i++ ) { 

					int valX = x - this.se.getCenter().y + points[i].y;
					int valY = y - this.se.getCenter().x + points[i].x;

					if (	valX < 0 || valX >= this.input.getXDim() 
						 || valY < 0 || valY >= this.input.getYDim()  ) continue;
					if ( !this.input.isPresentXY( valX,valY ) ) continue;

					// attention..just once for every color.
					if ( flags[ p[0]*9 + p[1]*3 + p[2] ] == false ) { 

						values[ p[0]*9 + p[1]*3 + p[2] ]++;
						flags[ p[0]*9 + p[1]*3 + p[2] ] = true;
						syc++;
					}	// fi
				}	// rof i

			}	// rof y
		}	// rof x

		// [0,1] araligina tasi
		if ( syc > 0 ) for ( int i = 0 ; i < values.length ; i++ ) values[i] = values[i] / syc;

		this.output = new HistogramData();
		this.output.setDescriptor( ( Class ) this.getClass() );
		this.output.setValues( values );
	}



}
