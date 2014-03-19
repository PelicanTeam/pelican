package fr.unistra.pelican.algorithms.descriptors.texture;

import fr.unistra.pelican.*;
import fr.unistra.pelican.algorithms.conversion.RGBToGray;
import fr.unistra.pelican.util.Pixel;
import fr.unistra.pelican.util.Tools;
import fr.unistra.pelican.util.data.DoubleArrayData;



/**	
 *	Difference between pixels of scan pattern (DBPSP).
 *
 *	Chuen-Horng Lina, Rong-Tai Chena, and Yung-Kuan Chanb,
 *	"A smart content-based image retrieval system based on color and texture feature"
 *
 *	( Get it there : <url>http://dx.doi.org/10.1016/j.imavis.2008.07.004</url> [part 2.2] )
 *
 *	@author Régis Witz
 */
public class DifferenceScanPattern extends Descriptor { 

	/**	Input image. */
	public Image input;
	/**	Output feature. */
	public DoubleArrayData output;

	/**	Algorithm specifications. */
	public DifferenceScanPattern() { 

		super();
		super.inputs = "input";
		super.outputs = "output";
	}



	/**	Convolution masks names. */
	private char[] cmasks = { 'c','d','e','f' };
	/**	. */
	private Double[] values;

	@SuppressWarnings("unchecked")
	@Override
	/**	@see Algorithm */
	public void launch() throws AlgorithmException {

		if ( this.input.getBDim() > 1 ) this.input = RGBToGray.exec( this.input );

		this.values = new Double[6];
		for ( int i = 0 ; i < 6 ; i++ ) this.values[i] = new Double( 0. );

		for( Pixel p : this.input ) 
			for ( Character c : this.cmasks ) 
				this.scan( p,c );

		// Is this the right way to normalize ? I seems so, but..
		this.values = Tools.vectorNormalize( this.values );

		this.output = new DoubleArrayData();
		this.output.setDescriptor( ( Class ) DifferenceScanPattern.class );
		this.output.setValues( this.values );
		
	}



	private void scan( Pixel p, char cmask ) { 

		double p1,p2,p3,p4;

		switch ( cmask ) {

			// doing : pi = this.input.getPixelXYZTDouble( p.x+dx,p.y+dx,p.z,p.t );
			// can throw an ArrayIndexOutOfBoundsException when pi is a "beyond borders" pixel.
			// so we take care of border pixels by setting them to the same values as 
			// their nearest "inside image" 4-neighbour.

			case 'c': // upper left convolution mask
				p4 = this.input.getPixelXYZTDouble( p.x,p.y,p.z,p.t );
				try { p3 = this.input.getPixelXYZTDouble( p.x-1,p.y,p.z,p.t ); }
				catch ( ArrayIndexOutOfBoundsException ex ) { p3 = p4; }
				try { p1 = this.input.getPixelXYZTDouble( p.x-1,p.y-1,p.z,p.t ); }
				catch ( ArrayIndexOutOfBoundsException ex ) { p1 = p3; }
				try { p2 = this.input.getPixelXYZTDouble( p.x,p.y-1,p.z,p.t ); }
				catch ( ArrayIndexOutOfBoundsException ex ) { p2 = p4; }
				break;

			case 'd': // upper right convolution mask
				p3 = this.input.getPixelXYZTDouble( p.x,p.y,p.z,p.t );
				try { p4 = this.input.getPixelXYZTDouble( p.x+1,p.y,p.z,p.t ); }
				catch ( ArrayIndexOutOfBoundsException ex ) { p4 = p3; }
				try { p1 = this.input.getPixelXYZTDouble( p.x,p.y-1,p.z,p.t ); }
				catch ( ArrayIndexOutOfBoundsException ex ) { p1 = p3; }
				try { p2 = this.input.getPixelXYZTDouble( p.x+1,p.y-1,p.z,p.t ); }
				catch ( ArrayIndexOutOfBoundsException ex ) { p2 = p4; }
				break;

			case 'e': // bottom left convolution mask
				p2 = this.input.getPixelXYZTDouble( p.x,p.y,p.z,p.t );
				try { p1 = this.input.getPixelXYZTDouble( p.x-1,p.y,p.z,p.t ); }
				catch ( ArrayIndexOutOfBoundsException ex ) { p1 = p2; }
				try { p3 = this.input.getPixelXYZTDouble( p.x-1,p.y+1,p.z,p.t ); }
				catch ( ArrayIndexOutOfBoundsException ex ) { p3 = p1; }
				try { p4 = this.input.getPixelXYZTDouble( p.x,p.y+1,p.z,p.t ); }
				catch ( ArrayIndexOutOfBoundsException ex ) { p4 = p2; }
				break;

			case 'f': // bottom right convolution mask
				p1 = this.input.getPixelXYZTDouble( p.x,p.y,p.z,p.t );
				try { p2 = this.input.getPixelXYZTDouble( p.x+1,p.y,p.z,p.t ); }
				catch ( ArrayIndexOutOfBoundsException ex ) { p2 = p1; }
				try { p3 = this.input.getPixelXYZTDouble( p.x,p.y+1,p.z,p.t ); }
				catch ( ArrayIndexOutOfBoundsException ex ) { p3 = p1; }
				try { p4 = this.input.getPixelXYZTDouble( p.x+1,p.y+1,p.z,p.t ); }
				catch ( ArrayIndexOutOfBoundsException ex ) { p4 = p2; }
				break;

			default : p1 = p2 = p3 = p4 = 0;
		}



		for ( int i = 1 ; i <= 6 ; i++ ) 
			this.values[i-1] += this.delta( i, p1,p2,p3,p4 );

	}



	/**	Records the pixel value differences among all scan directions within motifs of scan pattern.
	 *	@param i Scan pattern id.
	 *	@param p1 Upper left pixel.
	 *	@param p2 Upper right pixel.
	 *	@param p3 Bottom left pixel.
	 *	@param p4 Bottom right pixel.
	 *	@return Total pixel value difference of any coordinates (x, y) within the image.
	 */
	private double delta( int i, double p1, double p2, double p3, double p4 ) {

		switch ( i ) { 

			case 1 : return Math.abs( p1-p2 ) + Math.abs( p2-p3 ) + Math.abs( p3-p4 );
			case 2 : return Math.abs( p1-p3 ) + Math.abs( p3-p2 ) + Math.abs( p2-p4 );
			case 3 : return Math.abs( p1-p3 ) + Math.abs( p3-p4 ) + Math.abs( p4-p2 );
			case 4 : return Math.abs( p1-p2 ) + Math.abs( p2-p4 ) + Math.abs( p4-p3 );
			case 5 : return Math.abs( p1-p4 ) + Math.abs( p4-p3 ) + Math.abs( p3-p2 );
			case 6 : return Math.abs( p1-p4 ) + Math.abs( p4-p2 ) + Math.abs( p2-p3 );
			default : return -1.;
		}
	}





	  /////////////////
	 // EXEC METHOD //
	/////////////////

	public static DoubleArrayData exec( Image input ) {

		return ( DoubleArrayData ) new DifferenceScanPattern().process( input );
	}



}
