package fr.unistra.pelican.algorithms.segmentation;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;


/**
 * 
 * Given a colour image in a polar colour space, this class performs a hue
 * clustering.
 * 
 * It is based on the statistics.MultipleReferenceHues process.
 * 
 * @author Lefevre
 * 
 */
public class HueClassification extends Algorithm {
	/**
	 * the input image
	 */
	public Image input;

	/**
	 * the threshold (ratio of the average)
	 */
	public double threshold=1.0;
	
	/**
	 * the labeled image
	 */
	public Image output;

	/**
	 * Given a colour image in a polar colour space, this class performs a hue
	 * clustering
	 * 
	 * @param input
	 *          the input image
	 * @return the reference hues
	 */
	public static Image exec(Image input) {
		return (Image) new HueClassification().process(input);
	}

	public static Image exec(Image input,double threshold) {
		return (Image) new HueClassification().process(input,threshold);
	}

	/**
	 * Constructor
	 * 
	 */
	public HueClassification() {
		super.inputs = "input";
		super.outputs = "output";
		super.options="threshold";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		// compute its normalised histogram...over 360 bins
		double[] histoOriginal = new double[360];
		double[] histoTmp = new double[360];
		int[] labels = new int[360];
		for (int x = 0; x < input.getXDim(); x++) {
			for (int y = 0; y < input.getYDim(); y++) {
				double sat = input.getPixelXYBDouble(x, y, 1);
				double coeff = 1 / (1 + Math.exp(-5.0 * (sat - 0.5)));
				histoOriginal[(int) Math
					.floor(360.0 * input.getPixelXYBDouble(x, y, 0))] += coeff;
			}
		}
		// normalize
		for (int i = 0; i < 360; i++)
			histoOriginal[i] = histoOriginal[i] / input.size();
		// set histo as image
		Image tmp = new DoubleImage(360, 1, 1, 1, 1);
		for (int x = 0; x < 360; x++)
			tmp.setPixelXYDouble(x, 0, histoOriginal[x]);
		//BooleanImage se = FlatStructuringElement2D.createHorizontalLineFlatStructuringElement(10);
		// close it
		// tmp = (Image) new GrayClosing().process(tmp, se);
		// take back the values
		for (int x = 0; x < 360; x++)
			histoTmp[x] = tmp.getPixelXYDouble(x, 0);
		// get its average
		double avg = 0.0;
		for (int x = 0; x < 360; x++)
			avg += histoTmp[x];
		avg /= 360.0;
		avg *= threshold;
		System.err.println("Average hue:" + avg);
		// threshold using its average
		for (int x = 0; x < 360; x++)
			if (histoTmp[x] < avg)
				histoTmp[x] = 0.0;
		// get CCs and dont forget to connect the extrema of the hue circle
		Section[] sections = new Section[180]; // max CC number
		int j = 0;
		boolean flag = false;
		for (int x = 0; x < 360; x++) {
			if (histoTmp[x] > 0.0) {
				flag = false;
				if (sections[j] == null) {
					sections[j] = new Section(x, x);
					sections[j].sum += histoOriginal[x];
				} else {
					sections[j].end++;
					sections[j].sum += histoOriginal[x];
				}
			} else {
				if (flag == false && sections[j] != null) {
					j++;
					flag = true;
				}
			}
		}

		// index of the last section
		for (int last = 0; sections[last] != null; last++)
			// if the first and last sections are connected add them to the last
			if (sections[0].start == 0 && sections[last].end == 359) {
				sections[last].end = sections[0].end;
				sections[last].sum += sections[0].sum;
				sections[0] = sections[last];
				sections[last] = null;
			}
		for (int k = 0; sections[k] != null; k++)
			System.err.println(sections[k].start + " " + sections[k].end + " "
				+ sections[k].sum);

		output = new IntegerImage(input.getXDim(), input.getYDim(), 1, 1, 1);
		// build the LUT
		for (int i = 0; sections[i] != null; i++) {
			for (int k = sections[i].start; k < sections[i].end; k++)
				labels[k] = i + 1;
			if (sections[i].start > sections[i].end) {
				for (int k = sections[i].start; k < 360; k++)
					labels[k] = i + 1;
				for (int k = 0; k < sections[i].end; k++)
					labels[k] = i + 1;
			}
		}

		// label the image
		for (int x = 0; x < input.getXDim(); x++)
			for (int y = 0; y < input.getYDim(); y++)
				output.setPixelXYInt(x, y, labels[(int) Math.floor(360.0 * input
					.getPixelXYBDouble(x, y, 0))]);

	}

	private class Section {
		int start;

		int end;

		double sum;

		Section(int start, int end) {
			this.start = start;
			this.end = end;
			this.sum = 0.0;
		}
	}

}
