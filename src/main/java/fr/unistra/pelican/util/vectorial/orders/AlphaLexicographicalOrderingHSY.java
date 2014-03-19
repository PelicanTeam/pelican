package fr.unistra.pelican.util.vectorial.orders;

import java.awt.Point;
import java.util.Arrays;
import java.util.Comparator;

import fr.unistra.pelican.util.Tools;
import fr.unistra.pelican.util.vectorial.VectorPixel;

/**
 * This class represents an alpha-lexicographical ordering scheme 
 * for double valued arrays and derivatives..specific for LSH..
 * 
 * @author E.A.
 *
 */

public class AlphaLexicographicalOrderingHSY implements VectorialOrdering,Comparator
{
	private double alpha = 0.0;
	private double refHue = 0.0;
	
	public AlphaLexicographicalOrderingHSY(double alpha,double refHue)
	{
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
	
	public Point max(Point[] p)
	{
		Point max = p[0];

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
			
		
		// Luminance
		if(Tools.doubleCompare(p1[2] + alpha,p2[2]) == -1) return -1;
		else if(Tools.doubleCompare(p1[2] - alpha,p2[2]) == 1) return 1;
		
		// saturation
		if(Tools.doubleCompare(p1[1],p2[1]) == -1) return -1;
		else if(Tools.doubleCompare(p1[1],p2[1]) == 1) return 1;
		
		// hue..
		double abs1 = Math.abs(refHue - p1[0]);
		
		if(abs1 <= 0.5) abs1 = 0.5 - abs1;
		else abs1 = 0.5 - 1.0 + abs1;
		
		double abs2 = Math.abs(refHue - p2[0]);
		
		if(abs2 <= 0.5) abs2 = 0.5 - abs2;
		else abs2 = 0.5 - 1.0 + abs2;
		
		if(Tools.doubleCompare(abs1,abs2) == -1) return -1;
		else if(Tools.doubleCompare(abs1,abs2) == 1) return 1;
			
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
