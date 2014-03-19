package fr.unistra.pelican.algorithms.applied.remotesensing.coastline;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.arithmetic.AdditionConstantChecked;
import fr.unistra.pelican.algorithms.arithmetic.Minimum;
import fr.unistra.pelican.algorithms.conversion.GrayToPseudoColors;
import fr.unistra.pelican.algorithms.morphology.binary.BinaryAreaOpening;
import fr.unistra.pelican.algorithms.morphology.binary.BinaryClosing;
import fr.unistra.pelican.algorithms.morphology.binary.BinaryDilation;
import fr.unistra.pelican.algorithms.morphology.binary.geodesic.BinaryFillHole;
import fr.unistra.pelican.algorithms.morphology.binary.geodesic.FastBinaryReconstruction;
import fr.unistra.pelican.algorithms.morphology.gray.GrayClosing;
import fr.unistra.pelican.algorithms.morphology.gray.GrayDilation;
import fr.unistra.pelican.algorithms.segmentation.ManualThresholding;
import fr.unistra.pelican.algorithms.segmentation.MarkerBasedWatershed;
import fr.unistra.pelican.algorithms.segmentation.labels.FrontiersFromSegmentation;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 * Perform coastline detection using a double threshold method (requires two
 * inputs images, a marker without false positive and a mask without false
 * negative
 * 
 * Puissant, Lefèvre, Weber : ISPRS 2008
 * 
 * @author Lefevre
 */
public class DoubleThresholdCoastlineDetector extends Algorithm {
	/**
	 * Image to be processed
	 */
	public Image marker;

	/**
	 * Image to be processed
	 */
	public Image mask;

	/**
	 * (optional) filtering size to deal with image borders
	 */
	public int filteringSize = 2;

	/**
	 * Resulting image
	 */
	public Image output;

	/**
	 * Constructor
	 */
	public DoubleThresholdCoastlineDetector() {
		super.inputs = "marker,mask";
		super.options = "filteringSize";
		super.outputs = "output";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		// Seuillage du marqueur
		BooleanImage thr1 = ManualThresholding.exec(marker, 1);
		thr1 = BinaryAreaOpening.exec(thr1, 5);

		// Seuillage du masque
		BooleanImage thr2 = ManualThresholding.exec(mask, 1);

		thr2 = BinaryClosing.exec(thr2, FlatStructuringElement2D
				.createCircleFlatStructuringElement(3));

		// Reconstruction géodésique thr1,thr2 puis remplissage des trous
		BooleanImage mask2 = (BooleanImage) FastBinaryReconstruction.exec(thr1,
				thr2);
		mask2 = (BooleanImage) BinaryFillHole.exec(mask2);
		// mask2 = BinaryDilation.exec(mask2, FlatStructuringElement2D
		// .createCircleFlatStructuringElement(filteringSize));
		mask2 = (BooleanImage) GrayDilation.exec(mask2,
				FlatStructuringElement2D
						.createSquareFlatStructuringElement(filteringSize * 2));
		// Création du relief
		mask = AdditionConstantChecked.exec(mask, 1 / 255.);
		Image relief = Minimum.exec(mask, mask2);
		relief = GrayClosing.exec(relief, FlatStructuringElement2D
				.createCircleFlatStructuringElement(3));

		// Application du watershed et récupération des frontières
		mask2 = BinaryDilation.exec(mask2, FlatStructuringElement2D
				.createCircleFlatStructuringElement(5));
		Image labels = MarkerBasedWatershed.exec(relief);// , mask2,false);
		output = FrontiersFromSegmentation.exec(labels);
		// output= DrawFrontiersFromElevation.exec(labels, relief);

		// FIXME utiliser le masque dans Watershed2 pour aller plus vite
		// Viewer2D.exec(GrayToPseudoColors.exec(relief));
		// Image labels = Watershed2.exec(relief);
		// output = FromSpecificLabelToBooleanImage.exec((IntegerImage) labels,
		// Watershed2.WSHED);

	}

	/**
	 * Method that applies the double threshold coastline detection
	 * 
	 * @param marker
	 *            Marker image
	 * @param mask
	 *            Mask image
	 * @return image with coastline
	 */
	public static Image exec(Image marker, Image mask) {
		return (Image) new DoubleThresholdCoastlineDetector().process(marker,
				mask);
	}

	public static Image exec(Image marker, Image mask, int filteringSize) {
		return (Image) new DoubleThresholdCoastlineDetector().process(marker,
				mask, filteringSize);
	}
}
