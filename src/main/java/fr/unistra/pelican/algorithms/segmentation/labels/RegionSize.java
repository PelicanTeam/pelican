package fr.unistra.pelican.algorithms.segmentation.labels;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.Image;

/**
 * Return an array where which the size of all region by label. Max region size
 * is Integer.MAX_VALUE. Dimensions: X, Y
 * @author Sébastien Derivaux
 */
public class RegionSize extends Algorithm {

	// Inputs parameters
	public Image input;

	// Outputs parameters
	public int[] regionSize;

	/**
	 * Constructor
	 * 
	 */
	public RegionSize() {
		super.inputs = "input";
		super.outputs = "regionSize";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() {

		// Find the number of regions to allocate an array
		int max = 0;
		for (int i = 0; i < input.size(); i++)
			max = Math.max(input.getPixelInt(i), max);
		regionSize = new int[max + 1];

		// Fill this array with the size of each region.
		for (int i = 0; i < max; i++)
			regionSize[i] = 0;
		if(input.getMask()==null||input.getMask().isEmpty())
		{
			for (int i = 0; i < input.size(); i++) {
				int label = input.getPixelInt(i);
				regionSize[label]++;
			}
		}
		else
		{
			for (int i = 0; i < input.size(); i++) {
				if(input.isPresent(i)){				
					int label = input.getPixelInt(i);
					regionSize[label]++;
				}
			}	
		}

	}
	
	public static int[] exec(Image image) {
		return (int[])new RegionSize().process(image);
	}
	
}
