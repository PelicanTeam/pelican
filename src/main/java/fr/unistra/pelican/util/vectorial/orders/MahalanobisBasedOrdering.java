package fr.unistra.pelican.util.vectorial.orders;

import java.util.Arrays;
import java.util.Comparator;

import Jama.Matrix;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.vectorial.VectorPixel;


/**
 * This class represents an ordering based on the Mahalanobis distance from the cartesian origin.
 * 
 * @author E.A.
 *
 */

public class MahalanobisBasedOrdering implements VectorialOrdering,Comparator
{
	private double[] d = null;
	private double[][] sigma = null;
	
	public MahalanobisBasedOrdering(Image img)
	{
		int bDim = img.getBDim();
		int xDim = img.getXDim();
		int yDim = img.getYDim();
		
		sigma = new double[bDim][bDim];
		
		// transform the channels into columns of a 2D matrix...
		int size = xDim * yDim;
		
		double[][] pixels = new double[bDim][xDim * yDim];
		
		for(int b = 0; b < bDim; b++)
			for(int x = 0; x < xDim; x++)
				for(int y = 0; y < yDim; y++)
					pixels[b][y * xDim + x] = img.getPixelXYBDouble(x,y,b);
		
		// get the mean of each dimension
		double[] mean = new double[bDim];

		for(int i = 0; i < bDim; i++){
			mean[i] = 0.0;
			for(int j = 0; j < xDim * yDim; j++)
				mean[i] += pixels[i][j];
			mean[i] = mean[i] / size;
		}
		
		// substract the mean from the input..
		for(int i = 0; i < bDim; i++)
			for(int j = 0; j < size; j++)
				pixels[i][j] = pixels[i][j] - mean[i];
		
		// covariance matrix : multiply with transpose
		Matrix M = new Matrix(pixels,bDim,size);
		Matrix Mt = M.transpose();
		M = M.times(Mt);
		
		// then normalize
		M = M.times(1/(double)(size - 1));
		
		// and inverse
		M = M.inverse();
		
		// were done
		sigma = M.getArray();
	}

	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#max(double[][])
	 */
	public double[] max(double[][] p)
	{
		preprocess(p);

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
	public double[] min(double[][] p)
	{
		preprocess(p);

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

	private void preprocess(double[][] p)
	{
		d = new double[p.length];

		for(int i = 0; i < p.length; i++)
			d[i] = norm(p[i]);
	}

	private double norm(double[] p)
	{
		double norm = 0.0;
		double[] v = new double[p.length];
		
		for(int i = 0; i < p.length; i++)
			for(int j = 0; j < p.length; j++)
				v[i] += p[j] * sigma[j][i];
		
		for(int i = 0; i < p.length; i++)
			norm += v[i] * p[i];

		return Math.sqrt(norm);
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

		if(d[0] < d[1]) return -1;
		else if(d[0] > d[1]) return 1;
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
