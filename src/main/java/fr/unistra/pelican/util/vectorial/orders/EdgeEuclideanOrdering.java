package fr.unistra.pelican.util.vectorial.orders;

import java.util.Comparator;

import fr.unistra.pelican.util.Tools;


/**
 * This class represents a vector ordering for the RGB colour space where the max
 * is the vector closest to 1,1,1 and the min the closest to 0,0,0.
 * 
 * @author E.A.
 *
 */

public class EdgeEuclideanOrdering implements VectorialOrdering,Comparator
{
	private double[] max;
	private double[] min;

	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#max(double[][])
	 */
	public double[] max(double[][] p)
	{
		preprocess(p);

		return max;
	}

	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#min(double[][])
	 */
	public double[] min(double[][] p)
	{
		preprocess(p);

		return min;
	}

	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#rank(double[][], int)
	 */
	public double[] rank(double[][] p,int r)
	{
		System.err.println("rank not supported");
		return null;
	}

	private void preprocess(double[][] p)
	{
		double distB = Double.MAX_VALUE;
		double distS = Double.MAX_VALUE;
		
		double[] b = {1,1,1};
		double[] s = {0,0,0};
		
		for(int i = 0; i < p.length; i++){
			double tmp = Tools.euclideanDistance(b,p[i]); 
			if(tmp < distB){
				distB = tmp;
				max = p[i];
			}
			tmp = Tools.euclideanDistance(s,p[i]); 
			if(tmp < distS){
				distS = tmp;
				min = p[i];
			}
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
		System.err.println("binary comparison not supported");
		return 0;
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
