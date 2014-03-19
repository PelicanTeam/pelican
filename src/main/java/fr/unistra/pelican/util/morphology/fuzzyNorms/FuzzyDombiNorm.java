/**
 * 
 */
package fr.unistra.pelican.util.morphology.fuzzyNorms;

import fr.unistra.pelican.PelicanException;

/**
 * @author perret
 *
 */
public class FuzzyDombiNorm extends FuzzyNorm{

	private double lambda;
	
	public FuzzyDombiNorm(double lambda)
	{
		if (lambda<=0.0 )
			throw new PelicanException("lambda parametre invalid");
		this.lambda=lambda;
	}
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.fuzzy.util.Norms.FuzzyTNorm#tDistance(double, double)
	 */
	public double tDistance(double a, double b) {
		
		return 1.0/(1.0+Math.pow(Math.pow(1.0/a-1.0,lambda)+Math.pow(1.0/b-1.0,lambda), 1.0/lambda));
	}

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.fuzzy.util.Norms.FuzzyTCoNorm#tCoDistance(double, double)
	 */
	public double tCoDistance(double a, double b) {
		return 1.0/(1.0+Math.pow(Math.pow(1.0/a-1.0,-1.0*lambda)+Math.pow(1.0/b-1.0,-1.0*lambda), -1.0/lambda));
	}

}
