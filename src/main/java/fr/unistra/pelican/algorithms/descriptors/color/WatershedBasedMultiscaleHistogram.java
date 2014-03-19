package fr.unistra.pelican.algorithms.descriptors.color;

import fr.unistra.pelican.*;
import fr.unistra.pelican.algorithms.conversion.*;
import fr.unistra.pelican.algorithms.morphology.gray.*;
import fr.unistra.pelican.algorithms.morphology.vectorial.gradient.ColourHSYWeightedGradient;
import fr.unistra.pelican.algorithms.segmentation.Watershed2;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToValleyColor;
import fr.unistra.pelican.util.data.Data;
import fr.unistra.pelican.util.data.HistogramData;
import fr.unistra.pelican.util.data.distances.HistogramPyramidMatchDistance;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;



/**
 *	Normalised histograms in a watershed-leveling based scale-space
 *	HSV 7-3-3
 * 
 *	@author Erchan Aptoula
 *	@author Régis Witz (mask support and framework adaptation)
 */
public class WatershedBasedMultiscaleHistogram extends Descriptor
{
	public int scales = 3;
	public int[] size = {7,3,3};

	/**	First input parameter. */
	public Image input;

	/**	Output parameter. */
	public HistogramData output;

	/**	Constructor */
	public WatershedBasedMultiscaleHistogram() { 

		super();
		super.inputs = "input";
		super.options = "size,scales";
		super.outputs = "output";
		
	}

	public static HistogramData exec( Image input ) { 
		return ( HistogramData ) new WatershedBasedMultiscaleHistogram().process( input );
	}

	/**
	 *	@param input Image to be converted in a histogram.
	 *	@param size The number of bins of each band.
	 *	@param scales The number of scales.
	 *	@return The normalized histogram.
	 */
	public static HistogramData exec( Image input, int[] size, int scales ) { 
		return ( HistogramData ) new WatershedBasedMultiscaleHistogram().process(input,size,scales);
	}

	/**	@see fr.unistra.pelican.Algorithm#launch() */
	@SuppressWarnings("unchecked")
	public void launch() throws AlgorithmException { 

		int nbpix = 0;
		int totalHistoSizePerScale = 7 * 3 * 3;

		if ( input.getBDim() == 1 ) input = GrayToRGB.exec( input );
//		input = RGBToLSH.exec(input);
		input = RGBToHSV.exec(input);
		input = NonUniformHSVQuantization733.exec(input);

		int sizeTotal = scales * totalHistoSizePerScale;
		Double[] values = new Double[ sizeTotal ];
		for ( int i = 0 ; i < sizeTotal ; i++ ) values[i] = new Double(0);

		for (int s = 0; s < scales; s++) {

			// prepare this scale
			Image tmp = null;
			
			// level it
			BooleanImage se = FlatStructuringElement2D.createSquareFlatStructuringElement(5);
			Image marker = (Image) new GrayASF().process(
					input, se, GrayASF.OPENING_FIRST, new Integer(s * 2 + 1));
			tmp = (Image) new GrayLeveling().process(input, marker, new Integer(0));
			
			// get the color gradient and apply a watershed
			Image gradient = ColourHSYWeightedGradient.exec( tmp,
					FlatStructuringElement2D.createSquareFlatStructuringElement(3),
					ColourHSYWeightedGradient.ADAPTIVE,10.0,0.5 );
			Image segmented = Watershed2.exec(gradient);

			// set each basin to its average colour
			tmp = LabelsToValleyColor.exec(segmented,gradient,tmp);

			// and now extract the histogram
			for ( int x = 0 ; x < input.getXDim() ; x++ ) { 
				for ( int y = 0 ; y < input.getYDim() ; y++ ) { 

					if ( this.input.isPresentXY( x,y ) ) { 

						int[] p = tmp.getVectorPixelXYZTByte(x, y, 0, 0);
						values[ s * totalHistoSizePerScale 
						        + p[0] * size[1] * size[2] 
						        + p[1] * size[2] 
						        + p[2] ]++;
					}
				}
			}
		}

		// normalize
		if ( nbpix > 0 ) 
			for ( int i = 0 ; i < values.length ; i++ )
				values[i] = values[i] / nbpix;

		this.output = new HistogramData();
		this.output.setDescriptor( ( Class ) this.getClass() );
		this.output.setValues( values );
	}

	public static double distance( Data d1, Data d2 ) { 

//		Double[] v1 = (Double[]) d1.getValues();
//		Double[] v2 = (Double[]) d2.getValues();
//		return Tools.pyramidMatchDistance( v1, v2, 3, 7*3*3 );
		return new HistogramPyramidMatchDistance().distance( d1,d2 );
	}



}
