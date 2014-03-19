/**
 * 
 */
package fr.unistra.pelican.util.morphology.fuzzyNorms;

import fr.unistra.pelican.PelicanException;

/**
 * @author perret
 *
 */
public class FuzzyHamacherNorm extends FuzzyNorm {

	private double gamma;
	
	public FuzzyHamacherNorm(double gamma)
	{
		if (gamma <= 0.0) throw new PelicanException("Gamma parametre for Hamacher norm must be positive!");
		this.gamma=gamma;
	}
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.fuzzy.util.Norms.FuzzyTNorm#tDistance(double, double)
	 */
	public double tDistance(double a, double b) {		
		return (a*b)/(gamma+(1-gamma)*(a+b-a*b));
	}

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.fuzzy.util.Norms.FuzzyTCoNorm#tCoDistance(double, double)
	 */
	public double tCoDistance(double a, double b) {
		return (a+b-(gamma-2)*a*b)/(1+(gamma-1)*a*b);
	}

	public double getGamma() {
		return gamma;
	}

	public void setGamma(double gamma) {
		if (gamma <= 0.0)
			System.err.println("Invalid gamma value for Hamacher Norm");
		else this.gamma = gamma;
	}

}
