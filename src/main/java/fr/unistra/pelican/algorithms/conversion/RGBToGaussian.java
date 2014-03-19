package fr.unistra.pelican.algorithms.conversion;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;



/**	
 *	Conversion to Gaussian opponent colour model.
 *
 *	J.M. Geusebroek, R. Boomgaard, A.W.M. Smeulders, H. Geerts, 
 *	Color invariance, IEEE Transactions on Pattern Analysis and Machine Intelligence 23 (12) (2001)
 *	1338-1350.
 *
 *	http://staff.science.uva.nl/~mark/pub/2009/BurghoutsCVIU09.pdf
 *
 *	@author witz
 */
public class RGBToGaussian extends Algorithm {

	/**
	 * Input parameter
	 */
	public Image input;

	/**
	 * Output parameter
	 */
	public Image output;

	/**
	 * Constructor
	 * 
	 */
	public RGBToGaussian() {

		super();
		super.inputs = "input";
		super.outputs = "output";
		
	}

	public void launch() throws AlgorithmException {

		int xdim = input.getXDim();
		int ydim = input.getYDim();
		int zdim = input.getZDim();
		int tdim = input.getTDim();
		int bdim = input.getBDim();

		if (bdim != 3) throw new AlgorithmException( "The input must be a tristumulus RGB image" );

		output = new DoubleImage( xdim,ydim,zdim,tdim,bdim );
		this.output.setMask( this.input.getMask() );
		output.setColor( true );

		for ( int x = 0 ; x < xdim ; x++ ) { 
			for ( int y = 0 ; y < ydim ; y++) { 
				for ( int z = 0 ; z < zdim ; z++) { 
					for ( int t = 0 ; t < tdim ; t++) { 

						int R = input.getPixelXYZTBByte( x,y,z,t, 0 );
						int G = input.getPixelXYZTBByte( x,y,z,t, 1 );
						int B = input.getPixelXYZTBByte( x,y,z,t, 2 );

//						double I = ( R+G+B ) / ( 3.0*255.0 );
//						double YB = ( R+G-2.0*B ) / ( 4.0*255.0 );
//						double RG = ( R-2.0*G+B ) / ( 4.0*255.0 );

						double  I = 0.06*R + 0.63*G + 0.27*B;
						double YB = 0.30*R + 0.04*G - 0.35*B;
						double RG = 0.34*R - 0.60*G + 0.17*B;

						output.setPixelXYZTBDouble( x,y,z,t, 0,  I );
						output.setPixelXYZTBDouble( x,y,z,t, 1, YB );
						output.setPixelXYZTBDouble( x,y,z,t, 2, RG );
					}
				}
			}
		}
	}

	/**
	 * This class realizes the transformation of a tristumulus RGB image into a
	 * double valued I1I2I3 image.
	 * 
	 * @param input
	 *            Tristumulus RGB image.
	 * @return A double valued I1I2I3 image.
	 */
	public static Image exec(Image input) {
		return (Image) new RGBToGaussian().process(input);
	}
}
