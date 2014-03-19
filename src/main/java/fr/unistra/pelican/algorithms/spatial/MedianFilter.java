package fr.unistra.pelican.algorithms.spatial;

import java.util.ArrayList;
import java.util.Arrays;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.Point4D;

/**
 * A median filter
 * 
 * 
 * @author Jonathan Weber
 * 
 */
public class MedianFilter extends Algorithm {

	/**
	 * Input image
	 */
	public Image inputImage;

	/**
	 * Ouput image
	 */
	public Image outputImage;

	/**
	 * Filter (Structuring Element)
	 */
	public BooleanImage filter;

	/**
	 * Constructor
	 * 
	 */
	public MedianFilter() {

		super();
		super.inputs = "inputImage,filter";
		super.outputs = "outputImage";

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		Point4D[] filterPoints = filter.foreground();
		int xDim = inputImage.getXDim();
		int yDim = inputImage.getYDim();
		int zDim = inputImage.getZDim();
		int tDim = inputImage.getTDim();
		int bDim = inputImage.getBDim();
		outputImage = inputImage.copyImage(false);
		for (int z = 0; z < zDim; z++)
			for (int t = 0; t < tDim; t++)
				for (int y = 0; y < yDim; y++)
					for (int x = 0; x < xDim; x++)
						for (int b = 0; b < bDim; b++) {
							ArrayList<Integer> values = new ArrayList<Integer>();
							for (int i = 0; i < filterPoints.length; i++) {
								int locX = x + filterPoints[i].x
										- filter.getCenter().x;
								int locY = y + filterPoints[i].y
										- filter.getCenter().y;
								int locZ = z + filterPoints[i].z
										- filter.getCenter().z;
								int locT = t + filterPoints[i].t
										- filter.getCenter().t;
								if (locX >= 0 && locY >= 0 && locZ >= 0
										&& locT >= 0 && locX < xDim
										&& locY < yDim && locZ < zDim
										&& locT < tDim) {
									values.add(inputImage.getPixelXYZTBByte(
											locX, locY, locZ, locT, b));
								}
							}
							Integer[] valArray = new Integer[values.size()];
							valArray = values.toArray(valArray);
							Arrays.sort(valArray);
							int val = valArray[valArray.length / 2];
							// special case if the number of values if even
							if (valArray.length % 2 == 0) {
								val += valArray[(valArray.length - 1) / 2];
								val /= 2;
							}
							outputImage.setPixelXYZTBByte(x, y, z, t, b,
									(int) val);
						}
	}

	@SuppressWarnings("unchecked")
	public static <T extends Image> T exec(T input, BooleanImage filter) {
		return (T) new MedianFilter().process(input, filter);
	}

}
