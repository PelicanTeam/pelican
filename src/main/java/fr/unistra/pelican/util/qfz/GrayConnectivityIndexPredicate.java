package fr.unistra.pelican.util.qfz;

import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.util.Point4D;
import fr.unistra.pelican.util.Tools;

public class GrayConnectivityIndexPredicate extends GrayLogicalPredicate {

	private double alphaConnectedInternalPath=0;
	private double internalPath=0;
	private double beta;
	
	public GrayConnectivityIndexPredicate(double beta)
	{		
		type=GLOBALPREDICATE;
		this.beta=beta;
	}
	
	@Override
	protected boolean _check() 
	{
		return (alphaConnectedInternalPath/internalPath)>=beta;
	}

	@Override
	public void resetData() 
	{
		alphaConnectedInternalPath=0;
		internalPath=0;
	}

	@Override
	public void updatePredicateData(ByteImage inputImage, IntegerImage QFZ,
			int alpha, int currentAlpha, int x, int y, int z, int t,
			int currentLabel, Point4D[] neighbourhood) 
	{
		for(int i=0;i<neighbourhood.length;i++)
		{
			int locX = x + neighbourhood[i].x;
			int locY = y + neighbourhood[i].y;
			int locZ = z + neighbourhood[i].z;
			int locT = t + neighbourhood[i].t;
			if(!QFZ.isOutOfBoundsXYZT(locX, locY, locZ, locT))
			{
				if(QFZ.getPixelXYZTInt(locX, locY, locZ, locT)==currentLabel)
				{
					internalPath++;
					if(Math.abs(inputImage.getPixelXYZTByte(x, y, z, t)-inputImage.getPixelXYZTByte(locX, locY, locZ, locT))<=currentAlpha)
					{
						alphaConnectedInternalPath++;
					}
				}
			}
		}
	}

	@Override
	public void updatePredicateDataInteger(IntegerImage inputImage,
			IntegerImage QFZ, int alpha, int currentAlpha, int x, int y, int z,
			int t, int currentLabel, Point4D[] neighbourhood) {
		for(int i=0;i<neighbourhood.length;i++)
		{
			int locX = x + neighbourhood[i].x;
			int locY = y + neighbourhood[i].y;
			int locZ = z + neighbourhood[i].z;
			int locT = t + neighbourhood[i].t;
			if(!QFZ.isOutOfBoundsXYZT(locX, locY, locZ, locT))
			{
				if(QFZ.getPixelXYZTInt(locX, locY, locZ, locT)==currentLabel)
				{
					internalPath++;
					if(Math.abs(inputImage.getPixelXYZTInt(x, y, z, t)-inputImage.getPixelXYZTInt(locX, locY, locZ, locT))<=currentAlpha)
					{
						alphaConnectedInternalPath++;
					}
				}
			}
		}
		
	}

	@Override
	public void updatePredicateDataDouble(DoubleImage inputImage,
			IntegerImage QFZ, int alpha, int currentAlpha, int x, int y, int z,
			int t, int currentLabel, Point4D[] neighbourhood) {
		for(int i=0;i<neighbourhood.length;i++)
		{
			int locX = x + neighbourhood[i].x;
			int locY = y + neighbourhood[i].y;
			int locZ = z + neighbourhood[i].z;
			int locT = t + neighbourhood[i].t;
			if(!QFZ.isOutOfBoundsXYZT(locX, locY, locZ, locT))
			{
				if(QFZ.getPixelXYZTInt(locX, locY, locZ, locT)==currentLabel)
				{
					internalPath++;
					if(Math.abs(inputImage.getPixelXYZTDouble(x, y, z, t)-inputImage.getPixelXYZTDouble(locX, locY, locZ, locT))<=currentAlpha)
					{
						alphaConnectedInternalPath++;
					}
				}
			}
		}
		
	}
}
