/**
 * 
 */
package fr.unistra.pelican.util.vectorial.ordering;

import fr.unistra.pelican.util.Tools;

/**
 * The same as NormAndLexicographic ordering but with robust double comparison using Tools.relativeDoubleCompare 
 * 
 * @author Benjamin Perret
 *
 */
public class EnergyAndLexicographicalOrdering extends VectorialOrdering {

	public static  long compCounter=0;
	
	public static  long conflictCounter=0;
	
	private double energy(double [] o)
	{
		double e=0.0;
		for(int i=0;i<o.length;i++)
			//if(o[i]>0.0)
				e+=o[i]*o[i];
		return e;
	}
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.CC.Ordering.VectorialOrdering#compare(double[], double[])
	 */
	@Override
	public int compare(double[] o1, double[] o2) {

		//int a = Double.compare(energy(o1), energy(o2));
		int a = Tools.relativeDoubleCompare(energy(o1), energy(o2),100000);
		compCounter++;
		if (a != 0)
			return a;
		else {
			conflictCounter++;
			for (int i = 0; i < o1.length; i++) {
				int c = Double.compare(o1[i], o2[i]);
				if (c != 0) {
					return c;
				}
			}
		}
		conflictCounter--;
		return 0;
	}

}
