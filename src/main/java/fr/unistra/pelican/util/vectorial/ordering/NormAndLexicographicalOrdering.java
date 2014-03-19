/**
 * 
 */
package fr.unistra.pelican.util.vectorial.ordering;

import fr.unistra.pelican.util.Tools;

/**
 * Ordering based on norm first and then on traditional lexicographic ordering
 * 
 * @author Benjamin Perret
 *
 */
public class NormAndLexicographicalOrdering extends VectorialOrdering {

	private int [] bandMix=null;
	
	
	/**
	 * @param bandMix
	 */
	public NormAndLexicographicalOrdering() {
		super();
	}

	
	/**
	 * @param bandMix
	 */
	public NormAndLexicographicalOrdering(int[] bandMix) {
		super();
		this.bandMix = bandMix;
	}


	private double reducedNorm(double [] o)
	{
		double res=0.0;
		for(int i=0;i<o.length;i++)
			if(o[i]>0)
			res+=o[i]*o[i];
		return res;
	}

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.CC.Ordering.VectorialOrdering#compare(double[], double[])
	 */
	@Override
	public int compare(double[] o1, double[] o2) {

		int a = Double.compare(reducedNorm(o1), reducedNorm(o2));
		if (a != 0)
			return a;
		else {
			if (bandMix == null) {
				for (int i = 1; i < o1.length; i++) {
					int c = Double.compare(o1[i], o2[i]);
					if (c != 0) {
						return c;
					}
				}
			} else {
				for (int i = 0; i < o1.length; i++) {
					int c = Double.compare(o1[bandMix[i]], o2[bandMix[i]]);
					if (c != 0) {
						return c;
					}
				}
			}
		}

		return 0;
	}
}
