package fr.unistra.pelican.util.vectorial.orders;

import java.util.Arrays;
import java.util.Comparator;

import fr.unistra.pelican.util.Tools;

/**
 * This class represents a hue ordering as defined in
 * 
 * Hanbury and Serra
 * Morphological operators on the unit circle,
 * IEEE Transactions on IP,2001
 * 
 * based on the angular distances from a reference hue.
 * 
 * @author E.A.
 *
 */

public class HueOrdering implements BinaryVectorialOrdering,Comparator
{
	private double[] d = null;
	private double ref;
	
	public HueOrdering(double ref)
	{
		this.ref = ref;
	}

	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#max(double[][])
	 */
	public double[] min(double[][] p)
	{
		// the result of erosion must not favor the reference colour..
		// hence the min computes the max distance and vice versa
		preprocess(p);

		int max = 0;

		for(int i = 1; i < p.length; i++){
			if(Tools.doubleCompare(d[max],d[i]) == -1) max = i;
		}

		return p[max];
	}

	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#min(double[][])
	 */
	public double[] max(double[][] p)
	{
		preprocess(p);

		int min = 0;

		for(int i = 1; i < p.length; i++){
			if(Tools.doubleCompare(d[min],d[i]) == 1) min = i;
		}

		return p[min];
	}

	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#rank(double[][], int)
	 */
	public double[] rank(double[][] p,int r)
	{
		preprocess(p);

		// it might seem like trouble
		// to go into using subclasses
		// for a simple indexed sorting
		// but when the channel number goes up..
		// quicksort will pay off.
		IndexedDouble[] id = new IndexedDouble[d.length];

		for(int i = 0; i < d.length; i++)
			id[i] = new IndexedDouble(d[i],i);

		Arrays.sort(id);

		return p[id[r].i];
	}

	private void preprocess(double[][] p)
	{
		d = new double[p.length];

		for(int i = 0; i < p.length; i++){
			double abs = Math.abs(ref - p[i][0]);
			
			if(abs <= 0.5) d[i] = abs;
			else d[i] = 1.0 - abs;
		}
	}

	private class IndexedDouble implements Comparable
	{
		double d;
		int i;

		IndexedDouble(double d,int i)
		{
			this.d = d; this.i = i;
		}

		public int compareTo(Object o){
			IndexedDouble d = (IndexedDouble)o;

			if(this.d < d.d) return -1;
			else if(this.d > d.d) return 1;
			else return 0;
		}
	}

	/**
	 * Compares the given arguments according to this ordering
	 * 
	 * @param o1 first double valued array or vector pixel
	 * @param o2 second double valued array or vector pixel
	 * @return 1,-1 or 0 if o1 is respectively superior, inferior or equal to o2 
	 */
	public int compare(Object o1,Object o2)
	{
		double[][] v = new double[2][];

		v[0] = (double[])o1;
		v[1] = (double[])o2;

		preprocess(v);

		if(Tools.doubleCompare(d[0],d[1]) == 1) return -1;
		else if(Tools.doubleCompare(d[0],d[1]) == -1) return 1;
		else return 0;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#max(double[], double[])
	 */
	public double[] max(double[] p,double[] r)
	{
		if(compare(p,r) == -1) return r;
		else return p;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#min(double[], double[])
	 */
	public double[] min(double[] p,double[] r)
	{
		if(compare(p,r) == 1) return r;
		else return p;
	}
}
