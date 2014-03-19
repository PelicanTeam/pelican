package fr.unistra.pelican.algorithms.descriptors.color.vectorial;

import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Descriptor;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.conversion.GrayToRGB;
import fr.unistra.pelican.algorithms.conversion.NonUniformHSVQuantization733;
import fr.unistra.pelican.algorithms.conversion.RGBToHSV;
import fr.unistra.pelican.algorithms.morphology.gray.GrayASF;
import fr.unistra.pelican.algorithms.morphology.vectorial.VectorialASF;
import fr.unistra.pelican.algorithms.morphology.vectorial.VectorialLeveling;
import fr.unistra.pelican.util.Tools;
import fr.unistra.pelican.util.data.Data;
import fr.unistra.pelican.util.data.HistogramData;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;
import fr.unistra.pelican.util.vectorial.orders.QuantizationBasedLexicographicalOrdering;
import fr.unistra.pelican.util.vectorial.orders.VectorialOrdering;

/**
 *	Normalised histograms in a vectorial leveling based scale-space
 *	HSV 7-3-3
 * 
 *	@author Erchan Aptoula
 *	@author Régis Witz (correction, mask support, normalization and framework adaptation)
 * 
 */
public class VectorialLevelingBasedMultiscaleHistogram extends Descriptor { 

	public VectorialOrdering vo = new QuantizationBasedLexicographicalOrdering(10);

	public int scales = 3;
	public int[] size = {7,3,3};

	/**	First input parameter. */
	public Image input;

	/**	Output parameter. */
	public HistogramData output;

	/**	Constructor */
	public VectorialLevelingBasedMultiscaleHistogram() { 

		super();
		super.inputs = "input";
		super.options = "size,scales";
		super.outputs = "output";
		
	}

	/**
	 *	@param input Image to be converted in a histogram.
	 *	@return The normalized histogram.
	 */
	public static HistogramData exec( Image input ) { 
		return ( HistogramData ) new VectorialLevelingBasedMultiscaleHistogram().process( input );
	}

	/**
	 *	@param input Image to be converted in a histogram.
	 *	@param size The number of bins of each band.
	 *	@param scales The number of scales.
	 *	@return The normalized histogram.
	 */
	public static HistogramData exec( Image input, int[] size, int scales ) { 
		return ( HistogramData ) 
		new VectorialLevelingBasedMultiscaleHistogram().process( input,size,scales );
	}

	/**	@see fr.unistra.pelican.Algorithm#launch() */
	@SuppressWarnings("unchecked")
	public void launch() throws AlgorithmException { 

		int totalHistoSizePerScale = 7 * 3 * 3;
		int nbPresentPix = 0;
		int sizeTotal = this.scales * totalHistoSizePerScale;
		Double[] values = new Double[ sizeTotal ];
		for ( int i = 0 ; i < sizeTotal ; i++ ) values[i] = new Double(0);

		if ( this.input.getBDim() == 1 ) this.input = GrayToRGB.exec( this.input );
		this.input = RGBToHSV.exec(input);
		this.input = NonUniformHSVQuantization733.exec(input);

		for (int s = 0; s < scales; s++) {

			// prepare this scale
			Image tmp = null;
			
			BooleanImage se = FlatStructuringElement2D.createSquareFlatStructuringElement(5);
			Image marker = (Image) new VectorialASF().process( 
					this.input, se, GrayASF.OPENING_FIRST, new Integer( s*2 +1 ), this.vo );
			tmp = (Image) new VectorialLeveling().process( input, marker, new Integer(0), this.vo );

			// extract the histogram
			for ( int x = 0 ; x < this.input.getXDim() ; x++ ) { 
				for ( int y = 0 ; y < this.input.getYDim() ; y++ ) { 

					if ( this.input.isPresentXY( x,y ) ) { 

						int[] p = tmp.getVectorPixelXYZTByte(x, y, 0, 0);
						values[ s * totalHistoSizePerScale 
						        + p[0] * this.size[1] * this.size[2] 
						          + p[1] * this.size[2] 
						          + p[2] ]++;
						nbPresentPix++;
					}
				}
			}
		}

		// normalize
		if ( nbPresentPix > 0 )
			for ( int i = 0 ; i < values.length ; i++ )
				values[i] = values[i] / nbPresentPix;

		this.output = new HistogramData();
		this.output.setDescriptor( ( Class ) this.getClass() );
		this.output.setValues( values );
	}

	public static double distance( Data d1, Data d2 ) { 

		double[] v1 = (double[]) d1.getValues();
		double[] v2 = (double[]) d2.getValues();
		return Tools.pyramidMatchDistance( v1, v2, 3, 7*3*3 );
	}



}
