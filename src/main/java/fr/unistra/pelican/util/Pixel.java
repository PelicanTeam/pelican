package fr.unistra.pelican.util;

import java.awt.Point;
import java.io.Serializable;

/**
 *	Represents 5-D points in an image.
 *	@author Régis Witz
 */
public class Pixel implements Cloneable, Comparable<Pixel>, Serializable {



	  ///////////////
	 // CONSTANTS //
	///////////////

	private static final long serialVersionUID = 1L;



	  ////////////
	 // FIELDS //
	////////////

	public int x;
	public int y;
	public int z;
	public int t;
	public int b;



	  //////////////////
	 // CONSTRUCTORS //
	//////////////////

	public Pixel() { 

		this.x = 0;
		this.y = 0;
		this.z = 0;
		this.t = 0;
		this.b = 0;
	}

	public Pixel( int x, int y, int z, int t, int b ) { 

		this.x = x;
		this.y = y;
		this.z = z;
		this.t = t;
		this.b = b;
	}

	public Pixel( int x,int y ) { 

		this.x = x;
		this.y = y;
		this.z = 0;
		this.t = 0;
		this.b = 0;
	}

	public Pixel( Point4D p ) { 

		this( p.x, p.y, p.z, p.t, 0 );
	}


	public Pixel( Pixel p ) { 

		this( p.x, p.y, p.z, p.t, p.b );
	}

	public Pixel( Point p ) { 

		this( p.x, p.y, 0,0,0 );
	}



	  /////////////
	 // METHODS //
	/////////////

	/**	Conversion to {@link Point}. 
	 *	@return Same point, but as a  {@link Point}.
	 */
	public Point getPoint2D() { 

		return new Point( this.x,this.y );
	}

	/**	Coordinates equality test.
	 *	@param obj Instance of {@link Pixel} to compare <tt>this</tt> with.
	 *	@return <tt>true</tt> if coordinates are the same.
	 */
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Pixel))
			return false;
		Pixel p = (Pixel) obj;

		return p.x == x && p.y == y && p.z == z && p.t == t && p.b == b;
	}

	/**
	 * Just another {@link #clone} method.
	 * 
	 * @return Same point, different object.
	 */
	public Pixel getLocation() { 

		return new Pixel( this );
	}

	/**	Set coordinates.
	 *	@param x X coordinate.
	 *	@param y Y coordinate.
	 *	@param z Z coordinate.
	 *	@param t T coordinate.
	 *	@param b B coordinate.
	 */
	public void setLocation( int x, int y, int z, int t, int b ) { 

		this.x = x;
		this.y = y;
		this.z = z;
		this.t = t;
		this.b = b;
	}

	/**
	 * Set coordinates 
	 * @param p new coordinates
	 */
	public void setLocation( Pixel p ) { 

		this.x = p.x;
		this.y = p.y;
		this.z = p.z;
		this.t = p.t;
		this.b = p.b;
	}
	
	/**	Set coordinates. Even arguments are double, they are truncated to integers.
	 *	@param x X coordinate.
	 *	@param y Y coordinate.
	 *	@param z Z coordinate.
	 *	@param t T coordinate.
	 *	@param b B coordinate.
	 */
	public void setLocation( double x, double y, double z, double t, double b ) { 

		this.x = ( int ) x;
		this.y = ( int ) y;
		this.z = ( int ) z;
		this.t = ( int ) t;
		this.b = ( int ) b;
	}

	/**	Get X coordinate. 
	 *	@return X coordinate.
	 */
	public double getX() { return this.x; }

	/**	Get Y coordinate. 
	 *	@return Y coordinate.
	 */
	public double getY() { return this.y; }

	/**	Get Z coordinate. 
	 *	@return Z coordinate.
	 */
	public double getZ() { return this.z; }

	/**	Get T coordinate. 
	 *	@return T coordinate.
	 */
	public double getT() { return this.t; }

	/**	Get B coordinate. 
	 *	@return B coordinate.
	 */
	public double getB() { return this.b; }

	/**	Just another {@link #setLocation} method.
	 *	@param x X coordinate.
	 *	@param y Y coordinate.
	 *	@param z Z coordinate.
	 *	@param t T coordinate.
	 *	@param b B coordinate.
	 */
	public void move( int x, int y, int z, int t, int b ) { 

		this.setLocation( x,y,z,t,b );
	}

	public void setLocation( Point4D p ) {
		x = p.x;
		y = p.y;
		z = p.z;
		t = p.t;
	}

	/**	You knew it ! 
	 *	@return This point as a {@link String} representation. 
	 */
	public String toString() {
		return "Pixel ( " + this.x + 
						"," + this.y + 
						"," + this.z + 
						"," + this.t + 
						"," + this.b + " )";
	}

	/**	Translate the point relative to its coordinates.
	 *	@param x X translation.
	 *	@param y Y translation.
	 *	@param z Z translation.
	 *	@param t T translation.
	 *	@param b B translation.
	 */
	public void translate( int x, int y, int z, int t, int b ) { 

		this.x += x;
		this.y += y;
		this.z += z;
		this.t += t;
		this.b += b;
	}

	/**	Euclidean distance between two points, squared.
	 *	@param p Point to compute the squared distance with.
	 *	@return Euclidean distance between this and <yy>p</tt>, squared.
	 */
	public double distanceSq( Pixel p ) {
		double d = 0;
		d += ( p.x - this.x ) * ( p.x - this.x );
		d += ( p.y - this.y ) * ( p.y - this.y );
		d += ( p.z - this.z ) * ( p.z - this.z );
		d += ( p.t - this.t ) * ( p.t - this.t );
		d += ( p.b - this.b ) * ( p.b - this.b );
		return d;
	}

	/**	Euclidean distance between two points.
	 *	@param p Point to compute the distance with.
	 *	@return Euclidean distance between this and <yy>p</tt>.
	 */
	public double distance( Pixel p ) { 
		return Math.sqrt( distanceSq(p) );
	}

	/**	Comparison between two points.
	 *	@param p Point to compare with.
	 *	@return Difference of coordinates. 
	 */
	public int compareTo( Pixel p ) { 

		if ( this.x != p.x ) return this.x - p.x;
		if ( this.y != p.y ) return this.y - p.y;
		if ( this.z != p.z ) return this.z - p.z;
		if ( this.t != p.t ) return this.t - p.t;
		return this.b - p.b;
	}

	/** Useable thanks to from {@link Cloneable}.
	 *	@return Same coordinates, different object. 
	 */
	public Pixel clone() { 

		return new Pixel( this );
	}


}
