/**
 * 
 */
package fr.unistra.pelican.util.morphology.complements;

import fr.unistra.pelican.PelicanException;

/**
 * @author perret
 *
 */
public class FuzzyUnifiedSugenoAndYageComplement extends FuzzyComplement {

	private double omega;
	private double lambda;
	
	public FuzzyUnifiedSugenoAndYageComplement(double lambda,double omega)
	{
		if (omega<=0.0 || lambda <= -1.0) throw new PelicanException("Invalid lambda and/or omega parametre!");
		this.omega=omega;
		this.lambda=lambda;
	}
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.fuzzy.util.Complements.FuzzyComplement#complement(double)
	 */
	@Override
	public double complement(double a) {
		return Math.pow((1.0-Math.pow(a,omega))/(1.0+lambda*Math.pow(a,omega)), 1.0/omega);
	}

}
