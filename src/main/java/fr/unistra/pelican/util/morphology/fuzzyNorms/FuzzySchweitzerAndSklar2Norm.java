/**
 * 
 */
package fr.unistra.pelican.util.morphology.fuzzyNorms;

import fr.unistra.pelican.PelicanException;

/**
 * @author perret
 *
 */
public class FuzzySchweitzerAndSklar2Norm extends FuzzyNorm {

private double p;
	
	public FuzzySchweitzerAndSklar2Norm(double p)
	{
		if (p<=0.0 )
			throw new PelicanException("p parametre invalid");
		this.p=p;
	}
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.fuzzy.util.Norms.FuzzyTNorm#tDistance(double, double)
	 */
	public double tDistance(double a, double b) {
		return 1.0-Math.pow(Math.pow(1.0-a,p)+Math.pow(1.0-b,p)-Math.pow((1.0-a)*(1.0-b), p), 1.0/p);
	}

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.fuzzy.util.Norms.FuzzyTCoNorm#tCoDistance(double, double)
	 */
	public double tCoDistance(double a, double b) {
		return Math.pow(Math.pow(a, p)+Math.pow(b,p)-Math.pow(a*b, p),1.0/p);
	}

}
