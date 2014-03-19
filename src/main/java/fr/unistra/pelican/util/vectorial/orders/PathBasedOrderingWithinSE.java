package fr.unistra.pelican.util.vectorial.orders;

import java.util.Arrays;
import java.util.Comparator;

import fr.unistra.pelican.util.Tools;


/**
 * This class represents a vector ordering based on constructing an image specific and only image filling curve.
 * 
 * Highly unstable...and for RGB only
 * 
 * @author E.A.
 *
 */

public class PathBasedOrderingWithinSE implements VectorialOrdering,Comparator
{	
	private int[][][] order = new int[256][256][256];

	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#max(double[][])
	 */
	public double[] max(double[][] p)
	{
		preprocess(p);
		
		int max = 0;

		for(int i = 1; i < p.length; i++){
			int rmax = (int)Math.round(p[max][0] * 255.0);
			int gmax = (int)Math.round(p[max][1] * 255.0);
			int bmax = (int)Math.round(p[max][2] * 255.0);
			
			int r = (int)Math.round(p[i][0] * 255.0);
			int g = (int)Math.round(p[i][1] * 255.0);
			int b = (int)Math.round(p[i][2] * 255.0);
			
			if(order[rmax][gmax][bmax] < order[r][g][b]) max = i;
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
			int rmin = (int)Math.round(p[min][0] * 255.0);
			int gmin = (int)Math.round(p[min][1] * 255.0);
			int bmin = (int)Math.round(p[min][2] * 255.0);
			
			int r = (int)Math.round(p[i][0] * 255.0);
			int g = (int)Math.round(p[i][1] * 255.0);
			int b = (int)Math.round(p[i][2] * 255.0);
			
			if(order[rmin][gmin][bmin] > order[r][g][b]) min = i;
		}

		return p[min];
	}

	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#rank(double[][], int)
	 */
	public double[] rank(double[][] p,int rank)
	{
		preprocess(p);
		
		IndexedDouble[] id = new IndexedDouble[p.length];

		for(int i = 0; i < p.length; i++){
			int r = (int)Math.round(p[i][0] * 255.0);
			int g = (int)Math.round(p[i][1] * 255.0);
			int b = (int)Math.round(p[i][2] * 255.0);
			
			id[i] = new IndexedDouble(order[r][g][b],i);
		}

		Arrays.sort(id);

		return p[id[rank].i];
	}
	
	private void reset()
	{
		for(int i = 0; i < 256; i++){
			for(int j = 0; j < 256; j++){
				for(int k = 0; k < 256; k++){
					order[i][j][k] = 0;
				}
			}
		}
	}

	private void preprocess(double[][] p)
	{
		// temizle icersini
		//reset();
		
		// evet simdi SE icinde path olayini yapalim..bakalim ne olacak
		// 0,0,0 dan baslayacagiz...evet. en kucuk o olmali.
		
		double[] ref = new double[3];
		ref[0] = ref[1] = ref[2] = 0.0;
		
		// ref en yakini bul ve onu yeni ref yap
		boolean[] flags = new boolean[p.length];
		Arrays.fill(flags,true);
		
		for(int i = 0; i < p.length; i++){
			double dist = Double.MAX_VALUE;
			
			int k = 0;
			
			for(int j = 0; j < p.length; j++){
				if(flags[j] == false) continue;
				
				double tmp = Tools.euclideanDistance(p[j],ref);
				if(tmp < dist){
					dist = tmp;
					k = j;
				}
			}
			// en kucuk mesafedeki bulundu..yerlestirelim
			int r = (int)Math.round(p[k][0] * 255);
			int g = (int)Math.round(p[k][1] * 255);
			int b = (int)Math.round(p[k][2] * 255);
			
			order[r][g][b] = i;
			
			ref = p[k];
			
			flags[k] = false;
		}
	}

	private class IndexedDouble implements Comparable
	{
		int i;
		
		int label;

		IndexedDouble(int label,int i)
		{
			this.label = label;
			this.i = i;
		}

		public int compareTo(Object o){
			IndexedDouble d = (IndexedDouble)o;

			if(this.label < d.label) return -1;
			else if(this.label > d.label) return 1;
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
		System.err.println("no comparing function implemented");
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
