package fr.unistra.pelican.util.vectorial.orders;

import java.util.Arrays;
import java.util.Comparator;


/**
 * This class represents a hue ordering 
 * based on a saturation weighting as proposed by Hanbury.
 * 
 * @author E.A.
 *
 */

public class HanburyHueOrdering implements VectorialOrdering,Comparator
{
	private double[] d = null;
	private double refHue;
	
	public HanburyHueOrdering(double ref)
	{
		this.refHue = ref;
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
			if(d[max] < d[i]) max = i;
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
			if(d[min] > d[i]) min = i;
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
			double h = p[i][0];
			
			if(p[i][0] - refHue >= 0.0) h = p[i][0] - refHue;
			else h = 1.0 + p[i][0] - refHue;
			
			if(0.0 <= h && h <= 0.25) h = Math.max(h,0.25 * (1.0 - p[i][1]));
			else if(0.25 < h && h <= 0.5) h = Math.min(h,0.25 * (1.0 + p[i][1]));
			else if(0.5 < h && h <= 0.75) h = Math.max(h,0.25 * (3.0 - p[i][1]));
			else if(0.75 < h && h < 1.0) h = Math.min(h,0.25 * (3.0 + p[i][1]));
			
			double abs = Math.abs(refHue - h);
			
			if(abs <= 0.5) d[i] = 0.5 - abs;
			else d[i] = 0.5 - 1.0 + abs;
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

		if(d[0] < d[1]) return -1;
		else if(d[0] > d[1]) return 1;
		else return 0;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#max(double[], double[])
	 */
	public double[] max(double[] p,double[] r)
	{
		if(compare(p,r) == 1) return p;
		else return r;
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
