package fr.unistra.pelican.util.vectorial.orders;

import java.util.Arrays;
import java.util.Comparator;

import fr.unistra.pelican.util.vectorial.VectorPixel;


/**
 * This class represents a vector ordering operating in a marginal fashion 
 * and then replacing the marginal extremum with its linear combinarion 
 * with the closest initial vector to it in terms of an Euclidean distance.
 * 
 * @author E.A.
 *
 */

public class AdaptiveMarginalWithNormReplacementOrdering implements VectorialOrdering,Comparator
{
	private double[] dmax = null;
	private double[] dmin = null;
	
	private double[] mmax;
	private double[] mmin;
	
	private double alpha;
	
	public AdaptiveMarginalWithNormReplacementOrdering(double alpha)
	{
		if(alpha < 0.0) alpha = 0.0;
		else if(alpha > 1.0) alpha = 1.0;
		
		this.alpha = alpha;
	}

	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#max(double[][])
	 */
	public double[] max(double[][] p)
	{
		preprocess(p);

		int min = 0;

		for(int i = 1; i < p.length; i++){
			if(dmax[min] > dmax[i]) min = i;
		}

		return combo(p[min],mmax);
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
			if(dmin[min] > dmin[i]) min = i;
		}

		return combo(p[min],mmin);
	}
	
	/**
	 * Computes the weighted average of the two vectors according to alpha
	 * 
	 * @param p1
	 * @param p2
	 * @return the weighted average vector = alpha * p1 + (1 - alpha) * p2
	 */
	private double[] combo(double[] p1,double[] p2)
	{
		if(alpha == 0.0) return p2;
		else if(alpha == 1.0) return p1;
		else{
			for(int i = 0; i < p1.length; i++)
				p1[i] = alpha * p1[i] + (1.0 - alpha) * p2[i];
			
			return p1;
			
		}
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
		IndexedDouble[] id = new IndexedDouble[dmax.length];

		for(int i = 0; i < dmax.length; i++)
			id[i] = new IndexedDouble(dmax[i],i);

		Arrays.sort(id);

		return p[id[r].i];
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

		IndexedDouble[] id = new IndexedDouble[dmax.length];

		for(int i = 0; i < dmax.length; i++)
			id[i] = new IndexedDouble(dmax[i],i);

		Arrays.sort(id);

		for(int i = 0; i < v.length; i++)
			result[i] = v[id[i].i];

		return result;
	}

	private void preprocess(double[][] p)
	{
		// compute the marginal max and min of the vectors
		mmax = new double[p[0].length];
		mmin = new double[p[0].length];
		
		for(int i = 0; i < p[0].length; i++){
			mmax[i] = p[0][i];
			mmin[i] = p[0][i];
			for(int j = 1; j < p.length; j++){
				if(p[j][i] > mmax[i]) mmax[i] = p[j][i];
				if(p[j][i] < mmin[i]) mmin[i] = p[j][i];
			}
		}
		
		// compute the distances of the vectors from the marginal max and min
		dmax = new double[p.length];
		dmin = new double[p.length];
		
		for(int i = 0; i < p.length; i++){
			dmax[i] = distance(mmax,p[i]);
			dmin[i] = distance(mmin,p[i]);
		}
	}

	// for now only euclidean
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

		if(dmax[0] < dmax[1]) return -1;
		else if(dmax[0] > dmax[1]) return 1;
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
