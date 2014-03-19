
package fr.unistra.pelican.algorithms.segmentation.labels;

import java.util.ArrayList;
import java.util.TreeMap;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;

/**
 * It is used to filter something like a pixel classification by a segmentation.
 * For each segment only one label is used, the one that is the most frequent
 * for this segment.
 * @author Sébastien Derivaux
 */
public class FilteringLabelsBySegmentation extends Algorithm {


    /*
     * Input Image
     */
	public Image input;

    /*
     * Segmentation used to filter
     */
	public Image segmentation;

    /*
     * Output Image
     */
	public Image output;

	/**
	 * Constructor
	 * 
	 */
	public FilteringLabelsBySegmentation() {

		super();
		super.inputs = "input,segmentation";
		super.outputs = "output";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		output = input.copyImage(false);

		// Treemap<segmentId, compteur>
		TreeMap<Integer, ArrayList<Integer>> map = new TreeMap<Integer, ArrayList<Integer>>();

		/*
		 * Alors, on va parser tout les labels et les ajoute au compteur du
		 * segment auquel il appartient
		 */
		int xDim = input.getXDim();
		int yDim = input.getYDim();
		for (int x = 0; x < xDim; x++)
			for (int y = 0; y < yDim; y++) {
				int label = segmentation.getPixelXYInt(x, y);
				int value = input.getPixelXYInt(x, y);
				ArrayList<Integer> counter = map.get(label);
				if (counter == null) {
					counter = new ArrayList<Integer>(10);
					counter.add(0);
					counter.add(0);
					counter.add(0);
					counter.add(0);
					counter.add(0);
				}
				counter.set(value, counter.get(value) + 1);
				map.put(label, counter);
			}

		/*
		 * On utilise les compteurs pour relabeliser la classification
		 * 
		 */
		for (int x = 0; x < xDim; x++)
			for (int y = 0; y < yDim; y++) {
				int label = segmentation.getPixelXYInt(x, y);
				ArrayList<Integer> counter = map.get(label);

				int max = 0;
				for (int i = 1; i < counter.size(); i++)
					if (counter.get(max) < counter.get(i))
						max = i;

				output.setPixelXYInt(x, y, max);
			}
	}

	private int getKey(int x, int y, int z, int t) {
		return input.getPixelInt(x, y, z, t, 0);
	}

    /**
     * see class header
     */
    public Image exec(Image input, Image segmentation) {
	return (Image)new FilteringLabelsBySegmentation().process(input, segmentation);
    }
}
