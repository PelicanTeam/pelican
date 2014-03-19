/**
 * 
 */
package fr.unistra.pelican.util.morphology.fuzzyNorms;

import fr.unistra.pelican.PelicanException;

/**
 * @author perret
 *
 */
public class FuzzyDuboisAndPradeNorm extends FuzzyNorm {

	private double alpha;
	
	public FuzzyDuboisAndPradeNorm(double alpha)
	{
		if (alpha<0.0 || alpha>1.0)
			throw new PelicanException("alpha parametre invalid");
		this.alpha=alpha;
	}
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.fuzzy.util.Norms.FuzzyTNorm#tDistance(double, double)
	 */
	public double tDistance(double a, double b) {
		return (a*b)/(Math.max(Math.max(a,b),alpha));
	}

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.fuzzy.util.Norms.FuzzyTCoNorm#tCoDistance(double, double)
	 */
	public double tCoDistance(double a, double b) {
		return 1.0-((1.0-a)*(1.0-b))/(Math.max(Math.max(1.0-a,1.0-b),alpha));
	}

}
