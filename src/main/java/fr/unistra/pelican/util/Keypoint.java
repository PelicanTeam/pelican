package fr.unistra.pelican.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import fr.unistra.pelican.util.data.Data;
import fr.unistra.pelican.util.data.DataArrayData;
import fr.unistra.pelican.util.data.DoubleArrayData;



/**
 *	An interest point for using by interest points descriptors.
 *
 *	@author Régis Witz
 */
public class Keypoint implements Cloneable,Comparable<Keypoint> { 

	  ////////////
	 // FIELDS //
	////////////

	/**	Coordinates of the detected interest point */
	public double x, y;
//	public double[] descriptor;
	public Data data;



	  //////////////////
	 // CONSTRUCTORS //
	//////////////////

	public Keypoint( double x, double y ) { 

		this.x = x;
		this.y = y;
	}

	public Keypoint( double x, double y, Data data ) { 

		this( x,y );
		this.data = data;
	}

//	public Keypoint( double x, double y, double value ) {
//
//		this( x,y );
//		this.descriptor = new double[1];
//		this.descriptor[0] = value;
//	}
//
//	public Keypoint( double x, double y, double[] descriptor ) {
//
//		this( x,y );
//		this.descriptor = new double[ descriptor.length ];
//		for ( int i = 0 ; i < this.descriptor.length ; i++ ) this.descriptor[i] = descriptor[i];
//	}

	public Keypoint( Keypoint arg ) {  

		this.x = arg.x;
		this.y = arg.y;
//		if ( arg.descriptor != null ) { 
//
//			this.descriptor = new double[ arg.descriptor.length ];
//			for ( int i = 0 ; i < this.descriptor.length ; i++ ) descriptor[i] = arg.descriptor[i];
//		}
		if ( arg.data != null ) this.data = arg.data.clone();
	}



	  /////////////
	 // METHODS //
	/////////////

	public Keypoint clone()					{ return new Keypoint( this ); }

	public boolean equals( Keypoint key )	{ 

		if ( this.x != key.x ) return false;
		if ( this.y != key.y ) return false;
//		if ( this.descriptor == null ) 
//			if ( key.descriptor == null ) return true;
//			else return false;
//		else
//			if ( key.descriptor != null ) { 
//
//				if ( this.descriptor.length != key.descriptor.length ) return false;
//				for ( int index = 0 ; index < this.descriptor.length ; index++ ) 
//					if ( this.descriptor[ index ] != key.descriptor[ index ] ) return false;
//
//			} else return false;

		if ( this.data == null ) 
			if ( key.data == null ) return true;
			else return false;
		else
			if ( key.data != null ) return this.data.equals( key.data );
			else return false;
	}

	public int getDescLength() { 

		Object[] array;
		if ( this.data instanceof DoubleArrayData
		  || this.data instanceof DataArrayData ) 
			array = ( Object[] ) this.data.getValues();
		else return -1;

		if ( array == null ) return -1;
		else return array.length; 
	}
//
//	public void setVal( double val ) { 
//
//		DoubleArrayData data = new DoubleArrayData();
//		this.descriptor = new double[1];
//		this.descriptor[0] = val;
//	}
//
//	public double getVal() { 
//
//		// no "nil" check
//		return this.descriptor[0];
//	}

	public String toString() { 

//		String s = "Keypoint: [ ( "+this.x+","+this.y+" ) " +
////				"- scale:"+this.scale+" " +
////				"- laplacian:"+this.laplacian+" " +
////				"- orientation:"+this.orientation+" " +
//				"- descriptor okay ? "+ ( this.data != null ) +
//				" ]";
		String s = "<KEYPOINT="+this.getClass().getName()+">,"
				+ this.x + "," + this.y + "," + this.data
				+ ",</KEYPOINT>";

		return s;
	}

	@SuppressWarnings("unchecked")
	public static Keypoint getParsedInstance( String [] words, Offset c ) { 

		Keypoint point = null;
		double x,y;
		Data data = null;
		assert words[c.offset].startsWith( 
				"<KEYPOINT="+Keypoint.class.getName() ) : 
			"Wrong position of offset " + c.offset + ": \"" + words[c.offset] + "\".";
		c.offset++;

		int start,end;
		String dataClassName = null, s;
		Class<Data> dataClass = null;
		Method method;

		if ( c.offset < words.length ) { 

			x = Double.parseDouble( words[c.offset++] );
			y = Double.parseDouble( words[c.offset++] );

			s = words[c.offset];
			start = s.indexOf( "<DATA=" );
			end = s.indexOf( ">" );
			dataClassName = s.substring( start+6,end );
			try { dataClass = ( Class ) Class.forName( dataClassName ); }
			catch ( ClassNotFoundException ex ) { ex.printStackTrace(); }

			try { 
				
				method = dataClass.getMethod(	"getParsedInstance", 
												String[].class, 
												Offset.class ); 
				data = ( Data ) method.invoke( null,words,c );
			} 
			catch ( IllegalAccessException ex ) { ex.printStackTrace(); }
			catch ( InvocationTargetException ex ) { ex.printStackTrace(); }
			catch ( NoSuchMethodException ex ) { ex.printStackTrace(); }

			c.offset++; // pass </KEYPOINT>
			point = new Keypoint( x,y,data );
		}
		return point;
	}

	/**	This means nothing, except for this being used as a simple ( x,y, <tt>somevalue<tt> ) 
	 *	keypoint and <tt>somevalue<tt> being stored in scale (used in Harris).
	 *	@param key 
	 *	@return result 
	 */
	public int compareTo( Keypoint key ) {

//		if ( this.descriptor == null ) 
//			if ( key.descriptor == null ) return 0;
//			else return -1;
//		else 
//			if ( key.descriptor == null ) return 1;
//			else return new Double( this.getVal()-key.getVal() ).intValue();
		if ( this.data == null ) 
			if ( key.data == null ) return 0;
			else return -1;
		else 
			if ( key.data == null ) return 1;
			else return new Double( this.data.distance( key.data ) ).intValue();
	}
}
