package fr.unistra.pelican.algorithms.segmentation.labels;

import java.util.Arrays;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.IntegerImage;
/**
 * Merge the labels of regions belonging to the same class according to markers
 * Final label is marker label
 * 
 * @author Jonathan Weber
 */
public class MergeLabelsAccordingToMarkers extends Algorithm {

	public IntegerImage segmentation;
	
	public IntegerImage markers;
	
	public boolean unsafe=false;
	
	public IntegerImage mergedSegmentation;
	
	public MergeLabelsAccordingToMarkers()
	{
		super.inputs="segmentation,markers";
		super.options="unsafe";
		super.outputs="mergedSegmentation";
	}
	
	
	@Override
	public void launch() throws AlgorithmException {
		int nbLabels=segmentation.maximumInt()+1;
		int[] correspondingLabels=new int[nbLabels];
		Arrays.fill(correspondingLabels, -1);
		for(int i=0;i<segmentation.size();i++)
		{
			int markerVal=markers.getPixelInt(i);
			if(markerVal>0)
			{
				correspondingLabels[segmentation.getPixelInt(i)]=markerVal;
			}
		}
		if(unsafe)
			mergedSegmentation=segmentation;
		else
			mergedSegmentation=segmentation.copyImage(false);
		for(int i=0;i<segmentation.size();i++)
		{
			mergedSegmentation.setPixelInt(i, correspondingLabels[segmentation.getPixelInt(i)]);
		}

	}
	
	public static IntegerImage exec (IntegerImage segmentation, IntegerImage markers)
	{
		return (IntegerImage) new MergeLabelsAccordingToMarkers().process(segmentation,markers);
	}
	
	public static IntegerImage exec (IntegerImage segmentation, IntegerImage markers, boolean unsafe)
	{
		return (IntegerImage) new MergeLabelsAccordingToMarkers().process(segmentation,markers,unsafe);
	}

}
