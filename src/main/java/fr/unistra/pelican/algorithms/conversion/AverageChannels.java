package fr.unistra.pelican.algorithms.conversion;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;

/**
 *	This class realizes the averaging of all bands of a multiband image into a graylevel image
 *
 *	MASK MANAGEMENT (by Régis) : absent pixels don't count in the mean calculation (val).
 * 
 * @author Sebastien Lefevre.
 * 
 */

public class AverageChannels extends Algorithm {

	/**
	 * Input parameter
	 */
	public Image input;

	/**
	 * Output parameter.
	 */
	public Image output;

	/**
	 * Constructor
	 * 
	 */
	public AverageChannels() {

		super();
		super.inputs = "input";
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
		int bdim = input.getBDim();

		if ( bdim < 1 ) throw new AlgorithmException("The input must be a multiband image");

		if(bdim==1)
		{
			output=input.copyImage(true);
		}
		else
		{
			output = input.newInstance(xdim, ydim, zdim, tdim, 1);
			output.setColor(false);

			double val;
			int nbPresentBands;
			for ( int t = 0; t < tdim; t++ )
				for ( int z = 0; z < zdim; z++ )
					for ( int y = 0; y < ydim; y++ )
						for ( int x = 0; x < xdim; x++ )				
						{
							val = 0;
							nbPresentBands = 0;
							for ( int b = 0; b < bdim; b++ ) 
								if ( input.isPresent( x,y,z,t,b ) ) { 
									val += input.getPixelXYZTBDouble( x,y,z,t,b );
									nbPresentBands++;
								}
							if ( nbPresentBands == 0 ) output.setPixelXYZTBDouble( x,y,z,t,0, val );
							else output.setPixelXYZTBDouble(x, y, z, t, 0, val / nbPresentBands );
						}
		}
	}

	/**
	 * Realizes the averaging of all bands of a multiband image into
	 * a graylevel image
	 * 
	 * @param image
	 *            The multiband image
	 * @return The graylevel image.
	 */
	public static <T extends Image> T exec(T input) {
		return (T) new AverageChannels().process(input);
	}

}