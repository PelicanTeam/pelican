package fr.unistra.pelican.algorithms.descriptors.color;

import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Descriptor;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.data.HistogramData;
import fr.unistra.pelican.algorithms.conversion.GrayToRGB;
import fr.unistra.pelican.algorithms.conversion.NonUniformHSVQuantization733;
import fr.unistra.pelican.algorithms.conversion.RGBToHSV;

/**
 *	Produces a custom size histogram.
 * 
 *	@author Erchan Aptoula
 *	@author Régis Witz (mask support, normalization and framework adaptation)
 */
public class CustomSizeHistogram extends Descriptor { 

	/**	First input parameter. */
	public Image input;

	/**	Output parameter. */
	public HistogramData output;

	/**	Optional parameter which the number of bins for each band. */
	public int[] size = { 7,3,3 };

	/**	Constructor */
	public CustomSizeHistogram() { 

		super();
		super.inputs = "input";
		super.options = "size";
		super.outputs = "output";
		
	}

	/**	Produces a custom size histogram.
	 *	@param input Input image.
	 *	@return The histogram of <tt>input</tt> (an array of doubles).
	 */
	public static HistogramData exec( Image input ) { 
		return ( HistogramData ) new CustomSizeHistogram().process( input );
	}

	/**	Produces a custom size histogram.
	 *	@param input Input image.
	 *	@param size The number of bins for each band.
	 *	@return The histogram of <tt>input</tt> (an array of doubles).
	 */
	public static HistogramData exec( Image input, int[] size ) { 
		return ( HistogramData ) new CustomSizeHistogram().process( input, size );
	}

	/**	@see fr.unistra.pelican.Algorithm#launch() */
	@SuppressWarnings("unchecked")
	public void launch() throws AlgorithmException { 

		int nbPresentPix = 0;
		int sizeTotal = this.size[0] * this.size[1] * this.size[2];
		Double[] values = new Double[ sizeTotal ];
		for ( int i = 0 ; i < sizeTotal ; i++ ) values[i] = new Double(0);

		if ( this.input.getBDim() == 1  ) this.input = GrayToRGB.exec( this.input );
		this.input = RGBToHSV.exec( this.input );
		this.input = NonUniformHSVQuantization733.exec( this.input );

		for ( int x = 0 ; x < this.input.getXDim() ; x++ ) { 
			for ( int y = 0 ; y < this.input.getYDim() ; y++ ) { 

				if ( this.input.isPresentXY( x,y ) ) { 

					int[] p = this.input.getVectorPixelXYZTByte( x,y,0,0 );
					values [ p[0]*this.size[1]*this.size[2] + p[1]*this.size[2] + p[2] ]++;
					nbPresentPix ++;
				}
			}
		}

		if ( nbPresentPix != 0 ) 
			for ( int i = 0 ; i < sizeTotal ; i++ ) 
				values[i] = values[i] / nbPresentPix;

		this.output = new HistogramData();
		this.output.setDescriptor( ( Class ) this.getClass() );
		this.output.setValues( values );
	}



}
