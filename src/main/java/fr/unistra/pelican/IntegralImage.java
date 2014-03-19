package fr.unistra.pelican;

import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;



/**
 *	Integral image as described in :
 *
 *	Herbert Bay, Andreas Ess, Tinne Tuytelaars, Luc Van Gool, "SURF: Speeded Up Robust Features", 
 *	Computer Vision and Image Understanding (CVIU), Vol. 110, No. 3, pp. 346--359, 2008
 *	( Get it there : http://www.vision.ee.ethz.ch/~surf/papers.html )
 *
 *	"Given an input image In and a point (x,y) the integral image I is calculated by the sum of  
 *	the values between the point and the origin. Formally this can be defined by the formula : 
 *										i<=x j<=y
 *								I(x,y) =  E    E   IN(x,y)
 *										 i=0  j=0
 *	Using the integral image, the task of calculating the area of an upright rectangular region 
 *	is reduced four operations. If we consider a rectangle bounded by vertices A, B, C and D, 
 *	the sum of pixel intensities is calculated by A+D-(C+B). Since computation time is invariant 
 *	to change in size this approach is particularly useful when large areas are required."
 *
 *	Integral image for : 	- ByteImage and IntegerImage is an IntegerImage
 *							- DoubleImage and BooleanImage is a DoubleImage
 *	
 *	Be careful when dealing with high pixel values, there is risk of max value overflow (depending on image type)
 *
 *	@author Régis Witz, Jonathan Weber
 */
public class IntegralImage extends Image { 

	Image integralImage;

	public IntegralImage( Image input ) { 

		this.xdim = input.getXDim();
		this.ydim = input.getYDim();
		this.zdim = input.getZDim();
		this.tdim = input.getTDim();
		this.bdim = input.getBDim();
		if(input instanceof ByteImage)
		{
			this.integralImage = new IntegerImage( xdim,ydim,zdim,tdim,bdim );
			int sum;
			for ( int t = 0 ; t < this.tdim ; t++ ) 
				for ( int z = 0 ; z < this.tdim ; z++ ) 
					for ( int b = 0 ; b < this.bdim ; b++ ) { 

						for ( int y = 0 ; y < this.ydim ; y++ ) { 
							sum = 0;
							for ( int x = 0 ; x < this.xdim ; x++ ) { 

								sum += input.getPixelXYZTBByte( x,y,0,0,b );
								if ( y == 0 ) this.integralImage.setPixelXYZTBInt( x,y,0,0,b, sum );
								else this.integralImage.setPixelXYZTBInt( x,y,0,0,b, 
										this.integralImage.getPixelXYZTBInt( x,y-1,0,0,b ) + sum );
							}	}
					}
		}
		else if (input instanceof IntegerImage)
		{
			this.integralImage = new IntegerImage( xdim,ydim,zdim,tdim,bdim );
			int sum;
			for ( int t = 0 ; t < this.tdim ; t++ ) 
				for ( int z = 0 ; z < this.tdim ; z++ ) 
					for ( int b = 0 ; b < this.bdim ; b++ ) { 

						for ( int y = 0 ; y < this.ydim ; y++ ) { 
							sum = 0;
							for ( int x = 0 ; x < this.xdim ; x++ ) { 

								sum += input.getPixelXYZTBInt( x,y,0,0,b );
								if ( y == 0 ) this.integralImage.setPixelXYZTBInt( x,y,0,0,b, sum );
								else this.integralImage.setPixelXYZTBInt( x,y,0,0,b, 
										this.integralImage.getPixelXYZTBInt( x,y-1,0,0,b ) + sum );
							}	}
					}
		}
		else
		{
			this.integralImage = new DoubleImage( xdim,ydim,zdim,tdim,bdim );
			double sum;
			for ( int t = 0 ; t < this.tdim ; t++ ) 
				for ( int z = 0 ; z < this.tdim ; z++ ) 
					for ( int b = 0 ; b < this.bdim ; b++ ) { 

						for ( int y = 0 ; y < this.ydim ; y++ ) { 
							sum = 0.0;
							for ( int x = 0 ; x < this.xdim ; x++ ) { 

								sum += input.getPixelXYZTBDouble( x,y,0,0,b );
								if ( y == 0 ) this.integralImage.setPixelXYZTBDouble( x,y,0,0,b, sum );
								else this.integralImage.setPixelXYZTBDouble( x,y,0,0,b, 
										this.integralImage.getPixelXYZTBDouble( x,y-1,0,0,b ) + sum );
							}	}
					}
		}

	}

	public double area( int x, int y, int width, int height ) { 

		double[] sums = this.colorArea( x,y, width,height );
		return sums[0];
	}

	public double[] colorArea( int x, int y, int width, int height ) { 
		return this.colorArea( x,y,0,0, width,height ); 
	}

	public double[] colorArea( int x, int y, int z, int t, int width, int height ) { 


		if ( width <= 0 || height <= 0 ) 
			throw new PelicanException( "Width and height must be strictly positive." );

		double []sums = new double[ this.bdim ];

		int px = Math.max( x,0 );
		int py = Math.max( y,0 );
		int pxw = Math.min( x+width,this.xdim ) -1;
		int pyh = Math.min( y+height,this.ydim ) -1;

		if (	px >= 0  && px < this.xdim 
			 && py >= 0  && py < this.ydim 
			 && pxw >= 0 && pxw < this.xdim 
			 && pyh >= 0 && pyh < this.ydim ) { 
			
			if(integralImage instanceof IntegerImage)
			{
				for ( int i = 0 ; i < this.bdim ; i++ ) { 

					double a = this.getPixelXYZTBInt( px,py,z,t,i );
					double b = this.getPixelXYZTBInt( pxw,py,z,t,i );
					double c = this.getPixelXYZTBInt( px,pyh,z,t,i );
					double d = this.getPixelXYZTBInt( pxw,pyh,z,t,i );

					sums[i] = a+d-(c+b);	// = a-c-b+d ... is it faster ? 
				}
			}
			else
			{

				for ( int i = 0 ; i < this.bdim ; i++ ) { 

					double a = this.getPixelXYZTBDouble( px,py,z,t,i );
					double b = this.getPixelXYZTBDouble( pxw,py,z,t,i );
					double c = this.getPixelXYZTBDouble( px,pyh,z,t,i );
					double d = this.getPixelXYZTBDouble( pxw,pyh,z,t,i );

					sums[i] = a+d-(c+b);	// = a-c-b+d ... is it faster ? 
				}
			}
		} else for ( int i = 0 ; i < this.bdim ; i++ ) sums[i] = 0.0;
		return sums;
	}

	@Override	// TODO Auto-generated method stub -lazy programmer !
	public Image copyImage( boolean arg0 ) { return null; }

	@Override
	public boolean equals( Image arg ) { 

		if ( arg==null  || !(arg instanceof IntegralImage) ) return false;

		IntegralImage img = ( IntegralImage )arg;
		if ( !( this.xdim == img.xdim 
			 && this.ydim == img.ydim 
			 && this.zdim == img.zdim 
			 && this.tdim == img.tdim 
			 && this.bdim == img.bdim ) ) return false;

		int size = this.size();
		for ( int i = 0; i < size; i++ ) 
			if ( this.getPixelDouble(i) != img.getPixelDouble(i) ) return false;

		return true;		
	}

	@Override
	public boolean getPixelBoolean( int p ) { return this.integralImage.getPixelBoolean( p ); }

	@Override
	public int getPixelByte( int p ) { return this.integralImage.getPixelByte( p ); }

	@Override
	public double getPixelDouble( int p ) { return this.integralImage.getPixelDouble( p ); }

	@Override
	public int getPixelInt( int p ) { return this.integralImage.getPixelInt( p ); }

	@Override	// TODO Auto-generated method stub - me is lazy !
	public Image newInstance(int arg0, int arg1, int arg2, int arg3, int arg4) { return null; }

	// the "set" methods do nothing for not wrecking the pixel sums.
	@Override
	public void setPixel(	Image arg0, 
							int arg1, int arg2, int arg3, int arg4, int arg5, 
							int arg6, int arg7, int arg8, int arg9, int arg10 ) {}
	@Override
	/** Not allowed. */
	public void setPixelBoolean( int p, boolean value ) {}
	@Override
	/** Not allowed. */
	public void setPixelByte( int p, int value ) {}
	@Override
	/** Not allowed. */
	public void setPixelDouble( int p, double value ) {}
	@Override
	/** Not allowed. */
	public void setPixelInt( int p, int value ) {}

	@Override
	public int size() { return this.integralImage.size(); }


	public static final long serialVersionUID = 1L;

}
