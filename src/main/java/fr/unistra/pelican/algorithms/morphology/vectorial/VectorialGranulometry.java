package fr.unistra.pelican.algorithms.morphology.vectorial;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;
import fr.unistra.pelican.util.vectorial.orders.VectorialOrdering;

/**
 *	This class produces a normalized vectorial granulometric curve on 4 directions with line shaped 
 *	structuring elements.
 *	The result is normalised in [0,1]
 *	The size of SEs increases in steps of two, while both openings and closings are employed.
 * 
 *	For instance, a length of 10 would lead to a curve of 40 for each image channel. The first 5 
 *	being the antigranulometry and the next 5 the granulometry, all multiplied by the number of 
 *	directions.
 * 
 * @author Abdullah
 * 
 */
public class VectorialGranulometry extends Algorithm
{
	/**
	 * the input image
	 */
	public Image input;

	/**
	 * the length of the granulometric curve on each direction and with each operator
	 */
	public int length;

	/**
	 * whether to have scale invariance or not
	 */
	public boolean scaleInvariance;

	/**
	 * horizontal moment order
	 */
	public int MOMENTX;

	/**
	 * vertical moment order
	 */
	public int MOMENTY;
	
	/**
	 * the vectorial ordering to use
	 */
	public VectorialOrdering vo;

	/**
	 * the resulting curve
	 */
	public double[] curve;
	
	/**
	 * This class produces a normalized vectorial granulometric curve on 4 directions with line shaped structuring elements.
	 * @param image the input image
	 * @param size the length of the granulometric curve on each direction and with each operator
	 * @param scale whether to have scale invariance or not
	 * @param momentX horizontal moment
	 * @param momentY vertical moment
	 * @param vo vector ordering
	 * @return the granulometric curve
	 */
	public static double[] exec(Image input,Integer length,Boolean scaleInvariance,Integer momentX,Integer momentY,VectorialOrdering vo) {
		return (double[]) new VectorialGranulometry().process(input,length,scaleInvariance,momentX,momentY,vo);
	}

	/**
	 * Constructor
	 * 
	 */
	public VectorialGranulometry() {

		super();
		super.inputs = "input,length,scaleInvariance,MOMENTX,MOMENTY,vo";
		super.outputs = "curve";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		try {
			int size = (length + 1) * 4 * input.getBDim(); // length of SEs
															// increases in
															// steps of 2
			// System.err.println("size " + size);
			curve = new double[size];

			double[] originalVolumes = new double[input.getBDim()];

			for (int b = 0; b < input.getBDim(); b++)
				originalVolumes[b] = moment(input, b, MOMENTX, MOMENTY);

			// every size
			for (int i = 0; i < length; i += 2) {
				// vertical line
				BooleanImage se = FlatStructuringElement2D
						.createVerticalLineFlatStructuringElement(i * 2 + 1);

				// closing
				Image tmp = (Image) new VectorialClosing().process(input, se, vo);

				for (int b = 0; b < input.getBDim(); b++)
					curve[b * 4 * (length + 1) + (length + 1) / 2 - 1 - i / 2] = moment(
							tmp, b, MOMENTX, MOMENTY)
							/ originalVolumes[b];

				// opening
				tmp = (Image) new VectorialOpening().process(input, se, vo);

				for (int b = 0; b < input.getBDim(); b++)
					curve[b * 4 * (length + 1) + (length + 1) / 2 + i / 2] = moment(
							tmp, b, MOMENTX, MOMENTY)
							/ originalVolumes[b];

				// left diagonal line
				se = FlatStructuringElement2D
						.createLeftDiagonalLineFlatStructuringElement(i * 2 + 1);

				// closing
				tmp = (Image) new VectorialClosing().process(input, se, vo);

				for (int b = 0; b < input.getBDim(); b++)
					curve[b * 4 * (length + 1) + (length + 1) + (length + 1)
							/ 2 - 1 - i / 2] = moment(tmp, b, MOMENTX, MOMENTY)
							/ originalVolumes[b];

				// opening
				tmp = (Image) new VectorialOpening().process(input, se, vo);

				for (int b = 0; b < input.getBDim(); b++)
					curve[b * 4 * (length + 1) + (length + 1) + (length + 1)
							/ 2 + i / 2] = moment(tmp, b, MOMENTX, MOMENTY)
							/ originalVolumes[b];

				// horizontal line
				se = FlatStructuringElement2D
						.createHorizontalLineFlatStructuringElement(i * 2 + 1);

				// closing
				tmp = (Image) new VectorialClosing().process(input, se, vo);

				for (int b = 0; b < input.getBDim(); b++)
					curve[b * 4 * (length + 1) + 2 * (length + 1)
							+ (length + 1) / 2 - 1 - i / 2] = moment(tmp, b,
							MOMENTX, MOMENTY)
							/ originalVolumes[b];

				// opening
				tmp = (Image) new VectorialOpening().process(input, se, vo);

				for (int b = 0; b < input.getBDim(); b++)
					curve[b * 4 * (length + 1) + 2 * (length + 1)
							+ (length + 1) / 2 + i / 2] = moment(tmp, b,
							MOMENTX, MOMENTY)
							/ originalVolumes[b];

				// right diagonal line
				se = FlatStructuringElement2D
						.createRightDiagonalLineFlatStructuringElement(i * 2 + 1);

				// closing
				tmp = (Image) new VectorialClosing().process(input, se, vo);

				for (int b = 0; b < input.getBDim(); b++)
					curve[b * 4 * (length + 1) + 3 * (length + 1)
							+ (length + 1) / 2 - 1 - i / 2] = moment(tmp, b,
							MOMENTX, MOMENTY)
							/ originalVolumes[b];

				// opening
				tmp = (Image) new VectorialOpening().process(input, se, vo);

				for (int b = 0; b < input.getBDim(); b++)
					curve[b * 4 * (length + 1) + 3 * (length + 1)
							+ (length + 1) / 2 + i / 2] = moment(tmp, b,
							MOMENTX, MOMENTY)
							/ originalVolumes[b];
			}
		} catch (PelicanException e) {
			e.printStackTrace();
		}
	}

	private double moment(Image img, int channel, int i, int j) {
		double d = 0.0;

		double ort = 0.0;

		int gamma = (i + j) / 2 + 1;

		for (int x = 0; x < img.getXDim(); x++) {
			for (int y = 0; y < img.getYDim(); y++) {

				if ( input.isPresentXYB( x,y,channel ) ) { 
				
					double tmp = img.getPixelXYBDouble(x, y, channel);
					ort += tmp;
					d += Math.pow(x + 1, i) * Math.pow(y + 1, j) * tmp;
				}
			}
		}

		ort = Math.pow(ort, gamma);

		if ((i + j >= 2) && scaleInvariance == true)
			d = d / ort;

		return d;
	}

}
