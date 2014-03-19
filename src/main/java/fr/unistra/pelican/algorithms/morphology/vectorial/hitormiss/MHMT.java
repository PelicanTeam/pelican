package fr.unistra.pelican.algorithms.morphology.vectorial.hitormiss;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.morphology.gray.GrayDilation;
import fr.unistra.pelican.algorithms.morphology.gray.GrayErosion;
import fr.unistra.pelican.util.Point4D;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;
import fr.unistra.pelican.util.morphology.ValuedMonoBandFlatStructuringElement;

/**
 * 
 * This class computes a multivalued hit or miss transform, using channel wise
 * thresholds.
 * 
 * TODO : If you're courageous you can add the management of Z and T dimension
 * ;)
 * 
 * @author Jonathan Weber
 * 
 */
public class MHMT extends Algorithm {

	/**
	 * The image to compute
	 */
	public Image inputImage;

	/**
	 * Array of structuring element to use
	 */
	public ValuedMonoBandFlatStructuringElement[] fse;

	/**
	 * In case of multioriented MHMT, step of rotation
	 */
	public Double rotationStep = 360.0;

	/**
	 * Resulting image
	 */
	public Image outputImage;

	/**
	 * This method computes a multivalued hit or miss transform, using channel
	 * wise thresholds.
	 * 
	 * @param inputImage
	 *            the input image
	 * @param fse
	 *            Array of structuring element to use
	 * @return the output image
	 */
	public static Image exec(Image inputImage,
			ValuedMonoBandFlatStructuringElement[] fse) {
		return (Image) new MHMT().process(inputImage, fse);
	}

	/**
	 * Constructor
	 * 
	 */

	public MHMT() {
		super.inputs = "inputImage,fse";
		super.options = "rotationStep";
		super.outputs = "outputImage";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		int xDim = inputImage.getXDim();
		int yDim = inputImage.getYDim();
		Image[] bande = new Image[inputImage.getBDim()];

		for (int j = 0; j < inputImage.getBDim(); j++) {
			bande[j] = inputImage.getImage4D(j, Image.B);
		}
		outputImage = bande[0].copyImage(false);
		outputImage.fill(0.);

		// BooleanImage marker2 = new BooleanImage(bande[0].getXDim(), bande[0]
		// .getYDim(), 1, 1, 1);
		// marker2.fill(false);
		for (double angle = 0; angle < 360; angle = angle + rotationStep) {
			BooleanImage marker = new BooleanImage(bande[0].getXDim(), bande[0]
					.getYDim(), 1, 1, 1);
			marker.fill(true);
			Image[] resHMT = new Image[fse.length];
			// Fitting process
			for (int i = 0; i < fse.length; i++) {
				BooleanImage se = FlatStructuringElement2D.rotate(this.fse[i]
						.getSe(), angle);
				Point4D[] points = se.foreground();
				int bandIndex = this.fse[i].getBande();
				int centerX = se.getCenter().x;
				int centerY = se.getCenter().y;
				// System.out.println(angle + "( "+points.length+" )" +
				// fse[i].isInterne() + " "
				// + centerX + "," + centerY);
				// for (int ii = 0; ii < points.length; ii++) {
				// int valX = 0 - centerX + points[ii].x;
				// int valY = 0 - centerY + points[ii].y;
				// System.out.println(points[ii].x + " " + points[ii].y
				// + " => " + valX + " " + valY);
				// }
				if (fse[i].isInterne()) {
					for (int y = 0; y < yDim; y++)
						for (int x = 0; x < xDim; x++) {
							if (marker.getPixelXYBoolean(x, y)) {
								marker.setPixelXYBoolean(x, y, fitInternalSE(x,
										y, points, fse[i].getSeuil(), centerX,
										centerY, bande[bandIndex]));
							}
						}
				} else {
					for (int y = 0; y < yDim; y++)
						for (int x = 0; x < xDim; x++) {
							if (marker.getPixelXYBoolean(x, y)) {
								marker.setPixelXYBoolean(x, y, fitExternalSE(x,
										y, points, fse[i].getSeuil(), centerX,
										centerY, bande[bandIndex]));
							}
						}
				}
			}
			// marker2 = (BooleanImage) OR.exec(marker2, marker);
			for (int i = 0; i < fse.length; i++) {
				if (fse[i].isInterne())
					resHMT[i] = GrayErosion.exec(bande[fse[i].getBande()],
							FlatStructuringElement2D.rotate(fse[i].getSe(),
									angle), marker);
				else
					resHMT[i] = GrayDilation.exec(bande[fse[i].getBande()],
							FlatStructuringElement2D.rotate(fse[i].getSe(),
									angle), marker);
			}
			// Valuing process
			for (int i = 0; i < outputImage.size(); i++) {
				if (marker.getPixelBoolean(i)) {
					double valeur = 0;
					for (int j = 0; j < fse.length; j++) {
						double valeurlocale;
						if (fse[j].isInterne()) {
							valeurlocale = (resHMT[j].getPixelDouble(i) - fse[j]
									.getSeuil())
									/ (1 - fse[j].getSeuil());
						} else {
							valeurlocale = (fse[j].getSeuil() - resHMT[j]
									.getPixelDouble(i))
									/ fse[j].getSeuil();
						}

						valeur = valeur + valeurlocale;
					}
					outputImage.setPixelDouble(i, Math.max(outputImage
							.getPixelDouble(i), valeur / fse.length));
				}
			}
		}
		// Viewer2D.exec(marker2, "fit");
	}

	private boolean fitInternalSE(int x, int y, Point4D[] points,
			double thresh, int centerX, int centerY, Image band) {
		for (int i = 0; i < points.length; i++) {
			int valX = x - centerX + points[i].x;
			int valY = y - centerY + points[i].y;

			if (valX >= 0 && valX < this.inputImage.getXDim() && valY >= 0
					&& valY < this.inputImage.getYDim()) {
				if (band.getPixelXYDouble(valX, valY) < thresh)
					return false;
			} else
				return false;
		}
		return true;
	}

	private boolean fitExternalSE(int x, int y, Point4D[] points,
			double thresh, int centerX, int centerY, Image band) {
		for (int i = 0; i < points.length; i++) {
			int valX = x - centerX + points[i].x;
			int valY = y - centerY + points[i].y;

			if (valX >= 0 && valX < this.inputImage.getXDim() && valY >= 0
					&& valY < this.inputImage.getYDim()) {
				if (band.getPixelXYDouble(valX, valY) >= thresh)
					return false;
			} else
				return false;
		}
		return true;
	}
}
