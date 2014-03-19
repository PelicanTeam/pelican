package fr.unistra.pelican.util.vectorial.orders;

import java.util.Arrays;
import java.util.Comparator;

import fr.unistra.pelican.util.Tools;
import fr.unistra.pelican.util.vectorial.VectorPixel;


/**
 * This class represents a vector ordering designed for Hue Saturation Luminance/Lightness type spaces.
 * It consists of a weighted sum of its components. The dimension order is assumed as H-> S-> L.
 * 
 * @author E.A.
 *
 */

public class ModelBasedWeightedOrdering2 implements VectorialOrdering,Comparator
{
	private double[] d = null;
	private double offset;
	private double slope;
	
	private double refHue;
	
	public ModelBasedWeightedOrdering2(double refHue)
	{
		this.refHue = refHue;
		offset = 0.5;
		slope = 5.0;
	}
	
	public ModelBasedWeightedOrdering2(double refHue,double offset,double slope)
	{
		this.refHue = refHue;
		this.offset = offset;
		this.slope = slope;
	}

	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#max(double[][])
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
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#min(double[][])
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

	// scalarize every vector independently
	private void preprocess(double[][] p)
	{
		d = new double[p.length];
		
		for(int i = 0; i < d.length; i++)
			
			d[i] = scalarize(p[i]);
	}
	
	double scalarize(double[] z)
	{	
		double y = 0, chrom = 0;
		
		double alpha,beta;
		
		if(z[2] <= 0.5) beta =  2/(1 + Math.exp(-10 * (z[2]-0.5)));  
		else beta = 2/(1 + Math.exp(10 * (z[2]-0.5))); 
		
		alpha = 1 / (1 + Math.exp(-1 * slope * (z[1] - offset))) * beta;
		
		
		chrom = 2 * (0.5 - Tools.hueDistance(refHue,z[0]));
		
		return (1 - alpha) * z[2] + alpha * chrom;
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
			
			if(Math.abs(this.d - d.d) < Tools.epsilon) return 0;
			else if(this.d < d.d) return -1;
			else return 1;
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
		
		if(Math.abs(d[0] - d[1]) < Tools.epsilon) return 0;
		else if(d[0] < d[1]) return -1;
		else return 1;
		/*
		else{
			esitlik++;
			if(v[0][0] == v[1][0] && v[0][1] == v[1][1] && v[0][2] == v[1][2]) esitlik2++;
			
			return 0;
		}
		*/
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
