package fr.unistra.pelican.algorithms.statistics;

import java.util.Properties;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.conversion.AverageChannels;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.logical.AND;
import fr.unistra.pelican.algorithms.logical.CompareImage;
import fr.unistra.pelican.algorithms.logical.OR;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

/**
 * This class computes Confusion Matrix and related statistics between two
 * segmentations
 * 
 * @author Lefevre
 */

public class ConfusionMatrix extends Algorithm {

	/**
	 * Vrais positifs
	 */
	public static final int VP = 0;
	/**
	 * Vrais négatifs
	 */
	public static final int VN = 1;
	/**
	 * Faux positifs
	 */
	public static final int FP = 2;
	/**
	 * Faux négatifs
	 */
	public static final int FN = 3;
	/**
	 * Qualité producteur
	 */
	public static final int PROD = 4;
	/**
	 * Qualité utilisateur
	 */
	public static final int UTIL = 5;
	/**
	 * Precision globale
	 */
	public static final int PRECISION = 6;
	/**
	 * Indice kappa
	 */
	public static final int KAPPA = 7;
	/**
	 * Sensitivity (recall)
	 */
	public static final int SENSITIVITY = 8;
	/**
	 * Specificity
	 */
	public static final int SPECIFICITY = 9;
	/**
	 * Jaccard similarity index (surface distance)
	 */
	public static final int JACCARD = 10;
	/**
	 * Dice coefficient (F-measure, Soerensen index)
	 */
	public static final int DICE = 11;

	/**
	 * Reference image
	 */
	public Image reference;

	/**
	 * Evaluated image
	 */
	public Image result;

	/**
	 * (optional) display the stats
	 */
	public boolean display = false;

	/**
	 * (optional) store properties as String objects
	 */
	public boolean string = false;

	/**
	 * The output value as a set of properties
	 */
	public Properties output;

	/**
	 * This class computes Confusion Matrix and related statistics between two
	 * segmentations
	 * 
	 * @param reference
	 *            Reference image
	 * @param result
	 *            Evaluated image
	 * @return various measures stored in a Properties object
	 */
	public static Properties exec(Image reference, Image result) {
		return (Properties) new ConfusionMatrix().process(reference, result);
	}

	public static Properties exec(Image reference, Image result, Boolean display) {
		return (Properties) new ConfusionMatrix().process(reference, result,
				display);
	}

	public static Properties exec(Image reference, Image result,
			Boolean display, Boolean string) {
		return (Properties) new ConfusionMatrix().process(reference, result,
				display, string);
	}

	/**
	 * Constructor
	 */
	public ConfusionMatrix() {
		super.inputs = "reference,result";
		super.outputs = "output";
		super.options = "display,string";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		StringBuffer out = new StringBuffer();
		output = new Properties();
		// Binarisation
		result = new BooleanImage(result, true);
		reference = new BooleanImage(reference, true);
		double total = result.size();
		// Classif
		double resultFG = 0;
		for (int p = 0; p < result.size(); p++)
			if (result.getPixelBoolean(p))
				resultFG++;
		double resultBG = result.size() - resultFG;
		// Terrain
		double referenceFG = 0;
		for (int p = 0; p < reference.size(); p++)
			if (reference.getPixelBoolean(p))
				referenceFG++;
		double referenceBG = result.size() - referenceFG;
		// Vrais positifs
		Image vpImg = AND.exec(result, reference); // correct
		int vp = 0;
		for (int p = 0; p < vpImg.size(); p++)
			if (vpImg.getPixelBoolean(p))
				vp++;
		// Faux positifs
		Image fpImg = CompareImage.exec(result, reference, CompareImage.SUP);
		int fp = 0;
		for (int p = 0; p < fpImg.size(); p++)
			if (fpImg.getPixelBoolean(p))
				fp++;
		// Faux negatifs
		Image fnImg = CompareImage.exec(reference, result, CompareImage.SUP);
		int fn = 0;
		for (int p = 0; p < fnImg.size(); p++)
			if (fnImg.getPixelBoolean(p))
				fn++;
		// Vrais négatifs
		Image vnImg = OR.exec(result, reference); // fond
		int vn = 0;
		for (int p = 0; p < vnImg.size(); p++)
			if (!vnImg.getPixelBoolean(p))
				vn++;

		out.append("\nEvaluation par pixels");
		// Matrice de confusion
		out.append("\n**** Matrice de confusion ****");
		out.append("\n       fg   bg    total");
		out.append("\nfg   " + vp + "    " + fp + "    " + (int) referenceFG);
		out.append("\nbg   " + fn + "    " + vn + "    " + (int) referenceBG);
		out.append("\ntotal  " + (int) resultFG + "    " + (int) resultBG);
		// Exactitude producteur
		out.append("\n**** Qualité producteur ****");
		out.append("\n\t fg = " + vp / resultFG); // sensitivity
		double sensitivity = vp / resultFG;
		out.append("\n\t bg = " + vn / resultBG); // specificity
		double specificity = vn / resultBG;
		double prod = vp / resultFG;
		// Exactitude utilisateur
		out.append("\n**** Qualité utilisateur ****");
		out.append("\n\t fg = " + vp / referenceFG);
		out.append("\n\t bg = " + vn / referenceBG);
		double util = vp / referenceFG;
		// Precision globale
		double precision = (vp + vn) / total;
		out.append("\n**** Mesures globales ****");
		out.append("\n\t précision = " + precision);
		// Indice Kappa
		double chance = (referenceFG * resultFG) / (total * total)
				+ (referenceBG * resultBG) / (total * total);
		double kappa = (precision - chance) / (1 - chance);
		out.append("\n\t indice kappa = " + kappa);
		// Jaccard similarity
		double jaccard = ((double)vp) / (fp + vp + fn);
		// Dice coefficient
		double dice = ((double)(2 * vp)) / (fp + vp + vp + fn);
		out.append("\n");
		if (display)
			System.out.println(out);

		// Enregistrement des résultats
		output.put(string ? "VP" : VP, string ? Integer.toString(vp) : vp);
		output.put(string ? "VN" : VN, string ? Integer.toString(vn) : vn);
		output.put(string ? "FP" : FP, string ? Integer.toString(fp) : fp);
		output.put(string ? "FN" : FN, string ? Integer.toString(fn) : fn);
		output.put(string ? "SPECIFICITY" : SPECIFICITY, string ? Double
				.toString(specificity) : specificity);
		output.put(string ? "OVERALL ACCURACY" : PRECISION, string ? Double
				.toString(precision) : precision);
		output.put(string ? "PROD" : PROD, string ? Double.toString(prod)
				: prod);
		output.put(string ? "UTIL/PRECISION" : UTIL, string ? Double.toString(util)
				: util);
		output.put(string ? "SENSITIVITY/RECALL" : SENSITIVITY, string ? Double
				.toString(sensitivity) : sensitivity);
		output.put(string ? "KAPPA" : KAPPA, string ? Double.toString(kappa)
				: kappa);
		output.put(string ? "JACCARD" : JACCARD, string ? Double
				.toString(jaccard) : jaccard);
		output.put(string ? "DICE" : DICE, string ? Double.toString(dice)
				: dice);

	}

	public static void main(String args[]) {

		String path = "/home/lefevre/data/unversioned";
		String f1 = path + "/pi1.png";
		String f2 = path + "/pi2.png";

		if (args.length == 2) {
			f1 = args[0];
			f2 = args[1];
		}

		Image pi1 = AverageChannels.exec(ImageLoader.exec(f1));
		Image pi2 = AverageChannels.exec(ImageLoader.exec(f2));

		Viewer2D.exec(pi1, f1);
		Viewer2D.exec(pi2, f2);

		System.out.println("size 1:" + ((ByteImage) pi1).volume());
		System.out.println("size 2:" + ((ByteImage) pi2).volume());

		Properties prop = ConfusionMatrix.exec(pi1, pi2);
		System.out.println("VP=" + prop.get(VP));
		System.out.println("VN=" + prop.get(VN));
		System.out.println("FP=" + prop.get(FP));
		System.out.println("FN=" + prop.get(FN));
		System.out.println("PROD=" + prop.get(PROD));
		System.out.println("UTIL=" + prop.get(UTIL));
		System.out.println("PRECISION=" + prop.get(PRECISION));
		System.out.println("KAPPA=" + prop.get(KAPPA));

	}

}