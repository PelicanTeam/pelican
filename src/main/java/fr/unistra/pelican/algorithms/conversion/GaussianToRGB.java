package fr.unistra.pelican.algorithms.conversion;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;

/**	
 *	Conversion from Gaussian opponent colour model.
 *
 *	J.M. Geusebroek, R. Boomgaard, A.W.M. Smeulders, H. Geerts, 
 *	Color invariance, IEEE Transactions on Pattern Analysis and Machine Intelligence 23 (12) (2001)
 *	1338-1350.
 *
 *	http://staff.science.uva.nl/~mark/pub/2009/BurghoutsCVIU09.pdf
 *
 *	@author witz
 */
public class GaussianToRGB  extends Algorithm {

	public Image input;
	public Image output;

	public GaussianToRGB() {

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

		if (bdim != 3)
			throw new AlgorithmException( "The input must be a tristumulus RGB image" );

		output = new ByteImage( xdim,ydim,zdim,tdim,bdim );
		this.output.setMask( this.input.getMask() );
		output.setColor( true );

		for ( int x = 0 ; x < xdim ; x++ ) { 
			for ( int y = 0 ; y < ydim ; y++) { 
				for ( int z = 0 ; z < zdim ; z++) { 
					for ( int t = 0 ; t < tdim ; t++) { 

						double  I = input.getPixelXYZTBDouble( x,y,z,t, 0 );
						double YB = input.getPixelXYZTBDouble( x,y,z,t, 1 );
						double RG = input.getPixelXYZTBDouble( x,y,z,t, 2 );

//						double R = I + 4.0*YB/3.0 + 4.0*RG/3.0 ;
//						double G = I - 4.0*RG/3.0 ;
//						double B = I - 4.0*YB/3.0 ;

						int R = (int)( I*1.184397659182579 + YB*1.568510876407638 + RG*1.3481849339022172 );
						int G = (int)( I*0.9908838684106221 + YB*0.47562425683709875 - RG*0.5945303210463734 );
						int B = (int)( I*1.1284418642605674 - YB*1.4583479051549277 + RG*1.0876407637966006 );

						output.setPixelXYZTBByte( x,y,z,t, 0, R );
						output.setPixelXYZTBByte( x,y,z,t, 1, G );
						output.setPixelXYZTBByte( x,y,z,t, 2, B );
					}
				}
			}
		}
	}

	public static Image exec(Image input) {
		return (Image) new GaussianToRGB().process(input);
	}
}
