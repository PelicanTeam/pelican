package fr.unistra.pelican.algorithms.descriptors.texture;

import fr.unistra.pelican.*;
import fr.unistra.pelican.algorithms.geometric.Subdivide;
import fr.unistra.pelican.util.data.*;



/**	
 *	Edge histogram descriptor (EHD) from MPEG-7 standard.
 *
 *	Chee Sun Won, Dong Kwon Park, and Soo-Jun Park, 
 *	"Efficient Use of MPEG-7 Edge Histogram Descriptor".
 *
 *	( Get it there : 
 *	<url>http://etrij.etri.re.kr/Cyber/servlet/GetFile?fileid=SPF-1041924741673</url> )
 * 
 *	@author Régis Witz
 *	@date 8.4.2009
 */
public class EdgeHistogram extends Descriptor {



	  ////////////
	 // FIELDS //
	////////////

	/**	Input image. */
	public Image input;

	/**	Output feature. */
	public DataArrayData output;

	/**	With {@link #size} = 2 you respect MPEG-7, wich says 
	 *	the size of EHD feature must be 4x4x5 = 80.
	 */
	public int size = 2;

	/**	If an image block pixel values x <i>filter</i> > {@link #tEdge}, 
	 *	this image block corresponding to <i>filter</i>. 
	 */
	public double tEdge = 0.01;

	/**	If an image block is composed of pixel values different for less than {@link #tMonoton}, 
	 *	this block is considered as part of a monoton region (no edge).
	 */
	public double tMonoton = 0.01;


	  /////////////////
	 // CONSTRUCTOR //
	/////////////////

	/**	Algorithm specifications. */
	public EdgeHistogram() {

		super();
		super.inputs  = "input";
		super.options = "tEdge,tMonoton,size";
		super.outputs = "output";
	}




	  ///////////////
	 // CONSTANTS //
	///////////////

	/**	Used in vertical edge detection operations. */
	private static final int VERTICAL_EDGE = 0;
	/**	Used in horizontal edge detection operations. */
	private static final int HORIZONTAL_EDGE = 1;
	/**	Used in up-right/bottom-left diagonal edge detection operations. */
	private static final int DIAGONAL_45_EDGE = 2;
	/**	Used in up-left/bottom-right diagonal edge detection operations. */
	private static final int DIAGONAL_135_EDGE = 3;
	/**	Used in non directional edge detection operations. */
	private static final int NON_DIRECTIONAL_EDGE = 4;

	/**	Square root of 2. */
	private static final double V2 = Math.sqrt(2.);

	/**	Filters allowing to judge of the edgeness of an image block. */
	private static final double[][] filters =	
		{
			// vertical filter :
			{  1,-1, 
			   1,-1 }
			, 
			// horizontal filter :
			{  1, 1, 
			  -1,-1 }
			, 
			// 45-diagonal filter :
			{ V2, 0, 
			   0,V2 }
			, 
			// 135-diagonal filter :
			{ 0,V2, 
			 V2, 0 }
			, 
			// non directional filter :
			{ 2,-2, 
			 -2, 2 }
			,
		};
	/**	Number of bins of a sub image histogram. */
	private static final int nbBins = filters.length;



	  /////////////
	 // METHODS //
	/////////////

	@SuppressWarnings("unchecked")
	@Override
	/**	@see Algorithm */
	public void launch() throws AlgorithmException {

		if ( this.size < 2 ) throw new PelicanException( "Size must be at least 2, sorry." );

		this.size = new Double( Math.pow( 2.,this.size ) ).intValue();
		int nbSubimages = this.size*this.size;
		HistogramData[] histograms = new HistogramData[ nbSubimages ];

		Image[] subimages = new Image[1];
		subimages[0] = this.input;
		for ( int i = 1 ; i <= this.size ; i++ ) { 

			Image[] tmp = new Image[ 4*subimages.length ];
			for ( int j = 0 ; j < subimages.length ; j++ ) {

				Image[] inputs = Subdivide.exec( subimages[j], Subdivide.Uniform2x2 );
				for ( int k = 0 ; k < 4 ; k++ ) tmp[ 4*j+k ] = inputs[k];
				inputs = null;
			}
			subimages = tmp;
			tmp = null;
		}

		Image[] imageblocks;
		for ( int i = 0 ; i < this.size ; i++ ) { 

			for ( int j = 0 ; j < this.size ; j++ ) {

				imageblocks = Subdivide.exec( subimages[ ( i*this.size )+j  ], Subdivide.Lil22 ) ;
				// initialize then fill the local histogram corresponding to this subimage
				Double[] bins = new Double[ nbBins ];
				for ( int bin = 0 ; bin < nbBins ; bin++ ) bins[bin] = new Double( 0 );
				for ( int b = 0 ; b < imageblocks.length ; b++ ) 
					this.edgeClassification( imageblocks[b], bins );
				// normalize histogram. 
				// 
				if ( imageblocks.length > 0 )
					for ( int bin = 0 ; bin < nbBins ; bin++ ) bins[bin] /= imageblocks.length;

				// TODO: here, histogram should not be filled with bins directly. instead, each 
				//		 bin of bins should be quantized to be a 3-bits number.
				//		 see the paper for more details.
				//		 as HistogramData contains doubles, an other data structure should be used.
				HistogramData histogram = new HistogramData();
				histogram.setDescriptor( ( Class ) EdgeHistogram.class );
				histogram.setValues( bins );
				histograms[ i*this.size + j ] = histogram;

				// clear trash
				for ( int b = 0 ; b < imageblocks.length ; b++ ) imageblocks[b] = null;
				imageblocks = null;
			}
		}

		this.output = new DataArrayData();
		this.output.setDescriptor( ( Class ) EdgeHistogram.class );
		this.output.setValues( histograms );

	}



	/**	Tries to find if an image block depicts an edge. If so, increments the appropriate bin 
	 *	value of the local histogram. If no edge was found, this method does nothing. 
	 *	<p>
	 *	Please note that this implies that, at the very end of this algorithm, each 5-bin histogram 
	 *	length is not always equal to 1, but is in fact less than or equal to 1. This allows to 
	 *	consider information regarding non-edge distribution ( <i>ie.</i> smoothness ) in EHD.
	 *
	 *	@param block 2x2 image block.
	 *	@param bins Local histogram for a subimage. It must have exactly 5 bins.
	 */
	private void edgeClassification( Image block, Double[] bins ) {

			 if ( isEdge( block,VERTICAL_EDGE ) ) bins[ VERTICAL_EDGE ]++;
		else if ( isEdge( block,HORIZONTAL_EDGE ) ) bins[ HORIZONTAL_EDGE ]++;
		else if ( isEdge( block,DIAGONAL_45_EDGE ) ) bins[ DIAGONAL_45_EDGE ]++;
		else if ( isEdge( block,DIAGONAL_135_EDGE ) ) bins[ DIAGONAL_135_EDGE ]++;
		else if ( isMonoton( block ) ) ; 		// this is why histo length can be < 1.
		else bins[ NON_DIRECTIONAL_EDGE ]++;	// yes, filters[NON_DIRECTIONAL_EDGE]'s not used
			 									// this is dificult to define such filter ...
	}

	/**	Attempts to find if an image block <tt>block</tt> is an edge of type <tt>type</tt>.
	 *	@param block 2x2 image block.
	 *	@param type [ {@link #VERTICAL_EDGE} | {@link #HORIZONTAL_EDGE} 
	 *				| {@link #DIAGONAL_45_EDGE} | {@link #DIAGONAL_135_EDGE} 
	 *				| {@link #NON_DIRECTIONAL_EDGE} ]
	 *	@return <tt>true</tt> if <tt>block</tt> is an edge of type <tt>type</tt>, 
	 *			or else <tt>falsek</tt>.
	 */
	private boolean isEdge( Image block,int type ) {

		double[] filter = filters[ type ];
		double res =	block.getPixelXYDouble( 0,0 ) * filter[0] 
		              * block.getPixelXYDouble( 1,0 ) * filter[1]
		              * block.getPixelXYDouble( 0,1 ) * filter[2]
		              * block.getPixelXYDouble( 1,1 ) * filter[3]
		              ;
		return res > this.tEdge;
	}

	private boolean isMonoton( Image block ) { 

		double p00 = block.getPixelXYDouble( 0,0 );
		double p10 = block.getPixelXYDouble( 1,0 );
		double p01 = block.getPixelXYDouble( 0,1 );
		double p11 = block.getPixelXYDouble( 1,1 );
		if ( !okay( p00,p10 ) ) return false;
		if ( !okay( p00,p01 ) ) return false;
		if ( !okay( p00,p11 ) ) return false;
		if ( !okay( p10,p01 ) ) return false;
		if ( !okay( p10,p11 ) ) return false;
		if ( !okay( p01,p11 ) ) return false;
		return true;
	}

	private boolean okay( double p1, double p2 ) { 

		double min = Math.min( p1,p2 );
		double max = Math.max( p1,p2 );
		if ( min + tMonoton < max ) return false;
		return true;
	}





	  //////////////////////
	 // DISTANCE METHODS //
	//////////////////////

	/**	Distance between two given precomputed {@link Data}, 
	 *	customized for this descriptor. 
	 *
	 *	@param d1 First data
	 *	@param d2 Second data
	 *	@return Distance €[0;1] between <tt>d1</tt> and <tt>d2</tt>.
	 */
	public static double distance( Data d1, Data d2 ) { 

		Data[] h1 = ( Data[] ) ( ( DataArrayData )d1 ).getValues();
		Data[] h2 = ( Data[] ) ( ( DataArrayData )d2 ).getValues();
		int len = h1.length;
		if ( len != h2.length ) { 

			System.err.println( "Incompatible histogram numbers : "+len+" vs "+h2.length +"." );
			return 1.;
		}

		double[] g1 = new double[ nbBins ]; // global histogram corresponding to d1
		double[] g2 = new double[ nbBins ]; // global histogram corresponding to d2

		// distances. the latters are non standard adds of the paper.
		double local = 0;
		double global = 0;
		double semiglobal = 0;

		for ( int h = 0 ; h < len ; h++ ) { 

			local += subimagesDistance( h1[h],h2[h] );

			Double[] b1 = ( Double[] ) h1[h].getValues();
			Double[] b2 = ( Double[] ) h2[h].getValues();
			for ( int b = 0 ; b < nbBins ; b++ ) { 

				// fill global histograms
				g1[b] += b1[b];
				g2[b] += b2[b];
			}

		}

		// compute global distance
		for ( int b = 0 ; b < nbBins ; b++ ) 
			global += Math.abs( g1[b]-g2[b] );
		global *= nbBins;

		int side = new Double( Math.sqrt( len ) ).intValue(); 
		// if you have any doubts, remember how {@link #size} was computed ...

		double semiglobalverti = 0;	// semi global distances 1,2,3,4
		double semiglobalhoriz = 0; // semi global distances 5,6,7,8
		for ( int i = 0 ; i < side ; i++ ) { 

			for ( int j = 0 ; j < side ; j++ ) {

				semiglobalverti += subimagesDistance( h1[ j*side+i ],h2[ j*side+i ] );
				semiglobalhoriz += subimagesDistance( h1[ i*side+j ],h2[ i*side+j ] );
			}
		}
		// propagate these semiglobal distances
		semiglobal += semiglobalverti;
		semiglobal += semiglobalhoriz;

		// this was tiring to generalize this one T_T  
		// I didn't know if java compiler is clever, had to extract loop invarants ..
		int _s = side/2;
		int o10 = _s;				// offset for semiglobal distance 10
		int o11 = _s*side;			// offset for semiglobal distance 11
		int o12 = _s*side+_s;		// offset for semiglobal distance 12
		int o13 = (_s/2)*side+_s;	// offset for semiglobal distance 13
		int o;
		for ( int i = 0 ; i < _s ; i++ ) { 
			for ( int j = 0 ; j < _s ; j++ ) { 

				// partly compute each semi global distance of 9,10,11,12,13 
				o = _s*i+j;
				semiglobal += subimagesDistance( h1[ o ],h2[ o ] );
				semiglobal += subimagesDistance( h1[ o+o10 ],h2[ o+o10 ] );
				semiglobal += subimagesDistance( h1[ o+o11 ],h2[ o+o11 ] );
				semiglobal += subimagesDistance( h1[ o+o12 ],h2[ o+o12 ] );
				semiglobal += subimagesDistance( h1[ o+o13 ],h2[ o+o13 ] );
		}	}

		// total normalized distance..
		double distance = ( local + global + semiglobal ) / ( len*8 + side*5 );
		return distance;
	}

	/**	Pretty much a {@link HistogramData#distance} method, less secured but faster.
	 *	@param h1 One subimage.
	 *	@param h2 Another subimage.
	 *	@return Distance €[0;1] between the two subimages <tt>h1</tt> and <tt>h2</tt>.
	 */
	private static double subimagesDistance( Data h1, Data h2 ) {

		Double[] b1 = ( Double[] ) h1.getValues();
		Double[] b2 = ( Double[] ) h2.getValues();
		double distance = 0;
		for ( int b = 0 ; b < nbBins ; b++ ) 
			distance += Math.abs( b1[b]-b2[b] );
		return distance;
	}





	  ////////////////////
	 // "EXEC" METHODS //
	////////////////////

	public static DataArrayData exec( Image input ) { 

		return ( DataArrayData ) new EdgeHistogram().process( input );
	}

	public static DataArrayData exec( Image input, double tEdge, double tMonoton ) { 

		return ( DataArrayData ) new EdgeHistogram().process( input,tEdge,tMonoton );
	}

	public static DataArrayData exec( Image input, int size ) { 

		return ( DataArrayData ) new EdgeHistogram().process( input,size );
	}

	public static DataArrayData exec( Image input, double tEdge, double tMonoton, int size ) { 

		return ( DataArrayData ) new EdgeHistogram().process( input,tEdge,tMonoton,size );
	}



}
