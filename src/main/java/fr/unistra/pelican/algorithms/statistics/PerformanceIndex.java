package fr.unistra.pelican.algorithms.statistics;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.arithmetic.Inversion;
import fr.unistra.pelican.algorithms.conversion.AverageChannels;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.logical.AND;
import fr.unistra.pelican.algorithms.logical.BinaryDifference;
import fr.unistra.pelican.algorithms.logical.OR;
import fr.unistra.pelican.algorithms.morphology.binary.BinaryDilation;
import fr.unistra.pelican.algorithms.morphology.binary.geodesic.FastBinaryReconstruction;
import fr.unistra.pelican.algorithms.morphology.binary.hitormiss.BinaryHST;
import fr.unistra.pelican.algorithms.segmentation.flatzones.BooleanConnectedComponentsLabeling;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToBinaryMasks;
import fr.unistra.pelican.algorithms.segmentation.labels.RegionSize;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 * This class computes some kind of Performance Index between two segmentations
 * Assumes the main object crosses completely the image without hole
 * 
 * @author Lefevre
 */

public class PerformanceIndex extends Algorithm {

	/**
	 * Minimum difference
	 */
	public static final int MIN = 0;

	/**
	 * Maximum difference
	 */
	public static final int MAX = 1;

	/**
	 * Frechet distance
	 */
	public static final int FRECHET = 2;

	/**
	 * Difference based on skeletons
	 */
	public static final int SKEL = 3;

	/**
	 * specific mode to compute false positives in terms of components
	 */
	public static final int FPC = 4;

	/**
	 * specific mode to compute false positives in terms of pixels
	 */
	public static final int FPP = 5;

	/**
	 * pixel-based distance
	 */
	public static final int DIST = 6;

	/**
	 * specific mode to compute false negatives in terms of components
	 */
	public static final int FNC = 7;

	/**
	 * specific mode to compute false negatives in terms of pixels
	 */
	public static final int FNP = 8;

	/**
	 * Reference image
	 */
	public Image reference;

	/**
	 * Evaluated image
	 */
	public Image result;

	/**
	 * The mode to use
	 */
	public int mode;

	/**
	 * The output value
	 */
	public double output;

	/**
	 * A flag to ensure normalization
	 */
	public boolean normalisation = false;

	private Image work = null;
	private Image reference2 = null;
	private Image result2 = null;

	/**
	 * This method computes some kind of Performance Index between two
	 * segmentations Assumes the main object crosses completely the image without
	 * hole
	 * 
	 * @param reference
	 *          Reference image
	 * @param result
	 *          Evaluated image
	 * @param mode
	 *          The mode to use
	 * @return some kind of Performance Index between two segmentations Assumes
	 *         the main object crosses completely the image without hole
	 */
	public static Double exec(Image reference, Image result, Integer mode) {
		return (Double) new PerformanceIndex().process(reference, result, mode);
	}

	public static Double exec(Image reference, Image result, Integer mode,
		Boolean normalisation) {
		return (Double) new PerformanceIndex().process(reference, result, mode,
			normalisation);
	}

	/**
	 * Constructor
	 * 
	 */
	public PerformanceIndex() {
		super.inputs = "reference,result,mode";
		super.outputs = "output";
		super.options = "normalisation";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		// Binarisation
		result = new BooleanImage(result, true);
		reference = new BooleanImage(reference, true);
		if (((BooleanImage) result).isEmpty()) {
			output = -1;
			return;
		}
		int ratio = count(reference);

		switch (mode) {
		case FRECHET:
			td(true);
			break;
		case DIST:
			ratio = td(false);
			break;
		case SKEL: // Ecart entre les traits squelettisï¿œs
			reference = BinaryHST.exec(reference);
			result = BinaryHST.exec(result);
			distance();
			break;
		case MIN: // Ecart entre les traits
			distance();
			break;
		case MAX: // MAX = MIN + resultat - intersection
			distance();
			output += count(result2);
			output -= count(reference2);
			break;
//		case AVG: // AVG = (MIN + MAX) / 2
//			distance();
//			output += (count(result2) - count(reference2)) / 2;
//			break;
		case FPP: // Nombre de faux positifs (pixels)
			setup();
			output = count(fp());
			break;
		case FPC: // Nombre de faux positifs (composantes)
			setup();
			output = fp().getBDim();
			break;
		case FNP: // Nombre de faux nï¿œgatifs (pixels)
			setup();
			output = count(fn());
			break;
		case FNC: // Nombre de faux nï¿œgatifs (composantes)
			setup();
			// Calcul standard
			// output = (Integer) new
			// ConnectedComponentsLabeling().processOne(1,
			// fn(),ConnectedComponentsLabeling.CONNEXITY8,false);
			// Calcul rapide
			output = fnc();
			break;
		}
		if (normalisation && mode != FPP && mode != FPC && mode != FNP
			&& mode != FNC && mode !=FRECHET)
			output /= ratio;
	}

	private int td(boolean frechet) {
		ArrayList<Point> list1, list2;
		int xdim, ydim;
		double sum1, sum2, val, min,max;
		// Initialisation image 1
		list1 = new ArrayList<Point>();
		xdim = reference.getXDim();
		ydim = reference.getYDim();
		for (int y = 0; y < ydim; y++)
			for (int x = 0; x < xdim; x++)
				if (reference.getPixelXYBoolean(x, y))
					list1.add(new Point(x, y));
		// Initialisation image 2
		list2 = new ArrayList<Point>();
		xdim = result.getXDim();
		ydim = result.getYDim();
		for (int y = 0; y < ydim; y++)
			for (int x = 0; x < xdim; x++)
				if (result.getPixelXYBoolean(x, y))
					list2.add(new Point(x, y));
		// Calcul distances image 1
		max=0;
		sum1 = 0;
		for (Point p1 : list1) {
			min = Double.MAX_VALUE;
			for (Point p2 : list2) {
				val = p1.distance(p2);
				if (val < min)
					min = val;
			}
			sum1 += min;
			if(min>max)
				max=min;
		}
		sum2 = 0;
		for (Point p2 : list2) {
			min = Double.MAX_VALUE;
			for (Point p1 : list1) {
				val = p1.distance(p2);
				if (val < min)
					min = val;
			}
			sum2 += min;
			if(min>max)
				max=min;
		}
		if(!frechet)
		output = sum1 + sum2;
		else
			output=max;
		return (list1.size() + list2.size());
		// TODO: autres possibilitï¿œs ? sum1/size1 ou sum2/size2 ou les 2 ?
	}

	private Image fn() {
		Image missed = reference.copyImage(false);
		int cc = 0;
		// CC de result2
		Image labels = LabelsToBinaryMasks.exec(BooleanConnectedComponentsLabeling.exec(
			result2, BooleanConnectedComponentsLabeling.CONNEXITY8, false));
		Image[] tab = new Image[labels.getBDim()];
		for (int l = 0; l < labels.getBDim(); l++)
			tab[l] = labels.getImage4D(l, Image.B);
		// CC de reference non trouvï¿œs
		Image holes = LabelsToBinaryMasks.exec(BooleanConnectedComponentsLabeling.exec(
			BinaryDifference.exec(reference, reference2),
			BooleanConnectedComponentsLabeling.CONNEXITY8, false));

		boolean ok, bord;
		for (int h = 0; h < holes.getBDim(); h++) {
			Image work = holes.getImage4D(h, Image.B);
			ok = false;
			bord = false;
			// Cas particulier si le faux nï¿œgatif est sur le bord
			if (bords(work) > 0)
				bord = true;
			Image mask = OR.exec(work, result2);
			mask = FastBinaryReconstruction.exec(work, mask);
			mask = AND.exec(mask, result2);
			// On teste le faux nï¿œgatif avec l'ensemble des CC
			for (int l = 0; l < tab.length && !ok; l++) {
				if (subset(mask, tab[l]))
					// Si le faux nï¿œgatif est sur le bord, le CC doit l'ï¿œtre
					// aussi
					if (!bord || bords(tab[l]) > 0)
						ok = true;
			}
			// On conserve le faux nï¿œgatif s'il n'ï¿œtait pas dï¿œlimitï¿œ par l'un
			// des CC
			if (!ok) {
				missed = OR.exec(missed, work);
				cc++;
			}
		}
		return missed;
	}

	private Image fp() {
		return LabelsToBinaryMasks.exec(BooleanConnectedComponentsLabeling.exec(
			BinaryDifference.exec(result, result2),
			BooleanConnectedComponentsLabeling.CONNEXITY8, false));
	}

	private int fnc() {
		return (Integer) new BooleanConnectedComponentsLabeling().processOne(1, result2,
			BooleanConnectedComponentsLabeling.CONNEXITY8, false)
			- bords(result2);
	}

	private void setup() {
		reference2 = AND.exec(reference, result);
		result2 = FastBinaryReconstruction.exec(reference2, result);
	}

	private void distance() {
		setup();
		// Cas particulier lorsque les traits sont disjoints
		if (((BooleanImage) reference2).isEmpty()) {
			System.err.println("Disjoints sets");
			Image labels = LabelsToBinaryMasks.exec(BooleanConnectedComponentsLabeling.exec(
				Inversion.exec(OR.exec(reference, result)),
				BooleanConnectedComponentsLabeling.CONNEXITY4, false));
			reference = BinaryDilation.exec(reference, FlatStructuringElement2D
				.createSquareFlatStructuringElement(3));
			result2 = BinaryDilation.exec(result, FlatStructuringElement2D
				.createSquareFlatStructuringElement(3));
			work = result.copyImage(false);
			Image work2 = result.copyImage(false);
			for (int l = 0; l < labels.getBDim(); l++) {
				Image tmp = labels.getImage4D(l, Image.B);
				Image tmp2 = AND.exec(FastBinaryReconstruction.exec(reference, tmp),
					result2);
				if (!((BooleanImage) tmp2).isEmpty()) {
					work = OR.exec(work, tmp);
					work2 = OR.exec(work2, AND.exec(result, FastBinaryReconstruction
						.exec(tmp2, result2)));
				}
			}
			result2 = work2;
			output = count(work);
			// Viewer2D.exec(work);
			// Viewer2D.exec(result2);
			// Viewer2D.exec(reference2);
		} else {
			Image labels = BooleanConnectedComponentsLabeling.exec(Inversion.exec(OR.exec(
				reference, result2)), BooleanConnectedComponentsLabeling.CONNEXITY4, false);
			// Viewer2D.exec(Inversion.exec(OR.exec(reference, result2)));
			// Viewer2D.exec(result2);
			// Viewer2D.exec(reference2);
			int tab[] = RegionSize.exec(labels);
			tab[0] = 0;
			Arrays.sort(tab);
			for (int t = 0; t < tab.length - 2; t++)
				output += tab[t];
		}
	}

	private int count(Image im) {
		int s = 0;
		for (int p = 0; p < im.size(); p++)
			if (im.getPixelBoolean(p))
				s++;
		return s;
	}

	private static int bords(Image im) {
		int xdim = im.getXDim();
		int ydim = im.getYDim();
		int bords = 0;
		for (int x = 0; x < xdim; x++) {
			if (im.getPixelXYBoolean(x, 0))
				bords++;
			if (im.getPixelXYBoolean(x, ydim - 1))
				bords++;
		}
		for (int y = 0; y < ydim; y++) {
			if (im.getPixelXYBoolean(0, y))
				bords++;
			if (im.getPixelXYBoolean(xdim - 1, y))
				bords++;
		}
		return bords;
	}

	private static boolean subset(Image i1, Image i2) {
		if (i1.size() != i2.size())
			return false;
		for (int p = 0; p < i1.size(); p++)
			if (i1.getPixelBoolean(p) && !i2.getPixelBoolean(p))
				return false;
		return true;
	}

	public static void main(String args[]) {

		String path = "/home/lefevre/data/unversioned";
		String f1 = path + "/pi1.png";
		String f2 = path + "/pi9.png";

		if (args.length == 2) {
			f1 = args[0];
			f2 = args[1];
		}

		Image pi1 = AverageChannels.exec(ImageLoader.exec(f1));
		Image pi2 = AverageChannels.exec(ImageLoader.exec(f2));

		 Viewer2D.exec(pi1, "pi1");
		 Viewer2D.exec(pi2, "pi2");

		System.out.println("size 1:" + ((ByteImage) pi1).volume());
		System.out.println("size 2:" + ((ByteImage) pi2).volume());

		System.out.println("PI MIN ="
			+ PerformanceIndex.exec(pi1, pi2, PerformanceIndex.MIN, false) + " / "
			+ PerformanceIndex.exec(pi1, pi2, PerformanceIndex.MIN, true));
		System.out.println("PI DIST ="
			+ PerformanceIndex.exec(pi1, pi2, PerformanceIndex.DIST, false) + " / "
			+ PerformanceIndex.exec(pi1, pi2, PerformanceIndex.DIST, true));
		System.out.println("PI MAX ="
			+ PerformanceIndex.exec(pi1, pi2, PerformanceIndex.MAX, false) + " / "
			+ PerformanceIndex.exec(pi1, pi2, PerformanceIndex.MAX, true));
//		System.out.println("PI SKEL ="
//			+ PerformanceIndex.exec(pi1, pi2, PerformanceIndex.SKEL, false) + " / "
//			+ PerformanceIndex.exec(pi1, pi2, PerformanceIndex.SKEL, true));
		System.out.println("PI FRECHET ="
				+ PerformanceIndex.exec(pi1, pi2, PerformanceIndex.FRECHET));
		System.out.println("PI FPC ="
			+ PerformanceIndex.exec(pi1, pi2, PerformanceIndex.FPC));
		System.out.println("PI FPP ="
			+ PerformanceIndex.exec(pi1, pi2, PerformanceIndex.FPP));
		System.out.println("PI FNC ="
			+ PerformanceIndex.exec(pi1, pi2, PerformanceIndex.FNC));
		System.out.println("PI FNP ="
			+ PerformanceIndex.exec(pi1, pi2, PerformanceIndex.FNP));
	}

}