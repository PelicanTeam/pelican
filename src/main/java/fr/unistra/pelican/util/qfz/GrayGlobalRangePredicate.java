package fr.unistra.pelican.util.qfz;

import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.util.Point4D;

public class GrayGlobalRangePredicate extends GrayLogicalPredicate 
{
	private double min=Double.MAX_VALUE;
	private double max=Double.MIN_VALUE;
	private int omega;
	
	
	public GrayGlobalRangePredicate(int omega)
	{		
		type=LOCALPREDICATE;
		this.omega=omega;
	}
	
	protected final boolean _check()
	{
		return (max-min)<=omega;
	}
	
	public final void resetData()
	{
		min = Integer.MAX_VALUE;
		max = Integer.MIN_VALUE;
	}
	
	public final void updatePredicateData(ByteImage inputImage,IntegerImage QFZ,int alpha,int currentAlpha,int x, int y, int z, int t, int currentLabel, Point4D[] neighbourhood)
	{
		int pixelValue = inputImage.getPixelXYZTByte(x, y, z, t);
		if(pixelValue<min)
			min=pixelValue;
		if(pixelValue>max)
			max=pixelValue;
	}

	@Override
	public void updatePredicateDataInteger(IntegerImage inputImage,	IntegerImage QFZ, int alpha, int currentAlpha, int x, int y, int z,
			int t, int currentLabel, Point4D[] neighbourhood) {
		int pixelValue = inputImage.getPixelXYZTInt(x, y, z, t);
		if(pixelValue<min)
			min=pixelValue;
		if(pixelValue>max)
			max=pixelValue;
		
	}
	
	public void updatePredicateDataDouble(DoubleImage inputImage,	IntegerImage QFZ, int alpha, int currentAlpha, int x, int y, int z,
			int t, int currentLabel, Point4D[] neighbourhood) {
		double pixelValue = inputImage.getPixelXYZTDouble(x, y, z, t);
		if(pixelValue<min)
			min=pixelValue;
		if(pixelValue>max)
			max=pixelValue;
		
	}
}
