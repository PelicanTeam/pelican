package fr.unistra.pelican.algorithms.morphology.gray;

import java.awt.Point;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 * This class performs alternating sequential filters (ASF).
 * 
 * @author Erchan Aptoula
 */
public class GrayASF extends Algorithm {

	/**
	 * The input image
	 */
	public Image input;

	/**
	 * The flat structuring element used in the morphological operation
	 */
	public BooleanImage se;

	/**
	 * The type of ASF : OPENING_FIRST or CLOSING_FIRST
	 */
	public int flag;

	/**
	 * The number of iterations
	 */
	public int times;

	/**
	 * The output image
	 */
	public Image output;

	/**
	 * Constant representing ASF with opening as first operation
	 */
	public static final int OPENING_FIRST = 0;

	/**
	 * Constant representing ASF with closing as first operation
	 */
	public static final int CLOSING_FIRST = 1;

	/**
	 * Default constructor
	 */
	public GrayASF() {
		super.inputs = "input,se,flag,times";
		super.outputs = "output";
		
	}

	/**
	 * Performs alternating sequential filters (ASF)
	 * 
	 * @param input
	 *            The input image
	 * @param se
	 *            The flat structuring element used in the morphological
	 *            operation
	 * @param flag
	 *            The type of ASF : OPENING_FIRST or CLOSING_FIRST
	 * @param times
	 *            The number of iterations
	 * @return The output image
	 */
	public static Image exec( Image input, BooleanImage se, int flag, int times ) {
		return (Image) new GrayASF().process(input, se, flag, times);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() { 

		this.output = this.input.copyImage(true);
		if ( this.flag != OPENING_FIRST && this.flag != CLOSING_FIRST )
			throw new AlgorithmException( "Invalid flag" );
		if ( this.times < 1 ) 
			throw new AlgorithmException( "The number of iterations should be at least one" );

		if ( isRectangle( this.se ) ) {

			for ( int i = 0; i < this.times; i++ ) { 

				if ( this.flag == OPENING_FIRST ) { 

					this.output = GrayOpening.exec( this.output,this.se );
					this.output = GrayClosing.exec( this.output,this.se );
				} else { 

					this.output = GrayClosing.exec( this.output,this.se );
					this.output = GrayOpening.exec( this.output,this.se );
				}
				this.se = new BooleanImage( this.se.getXDim()+2,this.se.getYDim()+2,1,1,1 );
			}
		} else { 

			// the SE used to dilate the given SE
			BooleanImage magnifier = FlatStructuringElement2D.createSquareFlatStructuringElement(3);
			// prepare the SE so that it can take the dilation results...
			BooleanImage tmp = new BooleanImage( se.getXDim()+2*times,se.getYDim()+2*times,1,1,1);
			se.setCenter(new Point( se.getCenter().x + times, se.getCenter().y + times));
			tmp.fill(false);
			for ( int x = 0 ; x < se.getXDim() ; x++ ) 
				for ( int y = 0 ; y < se.getYDim() ; y++ ) 
					tmp.setPixelXYBoolean( x+times, y+times, this.se.getPixelXYBoolean(x,y) );
			se = tmp;
			for (int i = 0; i < times; i++) {
				if (flag == OPENING_FIRST) {
					output = GrayOpening.exec(output, se);
					output = GrayClosing.exec(output, se);
				} else {
					output = GrayClosing.exec(output, se);
					output = GrayOpening.exec(output, se);
				}
				se = (BooleanImage) GrayDilation.exec(se, magnifier);
		}
		}
	}

	private static boolean isRectangle( BooleanImage se ) {

		return se.getSum() == se.size();
	}

}
