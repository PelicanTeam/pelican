package fr.unistra.pelican.algorithms.morphology.binary.hitormiss;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.arithmetic.Difference;
import fr.unistra.pelican.algorithms.arithmetic.Equal;
import fr.unistra.pelican.algorithms.logical.OR;
import fr.unistra.pelican.algorithms.morphology.binary.geodesic.FastBinaryReconstruction;
import fr.unistra.pelican.util.morphology.CompositeStructuringElement;

/**
 * This class realizes the binary homotopic sequential thinning of its input (8
 * connected) The result is a boolean image.
 * 
 * @author aptoula, weber, lefevre
 */
public class BinaryHST extends Algorithm {
	/**
	 * Image to process
	 */
	public Image input;

	public int filteringSize = 0;

	/**
	 * Resulting picture
	 */
	public BooleanImage output;

	/**
	 * Constructor
	 * 
	 */
	public BinaryHST() {
		super.inputs = "input";
		super.options = "filteringSize";
		super.outputs = "output";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() {

		int fr = CompositeStructuringElement.FOREGROUND;
		int bg = CompositeStructuringElement.BACKGROUND;
		int un = CompositeStructuringElement.UNDEFINED;

		int val[][] = { { bg, bg, bg, un, fr, un, fr, fr, fr },
			{ fr, un, bg, fr, fr, bg, fr, un, bg },
			{ fr, fr, fr, un, fr, un, bg, bg, bg },
			{ bg, un, fr, bg, fr, fr, bg, un, fr },
			{ un, bg, bg, fr, fr, bg, un, fr, un },
			{ un, fr, un, fr, fr, bg, un, bg, bg },
			{ un, fr, un, bg, fr, fr, bg, bg, un },
			{ bg, bg, un, bg, fr, fr, un, fr, un } };

		BooleanImage se1fg[] = new BooleanImage[8];
		BooleanImage se1bg[] = new BooleanImage[8];

		for (int i = 0; i < 8; i++) {
			se1fg[i] = new BooleanImage(3, 3, 1, 1, 1);
			se1fg[i].resetCenter();
			se1bg[i] = new BooleanImage(3, 3, 1, 1, 1);
			se1bg[i].resetCenter();
			for (int j = 0; j < se1fg[i].size(); j++) {
				if (val[i][j] == fr)
					se1fg[i].setPixelBoolean(j, true);
				else if (val[i][j] == bg)
					se1bg[i].setPixelBoolean(j, true);
			}
		}

		output = new BooleanImage(input, true);
		BooleanImage tmp = null;
		int cnt = 0;

		do {
			tmp = output;

			for (int i = 0; i < 8; i++)
				output = BinaryThinning.exec(output, se1fg[i], se1bg[i]);
			cnt++;

			// System.err.println(cnt);
			// if(cnt % 25 == 0) Viewer2D.exec(output,"" + cnt);

		} while (!Equal.exec(output, tmp));

		// optional step
		if (filteringSize > 0) {
			BooleanImage skel = output.copyImage(true);

			int val2[][] = { { bg, bg, bg, bg, fr, bg, bg, un, un },
				{ bg, bg, bg, un, fr, bg, un, bg, bg },
				{ un, un, bg, bg, fr, bg, bg, bg, bg },
				{ bg, bg, un, bg, fr, un, bg, bg, bg },
				{ bg, bg, bg, bg, fr, un, bg, bg, un },
				{ bg, bg, bg, bg, fr, bg, un, un, bg },
				{ un, bg, bg, un, fr, bg, bg, bg, bg },
				{ bg, un, un, bg, fr, bg, bg, bg, bg } };

			BooleanImage se2fg[] = new BooleanImage[8];
			BooleanImage se2bg[] = new BooleanImage[8];

			for (int i = 0; i < 8; i++) {
				se2fg[i] = new BooleanImage(3, 3, 1, 1, 1);
				se2fg[i].resetCenter();
				se2bg[i] = new BooleanImage(3, 3, 1, 1, 1);
				se2bg[i].resetCenter();
				for (int j = 0; j < se2fg[i].size(); j++) {
					if (val2[i][j] == fr)
						se2fg[i].setPixelBoolean(j, true);
					else if (val2[i][j] == bg)
						se2bg[i].setPixelBoolean(j, true);
				}
			}
			int cnt2 = 0;
			do {
				tmp = output;

				for (int i = 0; i < 8; i++)
					output = BinaryThinning.exec(output, se2fg[i], se2bg[i]);
				cnt2++;

			} while (cnt2 < filteringSize);

			/*
			 * boolean ultime=false; if (output.isEmpty()) ultime=true; } while
			 * (!(Boolean)new Equal().process(output, tmp) && !ultime && cnt2<size);
			 * if (ultime) output=tmp;
			 */

			tmp = (BooleanImage) FastBinaryReconstruction.exec(Difference.exec(tmp,
				output), Difference.exec(skel, output));
			output = (BooleanImage) OR.exec(tmp, output);
		}

	}

	/**
	 * This method realizes the binary homotopic sequential thinning of its input
	 * 
	 * @param input
	 *          image to process
	 * @return Image thinned image
	 */
	public static BooleanImage exec(Image input) {
		return (BooleanImage) new BinaryHST().process(input);
	}

	public static BooleanImage exec(Image input, int filteringSize) {
		return (BooleanImage) new BinaryHST().process(input, filteringSize);
	}
}