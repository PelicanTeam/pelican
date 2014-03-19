package fr.unistra.pelican.util.vectorial.orders;

import java.util.Arrays;
import java.util.Comparator;

import fr.unistra.pelican.util.vectorial.VectorPixel;


/**
 * This class represents a vector ordering based on the euclidean norm of the vectors in question
 * 
 * @author E.A.
 *
 */

public class DynamicReferenceNormBasedOrdering implements VectorialOrdering,Comparator
{
	private double[] d = null;
	

	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#max(double[][])
	 */
	public double[] min(double[][] p)
	{
		preprocess(p,1);

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
	public double[] max(double[][] p)
	{
		preprocess(p,0);

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
		preprocess(p,0);

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

		preprocess(p,0);

		IndexedDouble[] id = new IndexedDouble[d.length];

		for(int i = 0; i < d.length; i++)
			id[i] = new IndexedDouble(d[i],i);

		Arrays.sort(id);

		for(int i = 0; i < v.length; i++)
			result[i] = v[id[i].i];

		return result;
	}

	private void preprocess(double[][] p,int flag)
	{
		// once ortalama vektoru bulalim
		double[] ort = new double[p[0].length];
		Arrays.fill(ort,0.0);
		
		for(int i = 0; i < p.length; i++)
			for(int j = 0; j < ort.length; j++)
				ort[j] += p[i][j];
		
		for(int j = 0; j < ort.length; j++)
			ort[j] = ort[j] / p.length;
		
		// simdi de koseyi bulalim..ort a kim en kisa mesafede..6 kose RGB'de..
		//double[][] koseler = {{0,0,0},{0,0,1},{0,1,0},{0,1,1},{1,0,0},{1,0,1},{1,1,0},{1,1,1}};
		double[][] koseler = {{0,0,1},{0,1,0},{0,1,1},{1,0,0},{1,0,1},{1,1,0},{1,1,1}};
		
		int min = 0;
		double minDistance = distance(koseler[min],ort);
		
		for(int i = 1; i < koseler.length; i++){
			double tmp = distance(koseler[i],ort);
			if(tmp < minDistance){
				min = i;
				minDistance = tmp; 
			}
		}
		
		if(flag == 1){
			koseler[min][0] = 1;
			koseler[min][1] = 1;
			koseler[min][2] = 1;
		}
		
		d = new double[p.length];

		for(int i = 0; i < p.length; i++)
			d[i] = distance(p[i],koseler[min]);
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

		preprocess(v,0);

		if(d[0] < d[1]) return -1;
		else if(d[0] > d[1]) return 1;
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
