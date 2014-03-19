package fr.unistra.pelican.util.mask;

import fr.unistra.pelican.BooleanImage;

/**
 *	Represents a boolean mask. 
 *	Note that this version is aimed at being used with an image whose binary representation is put 
 *	in the { @link#image } field.
 *	TODO : always return <code>!option</code> if the <code>isInMask</code> methodes are called for 
 *	a pixel which is out of { @link#image } bounds.
 *
 *	@see Mask
 *	@see fr.unistra.pelican.BooleanImage
 *
 *	@author witz
 *	@version 1.0
 *
 *	@deprecated use BooleanImage instead, this wrapper is useless B. Perret
 */
public class BooleanMask implements Mask {



	  ///////////////
	 // CONSTANTS //
	///////////////

	/**	Denote that <tt>true</tt>-valued pixels in {@link #image} are considered 
	 *	to be "present" ( from an image associated with this mask ).
	 */
	public static final boolean PRESENT = false;

	/**	Denote that <tt>true</tt>-valued pixels in {@link #image} are considered 
	 *	to be "absent" ( from an image associated with this mask ).
	 */
	public static final boolean NOT_PRESENT = true;



	  ////////////
	 // FIELDS //
	////////////

	/**	The boolean image that serves as the mask.
	 */
	private BooleanImage image;

	/**	At <tt>true</tt>, denote that <tt>true</tt>-valued pixels of {@link #image} are present.
	 *	At <tt>false</tt>, denote that <tt>false</tt>-valued pixels of {@link #image} are present.
	 */
	public boolean option = PRESENT;



	  //////////////////
	 // CONSTRUCTORS //
	//////////////////

	/**	Initializes a newly created <code>BooleanMask</code> object so that it represents 
	 *	a mask that can be put on an other image.
	 *	By default, <tt>true</tt>-valued pixels are considered not to be hidden by the mask, and 
	 *	thus "present" in the associated image, whereas <tt>false</tt>-valued pixels will be 
	 *	treated as "absent".
	 *	@param image	The boolean image that serves as the mask. By de
	 */
	public BooleanMask( BooleanImage image ) { 

		this.image = image.copyImage( true );
	}

	/**	Initializes a newly created <code>BooleanMask</code> object so that it represents 
	 *	a mask that can be put on an other image.
	 *	By default, <tt>true</tt>-valued pixels are considered not to be hidden by the mask, and 
	 *	thus "present" in the associated image, whereas <tt>false</tt>-valued pixels will be 
	 *	treated as "absent".
	 * 
	 * @param x X size of the mask.
	 * @param y Y size of the mask.
	 * @param z Z size of the mask.
	 * @param t T size of the mask.
	 * @param b B size of the mask.
	 * @param present Specify if pixel are present or absent by default at mask creation.
	 */
	public BooleanMask( int x, int y, int z, int t, int b, boolean present ) { 

		this.image = new BooleanImage( x,y,z,t,b );
		for ( int p = 0 ; p < this.image.size(); p++ ) this.image.setPixelBoolean( p, present );
	}

	/**	Initializes a newly created <code>BooleanMask</code> object so that it represents 
	 *	a mask that can be put on an other image.
	 *	The interpretation of "present" and "absent" pixels. See {@link #option}.
	 *	@param image	The boolean image that serves as the mask.
	 */
	public BooleanMask( BooleanImage image, boolean option ) { 

		this( image );
		this.option = option;
	}



	  /////////////
	 // METHODS //
	/////////////

	/**	Clone this instance of <tt>BooleanMask</tt>.
	 *	@return a copy of this mask.
	 */
	public Mask cloneMask() { 

		return new BooleanMask( new BooleanImage( this.image ) );
	}

	/**	Tests if a pixel is present at the given location, 
	 *	according to the value of {@link #option}.
	 *	@param loc index of the pixel considered.
	 *	@return presence.
	 */
	public boolean isInMask( int loc ) { 

		//	it's a XOR operation...
		return this.option ^ this.image.getPixelBoolean( loc );
	}
	
	/**	Tests if a pixel is present at the given location, 
	 *	according to the value of {@link #option}.
	 *	@param loc index of the pixel considered.
	 *	@return presence.
	 */
	public boolean isInMask( long loc ) { 

		//	it's a XOR operation...
		return this.option ^ this.image.getPixelBoolean( loc );
	}
	

	/**	Tests if a pixel is present at the given location, 
	 *	according to the value of {@link #option}.
	 *	@param x	horizontal position of the desired pixel.
	 *	@param y	vertical position of the desired pixel.
	 *	@param z	depth position of the desired pixel.
	 *	@param t	time position of the desired pixel.
	 *	@param b	channel number of the desired pixel.
	 *	@return presence.
	 */
	public boolean isInMask( int x, int y, int z, int t, int b ) { 

		return this.isInMaskXYZTB( x,y,z,t,b );
	}

	/**	Tests if a pixel is present at the given location, 
	 *	according to the value of {@link #option}.
	 *	@param x	horizontal position of the desired pixel.
	 *	@param y	vertical position of the desired pixel.
	 *	@return presence.
	 */
	public boolean isInMaskXY( int x, int y ) { 

		return this.option ^ this.image.getPixelXYBoolean( x,y );
	}

	/**	Tests if a pixel is present at the given location, 
	 *	according to the value of {@link #option}.
	 *	@param x	horizontal position of the desired pixel.
	 *	@param y	vertical position of the desired pixel.
	 *	@param b	channel number of the desired pixel.
	 *	@return presence.
	 */
	public boolean isInMaskXYB( int x, int y, int b ) { 

		return this.option ^ this.image.getPixelXYBBoolean( x,y,b );
	}

	/**	Tests if a pixel is present at the given location, 
	 *	according to the value of {@link #option}.
	 *	@param x	horizontal position of the desired pixel.
	 *	@param y	vertical position of the desired pixel.
	 *	@param t	time position of the desired pixel.
	 *	@return presence.
	 */
	public boolean isInMaskXYT( int x, int y, int t ) { 

		return this.option ^ this.image.getPixelXYTBoolean( x,y,t );
	}

	/**	Tests if a pixel is present at the given location, 
	 *	according to the value of {@link #option}.
	 *	@param x	horizontal position of the desired pixel.
	 *	@param y	vertical position of the desired pixel.
	 *	@param t	time position of the desired pixel.
	 *	@param b	channel number of the desired pixel.
	 *	@return presence.
	 */
	public boolean isInMaskXYTB( int x, int y, int t, int b ) { 

		return this.option ^ this.image.getPixelXYTBBoolean( x,y,t,b );
	}

	/**	Tests if a pixel is present at the given location, 
	 *	according to the value of {@link #option}.
	 *	@param x	horizontal position of the desired pixel.
	 *	@param y	vertical position of the desired pixel.
	 *	@param z	depth position of the desired pixel.
	 *	@return presence.
	 */
	public boolean isInMaskXYZ( int x, int y, int z ) { 

		return this.option ^ this.image.getPixelXYZBoolean( x,y,z );
	}

	/**	Tests if a pixel is present at the given location, 
	 *	according to the value of {@link #option}.
	 *	@param x	horizontal position of the desired pixel.
	 *	@param y	vertical position of the desired pixel.
	 *	@param z	depth position of the desired pixel.
	 *	@param b	channel number of the desired pixel.
	 *	@return presence.
	 */
	public boolean isInMaskXYZB( int x, int y, int z, int b ) { 

		return this.option ^ this.image.getPixelXYZBBoolean( x,y,z,b );
	}

	/**	Tests if a pixel is present at the given location, 
	 *	according to the value of {@link #option}.
	 *	@param x	horizontal position of the desired pixel.
	 *	@param y	vertical position of the desired pixel.
	 *	@param z	depth position of the desired pixel.
	 *	@param t	time position of the desired pixel.
	 *	@return presence.
	 */
	public boolean isInMaskXYZT( int x, int y, int z, int t ) { 

		return this.option ^ this.image.getPixelXYZTBoolean( x,y,z,t );
	}

	/**	Tests if a pixel is present at the given location, 
	 *	according to the value of {@link #option}.
	 *	@param x	horizontal position of the desired pixel.
	 *	@param y	vertical position of the desired pixel.
	 *	@param z	depth position of the desired pixel.
	 *	@param t	time position of the desired pixel.
	 *	@param b	channel number of the desired pixel.
	 *	@return presence.
	 */
	public boolean isInMaskXYZTB( int x, int y, int z, int t, int b ) { 

		return this.option ^ this.image.getPixelXYZTBBoolean( x,y,z,t,b );
	} 

	public int getXDim() { return this.image.getXDim(); }
	public int getYDim() { return this.image.getYDim(); }
	public int getZDim() { return this.image.getZDim(); }
	public int getTDim() { return this.image.getTDim(); }
	public int getBDim() { return this.image.getBDim(); }


}
