/**
 * 
 */
package fr.unistra.pelican.util.morphology.complements;

import fr.unistra.pelican.PelicanException;

/**
 * @author perret
 *
 */
public class FuzzyYageComplement extends FuzzyComplement {

	private double omega;
	
	public FuzzyYageComplement(double omega)
	{
		if (omega<=0.0) throw new PelicanException("Invalid omega parametre!");
		this.omega=omega;
	}
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.fuzzy.util.Complements.FuzzyComplement#complement(double)
	 */
	@Override
	public double complement(double a) {
		return Math.pow(1.0-Math.pow(a,omega), 1.0/omega);
	}

}
