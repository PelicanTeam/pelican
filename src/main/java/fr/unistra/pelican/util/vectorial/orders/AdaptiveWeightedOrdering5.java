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

public class AdaptiveWeightedOrdering5 implements VectorialOrdering,Comparator
{
	private double[] d = null;
	
	// weights for each dimension.
	private double a;
	private double b;
	private double c;
	
	// hue slope
	private double slope;
	
	// hue offset
	private double offset;
	
	// sat slope
	private double slope2;
	
	// sat offset
	private double offset2;
	
	private double coeff = 1.0;
	
	public double syc = 0;
	public double esitlik = 0;
	public double esitlik2 = 0;
	
	private double refHue;
	
	public AdaptiveWeightedOrdering5(double refHue)
	{
		this.refHue = refHue;
		slope = 5.0;
		offset = 0.5;
		
		slope2 = 5.0;
		offset2 = 0.5;
		
		a = b = c = 1.0;
	}
	
	public AdaptiveWeightedOrdering5(double offset,double slope,double coeff,double refHue)
	{
		this.refHue = refHue;
		
		this.slope = slope;
		this.offset = offset;
		this.coeff = coeff;
		
		slope2 = 5.0;
		offset2 = 0.5;
		
		a = b = c = 1.0;
	}
	
	public AdaptiveWeightedOrdering5(double offset,double slope,double refHue,double offset2,double slope2)
	{
		this.refHue = refHue;
		
		this.slope = slope;
		this.offset = offset;
		
		this.slope2 = slope2;
		this.offset2 = offset2;
		
		a = b = c = 1.0;
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
	
	double difference(double[] p1,double[] p2)
	{
		double h1,s1,y1;
		double h2,s2,y2;
		
		// luminances
		y1 = c * p1[2];
		y2 = c * p2[2];
		
		double ydiff = y1 - y2;
		
		// saturations
		s1 = b * p1[1] * 1 / (( 1 + Math.exp(1.0 * slope2 * (y1 - offset2)))/* * ( 1 + Math.exp(-1.0 * slope * (p1[1] - offset)))*/);
		s2 = b * p2[1] * 1 / (( 1 + Math.exp(1.0 * slope2 * (y2 - offset2)))/* * ( 1 + Math.exp(-1.0 * slope * (p2[1] - offset)))*/);
		
		double sdiff = s1 - s2;
		
		// hues
		double tmp1 = 1;// / ( 1 + Math.exp(-1.0 * slope * (s1 - offset)));
		double tmp2 = 1; /// ( 1 + Math.exp(-1.0 * slope * (s2 - offset)));
		
		double abs1 = Math.abs(refHue - p1[0]);	// \in [0,1[
		double abs2 = Math.abs(refHue - p2[0]);	// \in [0,1[
		
		if(abs1 <= 0.5) h1 = 2 * a * tmp1 * (0.5 - abs1); // all 3 components must be in the [0,1] interval
		else h1 = 2 * a * tmp1 * (0.5 - 1.0 + abs1);
		
		if(abs2 <= 0.5) h2 = 2 * a * tmp2 * (0.5 - abs2); // all 3 components must be in the [0,1] interval
		else h2 = 2 * a * tmp2 * (0.5 - 1.0 + abs2);
		
		double hdiff = h1 - h2;
		
		//return Math.sqrt(ydiff * ydiff + sdiff * sdiff + 0.0 * hdiff * hdiff);
		return Math.sqrt(ydiff * ydiff * slope2 * slope2 + sdiff * sdiff * slope * slope);
		
	}
	
	double scalarize(double[] z)
	{	
		double h = 0,s = 0,y = 0;

		// luminance
		//y = c * z[2] * (1 / (2 + 2 * Math.exp(1.0 * slope * (z[1] - offset))) + 0.5);
		
		// saturation
		//s = b * z[1] * y;//1 / (( 1 + Math.exp(-1.0 * slope2 * (y - offset2))) * ( 1 + Math.exp(-1.0 * slope * (z[1] - offset))));
		//s = b * z[1] * (Math.exp(z[2])  - 1.0)/ (Math.E - 1.0);
		//s = (Math.exp(z[1]) - 1.0)/(Math.E - 1.0);
		
		s = z[1];
		
		// hue
		double tmp = coeff / ( 1 + Math.exp(-1.0 * slope * (s - offset)));
		
		
		double abs = Math.abs(refHue - z[0]);	// \in [0,1[
		
		// ref e olan mesafe if den sonra..\in [0,0.5]
		// dilation un en uzaktakini degil de en yakindakini vermesi icin
		// mesafeyi tersine almali...yani 0.5 i ondan cikarmali.
		// ayrica 2 ile carpmali ki toplam deger \in [0,1] olsun
		
		if(abs <= 0.5) h = 2 * a * tmp *(0.5 - abs); // all 3 components must be in the [0,1] interval
		else h = 2 * a * tmp * (0.5 - 1.0 + abs);
		
		return Math.sqrt(y * y * 0.0 + s * s * 0.0 + h * h);
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
		
		syc++;
		
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
