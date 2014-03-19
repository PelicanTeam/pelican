package fr.unistra.pelican.algorithms.descriptors.localinvariants;



import java.util.ArrayList;

import fr.unistra.pelican.*;
import fr.unistra.pelican.algorithms.conversion.AverageChannels;
import fr.unistra.pelican.algorithms.detection.Harris;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.util.Keypoint;
import fr.unistra.pelican.util.Point4D;
import fr.unistra.pelican.util.data.*;



/** 
 *	Chun-Rong Huanga, Chu-Song Chena and Pau-Choo Chung,
 *	"Contrast context histogram - An efficient discriminating 
 *	local descriptor for object recognition and image matching"
 *
 *	( It's here:	http://dx.doi.org/10.1016/j.patcog.2008.03.013	)
 *
 *	@author Régis Witz
 */
public class CCH extends Descriptor {

	  ////////////
	 // INPUTS //
	////////////

	public Image input;

	  /////////////
	 // OUTPUTS //
	/////////////

	/**	Output parameter. */
	public KeypointArrayData output;

	  /////////////
	 // OPTIONS //
	/////////////

	/**	Circular window radius, in pixels. */
	public int radius = 30;
	public int distanceQuantization = 3;
	public int orientationQuantization = 8;

	  //////////////////
	 // OTHER FIELDS //
	//////////////////

	int rstep;
	double tethastep;

	  ///////////////
	 // CONSTANTS //
	///////////////

	

	  /////////////////
	 // CONSTRUCTOR //
	/////////////////

	public CCH() { 

		super.inputs = "input";
		super.options = "";
		super.outputs = "output";
	}

	  ////////////////////
	 // "EXEC" METHODS //
	////////////////////

	public static KeypointArrayData exec( Image input ) { 
		return ( KeypointArrayData ) new CCH().process( input );
	}

	  /////////////////////
	 // "LAUNCH" METHOD //
	/////////////////////

	@SuppressWarnings("unchecked")
	@Override
	public void launch() throws AlgorithmException {

		if ( this.input.getBDim() > 1 ) this.input = (Image) AverageChannels.exec( this.input );
		ArrayList<Keypoint> pcs = Harris.exec( this.input );

		this.rstep = new Double ( this.radius / (double)this.distanceQuantization ).intValue();
		this.tethastep = 2*Math.PI / this.orientationQuantization;
		int roffset;
		double tethaoffset;

		HistogramData histo;
		HistogramData[] desc;
		for ( Keypoint key : pcs ) { 

			desc = new HistogramData[ this.distanceQuantization*this.orientationQuantization ];
			for ( int k = 0 ; k < this.distanceQuantization ; k++ ) { 
				
				roffset = k*rstep;
				for ( int l = 0 ; l < this.orientationQuantization ; l++ ) { 

					tethaoffset = l*tethastep;
					histo = this.computeRegion( roffset,tethaoffset, key );
					desc[ k*this.orientationQuantization + l ] = ( HistogramData ) histo.clone();
				}
			}
			DataArrayData data = new DataArrayData();
			data.setDescriptor( ( Class ) this.getClass() );
			data.setValues( desc );
			key.data = data;
		}

		this.output = new KeypointArrayData();
		this.output.setDescriptor( ( Class ) this.getClass() );
		this.output.setValues( pcs );

	} 





	  ///////////////////
	 // OTHER METHODS //
	///////////////////

	private HistogramData computeRegion( int roffset, double tethaoffset, Keypoint pc ) { 

		ArrayList<Double> contrasts = new ArrayList<Double>();
		Point4D p;
		for ( int r = roffset ; r < roffset+this.rstep ; r++ )
			for ( double tetha = tethaoffset ; tetha < tethaoffset+this.tethastep ; tetha+=.2 ) { 

				p = CCH.polar2cartesian( r,tetha );
				contrasts.add( this.getCenterBasedContrast( p.x,p.y, pc ) );
			}
		return this.getHistogram( contrasts );
	}

	@SuppressWarnings( "unchecked" )
	private HistogramData getHistogram( ArrayList<Double> contrasts ) { 

		Double[] histo = { 0.,0. };	// contrast histograms
		int nbp = 0 ;				// number of positive contrast values
		int nbn = 0;				// number of negative contrast values
		for ( double val : contrasts ) { 

			if ( val >= 0 ) { 

				histo[0] += val;
				nbp++;
			} else { 

				histo[1] += val;
				nbn++;
			}
		}

		// normalize
		if ( nbp > 0 ) histo[0] /= nbp;	// positive contrast histogram
		if ( nbn > 0 ) histo[1] /= nbn;	// negative contrast histogram

		assert( 0 <= histo[0] && histo[0] <= 1
			&& -1 <= histo[1] && histo[1] <= 0
			&& nbp+nbn == contrasts.size() );

		HistogramData data = new HistogramData();
		data.setDescriptor( ( Class ) this.getClass() );
		data.setValues( histo );

//		System.out.println( "CCH set histogram { "+histo[0]+";"+histo[1]+" }" );
//		System.out.flush();
		return data;
	}

	private double getCenterBasedContrast( int i, int j, Keypoint pc ) { 

		if ( i < 0 || i > this.input.getXDim() 
		  || j < 0 || j > this.input.getYDim() ) return 0;
	
		double ip = this.input.getPixelXYDouble( i,j ); 
		double ipc = this.input.getPixelXYDouble( new Double( pc.x ).intValue(), 
												  new Double( pc.y ).intValue() );
		return ip - ipc;
	}

	private static Point4D polar2cartesian( double r, double tetha ) { 

		int x = new Double( r * Math.cos( tetha ) ).intValue();
		int y = new Double( r * Math.sin( tetha ) ).intValue();
		return new Point4D( x,y );
	}






	@SuppressWarnings( "unchecked" )
	public static double distance( Data d1, Data d2 ) { 

		ArrayList<Keypoint> values = 
			( ArrayList<Keypoint> ) ( ( KeypointArrayData ) d1 ).getValues();
		ArrayList<Keypoint> values2 = 
			( ArrayList<Keypoint> ) ( ( KeypointArrayData ) d2 ).getValues();

		int bins = values.get(0).getDescLength();
		int bins2 = values2.get(0).getDescLength();
		if( bins != bins2 ) { 

			System.err.println( "Incompatible keypoint descriptors lengths !" );
			return Double.MAX_VALUE;
		}

		double distance = 0, d, min;
		for ( Keypoint k1 : values ) { 

			min = 1;
			for ( Keypoint k2 : values2 ) { 

				d = CCH.match( k1,k2 );
				if ( d < 1 && d < min ) min = d;
			}
			distance += min;
		}
		if ( bins > 0 ) distance /= bins;

		assert 0 <= distance && distance <= 1 : 
			new CCH().getClass().getName() + " distance ¤[0;1] unverified : " + distance + ".";


		return distance;
	}

	public static double match( Keypoint k1, Keypoint k2 ) { 
		
//		HistogramData[] desc1 = ( HistogramData[] ) k1.data.getValues();
//		HistogramData[] desc2 = ( HistogramData[] ) k2.data .getValues();
		Data[] desc1 = ( Data[] ) k1.data.getValues();
		Data[] desc2 = ( Data[] ) k2.data .getValues();

		int bins = desc1.length;
		if ( bins != desc2.length ) { 

			System.err.println( "Incompatible keypoint descriptors lengths !" );
			return 1.0;
		}

		Double[] h1;
		Double[] h2;
		double distance = 0;
		for ( int i = 0 ; i < bins ; i++ ) { 

			h1 = ( Double[] ) desc1[i].getValues();
			h2 = ( Double[] ) desc2[i].getValues();

			assert(	h1.length == 2 && h2.length == 2 
				 && h1[0] >= 0 && h2[0] >= 0 
				 && h1[1] <= 0 && h2[1] <= 0 );

			distance += Math.abs( h1[0]-h2[0] );
			distance += Math.abs( h1[1]-h2[1] );
		}
		if ( bins > 0 ) distance /= bins;

		assert 0 <= distance && distance <= 1 : 
			new CCH().getClass().getName() + " distance ï¿½[0;1] unverified : " + distance + ".";

		return distance;
	}





	public static void main( String[] args ) { 

		Image img = ImageLoader.exec("samples/lenna256.png");
		KeypointArrayData data1 = CCH.exec( img );
		Image img2 = ImageLoader.exec("samples/cat with umbrella.png");
		KeypointArrayData data2 = CCH.exec( img2 );

		long t31 = System.currentTimeMillis();
		double d = data1.distanceTo( data2 );
		long t32 = System.currentTimeMillis();
		System.out.println( "1vs2 matching time : " + ( t32-t31 )/1000.0 + " s." );
		System.out.println( "distance : " + d + "." );
	}


}
