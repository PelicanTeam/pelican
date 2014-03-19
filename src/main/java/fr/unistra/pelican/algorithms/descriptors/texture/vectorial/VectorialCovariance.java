package fr.unistra.pelican.algorithms.descriptors.texture.vectorial;

import java.awt.Point;

import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Descriptor;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.conversion.GrayToRGB;
import fr.unistra.pelican.algorithms.conversion.RGBToHSV;
import fr.unistra.pelican.algorithms.morphology.vectorial.VectorialErosion;
import fr.unistra.pelican.util.data.DoubleArrayData;
import fr.unistra.pelican.util.vectorial.orders.*;

/**
 * This class computes normalised covariance curve on 4 directions
 * (0,45,90,135) for HSL.
 * 
 *	@author Erchan Aptoula
 *	@author Régis Witz 
 */
public class VectorialCovariance extends Descriptor
{
	public Image input;

	public int length = 25;
	
	public VectorialOrdering vo = new QuantizationBasedLexicographicalOrdering(10);
	
	private double refHue = 0.0;

	/**	Output parameter. */
	public DoubleArrayData output;

	public VectorialCovariance() { 

		super.inputs = "input";
		super.outputs = "output";
		super.options = "length";
	}

	/**	Computes normalised covariance curve on 4 directions (0,45,90,135)
	 *	@param input The input image
	 *	@return The output covariance curve
	 */
	public static DoubleArrayData exec( Image input ) { 
		return ( DoubleArrayData ) new VectorialCovariance().process( input );
	}


	/**	Computes normalised covariance curve on 4 directions (0,45,90,135)
	 *	@param input The input image
	 *	@param length The length of the covariance curve for each orientation
	 *	@return The output covariance curve
	 */
	public static DoubleArrayData exec( Image input, int length ) { 
		return ( DoubleArrayData ) new VectorialCovariance().process( input, length );
	}

	/**	@see fr.unistra.pelican.Algorithm#launch() */
	@SuppressWarnings("unchecked")
	public void launch()
	{
		if ( this.input.getBDim() != 3 ) this.input = GrayToRGB.exec( this.input );
		input = RGBToHSV.exec(input);
		
		int size = length * 4 * input.getBDim();

		Double[] curve= new Double[ size ];
		for ( int i = 0 ; i < size ; i++ ) curve[i] = new Double(0);
		
		double[] originalVolumes = new double[input.getBDim()];
		
		for (int b = 0; b < input.getBDim(); b++)
			originalVolumes[b] = volume(input, b);
		
		// every size
		for ( int i = 1 ; i <= length ; i++ ) { 

			int side = i * 2 + 1;

			// vertical line_____________________________
			BooleanImage se = new BooleanImage( side,side, 1,1,1 );
			se.setCenter( new Point( i,i ) );
			se.setPixelXYBoolean( 0,i, true );
			se.setPixelXYBoolean( side-1,i, true );

			Image tmp = VectorialErosion.exec( input,se,vo );

			for (int b = 0; b < input.getBDim(); b++)
				curve[ b*4*length + i-1 ] = volume( tmp,b ) / originalVolumes[b];

			// left diagonal line________________________
			se = new BooleanImage( side,side, 1,1,1 );
			se.setCenter( new Point( i,i ) );
			se.setPixelXYBoolean( 0,0, true );
			se.setPixelXYBoolean( side-1,side-1, true );

			tmp = VectorialErosion.exec( input,se,vo );
			
			for (int b = 0; b < input.getBDim(); b++)
				curve[ b*4*length + length + i-1 ] = volume( tmp,b ) / originalVolumes[b];
			
			// horizontal line___________________________
			se = new BooleanImage( side,side, 1,1,1 );
			se.setCenter( new Point( i,i ) );
			se.setPixelXYBoolean( i,0, true );
			se.setPixelXYBoolean( i,side-1, true );
			
			tmp = VectorialErosion.exec( input,se,vo );
			
			for (int b = 0; b < input.getBDim(); b++)
				curve[ b*4*length + 2*length + i-1 ] = volume( tmp,b ) / originalVolumes[b];
			
			// right diagonal line_______________________
			se = new BooleanImage( side,side, 1,1,1 );
			se.setCenter( new Point( i,i ) );
			se.setPixelXYBoolean( side-1,0, true );
			se.setPixelXYBoolean( 0,side-1, true );
			
			tmp = VectorialErosion.exec( input,se,vo );

			for (int b = 0; b < input.getBDim(); b++)
				curve[ b*4*length + 3*length + i-1 ] = volume( tmp,b ) / originalVolumes[b];
		}

		this.output = new DoubleArrayData();
		this.output.setDescriptor( ( Class ) this.getClass() );
		this.output.setValues( curve );
	}

	private double volume(Image img, int channel)
	{
		double d = 0.0;
		
		for ( int x = 0 ; x < img.getXDim() ; x++ ) { 
			for ( int y = 0 ; y < img.getYDim() ; y++ ) { 

				if ( img.isPresentXYB( x,y,channel ) ) { 

					double tmp = img.getPixelXYBDouble(x, y, channel);
					if( channel == 0 ) { 

						double abs = Math.abs( refHue-tmp );
						if(abs <= 0.5) tmp = abs;
						else tmp = 1.0 - abs;
						abs = 2 * abs;
					}
					d += tmp;
				}
			}
		}
		
		return d;
	}

//	public static double distance( Data d1, Data d2 ) { 
//
//		Double[] v1 = (Double[]) d1.getValues();
//		Double[] v2 = (Double[]) d2.getValues();
//		return Tools.euclideanDistance( v1,v2 );
//	}



}
