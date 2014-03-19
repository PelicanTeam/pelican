package fr.unistra.pelican.util.data;

import fr.unistra.pelican.util.Offset;



/**
 *	
 *	@author Régis Witz
 */
public class HistogramData extends DoubleArrayData {



	@Override
	public double distance( Data data ) {

		Double[] h1 = this.values;
		Double[] h2 = ( ( DoubleArrayData ) data ).values; // let's be permissive..

		int bins = h1.length;
		if ( bins != h2.length ) { 

			System.err.println("Incompatible histogram bin numbers : "+bins+" vs "+h2.length +".");
			return 1.0;
		}

		double distance = 0.0;
		for ( int i = 0 ; i < bins ; i++ ) distance += Math.abs( h1[i]-h2[i] );
		if ( bins > 0 ) distance /= bins;

		assert 0 <= distance && distance <= 1 : 
			this.getClass().getName() + " distance €[0;1] unverified : " + distance + ".";

		return distance;
	}



	public void normalize() { 

		if ( this.values == null ) return;

		int len = this.values.length;
		double norm = 0;
		for ( int i = 0 ; i < len ; i++ ) norm += this.values[i]*this.values[i];
		norm = Math.sqrt( norm );
		if ( norm == 0 ) return;
		if ( norm != 1 ) for ( int i = 0 ; i < len ; i++ ) this.values[i] /= norm;
	}

	@SuppressWarnings( "unchecked" )
	public static HistogramData getParsedInstance( String [] words, Offset c ) { 

		HistogramData data = null;
		Double [] values;
		assert words[c.offset].startsWith( 
				"<DATA="+new HistogramData().getClass().getName() ) : 
				"Wrong position of offset " + c.offset + ": \"" + words[c.offset] + "\".";
		c.offset++; // pass <Data=...>

		if ( c.offset < words.length ) { 

			data = new HistogramData();
			try { 
				Class desc = Class.forName( words[c.offset++] );
				data.setDescriptor( desc );

			} catch( ClassNotFoundException ex ) { ex.printStackTrace(); }

			values = new Double[ Integer.parseInt( words[c.offset++] ) ];
			for ( int i = 0 ; i < values.length ; i++ ) 
				values[i] = Double.parseDouble( words[c.offset++] );
			c.offset++; // pass </DATA>
			data.setValues( values );
		}
		return data;
	}

	@Override
	public HistogramData clone() { 

		HistogramData data = new HistogramData();
		data.setDescriptor( this.getDescriptor() );
		data.setValues( this.values.clone() );
		return data;
	}



}
