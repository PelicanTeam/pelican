package fr.unistra.pelican.util.vectorial.orders;

import java.util.Arrays;
import java.util.Comparator;

import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.Tools;
import fr.unistra.pelican.util.vectorial.VectorPixel;


/**
 * This class represents a vector ordering based on constructing an image specific and only image filling curve.
 * 
 * Highly unstable...and for RGB only
 * 
 * @author E.A.
 *
 */

public class PathBasedOrderingAlphaTrimmedHSY implements VectorialOrdering,Comparator
{	
	private int[][][] order = new int[256][100][120];		// L -> S -> dH
	private double refHue = 0.0;
	
	public PathBasedOrderingAlphaTrimmedHSY(Image img)
	{
		int xdim = img.getXDim();
		int ydim = img.getYDim();
		
		int pixels = xdim * ydim;
		
		// ref en yakini bul ve onu yeni ref yap
		boolean[][] flags = new boolean[xdim][ydim];
		
		double alpha;
		
		for(int i = 0; i < pixels; i++){
			if(i % 1000 == 0) System.err.println(i + " " + pixels);
			double dist = Double.MAX_VALUE;
			
			VectorPixel[] vects = new VectorPixel[pixels - i];
			int syc = 0;
			alpha = 0.000006866 * i + 0.000061035;
			
			for(int x = 0; x < xdim; x++){
				for(int y = 0; y < ydim; y++){
					if(flags[x][y] == true) continue;
					
					double[] p = img.getVectorPixelXYZTDouble(x,y,0,0);
				
					vects[syc++] = new VectorPixel(p,x,y);
				}
			}
			
			VectorPixel min = getAlphaTrimmedMin(vects,alpha);
			
			// en kucuk mesafedeki bulundu..yerlestirelim
			int l = (int)Math.round(min.vector[0] * 255);
			int s = (int)Math.round(min.vector[1] * 100);
			int h = (int)Math.round(min.vector[2] * 120);
			
			order[l][s][h] = i;
			
			flags[min.x][min.y] = true;
		}
	}
	
	public VectorPixel getAlphaTrimmedMin(VectorPixel[] p,double alpha)
	{
		double[] alphaV = new double[3];
		alphaV[0] = alpha;
		alphaV[1] = 0.45;
		alphaV[2] = 0.45;
		
		for(int d = 2; d >= 1; d--){
			// auxiliarry
			IndexedDouble[] ind = new IndexedDouble[p.length];
		
			// fill it
			for(int i = 0; i < ind.length; i++)
				ind[i] = new IndexedDouble(p[i].vector[d],i);
		
			// sort it
			Arrays.sort(ind);
			
			// keep first alpha %..IN the original array
			int trimSize = (int)Math.ceil(p.length * alphaV[d]);
			
			// get all vectors equal to the last one..
			// so that we can become equivament to lexico..
			// if for the dimension in progress there are more that are equal..
			int tmp = trimSize;
			
			for(int i = tmp; i < p.length; i++){
				if(Tools.doubleCompare(p[i].vector[d],p[tmp - 1].vector[d]) == 0) trimSize++;
				else break;
			}
			
			// if the first alpha % is too small..we're done.
			if(trimSize == 1) return p[ind[0].i];
			
			// else..once more
			VectorPixel[] tmp2 = new VectorPixel[trimSize];
			
			for(int i = 0; i < trimSize; i++)
				tmp2[i] = p[ind[i].i];
			
			p = tmp2;
		}
		
		// we've reached the last dimension and there are still more than one
		// vectors left...in that case..a simple ordering...of hues
		IndexedDouble[] ind = new IndexedDouble[p.length];
		
		// compute hue distances
		for(int i = 0; i < ind.length; i++){
			double dist = 0.0;
			
			double abs = Math.abs(refHue - p[i].vector[0]);
			
			if(abs <= 0.5) dist = 0.5 - abs;
			else dist = 0.5 - 1.0 + abs;
			
			ind[i] = new IndexedDouble(dist,i);
		}
	
		// sort it
		Arrays.sort(ind);
		
		return p[ind[0].i];
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
			int gmax = (int)Math.round(p[max][1] * 100.0);
			int bmax = (int)Math.round(p[max][2] * 120.0);
			
			int r = (int)Math.round(p[i][0] * 255.0);
			int g = (int)Math.round(p[i][1] * 100.0);
			int b = (int)Math.round(p[i][2] * 120.0);
			
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
			int gmin = (int)Math.round(p[min][1] * 100.0);
			int bmin = (int)Math.round(p[min][2] * 120.0);
			
			int r = (int)Math.round(p[i][0] * 255.0);
			int g = (int)Math.round(p[i][1] * 100.0);
			int b = (int)Math.round(p[i][2] * 120.0);
			
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
		IndexedInt[] id = new IndexedInt[p.length];

		for(int i = 0; i < p.length; i++){
			int r = (int)Math.round(p[i][0] * 255.0);
			int g = (int)Math.round(p[i][1] * 100.0);
			int b = (int)Math.round(p[i][2] * 120.0);
			
			id[i] = new IndexedInt(order[r][g][b],i);
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

	private class IndexedInt implements Comparable
	{
		int i;
		
		int label;

		IndexedInt(int label,int i)
		{
			this.label = label;
			this.i = i;
		}

		public int compareTo(Object o){
			IndexedInt d = (IndexedInt)o;

			if(this.label < d.label) return -1;
			else if(this.label > d.label) return 1;
			else return 0;
		}
	}
	
	private class IndexedDouble implements Comparable
	{
		int i;
		
		double d;

		IndexedDouble(double d,int i)
		{
			this.d = d;
			this.i = i;
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
