package fr.unistra.pelican.util.vectorial.orders;

import java.util.Arrays;
import java.util.Comparator;

import Jama.Matrix;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.vectorial.VectorPixel;


/**
 * This class represents a vector ordering operating in a marginal fashion
 * and then replacing the marginal extremum with the closest initial vector 
 * to it in terms of a Mahalanobis distance.
 * @author E.A.
 *
 */

public class MarginalWithMahalanobisReplacementOrdering implements VectorialOrdering,Comparator
{
	private double[] dmax = null;
	private double[] dmin = null;
	
	private double[][] sigma = null;
	
	public MarginalWithMahalanobisReplacementOrdering(Image img)
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

		int min = 0;

		for(int i = 1; i < p.length; i++){
			if(dmax[min] > dmax[i]) min = i;
		}

		return p[min];
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
			if(dmin[min] > dmin[i]) min = i;
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
		IndexedDouble[] id = new IndexedDouble[dmax.length];

		for(int i = 0; i < dmax.length; i++)
			id[i] = new IndexedDouble(dmax[i],i);

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

		IndexedDouble[] id = new IndexedDouble[dmax.length];

		for(int i = 0; i < dmax.length; i++)
			id[i] = new IndexedDouble(dmax[i],i);

		Arrays.sort(id);

		for(int i = 0; i < v.length; i++)
			result[i] = v[id[i].i];

		return result;
	}

	private void preprocess(double[][] p)
	{
		// compute the marginal max and min of the vectors
		double[] max = new double[p[0].length];
		double[] min = new double[p[0].length];
		
		for(int i = 0; i < p[0].length; i++){
			max[i] = p[0][i];
			min[i] = p[0][i];
			for(int j = 1; j < p.length; j++){
				if(p[j][i] > max[i]) max[i] = p[j][i];
				if(p[j][i] < min[i]) min[i] = p[j][i];
			}
		}
		
		// compute the distances of the vectors from the marginal max and min
		dmax = new double[p.length];
		dmin = new double[p.length];
		
		for(int i = 0; i < p.length; i++){
			dmax[i] = distance(max,p[i]);
			dmin[i] = distance(min,p[i]);
		}
	}
	
	private double distance(double[] p1,double[] p2)
	{
		double norm = 0.0;
		double[] p = new double[p1.length];
		
		for(int i = 0; i < p.length; i++)
			p[i] = p1[i] - p2[i];
		
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

		if(dmax[0] < dmax[1]) return -1;
		else if(dmax[0] > dmax[1]) return 1;
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
