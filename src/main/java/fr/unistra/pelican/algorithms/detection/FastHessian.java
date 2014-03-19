package fr.unistra.pelican.algorithms.detection;

import java.util.ArrayList;
import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegralImage;
import fr.unistra.pelican.util.Keypoint;
import fr.unistra.pelican.util.Tools;
import fr.unistra.pelican.util.data.DoubleArrayData;



/**
 *	Interest point detector
 *
 *	@author Régis Witz
 */
public class FastHessian extends Algorithm { 



	  ////////////
	 // INPUTS //
	////////////

	/**	Input integral image. */
	public IntegralImage integralImage;

	  /////////////
	 // OUTPUTS //
	/////////////

	/**	Output array of interest points. 
	 *	The keypoints with wich it will be filled will have only their {@link Keypoint#x}, 
	 *	{@link Keypoint#y }, scale and laplacian fields initialized. 
	 */
	public ArrayList<Keypoint> keys = new ArrayList<Keypoint>();

	  /////////////
	 // OPTIONS //
	/////////////

	public int octaves = 3;
	public int initSample = 3;
	public int intervals = 4;
	public double thres = 0.004;
	public int interpSteps = 5;



	  //////////////////
	 // OTHER FIELDS //
	//////////////////

	/**	Convenience field ; equal to the width (xdim) 
	 *	of the input image {@link #integralImage}. 
	 */
	private int width;
	/**	Convenience field ; equal to the height (ydim) 
	 *	of the input image {@link #integralImage}. 
	 */
	private int height;

	/**	An array containing the determinant of hessians (DoH) values 
	 *	for each row an column of {@link #integralImage} and for each 
	 *	interval of each octave.
	 */
	private double[] doh;



	  /////////////////
	 // CONSTRUCTOR //
	/////////////////

	public FastHessian() { 

		super.inputs = "integralImage";
		super.options = "octaves,initSample,intervals,thres,interpSteps";
		super.outputs = "keys";
	}



	  ////////////////////
	 // "EXEC" METHODS //
	////////////////////

	@SuppressWarnings("unchecked")
	public static ArrayList<Keypoint> exec( Image input ) { 
		return ( ArrayList<Keypoint> ) new FastHessian().process( input );
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<Keypoint> exec( Image input, 
											int octaves, 
											int initSample, 
											int intervals, 
											double thres, 
											int interpSteps ) { 

		return ( ArrayList<Keypoint> ) 
			new FastHessian().process( input, octaves, initSample, intervals, thres, interpSteps );
	}



	  /////////////////////
	 // "LAUNCH" METHOD //
	/////////////////////

	public void launch() { 

		this.width = this.integralImage.getXDim();
		this.height = this.integralImage.getYDim();

		// initialize doh
		this.doh = new double [ this.octaves*this.intervals*this.width*this.height ];
		this.computeResponses();

		for( int o = 0 ; o < this.octaves ; o++ ) { 

			// for each octave double the sampling step of the previous
			int step = this.initSample * Tools.cvround( Math.pow( 2,o ) );

			// determine border width for the largest filter for each ocave
			int border = ( 3 * Tools.cvround( Math.pow( 2,o+1 )*this.intervals +1 ) +1 )/2;

			// check for maxima across the scale space
			for ( int i = 1; i < this.intervals-1; ++i ) 
				for ( int r = border; r < height-border; r += step ) 
					for ( int c = border ; c < width-border ; c += step ) 
						if ( this.isExtremum( o,i,c,r ) ) this.getIpoint( o,i,c,r );


		} 

	}



	  ///////////////////
	 // OTHER METHODS //
	///////////////////

	/**	Calculate determinant of Hessians responses. */
	private void computeResponses() { 

		int lobe, border, step;
		double Dxx, Dyy, Dxy, scale;

		int width = this.integralImage.getXDim();
		int height = this.integralImage.getYDim();

		for( int o = 0 ; o < this.octaves ; o++ ) { 

			// calculate filter border for this octave
			border = (3 * Tools.cvround( Math.pow( 2,o+1 )*this.intervals +1 ) +1 )/2;
			step = this.initSample * Tools.cvround( Math.pow( 2,o ) );

			for( int i = 0 ; i < this.intervals ; i++ ) { 

				// calculate lobe length (filter side length/3)
				lobe = Tools.cvround( Math.pow( 2,o+1 )*( i+1 )+1 );
				scale = 1.0 / Math.pow( 3*lobe,2 );

				for( int y = border; y < height-border ; y += step ) { 
					for( int x = border; x < width-border ; x += step ) { 

						Dyy = this.integralImage.area( x-(lobe-1), y-((3*lobe-1)/2), 2*lobe-1, lobe )
						  - 2*this.integralImage.area( x-(lobe-1),  y-((lobe-1)/2) , 2*lobe-1, lobe )
						  +   this.integralImage.area( x-(lobe-1),  y+((lobe+1)/2) , 2*lobe-1, lobe );

						Dxx = this.integralImage.area( x-((3*lobe-1)/2), y-(lobe-1), lobe, 2*lobe-1 )
						  - 2*this.integralImage.area( x-((lobe-1)/2),   y-(lobe-1), lobe, 2*lobe-1 )
						  +   this.integralImage.area( x+((lobe+1)/2),   y-(lobe-1), lobe, 2*lobe-1 );
   
						Dxy = this.integralImage.area( x-lobe-1, y-lobe-1, lobe, lobe )
							+ this.integralImage.area( x+1     , y+1     , lobe, lobe )
							- this.integralImage.area( x-lobe-1, y+1     , lobe, lobe )
							- this.integralImage.area( x+1     , y-lobe-1, lobe, lobe );

						// Normalise the filter responses with respect to their size
						Dxx *= scale;
						Dyy *= scale;
						Dxy *= scale;

						// Get the sign of the laplacian
						int lap_sign = (Dxx+Dyy >= 0 ? 1 : -1);

						// Get the determinant of hessian response
						double res = Dxx*Dyy - 0.9*0.9*Dxy*Dxy;
						res = (res < this.thres ? 0 : lap_sign * res);

						// calculate approximated determinant of hessian value
						this.doh[ (o*this.intervals+i)*(width*height) + (y*width+x) ] = res;

					} // rof x
				} // rof y
			} // rof i
		} // rof o

	} // endfunc



	/**	Non Maximal Suppression function.
	 * @param octave
	 * @param interval
	 * @param c
	 * @param r
	 * @return If it is an extremum or not.
	 */
	private boolean isExtremum( int octave, int interval, int c, int r ) { 

		double val = this.getDoH( octave,interval,c,r );
		int step = this.initSample * Tools.cvround( Math.pow( 2,octave) );
		// reject points with low response to the determinant of hessian function
		if( val < this.thres ) return false;

		// check for maximum 
		for( int i = -1 ; i <= 1 ; i++ ) 
		for( int j = -step ; j <= step ; j += step ) 
		for( int k = -step ; k <= step ; k += step ) 
			if( this.getDoH( octave, interval+i, c+j, r+k ) > val ) return false;

		return true;
	}


	/**	
	 *	@param o
	 *	@param i
	 *	@param c
	 *	@param r
	 *	@return The precomputed value of the approximated determinant of hessians.
	 */
	private double getDoH( int o, int i, int c, int r ) { 

		double res = this.doh[ (o*this.intervals+i)*(this.width*this.height ) + (r*this.width+c) ];
		return Math.abs( res );
	}



	/**	Interpolate feature to sub pixel accuracy.
	 *	@param o
	 *	@param i
	 *	@param c
	 *	@param r
	 */
	private void getIpoint( int octave, int interval, int column, int row ) { 

		boolean converged = false;
		double [] x = { 0.0,0.0,0.0 };

		int o = octave;
		int i = interval;
		int c = column;
		int r = row;
		for( int steps = 0 ; steps <= this.interpSteps ; ++steps ) { 

			// perform a step of the interpolation
			x = this.stepInterp( o, i, c, r );

			// check stopping conditions
			if(		Math.abs( x[0] ) < 0.5 
				 && Math.abs( x[1] ) < 0.5 
				 && Math.abs( x[2] ) < 0.5 ) { 

				converged = true;
				break;
			}

			// find coords of different sample point
			c += Math.round( x[0] );
			r += Math.round( x[1] );
			i += Math.round( x[2] );

			// check if all params are within bounds
			if(		i < 1 || i >= this.intervals-1 
				 || c < 1 || c > this.width-1
				 || r < 1 || r > this.height-1 ) return;
		}

		// if interpolation has not converged on a result
		if( !converged ) return;

		// create Ipoint and push onto Ipoints vector
		Keypoint p = createKeypoint( (double) ( c+x[0] ), (double) ( r+x[1] ),			// x,y
								   (1.2/9.0) * (3*( Math.pow( 2,o+1 ) * (i+x[2]+1)+1) ),// scale
								   this.getSoL( o,i,c,r ) );							// laplacian
		keys.add( p );
	}

	private static Keypoint createKeypoint( double x, double y, double scale, int laplacian ) { 

		Double[] descriptor = new Double[2];
		descriptor[0] = scale;
		descriptor[1] = new Double(laplacian);

		DoubleArrayData data = new  DoubleArrayData();
//		data.setDescriptor( null );
		data.setValues( descriptor );

		return new Keypoint( x,y,data );

//		Keypoint point = new Keypoint( x,y );
//		point.descriptor = new double[2];
//		point.descriptor[0] = scale;
//		point.descriptor[1] = new Double(laplacian);
//		return point;
	}

	/**	Performs a step of interpolation (fitting 3D quadratic)
	 *	@param o
	 *	@param i
	 *	@param c
	 *	@param r
	 *	@param x
	 *	@return Resulting vector
	 */
	private double[] stepInterp( int o, int i, int c, int r ) { 

		int step = this.initSample * Tools.cvround( Math.pow( 2,o ) );

		// value of current pixel
		double val = getDoH( o, i, c, r );
 
		// first order derivs in 3D
		double dx = ( this.getDoH( o, i, c+step, r ) - this.getDoH( o, i, c-step, r ) ) / 2.0;
		double dy = ( this.getDoH( o, i, c, r+step ) - this.getDoH( o, i, c, r-step ) ) / 2.0;
		double ds = ( this.getDoH( o, i+1, c, r )    - this.getDoH( o, i-1, c, r )    ) / 2.0;

		// second order derivs in 3D
		double dxx = this.getDoH( o, i, c+step, r ) + this.getDoH( o, i, c-step, r ) - 2*val;
		double dyy = this.getDoH( o, i, c, r+step ) + this.getDoH( o, i, c, r-step ) - 2*val;
		double dss = this.getDoH( o, i+1, c, r )    + this.getDoH( o, i-1, c, r )    - 2*val;
		double dxy = ( this.getDoH( o, i, c+step, r+step ) 
					 - this.getDoH( o, i, c-step, r+step ) 
					 - this.getDoH( o, i, c+step, r-step ) 
					 + this.getDoH( o, i, c-step, r-step ) ) / 4.0;
		double dxs = ( this.getDoH( o, i+1, c+step, r ) 
					 - this.getDoH( o, i+1, c-step, r )
					 - this.getDoH( o, i-1, c+step, r ) 
					 + this.getDoH( o, i-1, c-step, r ) ) / 4.0;
		double dys = ( this.getDoH( o, i+1, c, r+step ) 
					 - this.getDoH( o, i+1, c, r-step ) 
					 - this.getDoH( o, i-1, c, r+step ) 
					 + this.getDoH( o, i-1, c, r-step ) ) / 4.0;

		// calculate determinant of:
		//	| dxx dxy dxs |
		//	| dxy dyy dys | 
		//	| dxs dys dss |
		double det = dxx * ( dyy*dss-dys*dys) - dxy * (dxy*dss-dxs*dys) + dxs * (dxy*dys-dxs*dyy);

		// calculate resulting vector after matrix mult:
		//	| dxx dxy dxs |-1  | dx |
		//	| dxy dyy dys |  X | dy |
		//	| dxs dys dss |    | ds |
		double[] v = new double[3];
		v[0] = -1.0/det *( dx*( dyy*dss-dys*dys ) +dy*( dxs*dys-dss*dxy ) +ds*( dxy*dys-dyy*dxs ) );
		v[1] = -1.0/det *( dx*( dys*dxs-dss*dxy ) +dy*( dxx*dss-dxs*dxs ) +ds*( dxs*dxy-dys*dxx ) );
		v[2] = -1.0/det *( dx*( dxy*dys-dxs*dyy ) +dy*( dxy*dxs-dxx*dys ) +ds*( dxx*dyy-dxy*dxy ) );

		return v;
	}

	/**	
	 *	@param o
	 *	@param i
	 *	@param c
	 *	@param r
	 *	@return The sign of the laplacian (trace of the hessian)
	 */
	private int getSoL( int o, int i, int c, int r ) { 

		double res = this.doh[ (o*this.intervals+i)*(this.width*this.height ) + (r*this.width+c) ];
	    return ( res >= 0 ? 1 : -1 );
	}



}
