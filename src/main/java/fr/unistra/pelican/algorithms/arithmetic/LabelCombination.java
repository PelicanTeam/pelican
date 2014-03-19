package fr.unistra.pelican.algorithms.arithmetic;

import java.util.TreeSet;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;

/**
 * Computes a new multiband binary image from a combination of the different
 * labels in the input image
 * 
 * @author Lefevre
 */
public class LabelCombination extends Algorithm {

	/**
	 * Input image
	 */
	public IntegerImage inputImage;

	/**
	 * Number of combinations
	 */
	public int combinations;

	/**
	 * Flag to consider background as label
	 */
	public boolean background = true;

	/**
	 * Output image
	 */
	public BooleanImage outputImage;

	/**
	 * Constructor
	 */
	public LabelCombination() {
		super.inputs = "inputImage,combinations";
		super.outputs = "outputImage";
		super.options = "background";
	}

	private int binomial(int n, int k) {
		if (n < k)
			return 0;
		int max = Math.max(k, n - k);
		int min = Math.min(k, n - k);
		int result = 1;
		int div = 1;
		for (int i = max + 1; i <= n; i++)
			result *= i;
		for (int i = 2; i <= min; i++)
			div *= i;
		return result / div;
	}

	public void launch() throws AlgorithmException {
		// Count the number of labels
		TreeSet<Integer> set = new TreeSet<Integer>();
		for (int p = 0; p < inputImage.size(); p++)
			if (background || inputImage.getPixelInt(p) != 0)
				set.add(inputImage.getPixelInt(p));
		int labels = set.size();
		Integer[] tab = new Integer[labels];
		tab = set.toArray(tab);
		// FIXME: accept combinations > 5
		int s = 0;
		int comb = Math.min(combinations, 5);
		int size = 0;
		for (int i = 1; i <= comb; i++) size += binomial(labels, i);

		outputImage = new BooleanImage(inputImage.getXDim(), inputImage.getYDim(),
			inputImage.getZDim(), inputImage.getTDim(), size);
		outputImage.setMask( inputImage.getMask() );
		// Build the combinations
		Image temp = null;
		// Images with one cluster
		if (comb >= 1)
			for (int c1 = 0; c1 < labels; c1++) {
				temp = new BooleanImage(inputImage.getXDim(), inputImage.getYDim(),
					inputImage.getZDim(), inputImage.getTDim(), 1);
				// System.out.println("cluster " + c1);
				for (int j = 0; j < inputImage.size(); j++) {
					if ( inputImage.isPresent(j) )
					if (background || inputImage.getPixelInt(j) != 0)
						if (inputImage.getPixelInt(j) == tab[c1])// c1+start)
							temp.setPixelBoolean(j, true);
						else
							temp.setPixelBoolean(j, false);
				}
				outputImage.setImage4D(temp, s++, Image.B);
			}
		// Images with two clusters
		if (comb >= 2)
			for (int c1 = 0; c1 < labels; c1++)
				for (int c2 = c1 + 1; c2 < labels; c2++) {
					temp = new BooleanImage(inputImage.getXDim(), inputImage.getYDim(),
						inputImage.getZDim(), inputImage.getTDim(), 1);
					// System.out.println("cluster " + c1 + ":" + c2);
					for (int j = 0; j < inputImage.size(); j++) {
						if ( inputImage.isPresent(j) )
						if (background || inputImage.getPixelInt(j) != 0)
							if (inputImage.getPixelInt(j) == tab[c1]// c1 + start
								|| inputImage.getPixelInt(j) == tab[c2])// c2 + start)
								temp.setPixelBoolean(j, true);
							else
								temp.setPixelBoolean(j, false);
					}
					outputImage.setImage4D(temp, s++, Image.B);
				}
		// Images with three clusters
		if (comb >= 3)
			for (int c1 = 0; c1 < labels; c1++)
				for (int c2 = c1 + 1; c2 < labels; c2++)
					for (int c3 = c2 + 1; c3 < labels; c3++) {
						temp = new BooleanImage(inputImage.getXDim(), inputImage.getYDim(),
							inputImage.getZDim(), inputImage.getTDim(), 1);
						// System.out.println("cluster " + c1 + ":" + c2 + ":" + c3);
						for (int j = 0; j < inputImage.size(); j++) {
							if ( inputImage.isPresent(j) )
							if (background || inputImage.getPixelInt(j) != 0)
								if (inputImage.getPixelInt(j) == tab[c1]// c1 + start
									|| inputImage.getPixelInt(j) == tab[c2]// c2 + start
									|| inputImage.getPixelInt(j) == tab[c3])// c3 + start)
									temp.setPixelBoolean(j, true);
								else
									temp.setPixelBoolean(j, false);
						}
						outputImage.setImage4D(temp, s++, Image.B);
					}
		// Images with four clusters
		if (comb >= 4)
			for (int c1 = 0; c1 < labels; c1++)
				for (int c2 = c1 + 1; c2 < labels; c2++)
					for (int c3 = c2 + 1; c3 < labels; c3++)
						for (int c4 = c3 + 1; c4 < labels; c4++) {
							temp = new BooleanImage(inputImage.getXDim(), inputImage
								.getYDim(), inputImage.getZDim(), inputImage.getTDim(), 1);
							// System.out.println("cluster " + c1 + ":" + c2 + ":" + c3 + ":"+
							// c4);
							for (int j = 0; j < inputImage.size(); j++) {
								if ( inputImage.isPresent(j) )
								if (background || inputImage.getPixelInt(j) != 0)
									if (inputImage.getPixelInt(j) == tab[c1]// c1 + start
										|| inputImage.getPixelInt(j) == tab[c2]// c2 + start
										|| inputImage.getPixelInt(j) == tab[c3]// c3 + start
										|| inputImage.getPixelInt(j) == tab[c4])// c4 + start)
										temp.setPixelBoolean(j, true);
									else
										temp.setPixelBoolean(j, false);
							}
							outputImage.setImage4D(temp, s++, Image.B);
						}
		// Images with five clusters
		if (comb >= 5)
			for (int c1 = 0; c1 < labels; c1++)
				for (int c2 = c1 + 1; c2 < labels; c2++)
					for (int c3 = c2 + 1; c3 < labels; c3++)
						for (int c4 = c3 + 1; c4 < labels; c4++)
							for (int c5 = c4 + 1; c5 < labels; c5++) {
								temp = new BooleanImage(inputImage.getXDim(), inputImage
									.getYDim(), inputImage.getZDim(), inputImage.getTDim(), 1);
								// System.out.println("cluster " + c1 + ":" + c2 + ":" + c3 +
								// ":"+ c4 + ":" + c5);
								for (int j = 0; j < inputImage.size(); j++) {
									if ( inputImage.isPresent(j) )
									if (background || inputImage.getPixelInt(j) != 0)
										if (inputImage.getPixelInt(j) == tab[c1]// c1 + start
											|| inputImage.getPixelInt(j) == tab[c2]// c2 + start
											|| inputImage.getPixelInt(j) == tab[c3]// c3 + start
											|| inputImage.getPixelInt(j) == tab[c4]// c4 + start
											|| inputImage.getPixelInt(j) == tab[c5])// c5 + start)
											temp.setPixelBoolean(j, true);
										else
											temp.setPixelBoolean(j, false);
								}
								outputImage.setImage4D(temp, s++, Image.B);
							}
	}

	/**
	 * Computes a new boolean image from a combination of the different labels in
	 * the input image
	 * 
	 * @param inputImage
	 *          Input Image
	 * @param combinations
	 *          Number of combinations
	 * @return outputImage Output image
	 */
	public static BooleanImage exec(IntegerImage inputImage, int combinations) {
		return (BooleanImage) new LabelCombination().process(inputImage,
			combinations);
	}

	/**
	 * Computes a new boolean image from a combination of the different labels in
	 * the input image
	 * 
	 * @param inputImage
	 *          Input Image
	 * @param combinations
	 *          Number of combinations
	 * @param background
	 *          Consider also the background
	 * @return outputImage Output image
	 */
	public static BooleanImage exec(IntegerImage inputImage, int combinations,
		boolean background) {
		return (BooleanImage) new LabelCombination().process(inputImage,
			combinations, background);
	}
}
