package fr.unistra.pelican.util.vectorial.orders;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;

import fr.unistra.pelican.util.Tools;


/**
 * 
 *
 */

public class MultipleHueOrdering implements VectorialOrdering,Comparator
{
	private double[] d = null;
	private Vector refs;
	private double sum = -1.0;
	
	/**
	 * 
	 * @param v
	 */
	public MultipleHueOrdering(Vector v)
	{
		this.refs = v;
	}

	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#max(double[][])
	 */
	public double[] min(double[][] p)
	{
		double[] min = p[0];

		for(int i = 1; i < p.length; i++){
			if(this.compare(min,p[i]) > 0) min = p[i];
		}

		return min;
	}

	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#min(double[][])
	 */
	public double[] max(double[][] p)
	{
		double[] max = p[0];

		for(int i = 1; i < p.length; i++){
			if(this.compare(max,p[i]) < 0) max = p[i];
		}

		return max;
	}

	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#rank(double[][], int)
	 */
	public double[] rank(double[][] p,int r)
	{
		Arrays.sort(p,this);

		return p[r];
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

		// her ref hue ile mesafelerini hesapla..tabii agirliklari unutmadan
		// kiminki daha kucuk ise o daha buyuktur.
		double d1 = 1.0;
		double d2 = 1.0;
		
		if(sum < 0.0){
			sum = 0.0;
			
			for(int i = 0; i < refs.size(); i++){
				double[] tmp = (double[])refs.get(i);
				sum += tmp[1];
			}
		}
		
		for(int i = 0; i < refs.size(); i++){
			double[] tmp = (double[])refs.get(i);
			
			// important the cluster => smaller d
			double weightedDistance = Tools.hueDistance(v[0][0],tmp[0]) * sum / tmp[1];		// with weights
			//double weightedDistance = Tools.hueDistance(v[0][0],tmp[0]);	// no weights
			
			if(weightedDistance < d1) d1 = weightedDistance;
			
			weightedDistance = Tools.hueDistance(v[1][0],tmp[0]) * sum / tmp[1];	// with weights
			//weightedDistance = Tools.hueDistance(v[1][0],tmp[0]); // no weights
			
			if(weightedDistance < d2) d2 = weightedDistance;
		}
		
		// smallest d wins ! ! !
		if(Tools.doubleCompare(d1,d2) == -1) return 1;
		else if(Tools.doubleCompare(d1,d2) == 1) return -1;
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
