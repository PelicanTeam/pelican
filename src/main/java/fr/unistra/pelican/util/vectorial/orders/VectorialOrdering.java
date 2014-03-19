package fr.unistra.pelican.util.vectorial.orders;

/**
 * An interface providing the fundamental methods for vector ordering and comparison
 * 
 * TODO : change name as VectorialExtremum
 * 
 * @author E.A.
 *
 */

public interface VectorialOrdering
{
	/**
	 * Calculates the max of a vector array
	 * @param p double valued vector array
	 * @return the greatest vector according to the ordering in question
	 */
	public double[] max(double[][] p);

	/**
	 * Calculates the min of a vector array
	 * @param p double valued vector array
	 * @return the smallest vector according to the ordering in question
	 */
	public double[] min(double[][] p);

	/**
	 * Sorts and returns the element of the given vector array in the given position
	 * @param p double valued vector array
	 * @param r desired position in the array
	 * @return the element of the given vector array in the given position
	 */	
	public double[] rank(double[][] p,int r);
}