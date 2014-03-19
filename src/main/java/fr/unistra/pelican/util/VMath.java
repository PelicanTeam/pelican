/**
 * 
 */
package fr.unistra.pelican.util;

import java.util.Arrays;

/**
 * Some math operations for double [].
 * <br> By default operations are done in place (the argument is modified).
 * <br> Function ending by F (Functional) do not modify their argument. 
 * 
 * @author Benjamin Perret
 *
 */
public final class VMath {

	/**
	 * Never instantiate this class
	 */
	private VMath(){
		
	}
	
	/**
	 * Create new double array of given size initialized to given value
	 * @param size
	 * @param value
	 * @return
	 */
	public static double [] newVector(int size, double value)
	{
		double [] res=new double [size];
		Arrays.fill(res, value);
		return res;
	}
	
	/**
	 * Absolute value
	 * @param a
	 * @return
	 */
	public static final double [] abs(double [] a)
	{
		for(int i=0;i<a.length;i++)
			a[i]=Math.abs(a[i]);
		return a;
	}
	
	/**
	 * Absolute value, new vector allocated
	 * @param a
	 * @return
	 */
	public static final double [] absF(double [] a)
	{
		double [] res=a.clone();
		for(int i=0;i<a.length;i++)
			res[i]=Math.abs(a[i]);
		return res;
	}
	
	/**
	 * Sum element by element (no length check!)
	 * @param a
	 * @param b
	 * @return
	 */
	public static final double [] add(double [] a, double [] b)
	{
		for(int i=0;i<a.length;i++)
			a[i]+=b[i];
		return a;
	}
	
	/**
	 * Sum element by element (no length check!), new vector allocated
	 * @param a
	 * @param b
	 * @return
	 */
	public static final double [] addF(double [] a, double [] b)
	{
		double [] res=a.clone();
		for(int i=0;i<a.length;i++)
			res[i]+=b[i];
		return res;
	}
	
	/**
	 * Subtraction element by element (no length check!)
	 * @param a
	 * @param b
	 * @return
	 */
	public static final double [] sub(double [] a, double [] b)
	{
		for(int i=0;i<a.length;i++)
			a[i]-=b[i];
		return a;
	}
	
	
	/**
	 * Subtraction element by element (no length check!), new vector allocated
	 * @param a
	 * @param b
	 * @return
	 */
	public static final double [] subF(double [] a, double [] b)
	{
		double [] res=a.clone();
		for(int i=0;i<a.length;i++)
			res[i]-=b[i];
		return res;
	}
	
	/**
	 * Divide all elements by the same number
	 * @param a
	 * @param k
	 * @return
	 */
	public static final double [] div(double [] a, double k)
	{
		for(int i=0;i<a.length;i++)
			a[i]/=k;
		return a;
	}
	
	/**
	 * Division element by element (no length check!)
	 * @param a
	 * @param b
	 * @return
	 */
	public static final double [] div(double [] a, double [] b)
	{
		for(int i=0;i<a.length;i++)
			a[i]/=b[i];
		return a;
	}
	
	/**
	 * Division element by element (no length check!), new vector allocated
	 * @param a
	 * @param b
	 * @return
	 */
	public static final double [] divF(double [] a, double [] b)
	{
		double [] res=a.clone();
		for(int i=0;i<a.length;i++)
			res[i]/=b[i];
		return res;
	}
	
	/**
	 * Multiplication element by element (no length check!)
	 * @param a
	 * @param b
	 * @return
	 */
	public static final double [] mul(double [] a, double [] b)
	{
		for(int i=0;i<a.length;i++)
			a[i]*=b[i];
		return a;
	}
	
	/**
	 * Multiplication element by element (no length check!), new vector allocated
	 * @param a
	 * @param b
	 * @return
	 */
	public static final double [] mulF(double [] a, double [] b)
	{
		double [] res=a.clone();
		for(int i=0;i<a.length;i++)
			res[i]*=b[i];
		return res;
	}
	
	/**
	 * Multiplication by a scalar
	 * @param a
	 * @param b
	 * @return
	 */
	public static final double [] mul(double [] a, double k)
	{
		for(int i=0;i<a.length;i++)
			a[i]*=k;
		return a;
	}
	
	/**
	 * Multiplication by a scalar, new vector allocated
	 * @param a
	 * @param b
	 * @return
	 */
	public static final double [] mulF(double [] a, double k)
	{
		double [] res=a.clone();
		for(int i=0;i<a.length;i++)
			res[i]*=k;
		return res;
	}
	
	/**
	 * Compute Euclidean norm
	 * @param a
	 * @return
	 */
	public static final double norm(double [] a)
	{
		double res=0.0;
		for(int i=0;i<a.length;i++)
			res+=a[i]*a[i];
		return Math.sqrt(res);
	}
	
	
	/**
	 * Compute dot product of an element by itself
	 * @param a
	 * @param b
	 * @return
	 */
	public static final double dotProduct(double [] a)
	{
		double res=0.0;
		for(int i=0;i<a.length;i++)
			res+=a[i]*a[i];
		return res;
	}
	
	/**
	 * Compute dot product (no length check!)
	 * @param a
	 * @param b
	 * @return
	 */
	public static final double dotProduct(double [] a,double [] b)
	{
		double res=0.0;
		for(int i=0;i<a.length;i++)
			res+=a[i]*b[i];
		return res;
	}
	
	
	/**
	 * Compute Euclidean distance (no length check!)
	 * @param a
	 * @param b
	 * @return
	 */
	public static final double dist(double [] a, double [] b)
	{
		double res=0.0;
		for(int i=0;i<a.length;i++)
		{
			double v=a[i]-b[i];
			res+=v*v;
		}
			
		return Math.sqrt(res);
	}
	
	/**
	 * Compute sum of elements
	 * @param a
	 * @return
	 */
	public static final double sum(double [] a)
	{
		double res=0.0;
		for(int i=0;i<a.length;i++)
			res+=a[i];
		return res;
	}
	
}
