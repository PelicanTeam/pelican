package fr.unistra.pelican.util.qfz;

import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.segmentation.qfz.color.MultivariateLogicalPredicateConnectivityAppliedOnRegion;
import fr.unistra.pelican.util.Point4D;

public abstract class MultivariateLogicalPredicate 
{
	public final static int LOCALPREDICATE = 1;
	public final static int GLOBALPREDICATE = 2;
	
	protected int type;
	protected int nBands;
	
	protected MultivariateLogicalPredicate(int nBands, int type)
	{
		this.nBands=nBands;
		this.type=type;
	}
	
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
	public abstract void updatePredicateData(ByteImage inputImage,IntegerImage QFZ,MultivariateAlphaLogicalPredicate alphaPred,int x, int y, int z, int t, int currentLabel, Point4D[] neighbourhood);
	
	/**
	 * Method which updates pre-computed predicate data
	 * in the context of merging of existing QFZ
	 * BASICALLY NOT DEFINED throw PelicanException 
	 * except for specific predicate 
	 * check in predicate class javadoc
	 */
	public void updatePredicateDataForMerging(MultivariateLogicalPredicateConnectivityAppliedOnRegion.Region region)
	{
		throw new PelicanException("Undefined for predicate "+this.getClass().getName());
	}
	
	/**
	 * This methods checks the predicate 
	 * @param alpha
	 * @return
	 */
	public final boolean check(MultivariateAlphaLogicalPredicate alphaPred)
	{
		if(alphaPred.isCurrentAlphaZero())
		{
			return true;
		}
		else
		{
			return _check();
		}			
	}
	
	public final boolean isLocal()
	{
		return this.type==LOCALPREDICATE;
	}
	
	
	public final int getType()
	{
		return type;
	}
}
