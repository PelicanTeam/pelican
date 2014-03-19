package fr.unistra.pelican.util.vectorial.orders;

import java.util.Arrays;

import fr.unistra.pelican.util.Tools;


/**
 * This class represents a vector ordering realized by means of the distances relative to a reference vector;
 * 
 * 12/12/2007
 * 
 * @author Abdullah
 *
 */

public class ReferenceBasedDistanceOrdering2 implements BinaryVectorialOrdering
{
	private double[] d = null;
	private double[] ref = null;
	
	public ReferenceBasedDistanceOrdering2(double[] ref)
	{
		this.ref = ref;	
	}

	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#max(double[][])
	 */
	public double[] max(double[][] p)
	{
		// smaller distance <=> "larger" vector
		preprocess(p);

		int max = 0;

		for(int i = 1; i < p.length; i++)
			if(d[i] < d[max]) max = i;

		return p[max];
	}

	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#min(double[][])
	 */
	public double[] min(double[][] p)
	{
		// larger distance <=> "smaller" vector
		preprocess(p);

		int min = 0;

		for(int i = 1; i < p.length; i++){
			if(d[i] > d[min]) min = i;
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

		// get every vector's distance to the reference
		for(int i = 0; i < p.length; i++){
			d[i] = Tools.euclideanDistance(ref,p[i]);
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

	public int compare(Object o1, Object o2)
	{
		double[] p1 = (double[])o1;
		double[] p2 = (double[])o2;
		
		double dist1 = Tools.euclideanDistance(p1,ref);
		double dist2 = Tools.euclideanDistance(p2,ref);
		
		if (dist1 < dist2) return 1;
		else if (dist1 > dist2) return -1;
		else return 0;
	}

	public double[] max(double[] p1, double[] p2)
	{
		if (compare(p1,p2) == 1) return p1;
		else return p2;
	}

	public double[] min(double[] p1, double[] p2)
	{
		if (compare(p1,p2) == 1) return p2;
		else return p1;
	}
}
