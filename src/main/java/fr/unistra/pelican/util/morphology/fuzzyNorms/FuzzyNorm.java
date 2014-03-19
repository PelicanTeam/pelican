package fr.unistra.pelican.util.morphology.fuzzyNorms;


/**
 * Abstract class that implements both T-Norm and T-Conorm.
 * @author Perret
 *
 */
public abstract class FuzzyNorm implements FuzzyTNorm, FuzzyTCoNorm {

	
	/** (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.fuzzy.util.Norms.FuzzyTNorm#tDistance(double, double)
	 */
	abstract public double tDistance(double a, double b); 

	/** (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.fuzzy.util.Norms.FuzzyTCoNorm#tCoDistance(double, double)
	 */
	abstract public double tCoDistance(double a, double b) ;
	
	/**
	 * Overide this function to give a descpription for a FuzzyNorm
	 * @return
	 */
	public String getDescription()
	{
		return "Description of norm's proprieties";
	}

}
