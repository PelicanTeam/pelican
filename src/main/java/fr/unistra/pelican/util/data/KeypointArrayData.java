package fr.unistra.pelican.util.data;

import java.util.ArrayList;

import fr.unistra.pelican.util.Keypoint;
import fr.unistra.pelican.util.Offset;
import fr.unistra.pelican.util.data.distances.KeypointArrayEuclideanDistance;



/**
 *	Represents the "array of keypoints" datatype.
 *	Useful for SIFT-like descriptors.
 *
 *	@see Keypoint
 *
 *	@author Régis Witz
 *	@date 27.01.09
 */
public class KeypointArrayData extends Data {

	/**	The effective data stored in the object. */
	ArrayList<Keypoint> values = new ArrayList<Keypoint>();



	/**	This is a SURF comparison. 
	 *	Note that only the laplacian and descriptor[i] values are used for matching.
	 *	x,y,scale and orientation where put in output for exhaustivity and coherence, but 
	 *	they can be thrown away of class Keypoint if one day memory cost is a problem.
	 *
	 *	@param data A <tt>KeypointArrayData</tt> with wich <tt>this</tt> must be compared.
	 *	@return Distance in ]0;1] ( ->0:near,1:far ) between the two keypoints arrays.
	 */
	@Override
	public double distance( Data data ) {

		return new KeypointArrayEuclideanDistance().distance( this, data );
	}

	@Override
	public boolean equals( Data data ) {

		ArrayList<Keypoint> values2 = ( ( KeypointArrayData ) data).values;
		int size = this.values.size();
		if ( size != values2.size() ) return false;
		Keypoint k1,k2;
		for ( int index = 0 ; index < size ; index++ ) { 

			k1 = this.values.get( index );
			k2 = this.values.get( index );
			if ( !k1.equals( k2 ) ) return false;
		}
		return true;
	}

	@Override
	public Object getValues() {
		return this.values;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setValues(Object values) {
		this.values = ( ArrayList<Keypoint> ) values;
	}

	@Override
	public Data clone() { 

		DoubleArrayData data = new DoubleArrayData();
		data.setDescriptor( this.getDescriptor() );
		data.setValues( this.values.clone() );
		return data;
	}


	@Override
	public String toString() { 

		String s = "<DATA="+this.getClass().getName()+">";
		if ( this.getDescriptor() != null ) s += "," + this.getDescriptor().getName();
		s += "," + this.values.size();
		for ( Keypoint point : this.values ) s += "," + point.toString();
		s += ",</DATA>";
		return s;
	}

	@SuppressWarnings( "unchecked" )
	public static KeypointArrayData getParsedInstance( String [] words, Offset c ) { 

		KeypointArrayData data = null;
		ArrayList<Keypoint> values = new ArrayList<Keypoint>();
		int size;
		assert words[c.offset].startsWith( 
				"<DATA="+new KeypointArrayData().getClass().getName() ) : 
			"Wrong position of offset " + c.offset + ": \"" + words[c.offset] + "\".";
		c.offset++; // pass <Data=...>

		if ( c.offset < words.length ) { 

			data = new KeypointArrayData();
			try { 
				Class desc = Class.forName( words[c.offset++] );
				data.setDescriptor( desc );

			} catch( ClassNotFoundException ex ) { ex.printStackTrace(); }

			size = new Integer( words[c.offset++] );
			for ( int i = 0 ; i < size ; i++ ) { 

				Keypoint point = Keypoint.getParsedInstance( words,c );
				values.add( point );
			}
			c.offset++; // pass </DATA>
			data.setValues( values );
		}
		return data;
	}



}
