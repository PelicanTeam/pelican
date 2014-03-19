package fr.unistra.pelican.algorithms.histogram;

import java.util.Arrays;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.IntegerImage;


/**
 * Computes the region mean size per frame histogram (regular or normalized) of a monoband label image
 * 
 * @author Jonathan Weber
 * 
 */
public class RegionMeanSizePerFrameHistogram extends Algorithm {
	
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
	public Double[] regionMeanSizePerFrameHistogram;
	
	public RegionMeanSizePerFrameHistogram()
	{
		super.inputs="inputSegmentation";
		super.options="normalized";
		super.outputs="regionMeanSizePerFrameHistogram";
	}

	@Override
	public void launch() throws AlgorithmException 
	{
		int xDim = inputSegmentation.getXDim();
		int yDim = inputSegmentation.getYDim();
		int tDim = inputSegmentation.getTDim();
		int zDim = inputSegmentation.getZDim();
		int numberOfRegions=inputSegmentation.maximumInt()+1;
		int size = inputSegmentation.size();
		int[] regionSize=new int[numberOfRegions];
		int[] frameMin = new int[numberOfRegions];
		int[] frameMax = new int[numberOfRegions];
		Arrays.fill(regionSize,0);
		Arrays.fill(frameMin,Integer.MAX_VALUE);
		Arrays.fill(frameMax,Integer.MIN_VALUE);
		if(inputSegmentation.getMask()==null||inputSegmentation.getMask().isEmpty())
		{
			for(int t=0;t<tDim;t++)
				for(int z=0;z<zDim;z++)
					for(int y=0;y<yDim;y++)
						for(int x=0;x<xDim;x++)
						{
							int label = inputSegmentation.getPixelXYZTInt(x, y, z, t);
							regionSize[label]++;
							if(frameMin[label]>t)
								frameMin[label]=t;
							frameMax[label]=t;
						}
		}
		else
		{
			for(int t=0;t<tDim;t++)
				for(int z=0;z<zDim;z++)
					for(int y=0;y<yDim;y++)
						for(int x=0;x<xDim;x++)
						{
							if(inputSegmentation.isPresentXYZT(x, y, z, t))
							{
								int label = inputSegmentation.getPixelXYZTInt(x, y, z, t);
								regionSize[label]++;
								if(frameMin[label]>t)
									frameMin[label]=t;
								frameMax[label]=t;
							}
						}
		}
		int sizeMax=0;
		for(int i=0;i<numberOfRegions;i++)
		{
			regionSize[i]/=(frameMax[i]-frameMin[i]+1);
			if(regionSize[i]>sizeMax)
				sizeMax=regionSize[i];
		}
		regionMeanSizePerFrameHistogram=new Double[sizeMax+1];
		Arrays.fill(regionMeanSizePerFrameHistogram, 0.);
		for(int i=0;i<numberOfRegions;i++)
		{
			regionMeanSizePerFrameHistogram[regionSize[i]]++;
		}
		regionMeanSizePerFrameHistogram[0]=0.;
		if(normalized)
		{
			int meanAreaSum=0;			
			for(int i=1;i<=sizeMax;i++)
				meanAreaSum+=regionMeanSizePerFrameHistogram[i];
			for(int i=1;i<=sizeMax;i++)
			{
				regionMeanSizePerFrameHistogram[i]/=meanAreaSum;
			}
		}
	}
	
	public static Double[] exec(IntegerImage input) {
		return (Double[]) new RegionMeanSizePerFrameHistogram().process(input);
	}
	
	public static Double[] exec(IntegerImage input, boolean normalized) {
		return (Double[]) new RegionMeanSizePerFrameHistogram().process(input,normalized);
	}

}
