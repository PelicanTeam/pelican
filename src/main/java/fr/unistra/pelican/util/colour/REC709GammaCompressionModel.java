/**
 * 
 */
package fr.unistra.pelican.util.colour;


/**
 * This is the standard REC.709 compression model for the standard REC.709 ICC profile (HDTV definition).
 * 
 * @author Benjamin Perret
 *
 */
public class REC709GammaCompressionModel extends GammaCompressionModel {

	

	private double gamma=2.22;
	private double gammaInv=1.0/gamma;
	private double a=4.5;
	private double bb=0.099;
	private double it=0.018;
	
	/**
	 * 
	 */
	public REC709GammaCompressionModel() {
	}

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.util.colour.GammaCompression#compress(double, fr.unistra.pelican.util.colour.GammaCompression.Band)
	 */
	@Override
	public double compress(double value, Band b) {
		return (value<it)?a*value:((1.0+bb)*Math.pow(value,gammaInv)-bb);
	}

}
