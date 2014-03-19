package fr.unistra.pelican.algorithms.morphology.vectorial;

import java.awt.Point;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.morphology.gray.GrayDilation;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;
import fr.unistra.pelican.util.vectorial.orders.VectorialOrdering;

/**
 * This class represents the alternating sequential filters. An alternating sequence of openings and closings
 * with monotonically varying structuring element sizes.
 * 
 * @author Abdullah
 * 
 */
public class VectorialASF extends Algorithm
{
	/**
	 * the input image
	 */
	public Image input;

	/**
	 * the structuring element
	 */
	public BooleanImage se;

	/**
	 * the order of operations, OPENING_FIRST or CLOSING_FIRST
	 */
	public int flag;

	/**
	 * how many times to apply each couple of operators.
	 */
	public int times;

	/**
	 * the vector ordering
	 */
	public VectorialOrdering vo;

	/**
	 * flag indicating to use openings first
	 */
	public static final int OPENING_FIRST = 0;

	/**
	 * flag indicating to use closings first
	 */
	public static final int CLOSING_FIRST = 1;

	/**
	 * the output image
	 */
	public Image output;
	
	/**
	 * This method applies an alternating sequential filter on its input
	 * @param image the input image
	 * @param se the structuring element
	 * @param flag a flag indicating which operator to use first
	 * @param times how many times to apply each operator couple
	 * @param vo the vector ordering
	 * @return the output image
	 */
	public static Image exec(Image input,BooleanImage se,Integer flag,Integer times,VectorialOrdering vo)
	{
		return (Image) new VectorialASF().process(input,se,flag,times,vo);
	}

	/**
	 * Constructor
	 * 
	 */
	public VectorialASF() {

		super();
		super.inputs = "input,se,flag,times,vo";
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
			BooleanImage tmp = new BooleanImage( se.getXDim() + 2 * times, 
												 se.getYDim() + 2 * times,
												 1,1,1 );
			tmp.setCenter( new Point( se.getCenter().x + times, se.getCenter().y + times ) );
			tmp.fill(false);

			for (int x = 0; x < se.getXDim(); x++)
				for (int y = 0; y < se.getYDim(); y++)
					tmp.setPixelXYBoolean(x + times, y + times, true);
			se = tmp;

			output = input;

			for (int i = 0; i < times; i++) {
				if (flag == OPENING_FIRST) {
					output = VectorialOpening.exec(output, se, vo);
					output = VectorialClosing.exec(output, se, vo);
				} else {
					output = VectorialClosing.exec(output, se, vo);
					output = VectorialOpening.exec(output, se, vo);
				}
				se = (BooleanImage) GrayDilation.exec(se,magnifier);
			}

		} catch (PelicanException e) {
			e.printStackTrace();
		}
	}

}
