package fr.unistra.pelican.algorithms.conversion;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;

/**
 * Transforms a gray image into a RGB colour image by tripling the channels
 *
 *	MASK MANAGEMENT (by Régis) : 
 *	- input's mask becomes output's mask.
 *	- no modification on color calculation.
 *
 * 
 * @author Erchan Aptoula
 * 
 */

public class GrayToRGB extends Algorithm {
	
	/**
	 * Input parameter.
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
	public GrayToRGB() {
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

		if (bdim != 1)
			throw new AlgorithmException("The input must be a grayscale image");

		output = new ByteImage(xdim, ydim, zdim, tdim, 3);
		this.output.setMask( this.input.getMask() );
		output.setColor(true);

		for(int t=0;t<tdim;t++)
			for(int z=0;z<zdim;z++)
				for (int y = 0; y < ydim; y++)
					for (int x = 0; x < xdim; x++) 
					{

						double p = input.getPixelXYZTDouble(x, y,z,t);
						output.setPixelXYZTBDouble(x, y, z, t, 0, p);
						output.setPixelXYZTBDouble(x, y, z, t, 1, p);
						output.setPixelXYZTBDouble(x, y, z, t, 2, p);
					}
		
	}
	
	/**
	 * Transforms a gray image into a RGB colour image by tripling the channels 
	 * 
	 * @param input  Gray image.
	 * @return RGB colour image.
	 */
	public static Image exec (Image input) {
		return (Image) new GrayToRGB().process(input);
	}
	
}