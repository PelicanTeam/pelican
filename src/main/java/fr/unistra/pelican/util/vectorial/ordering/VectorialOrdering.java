/**
 * 
 */
package fr.unistra.pelican.util.vectorial.ordering;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 * Vectorial ordering, a true one not like the one coded by Erchan ;)
 * 
 * @author Benjamin Perret
 *
 */
public abstract class VectorialOrdering implements Comparator<double []>, fr.unistra.pelican.util.vectorial.orders.VectorialOrdering{

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public abstract int compare(double [] o1, double [] o2);
	
	public double [] max(double [] ... vects)
	{
		double [] max=vects[0];
		for(int i=1;i<vects.length;i++)
		{
			if (compare(max,vects[i])<0)
				max=vects[i];
		}
		return max;
	}
		
	public double [] min(double [] ... vects)
	{
		double [] min=vects[0];
		for(int i=1;i<vects.length;i++)
		{
			if (compare(min,vects[i])>0)
				min=vects[i];
		}
		return min;
	}

	@Override
	public double[] rank(double[][] p, int r) {
		Arrays.sort(p,this);

		return p[r];
		
	}
	
	
	
}
