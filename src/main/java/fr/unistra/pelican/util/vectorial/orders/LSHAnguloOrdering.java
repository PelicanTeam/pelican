package fr.unistra.pelican.util.vectorial.orders;

import java.util.Arrays;
import java.util.Comparator;

import fr.unistra.pelican.util.vectorial.VectorPixel;

/**
 * This class represents a lexicographical LSH ordering scheme
 * according to the proposal of Angulo in his thesis.
 * 
 * the dimensions of the input vectors are considered in the order : H S L (or Y)
 * 
 * @author E.A.
 *
 */

public class LSHAnguloOrdering implements VectorialOrdering,Comparator
{
	private double alpha;		// dynamic range limiter for the luminance component
	private double refHue;		// hue reference
	
	/**
	 * 
	 * @param alpha
	 */
	public LSHAnguloOrdering(double alpha,double refHue)
	{
		if(alpha <= 0.0) alpha = 1.0;
		if(refHue > 1.0 || refHue < 0.0) refHue = 0.0;
		
		this.alpha = alpha;
		this.refHue = refHue;
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
		Arrays.sort(p,this);

		return p[r];
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
		
		double tmp1,tmp2;
		
		// limit the ranges for the luminance dimension
		tmp1 = Math.ceil((p1[2] * 255.0) / alpha) / 255.0;
		tmp2 = Math.ceil((p2[2] * 255.0) / alpha) / 255.0;
		
		if(tmp1 < tmp2) return -1;
		else if(tmp1 > tmp2) return 1;
		
		// inverse comparison for saturation...we favor less saturated colours...???
		if(p1[1] < p2[1]) return 1;
		else if(p1[1] > p2[1]) return -1;
		
		// reference based hue comparison..first the distances
		double abs = Math.abs(refHue - p1[0]);
		
		if(abs <= 0.5) tmp1 = abs;
		else tmp1 = 1.0 - abs;
		
		abs = Math.abs(refHue - p2[0]);
		
		if(abs <= 0.5) tmp2 = abs;
		else tmp2 = 1.0 - abs;
		
		// and compare...
		if(tmp1 < tmp2) return -1;
		else if(tmp1 > tmp2) return 1;
		
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
