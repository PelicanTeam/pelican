package fr.unistra.pelican.util.vectorial.orders;

import java.util.Comparator;

import fr.unistra.pelican.util.vectorial.VectorPixel;


/**
 * This class represents a vector ordering based on the euclidean norm of the vectors in question
 * 
 * @author E.A.
 *
 */

public class LexNormBasedOrdering implements VectorialOrdering,Comparator
{
	private double[][] d = null;

	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#max(double[][])
	 */
	public double[] max(double[][] p)
	{
		preprocess(p);

		int max = 0;

		for(int i = 1; i < p.length; i++){
			for(int j = 0; j < p[0].length; j++){
				if(d[max][j] < d[i][j]){
					max = i;
					break;
				}else if(d[max][j] > d[i][j])
					break;
			}
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
			for(int j = 0; j < p[0].length; j++){
				if(d[min][j] > d[i][j]){
					min = i;
					break;
				}else if(d[min][j] < d[i][j])
					break;
			}
		}

		return p[min];
	}
	
	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#order(fr.unistra.pelican.util.vectorial.VectorPixel[])
	 */
	public VectorPixel[] order(VectorPixel[] v)
	{
		return null;
	}
	
	public double[] rank(double[][] p,int r)
	{
		return null;
	}

	private void preprocess(double[][] p)
	{
		d = new double[p.length][p[0].length];

		for(int i = 0; i < p.length; i++){
			d[i][0] = norm(p[i],3);
			d[i][1] = norm(p[i],2);
			d[i][2] = norm(p[i],1);
		}
	}

	// for now only euclidean
	private double norm(double[] p, int k)
	{
		double norm = 0.0;

		for(int i = 0; i < k; i++)
			norm += p[i] * p[i];

		return Math.sqrt(norm);
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

		for(int i = 0; i < v.length; i++){
			if(d[0][i] < d[1][i]) return -1;
			else if(d[0][i] > d[1][i]) return 1;
		}
		return 0;
	}

	public double[] max(double[] p, double[] r) {
		// TODO Auto-generated method stub
		return null;
	}

	public double[] min(double[] p, double[] r) {
		// TODO Auto-generated method stub
		return null;
	}
}
