package fr.unistra.pelican.util.vectorial.orders;

/**
 * An extension to the standrad VectorialOrdering which adds the constraint of binarity
 * 
 * Theoretically speaking this is the actual VectorialOrdering, since orderings in the 
 * algebraic sense are binary relations. In practice however the VectorialOrdering class
 * of fr.unistra.pelican represents a more generic form of VectorialExtremum.
 * 
 * @author E.A.
 *
 */

public interface BinaryVectorialOrdering extends VectorialOrdering
{
	public int compare(Object o1,Object o2);
	
	public double[] max(double[] p1,double[] p2);
	public double[] min(double[] p1,double[] p2);
}