package fr.unistra.pelican.algorithms.segmentation.qfz.gray;


import java.util.ArrayList;
import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;

/**
 * This class performs a fast alpha-connexity algorithm.
 * 
 * It uses a fast 2-pass algorithm relying on a correspondance table
 * 
 * Alpha is in Byte precision.
 * 
 * @author Lefevre, Jonathan Weber
 */
public class FastGrayAlphaCC extends Algorithm {

	/**
	 * A constant representing the 4-connexity mode
	 */
	public static int CONNEXITY4 = 0;

	/**
	 * A constant representing the 8-connexity mode
	 */
	public static int CONNEXITY8 = 1;
	
	/**
	 * A constant representing the 6-temporal connexity mode
	 */
	public static int CONNEXITY6TEMPORAL = 2;
	
	/**
	 * A constant representing the 10-temporal connexity mode
	 */
	public static int CONNEXITY10TEMPORAL = 3;

	/**
	 * Input Image
	 */
	public Image input;

	/**
	 * The type of connexity considered (either CONNEXITY4 or CONNEXITY8)
	 */
	public int connexity = CONNEXITY8;
	
	/**
	 * Alpha Value
	 */
	public int alpha;

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
	
	private int current;
	private int min;
	private int xm1ym1Index;
	private int ym1Index;
	private int xp1ym1Index;
	private int xm1Index;
	private int tm1Index;
	private boolean val1, val2, val3, val4, val5;
	
	private int xDim;
	private int yDim;
	private int zDim;
	private int tDim;

	/**
	 * Constructor
	 */
	public FastGrayAlphaCC() {
		super.inputs = "input,connexity,alpha";
		super.options = "";
		super.outputs = "output,countLabels";
	}

	/**
	 * Performs a labeling of a binary image into connected components.
	 * 
	 * @param input
	 *          The input image
	 * @param connexity
	 *          The type of connexity considered
	 * @param alpha
	 * @return The label image
	 */
	public static IntegerImage exec(Image input, int connexity,int alpha) {
		return (IntegerImage) new FastGrayAlphaCC().process(input,
			connexity, alpha);
	}
	
	/**
	 * Performs a labeling of a binary image into connected components.
	 * 
	 * @param input
	 *          The input image
	 * @param connexity
	 *          The type of connexity considered
	 * @param alpha
	 * @return The label image
	 * @return number of labels
	 */
	public static ArrayList<Object> execAll(Image input, int connexity, int alpha) {
		return new FastGrayAlphaCC().processAll(input,	connexity, alpha);
	}

	public void launch() 
	{
		if(input.getBDim()==1)		{
		
			xDim = input.getXDim();
			yDim = input.getYDim();
			zDim = input.getZDim();
			tDim = input.getTDim();
			output = input.newIntegerImage();
			labels.add(0);
			
			int index=-1;

			// Initialisation
			for (int i = input.size(); --i>=0 ;)
			{
					this.output.setPixelInt(i, Integer.MAX_VALUE);
			}
			// Premier parcours
			if (connexity == CONNEXITY4)
			{
				for (int t = 0; t < tDim; t++)
					for (int z = 0; z < zDim; z++)
						for (int y = 0; y < yDim; y++)
							for (int x = 0; x < xDim; x++)
							{
								output.setPixelInt(++index, get4ConnexityLabel(x, y, z, t));
							}
			} 
			else if (connexity == CONNEXITY8)
			{
				for (int t = 0; t < tDim; t++)
					for (int z = 0; z < zDim; z++)
						for (int y = 0; y < yDim; y++)
							for (int x = 0; x < xDim; x++)
							{
								output.setPixelInt(++index, get8ConnexityLabel(x, y, z, t));
							}
			}
			else if (connexity == CONNEXITY6TEMPORAL)
			{
				for (int t = 0; t < tDim; t++)
					for (int z = 0; z < zDim; z++)
						for (int y = 0; y < yDim; y++)
							for (int x = 0; x < xDim; x++)
							{
								output.setPixelInt(++index, get6TemporalConnexityLabel(x, y, z, t));
							}
			}
			else if (connexity == CONNEXITY10TEMPORAL)
			{
				for (int t = 0; t < tDim; t++)
					for (int z = 0; z < zDim; z++)
						for (int y = 0; y < yDim; y++)
							for (int x = 0; x < xDim; x++)
							{
								output.setPixelInt(++index, get10TemporalConnexityLabel(x, y, z, t));
							}
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
				if (i == labels.get(i)) 
				{
					labels2[i] = countLabels++;
				}
			output.setProperty("nbRegions", countLabels);
			// Second parcours
			for(int i = output.size();--i>=0;)
			{
				output.setPixelInt(i, labels2[labels.get(output.getPixelInt(i))]);
			}
		}
		else
		{
			throw new AlgorithmException("inputImage must be a gray level image");
		}
	}



	private final int get4ConnexityLabel(int x, int y, int z, int t) {
		current = input.getPixelXYZTByte(x, y, z, t);
		min = Integer.MAX_VALUE;
		val1 = false;
		val2 = false;
		ym1Index = input.getLinearIndexXYZT_(x, y - 1, z, t);
		xm1Index = input.getLinearIndexXYZT_(x - 1, y, z, t);
		if (y - 1 >= 0 && Math.abs(current - input.getPixelByte( ym1Index))<=alpha) {
			min = Math.min(min, labels.get(output.getPixelInt( ym1Index)));
			val1 = true;
		}
		if (x - 1 >= 0 && Math.abs(current - input.getPixelByte(xm1Index))<=alpha) {
			min = Math.min(min, labels.get(output.getPixelInt(xm1Index)));
			val2 = true;
		}
		if (val1 && labels.get(output.getPixelInt( ym1Index)) != min)
			setTableMin(output.getPixelInt( ym1Index), min);
		if (val2 && labels.get(output.getPixelInt(xm1Index)) != min)
			setTableMin(output.getPixelInt(xm1Index), min);
		if (min == Integer.MAX_VALUE) {
			labels.add(++nbLabels);
			return nbLabels;
		} else
			return min;
	}

	private final int get8ConnexityLabel(int x, int y, int z, int t) {
		current = input.getPixelXYZTByte(x, y, z, t);
		min = Integer.MAX_VALUE;
		xm1ym1Index = input.getLinearIndexXYZT_(x - 1, y - 1, z, t);
		ym1Index = input.getLinearIndexXYZT_(x, y - 1, z, t);
		xp1ym1Index = input.getLinearIndexXYZT_(x + 1, y - 1, z, t);
		xm1Index = input.getLinearIndexXYZT_(x - 1, y, z, t);
		val1 = false;
		val2 = false;
		val3 = false;
		val4 = false;
		if (x - 1 >= 0 && y - 1 >= 0
			&& Math.abs(current - input.getPixelByte(xm1ym1Index))<=alpha) {
			min = Math
				.min(min, labels.get(output.getPixelInt(xm1ym1Index)));
			val1 = true;
		}
		if (y - 1 >= 0 && Math.abs(current - input.getPixelByte(ym1Index))<=alpha) {
			min = Math.min(min, labels.get(output.getPixelInt(ym1Index)));
			val2 = true;
		}
		if (y - 1 >= 0 && x + 1 < xDim
			&& Math.abs(current - input.getPixelByte(xp1ym1Index))<=alpha) {
			min = Math
				.min(min, labels.get(output.getPixelInt(xp1ym1Index)));
			val3 = true;
		}
		if (x - 1 >= 0 && Math.abs(current - input.getPixelByte(xm1Index))<=alpha) {
			min = Math.min(min, labels.get(output.getPixelInt(xm1Index)));
			val4 = true;
		}
		if (val1 && labels.get(output.getPixelInt(xm1ym1Index)) != min)
			setTableMin(output.getPixelInt(xm1ym1Index), min);
		if (val2 && labels.get(output.getPixelInt(ym1Index)) != min)
			setTableMin(output.getPixelInt(ym1Index), min);
		if (val3 && labels.get(output.getPixelInt(xp1ym1Index)) != min)
			setTableMin(output.getPixelInt(xp1ym1Index), min);
		if (val4 && labels.get(output.getPixelInt(xm1Index)) != min)
			setTableMin(output.getPixelInt(xm1Index), min);
		if (min == Integer.MAX_VALUE) {
			labels.add(++nbLabels);
			return nbLabels;
		} else
			return min;
	}

	private final int get6TemporalConnexityLabel(int x, int y, int z, int t) 
	{
		current = input.getPixelXYZTByte(x, y, z, t);
		min = Integer.MAX_VALUE;
		val1 = false;
		val2 = false;
		val3 = false;
		ym1Index = input.getLinearIndexXYZT_(x, y - 1, z, t);
		xm1Index = input.getLinearIndexXYZT_(x - 1, y, z, t);
		tm1Index = input.getLinearIndexXYZT_(x, y, z, t-1);
		if (y - 1 >= 0 && Math.abs(current - input.getPixelByte( ym1Index))<=alpha) {
			min = Math.min(min, labels.get(output.getPixelInt( ym1Index)));
			val1 = true;
		}
		if (x - 1 >= 0 && Math.abs(current - input.getPixelByte(xm1Index))<=alpha) {
			min = Math.min(min, labels.get(output.getPixelInt(xm1Index)));
			val2 = true;
		}
		if (t - 1 >= 0 && Math.abs(current - input.getPixelByte(tm1Index))<=alpha) {
			min = Math.min(min, labels.get(output.getPixelInt(tm1Index)));
			val3 = true;
		}
		if (val1 && labels.get(output.getPixelInt( ym1Index)) != min)
			setTableMin(output.getPixelInt( ym1Index), min);
		if (val2 && labels.get(output.getPixelInt(xm1Index)) != min)
			setTableMin(output.getPixelInt(xm1Index), min);
		if (val3 && labels.get(output.getPixelInt(tm1Index)) != min)
			setTableMin(output.getPixelInt(tm1Index), min);
		if (min == Integer.MAX_VALUE) {
			labels.add(++nbLabels);
			return nbLabels;
		} else
			return min;
	}

	private final int get10TemporalConnexityLabel(int x, int y, int z, int t) {
		current = input.getPixelXYZTByte(x, y, z, t);
		min = Integer.MAX_VALUE;
		xm1ym1Index = input.getLinearIndexXYZT_(x - 1, y - 1, z, t);
		ym1Index = input.getLinearIndexXYZT_(x, y - 1, z, t);
		xp1ym1Index = input.getLinearIndexXYZT_(x + 1, y - 1, z, t);
		xm1Index = input.getLinearIndexXYZT_(x - 1, y, z, t);
		tm1Index = input.getLinearIndexXYZT_(x, y, z, t - 1);
		val1 = false;
		val2 = false;
		val3 = false;
		val4 = false;
		val5 = false;
		if (x - 1 >= 0 && y - 1 >= 0
			&& Math.abs(current - input.getPixelByte(xm1ym1Index))<=alpha) {
			min = Math
				.min(min, labels.get(output.getPixelInt(xm1ym1Index)));
			val1 = true;
		}
		if (y - 1 >= 0 && Math.abs(current - input.getPixelByte(ym1Index))<=alpha) {
			min = Math.min(min, labels.get(output.getPixelInt(ym1Index)));
			val2 = true;
		}
		if (y - 1 >= 0 && x + 1 < xDim
			&& Math.abs(current - input.getPixelByte(xp1ym1Index))<=alpha) {
			min = Math
				.min(min, labels.get(output.getPixelInt(xp1ym1Index)));
			val3 = true;
		}
		if (x - 1 >= 0 && Math.abs(current - input.getPixelByte(xm1Index))<=alpha) {
			min = Math.min(min, labels.get(output.getPixelInt(xm1Index)));
			val4 = true;
		}
		if (t - 1 >= 0 && Math.abs(current - input.getPixelByte(tm1Index))<=alpha) {
			min = Math.min(min, labels.get(output.getPixelInt(tm1Index)));
			val5 = true;
		}
		if (val1 && labels.get(output.getPixelInt(xm1ym1Index)) != min)
			setTableMin(output.getPixelInt(xm1ym1Index), min);
		if (val2 && labels.get(output.getPixelInt(ym1Index)) != min)
			setTableMin(output.getPixelInt(ym1Index), min);
		if (val3 && labels.get(output.getPixelInt(xp1ym1Index)) != min)
			setTableMin(output.getPixelInt(xp1ym1Index), min);
		if (val4 && labels.get(output.getPixelInt(xm1Index)) != min)
			setTableMin(output.getPixelInt(xm1Index), min);
		if (val5 && labels.get(output.getPixelInt(tm1Index)) != min)
			setTableMin(output.getPixelInt(tm1Index), min);
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
