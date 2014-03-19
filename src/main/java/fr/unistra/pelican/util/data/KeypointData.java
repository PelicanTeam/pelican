package fr.unistra.pelican.util.data;

import fr.unistra.pelican.util.Keypoint;
import fr.unistra.pelican.util.Offset;

public class KeypointData extends Data {



	protected Keypoint point;



	@Override
	public Data clone() {

		KeypointData data = new KeypointData();
		data.setDescriptor( this.getDescriptor() );
		data.setValues( this.point.clone() );
		return data;
	}

	@Override
	public double distance( Data data ) { 

		Keypoint key = ( Keypoint ) data.getValues();
		return this.point.data.distance( key.data );
	}

	@Override
	public boolean equals( Data data ) { 

		Keypoint key = ( Keypoint ) data.getValues();
		return key.equals( this.point );
	}

	@Override
	public Object getValues()				{ return this.point; }

	@Override
	public void setValues( Object values )	{ this.point = ( Keypoint ) values; }

	@Override
	public String toString() { 

		String s = "<DATA="+this.getClass().getName()+">";
		if ( this.getDescriptor() != null ) s += "," + this.getDescriptor().getName();
		s += "," + this.point.toString() + ",</DATA>";
		return s;
	}

	@SuppressWarnings( "unchecked" )
	public static KeypointData getParsedInstance( String [] words, Offset c ) { 

		KeypointData data = null;
//		while ( !words[c.offset++].startsWith( 
//				"<DATA="+new KeypointData().getClass().getName() ) ) ;

		Keypoint point = Keypoint.getParsedInstance( words, c );

		if ( c.offset < words.length ) { 

			data = new KeypointData();
			try { 
				Class desc = Class.forName( words[c.offset++] );
				data.setDescriptor( desc );

			} catch( ClassNotFoundException ex ) { ex.printStackTrace(); }
			data.setValues( point );
		}
		return data;
	}

}
