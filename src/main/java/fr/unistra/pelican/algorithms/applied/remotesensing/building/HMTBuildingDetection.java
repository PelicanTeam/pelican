package fr.unistra.pelican.algorithms.applied.remotesensing.building;

import java.awt.Point;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.conversion.GrayToPseudoColors;
import fr.unistra.pelican.algorithms.io.ImageSave;
import fr.unistra.pelican.algorithms.logical.OR;
import fr.unistra.pelican.algorithms.morphology.binary.Binary2DGranulometry;
import fr.unistra.pelican.algorithms.morphology.binary.BinaryClosing;
import fr.unistra.pelican.algorithms.morphology.binary.BinaryOpening;
import fr.unistra.pelican.algorithms.morphology.binary.BinaryRectangularVariableHMT;
import fr.unistra.pelican.algorithms.morphology.binary.geodesic.FastBinaryReconstruction;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 * Class that performs building detection on a set of binary images
 * 
 * S. Lefèvre, J. Weber, D. Sheeren, Automatic building extraction in VHR images
 * using advanced morphological operators, IEEE/ISPRS Joint Workshop on Remote
 * Sensing and Data Fusion over Urban Areas (URBAN), Paris, April 2007,
 * doi:10.1109/URS.2007.371825
 * 
 * @author Sébastien Lefèvre, Jonathan Weber
 */
public class HMTBuildingDetection extends Algorithm {

	/*
	 * Inputs
	 */

	/**
	 * Input image to be processed (a N-band boolean images)
	 */
	public Image inputImage;

	/**
	 * Minimum height of the buildings
	 */
	public int Xmin;

	/**
	 * Minimum width of the buildings
	 */
	public int Ymin;

	/**
	 * Maximum height of the buildings
	 */
	public int Xmax;

	/**
	 * Maximum width of the buildings
	 */
	public int Ymax;

	/*
	 * Outputs
	 */

	/**
	 * Resulting image
	 */
	public Image outputImage;

	/*
	 * Options
	 */

	/**
	 * Flag to view the results
	 */
	public boolean view = true;

	/**
	 * Flag to save the results
	 */
	public boolean save = false;

	/**
	 * Flag to get the intermediate results
	 */
	public boolean debug = true;

	/**
	 * Filtering type : NO_FILTER, FILTER_MANUAL, FILTER_GRANULOMETRY,
	 * FILTER_SAFEGRANULOMETRY, SMALL_FILTER
	 */
	public int filterMode = SMALL_FILTER;

	/**
	 * Initial rotation of the SE
	 */
	public double angleFirst = 0;

	/**
	 * Step for rotations of the SE
	 */
	public double angleStep = 10;

	/**
	 * Flag to determine if a global TTR should be finally applied
	 */
	public boolean globalTTR = true;

	/**
	 * The minimum percentage of pixels kept by the opening operation, otherwise
	 * the result is set to null for the given binary image
	 */
	public double minRatio = 0;

	/**
	 * A generic String used in image caption (display) and filename (save)
	 */
	public String experiment = "building-";

	/**
	 * The step size for granulometry and HMT
	 */
	public int step = 2;

	/**
	 * The alpha parameter for HMT
	 */
	public double alpha = 0.6;

	/*
	 * Constants
	 */

	/**
	 * Constant for no filtering
	 */
	public static final int NO_FILTER = -1;

	/**
	 * Constant for the filtering mode in manual
	 */
	public static final int FILTER_MANUAL = 0;

	/**
	 * Constant for the filtering mode using granulometry
	 */
	public static final int FILTER_GRANULOMETRY = 1;

	/**
	 * Constant for the filtering mode using granulometry (or manual behaviour)
	 */
	public static final int FILTER_SAFEGRANULOMETRY = 2;

	/**
	 * Constant for the binarisation mode with a small filter
	 */
	public static final int SMALL_FILTER = 3;

	/*
	 * Private members
	 */
	private Point minSE;

	private BooleanImage orientationImage;

	/**
	 * Constructor
	 */
	public HMTBuildingDetection() {
		super.inputs = "inputImage,Xmin,Ymin,Xmax,Ymax";
		super.options = "view,save,debug,filterMode,angleFirst,angleStep,"
			+ "globalTTR,minRatio,experiment,step,alpha";
		super.outputs = "outputImage";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		// Initialisation
		outputImage = new BooleanImage(inputImage.getXDim(), inputImage.getYDim(),
			inputImage.getZDim(), inputImage.getTDim(), 1);
		orientationImage = new BooleanImage(inputImage.getXDim(), inputImage
			.getYDim(), inputImage.getZDim(), inputImage.getTDim(), 1);
		BooleanImage img = null;
		Image open = null;
		Image ttr = null;
		minSE = new Point(Xmin, Ymin);
		double angle = angleFirst;
		int clusters = inputImage.getBDim();
		for (int a = 0; a < angleStep; a++) {
			orientationImage.fill(false);
			System.out.println("*** Angle = " + angle + " ***");
			// Traitement de chaque image binaire
			for (int t = 0; t < clusters; t++) {
				// GÃ©nÃ©ration de l'image de travail
				System.out
					.println("Processing cluster # " + (t + 1) + " / " + clusters);
				img = (BooleanImage) inputImage.getImage4D(t, Image.B);
				// Filtrage
				open = null;
				switch (filterMode) {
				// FIXME : check if X Y or X Y
				case NO_FILTER:
					open = img.copyImage(true);
					break;
				case SMALL_FILTER:
					open = BinaryClosing.exec(img, FlatStructuringElement2D.rotate(
						FlatStructuringElement2D.createCrossFlatStructuringElement(1),
						angle));
					open = BinaryOpening.exec(open, FlatStructuringElement2D.rotate(
						FlatStructuringElement2D.createSquareFlatStructuringElement(3),
						angle));
					break;
				case FILTER_MANUAL:
					// open = BinaryClosing.exec(img, FlatStructuringElement2D.rotate(
					// FlatStructuringElement2D.createSquareFlatStructuringElement(3),
					// angle));
					open = BinaryClosing.exec(img, FlatStructuringElement2D.rotate(
						FlatStructuringElement2D.createCrossFlatStructuringElement(1),
						angle));
					open = BinaryOpening.exec(open, FlatStructuringElement2D.rotate(
						FlatStructuringElement2D.createRectangularFlatStructuringElement(
							Xmin, Ymin), angle), 2);
					System.out.println("Filter Manual with " + Xmin + "x" + Ymin);
					if (clusters > 2
						&& (((BooleanImage) open).getSum() < (((BooleanImage) img).getSum() * minRatio))) {
						open.fill(0);
						System.out.println("Filter Manual leads to empty image");
					}
					break;
				case FILTER_GRANULOMETRY:
				case FILTER_SAFEGRANULOMETRY:
open = BinaryClosing.exec(img, FlatStructuringElement2D.rotate(
FlatStructuringElement2D.createCrossFlatStructuringElement(1),
angle));
open = BinaryOpening.exec(open, FlatStructuringElement2D.rotate(
FlatStructuringElement2D.createSquareFlatStructuringElement(3),
angle));
Double[][] gran = (Double[][]) Binary2DGranulometry.exec((BooleanImage) open, Xmax,
Ymax, step, angle);
//					Double[][] gran = (Double[][]) Binary2DGranulometry.exec(img, Xmax,
//						Ymax, step, angle);
					Point maxFromGranulometry = getMaxPos(derivative(gran));
					// Filter the image with the max SE or empty it if the value is
					// less than lower bounds
					if ((maxFromGranulometry.x > Xmin) && (maxFromGranulometry.y > Ymin)) {
						open = BinaryClosing.exec(img, FlatStructuringElement2D.rotate(
							FlatStructuringElement2D.createSquareFlatStructuringElement(3),
							angle));
						open = BinaryOpening.exec(open, FlatStructuringElement2D.rotate(
							FlatStructuringElement2D.createRectangularFlatStructuringElement(
								maxFromGranulometry.x - 2, maxFromGranulometry.y - 2), angle));
						System.out
							.println("Filter Granulometry with "
								+ (maxFromGranulometry.x - 2) + "x"
								+ (maxFromGranulometry.y - 2));
						minSE = maxFromGranulometry;
						Viewer2D.exec(display(gran),"angle="+a);
						Viewer2D.exec(display(derivative(gran)),"derivative="+a);
//						for (int xx = 0; xx < gran.length; xx++)
//							for (int yy = 0; yy < gran.length; yy++)
//								System.out.println(xx + ":" + yy + " = " + gran[xx][yy]);
					} else {
						if (filterMode == FILTER_GRANULOMETRY) {
							System.out.println("Filter Granulometry leads to empty image ("
								+ maxFromGranulometry.x + "x" + maxFromGranulometry.y
								+ " instead of " + Xmin + "x" + Ymin + ")");
							open = img.copyImage(false);
						} else if (filterMode == FILTER_SAFEGRANULOMETRY) {
							System.out.println("Filter Granulometry leads to empty image ("
								+ maxFromGranulometry.x + "x" + maxFromGranulometry.y + ")");
							open = BinaryClosing.exec(img, FlatStructuringElement2D.rotate(
								FlatStructuringElement2D.createSquareFlatStructuringElement(3),
								angle));
							open = BinaryOpening.exec(img, FlatStructuringElement2D.rotate(
								FlatStructuringElement2D
									.createRectangularFlatStructuringElement(Xmin, Ymin), angle),
								2);
							System.out.println("force Filter Manual with " + Xmin + "x"
								+ Ymin);
							// FlatStructuringElement2D.print(FlatStructuringElement2D.rotate(
							// FlatStructuringElement2D
							// .createRectangularFlatStructuringElement(Xmin, Ymin), angle));
							// FlatStructuringElement2D
							// .print(FlatStructuringElement2D
							// .reflect(FlatStructuringElement2D.rotate(
							// FlatStructuringElement2D
							// .createRectangularFlatStructuringElement(Xmin, Ymin),
							// angle)));
							// System.out.println(FlatStructuringElement2D.rotate(
							// FlatStructuringElement2D
							// .createRectangularFlatStructuringElement(Xmin, Ymin),
							// angle).getCenter());
							// System.out.println(FlatStructuringElement2D.reflect(FlatStructuringElement2D.rotate(
							// FlatStructuringElement2D
							// .createRectangularFlatStructuringElement(Xmin, Ymin),
							// angle)).getCenter());
						}
					}
					break;
				}
				if (view && debug)
					Viewer2D.exec(open, "lissage : " + experiment + angle + "-" + t);
				if (save)
					ImageSave.exec(open, experiment + angle + "-" + t + "-smooth.png");
				// Si l'image filtrÃ©e est vide, on ne la traite pas
				if (((BooleanImage) open).isEmpty())
					continue;

				// TTR Adaptative et reconstruction
				System.out.println("HMT with angle " + angle + " / (" + minSE.x + ","
					+ minSE.y + ") to (" + Xmax + "," + Ymax + ")");
				ttr = BinaryRectangularVariableHMT.exec(open, minSE.x, Xmax, minSE.y,
					Ymax, step, alpha, angle);

				// marker=removeBorders(marker); // Suppression des bords ?
				System.out.println("Reconstruction");
				ttr = FastBinaryReconstruction.exec(ttr, open,
					FastBinaryReconstruction.CONNEXITY8);
				if (view && debug)
					Viewer2D.exec(ttr, "ttr : " + experiment + angle + "-" + t);
				if (save)
					ImageSave.exec(ttr, experiment + angle + "-" + t + "-ttr.png");
				// Filtrage
				/*
				 * if (tab.length>2 && lower(ttr,open,minRatio)) { ttr.fill(0); if
				 * (debug) System.out.println(t+" mis a 0 avec minRatio="+minRatio); }
				 */
				// Ajout du rÃ©sultat cluster au rÃ©sultat global
				orientationImage = (BooleanImage) OR.exec(orientationImage, ttr);
			}
			if (clusters > 1 && globalTTR) {
				// Traitement optionnel : on reapplique la TTR sur la fusion des
				// clusters
				Image markerFinal = BinaryRectangularVariableHMT.exec(orientationImage,
					Xmin, Xmax, Ymin, Ymax, step, alpha, angle);
				orientationImage = (BooleanImage) FastBinaryReconstruction.exec(
					markerFinal, orientationImage, FastBinaryReconstruction.CONNEXITY8);
				// outputImage = (BooleanImage)removeBorders(outputImage);
			}

			if (view && debug)
				Viewer2D.exec(orientationImage, "TTR : " + experiment + angle);
			if (save)
				ImageSave.exec(orientationImage, experiment + angle + "-ttr.png");

			// On cumule les rÃ©sultats dans toutes les directions.
			outputImage = OR.exec(outputImage, orientationImage);
			// IncrÃ©ment de l'angle Ã  la fin pour bien dÃ©buter Ã  angleFirst
			angle += 180 / angleStep;
		}

		if (view && debug)
			Viewer2D.exec(outputImage, "globalresult : " + experiment);
		if (save)
			ImageSave.exec(outputImage, experiment + "globalresult.png");

	}

	private Image display(Double[][]tab) {
		DoubleImage image=new DoubleImage(tab.length,tab[0].length,1,1,1);
		for (int x=0;x<image.getXDim();x++)for (int y=0;y<image.getYDim();y++)
			image.setPixelXYDouble(x,y,tab[x][y]);
		return GrayToPseudoColors.exec(image);
	}
	
	private Double[][] derivative(Double[][] tab) {
		Double[][] deriv = new Double[Xmax / step][Ymax / step];
		int i = 0;
		// Special case with i=0
		deriv[0][0] = 0.;
		for (int j = 1; j < Ymax / step; j++)
			deriv[0][j] = tab[0][j - 1] - tab[0][j];
		for (i = 1; i < Xmax / step; i++) {
			deriv[i][0] = tab[i - 1][0] - tab[i][0];
			for (int j = 1; j < Ymax / step; j++)
				deriv[i][j] = tab[i - 1][j - 1] - tab[i][j];
		}
		return deriv;
	}

	private Point getMaxPos(Double[][] tab) {
		// Search for the maximum value of the difference
		double max = 0.;
		Point pos = new Point();
		for (int i = 0; i < Xmax / step; i++)
			for (int j = 0; j < Ymax / step; j++)
				if (tab[i][j] >= max) {
					max = tab[i][j];
					pos.setLocation(i * step + 1, j * step + 1);
				}
		return pos;
	}

	/**
	 * Static method that performs building detection on grey-level satellite
	 * picture
	 * 
	 * @param inputImage
	 *          Satellite picture
	 * @param XMin
	 *          Minimum length in X of the building
	 * @param YMin
	 *          Minimum length in Y of the building
	 * @param XMax
	 *          Maximum length in X of the building
	 * @param YMax
	 *          Maximum length in Y of the building
	 * @return image of the detected building
	 */
	public static Image exec(Image inputImage, int XMin, int YMin, int XMax,
		int YMax) {
		return (Image) new HMTBuildingDetection().process(inputImage, XMin, YMin,
			XMax, YMax);
	}

	/**
	 * Static method that performs building detection on grey-level satellite
	 * picture with the full set of options
	 * 
	 * @param inputImage
	 *          Input image to be processed
	 * @param XMin
	 *          Minimum height of the buildings
	 * @param YMin
	 *          Minimum width of the buildings
	 * @param XMax
	 *          Maximum height of the buildings
	 * @param YMax
	 *          Maximum width of the buildings
	 * @param view
	 *          Flag to view the results
	 * @param save
	 *          Flag to save the results
	 * @param debug
	 *          Flag to get the intermediate results
	 * @param filterMode
	 *          Filtering type : FILTER_MANUAL, FILTER_GRANULOMETRY
	 * @param angleFirst
	 *          Initial rotation of the SE
	 * @param angleStep
	 *          Step for rotations of the SE
	 * @param globalTTR
	 *          Flag to determine if a global TTR should be finally applied
	 * @param minRatio
	 *          The minimum percentage of pixels kept by the opening operation,
	 *          otherwise the result is set to null for the given binary image
	 * @param experiment
	 *          A generic String used in image caption (display) and filename
	 *          (save)
	 * @return Resulting image
	 */
	public static Image exec(Image inputImage, int XMin, int YMin, int XMax,
		int YMax, boolean view, boolean save, boolean debug, int filterMode,
		double angleFirst, double angleStep, boolean globalTTR, double minRatio,
		String experiment) {
		return (Image) new HMTBuildingDetection().process(inputImage, XMin, YMin,
			XMax, YMax, view, save, debug, filterMode, angleFirst, angleStep,
			globalTTR, minRatio, experiment);
	}

	public static Image exec(Image inputImage, int XMin, int YMin, int XMax,
		int YMax, boolean view, boolean save, boolean debug, int filterMode,
		double angleFirst, double angleStep, boolean globalTTR, double minRatio,
		String experiment, int step, double alpha) {
		return (Image) new HMTBuildingDetection().process(inputImage, XMin, YMin,
			XMax, YMax, view, save, debug, filterMode, angleFirst, angleStep,
			globalTTR, minRatio, experiment, step, alpha);
	}

	/*
	 * public static Image exec(Image img, int binMod, int binPara, double binTh,
	 * int filterMod, double angl, int XMin, int YMin, int XMax, int YMax) {
	 * return (Image) new BinaryBuildingDetection().process(img, binMod, binPara,
	 * binTh, filterMod, angl, XMin, YMin, XMax, YMax); }
	 */

}
