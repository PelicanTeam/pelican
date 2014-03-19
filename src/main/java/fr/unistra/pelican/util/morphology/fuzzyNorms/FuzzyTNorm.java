package fr.unistra.pelican.util.morphology.fuzzyNorms;
/**
 * Represent a T-Norm for fuzzy operations as defined by Bloch and Maitre
 * @author perret
 *
 */
public interface FuzzyTNorm {
	
	/**
	 * Compute the T-Distance beatween a and b
	 * @param a first arg
	 * @param b second arg
	 * @return distance (a,b)
	 */
	public double tDistance(double a, double b);

}
