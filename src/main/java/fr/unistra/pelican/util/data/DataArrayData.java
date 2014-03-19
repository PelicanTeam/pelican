package fr.unistra.pelican.util.data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import fr.unistra.pelican.util.Offset;



/**
 *	Represents an array of instances of {@link Data}.
 *	These objects must not necessary be of the same class.
 *
 *	@author Régis Witz
 */
public class DataArrayData extends Data { 

	/**	The effective data stored in the object. */
	Data[] values;



	@Override
	public Data clone() {

		DataArrayData data = new DataArrayData();
		data.setDescriptor( this.getDescriptor() );
		data.setValues( this.values.clone() );
		return data;
	}

	@Override
	public double distance( Data data ) { 

		Data[] values2 = ( ( DataArrayData ) data ).values;

		int len = this.values.length;
		if ( len != values2.length ) return 1;

		double distance = 0;
		for ( int i = 0 ; i < this.values.length ; i++ ) 
			distance += this.values[i].distance( values2[i] );
		if ( len > 0 ) distance /= (double) len;

		assert 0 <= distance && distance <= 1 : 
			this.getClass().getName() + " distance €[0;1] unverified : " + distance + ".";

		return distance;
	}

	@Override
	public boolean equals( Data data ) { 

		Data[] values2 = ( ( DataArrayData ) data).values;

		if ( this.values.length != values2.length ) return false;

		for ( int i = 0; i < this.values.length ; i++ )
			if ( !this.values[i].equals( values2[i] ) ) return false;

		return true;
	}

	@Override
	public Object getValues()				{ return this.values; }

	@Override
	public void setValues( Object values )	{ this.values = ( Data[] ) values; }

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
	public static DataArrayData getParsedInstance( String[] words, Offset c ) { 

		DataArrayData data = null;
		Data [] values;
		assert words[c.offset].startsWith( 
				"<DATA="+new DataArrayData().getClass().getName() ) : 
				"Wrong position of offset " + c.offset + ": \"" + words[c.offset] + "\".";
		c.offset++; // pass <Data=...>

		if ( c.offset < words.length ) { 

			data = new DataArrayData();
			try { 
				Class desc = Class.forName( words[c.offset++] );
				data.setDescriptor( desc );

			} catch( ClassNotFoundException ex ) { ex.printStackTrace(); }

			values = new Data[ new Integer( words[c.offset++] ) ];
			for ( int i = 0 ; i < values.length ; i++ ) { 

				String dataClassName = null;
				Class dataClass = null;
				Method method;

				int start = words[c.offset].indexOf( "<DATA=" );
				int end = words[c.offset].indexOf( ">" );
				dataClassName = words[c.offset].substring( start+6,end );
				try { dataClass = Class.forName( dataClassName ); }
				catch ( ClassNotFoundException ex ) { ex.printStackTrace(); }

				try { 

					method = dataClass.getMethod(	"getParsedInstance", 
													String[].class, 
													Offset.class ); 
					values[i] = ( Data ) method.invoke( null,words,c );
				} 
				catch ( IllegalAccessException ex ) { ex.printStackTrace(); }
				catch ( InvocationTargetException ex ) { ex.printStackTrace(); }
				catch ( NoSuchMethodException ex ) { ex.printStackTrace(); }
			}
			c.offset++; // pass </DATA>
			data.setValues( values );
		}
		return data;
	}



}
