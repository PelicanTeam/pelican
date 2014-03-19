
package fr.unistra.pelican.util.morphology.fuzzyNorms;


/**
 * The Standard T-Norm and T-CoNorm for fuzzy MM
 * @author perret
 *
 */
public final class FuzzyStandardNorm  extends FuzzyNorm{

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.fuzzy.util.FuzzyTCoNorm#tCoDistance(double, double)
	 */
	public double tCoDistance(double a, double b) {
		return Math.max(a, b);
	}

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.fuzzy.util.FuzzyTNorm#tDistance(double, double)
	 */
	public double tDistance(double a, double b) {
		return Math.min(a, b);
	}

}
