package fr.unistra.pelican.algorithms.conversion;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;

/**
 * This class realizes the transformation of a tristumulus double valued CIE XYZ
 * image into a CIE LAB image.
 * 
 *	MASK MANAGEMENT (by Regis) : 
 *	- input's mask becomes output's mask.
 *	- no modification on color calculation.
 * 
 * @author Erchan Aptoula, Jonathan Weber
 * 
 */

public class XYZToLAB extends Algorithm {

	/**
	 * Input parameter
	 */
	public Image input;

	/**
	 * Output parameter
	 */
	public Image output;

	public boolean scaleToByte=false;
	
	/**
	 * Constructor
	 * 
	 */
	public XYZToLAB() {
		super.inputs = "input";
		super.options="scaleToByte";
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
					"The input must be a tristumulus XYZ image");

		output = input.newDoubleImage();
		this.output.setMask( this.input.getMask() );
		output.setColor(true);

		for(int i=0;i<size;i=i+3)
		{
			double X = input.getPixelDouble(i);
			double Y = input.getPixelDouble(i+1);
			double Z = input.getPixelDouble(i+2);

			double[] lab = convert(X, Y, Z);

			output.setPixelDouble(i, lab[0]);
			output.setPixelDouble(i+1, lab[1]);
			output.setPixelDouble(i+2, lab[2]);
		}
		if (scaleToByte)
			output=scaleToByte(output);
	}

	/**
	 * converts a triplet of xyz into cielab
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return the array of lab values
	 */
	public static double[] convert(double x, double y, double z) {
		// THE white point..D65
		double Xn = 0.950456;
		double Yn = 1.0;
		double Zn = 1.088754;

		double Xfrac = x / Xn;
		double Yfrac = y / Yn;
		double Zfrac = z / Zn;

		if (Xfrac > 0.008856)
			Xfrac = Math.pow(Xfrac, 0.333333);
		else
			Xfrac = 7.787 * Xfrac + 16.0 / 116.0;

		if (Yfrac > 0.008856)
			Yfrac = Math.pow(Yfrac, 0.333333);
		else
			Yfrac = 7.787 * Yfrac + 16.0 / 116.0;

		if (Zfrac > 0.008856)
			Zfrac = Math.pow(Zfrac, 0.333333);
		else
			Zfrac = 7.787 * Zfrac + 16.0 / 116.0;

		double[] lab = new double[3];

		lab[0] = 116 * Yfrac - 16.0;
		lab[1] = 500 * (Xfrac - Yfrac);
		lab[2] = 200 * (Yfrac - Zfrac);

		return lab;
	}

	/**
	 * Scales each band of the resulting lab image according to the value
	 * intervals L in [0,100], a and b in [-128,127] and returns a valid
	 * byteImage..of course only valid for the D65 white point.
	 * 
	 * @return resulting ByteImage
	 */
	private static Image scaleToByte(Image lab) {
		ByteImage bimg = new ByteImage(lab, false);
		int size = bimg.size();
		double f = 2.55; // 255.0/100.0

		for(int i=0;i<size;i=i+3)
		{
			double d = lab.getPixelDouble(i);
			// L
			bimg.setPixelByte(i, (int) Math.round(d * f));

			// a and b
			d = lab.getPixelDouble(i+1);
			bimg.setPixelByte(i+1, (int) Math.round(d + 128));

			d = lab.getPixelDouble(i+2);
			bimg.setPixelByte(i+2, (int) Math.round(d + 128));
		}

		return bimg;
	}

	/**
	 * This class realizes the transformation of a tristumulus double valued CIE
	 * XYZ image into a CIE LAB image.
	 * 
	 * @param input
	 *            Tristumulus double valued CIE XYZ image.
	 * @return A CIE LAB image.
	 */
	public static Image exec(Image input) {
		return (Image) new XYZToLAB().process(input);
	}
	
	public static Image exec(Image input,boolean scaleToByte) {
		return (Image) new XYZToLAB().process(input,scaleToByte);
	}
}