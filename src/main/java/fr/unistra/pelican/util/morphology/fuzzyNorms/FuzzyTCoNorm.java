package fr.unistra.pelican.util.morphology.fuzzyNorms;
/**
 * Represent a T-CoNorm (or S-Norm) for fuzzy operations as defined by Bloch and Maitre
 * @author perret
 *
 */
public interface FuzzyTCoNorm {
	
	/**
	 * Compute the T-CoDistance beatween a and b
	 * @param a first arg
	 * @param b second arg
	 * @return distance (a,b)
	 */
	public double tCoDistance(double a, double b);

}
