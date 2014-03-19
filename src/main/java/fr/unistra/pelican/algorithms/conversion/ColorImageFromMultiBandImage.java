package fr.unistra.pelican.algorithms.conversion;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.mask.*;

/**
 *	Take a multi band image and built a 3-band image with the user defined bands.
 *
 *	MASK MANAGEMENT (by Régis) : 
 *	input's mask is restricted to the 3 chosen bands to get output's mask.
 *
 * @author
 */

public class ColorImageFromMultiBandImage extends Algorithm {

	/**
	 * First Input parameter.
	 */
	public Image input;

	/**
	 * Output parameter.
	 */
	public Image output;

	/**
	 * Second Input parameter.
	 */
	public int[] choosenBands = new int[3];

	/**
	 * Constructor
	 * 
	 */
	public ColorImageFromMultiBandImage() {

		super();
		super.inputs = "input,choosenBands";
		super.outputs = "output";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		int xdim = input.getXDim();
		int ydim = input.getYDim();
		int zdim = input.getZDim();
		int tdim = input.getTDim();

		output = input.newInstance( xdim, ydim, zdim, tdim, 3 );

		output.setColor(true);

		for ( int x = 0 ; x < xdim ; x++ ) { 
			for ( int y = 0 ; y < ydim ; y++ ) { 
				for ( int z = 0 ; z < zdim ; z++ ) { 
					for ( int t = 0 ; t < tdim ; t++ ) { 
						for ( int b = 0 ; b < 3 ; b++ )
							output.setPixelDouble( x,y,z,t,b, 
									input.getPixelDouble( x,y,z,t, choosenBands[b] ) );
					}
				}
			}
		}

		BooleanImage mask = new BooleanImage( xdim, ydim, zdim, tdim, 3 );
		for ( int x = 0 ; x < xdim ; x++ ) 
			for ( int y = 0 ; y < ydim ; y++ ) 
				for ( int z = 0 ; z < zdim ; z++ ) 
					for ( int t = 0 ; t < tdim ; t++ ) 
						for ( int b = 0 ; b < 3 ; b++ )
							mask.setPixelXYZTBBoolean( x,y,z,t,b, 
								input.isPresentXYZTB( x,y,z,t, choosenBands[b] ) ) ;
		output.pushMask( new BooleanMask( mask ) );
	}

	/**
	 * Take a multi band image and built a 3-band image with the user defined
	 * bands.
	 * 
	 * @param input
	 *            Multi band image.
	 * @param choosenBands  
	 * 				The choosen bands.        
	 * @return 3-band image.
	 */
	public static Image exec(Image input, int[] choosenBands) {
		return (Image) new ColorImageFromMultiBandImage().process(input, choosenBands);
	}
	
	/**
	 * Take a multi band image and built a 3-band image with the user defined
	 * bands.
	 * 
	 * @param input
	 * 			Multi band image.
	 * @param band1
	 * 			First band.
	 * @param band2
	 * 			Second band.
	 * @param band3
	 * 			Third band.
	 * @return 3-band image.
	 */
	public static Image exec(Image input, int band1, int band2, int band3) {
		return (Image) new ColorImageFromMultiBandImage().process(input, new int[]{band1,band2,band3});
	}
	
	
	/**
	 * Take a multi band image and built a 3-band image with bands (0,1,2)
	 * 
	 * @param input
	 *            Multi band image.
	 * @return 3-band image.
	 */
	public static Image exec(Image input) {
		return (Image) new ColorImageFromMultiBandImage().process(input, new int[]{0,1,2});
	}
}