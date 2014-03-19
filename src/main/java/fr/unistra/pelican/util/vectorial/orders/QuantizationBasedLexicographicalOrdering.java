package fr.unistra.pelican.util.vectorial.orders;

import java.util.Arrays;
import java.util.Comparator;

import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.util.Tools;


/**
 * This class represents a lexicographical ordering scheme for double valued pixels.
 * The input pixels are considered in the HSL order..
 * 
 * The priority of the first component is modified using a subquantization of the first (luminance) dimension
 * followed by saturation during comparison. Hue is not take into account.
 * 
 * Specifically created for the comparison of lexicographical orderings.
 * 
 * 
 * @author E.A.
 *
 */

public class QuantizationBasedLexicographicalOrdering implements BinaryVectorialOrdering,Comparator
{
	private double alpha;
	
	private double[] transformed;
	
	public static int syc = 0;
	
	/**
	 * Alpha is to be chosen as if the pixels values were in [0,255]
	 * 
	 * @param alpha the rounding parameter
	 */
	public QuantizationBasedLexicographicalOrdering(double alpha)
	{
		if(alpha > 1.0) this.alpha = alpha;
		else this.alpha = 1.0;
		
		// integer value
		alpha = Math.round(alpha);
		
		transformed = new double[256];
		double value = 0;
		
		for(int z = 0; z < 256;){
			int k = (int)Math.ceil(alpha * sigma(z) + Tools.epsilon);
			
			for(int i = z; i < z + k; i++)
				transformed[i] = value;
			
			value++;
			z += k;	
		}
	}
	
	double sigma(int z)
	{
		double x = z / 255.0;
		
		if(x <= 0.5) return (1 / (1 + Math.exp(-10 * (x - 0.25))) - 0.0758)/0.84834;
		else return (1 / (1 + Math.exp(10 * (x - 0.75))) - 0.0758)/0.84834;
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
			}else throw new ClassCastException();

		}catch(ClassCastException ex){
			ex.printStackTrace();
		}
		
		int tmp1,tmp2;
		double dtmp1,dtmp2;
		
		// L rounded..
		if(alpha > 1.0){
			dtmp1 = Math.round(ByteImage.doubleToByte * p1[2]);
			dtmp1 = transformed[(int)dtmp1];
					
			dtmp2 = Math.round(ByteImage.doubleToByte * p2[2]);
			dtmp2 = transformed[(int)dtmp2];
					
				
			if(Tools.doubleCompare(dtmp1,dtmp2) < 0) return -1;
			else if(Tools.doubleCompare(dtmp1,dtmp2) > 0) return 1;
		}else{
			if(Tools.doubleCompare(p1[2],p2[2]) < 0) return -1;
			else if(Tools.doubleCompare(p1[2],p2[2]) > 0) return 1;
		}
				
		// saturation
		if(Tools.doubleCompare(p1[1],p2[1]) < 0) return -1;
		else if(Tools.doubleCompare(p1[1],p2[1]) > 0) return 1;
		
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
