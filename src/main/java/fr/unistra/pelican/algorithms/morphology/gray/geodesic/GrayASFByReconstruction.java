package fr.unistra.pelican.algorithms.morphology.gray.geodesic;

import java.awt.Point;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.morphology.gray.GrayDilation;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 * Alternating Sequential filters: opening and closing (for fun and profit)
 * 
 * @author Erchan Aptoula, Benjamin Perret
 */
public class GrayASFByReconstruction extends Algorithm {
	
	/**
	 * Input image
	 */
	public Image input;

	/**
	 * Input structuring element
	 */
	public BooleanImage se;
	
	/**
	 * Option to choose which filter to start with
	 * @see fr.unistra.pelican.algorithms.morphology.gray.geodesic.GrayASFByReconstruction#CLOSING_FIRST
	 * @see fr.unistra.pelican.algorithms.morphology.gray.geodesic.GrayASFByReconstruction#OPENING_FIRST
	 */
	public int flag;

	/**
	 * Number of iteration
	 */
	public int times;

	/**
	 * Option to start with an opening
	 */
	public static final int OPENING_FIRST = 0;

	/**
	 * Option to start with a closing
	 */
	public static final int CLOSING_FIRST = 1;

	/**
	 * Result
	 */
	public Image output;

	/**
	 * Constructor
	 * 
	 */
	public GrayASFByReconstruction() {

		super();
		super.inputs = "input,se,flag,times";
		super.outputs = "output";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		output = input.copyImage(false);

		if (flag != OPENING_FIRST && flag != CLOSING_FIRST)
			throw new AlgorithmException("Invalid flag");

		if (times < 1)
			throw new AlgorithmException(
					"The number of iterations should be at least one");

		try {
			// the SE used to dilate the given SE
			BooleanImage magnifier = FlatStructuringElement2D
					.createSquareFlatStructuringElement(3);

			// prepare the SE so that it can take the dilation results...
			BooleanImage tmp = new BooleanImage(se
					.getXDim()
					+ 2 * times, se.getYDim() + 2 * times,1,1,1);
			tmp.setCenter(new Point(se
					.getCenter().x
					+ times, se.getCenter().y + times));
			tmp.fill(false);

			for (int x = 0; x < se.getXDim(); x++)
				for (int y = 0; y < se.getYDim(); y++)
					tmp.setPixelXYBoolean(x + times, y + times, true);
			se = tmp;

			output = input;

			for (int i = 0; i < times; i++) {
				if (flag == OPENING_FIRST) {
					output = GrayOpeningByReconstruction.exec(output, se);
					output = GrayClosingByReconstruction.exec(output, se);
				} else {
					output = GrayClosingByReconstruction.exec(output, se);
					output = GrayOpeningByReconstruction.exec(output, se);
				}
				se = (BooleanImage) GrayDilation.exec(se,
						magnifier);
			}

		} catch (PelicanException e) { e.printStackTrace(); }
	}
	
	/**
	 * Alternating Sequential filters: opening and closing (for fun and profit)
	 *
	 * @param input Input Image
	 * @param se Input Structuring Element
	 * @param flag Specify which filter to start with (OPENING_FIRST  or CLOSING_FIRST)
	 * @param times Number of iterations
	 * @return result
	 */
	public static Image exec(Image input, BooleanImage se, int flag,int times)
	{
		return (Image ) new GrayASFByReconstruction().process(input,se,flag,times);
	}

}
