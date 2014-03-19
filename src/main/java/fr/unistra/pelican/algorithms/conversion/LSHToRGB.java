package fr.unistra.pelican.algorithms.conversion;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;

/**
 * LSH to RGB conversion.
 * For further information cf. RGBToLSH.
 * 
 *	MASK MANAGEMENT (by Régis) : 
 *	- input's mask becomes output's mask.
 *	- no modification on color calculation.
 * 
 * @author Abdullah
 * 
 */

public class LSHToRGB extends Algorithm {

	/**
	 * Input image
	 */
	public Image input;

	/**
	 * Output image
	 */
	public Image output;

	/**
	 * Constructor
	 * 
	 */
	public LSHToRGB() {

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

		if (bdim != 3)
			throw new AlgorithmException("The input must be a tristumulus LSH image");

		output = new ByteImage(xdim, ydim, zdim, tdim, bdim);
		this.output.setMask( this.input.getMask() );
		output.setColor(true);

		for (int x = 0; x < xdim; x++) {
			for (int y = 0; y < ydim; y++) {
				for (int z = 0; z < zdim; z++) {
					for (int t = 0; t < tdim; t++) {
						double L = input.getPixelXYZTBDouble(x, y, z, t, 0);
						double S = input.getPixelXYZTBDouble(x, y, z, t, 1);
						double H = input.getPixelXYZTBDouble(x, y, z, t, 2);

						int[] rgb = convert(L, S, H);

						output.setPixelXYZTBByte(x, y, z, t, 0, rgb[0]);
						output.setPixelXYZTBByte(x, y, z, t, 1, rgb[1]);
						output.setPixelXYZTBByte(x, y, z, t, 2, rgb[2]);
					}
				}
			}
		}
	}

	/**
	 * converts a triplet of hsy into rgb
	 * 
	 * @param L
	 * @param S
	 * @param H
	 * @return the array of rgb values
	 */
	private static int[] convert(double L, double S, double H) {
		int[] rgb = new int[3];

		double R, G, B;
		R = G = B = 0.0;

		// get lambda and phi
		double k = (1.0/6.0);
		H = H / k; 	//(H = lambda + phi)
		int lambda = (int)Math.floor(H);
		double phi = H - lambda;
		
		// compute the max,med and min
		double max = 0.0, med = 0.0, min = 0.0;
		
		if(Math.pow(-1,lambda) * (phi - 0.5) <= 0.0){
			max = L + (2.0/3.0) * S;
			
			med = L - (1.0/3.0) * Math.pow(-1,lambda) * S + (2.0/3.0) * Math.pow(-1,lambda) * S * phi;
			
			min = L - (1.0/3.0) * S - (2.0/3.0) * S * (lambda % 2) - (2.0/3.0) * Math.pow(-1,lambda) * S * phi;	
		}else{
			max = L + (1.0/3.0) * S + (2.0/3.0) * S * ((lambda+1) % 2) - (2.0/3.0) * Math.pow(-1,lambda) * S * phi;
			
			med = L - (1.0/3.0) * Math.pow(-1,lambda) * S + (2.0/3.0) * Math.pow(-1,lambda) * S * phi;
			
			min = L - (2.0/3.0) * S;
		}
		
		// set them right...
		if(lambda == 0){
			R = max;
			G = med;
			B = min;
		}else if(lambda == 1){
			R = med;
			G = max;
			B = min;
		}else if(lambda == 2){
			R = min;
			G = max;
			B = med;
		}else if(lambda == 3){
			R = min;
			G = med;
			B = max;
		}else if(lambda == 4){
			R = med;
			G = min;
			B = max;
		}else if(lambda == 5){
			R = max;
			G = min;
			B = med;
		}

		rgb[0] = (int) Math.round(R * 255);
		if (rgb[0] > 255)
			rgb[0] = 255;
		else if (rgb[0] < 0)
			rgb[0] = 0;

		rgb[1] = (int) Math.round(G * 255);
		if (rgb[1] > 255)
			rgb[1] = 255;
		else if (rgb[1] < 0)
			rgb[1] = 0;

		rgb[2] = (int) Math.round(B * 255);
		if (rgb[2] > 255)
			rgb[2] = 255;
		else if (rgb[2] < 0)
			rgb[2] = 0;

		return rgb;
	}

	/**
	 * Realizes the transformation of a tristumulus double valued HSY
	 * image with pixels in [0,1], into a byte valued RGB image.
	 * 
	 * @param input
	 *            Tristumulus double valued HSY image with pixels in [0,1].
	 * @return Byte valued RGB image.
	 */
	public static Image exec(Image input) {
		return (Image) new LSHToRGB().process(input);
	}
	
//	public static void main(String[] args)
//	{
//		Image asil = ImageLoader.exec("samples/macaws.png");
//		Image img = RGBToLSH.exec(asil);
//		img.color = false;
//		Viewer2D.exec(img);
//		
//		Image geri = LSHToRGB.exec(img);
//		Viewer2D.exec(geri);
//		
//		for(int x = 0; x < asil.getXDim(); x++){
//			for(int y = 0; y < asil.getYDim(); y++){
//				int[] p = asil.getVectorPixelXYZTByte(x,y,0,0);
//				int[] q = geri.getVectorPixelXYZTByte(x,y,0,0);
//				
//				if(p[0] != q[0] || p[1] != q[1] || p[2] != q[2])
//					System.err.println(p[0] + " " + q[0] + " " + p[1] + " " + q[1] + " " + p[2] + " " + q[2]);
//			}
//		}
//	}
}