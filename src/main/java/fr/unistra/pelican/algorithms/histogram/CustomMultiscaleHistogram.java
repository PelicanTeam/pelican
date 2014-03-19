package fr.unistra.pelican.algorithms.histogram;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.morphology.gray.GrayASF;
import fr.unistra.pelican.algorithms.morphology.gray.GrayLeveling;
import fr.unistra.pelican.algorithms.morphology.gray.GrayOpening;
import fr.unistra.pelican.algorithms.morphology.gray.geodesic.GrayOpeningByReconstruction;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 * 
 * Produces normalized histograms with a custom number of bins using a custom
 * scale-space.
 * 
 * @author Erchan Aptoula
 * 
 */
public class CustomMultiscaleHistogram extends Algorithm {

	/**
	 * first input parameter.
	 */
	public Image input;

	/**
	 * Output parameter.
	 */
	public double[] output;

	/**
	 * Second input parameter which is the number of bins of each band.
	 */
	public int size;

	/**
	 * Third input parameter which is the number of scales.
	 */
	public int scales;

	/**
	 * Fourth input parameter which is the type of scale.
	 */
	public int type;

	private static final int Opening = 0;

	private static final int OpeningByRecon = 1;

	private static final int ASF = 2;

	private static final int Leveling = 3;

	/**
	 * Constructor
	 * 
	 */
	public CustomMultiscaleHistogram() {

		super();
		super.inputs = "input,size,scales,type";
		super.outputs = "output";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		int totalHistoSizePerScale = size * size * size;

		output = new double[scales * totalHistoSizePerScale];

		for (int s = 0; s < scales; s++) {

			// prepare this scale
			Image tmp = null;

			if (type == Opening) {
				BooleanImage se = FlatStructuringElement2D
						.createSquareFlatStructuringElement(s * 2 + 1);
				tmp = (Image) new GrayOpening().process(input, se);
			} else if (type == OpeningByRecon) {
				BooleanImage se = FlatStructuringElement2D
						.createSquareFlatStructuringElement(s * 2 + 1);
				tmp = (Image) new GrayOpeningByReconstruction().process(input,
						se);
			} else if (type == ASF) {
				BooleanImage se = FlatStructuringElement2D
						.createSquareFlatStructuringElement(5);
				tmp = (Image) new GrayASF().process(input, se,
						GrayASF.OPENING_FIRST, new Integer(s + 1));
			} else if (type == Leveling) {
				BooleanImage se = FlatStructuringElement2D
						.createSquareFlatStructuringElement(5);
				Image marker = (Image) new GrayASF().process(input, se,
						GrayASF.OPENING_FIRST, new Integer(s * 2 + 1));
				tmp = (Image) new GrayLeveling().process(input, marker,
						new Integer(0));
			} else
				throw new AlgorithmException("Unsupported scale space type");

			// extract the histogram
			for (int x = 0; x < input.getXDim(); x++) {
				for (int y = 0; y < input.getYDim(); y++) {
					int[] p = tmp.getVectorPixelXYZTByte(x, y, 0, 0);
					output[s * totalHistoSizePerScale + p[0] * size * size
							+ p[1] * size + p[2]]++;
				}
			}
		}

		// normalize
		for (int i = 0; i < output.length; i++)
			output[i] = output[i] / (input.getXDim() * input.getYDim());
	}

	/**
	 * Produces normalized histograms with a custom number of bins using a
	 * custom scale-space.
	 * 
	 * @param inputImage
	 *            Image to be converted in a histogram.
	 * @return The normalized histogram.
	 */
	public static Image exec(Image input) {
		return (Image) new CustomMultiscaleHistogram().process(input);
	}
	
	/**
	 * Produces normalized histograms with a custom number of bins using a
	 * custom scale-space.
	 * 
	 * @param inputImage
	 * 				Image to be converted in a histogram.
	 * @param size
	 * 				The number of bins of each band.
	 * @param scales
	 * 				The number of scales.
	 * @param type
	 * 				The type of scale.
	 * @return The normalized histogram.
	 */
	public static Image exec(Image input, int size, int scales, int type) {
		return (Image) new CustomMultiscaleHistogram().process(input, size, scales, type);
	}
}
