/**
 * 
 */
package fr.unistra.pelican.util.morphology.fuzzyNorms;

/**
 * @author perret
 *
 */
public class FuzzyDrasticNorm extends FuzzyNorm {

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.fuzzy.util.Norms.FuzzyTNorm#tDistance(double, double)
	 */
	public double tDistance(double a, double b) {
		double ret=0.0;
		if(Math.abs(b-1.0)<0.00001) ret=a;
		else if(Math.abs(a-1.0)<0.00001) ret=b;
		return ret;
	}

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.fuzzy.util.Norms.FuzzyTCoNorm#tCoDistance(double, double)
	 */
	public double tCoDistance(double a, double b) {
		double ret=1.0;
		if(Math.abs(b)<0.00001) ret=a;
		else if(Math.abs(a)<0.00001) ret=b;
		return ret;
	}

}
