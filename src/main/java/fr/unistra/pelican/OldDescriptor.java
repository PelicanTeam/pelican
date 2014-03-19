package fr.unistra.pelican;


/**
 * 
 * @author Abdullah
 *
 */
public abstract class OldDescriptor extends Algorithm
{
	/**
	 * 
	 * @param d1
	 * @param d2
	 * @return the distance \in [0,1] of two feature vector computed by this descriptor 
	 */
	public abstract double distance(double[] d1,double[] d2);

}
