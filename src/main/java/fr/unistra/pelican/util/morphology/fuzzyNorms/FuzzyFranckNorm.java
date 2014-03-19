/**
 * 
 */
package fr.unistra.pelican.util.morphology.fuzzyNorms;

import fr.unistra.pelican.PelicanException;

/**
 * @author perret
 *
 */
public class FuzzyFranckNorm  extends FuzzyNorm{
	private double s;
	private double logs;
	
	public FuzzyFranckNorm(double s)
	{
		if (s<=0.0 || Math.abs(s-1.0)<=0.00001) throw new PelicanException("s parametre of Franck Norm must be positif and different from 1.0");
		this.s=s;
		logs=Math.log(s);
	}
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.fuzzy.util.Norms.FuzzyTCoNorm#tCoDistance(double, double)
	 */
	public double tCoDistance(double a, double b) {
		return 1.0 - Math.log(1.0+(Math.pow(s, 1.0-a)-1)*(Math.pow(s, 1.0-b)-1)/(s-1))/logs;
		
	}

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.fuzzy.util.Norms.FuzzyTNorm#tDistance(double, double)
	 */
	public double tDistance(double a, double b) {
		return Math.log(1.0+(Math.pow(s, a)-1)*(Math.pow(s, b)-1)/(s-1))/logs;
		
	}

	public double getS() {
		return s;
	}

	public void setS(double s) {
		if (s<=0.0 || Math.abs(s-1.0)<=0.00001) 
			System.err.println("Invalid s parametre for Franck Norm!");
		else {
			this.s=s;
			logs=Math.log(s);
			}	
	}

}
