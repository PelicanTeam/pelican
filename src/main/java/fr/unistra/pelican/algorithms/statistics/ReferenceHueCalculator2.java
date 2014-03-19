package fr.unistra.pelican.algorithms.statistics;

import java.util.Arrays;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.morphology.gray.GrayOpening;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 * This class computes the reference hue of an image as the maximum of its saturation
 * weighted histogram Something more evolved would take into account the
 * surroundings of the histogram bins...like the largest CC etc.
 * 
 * The input is supposed to be a HSY (or similar: HSV,HSL) colour image
 * 
 * 22/10/2006
 * 
 * @author Abdullah
 * 
 */
public class ReferenceHueCalculator2 extends Algorithm
{
	/**
	 * The input image 
	 */
	public Image input;

	/**
	 * The output hue
	 */
	public Double output;
	
	/**
	 * This class computes the reference hue of an image as the maximum of its saturation weighted histogram
	 * @param original The original image
	 * @return the reference hue
	 */
	public static Double exec(Image input)
	{
		return (Double) new ReferenceHueCalculator2().process(input);
	}

	/**
	 * Constructor
	 * 
	 */
	public ReferenceHueCalculator2() {

		super();
		super.inputs = "input";
		super.outputs = "output";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		double[] histo = new double[360];
		Arrays.fill(histo, 0.0);

		double slope = 10.0;
		double offset = 0.5;

		// compute the saturation weighted histogram
		for (int x = 0; x < input.getXDim(); x++) {
			for (int y = 0; y < input.getYDim(); y++) {
				double[] p = input.getVectorPixelXYZTDouble(x, y, 0, 0);

				int hue = (int) Math.floor(p[0] * 360.0);
				if (hue == 360)
					hue = 359;

				double beta = 0.0;

				if (p[2] <= 0.5)
					beta = 2 / (1 + Math.exp(-10 * (p[2] - 0.5)));
				else
					beta = 2 / (1 + Math.exp(10 * (p[2] - 0.5)));

				double alpha = 1 / (1 + Math.exp(-1 * slope * (p[1] - offset)))
						* beta;

				double coeff = alpha;

				histo[hue] += coeff;
			}
		}

		// opening yapalim buna...normalisation a gerek olmaz doubleImage olursa
		Image tmp = new DoubleImage(360, 1, 1, 1, 1);
		for (int x = 0; x < 360; x++)
			tmp.setPixelXYDouble(x, 0, histo[x]);

		BooleanImage se = FlatStructuringElement2D
				.createHorizontalLineFlatStructuringElement(3);
		tmp = (Image) new GrayOpening().process(tmp, se);

		for (int x = 0; x < 360; x++)
			histo[x] = tmp.getPixelXYDouble(x, 0);

		// ortalamayi bul
		double avg = 0.0;

		for (int x = 0; x < 360; x++)
			avg += histo[x];
		avg = avg / 360.0;

		// esikle ortalamayi kullanarak
		for (int x = 0; x < 360; x++)
			if (histo[x] < avg)
				histo[x] = 0.0;

		// simdi de sifir olmayan kesimleri bul...360 i 0 ile baglamayi unutma.
		Section[] sections = new Section[180]; // en fazla 180 olabilir;..deger
												// asiri yani

		int j = 0;
		boolean flag = false;

		for (int x = 0; x < 360; x++) {
			if (histo[x] > 0.0) {
				flag = false;
				if (sections[j] == null) {
					sections[j] = new Section(x, x);
					sections[j].sum += histo[x];
				} else {
					sections[j].end++;
					sections[j].sum += histo[x];
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
				sections[last].end += sections[0].end;
				sections[last].sum += sections[0].sum;
			}

		// for(int k = 0; sections[k] != null; k++)
		// System.err.println(sections[k].start + " " + sections[k].end + " " +
		// sections[k].sum);

		// for(int x = 0; x < 360; x++)
		// System.err.println(histo[x]);

		// get the section with the highest sum or integral
		int maxSec = 0;

		for (int k = 1; sections[k] != null; k++)
			if (sections[k].sum > sections[maxSec].sum)
				maxSec = k;

		// and now get the max of this chosen section
		int max = sections[maxSec].start;

		for (int start = sections[maxSec].start + 1; start <= sections[maxSec].end; start++)
			if (histo[start % 360] > histo[max])
				max = start % 360;

		output = max / 360.0;
		// System.err.println("Reference Hue 2 : " + output);
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
