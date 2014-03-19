package fr.unistra.pelican.algorithms.segmentation.labels;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;

/** 
 * Filter a label image using the most frequent label in the structuring element.
 * @author Sébastien Derivaux
 */
public class FilteringLabels extends Algorithm {

	// Inputs parameters
	public Image inputImage;

	public BooleanImage se;

	// Outputs parameters
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public FilteringLabels() {

		super();
		super.inputs = "inputImage,se";
		super.outputs = "outputImage";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = inputImage.copyImage(false);

		TreeMap<Integer, Integer> map;

		// Do it for all bands
		int xDim = inputImage.getXDim();
		int yDim = inputImage.getYDim();
		int zDim = inputImage.getZDim();
		int tDim = inputImage.getTDim();
		int bDim = inputImage.getBDim();
		for (int x = 0; x < xDim; x++)
			for (int y = 0; y < yDim; y++)
				for (int z = 0; z < zDim; z++)
					for (int t = 0; t < tDim; t++) {
						map = new TreeMap<Integer, Integer>();
						for (int[] p : getMask(x, y)) {
							int value = getKey(p[0], p[1], z, t);
							if (!map.containsKey(value))
								map.put(value, 1);
							else
								map.put(value, map.get(value) + 1);
						}
						int initialKey = getKey(x, y, z, t);
						int winner = 0;
						int max = 0;
						for (Entry<Integer, Integer> e : map.entrySet()) {
							if (e.getValue() > max
									|| (e.getValue() == max && e.getKey() == initialKey)) {
								winner = e.getKey();
								max = e.getValue();
							}
						}
						outputImage.setPixelInt(x, y, z, t, 0, winner);
					}

	}

	private List<int[]> getMask(int x, int y) {
		LinkedList<int[]> list = new LinkedList<int[]>();

		for (int i = 0; i < se.getXDim(); i++) {
			for (int j = 0; j < se.getYDim(); j++) {
				int valX = x - se.getCenter().x + i;
				int valY = y - se.getCenter().y + j;

				if (se.getPixelXYBoolean(i, j) && valX >= 0
						&& valX < inputImage.getXDim() && valY >= 0
						&& valY < inputImage.getYDim()) {
					int[] tmp = new int[2];
					tmp[0] = valX;
					tmp[1] = valY;
					list.add(tmp);
				}
			}
		}

		return list;
	}

	private int getKey(int x, int y, int z, int t) {
		return inputImage.getPixelInt(x, y, z, t, 0);
	}
	

    /**
     * Filter a label imag using the most frequent label in a struturing element.
     */
	public static Image exec(Image image, BooleanImage se) {
		return (Image)new FilteringLabels().process(image, se);
	}
}
