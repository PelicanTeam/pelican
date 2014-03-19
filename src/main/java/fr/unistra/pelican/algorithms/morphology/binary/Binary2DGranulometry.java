package fr.unistra.pelican.algorithms.morphology.binary;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 * Compute a 2D granulometry using rectangular SE and efficient implementation
 * 
 * @author Jonathan Weber, Lefevre
 * 
 */
public class Binary2DGranulometry extends Algorithm {

	/**
	 * Image to be processed
	 */
	public BooleanImage inputImage;

	/**
	 * Limit max of SE's length
	 */
	public Integer xMax;

	/**
	 * Limit max of SE's width
	 */
	public Integer yMax;

	/**
	 * Step for SE's size variation
	 */
	public Integer step;

	/**
	 * SE's angle rotation
	 */
	public Double angle = 0.;

	/**
	 * Flag to compute differential granulometry instead of standard one
	 */
	public boolean diff = false;

	/**
	 * Granulometry array
	 */
	public Double[][] outputTab;

	/**
	 * Constructor
	 * 
	 */
	public Binary2DGranulometry() {
		super.inputs = "inputImage,xMax,yMax,step";
		super.options = "angle,diff";
		super.outputs = "outputTab";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		// Initialisation
		int xCurve, yCurve;
		if (xMax % 2 == 0)
			xCurve = xMax / step;
		else
			xCurve = xMax / step + 1;
		if (yMax % 2 == 0)
			yCurve = yMax / step;
		else
			yCurve = yMax / step + 1;
		outputTab = new Double[xCurve][yCurve];
		// Compute the total number of pixels to be used in the ratio measure
		double total = inputImage.getSum();
		boolean calcul;
		// Computation of the granulometry
		for (int i = 0; i < xMax; i = i + step) {
			calcul = true;
			BooleanImage erosionH = BinaryErosion
				.exec(inputImage, FlatStructuringElement2D
					.createLineFlatStructuringElement(i + 1, angle), 2);
			// BooleanImage erosionH = BinaryErosion.exec(inputImage,
			// FlatStructuringElement2D.rotate(FlatStructuringElement2D
			// .createHorizontalLineFlatStructuringElement(i + 1), angle), 2);
			for (int j = 0; j < yMax; j = j + step) {
				if (calcul) {
					// Decompose the 2D opening by a sequence of 1D erosions and
					// dilations
					BooleanImage erosionV = BinaryErosion.exec(erosionH,
						FlatStructuringElement2D.createLineFlatStructuringElement(j + 1,
							angle + 90), 2);
					BooleanImage dilationH = BinaryDilation.exec(erosionV,
						FlatStructuringElement2D.createLineFlatStructuringElement(i + 1,
							angle), 2);
					BooleanImage open = BinaryDilation.exec(dilationH,
						FlatStructuringElement2D.createLineFlatStructuringElement(j + 1,
							angle + 90), 2);
					// BooleanImage erosionV = BinaryErosion.exec(erosionH,
					// FlatStructuringElement2D.rotate(FlatStructuringElement2D
					// .createVerticalLineFlatStructuringElement(j + 1), angle), 2);
					// BooleanImage dilationH = BinaryDilation.exec(erosionV,
					// FlatStructuringElement2D.rotate(FlatStructuringElement2D
					// .createHorizontalLineFlatStructuringElement(i + 1), angle), 2);
					// BooleanImage open = BinaryDilation.exec(dilationH,
					// FlatStructuringElement2D.rotate(FlatStructuringElement2D
					// .createVerticalLineFlatStructuringElement(j + 1), angle), 2);
					// Compute the number of pixels after the opening
					double nb = open.getSum();
					outputTab[i / step][j / step] = nb / total;
					// Check if optimization can be applied
					if (nb == 0.)
						calcul = false;
				} else {
					// Set all remaining values of the line to 0
					int z;
					for (z = j; z < yMax; z = z + step)
						outputTab[i / step][z / step] = 0.;
					j = z;
				}
			}
		}
		if (diff) {
			Double outputTab2[][] = new Double[xCurve][yCurve];
			for (int i = 0; i < xCurve; i++)
				for (int j = 0; j < yCurve; j++) {
					if (i == 0 && j == 0)
						outputTab2[i][j] = 0.0;
					else if (i == 0)
						outputTab2[i][j] = outputTab[i][j - 1] - outputTab[i][j];
					else if (j == 0)
						outputTab2[i][j] = outputTab[i - 1][j] - outputTab[i][j];
					else
						outputTab2[i][j] = 2 * outputTab[i - 1][j - 1]
							- outputTab[i][j - 1] - outputTab[i - 1][j];
					outputTab2[i][j] /= 2;
				}
			outputTab = outputTab2;
		}

	}

	/**
	 * This method computes a 2D granulometry using rectangular SE and efficient
	 * implementation.
	 * 
	 * @param inputImage
	 *          Image to be processed
	 * @param xMax
	 *          Limit max of SE's length
	 * @param yMax
	 *          Limit max of SE's width
	 * @param step
	 *          Step for SE's size variation
	 * @param angle
	 *          SE's angle rotation
	 * @return 2D granulometry
	 */
	public static Double[][] exec(BooleanImage inputImage, Integer xMax,
		Integer yMax, Integer step, Double angle) {
		return (Double[][]) new Binary2DGranulometry().process(inputImage, xMax,
			yMax, step, angle);
	}

	public static Double[][] exec(BooleanImage inputImage, Integer xMax,
		Integer yMax, Integer step, Double angle, boolean diff) {
		return (Double[][]) new Binary2DGranulometry().process(inputImage, xMax,
			yMax, step, angle, diff);
	}

}