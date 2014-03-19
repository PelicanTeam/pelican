package fr.unistra.pelican.algorithms.conversion;

import fr.unistra.pelican.*;
import fr.unistra.pelican.util.Pixel;



public class NonUniformLSHQuantization extends Algorithm { 

	public Image input;
	public Image output;

	public double k = 0.04;
	public int s0 = 50;

	public double m = 0.07;
	public int ll = 64;
	public int lu = 192;

	public NonUniformLSHQuantization() { 

		super.inputs = "input";
		super.options = "k,s0,m,ll,lu";
		super.outputs = "output";
	}

	@Override
	public void launch() throws AlgorithmException { 

		if ( this.input.getBDim() != 3 ) 
			throw new AlgorithmException( "The input must be a tristumulus LSH image" );

		this.output = this.input.copyImage( false );
		this.output.setMask( this.input.getMask() );
		for ( Pixel px : this.input ) { 

			int[] lsh = this.input.getVectorPixelXYZTByte( px.x,px.y,px.z,px.t );
			// L
			if ( lsh[0] < 0.5 ) 
				 lsh[0] = new Double( 1. / ( 1 + Math.exp( -m*( lsh[0]-ll ) ) ) ).intValue();
			else lsh[0] = new Double( 1. / ( 1 + Math.exp(  m*( lsh[0]-lu ) ) ) ).intValue();
			// S
			lsh[1] = new Double( 1. / ( 1 + Math.exp( -k*( lsh[1]-s0 ) ) ) ).intValue();
			// H
			;
			this.output.setVectorPixelXYZTByte( px.x,px.y,px.z,px.t, lsh );
		}
	}

	public static Image exec( Image input ) { 
		return ( Image ) new NonUniformLSHQuantization().process( input );
	}
	public static Image exec( Image input, double k , int s0, double m, int ll, int lu ) { 
		return ( Image ) new NonUniformLSHQuantization().process( input, k,s0,m,ll,lu );
	}



}
