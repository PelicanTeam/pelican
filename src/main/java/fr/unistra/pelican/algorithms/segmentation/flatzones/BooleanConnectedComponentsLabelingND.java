package fr.unistra.pelican.algorithms.segmentation.flatzones;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.IntegerImage;

/**
 * This class performs a labeling of a binary image into connected components.
 * 
 * It uses a fast 2-pass algorithm relying on a correspondance table and offers
 * two options : the connexity used (either CONNEXITY4 or CONNEXITY8) and the
 * possiblity to label background pixels
 * 
 * TODO: add support to integer images (not only boolean images)
 * 
 * @author Lefevre
 */
public class BooleanConnectedComponentsLabelingND extends Algorithm {

	/**
	 * A constant representing the 4-connexity mode
	 */
	public static int CONNEXITY4 = 0;

	/**
	 * A constant representing the 8-connexity mode
	 */
	public static int CONNEXITY8 = 1;

	/**
	 * Input Image
	 */
	public BooleanImage input;

	/**
	 * The type of connexity considered (either CONNEXITY4 or CONNEXITY8)
	 */
	public int connexity = CONNEXITY8;

	/**
	 * Flag to determine if background pixels should also be labeled
	 */
	public boolean background = false;

	/**
	 * Label image
	 */
	public IntegerImage output;

	/**
	 * Number of labels used
	 */
	public int countLabels;

	/*
	 * Private attributes
	 */
	private int nbLabels = 0;

	private Vector<Integer> labels = new Vector<Integer>();

	/**
	 * Constructor
	 */
	public BooleanConnectedComponentsLabelingND() {
		super.inputs = "input";
		super.options = "connexity,background";
		super.outputs = "output,countLabels";
	}

	/**
	 * Performs a labeling of a binary image into connected components.
	 * 
	 * @param input
	 *          The input image
	 * @param connexity
	 *          The type of connexity considered (either CONNEXITY4 or CONNEXITY8)
	 * @param background
	 *          Flag to determine if background pixels should also be labeled
	 * @return The label image
	 */
	public static IntegerImage exec(BooleanImage input, int connexity,
		boolean background) {
		return (IntegerImage) new BooleanConnectedComponentsLabelingND().process(input,
			connexity, background);
	}

	public static IntegerImage exec(BooleanImage input, boolean background) {
		return (IntegerImage) new BooleanConnectedComponentsLabelingND().process(input,
			null, background);
	}

	public static IntegerImage exec(BooleanImage input, int connexity) {
		return (IntegerImage) new BooleanConnectedComponentsLabelingND().process(input,
			connexity);
	}

	/**
	 * Performs a labeling of a binary image into connected components.
	 * 
	 * @param input
	 *          The input image
	 * @return The label image
	 */
	public static IntegerImage exec(BooleanImage input) {
		return (IntegerImage) new BooleanConnectedComponentsLabelingND().process(input);
	}

	public void launch() {
		output = new IntegerImage(input.getXDim(), input.getYDim(),
			input.getZDim(), input.getTDim(), input.getBDim());
		labels.add(0);
		xDim = input.getXDim();
		yDim = input.getYDim();
		zDim = input.getZDim();
		tDim = input.getTDim();
		bDim = input.getBDim();
		for (int i = 0; i < input.size(); i++)
			if (background || input.getPixelBoolean(i))
				this.output.setPixelInt(i, Integer.MAX_VALUE);
			else
				this.output.setPixelInt(i, 0);
		if (connexity == CONNEXITY8) {
			valid = new boolean[3 * 3 * 3 * 3 * 3];
			neighbours = new boolean[3 * 3 * 3 * 3 * 3];
		}
		// Premier parcours
		for (int b = 0; b < bDim; b++)
			for (int t = 0; t < tDim; t++)
				for (int z = 0; z < zDim; z++)
					for (int y = 0; y < yDim; y++)
						for (int x = 0; x < xDim; x++)
							if (background || input.getPixelBoolean(x, y, z, t, b)) {
								if (connexity == CONNEXITY4)
									output.setPixelInt(x, y, z, t, b,
										get4Connexity(x, y, z, t, b));
								else if (connexity == CONNEXITY8)
									output.setPixelInt(x, y, z, t, b,
										get8Connexity(x, y, z, t, b));
							}
		// Simplification de la table d'equivalence
		ArrayList<Integer> alreadyTreatedLabels = new ArrayList<Integer>();
		for (int i = nbLabels; i >= 0; i--) 
		{
			int j = i;
			alreadyTreatedLabels.add(j);
			while (labels.get(j) != j)
			{
				j = labels.get(j);
				// loop treatment
				if(alreadyTreatedLabels.contains(j))
				{
					for(int label=0;label<alreadyTreatedLabels.size();label++)
					{
						labels.set(alreadyTreatedLabels.get(label), i);
					}
					j = i;
				} else
				{
					alreadyTreatedLabels.add(j);
				}
			}
			labels.set(i, j);
			alreadyTreatedLabels.clear();
		}
		// Calcul du nombre de labels et requantification des labels
		int[] labels2 = new int[nbLabels + 1];
		countLabels = 0;
		for (int i = 0; i < nbLabels + 1; i++)
			if (i == labels.get(i)) {
				labels2[i] = countLabels;
				countLabels++;
			}

		output.setProperty("nbRegions", countLabels);
		// Second parcours
		for (int b = 0; b < bDim; b++)
			for (int t = 0; t < tDim; t++)
				for (int z = 0; z < zDim; z++)
					for (int y = 0; y < yDim; y++)
						for (int x = 0; x < xDim; x++)
							if (background || input.getPixelBoolean(x, y, z, t, b))
								output.setPixelInt(x, y, z, t, b, labels2[labels.get(output
									.getPixelInt(x, y, z, t, b))]);
	}

	private int get4Connexity(int x, int y, int z, int t, int b) {
		boolean current = input.getPixelBoolean(x, y, z, t, b);
		int min = Integer.MAX_VALUE;
		boolean val1 = false, val2 = false, val3 = false, val4 = false, val5 = false;
		if (x - 1 >= 0 && current == input.getPixelBoolean(x - 1, y, z, t, b)) {
			min = Math.min(min, labels.get(output.getPixelInt(x - 1, y, z, t, b)));
			val1 = true;
		}
		if (y - 1 >= 0 && current == input.getPixelBoolean(x, y - 1, z, t, b)) {
			min = Math.min(min, labels.get(output.getPixelInt(x, y - 1, z, t, b)));
			val2 = true;
		}
		if (z - 1 >= 0 && current == input.getPixelBoolean(x, y, z - 1, t, b)) {
			min = Math.min(min, labels.get(output.getPixelInt(x, y, z - 1, t, b)));
			val3 = true;
		}
		if (t - 1 >= 0 && current == input.getPixelBoolean(x, y, z, t - 1, b)) {
			min = Math.min(min, labels.get(output.getPixelInt(x, y, z, t - 1, b)));
			val4 = true;
		}
		if (b - 1 >= 0 && current == input.getPixelBoolean(x, y, z, t, b - 1)) {
			min = Math.min(min, labels.get(output.getPixelInt(x, y, z, t, b - 1)));
			val5 = true;
		}
		if (val1 && labels.get(output.getPixelInt(x - 1, y, z, t, b)) != min)
			setTableMin(output.getPixelInt(x - 1, y, z, t, b), min);
		if (val2 && labels.get(output.getPixelInt(x, y - 1, z, t, b)) != min)
			setTableMin(output.getPixelInt(x, y - 1, z, t, b), min);
		if (val3 && labels.get(output.getPixelInt(x, y, z - 1, t, b)) != min)
			setTableMin(output.getPixelInt(x, y, z - 1, t, b), min);
		if (val4 && labels.get(output.getPixelInt(x, y, z, t - 1, b)) != min)
			setTableMin(output.getPixelInt(x, y, z, t - 1, b), min);
		if (val5 && labels.get(output.getPixelInt(x, y, z, t, b - 1)) != min)
			setTableMin(output.getPixelInt(x, y, z, t, b - 1), min);
		if (min == Integer.MAX_VALUE) {
			nbLabels++;
			labels.add(nbLabels);
			return nbLabels;
		} else
			return min;
	}

	private boolean neighbours[];
	private boolean valid[];
	private int xDim, yDim, zDim, tDim, bDim;

	private int get8Connexity(int x, int y, int z, int t, int b) {
		boolean current = input.getPixelBoolean(x, y, z, t, b);
		int min = Integer.MAX_VALUE;
		int k = 0;
		boolean stop = false;
		Arrays.fill(neighbours, false);
		Arrays.fill(valid, false);
		for (int bb = -1; bb <= 1; bb++)
			for (int tt = -1; tt <= 1; tt++)
				for (int zz = -1; zz <= 1; zz++)
					for (int yy = -1; yy <= 1; yy++)
						for (int xx = -1; xx <= 1; xx++)
							if (!stop) {
								k++;
								if (xx == 0 && yy == 0 && zz == 0 && tt == 0 && bb == 0)
									stop = true;
								else if (x + xx >= 0 && y + yy >= 0 && z + zz >= 0
									&& t + tt >= 0 && b + bb >= 0 && x + xx < xDim
									&& y + yy < yDim && z + zz < zDim && t + tt < tDim
									&& b + bb < bDim) {
									valid[k - 1] = true;
									if (current == input.getPixelBoolean(x + xx, y + yy, z + zz,
										t + tt, b + bb)) {
										min = Math.min(min, labels.get(output.getPixelInt(x + xx, y
											+ yy, z + zz, t + tt, b + bb)));
										neighbours[k - 1] = true;
									}
								}
							}
		k = 0;
		stop = false;
		for (int bb = -1; bb <= 1; bb++)
			for (int tt = -1; tt <= 1; tt++)
				for (int zz = -1; zz <= 1; zz++)
					for (int yy = -1; yy <= 1; yy++)
						for (int xx = -1; xx <= 1; xx++)
							if (!stop) {
								k++;
								if (xx == 0 && yy == 0 && zz == 0 && tt == 0 && bb == 0)
									stop = true;
								else if (valid[k - 1]
									&& neighbours[k - 1]
									&& labels.get(output.getPixelInt(x + xx, y + yy, z + zz, t
										+ tt, b + bb)) != min)
									setTableMin(output.getPixelInt(x + xx, y + yy, z + zz,
										t + tt, b + bb), min);
							}
		if (min == Integer.MAX_VALUE) {
			nbLabels++;
			labels.add(nbLabels);
			return nbLabels;
		} else
			return min;
	}

	// private int get8Connexity(int x, int y, int z, int t, int b) {
	// boolean current = input.getPixelBoolean(x, y, z, t, b);
	// int min = Integer.MAX_VALUE;
	// for (int bb = -1; bb <= 0; bb++)
	// for (int tt = -1; tt <= 0; tt++)
	// for (int zz = -1; zz <= 0; zz++)
	// for (int yy = -1; yy <= 0; yy++)
	// for (int xx = -1; xx <= 0; xx++) {
	// if (x + xx >= 0
	// && y + yy >= 0
	// && z + zz >= 0
	// && t + tt >= 0
	// && b + bb >= 0
	// && xx + yy + zz + tt + bb != 0
	// && current == input.getPixelBoolean(x + xx, y + yy, z + zz, t
	// + tt, b + bb)) {
	// min = Math.min(min, labels.get(output.getPixelInt(x + xx, y
	// + yy, z + zz, t + tt, b + bb)));
	// val[xx + 1][yy + 1][zz + 1][tt + 1][bb + 1] = true;
	// } else
	// val[xx + 1][yy + 1][zz + 1][tt + 1][bb + 1] = false;
	// }
	// for (int bb = -1; bb <= 0; bb++)
	// for (int tt = -1; tt <= 0; tt++)
	// for (int zz = -1; zz <= 0; zz++)
	// for (int yy = -1; yy <= 0; yy++)
	// for (int xx = -1; xx <= 0; xx++)
	// if (val[xx + 1][yy + 1][zz + 1][tt + 1][bb + 1]
	// && labels.get(output.getPixelInt(x + xx, y + yy, z + zz,
	// t + tt, b + bb)) != min)
	// setTableMin(output.getPixelInt(x + xx, y + yy, z + zz, t + tt,
	// b + bb), min);
	// if (min == Integer.MAX_VALUE) {
	// nbLabels++;
	// labels.add(nbLabels);
	// return nbLabels;
	// } else
	// return min;
	// }

	private void setTableMin(int u, int min) {
		int v = labels.get(u);
		while (u != v) {
			labels.set(u, min);
			u = v;
			v = labels.get(v);
		}
		labels.set(u, min);
	}

}