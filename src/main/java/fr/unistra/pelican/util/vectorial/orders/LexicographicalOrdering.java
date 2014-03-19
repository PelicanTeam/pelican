package fr.unistra.pelican.util.vectorial.orders;

import java.util.Arrays;
import java.util.Comparator;

import fr.unistra.pelican.util.Tools;
import fr.unistra.pelican.util.vectorial.VectorPixel;

/**
 * This class represents a lexicographical ordering scheme for double valued arrays and derivatives.
 * 
 * @author E.A.
 *
 */

public class LexicographicalOrdering implements BinaryVectorialOrdering,Comparator
{
	/**
	 * number of comparisons
	 */
	public int comps = 0;
	
	/**
	 * number of equalities
	 */
	public int equals = 0;
	
	/**
	 * number of comparisons determined by each dimension
	 */
	public int[] dims = new int[3];
	
	public LexicographicalOrdering() {
	}
	
	public LexicographicalOrdering (int dims) {
		this.dims=new int[dims];
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
			if(this.compare(min,p[i]) > 0) 
				min = p[i];
		}

		return min;
	}

	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#rank(double[][], int)
	 */
	public double[] rank(double[][] p,int r)
	{
		Arrays.sort(p,this);

		return p[r];
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
		// one more comparison
		comps++;
		
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
			if(Tools.doubleCompare(p1[i],p2[i]) == -1){
			dims[i]++;
				return -1;
			}
			else if(Tools.doubleCompare(p1[i],p2[i]) == 1){
				dims[i]++;
				return 1;
			}
		}
		
		// one more equality
		equals++;
		
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
