package fr.unistra.pelican.algorithms.statistics;

import fr.unistra.pelican.*;
import fr.unistra.pelican.util.Pixel;



/**	
 *	Computes the co-occurrence matrix of an image.
 *	Currently only work for images whose pixels are only positive integers.
 *
 *	@author Régis Witz
 *	@date 07.04.2009
 */
public class Cooccurence extends Algorithm {



	  ////////////
	 // FIELDS //
	////////////

	/** Input image. */
	public ByteImage input;

	/** Deviation vector. */
	public Pixel t = new Pixel();

	/**	Side of {@link #output} matrix.
	 *	<br>
	 *	If not given by the user, it will be computed automatically, 
	 *	but this will cost additionnal time ( <i>ie.</i> one pass ). 
	 *	<br>
	 *	No verification is done on an user-given value.
	 */
	public int side = -1;

	/**	Should {@link #output} be normalized ? */
	public boolean normalize = true;

	/**	Output co-occurence matrix. It's a {@link #side}x{@link #side} square matrix. */
	public IntegerImage output;



	  /////////////
	 // PROFILE //
	/////////////

	/**	Algorithm specifications. */
	public Cooccurence() { 

		super();
		super.inputs = "input";
		super.options = "t,normalize,side";
		super.outputs = "output";
	}



	  ///////////////////
	 // LAUNCH METHOD //
	///////////////////

	@Override
	/**	@see Algorithm */
	public void launch() throws AlgorithmException {

//		// {@link #input} must be an instance of one of these class. for example, 
//		// there is no sense to compute a co-occurence matrix on a DoubleImage ...
//		if ( !(	this.input instanceof BooleanImage
//			 || this.input instanceof ByteImage
//			 || this.input instanceof IntegerImage ) ) 
//			throw new PelicanException( "Cannot compute co-occurence matrix " +
//										"on a " + this.input.getClass().getName() + " image." );
//
		if ( side < 0 ) { 
//		if ( this.input instanceof BooleanImage ) this.side = 2;
		if ( this.input instanceof ByteImage ) 	  this.side = 256;
//		if ( this.input instanceof IntegerImage ) this.side = Integer.MAX_VALUE-Integer.MIN_VALUE+1;
		}

		// create the co-occurence matrix
		this.output = new IntegerImage( this.side,this.side,1,1,1 );
		for ( Pixel p : this.output ) this.output.setPixelInt( p,0 );

		// compute the co-occurence matrix
		int p1,p2, sum = 0;
		Pixel pt = null;
		for ( Pixel p : this.input ) { 

			pt = new Pixel(   p.x + this.t.x, 
							  p.y + this.t.y, 
							  p.z + this.t.z, 
							  p.t + this.t.t, 
							  p.b + this.t.b );

			try { p2 = this.input.getPixelByte( pt ); }
			catch ( ArrayIndexOutOfBoundsException ex ) { continue; }

			p1 = this.input.getPixelByte( p );
//			if ( p1 != 0 ) System.out.println( p1 );
//			if ( p2 != 0 ) System.out.println( p2 );
			
			this.output.setPixelXYInt( p1,p2, this.output.getPixelXYInt( p1,p2 )+1 );
			sum++;
		}
		pt = null;
System.out.println( "/cooc: " + sum / 4. );
		// normalize the co-occurence matrix
		if ( sum > 0 )
			for ( Pixel p : this.output ) 
				this.output.setPixelInt( p, this.output.getPixelInt( p ) / sum );

	} // endfunc



	  //////////////////
	 // EXEC METHODS //
	//////////////////

	public static IntegerImage exec( Image input ) {

		return ( IntegerImage ) new Cooccurence().process( input );
	}

	public static IntegerImage exec( Image input, boolean normalize ) {

		return ( IntegerImage ) new Cooccurence().process( input,normalize );
	}

	public static IntegerImage exec( Image input, int size ) {

		return ( IntegerImage ) new Cooccurence().process( input,size );
	}

	public static IntegerImage exec( Image input, boolean normalize, int size ) {

		return ( IntegerImage ) new Cooccurence().process( input,normalize,size );
	}

	public static IntegerImage exec( Image input, Pixel t ) {

		return ( IntegerImage ) new Cooccurence().process( input,t );
	}

	public static IntegerImage exec( Image input, Pixel t, boolean normalize ) {

		return ( IntegerImage ) new Cooccurence().process( input,t,normalize );
	}

	public static IntegerImage exec( Image input, Pixel t, int size ) {

		return ( IntegerImage ) new Cooccurence().process( input,t,size );
	}

	public static IntegerImage exec( Image input, Pixel t, boolean normalize, int size ) {

		return ( IntegerImage ) new Cooccurence().process( input,t,normalize,size );
	}



}
