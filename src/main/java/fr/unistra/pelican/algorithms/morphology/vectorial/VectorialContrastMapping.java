package fr.unistra.pelican.algorithms.morphology.vectorial;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.util.Tools;
import fr.unistra.pelican.util.vectorial.orders.VectorialOrdering;

/**
 * This class performs a vectorial contrast mapping.
 * 
 * @author Abdullah
 * 
 */
public class VectorialContrastMapping extends Algorithm
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
	 * the vector ordering
	 */
	public VectorialOrdering vo;

	/**
	 * the output image
	 */
	public Image output;
	
	/**
	 * This method performs a vectorial contrast mapping.
	 * @param input the input image
	 * @param se the structuring element
	 * @param vo teh vector ordering
	 * @return the output image
	 */
	public static Image exec(Image input,BooleanImage se,VectorialOrdering vo) {
		return (Image) new VectorialContrastMapping().process(input,se,vo);
	}

	/**
	 * Constructor
	 * 
	 */
	public VectorialContrastMapping() {

		super();
		super.inputs = "input,se,vo";
		super.outputs = "output";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		output = input.copyImage(false);

		try {
			Image dilated = (Image) new VectorialDilation().process(input, se, vo);
			Image eroded = (Image) new VectorialErosion().process(input, se, vo);

			for (int x = 0; x < input.getXDim(); x++) {
				for (int y = 0; y < input.getYDim(); y++) {

					if ( !input.isPresentXYZT( x,y,0,0 ) ) continue;

					double[] d = dilated.getVectorPixelXYZTDouble(x, y, 0, 0);
					double[] e = eroded.getVectorPixelXYZTDouble(x, y, 0, 0);
					double[] o = input.getVectorPixelXYZTDouble(x, y, 0, 0);

					if ( Tools.euclideanNorm(Tools.VectorDifference(o, e)) 
							< Tools.euclideanNorm(Tools.VectorDifference(o, d)))
						output.setVectorPixelXYZTDouble(x, y, 0, 0, e);
					else
						output.setVectorPixelXYZTDouble(x, y, 0, 0, d);
				}
			}

		} catch (PelicanException e) {
			e.printStackTrace();
		}
	}
}
