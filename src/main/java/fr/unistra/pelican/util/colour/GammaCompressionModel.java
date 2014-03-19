/**
 * 
 */
package fr.unistra.pelican.util.colour;

/**
 * Abstract class for an object providing method for gamma 
 * compression to prepare an image with linear dynamic range for display.
 * The gamma expansion being automatically done by the graphic card and the monitor, 
 * please use the gamma compression model best adapted to the ICC profile 
 * of the display device your going to use (and prefer sRGB for standard diffusion).
 * 
 * @author Benjamin Perret
 *
 */
public abstract class GammaCompressionModel {
	/**
	 * Compression can differ for each band, but it is up to to the final 
	 * class to decide for which bands it applies a special process.
	 * @author Benjamin Perret
	 *
	 */
	public static enum Band{UNKNOWN,R,G,B,Y,U,V,H,S,I,L,A,C};
	
	
	/**
	 * Compress given value using the final class gamma compression model 
	 * according to the given band (or using default behavior is given band is not supported).
	 * @param value Pixel linear value in [0,1], this value is given in double precision to avoid loose of dynamic range due to non optimal quantization in byte precision.
	 * @param b Value concerns the given band (special compression may be applied)
	 * @return Compressed version of the given value.
	 */
	public abstract double compress(double value, Band b);

	
}
