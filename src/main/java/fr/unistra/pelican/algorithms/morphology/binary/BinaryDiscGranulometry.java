package fr.unistra.pelican.algorithms.morphology.binary;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 * Performs a binary granulometry with a disc shaped flat structuring element.
 * 
 * @author S.L.
 */

public class BinaryDiscGranulometry extends Algorithm {
	
	/**
	 * Image to be processed
	 */
	public Image input;

	/**
	 * Size of the square structuring element
	 */
	public Integer length;

	/**
	 * Indicates if structuring element is convex or not
	 */
	public Boolean flag;
	
	/**
	 * Resulting granulometry
	 */
	public Double[] output;

	/**
	 * Constructor
	 * 
	 */
	public BinaryDiscGranulometry() {
		super.inputs = "input,length,flag";
		super.outputs = "output";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		int size = length * input.getBDim(); // size of SEs increases in
												// steps of 2
		output = new Double[size];

		int MOMENTX = 0;
		int MOMENTY = 0;

		// every size
		for (int i = 0; i < length; i++) {
			int side=i;
			BooleanImage disc = FlatStructuringElement2D
					.createCircleFlatStructuringElement(side);

			// schnell Hans, schnell!!!
			Image tmp = BinaryOpening.exec(input, disc);

			for ( int b = 0 ; b < input.getBDim() ; b++ ) { 

				double d = 0.0;
				double original = input.getNumberOfPresentPixel( b );
				if ( original != 0 ) d = moment( tmp,b,MOMENTX,MOMENTY,side);
				output[b * length + i] = d /
				 	( ( Math.pow(input.getXDim(),MOMENTX+1)*Math.pow(input.getYDim(),MOMENTY+1) ) / 
						Math.pow(Math.sqrt(original), (double)MOMENTX/2 + (double)MOMENTY/2 + 1 ) );

			}
		}
	}

	private double moment(Image img, int channel, int i, int j, int radius) { 

		double d = 0.0;
		for (int x = radius; x < img.getXDim() - radius; x++) {
			for (int y = radius; y < img.getYDim() - radius; y++) {

				if ( img.isPresentXYB( x,y, channel ) )
					d += ( img.getPixelXYBBoolean( x,y, channel ) ) ? 1 : 0;
			}
		}

		return d;
	}


	/**
	 * This method performs a binary granulometry with a disc shaped flat structuring
	 * element.
	 * @param image image to be processed
	 * @param length size of the disc struturing element
	 * @return granulometry array
	 */
	public static Double[] exec(Image input, Integer length,Boolean flag) {
		return (Double[]) new BinaryDiscGranulometry().process(input,length,flag);
	}

}
