package fr.unistra.pelican.algorithms.morphology.gray;

import fr.unistra.pelican.*;
import fr.unistra.pelican.util.Point4D;
import fr.unistra.pelican.util.buffers.DoubleBuffers;



/**
 *	Performs a gray dilation with a 2-D flat structuring element.
 *	<p>
 *	TODO: Optimization for standard dilation, i will do the rest further - Jonathan
 * 
 *	@author PELICAN team.
 */
public class GrayDilation extends Algorithm {



	  ////////////
	 // FIELDS //
	////////////

	/** Input image. */
	public Image inputImage;

	/** Flat structuring element used in the morphological operation. */
	public BooleanImage se;
	
	/** Mask to limit the computing to a specified area. */
	public BooleanImage mask=null;

	/** Output image. */
	public Image outputImage;

	/**	If different from {@link GrayErosion#NO_OPTIMIZATION}, things will go faster 
	 *	if given an appropriate structuring element. 
	 */
	public int optimization = GrayErosion.NO_OPTIMIZATION;



	  ///////////////////////
	 // ALGORITHM PROFILE //
	///////////////////////

	public GrayDilation() { 

		super.inputs = "inputImage,se";
		super.options = "mask,optimization";
		super.outputs = "outputImage";
	}



	  ////////////////////
	 // "EXEC" METHODS //
	////////////////////

	/**	Performs a gray dilation with a 2-D flat structuring element.
	 *	@param inputImage Input image
	 *	@param se Flat structuring element used in the morphological operation
	 *	@return Output image
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Image> T exec(T inputImage, BooleanImage se) {
		return (T) new GrayDilation().process(inputImage, se);
	}
	
	/**	Performs a gray dilation with a 2-D flat structuring element and a mask.
	 *	@param inputImage Input image
	 *	@param se Flat structuring element used in the morphological operation
	 *	@param mask Mask used to only compute a part of the image          
	 *	@return Output image
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Image> T exec(T inputImage, BooleanImage se, BooleanImage mask) {
		return (T) new GrayDilation().process(inputImage, se, mask);
	}

	/**	Performs a specific gray dilation with a 2-D flat structuring element and a mask.
	 *	@param inputImage Input image.
	 *	@param se Flat structuring element used in the morphological operation.
	 *	@param mask Mask used to only compute a part of the image.
	 *	@param o Way of optimize things. Should be one of GrayErosion.XXX_OPTIMIZATION constants.
	 *	@return Output image.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Image> T exec(T inputImage, BooleanImage se, BooleanImage mask, int o ) { 
		return ( T ) new GrayDilation().process(inputImage, se, mask, o );
	}



	  /////////////////////
	 // "LAUNCH" METHOD //
	/////////////////////
	
	/** @see fr.unistra.pelican.Algorithm#launch() */
	public void launch() { 

		switch ( this.optimization ) { 

			case GrayErosion.RECTANGLE_OPTIMIZATION: 
				this.rectangleDilation();
				break;
			case GrayErosion.HLINE_OPTIMIZATION: 
				this.standardDilation();
				break;
			case GrayErosion.VLINE_OPTIMIZATION: 
				this.standardDilation();
				break;
			case GrayErosion.VANHERK_HLINE_OPTIMIZATION: 
				this.horizontalDilation();
				break;
			case GrayErosion.VANHERK_VLINE_OPTIMIZATION: 
				this.verticalDilation();
				break;
			default : // try to find a possible optimization ...
				int opt = GrayErosion.wichOptimization( this.se, this.inputImage );
				if ( opt == GrayErosion.NO_OPTIMIZATION ) 
					this.standardDilation();
				else this.outputImage = GrayDilation.exec( this.inputImage,this.se,this.mask,opt );
		}
	}

	  ///////////////////
	 // OTHER METHODS //
	///////////////////

	/**	Returns the max value under a flat structuring element.
	 *	@param x X coordinate.
	 *	@param y Y coordinate.
	 *	@param z Z coordinate.
	 *	@param t T coordinate.
	 *	@param b B coordinate.
	 *	@param points Present points of {@link #se}.
	 */
	private double getMaxGray(int x, int y, int z, int t, int b, Point4D[] points) {

		double max = Double.MIN_VALUE;
		boolean flag = false;

		for ( int i = 0 ; i < points.length ; i++ ) { 

			int valX = x - this.se.getCenter().x + points[i].x;
			int valY = y - this.se.getCenter().y + points[i].y;
			int valZ = z - this.se.getCenter().z + points[i].z;
			int valT = t - this.se.getCenter().t + points[i].t;
			if (	valX >= 0 && valX < this.inputImage.getXDim() 
				 && valY >= 0 && valY < this.inputImage.getYDim() 
				 && valZ >= 0 && valZ < this.inputImage.getZDim() 
				 && valT >= 0 && valT < this.inputImage.getTDim() 
				 && this.inputImage.isPresent( valX,valY,valZ,valT,b ) ) { 

				double value = this.inputImage.getPixelDouble( valX, valY, valZ, valT, b );
				if ( max < value ) max = value;
				flag = true;
			} // fi
		} // rof i
		// FIXME: Strange, if nothing is under the se, what is the right way?
		return (flag == true) ? max : this.inputImage.getPixelDouble( x,y,z,t,b );
	} // endfunc



	 //
	// STANDARD DILATION METHOD

	/**	Performs a standard "naive" dilation. */
	private void standardDilation() { 

		this.outputImage = inputImage.copyImage( false );
		int xDim = this.inputImage.getXDim();
		int yDim = this.inputImage.getYDim();
		int tDim = this.inputImage.getTDim();
		int bDim = this.inputImage.getBDim();
		int zDim = this.inputImage.getZDim();
		Point4D[] points = se.foreground();
		boolean isHere;
		for ( int t = 0 ; t < tDim ; t++ )
		for ( int z = 0 ; z < zDim ; z++ )
		for ( int y = 0 ; y < yDim ; y++ ) 
		for ( int x = 0 ; x < xDim ; x++ )
		for ( int b = 0 ; b < bDim ; b++ ) { 
			if(mask!=null)
				isHere = this.inputImage.isPresent( x,y,z,t,b )&&mask.getPixelXYZTBBoolean(x, y, z, t, b);
			else
				isHere = this.inputImage.isPresent( x,y,z,t,b );
			if ( isHere ) 
				 this.outputImage.setPixelDouble( x,y,z,t,b, this.getMaxGray( x,y,z,t,b, points ) );
			else this.outputImage.setPixelDouble( x,y,z,t,b, 0. );
		} // rof
	} // endfunc


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
		this.outputImage = GrayDilation.exec( this.inputImage,optSe,null );
		optSe = fr.unistra.pelican.util.morphology.FlatStructuringElement2D.
								createVerticalLineFlatStructuringElement( 
									ydim, new java.awt.Point( 0, this.se.getCenter().y ) );
		this.outputImage = GrayDilation.exec( this.outputImage,optSe );
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
	private void initColumnBuffers( int x, int z, int t, int b, DoubleBuffers buffers, int len ) {

		double px;
		boolean isHere;
		for ( int y = 0 ; y < buffers.size ; y++ ) { // fill g

			isHere = this.inputImage.isPresent( x,y,z,t,b );
			if ( isHere ) px = this.inputImage.getPixelXYZTBDouble( x,y,z,t,b );
			else px = Double.MIN_VALUE;
			if ( y%len == 0 ) buffers.g[y] = px;
			else buffers.g[y] = Math.max( buffers.g[y-1],px );
		} // rof x

		for ( int y = buffers.size-1 ; y >= 0 ; y-- ) { // fill h

			isHere = this.inputImage.isPresent( x,y,z,t,b );
			if ( isHere ) px = this.inputImage.getPixelXYZTBDouble( x,y,z,t,b );
			else px = Double.MIN_VALUE;
			if ( y%len == len-1 ) buffers.h[y] = px;
			else { 

				if ( y+1 < buffers.size ) buffers.h[y] = Math.max( buffers.h[y+1],px );
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
	private void initRowBuffers( int y, int z, int t, int b, DoubleBuffers buffers, int len ) {

		double px;
		boolean isHere;
		for ( int x = 0 ; x < buffers.size ; x++ ) { // fill g

			isHere = this.inputImage.isPresent( x,y,z,t,b );
			if ( isHere ) px = this.inputImage.getPixelXYZTBDouble( x,y,z,t,b );
			else px = Double.MIN_VALUE;
			if ( x%len == 0 ) buffers.g[x] = px;
			else buffers.g[x] = Math.max( buffers.g[x-1],px );
		} // rof x

		for ( int x = buffers.size-1 ; x >= 0 ; x-- ) { // fill h

			isHere = this.inputImage.isPresent( x,y,z,t,b );
			if ( isHere ) px = this.inputImage.getPixelXYZTBDouble( x,y,z,t,b );
			else px = Double.MIN_VALUE;
			if ( x%len == len-1 ) buffers.h[x] = px;
			else { 

				if ( x+1 < buffers.size ) buffers.h[x] = Math.max( buffers.h[x+1],px );
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
		DoubleBuffers buffers = new DoubleBuffers( size );
		int lambda = this.se.getXDim(); //  lambada !
		int o = this.se.getCenter().x;

		assert buffers.size%lambda == 0;

		double px;
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

					if ( n < 0 ) px = Double.MIN_VALUE;
					else px = buffers.h[ n ];

				} else {

					if ( n < 0 ) px = buffers.g[ m ];
					else px = Math.max( buffers.g[ m ],buffers.h[ n ] );
				}
				this.outputImage.setPixelDouble( x,y,z,t,b, px );
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
		DoubleBuffers buffers = new DoubleBuffers( size );
		int lambda = this.se.getYDim(); //  lambada !
		int o = this.se.getCenter().y;

		assert buffers.size%lambda == 0;

		double px;
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

					if ( n < 0 ) px = Double.MIN_VALUE;
					else px = buffers.h[ n ];

				} else {

					if ( n < 0 ) px = buffers.g[ m ];
					else px = Math.max( buffers.g[ m ],buffers.h[ n ] );
				}
				this.outputImage.setPixelDouble( x,y,z,t,b, px );
			} // rof x

		} // rof

	} // endfunc



}
