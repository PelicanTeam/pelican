/**
 * 
 */
package fr.unistra.pelican.util.morphology.complements;

/**
 * Fuzzy Standard Complement fonction
 * @author perret
 *
 */
public class FuzzyStandardComplement extends FuzzyComplement {

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.fuzzy.util.FuzzyComplement#complement(double)
	 */
	public double complement(double a) {
		return 1.0-a;
	}

}
