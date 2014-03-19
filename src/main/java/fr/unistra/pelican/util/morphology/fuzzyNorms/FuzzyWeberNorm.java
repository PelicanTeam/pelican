/**
 * 
 */
package fr.unistra.pelican.util.morphology.fuzzyNorms;

import fr.unistra.pelican.PelicanException;

/**
 * @author perret
 *
 */
public class FuzzyWeberNorm extends FuzzyNorm{

	private double lambda;
	
	public FuzzyWeberNorm(double lambda)
	{
		if (lambda<=-1.0 )
			throw new PelicanException("lambda parametre invalid");
		this.lambda=lambda;
	}
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.fuzzy.util.Norms.FuzzyTNorm#tDistance(double, double)
	 */
	public double tDistance(double a, double b) {
		return Math.max(0.0,(a+b+lambda*a*b-1.0)/(1.0+lambda));
	}

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.fuzzy.util.Norms.FuzzyTCoNorm#tCoDistance(double, double)
	 */
	public double tCoDistance(double a, double b) {
		return Math.min(1.0,a+b-(lambda*a*b)/(1.0+lambda));
	}

}
