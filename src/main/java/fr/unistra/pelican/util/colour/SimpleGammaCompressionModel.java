/**
 * 
 */
package fr.unistra.pelican.util.colour;

/**
 * Simple gamma correction
 * c(x)=x^(1/gamma)
 * <br>Same correction applied to all bands.
 * 
 * @author Benjamin Perret
 *
 */
public class SimpleGammaCompressionModel extends GammaCompressionModel {

	/**
	 * Exponent of the gamma expansion of the display device 
	 * (default value is 2.2 for standard monitor but reality may significantly differ)
	 */
	private double gamma=2.2;
	
	private double gammaInv=1.0/gamma;
	
	/**
	 * 
	 */
	public SimpleGammaCompressionModel() {
		super();
	}
	
	/**
	 * @param gamma
	 */
	public SimpleGammaCompressionModel(double gamma) {
		super();
		setGamma(gamma);
	}



	/* (non-Javadoc)
	 * @see fr.unistra.pelican.util.colour.GammaCompression#compress(double, fr.unistra.pelican.util.colour.GammaCompression.Band)
	 */
	@Override
	public double compress(double value, Band b) {
		
		return Math.pow(value,gammaInv);
	}

	/**
	 * @return the gamma
	 */
	public double getGamma() {
		return gamma;
	}

	/**
	 * @param gamma the gamma to set
	 */
	public void setGamma(double gamma) {
		this.gamma = gamma;
		gammaInv=1.0/gamma;
	}

	

}
