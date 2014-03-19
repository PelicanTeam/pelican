package fr.unistra.pelican.algorithms.descriptors.localinvariants;

import java.awt.Point;
import java.io.*;
import java.util.*;

import fr.unistra.pelican.*;
import fr.unistra.pelican.algorithms.detection.FastHessian;
import fr.unistra.pelican.util.*;
import fr.unistra.pelican.util.data.*;
import fr.unistra.pelican.util.data.distances.KeypointArraySURFDistance;



/**
 *	SURF interest points descriptor as described in the following paper :
 *
 *	Herbert Bay, Andreas Ess, Tinne Tuytelaars, Luc Van Gool, "SURF: Speeded Up Robust Features", 
 *	Computer Vision and Image Understanding (CVIU), Vol. 110, No. 3, pp. 346--359, 2008
 *
 *	( Get it there : <url>http://www.vision.ee.ethz.ch/~surf/papers.html</url> )
 *
 *	@author Régis Witz
 *	@date 27.01.09
 */
public class SURF extends Descriptor { 

	  ////////////
	 // INPUTS //
	////////////

	public Image input;

	  /////////////
	 // OUTPUTS //
	/////////////

	/**	Output parameter. */
	public KeypointArrayData output;

	  /////////////
	 // OPTIONS //
	/////////////

	/**	<tt>true</tt> for U-SURF. */
	public boolean upright = false;

	  //////////////////
	 // OTHER FIELDS //
	//////////////////

	public ArrayList<Keypoint> keys;

	IntegralImage integralImage;

	  ///////////////
	 // CONSTANTS //
	///////////////

	public static final int DESCRIPTOR_LENGTH = 64;

	  /////////////////
	 // CONSTRUCTOR //
	/////////////////

	public SURF() { 

		super.inputs = "input";
		super.options = "upright";
		super.outputs = "output";
	}

	  ////////////////////
	 // "EXEC" METHODS //
	////////////////////

	public static KeypointArrayData exec( Image input ) { 
		return ( KeypointArrayData ) new SURF().process( input );
	}

	  /////////////////////
	 // "LAUNCH" METHOD //
	/////////////////////

	@SuppressWarnings("unchecked")
	public void launch() { 

		this.integralImage = new IntegralImage( this.input );
		this.keys = FastHessian.exec( this.integralImage );

		int size = this.keys.size();
		// Check if there are keypoints to be described
		if ( size == 0 ) return;

		if ( this.upright ) { 

			// U-SURF loop just gets descriptors
			for ( int i = 0 ; i < size ; ++i ) { 

				// Extract upright (i.e. not rotation invariant) descriptors
				this.getUprightDescriptor( i );
			}

		} else { 

			// assign orientations 
			for ( int i = 0 ; i < size ; ++i ) this.getOrientation( i ); 
			// with getOrientation(), additional points can be added, 
			// so make sure this newbies are been taken in account
			size = this.keys.size();
			// extract rotation invariant descriptors
			for ( int i = 0 ; i < size ; ++i ) this.getDescriptor( i );
		}

		this.output = new KeypointArrayData();
		this.output.setDescriptor( ( Class ) this.getClass() );
		this.output.setValues( this.keys );

	}





	  ///////////////////
	 // OTHER METHODS //
	///////////////////

	private static double getScale( Keypoint key ) { 

		Double[] descriptor = ( Double[] ) key.data.getValues();
		return descriptor[0];
	}
	private static int getLaplacian( Keypoint key ) { 

		Double[] descriptor = ( Double[] ) key.data.getValues();
		return new Double( descriptor[1] ).intValue();
	}
	private static double getOrientation( Keypoint key ) { 

		Double[] descriptor = ( Double[] ) key.data.getValues();
		return descriptor[2];
	}
	@SuppressWarnings("unchecked")
	private static void setOrientation( Keypoint key, double orientation ) { 

		Double[] descriptor = ( Double[] ) key.data.getValues();
		Double[] desc = new Double[3];
		desc[0] = descriptor[0];	// scale
		desc[1] = descriptor[1];	// laplacian
		desc[2] = orientation;		// orientation
		DoubleArrayData data = new DoubleArrayData();
		data.setDescriptor( ( Class ) new SURF().getClass() );
		data.setValues( desc );
		key.data = data;
	}

	/**	
	 *	@param index Index of current interest point in the vector.
	 */
	private void getOrientation( int index ) { 

		Keypoint ipt = this.keys.get( index );
		double gauss;
		int s = Tools.cvround( SURF.getScale( ipt ) );
		int r = Tools.cvround( ipt.y );
		int c = Tools.cvround( ipt.x );
		ArrayList<Double> resX = new ArrayList<Double> ();
		ArrayList<Double> resY = new ArrayList<Double> ();
		ArrayList<Double> resAngle = new ArrayList<Double> ();

		// calculate haar responses for points within radius of 6*scale
		for( int i = -6*s; i <= 6*s; i += s ) 
		for( int j = -6*s; j <= 6*s; j += s ) 
			if ( i*i + j*j  < 36*s*s ) { // check if current sample point is within the circle

				gauss = gaussian( new Double(i),new Double(j), 2.5*s );
				double gaussHaarX = gauss * this.haarX( r+j,c+i,4*s );
				double gaussHaarY = gauss * this.haarY( r+j,c+i,4*s );
				resX.add( new Double( gaussHaarX ) );
				resY.add( new Double( gaussHaarY ) );
				resAngle.add( this.getAngle( gaussHaarX,gaussHaarY ) );
			}

		// calculate the dominant direction 
		double sumX, sumY;
		double max = 0, old_max = 0, orientation = 0, old_orientation = 0;
		double ang1, ang2, ang;

		// loop slides pi/3 window around feature point
		for( ang1 = 0 ; ang1 < 2*Math.PI ;  ang1 += 0.2 ) { 

			ang2 = ( ang1+Math.PI/3.0 > 2*Math.PI ? ang1-5.0*Math.PI/3.0 : ang1+Math.PI/3.0);
			sumX = sumY = 0; 

			for( int k = 0; k < resAngle.size(); k++) { 

				// get angle from the x-axis of the sample point
				ang = resAngle.get(k);

				// determine whether the point is within the window
				if ( ang1 < ang2 && ang1 < ang && ang < ang2 ) {

					sumX += resX.get(k);  
					sumY += resY.get(k);

				} else 
				if ( ang2 < ang1 &&  ( ( ang > 0 && ang < ang2 ) 
									|| ( ang > ang1 && ang < 2*Math.PI ) ) ) { 

					sumX += resX.get(k);  
					sumY += resY.get(k);
				}
			}

			// if the vector produced from this window is longer than all 
			// previous vectors then this forms the new dominant direction
			if ( sumX*sumX + sumY*sumY > max )  { 

				// store second largest orientation
				old_max = max;
				old_orientation = orientation;

				// store largest orientation
				max = sumX*sumX + sumY*sumY;
				orientation = this.getAngle( sumX,sumY );
			}
		}

		// check whether there are two dominant orientations based on 0.8 threshold
		if ( old_max >= 0.8*max ) { 

			// assign second largest orientation and push copy onto vector
			Keypoint ipt2 = ipt.clone();
			SURF.setOrientation( ipt2, old_orientation );
			keys.add( ipt2 );
		}

		// assign orientation of the dominant response vector
		SURF.setOrientation( ipt, orientation );
	}



	/**	Get the descriptor vector of the provided IPoint
	 *	@param index Index of current interest point in the vector.
	 */
	@SuppressWarnings("unchecked")
	private void getDescriptor( int index ) { 

		int count = 0;
		double dx, dy, mdx, mdy;
		double gauss, rx, ry, rrx, rry, len = 0;

		Keypoint ipt = this.keys.get( index );
		int x = Tools.cvround( ipt.x );
		int y = Tools.cvround( ipt.y );  
		double scale = SURF.getScale( ipt );
		double co = Math.cos( SURF.getOrientation( ipt ) );
		double si = Math.sin( SURF.getOrientation( ipt ) );
		Double[] desc = new Double[ DESCRIPTOR_LENGTH + 3 ];

		desc[ count++ ] = SURF.getScale( ipt );
		desc[ count++ ] = new Double( SURF.getLaplacian( ipt ) );
		desc[ count++ ] = SURF.getOrientation( ipt );

		// Calculate descriptor for this interest point
		for ( int i = -10; i < 10; i += 5 ) { 
			for ( int j = -10; j < 10; j += 5 ) { 

				dx = dy = mdx = mdy = 0;
				for ( int k = i; k < i+5 ; ++k ) { 

					for ( int l = j; l < j+5 ; ++l ) { 

						// Get coords of sample point on the rotated axis
						int sample_x = Tools.cvround( x + (-l*scale*si + k*scale*co ) );
				        int sample_y = Tools.cvround( y + ( l*scale*co + k*scale*si ) );

						// Get the gaussian weighted x and y responses
						gauss = this.gaussian( k*scale, l*scale, 3.3*scale );  
						rx = gauss * this.haarX( sample_y, sample_x, 2*scale );
						ry = gauss * this.haarY( sample_y, sample_x, 2*scale );

						// Get the gaussian weighted x and y responses on rotated axis
						rrx = -rx*si + ry*co;
						rry = rx*co + ry*si;

						dx += rrx;
						dy += rry;
						mdx += Math.abs(rrx);
						mdy += Math.abs(rry);
					}
				}

				// add the values to the descriptor vector
				desc[ count++ ] = dx;
				desc[ count++ ] = dy;
				desc[ count++ ] = mdx;
				desc[ count++ ] = mdy;

				// store the current length^2 of the vector
				len += dx*dx + dy*dy + mdx*mdx + mdy*mdy;
			}
		}

		// convert to unit vector (for contrast invariance)
		len = Math.sqrt( len );
		for( int i = 0 ; i < DESCRIPTOR_LENGTH ; i++ ) desc[i+3] /= len;

		DoubleArrayData data = new DoubleArrayData();
		data.setDescriptor( ( Class ) this.getClass() );
		data.setValues( desc );
		ipt.data = data;

	}





	/**	Get the upright descriptor vector of the provided Ipoint
	 *	@param index
	 */
	@SuppressWarnings("unchecked")
	void getUprightDescriptor( int index ) { 

		int count = 0;
		double dx, dy, mdx, mdy;
		double gauss, rx, ry, len = 0.0;

		Keypoint ipt = this.keys.get( index );

		int scale = (int)Math.round( SURF.getScale( ipt ) );
		int y = Tools.cvround( ipt.y );  
		int x = Tools.cvround( ipt.x );
/////
		double[] desc = new double[ DESCRIPTOR_LENGTH + 3 ];

		desc[ count++ ] = SURF.getScale( ipt );
		desc[ count++ ] = SURF.getLaplacian( ipt );
		desc[ count++ ] = 0.0;	// no orientation is calculated here

		// Calculate descriptor for this interest point
		for ( int i = -10 ; i < 10 ; i += 5 ) { 

			for ( int j = -10 ; j < 10 ; j +=5  ) { 

				dx = dy = mdx = mdy = 0;

				for ( int k = i ; k < i+5 ; ++k ) { 

					for ( int l = j ; l < j+5; ++l ) { 

						// get Gaussian weighted x and y responses
						gauss = this.gaussian( k*scale, l*scale, 3.3*scale );  
						rx = gauss * this.haarX( k*scale+y, l*scale+x, 2*scale );
						ry = gauss * this.haarY( k*scale+y, l*scale+x, 2*scale );

						dx += rx;
						dy += ry;
						mdx += Math.abs(rx);
						mdy += Math.abs(ry);
					}
				}

				// add the values to the descriptor vector
				desc[ count++ ] = dx;
				desc[ count++ ] = dy;
				desc[ count++ ] = mdx;
				desc[ count++ ] = mdy;

				// store the current length^2 of the vector
				len += dx*dx + dy*dy + mdx*mdx + mdy*mdy;

			} // rof j
		} // rof i

		// convert to unit vector
		len = Math.sqrt(len);
		for( int i = 0 ; i < DESCRIPTOR_LENGTH ; i++ ) desc[i+3] /= len;

		DoubleArrayData data = new DoubleArrayData();
		data.setDescriptor( ( Class ) this.getClass() );
		data.setValues( desc );
		ipt.data = data;

	}





	/**	Calculate the value of the 2d gaussian at x,y
	 *	@param x
	 *	@param y
	 *	@param sig
	 *	@return
	 */
	double gaussian( double x, double y, double sig ) { 
		return 1./(2.*Math.PI*sig*sig) * Math.exp( -(x*x+y*y)/(2.*sig*sig));
	}

	/**	Calculate Haar wavelet responses in x direction
	 *	@param row
	 * 	@param column
	 *	@param s
	 *	@return
	 */
	double haarX( int row, int column, double s ) { 

		int vs = new Double(s).intValue();
		int vs2 = new Double(s/2.).intValue();
		return (	this.integralImage.area( column,row-vs2, vs2,vs ) 
				-1* this.integralImage.area( column-vs2,row-vs2, vs2,vs ) );
	}

	/**	Calculate Haar wavelet responses in y direction
	 *	@param row
	 * 	@param column
	 *	@param s
	 *	@return
	 */
	double haarY( int row, int column, double s ) { 

		int vs = new Double(s).intValue();
		int vs2 = new Double(s/2.).intValue();
		return (	this.integralImage.area( column-vs2,row, vs,vs2 ) 
				-1* this.integralImage.area( column-vs2,row-vs2, vs,vs2 ) );
	}

	/**	Get the angle from the +ve x-axis of the vector given by (x,y)
	 *	@param x
	 *	@param y
	 *	@return
	 */
	double getAngle( double x, double y ) { 

		if ( x >= 0 && y >= 0 ) return Math.atan(y/x);
		if ( x < 0 && y >= 0 ) return Math.PI - Math.atan(-y/x);
		if ( x < 0 && y < 0 ) return Math.PI + Math.atan(y/x);
		if ( x >= 0 && y < 0 ) return 2*Math.PI - Math.atan(-y/x);
		return 0;
	}





	public static double distance( Data d1, Data d2 ) {

		return new KeypointArraySURFDistance().distance( d1, d2 );
	}





	  ////////////////
	 // SURF TOOLS //
	////////////////



	/**	Write set of keypoints to a file in ASCII format.  
	 *	The file format starts with 2 integers giving the total number of
	 *	keypoints, and size of descriptor vector for each keypoint. Then
	 *	each keypoint is specified by 4 floating point numbers giving
	 *	subpixel row and column location, scale, and orientation (in 
	 *	radians from -PI to PI).  Then the descriptor vector for each
	 *	keypoint is given as a list of integers in range [0,255].
	 *
	 *	@param path 
	 *	@param keys 
	 */
	public static void writeKeypoints( String path, double[] keys ) { 

		int step = 5+SURF.DESCRIPTOR_LENGTH;
		int count = keys.length / step;

		FileWriter fw = null;
		try { fw = new FileWriter( path ); }
		catch ( java.io.IOException ex ) { ex.printStackTrace(); }
		if ( fw == null ) { 

			System.err.println( "Couldn't write keypoints into " + path + "." );
			return;
		}
		PrintWriter pw = new PrintWriter( new BufferedWriter( fw ) );
		String s = "";

		for ( int i = 0 ; i < count ; i++ ) { 

			s = "";
			s += keys[  i*step  ] + "," + keys[ i*step +1 ];
			s += "," + keys[ i*step +2 ];
			s += "," + keys[ i*step +3 ];
			s += "," + keys[ i*step +4 ];
			for ( int j = 0 ; j < SURF.DESCRIPTOR_LENGTH ; j++ ) s += "," + keys[ i*step +5+j ];

			pw.println(s);
		}
		pw.close();
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<Keypoint> readKeypoints( String path ) { 

		ArrayList<Keypoint> keys = new ArrayList<Keypoint>();

		FileReader fr = null;
		try { fr = new FileReader( path ); }
		catch ( java.io.IOException ex ) { ex.printStackTrace(); }
		BufferedReader br = null;
		br = new BufferedReader( fr );
		String s = "$";

		try { s = br.readLine(); }
		catch ( IOException ex ) { ex.printStackTrace(); }
		while ( s != null ) { 

			if ( s.length() == 0 ) continue;

			String[] cut = s.split( "," );
			Keypoint k = null;
			k = new Keypoint( new Double( cut[0] ), new Double( cut[1] ) );
			double[] descriptor = new double[ 64 + 3 ];
			if ( cut.length > 2 ) descriptor[0] = new Double( cut[2] );
			if ( cut.length > 2 ) descriptor[2] = new Double( cut[3] );
			if ( cut.length > 2 ) descriptor[1] = new Integer( cut[4] );

			if ( cut.length > 5 ) 
				for ( int i = 0 ; i < 64 ; i++ ) descriptor[ i+3 ] = new Double( cut[ 5+i ] );

			DoubleArrayData data = new DoubleArrayData();
			data.setDescriptor( ( Class ) new SURF().getClass() );
			data.setValues( descriptor );
			k.data = data;
	
			keys.add( k );

			try { s = br.readLine(); }
			catch ( IOException ex ) { ex.printStackTrace(); }
		}

		try { br.close(); }
		catch ( IOException ex ) { ex.printStackTrace(); }

		return keys;
	} // endfunc



	public static Image displayMatch( Image image1, 
									  Image image2, 
									  HashMap<Keypoint,Keypoint> matchmap ) { 

		int frontierwidth = 10;
		int xdim1 = image1.getXDim();
		int ydim1 = image1.getYDim();
		int xdim2 = image2.getXDim();
		int ydim2 = image2.getYDim();
		int xout = xdim1 +  frontierwidth + xdim2;
		int yout = Math.max( ydim1,ydim2 );
		int bdim = Math.max( image1.getBDim(),image2.getBDim() );
		ByteImage output = new ByteImage( xout,yout,1,1,bdim );
		int[] bgcolor = { 0,0,0 };		// background color
		int[] lkcolor = { 0,0,255 };	// links between points color
		int[] cscolor = { 255,0,0 };	// points crosses color
		int crosssize = 5;				// size in pixels of points crosses

		// copy image1 to the left of output
		for ( int x = 0 ; x < xdim1 ; x++ ) { 

			for ( int y = 0 ; y < ydim1 ; y++ ) 
				output.setVectorPixelXYZTByte( x,y,0,0, image1.getVectorPixelXYZTByte( x,y,0,0 ) );
			// fill the space between image1 and the bottom of output if any
			if ( ydim1 < yout ) 
				for ( int y = ydim1 ; y < yout ; y++ ) 
						output.setVectorPixelXYZTByte( x,y,0,0, bgcolor );
		}

		// fill the frontier between image1 and image2 in output
		int xoffset = xdim1;
		for ( int x = 0 ; x < frontierwidth ; x++ ) 
			for ( int y = 0 ; y < yout ; y++ ) 
				output.setVectorPixelXYZTByte( x+xoffset,y,0,0, bgcolor );

		// copy image2 to the right of output
		xoffset = xdim1+frontierwidth;
		for ( int x = 0 ; x < xdim2 ; x++ ) { 

			for ( int y = 0 ; y < ydim2 ; y++ ) 
				output.setVectorPixelXYZTByte( x+xoffset,y,0,0, image2.getVectorPixelXYZTByte( x,y,0,0 ) );
			// fill the space between image1 and the bottom of output if any
			if ( ydim2 < yout ) 
				for ( int y = ydim2 ; y < yout ; y++ ) 
						output.setVectorPixelXYZTByte( x+xoffset,y,0,0, bgcolor );
		}

		// draw lines between each matching couple ( keys1.k,keys2.k )
		Keypoint k1,k2;
		Point p1,p2;
		Iterator<Keypoint> iterator = matchmap.keySet().iterator();
		while(	iterator.hasNext() ) {

			k1 = iterator.next();
			k2 = matchmap.get( k1 );
			p1 = new Point( (int)k1.x,(int)k1.y );
			p2 = new Point( (int)k2.x+xoffset,(int)k2.y );

			SURF.drawCross( output, p1, crosssize, cscolor );
			SURF.drawCross( output, p2, crosssize, cscolor );
			SURF.drawLine( output, p1,p2, lkcolor );

		}

		return output;
	} // endfunc

	public static void drawCross( Image image, Point p, int size, int[] color ) { 

		for ( int i = p.x-size ; i <= p.x+size ; i++ ) 
			image.setVectorPixelXYZTByte( i,p.y,0,0, color );
		for ( int j = p.y-size ; j <= p.y+size ; j++ ) 
			image.setVectorPixelXYZTByte( p.x,j,0,0, color );
	}

	public static void drawLine( Image image, Point p1, Point p2, int[] color ) { 

		Line link = new Line( p1,p2 );
		Point p;
		java.util.Iterator<Point> it = link.iterator();
		while ( it.hasNext() ) { 

			p = it.next();
			image.setVectorPixelXYZTByte( p.x,p.y,0,0, color );
		}
	}




	  ////////////////////////
	 // DEMONSTRATION MAIN //
	////////////////////////

	public static void main( String[] args ) { 

		boolean color = false;

		String imagedbpath = "/home/miv/witz/Desktop/petitesbases/eiffel/";
		File root = new File( imagedbpath );
		File[] files = root.listFiles();
		ArrayList<String> paths = new ArrayList<String>();
		HashMap<String,Image> images = new HashMap<String,Image>();
		HashMap<String,KeypointArrayData> results = new HashMap<String,KeypointArrayData>();
		int c = 0;
		for ( int i = 0 ; i < files.length ; i++ ) { 

			String path = files[i].getPath();
			if ( !path.endsWith( ".jpg" ) ) continue;
			paths.add( path );
		}
		Collections.sort( paths );

		for ( String path : paths ) { 

			System.out.print( "load image \"" + path + "\" ( " + (c++) + " ) ... " );
			Image image = fr.unistra.pelican.algorithms.io.ImageLoader.exec( path );
			System.out.print( "compute SURF on it ... " );
			if ( !color ) image = fr.unistra.pelican.algorithms.conversion.RGBToGray.exec( image );
			KeypointArrayData data = SURF.exec( image );
			System.out.println( "done." );
			images.put( path,image );
			results.put( path,data );
			path = null;
		}

		String path1,path2,opath;
		Image image1,image2;
		KeypointArrayData data1,data2;
		Image matches;
		for ( int i = 0 ; i < results.size() ; i++ ) {

			path1 = getKey( results,i );
			data1 = results.get( path1 );
			image1 = images.get( path1 );
			for ( int j = i+1 ; j < results.size() ; j++ ) {

				path2 = getKey( results,j );
				data2 = results.get( path2 );
				image2 = images.get( path2 );

				double distance = data1.distanceTo( data2 );
				System.out.println( "Distance of " + i + " to " + j + " : " + distance + "." );
				HashMap<Keypoint,Keypoint> matchmap = new fr.unistra.pelican.util.data.distances
											.KeypointArraySURFDistance().getMatches( data1,data2 ); 
				matches = SURF.displayMatch( image1,image2, matchmap );
				opath = imagedbpath + "/matches/" + i + "vs" + j + ":" + matchmap.size() + ".jpg";
				fr.unistra.pelican.algorithms.io.ImageSave.exec( matches,opath );
//				System.out.println( "Image saved to \"" + opath + "\"." );
			}
		}

		System.out.println( "endfunc." );
	}

	private static String getKey( HashMap<String,KeypointArrayData> map, int i ) {

		String path;
		Iterator<String> iterator = map.keySet().iterator();
		int c = -1;
		while ( iterator.hasNext() ) { 

			path = iterator.next();
			c++;
			if ( c < i ) continue;
			return path;
		}
		return null;
	}



}
