package fr.unistra.pelican.algorithms.statistics;

import java.util.Vector;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.morphology.gray.GrayClosing;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 * 
 * Given a colour image in a polar colour space, this class allegedly computes the major
 * hue references.
 *  
 * In more detail, the input is supposed to be a HSY (or similar: HSV,HSL) colour image. We
 * first compute the hue histogram..optionally weighted with saturation (+
 * luminance)
 * 
 * optionally an opening is applied
 * 
 * and the 1D-histogram is clustered..using either meanshift (on 1D???) or
 * threshold+CC or even a wshed
 * 
 * and we return the mean of each cluster and its discrete integral. as a vector
 * containing arrays of doubles to be used with the MultipleHueOrdering class
 * 
 * 19/12/2006
 * 
 * @author Abdullah
 * 
 */
public class MultipleReferenceHues extends Algorithm
{
	/**
	 * the input image
	 */
	public Image input;

	/**
	 * the computed reference points
	 */
	public Vector output;
	
	/**
	 * Given a colour image in a polar colour space, this class allegedly computes the major hue references
	 * @param input the input image
	 * @return the reference hues
	 */
	public static Vector exec(Image input)
	{
		return (Vector) new MultipleReferenceHues().process(input);
	}

	/**
	 * Constructor
	 * 
	 */
	public MultipleReferenceHues() {

		super();
		super.inputs = "input";
		super.outputs = "output";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException
	{
		// compute its normalised histogram...over 360 bins
		double[] histoOriginal = new double[360];
		double[] histoTmp = new double[360];

		for (int x = 0; x < input.getXDim(); x++) {
			for (int y = 0; y < input.getYDim(); y++) {
				double sat = input.getPixelXYBDouble(x, y, 1);
				double coeff = 1 / (1 + Math.exp(-5.0 * (sat - 0.5)));
				histoOriginal[(int) Math.floor(360.0 * input.getPixelXYBDouble(x,y,0))] += coeff;
			}
		}

		// normalize
		for (int i = 0; i < 360; i++)
			histoOriginal[i] = histoOriginal[i] / input.size();

		// Image histoImg = BasicHistogramViewer.exec(histoOriginal);
		// Viewer2D.exec(histoImg,"asil dagilim");

		// set histo as image
		Image tmp = new DoubleImage(360, 1, 1, 1, 1);

		for (int x = 0; x < 360; x++)
			tmp.setPixelXYDouble(x, 0, histoOriginal[x]);

		BooleanImage se = FlatStructuringElement2D
				.createHorizontalLineFlatStructuringElement(10);

		// close it
		tmp = (Image) new GrayClosing().process(tmp, se);

		// take back the values
		for (int x = 0; x < 360; x++)
			histoTmp[x] = tmp.getPixelXYDouble(x, 0);

		// get its average
		double avg = 0.0;

		for (int x = 0; x < 360; x++)
			avg += histoTmp[x];
		avg = avg / 360.0;
		System.err.println("Average hue:"+avg);
		//avg/=2;
		//avg = 0.00035;

		// threshold using its average
		for (int x = 0; x < 360; x++)
			if (histoTmp[x] < avg)
				histoTmp[x] = 0.0;

		//  histoImg = BasicHistogramViewer.exec(histoTmp);
		// Viewer2D.exec(histoImg,"islenmis dagilim");

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

		// for(int x = 0; x < 360; x++)
		// System.err.println(histo[x]);

		output = new Vector();

		// return the maximum and integral of each histogram section
		for (int i = 0; sections[i] != null; i++) {

			// get the maximum
			double[] couple = new double[2];
			double max = 0.0;

			int sectorLength = sections[i].end - sections[i].start;
			if (sectorLength < 0)
				sectorLength = 360 + sectorLength;

			for (int syc = 0, k = sections[i].start; syc < sectorLength; k = (++k) % 360, syc++) {
				if (histoOriginal[k] > max) {
					couple[0] = k / 360.0;
					max = histoOriginal[k];
				}

			}
			couple[1] = sections[i].sum;
			System.err.println("ref " + couple[0]);
			output.add(couple);
		}

		System.err.println(output.size());

		/*
		 * for(int k = 0; sections[k] != null; k++){ double[] d =
		 * (double[])output.get(k); System.err.println(d[0] + " " + d[1]); }
		 */

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
