/**
 * 
 */
package fr.unistra.pelican.util.morphology.fuzzyNorms;

/**
 * @author perret
 *
 */
public class FuzzyBoundedNorm extends FuzzyNorm {

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.fuzzy.util.Norms.FuzzyTNorm#tDistance(double, double)
	 */
	public double tDistance(double a, double b) {
		return Math.max(0.0,a+b-1.0);
	}

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.fuzzy.util.Norms.FuzzyTCoNorm#tCoDistance(double, double)
	 */
	public double tCoDistance(double a, double b) {
		return Math.min(1.0,a+b);
	}

}
