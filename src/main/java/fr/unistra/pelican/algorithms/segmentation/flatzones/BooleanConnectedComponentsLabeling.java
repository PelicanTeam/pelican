package fr.unistra.pelican.algorithms.segmentation.flatzones;

import java.util.ArrayList;
import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToRandomColors;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

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
public class BooleanConnectedComponentsLabeling extends Algorithm {

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
	public Image input;

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

	private ArrayList<Integer> labels = new ArrayList<Integer>();

	/**
	 * Constructor
	 */
	public BooleanConnectedComponentsLabeling() {
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
	public static IntegerImage exec(Image input, int connexity, boolean background) {
		return (IntegerImage) new BooleanConnectedComponentsLabeling().process(input,
			connexity, background);
	}

	public static IntegerImage exec(Image input, boolean background) {
		return (IntegerImage) new BooleanConnectedComponentsLabeling().process(input,
			null, background);
	}

	public static IntegerImage exec(Image input, int connexity) {
		return (IntegerImage) new BooleanConnectedComponentsLabeling().process(input,
			connexity);
	}

	/**
	 * Performs a labeling of a binary image into connected components.
	 * 
	 * @param input
	 *          The input image
	 * @return The label image
	 */
	public static IntegerImage exec(Image input) {
		return (IntegerImage) new BooleanConnectedComponentsLabeling().process(input);
	}

	public void launch() {
		int xDim = input.getXDim();
		int yDim = input.getYDim();
		int zDim = input.getZDim();
		int tDim = input.getTDim();
		int bDim = input.getBDim();		
		output = new IntegerImage(xDim, yDim, zDim, tDim, bDim);
		labels.add(0);
		// Cas binaire
		if (input instanceof BooleanImage) {
			// Initialisation
			for (int i = 0; i < input.size(); i++)
				if (background || input.getPixelBoolean(i))
					this.output.setPixelInt(i, Integer.MAX_VALUE);
				else
					this.output.setPixelInt(i, 0);
			// Premier parcours
			for (int b = 0; b < bDim; b++)
				for (int t = 0; t < tDim; t++)
					for (int z = 0; z < zDim; z++)
						for (int y = 0; y < yDim; y++)
							for (int x = 0; x < xDim; x++)
								if (background || input.getPixelBoolean(x, y, z, t, b)) {
									if (connexity == CONNEXITY4)
										output.setPixelInt(x, y, z, t, b, get4Connexity(x, y, z, t,
											b));
									else if (connexity == CONNEXITY8)
										output.setPixelInt(x, y, z, t, b, get8Connexity(x, y, z, t,
											b));
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
			int firstLabel=0;
			for (int i = firstLabel; i < nbLabels + 1; i++)
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
		// Cas d'une image label : fonctionne pour des ByteImage et IntegerImage (label)
		else {
			int bg=Integer.MIN_VALUE;
			if(input instanceof IntegerImage)
				bg=0;
			// Initialisation
			for (int i = input.size(); --i>=0 ;)
				if (background || input.getPixelInt(i)!=bg)
					this.output.setPixelInt(i, Integer.MAX_VALUE);
				else
					this.output.setPixelInt(i, 0);
			// Premier parcours
			for (int b = 0; b < bDim; b++)
				for (int t = 0; t < tDim; t++)
					for (int z = 0; z < zDim; z++)
						for (int y = 0; y < yDim; y++)
							for (int x = 0; x < xDim; x++)
								if (background || input.getPixelInt(x, y, z, t, b) != bg) {
									if (connexity == CONNEXITY4)
										output.setPixelInt(x, y, z, t, b, get4ConnexityLabel(x, y,
											z, t, b));
									else if (connexity == CONNEXITY8)
										output.setPixelInt(x, y, z, t, b, get8ConnexityLabel(x, y,
											z, t, b));
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
			int firstLabel=0;
			for (int i = firstLabel; i < nbLabels + 1; i++)
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
								if (background || input.getPixelInt(x, y, z, t, b) != bg)
									output.setPixelInt(x, y, z, t, b, labels2[labels.get(output
										.getPixelInt(x, y, z, t, b))]);
		}
	}

	private final int get4Connexity(int x, int y, int z, int t, int b) {
		boolean current = input.getPixelBoolean(x, y, z, t, b);
		int min = Integer.MAX_VALUE;
		boolean val1 = false, val2 = false;
		if (y - 1 >= 0 && current == input.getPixelBoolean(x, y - 1, z, t, b)) {
			min = Math.min(min, labels.get(output.getPixelInt(x, y - 1, z, t, b)));
			val1 = true;
		}
		if (x - 1 >= 0 && current == input.getPixelBoolean(x - 1, y, z, t, b)) {
			min = Math.min(min, labels.get(output.getPixelInt(x - 1, y, z, t, b)));
			val2 = true;
		}
		if (val1 && labels.get(output.getPixelInt(x, y - 1, z, t, b)) != min)
			setTableMin(output.getPixelInt(x, y - 1, z, t, b), min);
		if (val2 && labels.get(output.getPixelInt(x - 1, y, z, t, b)) != min)
			setTableMin(output.getPixelInt(x - 1, y, z, t, b), min);
		if (min == Integer.MAX_VALUE) {
			labels.add(++nbLabels);
			return nbLabels;
		} else
			return min;
	}

	private final int get8Connexity(int x, int y, int z, int t, int b) {
		boolean current = input.getPixelBoolean(x, y, z, t, b);
		int min = Integer.MAX_VALUE;
		boolean val1 = false, val2 = false, val3 = false, val4 = false;
		if (x - 1 >= 0 && y - 1 >= 0
			&& current == input.getPixelBoolean(x - 1, y - 1, z, t, b)) {
			min = Math
				.min(min, labels.get(output.getPixelInt(x - 1, y - 1, z, t, b)));
			val1 = true;
		}
		if (y - 1 >= 0 && current == input.getPixelBoolean(x, y - 1, z, t, b)) {
			min = Math.min(min, labels.get(output.getPixelInt(x, y - 1, z, t, b)));
			val2 = true;
		}
		if (y - 1 >= 0 && x + 1 < input.getXDim()
			&& current == input.getPixelBoolean(x + 1, y - 1, z, t, b)) {
			min = Math
				.min(min, labels.get(output.getPixelInt(x + 1, y - 1, z, t, b)));
			val3 = true;
		}
		if (x - 1 >= 0 && current == input.getPixelBoolean(x - 1, y, z, t, b)) {
			min = Math.min(min, labels.get(output.getPixelInt(x - 1, y, z, t, b)));
			val4 = true;
		}
		if (val1 && labels.get(output.getPixelInt(x - 1, y - 1, z, t, b)) != min)
			setTableMin(output.getPixelInt(x - 1, y - 1, z, t, b), min);
		if (val2 && labels.get(output.getPixelInt(x, y - 1, z, t, b)) != min)
			setTableMin(output.getPixelInt(x, y - 1, z, t, b), min);
		if (val3 && labels.get(output.getPixelInt(x + 1, y - 1, z, t, b)) != min)
			setTableMin(output.getPixelInt(x + 1, y - 1, z, t, b), min);
		if (val4 && labels.get(output.getPixelInt(x - 1, y, z, t, b)) != min)
			setTableMin(output.getPixelInt(x - 1, y, z, t, b), min);
		if (min == Integer.MAX_VALUE) {
			labels.add(++nbLabels);
			return nbLabels;
		} else
			return min;
	}

	private final int get4ConnexityLabel(int x, int y, int z, int t, int b) {
		int current = input.getPixelInt(x, y, z, t, b);
		int min = Integer.MAX_VALUE;
		boolean val1 = false, val2 = false;
		if (y - 1 >= 0 && current == input.getPixelInt(x, y - 1, z, t, b)) {
			min = Math.min(min, labels.get(output.getPixelInt(x, y - 1, z, t, b)));
			val1 = true;
		}
		if (x - 1 >= 0 && current == input.getPixelInt(x - 1, y, z, t, b)) {
			min = Math.min(min, labels.get(output.getPixelInt(x - 1, y, z, t, b)));
			val2 = true;
		}
		if (val1 && labels.get(output.getPixelInt(x, y - 1, z, t, b)) != min)
			setTableMin(output.getPixelInt(x, y - 1, z, t, b), min);
		if (val2 && labels.get(output.getPixelInt(x - 1, y, z, t, b)) != min)
			setTableMin(output.getPixelInt(x - 1, y, z, t, b), min);
		if (min == Integer.MAX_VALUE) {
			labels.add(++nbLabels);
			return nbLabels;
		} else
			return min;
	}

	private final int get8ConnexityLabel(int x, int y, int z, int t, int b) {
		int current = input.getPixelInt(x, y, z, t, b);
		int min = Integer.MAX_VALUE;
		boolean val1 = false, val2 = false, val3 = false, val4 = false;
		if (x - 1 >= 0 && y - 1 >= 0
			&& current == input.getPixelInt(x - 1, y - 1, z, t, b)) {
			min = Math
				.min(min, labels.get(output.getPixelInt(x - 1, y - 1, z, t, b)));
			val1 = true;
		}
		if (y - 1 >= 0 && current == input.getPixelInt(x, y - 1, z, t, b)) {
			min = Math.min(min, labels.get(output.getPixelInt(x, y - 1, z, t, b)));
			val2 = true;
		}
		if (y - 1 >= 0 && x + 1 < input.getXDim()
			&& current == input.getPixelInt(x + 1, y - 1, z, t, b)) {
			min = Math
				.min(min, labels.get(output.getPixelInt(x + 1, y - 1, z, t, b)));
			val3 = true;
		}
		if (x - 1 >= 0 && current == input.getPixelInt(x - 1, y, z, t, b)) {
			min = Math.min(min, labels.get(output.getPixelInt(x - 1, y, z, t, b)));
			val4 = true;
		}
		if (val1 && labels.get(output.getPixelInt(x - 1, y - 1, z, t, b)) != min)
			setTableMin(output.getPixelInt(x - 1, y - 1, z, t, b), min);
		if (val2 && labels.get(output.getPixelInt(x, y - 1, z, t, b)) != min)
			setTableMin(output.getPixelInt(x, y - 1, z, t, b), min);
		if (val3 && labels.get(output.getPixelInt(x + 1, y - 1, z, t, b)) != min)
			setTableMin(output.getPixelInt(x + 1, y - 1, z, t, b), min);
		if (val4 && labels.get(output.getPixelInt(x - 1, y, z, t, b)) != min)
			setTableMin(output.getPixelInt(x - 1, y, z, t, b), min);
		if (min == Integer.MAX_VALUE) {
			labels.add(++nbLabels);
			return nbLabels;
		} else
			return min;
	}

	private final void setTableMin(int u, int min) {
		int v = labels.get(u);
		while (u != v) {
			labels.set(u, min);
			u = v;
			v = labels.get(v);
		}
		labels.set(u, min);
	}

}