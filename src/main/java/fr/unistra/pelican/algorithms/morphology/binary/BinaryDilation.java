package fr.unistra.pelican.algorithms.morphology.binary;

import fr.unistra.pelican.*;
import fr.unistra.pelican.util.Point4D;
import fr.unistra.pelican.util.buffers.BooleanBuffers;


/**
 *	Performs a binary dilation with a flat structuring element.
 *	@author ?, Jonathan Weber, Régis Witz (mask management + optimization)
 */
public class BinaryDilation extends Algorithm {

	  ///////////////
	 // CONSTANTS //
	///////////////

	/**	Constant for ignoring out-of-image pixels. */
	public static final int IGNORE = 0;
	/**	Constant for setting to white out-of-image pixels. */
	public static final int WHITE = 1;
	/**	Constant for setting to black out-of-image pixels. */
	public static final int BLACK = 2;

	  ////////////
	 // FIELDS //
	////////////

	/**	Image to be processed. */
	public Image inputImage;

	/**	Structuring Element used for the dilation. */
	public BooleanImage se;

	/**	Resulting picture. */
	public Image outputImage;

	/**	Option for considering out-of-image pixels. */
	public Integer option = IGNORE;

	/**	Indicates if structuring element is convex or not. */
	public Boolean convexSEFlag = false;

	public int optimization = 
		fr.unistra.pelican.algorithms.morphology.gray.GrayErosion.NO_OPTIMIZATION;



	  /////////////
	 // PROFILE //
	/////////////

	/**	Algorithm profile. */
	public BinaryDilation() {

		super();
		super.inputs = "inputImage,se";
		super.options= "option,convexSEFlag,optimization";
		super.outputs = "outputImage";
		
	}



	  ////////////////////
	 // "EXEC" METHODS //
	////////////////////

	/**	Performs a binary dilation with a flat structuring element.
	 *	@param <T> Type of <tt>image</tt>. 
	 *	@param image Image to be processed
	 *	@param se Structuring element
	 *	@return Dilated picture
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Image> T  exec( T image,BooleanImage se ) {
		return (T) new BinaryDilation().process(image,se);
	}

	/**	Performs a binary dilation with a flat structuring element.
	 *	@param <T> Type of <tt>image</tt>. 
	 *	@param image Image to be processed
	 *	@param se Structuring element
	 *	@param option How to consider out-of-image pixels
	 *	@return Dilated picture
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Image> T  exec( T image,BooleanImage se,Integer option ) { 
		return (T) new BinaryDilation().process(image,se,option);
	}

	/**	Performs a binary dilation with a flat structuring element.
	 *	@param <T> Type of <tt>image</tt>. 
	 *	@param image Image to be processed
	 *	@param se Structuring element
	 *	@param flag If structuring element is convex or not
	 *	@return Dilated picture
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Image> T  exec( T image, BooleanImage se,Boolean flag ) { 
		return (T) new BinaryDilation().process(image,se,null,flag);
	}

	/**	Performs a binary dilation with a flat structuring element.
	 *	@param <T> Type of <tt>image</tt>. 
	 *	@param image Image to be processed.
	 *	@param se Structuring element.
	 *	@param opt Way of optimize things. Should be one of this class XXX_OPTIMIZATION constants.
	 *	@return Dilated picture.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Image> T  exec( T image, BooleanImage se, int opt ) { 
		return (T) new BinaryDilation().process( image,se,null,null,opt );
	}

	/**	Performs a binary dilation with a flat structuring element.
	 *	@param <T> Type of <tt>image</tt>. 
	 *	@param image Image to be processed.
	 *	@param se Structuring element.
	 *	@param option How to consider out-of-image pixels.
	 *	@param flag If structuring element is convex or not.
	 *	@param opt Way of optimize things. Should be one of this class XXX_OPTIMIZATION constants.
	 *	@return Dilated picture.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Image> T  exec( T image, 
											 BooleanImage se, 
											 int option, 
											 Boolean flag, 
											 int opt ) { 
		return (T) new BinaryDilation().process( image,se,new Integer(option),flag,opt );
	}





	  /////////////////////
	 // "LAUNCH" METHOD //
	/////////////////////

	/** @see fr.unistra.pelican.Algorithm#launch() */
	public void launch() throws AlgorithmException { 

		switch ( this.optimization ) { 

			case fr.unistra.pelican.algorithms.morphology.gray.GrayErosion.RECTANGLE_OPTIMIZATION: 
				this.rectangleDilation();
				break;
			case fr.unistra.pelican.algorithms.morphology.gray.GrayErosion.HLINE_OPTIMIZATION: 
				this.standardDilation();
				break;
			case fr.unistra.pelican.algorithms.morphology.gray.GrayErosion.VLINE_OPTIMIZATION: 
				this.standardDilation();
				break;
			case fr.unistra.pelican.algorithms.morphology.gray.GrayErosion.VANHERK_HLINE_OPTIMIZATION: 
				this.horizontalDilation();
				break;
			case fr.unistra.pelican.algorithms.morphology.gray.GrayErosion.VANHERK_VLINE_OPTIMIZATION: 
				this.verticalDilation();
				break;
			default : // try to find a possible optimization ...
				int opt = fr.unistra.pelican.algorithms.morphology.gray.GrayErosion
							.wichOptimization( this.se, this.inputImage );
				if ( opt == fr.unistra.pelican.algorithms.morphology.gray.GrayErosion.NO_OPTIMIZATION ) 
					this.standardDilation();
				else this.outputImage = 
					BinaryDilation.exec( this.inputImage,this.se,option,convexSEFlag,opt );
		}
	}
	  ///////////////////
	 // OTHER METHODS //
	///////////////////

	private void standardDilation() { 

		int xDim = this.inputImage.getXDim();
		int yDim = this.inputImage.getYDim();
		int tDim = this.inputImage.getTDim();
		int bDim = this.inputImage.getBDim();
		int zDim = this.inputImage.getZDim();

		this.outputImage = this.inputImage.copyImage(false);
		Point4D[] points = this.se.foreground();

		boolean isHere;
		
		for ( int t = 0 ; t < tDim ; t++ )
		for ( int z = 0 ; z < zDim ; z++ )
		for ( int y = 0 ; y < yDim ; y++ ) 
		for ( int x = 0 ; x < xDim ; x++ )
		for ( int b = 0 ; b < bDim ; b++ )
		{ 

			isHere = this.inputImage.isPresent( x,y,z,t,b );
			if  ( !isHere && this.option == IGNORE ) continue; 

			
//			// FIXME sans doute encore un bug !!!!
//			if (	this.convexSEFlag == true
//				 && (  ( !isHere && this.option == BLACK )
//					|| ( isHere && !this.inputImage.getPixelBoolean( x,y,z,t,b ) ) ) 
//			   ) continue;
//			this.outputImage.setPixelBoolean( x,y,z,t,b, getMax( x,y,z,t,b, points ) );

			if ( isHere ) 
				 this.outputImage.setPixelBoolean( x,y,z,t,b, this.getMax( x,y,z,t,b, points ) );
			else this.outputImage.setPixelBoolean( x,y,z,t,b, false );
		}
	}

	/**	Gets the max value for the pixels under the structuring element when the
	 *	last is at the given coordinates.
	 *	@param x X coordinate.
	 *	@param y Y coordinate.
	 *	@param z Z coordinate.
	 *	@param t T coordinate.
	 *	@param b B coordinate.
	 *	@param points Present points of {@link #se}.
	 *	@return Max value.
	 */
	public boolean getMax(int x, int y, int z, int t, int b, Point4D[] points) {
		boolean flag = false;

		for (int i = 0; i < points.length; i++) {
			int valX = x - se.getCenter().x + points[i].x;
			int valY = y - se.getCenter().y + points[i].y;
			int valZ = z - se.getCenter().z + points[i].z;
			int valT = t - se.getCenter().t + points[i].t;
			if (valX >= 0 && valX < inputImage.getXDim() && valY >= 0
				&& valY < inputImage.getYDim() && valZ >= 0
				&& valZ < inputImage.getZDim() && valT >= 0
				&& valT < inputImage.getTDim()) {
				flag = true;

				boolean p = inputImage.getPixelBoolean( valX,valY,valZ,valT,b );
				if ( !inputImage.isPresent( valX,valY,valZ,valT,b ) ) { 

					if ( this.option == WHITE ) return true;
				} else if ( p ) return true;

			} else {
				if (option == WHITE) {
					return true;
				}

			}
		}
		// FIXME: Strange, if nothing is under the se, what is the right way?
		return (flag == true) ? false : inputImage.getPixelBoolean(x, y, z, t,
				b);
	}


	 //
	// RECTANGLE DILATION METHOD

	/**	Performs a faster dilation with a square structuring element. */
	private void rectangleDilation() {

		this.outputImage = inputImage.copyImage( false );
		int xdim = this.se.getXDim();
		int ydim = this.se.getYDim();
		BooleanImage optSe = fr.unistra.pelican.util.morphology.FlatStructuringElement2D.
								createHorizontalLineFlatStructuringElement( 
									xdim, new java.awt.Point( this.se.getCenter().x, 0 ) );
		this.outputImage = BinaryDilation.exec( this.inputImage,optSe );
		optSe = fr.unistra.pelican.util.morphology.FlatStructuringElement2D.
								createVerticalLineFlatStructuringElement( 
									ydim, new java.awt.Point( 0, this.se.getCenter().y ) );
		this.outputImage = BinaryDilation.exec( this.outputImage,optSe );
	} // endfunc



	 //
	// LINES EROSION MATERIAL

	/**	Initializes buffers corresponding to line <tt>y</tt>.
	 *	@param x Column of {@link inputImage} that <tt>buffers</tt> must represent at call's end.
	 *	@param z Z coordinate.
	 *	@param t T coordinate.
	 *	@param b B coordinate.
	 *	@param buffers Up and down buffers.
	 *	@param len Length of horizontal line structuring element {@link #se}.
	 */
	private void initColumnBuffers( int x, int z, int t, int b, BooleanBuffers buffers, int len ) {

		boolean px;
		boolean isHere;
		for ( int y = 0 ; y < buffers.size ; y++ ) { // fill g

			isHere = this.inputImage.isPresent( x,y,z,t,b );
			if ( isHere ) px = this.inputImage.getPixelXYZTBBoolean( x,y,z,t,b );
			else px = false;
			if ( y%len == 0 ) buffers.g[y] = px;
			else buffers.g[y] = buffers.g[y-1] || px;
		} // rof x

		for ( int y = buffers.size-1 ; y >= 0 ; y-- ) { // fill h

			isHere = this.inputImage.isPresent( x,y,z,t,b );
			if ( isHere ) px = this.inputImage.getPixelXYZTBBoolean( x,y,z,t,b );
			else px = false;
			if ( y%len == len-1 ) buffers.h[y] = px;
			else { 

				if ( y+1 < buffers.size ) buffers.h[y] = buffers.h[y+1] || px;
				else buffers.h[y] = px;
			}
		} // rof x

	} // endfunc

	/**	Initializes buffers corresponding to line <tt>y</tt>.
	 *	@param y Line of {@link inputImage} that <tt>buffers</tt> must represent at call's end.
	 *	@param z Z coordinate.
	 *	@param t T coordinate.
	 *	@param b B coordinate.
	 *	@param buffers Left and right buffers.
	 *	@param len Length of horizontal line structuring element {@link #se}.
	 */
	private void initRowBuffers( int y, int z, int t, int b, BooleanBuffers buffers, int len ) {

		boolean px;
		boolean isHere;
		for ( int x = 0 ; x < buffers.size ; x++ ) { // fill g

			isHere = this.inputImage.isPresent( x,y,z,t,b );
			if ( isHere ) px = this.inputImage.getPixelXYZTBBoolean( x,y,z,t,b );
			else px = false;
			if ( x%len == 0 ) buffers.g[x] = px;
			else buffers.g[x] = buffers.g[x-1] || px ;
		} // rof x

		for ( int x = buffers.size-1 ; x >= 0 ; x-- ) { // fill h

			isHere = this.inputImage.isPresent( x,y,z,t,b );
			if ( isHere ) px = this.inputImage.getPixelXYZTBBoolean( x,y,z,t,b );
			else px = false;
			if ( x%len == len-1 ) buffers.h[x] = px;
			else { 

				if ( x+1 < buffers.size ) buffers.h[x] = buffers.h[x+1] || px;
				else buffers.h[x] = px;
			}
		} // rof x

	} // endfunc

	/**	Performs a faster dilation with a horizontal structuring element. 
	 *	<p>
	 *	M. van Herk, <i>A fast algorithm for local minimum and maximum filters on rectangular and 
	 *	octogonal kernels</i> (1992).
	 *	Algorithm leeched from P. Soille, <i>Morphological Image Analysis</i> (3.9.1).
	 */
	private void horizontalDilation() { 

		this.outputImage = this.inputImage.copyImage( false );
		int xdim = this.inputImage.getXDim();
		int ydim = this.inputImage.getYDim();
		int zdim = this.inputImage.getZDim();
		int tdim = this.inputImage.getTDim();
		int bdim = this.inputImage.getBDim();
		int size = xdim;
		BooleanBuffers buffers = new BooleanBuffers( size );
		int lambda = this.se.getXDim(); //  lambada !
		int o = this.se.getCenter().x;

		assert buffers.size%lambda == 0;

		boolean px;
		int m,n;
		for ( int b = 0 ; b < bdim ; b++ ) 
		for ( int t = 0 ; t < tdim ; t++ )
		for ( int z = 0 ; z < zdim ; z++ )
		for ( int y = 0 ; y < ydim ; y++ ) { 

			this.initRowBuffers( y,z,t,b, buffers,lambda );
			for ( int x = 0 ; x < xdim ; x++ ) { 

				m = x+lambda-o-1;
				n = x-o;
				if ( m >= xdim ) { 

					if ( n < 0 ) px = false;
					else px = buffers.h[ n ];

				} else {

					if ( n < 0 ) px = buffers.g[ m ];
					else px = buffers.g[ m ] || buffers.h[ n ];
				}
				this.outputImage.setPixelBoolean( x,y,z,t,b, px );
			} // rof x

		} // rof

	} // endfunc

	/**	Performs a faster dilation (van Herk,1992) with a vertical structuring element. 
	 *	<p>
	 *	M. van Herk, <i>A fast algorithm for local minimum and maximum filters on rectangular and 
	 *	octogonal kernels</i> (1992).
	 *	Algorithm leeched from P. Soille, <i>Morphological Image Analysis</i> (3.9.1).
	 */
	private void verticalDilation() { 

		this.outputImage = this.inputImage.copyImage( false );
		int xdim = this.inputImage.getXDim();
		int ydim = this.inputImage.getYDim();
		int zdim = this.inputImage.getZDim();
		int tdim = this.inputImage.getTDim();
		int bdim = this.inputImage.getBDim();
		int size = ydim;
		BooleanBuffers buffers = new BooleanBuffers( size );
		int lambda = this.se.getYDim(); //  lambada !
		int o = this.se.getCenter().y;

		assert buffers.size%lambda == 0;

		boolean px;
		int m,n;
		for ( int b = 0 ; b < bdim ; b++ ) 
		for ( int t = 0 ; t < tdim ; t++ )
		for ( int z = 0 ; z < zdim ; z++ )
		for ( int x = 0 ; x < xdim ; x++ ) { 

			this.initColumnBuffers( x,z,t,b, buffers,lambda );
			for ( int y = 0 ; y < ydim ; y++ ) { 

				m = y+lambda-o-1;
				n = y-o;
				if ( m >= ydim ) { 

					if ( n < 0 ) px = false;
					else px = buffers.h[ n ];

				} else {

					if ( n < 0 ) px = buffers.g[ m ];
					else px = buffers.g[ m ] || buffers.h[ n ];
				}
				this.outputImage.setPixelBoolean( x,y,z,t,b, px );
			} // rof x

		} // rof

	} // endfunc





	  //////////
	 // MAIN //
	//////////

//	public static void main( String[] args ) {
//
//		Image image = fr.unistra.pelican.algorithms.io.ImageLoader.exec( "samples/binary.png" );
//		image = new BooleanImage( image );
//		BooleanImage se = fr.unistra.pelican.util.morphology.FlatStructuringElement2D
//							.createSquareFlatStructuringElement(5);
//		Image result = BinaryDilation.exec( image,se );
//		fr.unistra.pelican.algorithms.io.ImageSave.exec( result, 
//				"src/test/resources/binarydilation-square5x5-truth.png" );
//	}


	
}
