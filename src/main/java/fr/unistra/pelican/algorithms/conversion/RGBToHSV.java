package fr.unistra.pelican.algorithms.conversion;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;

/**
 * This class realizes the transformation of a tristumulus RGB image into a
 * double valued HSV image with pixels in the interval [0,1]. Thus it is
 * adequate also for visualisation.
 * 
 *	MASK MANAGEMENT (by Régis) : 
 *	- input's mask becomes output's mask.
 *	- no modification on color calculation.
 * 
 * @author Erchan Aptoula, Jonathan Weber
 * 
 */

public class RGBToHSV extends Algorithm {

	/**
	 * Input parameter
	 */
	public Image input;

	public boolean scaleToByte=false;
	
	/**
	 * Output parameter
	 */
	public Image output;

	/**
	 * Constructor
	 * 
	 */
	public RGBToHSV() {

		super();
		super.inputs = "input";
		super.options = "scaleToByte";
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

		for (int i = 0; i < size; i=i+3) {			
			int R = input.getPixelByte(i);
			int G = input.getPixelByte(i+1);
			int B = input.getPixelByte(i+2);

			// normalise to [0,1]
			double rN = R * 0.003921;
			double gN = G * 0.003921;
			double bN = B * 0.003921;

			double H, S, V;

			double min = rN;
			if (gN < min)
				min = gN;
			if (bN < min)
				min = bN;

			double max = rN;
			if (gN > max)
				max = gN;
			if (bN > max)
				max = bN;

			S = H = 0.0;
			V = max;

			double delta = max - min;

			if (max != 0 && delta != 0.0) {
				S = delta / max;

				if (rN == max)
					H = 60 * (gN - bN) / delta;

				else if (gN == max)
					H = 60 * (bN - rN) / delta + 120;

				else
					H = 60 * (rN - gN) / delta + 240; // bN == max

					if (H < 0.0)
						H += 360;
					if (H > 360)
						H -= 360;

					H = H / 360.0;
			}

			output.setPixelDouble(i, H);
			output.setPixelDouble(i+1, S);
			output.setPixelDouble(i+2, V);
		}
		if (scaleToByte)
			output=scaleToByte(output);

	}

	private static Image HSVToVSH(Image hsv) {
		Image vsh = hsv.copyImage(true);

		for (int x = 0; x < hsv.getXDim(); x++) {
			for (int y = 0; y < hsv.getYDim(); y++) {
				double h = hsv.getPixelXYBDouble(x, y, 0);
				double v = hsv.getPixelXYBDouble(x, y, 2);
				vsh.setPixelXYBDouble(x, y, 0, v);
				vsh.setPixelXYBDouble(x, y, 2, h);
			}
		}

		return vsh;
	}

	private static Image VSHToHSV(Image vsh) {
		Image hsv = vsh.copyImage(true);

		for (int x = 0; x < hsv.getXDim(); x++) {
			for (int y = 0; y < hsv.getYDim(); y++) {
				double h = vsh.getPixelXYBDouble(x, y, 2);
				double v = vsh.getPixelXYBDouble(x, y, 0);
				hsv.setPixelXYBDouble(x, y, 2, v);
				hsv.setPixelXYBDouble(x, y, 0, h);
			}
		}

		return hsv;
	}
	
	/**
	 * Scales each band of the resulting HSV image according to the value
	 * intervals H,S,V in [0,1] and returns a valid byteImage..
	 * 
	 * @return resulting ByteImage
	 */
	private static Image scaleToByte(Image hsv) {
		ByteImage bimg = new ByteImage(hsv, false);
		int size = bimg.size();

		for(int i=0;i<size;i=i+3)
		{
			double d = hsv.getPixelDouble(i);
			// H
			bimg.setPixelByte(i, (int) Math.round(d * 255));

			// S
			d = hsv.getPixelDouble(i+1);
			bimg.setPixelByte(i+1, (int) Math.round(d * 255));

			// V
			d = hsv.getPixelDouble(i+2);
			bimg.setPixelByte(i+2, (int) Math.round(d * 255));
		}

		return bimg;
	}

	/**
	 * This class realizes the transformation of a tristumulus RGB image into a
	 * double valued HSV image with pixels in the interval [0,1]. Thus it is
	 * adequate also for visualisation.
	 * 
	 * @param input
	 *            Tristumulus RGB image.
	 * @return A double valued HSV image.
	 */
	public static Image exec(Image input) {
		return (Image) new RGBToHSV().process(input);
	}
	
	/**
	 * This class realizes the transformation of a tristumulus RGB image into a
	 * double valued HSV image with pixels in the interval [0,1]. Thus it is
	 * adequate also for visualisation.
	 * 
	 * @param input Tristumulus RGB image.
	 * @param scaleToByte Scale result to byteImage
	 * @return A double valued HSV image.
	 */
	public static Image exec( Image input, boolean scaleToByte ) { 
		return ( Image ) new RGBToHSV().process(input,scaleToByte);
	}
}