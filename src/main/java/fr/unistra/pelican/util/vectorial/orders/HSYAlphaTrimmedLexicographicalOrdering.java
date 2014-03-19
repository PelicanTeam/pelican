package fr.unistra.pelican.util.vectorial.orders;


import java.util.Arrays;
import java.util.Comparator;

import fr.unistra.pelican.util.Tools;
import fr.unistra.pelican.util.vectorial.VectorPixel;

/**
 * This class represents a flexible lexicographical ordering scheme 
 * for double valued arrays and derivatives.
 * 
 * @author E.A.
 *
 */

public class HSYAlphaTrimmedLexicographicalOrdering implements VectorialOrdering,Comparator
{
	private double[] alphaV = null;
	private double alpha = 0.01;
	private double beta;
	private double refHue;
	
	/**
	 * 
	 * @param alpha
	 */
	public HSYAlphaTrimmedLexicographicalOrdering(double alpha,double refHue)
	{
		if(alpha > 0.0){
			this.alpha = alpha;
			this.beta = alpha;
		}
		else{
			this.alpha = 0.01;
			this.beta = 0.01;
		}
		
		this.refHue = refHue;
	}
	
	/**
	 * 
	 * @param alpha
	 */
	public HSYAlphaTrimmedLexicographicalOrdering(double alpha,double beta,double refHue)
	{
		if(alpha > 0.0) this.alpha = alpha;
		else this.alpha = 0.01;
		
		if(beta > 0.0) this.beta = beta;
		else this.beta = 0.01;
		
		this.refHue = refHue;
	}
	
	/**
	 * 
	 * @param alpha
	 */
	public HSYAlphaTrimmedLexicographicalOrdering(double[] alpha,double refHue)
	{
		for(int i = 0; i < alpha.length; i++){
			if(alpha[i] <= 0.0) alpha[i] = 0.01;
		}
		this.alphaV = alpha;
		
		this.refHue = refHue;
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

			if(Tools.doubleCompare(this.d,d.d) == -1) return -1;
			else if(Tools.doubleCompare(this.d,d.d) == 1) return 1;
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

			if(Tools.doubleCompare(this.d,d.d) == -1) return 1;
			else if(Tools.doubleCompare(this.d,d.d) == 1) return -1;
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
			
			alphaV[0] = 0.0; // bosver
			alphaV[1] = beta;
			alphaV[2] = alpha;
		}
		
		int dim = p[0].length;
		for(int d = 2; d >= 1; d--){
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
			if(trimSize <= 0) trimSize = 1;
			int tmp = trimSize;
			
			for(int i = tmp; i < p.length; i++){
				if(Tools.doubleCompare(p[i][d],p[tmp - 1][d]) == 0) trimSize++;
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
		// vectors left...in that case..a simple ordering...of hues
		IndexedDouble[] ind = new IndexedDouble[p.length];
		
		// compute hue distances
		for(int i = 0; i < ind.length; i++){
			double dist = 0.0;
			
			double abs = Math.abs(refHue - p[i][0]);
			
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
		if(alphaV == null){
			alphaV = new double[p[0].length];
			
			alphaV[0] = 0.0; // bosver
			alphaV[1] = beta;
			alphaV[2] = alpha;
		}
		
		int dim = 3;
		
		for(int d = 2; d >= 1; d--){
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
			if(trimSize <= 0) trimSize = 1;
			int tmp = trimSize;
			
			for(int i = tmp; i < p.length; i++){
				if(Tools.doubleCompare(p[i][d],p[tmp - 1][d]) == 0) trimSize++;
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
		
		// compute hue distances
		for(int i = 0; i < ind.length; i++){
			double dist = 0.0;
			
			double abs = Math.abs(refHue - p[i][0]);
			
			if(abs <= 0.5) dist = 0.5 - abs;
			else dist = 0.5 - 1.0 + abs;
			
			ind[i] = new ReverseIndexedDouble(dist,i);
		}
	
		// sort it
		Arrays.sort(ind);
		
		return p[ind[0].i];
	}

	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#rank(double[][], int)
	 */
	public double[] rank(double[][] p,int r)
	{	
		int dim = 3;
		
		for(int d = 2; d >= 1; d--){
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
				
				for(int i = lowerIndex; i <= upperIndex; i++)
					tmp2[i - lowerIndex] = p[ind[i].i];
			
				p = tmp2;
			}
		}
		
		// we've reached the last dimension and there are still more than one
		// vectors left...in that case..a simple ordering.
		IndexedDouble[] ind = new IndexedDouble[p.length];
		
		// compute hue distances
		for(int i = 0; i < ind.length; i++){
			double dist = 0.0;
			
			double abs = Math.abs(refHue - p[i][0]);
			
			if(abs <= 0.5) dist = 0.5 - abs;
			else dist = 0.5 - 1.0 + abs;
			
			ind[i] = new IndexedDouble(dist,i);
		}
		
		// sort it
		Arrays.sort(ind);
		
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
