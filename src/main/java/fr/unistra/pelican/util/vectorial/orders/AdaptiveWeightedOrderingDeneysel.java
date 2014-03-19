package fr.unistra.pelican.util.vectorial.orders;

import java.util.Arrays;
import java.util.Comparator;

import fr.unistra.pelican.util.vectorial.VectorPixel;


/**
 * This class represents a vector ordering designed for Hue Saturation Luminance/Lightness type spaces.
 * It consists of a weighted sum of its components. The dimension order is assumed as H-> S-> L.
 * 
 * @author E.A.
 *
 */

public class AdaptiveWeightedOrderingDeneysel implements VectorialOrdering,Comparator
{
	private double[] d = null;
	private double a = 0;
	private double b = 0;
	private double c = 0;
	
	private double t = 0;
	
	private double j;
	
	public double syc = 0;
	public double esitlik = 0;
	public double esitlik2 = 0;
	
	private double refHue;
	
	public AdaptiveWeightedOrderingDeneysel(double refHue)
	{
		this.refHue = refHue;
		t = 5.0;
		j = 0.5;
	}
	
	public AdaptiveWeightedOrderingDeneysel(double t,double refHue,double j)
	{
		this.refHue = refHue;
		
		this.t = t;	
		this.j = j;

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
	
	private double scalarize(double[] z)
	{	
		double s,y,h;
		
		c = 1.0;
		y = c * z[2];
		
		b = 1.0;
		s = b * z[1];
		
		a = 1 / ( 1 + Math.exp(-1.0 * t * (s - j)));
		
		double abs = Math.abs(refHue - z[0]);	// \in [0,1[
		
		if(abs <= 0.5) h = 2 * a * (0.5 - abs); // all 3 components must be in the [0,1] interval
		else h = 2 * a * (0.5 - 1.0 + abs);
		
		return Math.sqrt(y * y + 0.0 * s * s + 0.0 * h * h);
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
		
		syc++;

		if(d[0] < d[1]) return -1;
		else if(d[0] > d[1]) return 1;
		else{
			esitlik++;
			if(v[0][0] == v[1][0] && v[0][1] == v[1][1] && v[0][2] == v[1][2]) esitlik2++;
			
			return 0;
		}
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
