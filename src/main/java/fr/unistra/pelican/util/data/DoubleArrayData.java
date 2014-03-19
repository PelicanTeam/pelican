package fr.unistra.pelican.util.data;

import fr.unistra.pelican.util.Offset;
import fr.unistra.pelican.util.data.distances.DoubleArrayEuclideanDistance;

/**
 * Class representing the double array data type.
 * 
 *	@author lefevre
 *	@author Régis Witz (clone,toString,getParsedInstance)
 * 
 */
public class DoubleArrayData extends Data {

	/**	The effective data stored in the object. */
	Double[] values;



	@Override
	public double distance( Data data ) 
	{ 
		return new DoubleArrayEuclideanDistance().distance( this, data );
	}

	@Override
	public Object getValues()				{ return values; }

	@Override
	public void setValues(Object values)	{ this.values = ( Double[] ) values; }

	@Override
	public boolean equals( Data data ) { 

		Double[] values2 = ( ( DoubleArrayData ) data ).values;

		if ( this.values.length != values2.length ) return false;

		for ( int i = 0 ; i < this.values.length ; i++ ) 
			if ( this.values[i] != values2[i] ) return false;

		return true;
	}

	@Override
	public DoubleArrayData clone() { 

		DoubleArrayData data = new DoubleArrayData();
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
	public static DoubleArrayData getParsedInstance( String[] words, Offset c ) { 

		DoubleArrayData data = null;
		Double [] values;
		assert words[c.offset].startsWith( 
				"<DATA="+new DoubleArrayData().getClass().getName() ) : 
				"Wrong position of offset " + c.offset + ": \"" + words[c.offset] + "\".";
		c.offset++; // pass <Data=...>

		if ( c.offset < words.length ) { 

			data = new DoubleArrayData();
			try { 
				Class desc = Class.forName( words[c.offset++] );
				data.setDescriptor( desc );

			} catch( ClassNotFoundException ex ) { ex.printStackTrace(); }

			values = new Double[ new Integer( words[c.offset++] ) ];
			for ( int i = 0 ; i < values.length ; i++ ) 
				values[i] = new Double( words[c.offset++] );
			c.offset++; // pass </DATA>
			data.setValues( values );
		}

		return data;
	}



}
