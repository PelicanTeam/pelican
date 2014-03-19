package fr.unistra.pelican.algorithms.conversion;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.PelicanException;


/**
 * This class converts an Improved HLS image in the RGB color space.
 * 
 * It is based on Allan Hanbury and Jean Serra, 
 * A 3D-polar Coordinate Colour Representation Suitable for Image Analysis,
 * Technical Report of Vienna University of Technology, March 2003 
 * 
 * Order of components is YSH
 * 
 * H have to belong to [0;2Pi]
 * 
 * @author Jonathan Weber
 *
 */
public class IHLSToRGB extends Algorithm {

	/**
	 * Input parameter
	 */
	public DoubleImage input;

	/**
	 * Output parameter
	 */
	public ByteImage output;

	/**
	 * Constructor
	 * 
	 */
	public IHLSToRGB() {

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
			throw new PelicanException("The input must be a tristumulus IHLS image");
		
		output = input.newByteImage();
		
		for(int t=0;t<tdim;t++)
			for(int z=0;z<zdim;z++)
				for(int y=0;y<ydim;y++)
					for(int x=0;x<xdim;x++)
					{
						double Y = input.getPixelXYZTBDouble(x, y, z, t, 0);
						double S = input.getPixelXYZTBDouble(x, y, z, t, 1);
						double H = input.getPixelXYZTBDouble(x, y, z, t, 2);

						double[] rgb = convert(Y, S, H);

						output.setPixelXYZTBDouble(x, y, z, t, 0, rgb[0]);
						output.setPixelXYZTBDouble(x, y, z, t, 1, rgb[1]);
						output.setPixelXYZTBDouble(x, y, z, t, 2, rgb[2]);						
					}		
	}

	/**
	 * Transform IHLS triplet in RGB triplet
	 * order of IHLS have to be YSH.
	 * 
	 * H have to belong to [0;2Pi]
	 * 
	 * @param r
	 * @param g
	 * @param b
	 * @return
	 */
	public static double[] convert(double y, double s, double h) {
		double[] rgb = new double[3];
		double hEtoile = h%(Math.PI/3.0);
		double c = (Math.sqrt(3)*s)/(2*Math.sin(2*Math.PI/3-hEtoile));
		
		double c1 = c * Math.cos(h);
		double c2 = -c * Math.sin(h);
		
		rgb[0] = Math.min(1, Math.max(0,y+0.7875*c1+0.3714*c2));
		rgb[1] = Math.min(1, Math.max(0,y-0.2125*c1-0.2059*c2));
		rgb[2] = Math.min(1, Math.max(0,y-0.2125*c1+0.9488*c2));
		
		
		return rgb;		
	}

	/**
	 * This class converts an Improved HLS image in the RGB color space.
	 * 
	 * It is based on Allan Hanbury and Jean Serra, 
	 * A 3D-polar Coordinate Colour Representation Suitable for Image Analysis,
	 * Technical Report of Vienna University of Technology, March 2003 
	 * 
	 * Order of components is YSH
	 * 
	 * H have to belongs to [0;2Pi]
	 * 
	 */
	public static ByteImage exec(DoubleImage input) {
		return (ByteImage) new IHLSToRGB().process(input);
	}
}


