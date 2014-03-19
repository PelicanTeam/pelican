package fr.unistra.pelican.algorithms.geometric;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.Image;

/**
 * This class performs a spatial resampling of the images, and work in any
 * dimensions
 * 
 * @author Sébastien Lefevre
 * 
 * TODO: améliorer la gestion des bords
 */

public class ResamplingByRatio extends Algorithm {

	/**
	 * The input image
	 */
	public Image input;

	/**
	 * The resampling ratio on the dimension X
	 */
	public Double rx;

	/**
	 * The resampling ratio on the dimension Y
	 */
	public Double ry;
	
	/**
	 * The resampling ratio on the dimension Z
	 */
	public Double rz;
	
	/**
	 * The resampling ratio on the dimension T
	 */
	public Double rt;
	
	/**
	 * The resampling ratio on the dimension B
	 */
	public Double rb;

	/**
	 * The resampling method : NEAREST, BILINEAR
	 */
	public Integer resamplingMethod;

	/**
	 * The output image
	 */
	public Image output;

	/**
	 * Constant to represent NEAREST method: Considers one pixel every X
	 */
	public final static int NEAREST = 0;

	/**
	 * Constant to represent NEAREST method: Considers the average of X pixels
	 */
	public final static int BILINEAR = 1;

	/**
	 * Default constructor
	 */
	public ResamplingByRatio() {
		super.inputs = "input,rx,ry,rz,rt,rb,resamplingMethod";
		super.outputs = "output";
		
	}

	/**
	 * Performs a spatial resampling of the images and work in any dimensions
	 * @param input The input image
	 * @param rx The resampling ratio on the dimension X
	 * @param ry The resampling ratio on the dimension Y
	 * @param rz The resampling ratio on the dimension Z
	 * @param rt The resampling ratio on the dimension T
	 * @param rb The resampling ratio on the dimension B
	 * @param resamplingMethod The resampling method : NEAREST, BILINEAR
	 * @return The output image
	 */
	public static Image exec(Image input, double rx, double ry, double rz, double rt, double rb, int resamplingMethod) {
		return (Image) new ResamplingByRatio().process(input,rx,ry,rz,rt,rb,resamplingMethod);
	}
	
	/*
	 * (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() {
		int xdim = input.getXDim();
		int ydim = input.getYDim();
		int zdim = input.getZDim();
		int tdim = input.getTDim();
		int bdim = input.getBDim();
		int vx = (int) (xdim * rx);
		int vy = (int) (ydim * ry);
		int vz = (int) (zdim * rz);
		int vt = (int) (tdim * rt);
		int vb = (int) (bdim * rb);
		output= ResamplingByValue.exec(input, vx, vy, vz, vt, vb, resamplingMethod);
	}
}
