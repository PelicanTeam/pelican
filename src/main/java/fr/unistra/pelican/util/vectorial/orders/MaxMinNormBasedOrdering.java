package fr.unistra.pelican.util.vectorial.orders;

import java.util.Arrays;
import java.util.Comparator;

import fr.unistra.pelican.util.vectorial.VectorPixel;


/**
 *
 */

public class MaxMinNormBasedOrdering implements VectorialOrdering,Comparator
{
	private double[] distMin = null;
	private double[] distMedian = null;
	private double[] distMax = null;

	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#max(double[][])
	 */
	public double[] max(double[][] p)
	{
		preprocess(p);

		int max = 0;

		for(int i = 1; i < p.length; i++){
			if(distMax[max] > distMax[i]) max = i;
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
			if(distMin[min] > distMin[i]) min = i;
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
		IndexedDouble[] id = new IndexedDouble[distMax.length];

		for(int i = 0; i < distMax.length; i++)
			id[i] = new IndexedDouble(distMax[i],i);

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

		IndexedDouble[] id = new IndexedDouble[distMax.length];

		for(int i = 0; i < distMax.length; i++)
			id[i] = new IndexedDouble(distMax[i],i);

		Arrays.sort(id);

		for(int i = 0; i < v.length; i++)
			result[i] = v[id[i].i];

		return result;
	}

	private void preprocess(double[][] p)
	{
		distMax = new double[p.length];
		distMedian = new double[p.length];
		distMin = new double[p.length];
		
		double[] maxM = new double[p[0].length];
		double[] medianM = new double[p[0].length];
		double[] minM = new double[p[0].length];
		
		for(int i = 0; i < maxM.length; i++){
			double tmpMax = p[0][i];
			double tmpMin = p[0][i];
			for(int j = 0; j < p.length; j++){
				if(p[j][i] > tmpMax) tmpMax = p[j][i];
				if(p[j][i] < tmpMin) tmpMin = p[j][i];
			}
			maxM[i] = tmpMax;
			minM[i] = tmpMin;
			medianM[i] = median(p,i);
		}

		for(int i = 0; i < p.length; i++){
			distMax[i] = distance(p[i],maxM);
			distMin[i] = distance(p[i],minM);
			distMedian[i] = distance(p[i],medianM);
		}
	}
	
	private double median(double[][] p,int i)
	{
		double[] tmp = new double[p.length];
		
		for(int j = 0; j < p.length; j++)
			tmp[i] = p[j][i];
		
		Arrays.sort(tmp);
		
		return tmp[p.length/2];
	}

	// for now only euclidean
	private double distance(double[] p1,double[] p2)
	{
		double tmp = 0.0;

		for(int i = 0; i < p1.length; i++)
			tmp += (p1[i] - p2[i]) * (p1[i] - p2[i]);

		return Math.sqrt(tmp);
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

		if(distMax[0] < distMax[1]) return -1;
		else if(distMax[0] > distMax[1]) return 1;
		else return 0;
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
