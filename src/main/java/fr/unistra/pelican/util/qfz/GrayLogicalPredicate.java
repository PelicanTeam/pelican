package fr.unistra.pelican.util.qfz;

import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.util.Point4D;

public abstract class GrayLogicalPredicate 
{
	public final static int LOCALPREDICATE = 1;
	public final static int GLOBALPREDICATE = 2;
	
	protected int type;
	
	/**
	 * Method which checks the predicate
	 */
	protected abstract boolean _check();
	
	/**
	 * Method which reset pre-computed data of the predicate
	 */
	public abstract void resetData();
	
	/**
	 * Method which updates pre-computed predicate data 
	 * when adding a pixel to the current ZQP
	 */
	public abstract void updatePredicateData(ByteImage inputImage,IntegerImage QFZ,int alpha,int currentAlpha,int x, int y, int z, int t, int currentLabel, Point4D[] neighbourhood);
	
	/**
	 * Method which updates pre-computed predicate data 
	 * when adding a pixel to the current ZQP
	 */
	public abstract void updatePredicateDataInteger(IntegerImage inputImage,IntegerImage QFZ,int alpha,int currentAlpha,int x, int y, int z, int t, int currentLabel, Point4D[] neighbourhood);
	
	/**
	 * Method which updates pre-computed predicate data 
	 * when adding a pixel to the current ZQP
	 */
	public abstract void updatePredicateDataDouble(DoubleImage inputImage,IntegerImage QFZ,int alpha,int currentAlpha,int x, int y, int z, int t, int currentLabel, Point4D[] neighbourhood);
	
	
	/**
	 * This methods checks the predicate 
	 * @param alpha
	 * @return
	 */
	public final boolean check(int currentAlpha)
	{
		if(currentAlpha<=0)
		{
			return true;
		}
		else
		{
			return _check();
		}			
	}
	
	
	
	
	public final int getType()
	{
		return type;
	}

	
}
