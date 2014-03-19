package fr.unistra.pelican.algorithms.conversion;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;

/**
 * This class realizes the transformation of a tristumulus RGB image into a
 * double valued CIE XYZ image with pixel values eventually outside of the [0,1]
 * interval. Consequently scaling is necessary before any visualisation attempt
 * of the result.
 * 
 *	MASK MANAGEMENT (by Régis) : 
 *	- input's mask becomes output's mask.
 *	- no modification on color calculation.
 * 
 * @author Erchan Aptoula, Jonathan Weber
 * 
 */

public class RGBToXYZ extends Algorithm {

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
	public RGBToXYZ() {

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
		int size = input.size();

		if (input.getBDim() != 3)
			throw new AlgorithmException(
					"The input must be a tristumulus RGB image");

		output = input.newDoubleImage();
		this.output.setMask( this.input.getMask() );
		output.setColor(true);
		
		for(int i=0;i<size;i=i+3)
		{
			int R = input.getPixelByte(i);
			int G = input.getPixelByte(i+1);
			int B = input.getPixelByte(i+2);

			double[] xyz = convert(R, G, B);

			output.setPixelDouble(i, xyz[0]);
			output.setPixelDouble(i+1, xyz[1]);
			output.setPixelDouble(i+2, xyz[2]);
			
		}		
	}

	/**
	 * converts a triplet of rgb in [0,255] into xyz
	 * 
	 * @param r
	 * @param g
	 * @param b
	 * @return the array of xyz values
	 */
	public static double[] convert(int r, int g, int b) {
		// normalise to [0,1]
		double rN = r * 0.003921;
		double gN = g * 0.003921;
		double bN = b * 0.003921;

		double[] xyz = new double[3];

		xyz[0] = 0.412453 * rN + 0.357580 * gN + 0.180423 * bN;
		xyz[1] = 0.212671 * rN + 0.715160 * gN + 0.072169 * bN;
		xyz[2] = 0.019334 * rN + 0.119193 * gN + 0.950227 * bN;

		return xyz;
	}

	/**
	 * This class realizes the transformation of a tristumulus RGB image into a
	 * double valued CIE XYZ image with pixel values eventually outside of the
	 * [0,1] interval. Consequently scaling is necessary before any
	 * visualisation attempt of the result.
	 * 
	 * @param input
	 *            Tristumulus RGB image.
	 * @return A double valued CIE XYZ image.
	 */
	public static Image exec(Image input) {
		return (Image) new RGBToXYZ().process(input);
	}
}