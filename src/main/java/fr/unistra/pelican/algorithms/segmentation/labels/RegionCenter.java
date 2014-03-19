package fr.unistra.pelican.algorithms.segmentation.labels;

import java.awt.Point;
import java.util.Arrays;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;

/**
 * Return an array where which the center of all region by label. Dimensions: X,
 * Y
 */
public class RegionCenter extends Algorithm {

	// Inputs parameters
	public Image input;

	// Outputs parameters
	public Point[] regionCenter;

	public int[] regionSize;

	public int[] regionX;

	public int[] regionY;

	/**
	 * Constructor
	 * 
	 */
	public RegionCenter() {

		super();
		super.inputs = "input";
		super.outputs = "regionCenter";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {

		// Find the number of regions to allocate an array
		int max = 0;
		for (int i = 0; i < input.size(); i++)
			max = Math.max(input.getPixelInt(i), max);
		regionCenter = new Point[max + 1];
		regionSize = new int[max + 1];
		regionX = new int[max + 1];
		regionY = new int[max + 1];

		// Fill this array with the size of each region.
		Arrays.fill(regionSize, 0);
		Arrays.fill(regionX, 0);
		Arrays.fill(regionY, 0);
		for (int x = 0; x < input.getXDim(); x++)
			for (int y = 0; y < input.getYDim(); y++) {
				int label = input.getPixelXYInt(x, y);
				regionSize[label]++;
				regionX[label] += x;
				regionY[label] += y;
			}
		for (int i = 0; i < max + 1; i++)
			if (regionSize[i] != 0)
				regionCenter[i] = new Point(regionX[i] / regionSize[i],
						regionY[i] / regionSize[i]);

	}
}
