package fr.unistra.pelican.algorithms.applied.remotesensing.building;

import java.awt.Point;
import java.util.ArrayList;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.arithmetic.Inversion;
import fr.unistra.pelican.algorithms.io.ImageSave;
import fr.unistra.pelican.algorithms.logical.OR;
import fr.unistra.pelican.algorithms.morphology.binary.Binary2DGranulometry;
import fr.unistra.pelican.algorithms.morphology.binary.BinaryClosing;
import fr.unistra.pelican.algorithms.morphology.binary.BinaryOpening;
import fr.unistra.pelican.algorithms.morphology.binary.BinaryRectangularVariableHMT;
import fr.unistra.pelican.algorithms.morphology.binary.geodesic.FastBinaryReconstruction;
import fr.unistra.pelican.algorithms.morphology.gray.GrayOCCO;
import fr.unistra.pelican.algorithms.segmentation.HistogramBasedClustering;
import fr.unistra.pelican.algorithms.segmentation.ManualThresholding;
import fr.unistra.pelican.algorithms.segmentation.RidlerThresholding;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToRandomColors;
import fr.unistra.pelican.algorithms.segmentation.weka.WekaSegmentationKmeans;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 * Class that performs building detection on graylevel remotely sensed image
 * 
 * S. Lefï¿œvre, J. Weber, D. Sheeren, Automatic building extraction in VHR images
 * using advanced morphological operators, IEEE/ISPRS Joint Workshop on Remote
 * Sensing and Data Fusion over Urban Areas (URBAN), Paris, April 2007,
 * doi:10.1109/URS.2007.371825
 * 
 * @author Sï¿œbastien Lefevre, Jonathan Weber
 */
public class OriginalBinaryBuildingDetection extends Algorithm {

	/*
	 * Inputs
	 */

	/**
	 * Input image to be processed
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
	 * Flag to ignore black pixels (useful for clustering in case of rotated
	 * images)
	 */
	public boolean ignoreBlackPixels = false;

	/**
	 * Binarisation threshold
	 */
	public double binThr = 0.8; // 0.8

	/**
	 * Binarisation type : BINARISATION_MANUAL, BINARISATION_AUTO,
	 * BINARISATION_CLUSTER, BINARISATION_KMEANS
	 */
	public int binMode = BINARISATION_CLUSTER;

	/**
	 * Filtering type : FILTER_MANUAL, FILTER_GRANULOMETRY,
	 * FILTER_SAFEGRANULOMETRY
	 */
	public int filterMode = FILTER_SAFEGRANULOMETRY;

	/**
	 * Initial rotation of the SE
	 */
	public double angleFirst = 0;

	/**
	 * Step for rotations of the SE
	 */
	public double angleStep = 10;

	/**
	 * Number of clusters, special case if equals to 0 (original image) or 1
	 * (inverted image)
	 */
	public int nbClusters = 3;

	/**
	 * The size step of the SE in the granulometry
	 */
	public int granStep = 2;

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
	 * The size of the OCCO filter
	 */
	public int occo = 5;

	/**
	 * A generic String used in image caption (display) and filename (save)
	 */
	public String experiment = "building-";

	/*
	 * Constants
	 */

	/**
	 * Constant for the binarisation mode in manual
	 */
	public static final int BINARISATION_MANUAL = 0;

	/**
	 * Constant for the binarisation mode in automatic mode
	 */
	public static final int BINARISATION_AUTO = 1;

	/**
	 * Constant for the binarisation mode in clustering mode
	 */
	public static final int BINARISATION_CLUSTER = 2;

	/**
	 * Constant for the binarisation mode in k-menas mode
	 */
	public static final int BINARISATION_KMEANS = 3;

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
	public OriginalBinaryBuildingDetection() {
		super.inputs = "inputImage,Xmin,Ymin,Xmax,Ymax";
		super.options = "view,save,debug,ignoreBlackPixels,"
			+ "binThr,binMode,filterMode,angleFirst,angleStep,"
			+ "nbClusters,granStep,globalTTR,minRatio,occo,experiment";
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
		BooleanImage[] tab = null;
		BooleanImage img = null;
		Image open = null;
		Image ttr = null;
		minSE = new Point(Xmin, Ymin);
		double angle = angleFirst;

		// Viewer2D.exec(GrayOCCO/* ByReconstruction */.exec(inputImage,
		// FlatStructuringElement2D.rotate(FlatStructuringElement2D
		// .createSquareFlatStructuringElement(occo), angle)));

		for (int a = 0; a < angleStep; a++) {

			System.out.println("*** Angle = " + angle + " ***");

			// Lissage avant binarisation
			Image lisse = null;
			if (occo >= 3) {
				lisse = GrayOCCO/* ByReconstruction */.exec(inputImage,
					FlatStructuringElement2D.rotate(FlatStructuringElement2D
						.createSquareFlatStructuringElement(occo), angle));
			} else {
				lisse = inputImage.copyImage(true);
			}
			if (view && debug)
				Viewer2D.exec(lisse, "lissage: " + experiment + angle + "-");
			if (save)
				ImageSave.exec(lisse, experiment + angle + "-" + "lisse.png");

			// Binarisation plus d'une fois seulement si lissage effectif
			if (a == 0 || occo >= 3)
				switch (binMode) {
				case BINARISATION_MANUAL:
					System.out.println("Binarisation Manual");
					tab = thresholding(ManualThresholding.exec(lisse, binThr));
					break;
				case BINARISATION_AUTO:
					System.out.println("Binarisation Auto");
					tab = thresholding(RidlerThresholding.exec(lisse, false));
					break;
				case BINARISATION_CLUSTER:
					System.out.println("Binarisation Cluster");
					ArrayList<Object> v = new HistogramBasedClustering().processAll(lisse,
						binThr, ignoreBlackPixels);
					tab = merge((Image) v.get(0), (Integer) v.get(1), nbClusters, angle,1);
					break;
				case BINARISATION_KMEANS:
					System.out.println("Binarisation KMeans");
					tab = merge((Image) new WekaSegmentationKmeans().process(lisse,
						(int) binThr), (int) binThr, nbClusters, angle,0);
					break;
				}
			else
				System.out.println("Binarisation already performed, use old one");

			// Traitement de chaque image binaire
			for (int t = 0; t < tab.length; t++) {
				// Gï¿œnï¿œration de l'image de travail
				System.out.println("Processing cluster # " + (t + 1) + " / "
					+ tab.length);
				img = tab[t];
				if (view && debug)
					Viewer2D.exec(img, "binarisation : " + experiment + angle + "-" + t);
				if (save)
					ImageSave.exec(img, experiment + angle + "-" + t + "-bin.png");

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
					open = BinaryOpening.exec(img, FlatStructuringElement2D.rotate(
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
					if (tab.length > 2
						&& (((BooleanImage) open).getSum() < (((BooleanImage) img).getSum() * minRatio))) {
						open.fill(0);
						System.out.println("Filter Manual leads to empty image");
					}
					break;
				case FILTER_GRANULOMETRY:
				case FILTER_SAFEGRANULOMETRY:
					Double[][] gran = (Double[][]) Binary2DGranulometry.exec(img, Xmax,
						Ymax, granStep, angle);
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

				// Si l'image filtrï¿œe est vide, on ne la traite pas
				if (((BooleanImage) open).isEmpty())
					continue;

				// TTR Adaptative et reconstruction
				System.out.println("HMT with angle " + angle + " /" + minSE.x + " "
					+ minSE.y);
				ttr = BinaryRectangularVariableHMT.exec(open,
					minSE.x, Xmax, minSE.y, Ymax, angle);
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

				// Ajout du rï¿œsultat cluster au rï¿œsultat global
				orientationImage = (BooleanImage) OR.exec(orientationImage, ttr);
			}

			if (tab.length > 1 && globalTTR) {
				// Traitement optionnel : on reapplique la TTR sur la fusion des
				// clusters
				Image markerFinal = (BooleanImage) new BinaryRectangularVariableHMT()
					.process(orientationImage, Xmin, Xmax, Ymin, Ymax, null, null, angle);
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

	/**
	 * 
	 * @param img
	 *          Label image of the different clusters
	 * @param nbClusters
	 *          Number of clusters
	 * @param mergingLevel
	 *          Level of the merging
	 * @return Array of merged clusters
	 */
	private BooleanImage[] merge(Image img, int nbClusters, int mergingLevel,
		double angle,int start) {
		// ((Image)new LabelsToBinaryMasks().process(img)).to4DArray(Image.B);
		Image display = LabelsToRandomColors.exec(img);
		if (view && debug)
			Viewer2D.exec(display, "clusters: " + experiment);
		if (save)
			ImageSave.exec(display, experiment + angle + "-" + "clusters.png");
		// Cluster image creation
		Image temp = null;
		ArrayList<Image> vect = new ArrayList<Image>();
		// Images with one cluster binarized
		if (mergingLevel >= 1)
			for (int c1 = 0; c1 < nbClusters; c1++) {
				temp = new BooleanImage(inputImage.getXDim(), inputImage.getYDim(),
					inputImage.getZDim(), inputImage.getTDim(), 1);
				System.out.println("cluster " + c1);
				for (int j = 0; j < img.size(); j++) {
					if (img.getPixelInt(j) == c1+start)
						temp.setPixelBoolean(j, true);
					else
						temp.setPixelBoolean(j, false);
				}
				vect.add(temp);

			}
		// Images with two clusters binarized
		if (mergingLevel >= 2)
			for (int c1 = 0; c1 < nbClusters; c1++)
				for (int c2 = c1 + 1; c2 < nbClusters; c2++) {
					temp = new BooleanImage(inputImage.getXDim(), inputImage.getYDim(),
						inputImage.getZDim(), inputImage.getTDim(), 1);
					System.out.println("cluster " + c1 + ":" + c2);
					for (int j = 0; j < img.size(); j++) {
						if (img.getPixelInt(j) == c1+start || img.getPixelInt(j) == c2+start)
							temp.setPixelBoolean(j, true);
						else
							temp.setPixelBoolean(j, false);
					}
					vect.add(temp);
				}
		// Images with three clusters binarized
		if (mergingLevel >= 3)
			for (int c1 = 0; c1 < nbClusters; c1++)
				for (int c2 = c1 + 1; c2 < nbClusters; c2++)
					for (int c3 = c2 + 1; c3 < nbClusters; c3++) {
						temp = new BooleanImage(inputImage.getXDim(), inputImage.getYDim(),
							inputImage.getZDim(), inputImage.getTDim(), 1);
						System.out.println("cluster " + c1 + ":" + c2 + ":" + c3);
						for (int j = 0; j < img.size(); j++) {
							if (img.getPixelInt(j) == c1+start || img.getPixelInt(j) == c2+start
								|| img.getPixelInt(j) == c3+start)
								temp.setPixelBoolean(j, true);
							else
								temp.setPixelBoolean(j, false);
						}
						vect.add(temp);
					}
		// Images with four clusters binarized
		if (mergingLevel >= 4)
			for (int c1 = 0; c1 < nbClusters; c1++)
				for (int c2 = c1 + 1; c2 < nbClusters; c2++)
					for (int c3 = c2 + 1; c3 < nbClusters; c3++)
						for (int c4 = c3 + 1; c4 < nbClusters; c4++) {
							temp = new BooleanImage(inputImage.getXDim(), inputImage
								.getYDim(), inputImage.getZDim(), inputImage.getTDim(), 1);
							System.out.println("cluster " + c1 + ":" + c2 + ":" + c3 + ":"
								+ c4);
							for (int j = 0; j < img.size(); j++) {
								if (img.getPixelInt(j) == c1+start || img.getPixelInt(j) == c2+start
									|| img.getPixelInt(j) == c3+start || img.getPixelInt(j) == c4+start)
									temp.setPixelBoolean(j, true);
								else
									temp.setPixelBoolean(j, false);
							}
							vect.add(temp);
						}

		// Images with five clusters binarized
		if (mergingLevel >= 5)
			for (int c1 = 0; c1 < nbClusters; c1++)
				for (int c2 = c1 + 1; c2 < nbClusters; c2++)
					for (int c3 = c2 + 1; c3 < nbClusters; c3++)
						for (int c4 = c3 + 1; c4 < nbClusters; c4++)
							for (int c5 = c4 + 1; c5 < nbClusters; c5++) {
								temp = new BooleanImage(inputImage.getXDim(), inputImage
									.getYDim(), inputImage.getZDim(), inputImage.getTDim(), 1);
								System.out.println("cluster " + c1 + ":" + c2 + ":" + c3 + ":"
									+ c4 + ":" + c5);
								for (int j = 0; j < img.size(); j++) {
									if (img.getPixelInt(j) == c1+start || img.getPixelInt(j) == c2+start
										|| img.getPixelInt(j) == c3+start || img.getPixelInt(j) == c4+start
										|| img.getPixelInt(j) == c5+start)
										temp.setPixelBoolean(j, true);
									else
										temp.setPixelBoolean(j, false);
								}
								vect.add(temp);
							}

		// Put the images clustered in the resulting tab
		System.out.println(vect.size() + " clusters generated");
		return vect.toArray(new BooleanImage[vect.size()]);
	}

	private BooleanImage[] thresholding(BooleanImage img) {
		BooleanImage[] tab = null;
		if (nbClusters == 0) {
			tab = new BooleanImage[1];
			tab[0] = (BooleanImage) Inversion.exec(img);
		} else if (nbClusters == 1) {
			tab = new BooleanImage[1];
			tab[0] = img;
		} else if (nbClusters == 2) {
			tab = new BooleanImage[2];
			tab[0] = img;
			tab[1] = (BooleanImage) Inversion.exec(img);
		}
		return tab;
	}

	private Double[][] derivative(Double[][] tab) {
		Double[][] deriv = new Double[Xmax / granStep][Ymax / granStep];
		int i = 0;
		// Special case with i=0
		deriv[0][0] = 0.;
		for (int j = 1; j < Ymax / granStep; j++)
			deriv[0][j] = tab[0][j - 1] - tab[0][j];
		for (i = 1; i < Xmax / granStep; i++) {
			deriv[i][0] = tab[i - 1][0] - tab[i][0];
			for (int j = 1; j < Ymax / granStep; j++)
				deriv[i][j] = tab[i - 1][j - 1] - tab[i][j];
		}
		return deriv;
	}

	private Point getMaxPos(Double[][] tab) {
		// Search for the maximum value of the difference
		double max = 0.;
		Point pos = new Point();
		for (int i = 0; i < Xmax / granStep; i++)
			for (int j = 0; j < Ymax / granStep; j++)
				if (tab[i][j] >= max) {
					max = tab[i][j];
					pos.setLocation(i * granStep + 1, j * granStep + 1);
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
		return (Image) new OriginalBinaryBuildingDetection().process(inputImage, XMin,
			YMin, XMax, YMax);
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
	 * @param ignoreBlackPixels
	 *          Flag to ignore black pixels (useful for clustering in case of
	 *          rotated images)
	 * @param binThr
	 *          Binarisation threshold
	 * @param binMode
	 *          Binarisation type : BINARISATION_MANUAL, BINARISATION_AUTO,
	 *          BINARISATION_CLUSTER, BINARISATION_KMEANS
	 * @param filterMode
	 *          Filtering type : FILTER_MANUAL, FILTER_GRANULOMETRY
	 * @param angleFirst
	 *          Initial rotation of the SE
	 * @param angleStep
	 *          Step for rotations of the SE
	 * @param nbClusters
	 *          Number of clusters, special case if equals to 0 (original image)
	 *          or 1 (inverted image)
	 * @param granStep
	 *          The size step of the SE in the granulometry
	 * @param globalTTR
	 *          Flag to determine if a global TTR should be finally applied
	 * @param minRatio
	 *          The minimum percentage of pixels kept by the opening operation,
	 *          otherwise the result is set to null for the given binary image
	 * @param occo
	 *          The size of the OCCO filter
	 * @param experiment
	 *          A generic String used in image caption (display) and filename
	 *          (save)
	 * @return Resulting image
	 */
	public static Image exec(Image inputImage, int XMin, int YMin, int XMax,
		int YMax, boolean view, boolean save, boolean debug,
		boolean ignoreBlackPixels, double binThr, int binMode, int filterMode,
		double angleFirst, double angleStep, int nbClusters, int granStep,
		boolean globalTTR, double minRatio, int occo, String experiment) {
		return (Image) new OriginalBinaryBuildingDetection().process(inputImage, XMin,
			YMin, XMax, YMax, view, save, debug, ignoreBlackPixels, binThr, binMode,
			filterMode, angleFirst, angleStep, nbClusters, granStep, globalTTR,
			minRatio, occo, experiment);
	}

	/*
	 * public static Image exec(Image img, int binMod, int binPara, double binTh,
	 * int filterMod, double angl, int XMin, int YMin, int XMax, int YMax) {
	 * return (Image) new BinaryBuildingDetection().process(img, binMod, binPara,
	 * binTh, filterMod, angl, XMin, YMin, XMax, YMax); }
	 */

}
