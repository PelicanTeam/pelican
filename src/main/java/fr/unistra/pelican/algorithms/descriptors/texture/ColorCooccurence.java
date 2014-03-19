package fr.unistra.pelican.algorithms.descriptors.texture;

import fr.unistra.pelican.*;
import fr.unistra.pelican.algorithms.conversion.RGBToGray;
import fr.unistra.pelican.algorithms.statistics.Cooccurence;
import fr.unistra.pelican.util.Pixel;
import fr.unistra.pelican.util.data.MatrixData;


/**	
 *	Color co-occurence matrix (CCM).
 *
 *	Chuen-Horng Lina, Rong-Tai Chena, and Yung-Kuan Chanb,
 *	"A smart content-based image retrieval system based on color and texture feature"
 *
 *	( Get it there : <url>http://dx.doi.org/10.1016/j.imavis.2008.07.004</url> [part 2.1] )
 *
 *	This descriptor describes the direction of textures but not the complexity of textures. 
 *	In short terms :
 *
 *	<ol>
 *	<li>
 *	Each pixel is convoluted with a 3x3 mask (a) :
 *	<br>	1 2 3
 *	<br>	4 p 5
 *	<br>	6 7 8
 *	<p>
 *	(a) being itself subdivided in four masks :
 *
 *		<ol>
 *		<li type="c"> 
 *		an upper left one (c) :
 *		<br>	1 2
 *		<br>	4 p
 *
 *		<li> 
 *		an upper right one (d) : 
 *		<br>	2 3
 *		<br>	p 5
 *
 *		<li> 
 *		a bottom left one (e) :
 *		<br>	4 p
 *		<br>	6 7
 *
 *		<li> 
 *		and a bottom right one (f) :
 *		<br>	p 5
 *		<br>	7 8
 *
 *		</ol>
 *
 *	<li>
 *	For each mask convolution, we search for motifs of scan pattern 
 *	which would traverse the grid in an optimal sense. We get the code 
 *	associated with the motif found ( see {@link #scanf} ).
 *
 *	<li>
 *	We set this code in the appropriate element of a NxMx4 matrix.
 *
 *	<li>
 *	We compute the co-occurence matrix of the NxMx4 matrix.
 *	The resulting co-occurence matrix is thus our feature, with is a 7x7 matrix.
 *	done.
 *
 *	</ol>
 *
 *	<p>
 *	TODO: Image traversal should be widely optimized for strong efficience improvement.
 *
 *
 *	@author Régis Witz
 *	@date 07.04.2009
 */
public class ColorCooccurence extends Descriptor { 

	/**	Input image. */
	public Image input;
	/**	Adjacence. */
	public Pixel adjacent = new Pixel( 1,0,0,0,0 );
	/**	Output feature. */
	public MatrixData output;

	/**	Algorithm specifications. */
	public ColorCooccurence() {

		super();
		super.inputs = "input";
		super.options = "adjacent";
		super.outputs = "output";
	}



	/**	Convolution masks names. */
	private char[] cmasks = { 'c','d','e','f' };
	/**	P matrix. */
	private ByteImage P;

	@SuppressWarnings("unchecked")
	@Override
	/**	@see Algorithm */
	public void launch() throws AlgorithmException {

		if ( this.input.getBDim() > 1 ) this.input = RGBToGray.exec( this.input );

		this.P = new ByteImage(	this.input.getXDim(), 
								this.input.getYDim(), 
								this.input.getZDim(), 
								this.input.getTDim(), 
								4 );

		System.out.println( "begin: " + this.input.size() );
		for( Pixel p : this.input ) { 

			for ( int i = 0 ; i < 4 ; i++ ) { 

				int m = this.scan( p, this.cmasks[i] );
				this.P.setPixelXYZTBByte( p.x,p.y,p.z,p.t,i, m );
			}
		}

		IntegerImage matrix = Cooccurence.exec( this.P,this.adjacent,true,7 );

		this.output = new MatrixData();
		this.output.setDescriptor( ( Class ) ColorCooccurence.class );
		this.output.setValues( matrix );

		System.out.println( "CCOOC done : " + matrix.size() );
		
	}



	private int scan( Pixel p, char cmask ) { 

		double p2,p3,p4;
		int m;

		switch ( cmask ) {

			// doing : pi = this.input.getPixelXYZTDouble( p.x+dx,p.y+dx,p.z,p.t );
			// can throw an ArrayIndexOutOfBoundsException when pi is a "beyond borders" pixel.
			// so we take care of border pixels by setting them to 0 by default.
			//
			// TODO: this is maybe not the best thing to do : setting a border pixel to 0
			// makes that the scanx() method automatically return 0, for we have 2 pixel 
			// that are equal ... 
			// should the scanx() methods return 0 in the sole case when ALL 4 pixels are equal ? 
			// then how should cases of configurations with only 2 or 3 pixels equal be resolved ?

			case 'c': // upper left convolution mask
//				try { p1 = this.input.getPixelXYZTDouble( p.x-1,p.y-1,p.z,p.t ); }
//				catch ( ArrayIndexOutOfBoundsException ex ) { p1 = 0; }
				try { p2 = this.input.getPixelXYZTDouble( p.x,p.y-1,p.z,p.t ); }
				catch ( ArrayIndexOutOfBoundsException ex ) { p2 = 0; }
				try { p3 = this.input.getPixelXYZTDouble( p.x-1,p.y,p.z,p.t ); }
				catch ( ArrayIndexOutOfBoundsException ex ) { p3 = 0; }
				p4 = this.input.getPixelXYZTDouble( p.x,p.y,p.z,p.t );
//				m = scanc( p1,p2,p3,0 );
				break;

			case 'd': // upper right convolution mask
//				try { p1 = this.input.getPixelXYZTDouble( p.x,p.y-1,p.z,p.t ); }
//				catch ( ArrayIndexOutOfBoundsException ex ) { p1 = 0; }
				try { p2 = this.input.getPixelXYZTDouble( p.x+1,p.y-1,p.z,p.t ); }
				catch ( ArrayIndexOutOfBoundsException ex ) { p2 = 0; }
				p3 = this.input.getPixelXYZTDouble( p.x,p.y,p.z,p.t );
				try { p4 = this.input.getPixelXYZTDouble( p.x+1,p.y,p.z,p.t ); }
				catch ( ArrayIndexOutOfBoundsException ex ) { p4 = 0; }
//				m = scand( p1,p2,0,p4 );
				break;

			case 'e': // bottom left convolution mask
//				try { p1 = this.input.getPixelXYZTDouble( p.x-1,p.y,p.z,p.t ); }
//				catch ( ArrayIndexOutOfBoundsException ex ) { p1 = 0; }
				p2 = this.input.getPixelXYZTDouble( p.x,p.y,p.z,p.t );
				try { p3 = this.input.getPixelXYZTDouble( p.x-1,p.y+1,p.z,p.t ); }
				catch ( ArrayIndexOutOfBoundsException ex ) { p3 = 0; }
				try { p4 = this.input.getPixelXYZTDouble( p.x,p.y+1,p.z,p.t ); }
				catch ( ArrayIndexOutOfBoundsException ex ) { p4 = 0; }
//				m = scane( p1,0,p3,p4 );
				break;

			case 'f': // bottom right convolution mask
//				p1 = this.input.getPixelXYZTDouble( p.x,p.y,p.z,p.t );
				try { p2 = this.input.getPixelXYZTDouble( p.x+1,p.y,p.z,p.t ); }
				catch ( ArrayIndexOutOfBoundsException ex ) { p2 = 0; }
				try { p3 = this.input.getPixelXYZTDouble( p.x,p.y+1,p.z,p.t ); }
				catch ( ArrayIndexOutOfBoundsException ex ) { p3 = 0; }
				try { p4 = this.input.getPixelXYZTDouble( p.x+1,p.y+1,p.z,p.t ); }
				catch ( ArrayIndexOutOfBoundsException ex ) { p4 = 0; }
				break;

			default : p2 = p3 = p4 = 0;
		}
		m = scanf( 0,p2,p3,p4 );
		return m;
	}

	/**	Scans the traversal of a (c) 2x2 convolution mask. 
	 *	<br> 
	 *	The 7 scannings for (c) :
	 *	<ol>
	 *		<li> The situation wherein the motif cannot be formed due to the equivalence.
	 *		<li> p4,p3,p2,p1
	 *		<li> p4,p2,p3,p1
	 *		<li> p4,p3,p1,p2
	 *		<li> p4,p2,p1,p3
	 *		<li> p4,p1,p2,p3
	 *		<li> p4,p1,p3,p2
	 *	</ol>
	 *
	 *	@param p1 Upper left pixel.
	 *	@param p2 Upper right pixel.
	 *	@param p3 Bottom left pixel.
	 *	@param p4 Bottom right pixel. 
	 *			  This one is the starting point of the traversal and must be set to <tt>0</tt>.
	 *	@return m€[0;6], m€|N.
	 */
	int scanc( double p1, double p2, double p3, double p4 ) { 

		if ( p3 < p2 && p2 < p1 ) return 1;
		if ( p2 < p3 && p3 < p1 ) return 2;
		if ( p3 < p1 && p1 < p2 ) return 3;
		if ( p2 < p1 && p1 < p3 ) return 4;
		if ( p1 < p2 && p2 < p3 ) return 5;
		if ( p1 < p3 && p3 < p4 ) return 6;
		return 0;
	}

	/**	Scans the traversal of a (d) 2x2 convolution mask. 
	 *	<br>
	 *	The 7 scannings for (d) :
	 *	<ol>
	 *		<li> The situation wherein the motif cannot be formed due to the equivalence.
	 *		<li> p3,p4,p1,p2
	 *		<li> p3,p1,p4,p3
	 *		<li> p3,p4,p2,p1
	 *		<li> p3,p1,p2,p4
	 *		<li> p3,p2,p1,p4
	 *		<li> p3,p2,p4,p1
	 *	</ol>
	 *
	 *	@param p1 Upper left pixel.
	 *	@param p2 Upper right pixel.
	 *	@param p3 Bottom left pixel.
	 *			  This one is the starting point of the traversal and must be set to <tt>0</tt>.
	 *	@param p4 Bottom right pixel.
	 *	@return m€[0;6], m€|N.
	 */
	int scand( double p1, double p2, double p3, double p4 ) { 

		if ( p4 < p1 && p1 < p2 ) return 1;
		if ( p1 < p4 && p4 < p2 ) return 2;
		if ( p4 < p2 && p2 < p1 ) return 3;
		if ( p1 < p2 && p2 < p4 ) return 4;
		if ( p2 < p1 && p1 < p4 ) return 5;
		if ( p2 < p4 && p4 < p1 ) return 6;
		return 0;
	}

	/**	Scans the traversal of a (e) 2x2 convolution mask. 
	 *	<br>
	 *	The 7 scannings for (e) :
	 *	<ol>
	 *		<li> The situation wherein the motif cannot be formed due to the equivalence.
	 *		<li> p2,p1,p4,p3
	 *		<li> p2,p4,p1,p3
	 *		<li> p2,p1,p3,p4
	 *		<li> p2,p4,p3,p1
	 *		<li> p2,p3,p1,p4
	 *		<li> p2,p3,p4,p1
	 *	</ol>
	 *
	 *	@param p1 Upper left pixel.
	 *	@param p2 Upper right pixel.
	 *			  This one is the starting point of the traversal and must be set to <tt>0</tt>.
	 *	@param p3 Bottom left pixel.
	 *	@param p4 Bottom right pixel.
	 *	@return m€[0;6], m€|N.
	 */
	int scane( double p1, double p2, double p3, double p4 ) { 

		if ( p1 < p4 && p4 < p3 ) return 1;
		if ( p4 < p1 && p1 < p3 ) return 2;
		if ( p1 < p3 && p3 < p4 ) return 3;
		if ( p4 < p3 && p3 < p1 ) return 4;
		if ( p3 < p1 && p1 < p4 ) return 5;
		if ( p3 < p4 && p4 < p1 ) return 6;
		return 0;
	}

	/**	Scans the traversal of a (f) 2x2 convolution mask of the form :
	 * 	<p>
	 * 	<code>
	 *	p1 p2<br> 
	 *	p3 p4<br>
	 *	</code>
	 *	<p>
	 *	The 7 scannings for (f) :
	 *	<ol>
	 *		<li> The situation wherein the motif cannot be formed due to the equivalence.
	 *		<li> p1,p2,p3,p4
	 *		<li> p1,p3,p2,p4
	 *		<li> p1,p3,p4,p2
	 *		<li> p1,p2,p4,p3
	 *		<li> p1,p4,p3,p2
	 *		<li> p1,p4,p2,p3
	 *	</ol>
	 *
	 *	@param p1 Upper left pixel.
	 *			  This one is the starting point of the traversal and must be set to <tt>0</tt>.
	 *	@param p2 Upper right pixel.
	 *	@param p3 Bottom left pixel.
	 *	@param p4 Bottom right pixel.
	 *	@return m€[0;6], m€|N.
	 */
	int scanf( double p1, double p2, double p3, double p4 ) { 

		if ( p2 < p3 && p3 < p4 ) return 1;
		if ( p3 < p2 && p2 < p4 ) return 2;
		if ( p3 < p2 && p4 < p3 ) return 3;
		if ( p2 < p4 && p4 < p3 ) return 4;
		if ( p4 < p3 && p3 < p2 ) return 5;
		if ( p4 < p2 && p2 < p3 ) return 6;
		return 0;
	}





	  //////////////////
	 // EXEC METHODS //
	//////////////////

	public static MatrixData exec( Image input ) {

		return ( MatrixData ) new ColorCooccurence().process( input );
	}

	public static MatrixData exec( Image input, Pixel adjacent ) {

		return ( MatrixData ) new ColorCooccurence().process( input,adjacent );
	}

	  //////////////////
	 // EXEC METHODS //
	//////////////////

	public static void main( String[] args ) {

		String path1 = "/home/miv/witz/bases/wang_1000/722.jpg";
		String path2 = "/home/miv/witz/bases/wang_1000/223.jpg";
		Image image1 = fr.unistra.pelican.algorithms.io.ImageLoader.exec( path1 );
		Image image2 = fr.unistra.pelican.algorithms.io.ImageLoader.exec( path2 );
		System.out.println( "..." );
		fr.unistra.pelican.util.data.Data data1 = ColorCooccurence.exec( image1 );
		System.out.println( "..." );
		fr.unistra.pelican.util.data.Data data2 = ColorCooccurence.exec( image2 );
		System.out.println( "..." );

		data1.distance( data2 );
		System.out.println( "..." );
		data2.distance( data1 );
		System.out.println( "..." );
	}


}
