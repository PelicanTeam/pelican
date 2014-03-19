/**
 * 
 */
package fr.unistra.pelican.util.vectorial.ordering;

import fr.unistra.pelican.InvalidParameterException;

/**
 * Ordering for astronomical multiband images. 
 * Each band is weighted by the deviation of noise in this band (assuming background has been removed).
 * 
 * Let v=[v1,...,vn] and v'=[v'1,...,v'2] be the 2 vectors to compare.
 * Let o=[o1,...,on] be the deviation in each band.
 * Let !x! stands for the ceil of x
 * Let E(v)=�||[v1/(k*o1),...,vn/(k*on)]||� be the truncated weighted energy of vector v
 * The comparison is defined by the lexicographical ordering of vector 
 * V(v) = [E(v),!v1/(k*o1)!,...,!vn/(k*on)!,v1,...,vn]
 * Thus coming from a vectors of n components the ordering implies vectors of 2n+1 components.
 * 
 * 
 * 
 * Moreover the band can be reordered using ordering field. The specified ordering should put he band with best resolution at first and so on.
 *
 * For example given a 3 band image with deviations of noise [5,7,3] and resolution [1.2,2.0,0.8], you can construct an ordering with
 * new AstronomicalOrdering([5,7,3],k,[2,0,1]) with k a value of your choice.
 *
 * You can access statistics on the behavior of the ordering through the static fields of the class.
 *
 * @author Benjamin Perret
 *
 */
public class AstronomicalOrdering extends VectorialOrdering {

	/**
	 * deviation of noise (assumed Gaussian) in each band
	 */
	private double [] deviation;
	
	
	/**
	 * reordering of bands
	 */
	private int [] ordering;
	
	/**
	 * Gives for each element the  number of times it determined an inequality
	 */
	private  static long resolvedAtComponent [];
	
	/**
	 * Number of comparisons computed
	 */
	private static long totalComparisions=0;
	
	/**
	 * Number of times comparison concluded to equality
	 */
	private static long totalEqualities=0;
	
	/**
	 * @return the totalEqualities
	 */
	public static long getTotalEqualities() {
		return totalEqualities;
	}

	/**
	 * @return the totalComparisions
	 */
	public static long getTotalComparisions() {
		return totalComparisions;
	}

	/**
	 * @return the equalities
	 */
	public static long[] getResolvedAtComponent() {
		return resolvedAtComponent.clone();
	}

	/**
	 * @param wheight
	 */
	public AstronomicalOrdering(double[] deviation, double confidenceFactor, int [] ordering) {
		super();
		
		
		this.ordering=ordering;
		if (deviation.length!=ordering.length)
			throw new InvalidParameterException("Error during creation of Astronomical Ordering : variance and ordering must have same length.");
		if(confidenceFactor<=0.0)
			throw new InvalidParameterException("Error during creation of Astronomical Ordering : confidence factor may be strictly positive.");
		double [] op=deviation.clone();
		for(int i=0;i<deviation.length;i++)
			op[ordering[i]]=deviation[i]*confidenceFactor;
		this.deviation=op;
		if(resolvedAtComponent==null || resolvedAtComponent.length!=2*deviation.length+1)
			resolvedAtComponent =new long[2*op.length+1];
	}

	/**
	 * Computed weighted truncated energy of given vector
	 * @param o
	 * @return
	 */
	private double energy(double [] o)
	{
		double e=0.0;
		for(int i=0;i<o.length;i++)
			{
				double v=o[ordering[i]]/deviation[i];
				e+=v*v;
			}
				
		return Math.ceil(Math.sqrt(e));
	}
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.CC.Ordering.VectorialOrdering#compare(double[], double[])
	 */
	@Override
	public int compare(double[] o1, double[] o2) {
		totalComparisions++;
		int a = Double
				.compare(energy(o1), energy(o2));
		if (a != 0)
		{
			resolvedAtComponent[0]++;
			return a;
		}
			
		else {
			
			for (int i = 0; i < o1.length; i++) {
				int c = Double.compare(Math.ceil(o1[ordering[i]]/deviation[i]), Math.ceil(o2[ordering[i]]/deviation[i]));
				
				if (c != 0) {
					resolvedAtComponent[ordering[i]+1]++;
					return c;
				}
				
			}
			for (int i = 0; i < o1.length; i++) {
				int c = Double.compare(o1[ordering[i]], o2[ordering[i]]);
				if (c != 0) {
					resolvedAtComponent[o1.length+ordering[i]+1]++;
					return c;
				}
				
			}
		}
		totalEqualities++;
		return 0;
	}

}
