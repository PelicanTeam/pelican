package fr.unistra.pelican.algorithms.morphology.vectorial;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.util.vectorial.orders.BinaryVectorialOrdering;

/**
 * This class performs a vectorial morphological center operation.
 * 
 * @LUC (least used class) award 2007
 * 
 * @author Abdullah
 * 
 */
public class VectorialCenter extends Algorithm
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
	public BinaryVectorialOrdering vo;

	/**
	 * the output image
	 */
	public Image output;
	
	/**
	 * This method performs a vectorial morphological center.
	 * @param input the input image
	 * @param se the structuring element
	 * @param vo the vector ordering
	 * @return the output image 
	 */
	public static Image exec(Image input,BooleanImage se,BinaryVectorialOrdering vo) {
		return (Image) new VectorialCenter().process(input,se,vo);
	}

	/**
	 * Constructor
	 * 
	 */
	public VectorialCenter() {

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
			Image gfg = (Image) new VectorialOpening().process(input, se, vo);
			gfg = (Image) new VectorialClosing().process(gfg, se, vo);
			gfg = (Image) new VectorialOpening().process(gfg, se, vo);

			Image fgf = (Image) new VectorialClosing().process(input, se, vo);
			fgf = (Image) new VectorialOpening().process(fgf, se, vo);
			fgf = (Image) new VectorialClosing().process(fgf, se, vo);

			for (int x = 0; x < input.getXDim(); x++) {
				for (int y = 0; y < input.getYDim(); y++) {

					if ( !input.isPresentXYZT( x,y,0,0 ) ) continue;

					double[] d = gfg.getVectorPixelXYZTDouble(x, y, 0, 0);
					double[] e = fgf.getVectorPixelXYZTDouble(x, y, 0, 0);
					double[] o = input.getVectorPixelXYZTDouble(x, y, 0, 0);

					double[] s = vo.min(vo.max(o, vo.min(d, e)), vo.max(d, e));
					output.setVectorPixelXYZTDouble(x, y, 0, 0, s);
				}
			}
		} catch (PelicanException e) {
			e.printStackTrace();
		}
	}
}
