package fr.unistra.pelican.algorithms.morphology.binary;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 * Compute an angular granulometry with line SE
 * 
 * @author Lefevre
 * 
 */
public class BinaryAngularGranulometry extends Algorithm {

	/**
	 * Image to be processed
	 */
	public BooleanImage inputImage;

	/**
	 * Number of SE lengths
	 */
	public Integer length;

	/**
	 * Number of SE angles
	 */
	public Integer angles;

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
	public BinaryAngularGranulometry() {
		super.inputs = "inputImage,length,angles";
		super.options = "diff";
		super.outputs = "outputTab";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		// Initialisation
		outputTab = new Double[length][angles];
		// Compute the total number of pixels to be used in the ratio measure
		double total = inputImage.getSum();
		// Computation of the granulometry
		for (int i = 0; i < length; i++)
			for (int a = 0; a < angles; a++) {
				BooleanImage se=FlatStructuringElement2D.createLineFlatStructuringElement(2*i+1, a*180/angles);
				//BooleanImage open = BinaryOpening.exec(inputImage,se);
				BooleanImage open = BinaryErosion.exec(inputImage,se);
				open= BinaryDilation.exec(open,se);
				// Compute the number of pixels after the opening
				double nb = open.getSum();
				outputTab[i][a] = nb / total;
			}
		if (diff) {
			Double outputTab2[][] = new Double[length][angles];
			for (int i = 0; i < length; i++)
				for (int j = 0; j < angles; j++) {
					if (i == 0 && j == 0)
						outputTab2[i][j] = 0.0;
					else if (i == 0)
						outputTab2[i][j] = outputTab[i][j - 1]
								- outputTab[i][j];
					else if (j == 0)
						outputTab2[i][j] = outputTab[i - 1][j]
								- outputTab[i][j];
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
	 *            Image to be processed
	 * @param xMax
	 *            Limit max of SE's length
	 * @param yMax
	 *            Limit max of SE's width
	 * @param step
	 *            Step for SE's size variation
	 * @param angle
	 *            SE's angle rotation
	 * @return 2D granulometry
	 */
	public static Double[][] exec(BooleanImage inputImage, Integer length,
			Integer angles) {
		return (Double[][]) new BinaryAngularGranulometry().process(inputImage,
				length, angles);
	}

	public static Double[][] exec(BooleanImage inputImage, Integer length,
			Integer angles, boolean diff) {
		return (Double[][]) new BinaryAngularGranulometry().process(inputImage,
				length, angles, diff);
	}

}