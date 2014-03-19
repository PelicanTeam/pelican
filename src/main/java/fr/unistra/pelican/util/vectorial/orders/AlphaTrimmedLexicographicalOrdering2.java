package fr.unistra.pelican.util.vectorial.orders;

import java.util.Arrays;
import java.util.Comparator;

import fr.unistra.pelican.util.vectorial.VectorPixel;

/**
 * This class represents a flexible lexicographical ordering scheme 
 * for double valued arrays and derivatives.
 * 
 * @author E.A.
 *
 */

public class AlphaTrimmedLexicographicalOrdering2 implements VectorialOrdering,Comparator
{
	private double[] alphaV = null;
	private double alpha = 0.01;
	
	/**
	 * 
	 * @param alpha
	 */
	public AlphaTrimmedLexicographicalOrdering2(double alpha)
	{
		if(alpha > 0.0) this.alpha = alpha;
		else this.alpha = 0.01;
	}
	
	/**
	 * 
	 * @param alpha
	 */
	public AlphaTrimmedLexicographicalOrdering2(double[] alpha)
	{
		for(int i = 0; i < alpha.length; i++){
			if(alpha[i] <= 0.0) alpha[i] = 0.01;
		}
		this.alphaV = alpha;
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
	
	private class ReverseIndexedDouble implements Comparable
	{
		double d;
		int i;

		ReverseIndexedDouble(double d,int i)
		{
			this.d = d; this.i = i;
		}

		public int compareTo(Object o){
			ReverseIndexedDouble d = (ReverseIndexedDouble)o;

			if(this.d < d.d) return 1;
			else if(this.d > d.d) return -1;
			else return 0;
		}
	}
	
	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#max(double[][])
	 */
	public double[] min(double[][] p)
	{
		if(alphaV == null){
			alphaV = new double[p[0].length];
			for(int i = 0; i < alphaV.length; i++)
				alphaV[i] = alpha;
		}
		
		int dim = p[0].length;
		
		// we need an array holding the smallest, not taken into account, 
		// vector components of each dimension except for the last
		double[] ref = new double[dim - 1];
		
		for(int d = 0; d < dim - 1; d++){
			// auxiliarry
			IndexedDouble[] ind = new IndexedDouble[p.length];
		
			// fill it
			for(int i = 0; i < ind.length; i++)
				ind[i] = new IndexedDouble(p[i][d],i);
		
			// sort it
			Arrays.sort(ind);
			
			// keep first alpha %..IN the original array
			int trimSize = (int)Math.ceil(p.length * alphaV[d]);
			
			// get all vectors equal to the last one..
			// so that we can become equivament to lexico..
			// if for the dimension in progress there are more that are equal..
			int tmp = trimSize;
			
			// only if not all are taken...
			if(tmp < p.length - 1) ref[d] = p[tmp][d];
			else ref[d] = 0.0;
			
			for(int i = tmp; i < p.length; i++){
				if(p[i][d] == p[tmp - 1][d]) trimSize++;
				else break;
			}
			
			// if the first alpha % is too small..we're done.
			if(trimSize == 1) return p[ind[0].i];
			
			// else..once more
			double[][] tmp2 = new double[trimSize][dim];
			
			for(int i = 0; i < trimSize; i++)
				tmp2[i] = p[ind[i].i];
			
			p = tmp2;
		}
		
		// we've reached the last dimension and there are still more than one
		// vectors left...in that case..a simple ordering.
		IndexedDouble[] ind = new IndexedDouble[p.length];
		
		// add up their distances from the ref of each dimension
		// FIXME all values are presumed to be normalized
		for(int i  = 0; i < ind.length; i++){
			for(int d = 0; d < dim - 1; d++)
				p[i][dim - 1] += Math.abs(p[i][dim - 1] - ref[d]);
		}
		
		// fill it
		for(int i = 0; i < ind.length; i++)
			ind[i] = new IndexedDouble(p[i][dim - 1],i);
	
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
		if(alphaV == null){
			alphaV = new double[p[0].length];
			for(int i = 0; i < alphaV.length; i++)
				alphaV[i] = alpha;
		}
		
		int dim = p[0].length;
		
		// we need an array holding the largest, not taken into account, 
		// vector components of each dimension except for the last
		double[] ref = new double[p[0].length - 1];
		
		for(int d = 0; d < dim - 1; d++){
			// auxiliarry
			ReverseIndexedDouble[] ind = new ReverseIndexedDouble[p.length];
		
			// fill it
			for(int i = 0; i < ind.length; i++)
				ind[i] = new ReverseIndexedDouble(p[i][d],i);
		
			// sort it
			Arrays.sort(ind);
			
			// reverse order
			
			// keep first alpha %..IN the original array
			int trimSize = (int)Math.ceil(p.length * alphaV[d]);
			
			// get all vectors equal to the last one..
			// so that we can become equivament to lexico..
			// if for the dimension in progress there are more that are equal..
			int tmp = trimSize;
			
			if(tmp < p.length - 1) ref[d] = p[tmp][d];
			else ref[d] = 0.0;
				
			for(int i = tmp; i < p.length; i++){
				if(p[i][d] == p[tmp - 1][d]) trimSize++;
				else break;
			}
			
			// if the first alpha % is too small..we're done.
			if(trimSize == 1) return p[ind[0].i];
			
			// else..once more
			double[][] tmp2 = new double[trimSize][dim];
			
			for(int i = 0; i < trimSize; i++)
				tmp2[i] = p[ind[i].i];
			
			p = tmp2;
		}
		
		// we've reached the last dimension and there are still more than one
		// vectors left...in that case..a simple ordering.
		ReverseIndexedDouble[] ind = new ReverseIndexedDouble[p.length];
		
		double[] tmp = new double[ind.length];
		
		// add up their distances from the ref of each dimension
		for(int i  = 0; i < ind.length; i++){
			tmp[i] = p[i][dim - 1];
			for(int d = 0; d < dim - 1; d++)
				tmp[i] += Math.abs(p[i][dim - 1] - ref[d]);
		}
		
		// fill it
		for(int i = 0; i < ind.length; i++)
			ind[i] = new ReverseIndexedDouble(tmp[i],i);
	
		// sort it
		Arrays.sort(ind);
		
		//System.err.println("done at dim " + dim);
		
		return p[ind[0].i];
	}

	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#rank(double[][], int)
	 */
	public double[] rank(double[][] p,int r)
	{	
		int dim = p[0].length;
		
		for(int d = 0; d < dim - 1; d++){
			// auxiliarry
			IndexedDouble[] ind = new IndexedDouble[p.length];
		
			// fill it
			for(int i = 0; i < ind.length; i++)
				ind[i] = new IndexedDouble(p[i][d],i);
		
			// sort it
			Arrays.sort(ind);
			
			// eliminate the upper and lower alpha %
			// not ceil...floor...we favor having less trimmed
			int trimSize = (int)Math.floor(p.length * alpha);
			
			if(trimSize > 0){
				
				int lowerIndex = trimSize;
				int upperIndex = p.length - trimSize - 1;
			
				// total number to take
				if(upperIndex < lowerIndex){
					int tmp2 = upperIndex;
					
					upperIndex = lowerIndex;
					lowerIndex = tmp2;
				}
				
				int total = upperIndex - lowerIndex + 1;
			
				// if the first alpha % is too small..we're done.
				if(upperIndex == lowerIndex) return p[ind[upperIndex].i];
			
				// else..once more
				double[][] tmp2 = new double[total][dim];
				//System.err.println(lowerIndex + " " + upperIndex + " " + p.length + " " + trimSize);
				
				for(int i = lowerIndex; i <= upperIndex; i++)
					tmp2[i - lowerIndex] = p[ind[i].i];
			
				p = tmp2;
			}
		}
		
		// we've reached the last dimension and there are still more than one
		// vectors left...in that case..a simple ordering.
		IndexedDouble[] ind = new IndexedDouble[p.length];
		
		// fill it
		for(int i = 0; i < ind.length; i++)
			ind[i] = new IndexedDouble(p[i][dim - 1],i);
	
		// sort it
		Arrays.sort(ind);
		
		//System.err.println(p.length);
		
		return p[ind[p.length/2].i];
	}

	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#order(fr.unistra.pelican.util.vectorial.VectorPixel[])
	 */
	public VectorPixel[] order(VectorPixel[] v)
	{
		Arrays.sort(v,this);

		return v;
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
		double[] p1 = null,p2 = null;

		try{
			if(o1.getClass().getName().equals("[D")){
				p1 = (double[])o1;
				p2 = (double[])o2;
			}else if(o1.getClass().getName().equals("fr.unistra.pelican.util.vectorial.VectorPixel")){
				p1 = ((VectorPixel)o1).getVector();
				p2 = ((VectorPixel)o2).getVector();
				
			}else throw new ClassCastException();

		}catch(ClassCastException ex){
			ex.printStackTrace();
		}

		for(int i = 0; i < p1.length; i++){
			if(p1[i] < p2[i]) return -1;
			else if(p1[i] > p2[i]) return 1;
		}
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
