package fr.unistra.pelican.util.qfz;

import java.util.Arrays;

import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.segmentation.qfz.color.MultivariateLogicalPredicateConnectivityAppliedOnRegion;
import fr.unistra.pelican.util.Point4D;

public class MultivariateGlobalRangeSoillePredicate extends MultivariateLogicalPredicate 
{
	private int[] min;
	private int[] max;
	private int omega;
	
	
	
	public MultivariateGlobalRangeSoillePredicate(int omega, int nBands)
	{		
		super(nBands,LOCALPREDICATE);
		this.omega=omega;
		min=new int[nBands];
		max=new int[nBands];
		resetData();
	}
	
	protected final boolean _check()
	{
		for(int i=0;i<nBands;i++)
		{
			if((max[i]-min[i])>omega)
			{
				return false;
			}
		}		
		return true;
	}
	
	public final void resetData()
	{
		Arrays.fill(min,Integer.MAX_VALUE);
		Arrays.fill(max,Integer.MIN_VALUE);		
	}
	
	public final void updatePredicateData(ByteImage inputImage,IntegerImage QFZ,MultivariateAlphaLogicalPredicate alphaPred,int x, int y, int z, int t, int currentLabel, Point4D[] neighbourhood)
	{
		int[] pixelValue = inputImage.getVectorPixelXYZTByte(x, y, z, t);
		for(int i=0;i<nBands;i++)
		{
			if(pixelValue[i]<min[i])
				min[i]=pixelValue[i];
			if(pixelValue[i]>max[i])
				max[i]=pixelValue[i];
		}
		
	}
	
	@Override
	public final void updatePredicateDataForMerging(MultivariateLogicalPredicateConnectivityAppliedOnRegion.Region region)
	{
		for(int i=0;i<nBands;i++)
		{
			if(region.getValues()[i]<min[i])
				min[i]=region.getValues()[i];
			if(region.getValues()[i]>max[i])
				max[i]=region.getValues()[i];
		}
	}
}
