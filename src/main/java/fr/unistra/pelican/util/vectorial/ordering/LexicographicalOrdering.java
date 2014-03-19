/**
 * 
 */
package fr.unistra.pelican.util.vectorial.ordering;

import fr.unistra.pelican.util.Tools;

/**
 * Lexicographic ordering with a possible band reordering 
 * 
 * @author Benjamin Perret
 *
 */
public class LexicographicalOrdering extends VectorialOrdering {

	
	private int [] bandMix=null;
	
	public  LexicographicalOrdering(){
		
	}
	
	public  LexicographicalOrdering(int [] bandMix){
		this.bandMix=bandMix;
	}
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.CC.Ordering.VectorialOrdering#compare(double[], double[])
	 */
	@Override
	public int compare(double[] o1, double[] o2) {
		if (bandMix == null) {
			for (int i = 0; i < o1.length; i++) {
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

		return 0;
	}

}
