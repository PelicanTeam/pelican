package fr.unistra.pelican.algorithms.morphology.binary;

import java.awt.Point;
import java.util.ArrayList;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.arithmetic.Inversion;
import fr.unistra.pelican.algorithms.logical.OR;
import fr.unistra.pelican.util.Point4D;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 * This class performs a binary rectangular HMT with various size. background SE
 * is the one pixel size perimeter of the rectangle as foreground is the
 * rectangle of size alpha*rectangle size centered at the center of the
 * rectangle.
 * 
 * @author Jonathan Weber
 */

public class BinaryRectangularVariableHMT extends Algorithm {

	/**
	 * Image to be processed
	 */
	public Image inputImage;

	/**
	 * Inferior limit of rectangule X value
	 */
	public Integer Xmin;
	/**
	 * Superior limit of rectangule X value
	 */
	public Integer Xmax;
	/**
	 * Inferior limit of rectangule Y value
	 */
	public Integer Ymin;
	/**
	 * Superior limit of rectangule Y value
	 */
	public Integer Ymax;

	/**
	 * Resulting picture
	 */
	public BooleanImage outputImage;

	/**
	 * Orientation angle of the rectangle. Default is 0
	 */
	public Double angle = 0.;

	/**
	 * Step for size variance Default is 2
	 */
	public Integer step = 2;

	/**
	 * ratio of foreground SE Default is 0.6
	 */
	public double alpha = 0.6;

	/**
	 * Constructor
	 * 
	 */
	public BinaryRectangularVariableHMT() {
		super.inputs = "inputImage,Xmin,Xmax,Ymin,Ymax";
		super.options = "step,alpha,angle";
		super.outputs = "outputImage";

	}

	public void launch() throws AlgorithmException {
		int XDim = inputImage.getXDim();
		int YDim = inputImage.getYDim();
		Image inversion = (Image) new Inversion().process(inputImage);
		outputImage = new BooleanImage(inputImage.getXDim(), inputImage.getYDim(),
			inputImage.getZDim(), inputImage.getTDim(), 1);
		outputImage.fill(0.);
		Point pointsTab[][] = new Point[XDim][YDim];
		for (int i = 0; i < XDim; i++)
			for (int j = 0; j < YDim; j++) {
				pointsTab[i][j] = new Point();
				pointsTab[i][j].x = Xmax + 1;
				pointsTab[i][j].y = Ymax + 1;
			}

		for (int Tx = Xmin; Tx <= Xmax; Tx += step) {
			BooleanImage eroHMarker = new BooleanImage(inputImage.getXDim(),
				inputImage.getYDim(), inputImage.getZDim(), inputImage.getTDim(), 1);
			eroHMarker.fill(0.);
			// Computation of the horizontal erosion
			BooleanImage fse;
//			if (angle != 0.)
				fse = FlatStructuringElement2D.createLineFlatStructuringElement((int) (alpha * Tx),
					angle);
//				fse = FlatStructuringElement2D.rotate(FlatStructuringElement2D
//					.createHorizontalLineFlatStructuringElement((int) (alpha * Tx)),
//					angle);
//			else
//				fse = FlatStructuringElement2D
//					.createHorizontalLineFlatStructuringElement((int) (alpha * Tx))/* .rotate(angle) */;
			BinaryErosion erosion = new BinaryErosion();
			ArrayList<Object> inputs = new ArrayList<Object>(3);
			inputs.add(inputImage);
			inputs.add(fse);
			inputs.add(2);
			inputs.add(true);
			erosion.setInput(inputs);

			Point4D[] points = fse.foreground();

			for (int x = 0; x < XDim; x++)
				for (int y = 0; y < YDim; y++) {
					if (pointsTab[x][y].x > Tx) {
						boolean result = erosion.getMin(x, y, 0, 0, 0, points);
						if (result) {
							eroHMarker.setPixelBoolean(x, y, 0, 0, 0, true);
						} else {
							pointsTab[x][y].x = Tx;
						}
					}
				}
			for (int Ty = Ymin; Ty <= Ymax; Ty += step) {

				// Computation of the Vertical erosion
				BooleanImage eroVMarker = new BooleanImage(inputImage.getXDim(),
					inputImage.getYDim(), inputImage.getZDim(), inputImage.getTDim(), 1);
				eroVMarker.fill(0.);
//				if (angle != 0.)
					fse = FlatStructuringElement2D.createLineFlatStructuringElement((int) (alpha * Ty),
						angle+90);
//					fse = FlatStructuringElement2D.rotate(FlatStructuringElement2D
//						.createVerticalLineFlatStructuringElement((int) (alpha * Ty)),
//						angle);
//				else
//					fse = FlatStructuringElement2D
//						.createVerticalLineFlatStructuringElement((int) (alpha * Ty));
				erosion = new BinaryErosion();
				inputs = new ArrayList<Object>(3);
				inputs.add(eroHMarker);
				inputs.add(fse);
				inputs.add(2);
				inputs.add(true);
				erosion.setInput(inputs);

				points = fse.foreground();

				for (int x = 0; x < XDim; x++)
					for (int y = 0; y < YDim; y++) {
						if (pointsTab[x][y].x > Tx && pointsTab[x][y].y > Ty) {
							boolean result = erosion.getMin(x, y, 0, 0, 0, points);
							if (result) {
								eroVMarker.setPixelBoolean(x, y, 0, 0, 0, true);
							} else {
								pointsTab[x][y].y = Ty;
							}
						}
					}
				// Computation of the background erosion
				BooleanImage eroBG = new BooleanImage(inputImage.getXDim(), inputImage
					.getYDim(), inputImage.getZDim(), inputImage.getTDim(), 1);
				eroBG.fill(0.);
				fse = new BooleanImage(Tx, Ty, 1, 1, 1);
				fse.resetCenter();
				for (int k = 0; k < Tx; k++)
					for (int l = 0; l < Ty; l++) {
						if (k == 0 | k == (Tx - 1) | l == 0 | l == (Ty - 1))
							fse.setPixelXYBoolean(k, l, true);
						else
							fse.setPixelXYBoolean(k, l, false);
					}
				if (angle != 0.)
					fse = FlatStructuringElement2D.rotate(fse, angle);
				
				erosion = new BinaryErosion();
				inputs = new ArrayList<Object>(3);
				inputs.add(inversion);
				inputs.add(fse);
				inputs.add(2);
				inputs.add(true);
				erosion.setInput(inputs);
				points = fse.foreground();

				for (int x = 0; x < XDim; x++)
					for (int y = 0; y < YDim; y++) {
						// if(pointsTab[x][y].x>Tx&&pointsTab[x][y].y>Ty)
						if (eroVMarker.getPixelBoolean(x, y, 0, 0, 0)) {

							boolean result = erosion.getMin(x, y, 0, 0, 0, points);
							if (result) {
								eroBG.setPixelBoolean(x, y, 0, 0, 0, true);
							}
						}
					}
//eroBG=eroVMarker.copyImage(true);

				outputImage = (BooleanImage) new OR().process(outputImage, eroBG);

			}
		}
	}

	/**
	 * This method performs a binary rectangular HMT with various size.
	 * 
	 * @param InputImage
	 *          image to be processed
	 * @param xmin
	 *          Inferior limit of rectangule X value
	 * @param xmax
	 *          Superior limit of rectangule X value
	 * @param ymin
	 *          Inferior limit of rectangule Y value
	 * @param ymax
	 *          Superior limit of rectangule Y value
	 * @return Rectangular variable HMT picture
	 */
	public static BooleanImage exec(Image InputImage, Integer xmin, Integer xmax,
		Integer ymin, Integer ymax) {
		return (BooleanImage) new BinaryRectangularVariableHMT().process(
			InputImage, xmin, xmax, ymin, ymax);
	}

	public static BooleanImage exec(Image InputImage, Integer xmin, Integer xmax,
		Integer ymin, Integer ymax,int step,double alpha,double angle) {
		return (BooleanImage) new BinaryRectangularVariableHMT().process(
			InputImage, xmin, xmax, ymin, ymax,step,alpha,angle);
	}

	public static BooleanImage exec(Image InputImage, Integer xmin, Integer xmax,
		Integer ymin, Integer ymax,int step,double angle) {
		return (BooleanImage) new BinaryRectangularVariableHMT().process(
			InputImage, xmin, xmax, ymin, ymax,step,null,angle);
	}

	public static BooleanImage exec(Image InputImage, Integer xmin, Integer xmax,
		Integer ymin, Integer ymax,double angle) {
		return (BooleanImage) new BinaryRectangularVariableHMT().process(
			InputImage, xmin, xmax, ymin, ymax,null,null,angle);
	}

}
