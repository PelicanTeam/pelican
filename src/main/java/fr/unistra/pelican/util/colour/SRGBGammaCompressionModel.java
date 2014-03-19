/**
 * 
 */
package fr.unistra.pelican.util.colour;

/**
 * This is the standard sRGB compression model for the standard sRGC ICC profile.
 * 
 * @author Benjamin Perret
 *
 */
public class SRGBGammaCompressionModel extends GammaCompressionModel {

	private double gamma=2.4;
	private double gammaInv=1.0/gamma;
	private double a=12.92;
	private double bb=0.055;
	private double it=0.00304;
	
	/**
	 * 
	 */
	public SRGBGammaCompressionModel() {
	}

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.util.colour.GammaCompression#compress(double, fr.unistra.pelican.util.colour.GammaCompression.Band)
	 */
	@Override
	public double compress(double value, Band b) {
		return (value<it)?a*value:((1.0+bb)*Math.pow(value,gammaInv)-bb);
	}

}
