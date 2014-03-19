package fr.unistra.pelican.util.vectorial.orders;

import java.util.Arrays;
import java.util.Comparator;

import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.Tools;


/**
 * This class represents a vector ordering based on constructing an image specific and only image filling curve.
 * 
 * Highly unstable...and for RGB only
 * 
 * @author E.A.
 *
 */

public class PathBasedOrdering1 implements VectorialOrdering,Comparator
{	
	private int[][][] order = new int[256][256][256];
	
	public PathBasedOrdering1(Image img)
	{
		int xdim = img.getXDim();
		int ydim = img.getYDim();
		
		int pixels = xdim * ydim;
		
		double[] ref = new double[3];
		ref[0] = ref[1] = ref[2] = 0.0;
		
		// ref en yakini bul ve onu yeni ref yap
		boolean[][] flags = new boolean[xdim][ydim];
		
		for(int i = 0; i < pixels; i++){
			if(i % 1000 == 0) System.err.println(i + " " + pixels);
			double dist = Double.MAX_VALUE;
			
			int x_max = 0;
			int y_max = 0;
			
			for(int x = 0; x < xdim; x++){
				for(int y = 0; y < ydim; y++){
					if(flags[x][y] == true) continue;
					
					double[] p = img.getVectorPixelXYZTDouble(x,y,0,0);
				
					double tmp = Tools.euclideanDistance(p,ref);
					if(tmp < dist){
						dist = tmp;
						x_max = x;
						y_max = y;
					}
				}
			}
			// en kucuk mesafedeki bulundu..yerlestirelim
			double[] p = img.getVectorPixelXYZTDouble(x_max,y_max,0,0);
			int r = (int)Math.round(p[0] * 255);
			int g = (int)Math.round(p[1] * 255);
			int b = (int)Math.round(p[2] * 255);
			
			order[r][g][b] = i;
			
			ref = p;
			
			flags[x_max][y_max] = true;
		}
	}

	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#max(double[][])
	 */
	public double[] max(double[][] p)
	{
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

	// for now only euclidean
	private double norm(double[] p)
	{
		double norm = 0.0;

		for(int i = 0; i < p.length; i++)
			norm += p[i] * p[i];

		return Math.sqrt(norm);
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

		System.err.println("Error : No compare function implemented");
			
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
