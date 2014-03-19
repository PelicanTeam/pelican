package fr.unistra.pelican.util.qfz;



public abstract class MultivariateAlphaLogicalPredicate {
	
	protected int decreaseStep=1;
	protected boolean finalAlpha;
	
	
	protected MultivariateAlphaLogicalPredicate()
	{
		finalAlpha=false;
	}
	
	
	
	
	protected abstract boolean _check(int[] values1, int[] values2);
	public abstract double getDistance(int[] values1, int[] values2);
	public abstract void decreaseCurrentAlpha();
	public abstract boolean isCurrentAlphaZero();
	public abstract void resetCurrentAlpha();
	public abstract void predicateViolationUpdate();
	public abstract void predicateValidationUpdate();
	
	
	/**
	 * Method which checks the predicate
	 */
	public final boolean check(int[] values1, int[] values2)
	{
		return _check(values1, values2);
	}
	
	/**
	 * Method which checks if the dichotomic alpha search is complete
	 */
	public final boolean isFinalAlpha()
	{
		return finalAlpha;
	}
	
//	public final int getCurrentAlpha()
//	{
//		return currentAlpha[0];
//	}
//	
//	public final int[] getCurrentAlphaVector()
//	{
//		return currentAlpha;
//	}
	
	
	public final void setDecreaseStep(int decreaseStep)
	{
		this.decreaseStep=decreaseStep;
	}

}
