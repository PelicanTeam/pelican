package fr.unistra.pelican.util.vectorial.orders;

import java.util.Arrays;

import fr.unistra.pelican.util.vectorial.VectorPixel;


/**
 * This class represents a vector ordering realized by means of the distances relative to a reference vector;
 * in this case the median.
 * 
 * @author E.A.
 *
 */

public class ReferenceBasedDistanceOrdering implements VectorialOrdering
{
	private double[] d = null;

	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#max(double[][])
	 */
	public double[] max(double[][] p)
	{
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
	public double[] min(double[][] p)
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

		// get every vector's cumulative distances
		for(int i = 0; i < p.length; i++){
			d[i] = 0.0;

			for(int j = 0; j < p.length; j++){
				if(j != i) d[i] += distance(p[i],p[j]);
			}

		}

		// smallest = median vector = reference vector (for now)
		int min = 0;

		for(int i = 1; i < p.length; i++){
			if(d[min] > d[i]) min = i;
		}

		// put in d the distances from the reference vector
		for(int i = 0; i < p.length; i++)
			d[i] = distance(p[min],p[i]);

	}

	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#order(fr.unistra.pelican.util.vectorial.VectorPixel[])
	 */
	public VectorPixel[] order(VectorPixel[] v)
	{
		double[][] p = new double[v.length][];
		VectorPixel[] result = new VectorPixel[v.length];

		for(int i = 0; i < v.length; i++)
			p[i] = v[i].getVector();

		preprocess(p);

		IndexedDouble[] id = new IndexedDouble[d.length];

		for(int i = 0; i < d.length; i++)
			id[i] = new IndexedDouble(d[i],i);

		Arrays.sort(id);

		for(int i = 0; i < v.length; i++)
			result[i] = v[id[i].i];

		return result;
	}

	private double distance(double[] p1,double[] p2)
	{
		double dist = 0.0;

		for(int i = 0; i < p1.length; i++){
			double tmp = p1[i] - p2[i];
			dist += tmp * tmp;
		}

		return Math.sqrt(dist);
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
	
	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#max(double[], double[])
	 */
	public double[] max(double[] p,double[] r)
	{
		return r;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#min(double[], double[])
	 */
	public double[] min(double[] p,double[] r)
	{
		return p;
	}
}
