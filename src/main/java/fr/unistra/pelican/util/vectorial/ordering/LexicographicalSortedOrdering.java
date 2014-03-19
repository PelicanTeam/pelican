/**
 * 
 */
package fr.unistra.pelican.util.vectorial.ordering;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;

/**
 * Some kind of mixing between of max ordering and lexicographic ordering.
 * Vector elements are first sorted in decreasing order.
 * Sorted vectors are compared using traditional lexicographic ordering.
 * 
 * @author Benjamin Perret
 *
 */
public class LexicographicalSortedOrdering extends VectorialOrdering {

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.CC.Ordering.VectorialOrdering#compare(double[], double[])
	 */
	@Override
	public int compare(double[] o11, double[] o22) {
		double[] o1=o11.clone();
		double[] o2=o22.clone();
		Arrays.sort(o1);
		Arrays.sort(o2);
		
		for (int i = o1.length-1; i >= 0; i--) {
			int c = Double.compare(o1[i], o2[i]);
			if (c != 0) {
				return c;
			}
		}

		return 0;
	}

}
