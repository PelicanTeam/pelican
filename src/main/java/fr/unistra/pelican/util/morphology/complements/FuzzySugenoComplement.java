/**
 * 
 */
package fr.unistra.pelican.util.morphology.complements;

import fr.unistra.pelican.PelicanException;

/**
 * @author perret
 *
 */
public class FuzzySugenoComplement extends FuzzyComplement {

	
	private double lambda;
	
	public FuzzySugenoComplement(double lambda)
	{
		if (lambda<=-1.0) throw new PelicanException("Invalid lambda parametre!");
		this.lambda=lambda;
	}
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.fuzzy.util.Complements.FuzzyComplement#complement(double)
	 */
	@Override
	public double complement(double a) {
		return (1.0-a)/(1.0+lambda*a);
	}

}
