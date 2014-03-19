package fr.unistra.pelican.algorithms.histogram;

import java.util.Arrays;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.IntegerImage;


/**
 * Computes the region size histogram (regular or normalized) of a monoband label image
 * 
 * @author Jonathan Weber
 * 
 */
public class RegionSizeHistogram extends Algorithm {
	
	/**
	 * Input parameter.
	 */
	public IntegerImage inputSegmentation;
	
	/**
	 * Normalized parameter
	 * true if normalized
	 * default is normalized
	 */
	public boolean normalized=true;
	
	/**
	 * Output histogram
	 */
	public Double[] regionSizeHistogram;
	
	public RegionSizeHistogram()
	{
		super.inputs="inputSegmentation";
		super.options="normalized";
		super.outputs="regionSizeHistogram";
	}

	@Override
	public void launch() throws AlgorithmException 
	{
		int numberOfRegions=inputSegmentation.maximumInt()+1;
		int size = inputSegmentation.size();
		int[] regionSize=new int[numberOfRegions];
		Arrays.fill(regionSize,0);
		if(inputSegmentation.getMask()==null||inputSegmentation.getMask().isEmpty())
		{
			for(int i=0;i<size;i++)
			{
				regionSize[inputSegmentation.getPixelInt(i)]++;
			}
		}
		else
		{
			for(int i=0;i<size;i++)
			{
				if(inputSegmentation.isPresent(i))
					regionSize[inputSegmentation.getPixelInt(i)]++;
			}
		}
		int sizeMax=0;
		for(int i=0;i<numberOfRegions;i++)
		{
			if(regionSize[i]>sizeMax)
				sizeMax=regionSize[i];
		}
		regionSizeHistogram=new Double[sizeMax+1];
		Arrays.fill(regionSizeHistogram, 0.);
		for(int i=0;i<numberOfRegions;i++)
		{
			regionSizeHistogram[regionSize[i]]++;
		}
		regionSizeHistogram[0]=0.;
		if(normalized)
		{
			numberOfRegions--;
			for(int i=1;i<=sizeMax;i++)
			{
				regionSizeHistogram[i]/=numberOfRegions;
			}
		}
	}
	
	public static Double[] exec(IntegerImage input) {
		return (Double[]) new RegionSizeHistogram().process(input);
	}
	
	public static Double[] exec(IntegerImage input, boolean normalized) {
		return (Double[]) new RegionSizeHistogram().process(input,normalized);
	}

}
