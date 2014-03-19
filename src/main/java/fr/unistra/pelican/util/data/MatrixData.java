package fr.unistra.pelican.util.data;

import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.util.Offset;
import fr.unistra.pelican.util.Pixel;
import fr.unistra.pelican.util.data.distances.MatrixEuclideanDistance;



/**	
 *	Represents a 5D matrix Data.
 *
 *	@author Witz
 */
public class MatrixData extends Data {

	/**	Matrix simulated by an Image, but <b>DO NOT MASK IT !!</b> */
	IntegerImage matrix;

	public int getXDim() { return this.matrix.getXDim(); }
	public int getYDim() { return this.matrix.getYDim(); }
	public int getZDim() { return this.matrix.getZDim(); }
	public int getTDim() { return this.matrix.getTDim(); }
	public int getBDim() { return this.matrix.getBDim(); }

	public double getValue( int x, int y, int z, int t, int b ) { 

		return this.matrix.getPixelXYZTBDouble( x,y,z,t,b );
	}

	public double getValue( Pixel p ) { 

		return this.matrix.getPixelXYZTBDouble( p.x,p.y,p.z,p.t,p.b );
	}

	public void setValue( int x, int y, int z, int t, int b, double value ) { 

		this.matrix.setPixelXYZTBDouble( x,y,z,t,b, value );
	}

	public void setValue( Pixel p, double value ) { 

		this.matrix.setPixelXYZTBDouble( p.x,p.y,p.z,p.t,p.b, value );
	}



	@Override
	public Data clone() { 

		MatrixData data = new MatrixData();
		data.setDescriptor( this.getDescriptor() );
		data.setValues( this.matrix.copyImage( true ) );
		return data;
	}

	@Override
	public double distance( Data data ) { 

		return new MatrixEuclideanDistance().distance( this, data );
	}

	@Override
	public boolean equals( Data data ) { 

		IntegerImage matrix = ( IntegerImage ) ( ( MatrixData ) data ).getValues();
		return this.matrix.equals( matrix );
	}

	@Override
	public Object getValues() { return this.matrix; }

	@Override
	public void setValues( Object values ) { this.matrix = ( IntegerImage ) values; }

	@Override
	public String toString() { 

		String s = "<DATA="+this.getClass().getName()+">";
		if ( this.getDescriptor() != null ) s += "," + this.getDescriptor().getName();
		s += "," + this.getXDim() + 
			 "," + this.getYDim() + 
			 "," + this.getZDim() + 
			 "," + this.getTDim() + 
			 "," + this.getBDim()  
			 ;
		for ( Pixel p : this.matrix ) s += "," + this.getValue( p );
		s += ",</DATA>";
		return s;
	}

	@SuppressWarnings("unchecked")
	public static MatrixData getParsedInstance( String[] words, Offset c ) { 

		MatrixData data = null;
		assert words[c.offset].startsWith( 
				"<DATA="+new MatrixData().getClass().getName() ) : 
				"Wrong position of offset " + c.offset + ": \"" + words[c.offset] + "\".";
		c.offset++; // pass <Data=...>

		if ( c.offset < words.length ) { 

			data = new MatrixData();
			try { 
				Class desc = Class.forName( words[c.offset++] );
				data.setDescriptor( desc );

			} catch( ClassNotFoundException ex ) { ex.printStackTrace(); }

			int xdim,ydim,zdim,tdim,bdim;
			xdim = Integer.parseInt( words[c.offset++] );
			ydim = Integer.parseInt( words[c.offset++] );
			zdim = Integer.parseInt( words[c.offset++] );
			tdim = Integer.parseInt( words[c.offset++] );
			bdim = Integer.parseInt( words[c.offset++] );
			IntegerImage matrix = new IntegerImage( xdim,ydim,zdim,tdim,bdim );
			data.setValues( matrix );

			for ( Pixel p : matrix ) 
				data.setValue( p, Integer.parseInt( words[c.offset++] ) );

			c.offset++; // pass </DATA>
		}
		return data;
	}

}
