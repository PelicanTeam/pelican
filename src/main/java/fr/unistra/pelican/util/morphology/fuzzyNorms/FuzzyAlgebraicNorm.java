/**
 * 
 */
package fr.unistra.pelican.util.morphology.fuzzyNorms;

/**
 * @author perret
 *
 */
public class FuzzyAlgebraicNorm extends FuzzyNorm {

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.fuzzy.util.Norms.FuzzyTNorm#tDistance(double, double)
	 */
	public double tDistance(double a, double b) {
		
		return a*b;
	}

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.fuzzy.util.Norms.FuzzyTCoNorm#tCoDistance(double, double)
	 */
	public double tCoDistance(double a, double b) {

		return a+b-a*b;
	}

}
