package fr.unistra.pelican.algorithms.segmentation.labels;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;

/**
 * This class remove border pixels from a segmentation map by assigning them to
 * the biggest connected region.
 * 
 * Works only in X Y dimension.
 * 
 * @author Lefevre
 */
public class DeleteFrontiers extends Algorithm {

	/**
	 * The input image
	 */
	public Image input;

	/**
	 * The output image
	 */
	public Image output;

	/**
	 * The label of border pixels
	 */
	public static final int WSHED = 0;

	/**
	 * Constructor
	 */
	public DeleteFrontiers() {
		super.inputs = "input";
		super.outputs = "output";
	}

	/**
	 * Remove border pixels from a segmentation map by assigning them to the
	 * biggest connected region.
	 * 
	 * @param input
	 *            The input image
	 * @return The output image
	 */
	public static Image exec(Image input) {
		return (Image) new DeleteFrontiers().process(input);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() {
		int xDim = input.getXDim();
		int yDim = input.getYDim();
		int bDim = input.getBDim();
		int labels[][] = new int[input.getBDim()][];
		for (int b = 0; b < input.getBDim(); b++) {
			// Calcul du nombre de labels dans l'image d'origine
			int max = 0;
			for (int x = 0; x < input.getXDim(); x++)
				for (int y = 0; y < input.getYDim(); y++)
					if (input.getPixelXYBInt(x, y, b) > max)
						max = input.getPixelXYBInt(x, y, b);
			labels[b] = new int[max + 1];
			// Calcul de la taille de chaque region
			for (int i = 0; i < max; i++)
				labels[b][i] = 0;
			for (int x = 0; x < input.getXDim(); x++)
				for (int y = 0; y < input.getYDim(); y++) {
					int label = input.getPixelXYBInt(x, y, b);
					// System.out.println(label);
					labels[b][label]++;
				}
		}

		output = new IntegerImage(input);
		int nbval;
		int tab[] = new int[8];
		int val;
		for (int b = 0; b < bDim; b++)
			for (int x = 0; x < xDim; x++)
				for (int y = 0; y < yDim; y++)
					// On ne traite que les pixels de contours
					if (input.getPixelXYBInt(x, y, b) == WSHED) {
						nbval = 0;
						// for every pixel in the 8-neighbourhood of p
						for (int l = y - 1; l <= y + 1; l++)
							for (int k = x - 1; k <= x + 1; k++) {
								if (k < 0 || k >= input.getXDim() || l < 0
										|| l >= input.getYDim())
									continue;
								if (!(k == x && l == y)
										&& input.getPixelXYBInt(k, l, b) != WSHED) {
									val = input.getPixelXYBInt(k, l, b);
									tab[nbval++] = val;
								}
							}
						// Affectation a la plus grande region voisinne
						int max = 0, imax = -1;
						for (int i = 0; i < nbval; i++)
							if (labels[b][tab[i]] > max) {
								max = labels[b][tab[i]];
								imax = i;
							}
						if (imax != -1)
							output.setPixelXYBInt(x, y, b, tab[imax]);
						else if (nbval == 0)// System.out.println("point
							// isole");
							// Affectation aleatoire a partir des labels des
							// voisins
							output.setPixelXYBInt(x, y, b, tab[(int) (Math
									.random() * nbval)]);

					}

	}

}
