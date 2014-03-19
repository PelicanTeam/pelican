package fr.unistra.pelican.algorithms.statistics;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.IntegerImage;

/**
 * This class performs the Maximal Precision to compare an oversegmentation to a reference segmentation.
 * S. Derivaux, G. Forestier, C. Wemmert et S. Lefèvre : Supervised image segmentation
 * using watershed transform, fuzzy classification and evolutionary computation.
 * Pattern Recognition Letters, 31(15):2364–2374, 2010.
 * 
 * @author Jonathan Weber
 *
 */
public class OverSegmentationEvaluation extends Algorithm {

	public IntegerImage overSegmentation;
	
	public IntegerImage referenceSegmentation;
	
	public double precisionMaximum;
	
	public OverSegmentationEvaluation()
	{
		super.inputs="overSegmentation,referenceSegmentation";
		super.outputs="precisionMaximum";
	}
	
	@Override
	public void launch() throws AlgorithmException 
	{
		precisionMaximum=0;
		int nbPixels=overSegmentation.size();
		int nbOverSegRegions=overSegmentation.maximumInt()+1;
		int nbReferenceSegmentationRegions=referenceSegmentation.maximumInt()+1;
		int[][] segmentationMap = new int[nbOverSegRegions][nbReferenceSegmentationRegions];
		for(int i=0;i<nbPixels;i++)
		{
			segmentationMap[overSegmentation.getPixelInt(i)][referenceSegmentation.getPixelInt(i)]++;
		}
		for(int i=0;i<nbOverSegRegions;i++)
		{
			int[] regionMap = segmentationMap[i];
			int max=regionMap[0];
			for(int j=1;j<nbReferenceSegmentationRegions;j++)
				if(regionMap[j]>max)
				{
					max=regionMap[j];
				}
			precisionMaximum+=max;
		}
		precisionMaximum/=nbPixels;
	}
	
	public static double exec(IntegerImage overSegmentation, IntegerImage referenceSegmentation)
	{
		return (Double) new OverSegmentationEvaluation().process(overSegmentation,referenceSegmentation);
	}
}
