package fr.unistra.pelican.algorithms.morphology.vectorial;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.util.vectorial.orders.VectorialOrdering;

/**
 * This class performs a vectorial OCCO (mean between an opening the closing and a closing
 * followed by an opening with the same structuring element. Works on a double
 * precision.
 * 
 * @author Abdullah
 */
public class VectorialOCCO extends Algorithm
{
	/**
	 * The input image
	 */
	public Image inputImage;

	/**
	 * the structuring element
	 */
	public BooleanImage se;

	/**
	 * the vectorial ordering
	 */
	public VectorialOrdering vo;

	/**
	 * the output image
	 */
	public Image outputImage;
	
	/**
	 * This class performs a vectorial OCCO (mean between an opening the closing and a closing followed by an opening with the same structuring element. Works on a double precision.
	 * @param image the input image
	 * @param se the structuring element
	 * @param vo the vectorial ordering
	 * @return the output image
	 */
	public static Image exec(Image inputImage, BooleanImage se,VectorialOrdering vo) {
		return (Image) new VectorialOCCO().process(inputImage,se,vo);
	}

	/**
	 * Constructor
	 * 
	 */
	public VectorialOCCO() {

		super();
		super.inputs = "inputImage,se,vo";
		super.outputs = "outputImage";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		try {
			Image tmp;

			// opening then closing
			outputImage = (Image) new VectorialOpening().process(inputImage, se, vo);
			outputImage = (Image) new VectorialClosing().process(outputImage, se, vo);

			// closing then opening
			tmp = (Image) new VectorialClosing().process(inputImage, se, vo);
			tmp = (Image) new VectorialOpening().process(tmp, se, vo);

			// Merge by mean.
			int size = inputImage.size();

			for (int i = 0; i < size; i++) {

				if ( !inputImage.isPresent(i) ) continue;

				double p1 = outputImage.getPixelDouble(i);
				double p2 = tmp.getPixelDouble(i);

				outputImage.setPixelDouble(i, (p1 + p2) / 2.0);
			}
		} catch (PelicanException e) {
			e.printStackTrace();
		}
	}

}