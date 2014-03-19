package fr.unistra.pelican.algorithms.conversion;

import fr.unistra.pelican.*;

/**
 *	Transforms a quadristumulus RGBA image into a graylevel image using the formula : 
 *		<tt>gray = 0.299 x R + 0.587 x G + 0.114 x B</tt>
 *	<p>
 *	MASK MANAGEMENT : 
 *	<ul>
 *	<li> input's mask becomes output's mask.
 *	<li> no modification on color calculation.
 *	</ul>
 * 
 *	@author Régis Witz
 */
public class RGBAToGray extends Algorithm {

	/** Input RGBA image. */
	public Image input;

	/**	Output gray image. */
	public Image output;



	public RGBAToGray() { 

		super.inputs = "input";
		super.outputs = "output";
		
	}

	public void launch() throws AlgorithmException { 

		int xdim = this.input.getXDim();
		int ydim = this.input.getYDim();
		int zdim = this.input.getZDim();
		int tdim = this.input.getTDim();
		int bdim = this.input.getBDim();

		if ( bdim != 4 && bdim != 3 ) 
			throw new AlgorithmException( "The input must be a tristumulus RGB image" );

		this.output = new ByteImage( xdim,ydim,zdim,tdim, 1 );
		this.output.setMask( this.input.getMask() );
		this.output.setColor( false );

		for ( int x = 0 ; x < xdim ; x++ ) 
		for ( int y = 0 ; y < ydim ; y++ ) 
		for ( int z = 0 ; z < zdim ; z++ ) 
		for ( int t = 0 ; t < tdim ; t++ ) { 

			double R = this.input.getPixelXYZTBDouble( x,y,z,t,0 );
			double G = this.input.getPixelXYZTBDouble( x,y,z,t,1 );
			double B = this.input.getPixelXYZTBDouble( x,y,z,t,2 );
			double gray = 0.299 * R + 0.587 * G + 0.114 * B;
			output.setPixelXYZTDouble( x,y,z,t, gray );
		}
	}

	public static Image exec( Image input ) { 
		return ( Image ) new RGBAToGray().process( input );
	}



}
