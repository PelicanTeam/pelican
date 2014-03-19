package fr.unistra.pelican.util.data;

import fr.unistra.pelican.util.Offset;
import fr.unistra.pelican.util.data.distances.DoubleArray2DEuclideanDistance;

/**
 * Class representing the double array data 2D type.
 * 
 *	@author Jonathan Weber
 * 
 */
public class DoubleArray2DData extends Data {

	/**	The effective data stored in the object. */
	Double[][] values;



	@Override
	public double distance( Data data ) 
	{ 
		return new DoubleArray2DEuclideanDistance().distance( this, data );
	}

	@Override
	public Object getValues()				{ return values; }

	@Override
	public void setValues(Object values)	{ this.values = ( Double[][] ) values; }

	@Override
	public boolean equals( Data data ) { 

		Double[][] values2 = ( ( DoubleArray2DData ) data ).values;

		if ( this.values.length != values2.length ) return false;

		for ( int i = 0 ; i < this.values.length ; i++ )
		{
			if ( this.values[i].length != values2[i].length ) return false;
			for ( int j = 0 ; j < this.values[i].length ; j++ )
				if ( this.values[i][j] != values2[i][j] ) return false;

		}
		return true;
	}

	@Override
	public DoubleArray2DData clone() { 

		DoubleArray2DData data = new DoubleArray2DData();
		data.setDescriptor( this.getDescriptor() );
		data.setValues( this.values.clone() );
		return data;
	}

	@Override
	public String toString() { 

		String s = "<DATA="+this.getClass().getName()+">";
		if ( this.getDescriptor() != null ) s += "," + this.getDescriptor().getName();
		s += "," + this.values.length;
		for ( int i = 0 ; i < this.values.length ; i++ ) s += "," + this.values[i].toString();
		s += ",</DATA>";
		return s;
	}

	@SuppressWarnings( "unchecked" )
	public static DoubleArray2DData getParsedInstance( String[] words, Offset c ) { 

		DoubleArray2DData data = null;
		Double [][] values;
		assert words[c.offset].startsWith( 
				"<DATA="+new DoubleArray2DData().getClass().getName() ) : 
				"Wrong position of offset " + c.offset + ": \"" + words[c.offset] + "\".";
		c.offset++; // pass <Data=...>

		if ( c.offset < words.length ) { 

			data = new DoubleArray2DData();
			try { 
				Class desc = Class.forName( words[c.offset++] );
				data.setDescriptor( desc );

			} catch( ClassNotFoundException ex ) { ex.printStackTrace(); }

			values = new Double[ new Integer( words[c.offset++] ) ][ new Integer( words[c.offset++] )];
			for ( int i = 0 ; i < values.length ; i++ ) 
				for ( int j = 0 ; j < values[i].length ; j++ )
					values[i][j] = new Double( words[c.offset++] );
			c.offset++; // pass </DATA>
			data.setValues( values );
		}

		return data;
	}



}
