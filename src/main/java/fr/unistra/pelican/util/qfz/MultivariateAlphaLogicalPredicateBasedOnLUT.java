package fr.unistra.pelican.util.qfz;

import fr.unistra.pelican.util.lut.ThreeBandByteDistanceLUT;

public class MultivariateAlphaLogicalPredicateBasedOnLUT extends MultivariateScalarAlphaLogicalPredicate {
	
	private ThreeBandByteDistanceLUT lut;
	
	public MultivariateAlphaLogicalPredicateBasedOnLUT(int alpha, ThreeBandByteDistanceLUT lut)
	{
		super(alpha);
		this.lut=lut;
	}
	
	public boolean _check(int[] values1, int[] values2)
	{
		return(lut.get(Math.abs(values1[0]-values2[0]), Math.abs(values1[1]-values2[1]), Math.abs(values1[2]-values2[2]))<=currentAlpha);
	}
	
	public double getDistance(int[] values1, int[] values2)
	{
		return lut.get(Math.abs(values1[0]-values2[0]), Math.abs(values1[1]-values2[1]), Math.abs(values1[2]-values2[2]));
	}
}
