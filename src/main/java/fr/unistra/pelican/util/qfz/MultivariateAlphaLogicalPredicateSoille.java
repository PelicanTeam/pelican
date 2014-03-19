package fr.unistra.pelican.util.qfz;


public class MultivariateAlphaLogicalPredicateSoille extends MultivariateScalarAlphaLogicalPredicate {
	
	
	public MultivariateAlphaLogicalPredicateSoille(int alpha)
	{
		super(alpha);
	}
	
	public boolean _check(int[] values1, int[] values2)
	{
		boolean conditionChecked=true;
		for(int i=0;i<values1.length;i++)
		{
			if(Math.abs(values1[i]-values2[i])>currentAlpha)
			{
				conditionChecked=false;
				break;
			}
		}
		return conditionChecked;
	}
	
	public double getDistance(int[] values1, int[] values2)
	{
		double distance=Double.NEGATIVE_INFINITY;
		for(int i=0;i<values1.length;i++)
		{
			if(Math.abs(values1[i]-values2[i])>distance)
			{
				distance=Math.abs(values1[i]-values2[i]);
			}
		}		
		return distance;
	}
}