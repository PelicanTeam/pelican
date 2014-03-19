/**
 * 
 */
package fr.unistra.pelican.util.optimization;

import java.util.Arrays;

/**
 * Interface to implement to realize optimization of a function with LevenBerg-Marquardt algorithm.
 * If you cannot provide analytical Jacobian you can compute it using finite difference 
 * 
 * @author Benjamin Perret
 *
 */
public interface LevenbergMarquardtFunction {
	
	/**
	 * Get function value over all space for given parameter
	 * @return Value of the function for parameter a.
	 * @param a Function parameters. 
	 * @param if buffer is not null result is put in buffer, new space is allocated otherwise
	 */
	public abstract double [] getY(double[] a, double [] buffer);
	

	/** 
	 * The method which gives the Jacobian ie all the partial derivatives used in the LMA fit.
	 * @return the Jacobian matrix J, i.e. J[i][j]=d getY[j]/ di 
	 * @param a Function parameters.
	 */
	public abstract double [][] getJacobian( double[] a);
	

	/**
	 * Test if parameters respect the constraint of the function
	 * @param a parameters
	 * @return true is constraints are fulfilled, false otherwise
	 */
	public abstract boolean checkConstraints(double [] a);
	
	
	

}
