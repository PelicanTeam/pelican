package fr.unistra.pelican.algorithms.conversion;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;


/**
 * This class converts a RGB image in the Improved HLS color space.
 * 
 * It is based on Allan Hanbury and Jean Serra, 
 * A 3D-polar Coordinate Colour Representation Suitable for Image Analysis,
 * Technical Report of Vienna University of Technology, March 2003 
 * 
 * Order of component is YSH to distinguish it from RGBToHSY and RGBToHSY2 algorithm
 * 
 * H belongs to [0;2Pi]
 * 
 * @author Jonathan Weber
 *
 */
public class RGBToIHLS extends Algorithm {

	/**
	 * Input RGB image
	 */
	public Image input;

	/**
	 * Output IHLS image
	 */
	public DoubleImage output;

	/**
	 * Constructor
	 * 
	 */
	public RGBToIHLS() {

		super();
		super.inputs = "input";
		super.outputs = "output";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws PelicanException 
	{
		int xdim = input.getXDim();
		int ydim = input.getYDim();
		int zdim = input.getZDim();
		int tdim = input.getTDim();
		int bdim = input.getBDim();

		if (bdim != 3)
			throw new PelicanException("The input must be a tristumulus RGB image");
		
		output = input.newDoubleImage();
		
		for(int t=0;t<tdim;t++)
			for(int z=0;z<zdim;z++)
				for(int y=0;y<ydim;y++)
					for(int x=0;x<xdim;x++)
					{
						double R = input.getPixelXYZTBDouble(x, y, z, t, 0);
						double G = input.getPixelXYZTBDouble(x, y, z, t, 1);
						double B = input.getPixelXYZTBDouble(x, y, z, t, 2);

						double[] ihls = convert(R, G, B);

						output.setPixelXYZTBDouble(x, y, z, t, 0, ihls[0]);
						output.setPixelXYZTBDouble(x, y, z, t, 1, ihls[1]);
						output.setPixelXYZTBDouble(x, y, z, t, 2, ihls[2]);
					}		
	}

	/**
	 * Transform RGB triplet in IHLS triplet
	 * in the order YSH.
	 * 
	 * H belongs to [0;2Pi]
	 * 
	 * @param r
	 * @param g
	 * @param b
	 * @return
	 */
	public static double[] convert(double r, double g, double b) {
		double[] ihls = new double[3];
		ihls[0]=0.2126*r+0.7152*g+0.0722*b;
		ihls[1]=Math.max(r, Math.max(g,b))-Math.min(r, Math.min(g,b));
		
		double upperFrac = r - 0.5*g - 0.5*b;
		double underFrac = Math.sqrt(r*r+g*g+b*b-r*g-r*b-b*g);
		double hPrime;
		
		// Different hPrime due to Java management of double		
		if(underFrac==0||upperFrac/underFrac<=-1||upperFrac/underFrac>=1)
			hPrime=0;
		else if(upperFrac==0)
			hPrime = Math.acos(0.);
		else
			hPrime = Math.acos(upperFrac/underFrac);
		
		if(b>g)
			ihls[2]=(2*Math.PI)-hPrime;
		else
			ihls[2]=hPrime;
		
		if(underFrac==0||upperFrac/underFrac<=-1||upperFrac/underFrac>=1)
			hPrime=0;
		
		
		
		return ihls;		
	}

/**
 * This class converts a RGB image in the Improved HLS color space.
 * 
 * It is based on Allan Hanbury and Jean Serra, 
 * A 3D-polar Coordinate Colour Representation Suitable for Image Analysis,
 * Technical Report of Vienna University of Technology, March 2003 
 * 
 * Order of component is YSH to distinguish it from RGBToHSY and RGBToHSY2 algorithm
 * 
 * H belongs to [0;2Pi]
 * 
 * @param image to transform  
 */
	public static DoubleImage exec(Image input) {
		return (DoubleImage) new RGBToIHLS().process(input);
	}
}

