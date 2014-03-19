package fr.unistra.pelican.util.qfz;

public abstract class MultivariateScalarAlphaLogicalPredicate extends MultivariateAlphaLogicalPredicate
{
	protected int alpha;
	protected int currentAlpha;
	protected int alphaMax;
	protected int alphaMin;
	
	protected MultivariateScalarAlphaLogicalPredicate(int alpha) {
		super();
		this.alpha= alpha;
		currentAlpha= alpha;
		alphaMax=alpha+1;
		alphaMin=0;
	}
	
	
	public void decreaseCurrentAlpha()
	{
		currentAlpha-=decreaseStep;
		if(currentAlpha<0)
		{
			currentAlpha=0;
		}
	}
	
	public boolean isCurrentAlphaZero()
	{
		return(currentAlpha<=0);
	}
	
	public void resetCurrentAlpha()
	{
		currentAlpha=alpha;
		finalAlpha=false;
		alphaMax=alpha+1;
		alphaMin=0;
	}
	
	public void predicateViolationUpdate()
	{
		alphaMax=currentAlpha;
		currentAlpha=(alphaMax+alphaMin)/2;
	}
	public void predicateValidationUpdate()
	{
		if(currentAlpha+1==alphaMax)
		{
			finalAlpha=true;
		}
		else
		{
			alphaMin=currentAlpha;
			currentAlpha=(alphaMax+alphaMin)/2;
		}
	}

	/***
	 * Just for compatibility with MultivariateLogicalPredicateConnectivityRegionMergingVideo until its rewriting
	 * @return
	 */
	public int getCurrentAlpha() {
		return currentAlpha;
	}

}
