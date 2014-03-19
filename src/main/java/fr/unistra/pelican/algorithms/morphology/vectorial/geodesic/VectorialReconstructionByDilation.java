package fr.unistra.pelican.algorithms.morphology.vectorial.geodesic;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.arithmetic.Equal;
import fr.unistra.pelican.util.vectorial.orders.VectorialOrdering;

/**
 * This class performs a vectorial reconstruction by dilation with the given structuring
 * element, mask and vectorial ordering. Works on double precision. Output
 * format is the same as mask.
 * 
 * @author Abdullah
 */
public class VectorialReconstructionByDilation extends Algorithm
{
	/**
	 * the input image
	 */
	public Image inputImage;

	/**
	 * the mask
	 */
	public Image mask;

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
	public Image outputImage;
	
	/**
	 * This class performs a vectorial reconstruction by dilation with the given structuring element, mask and vectorial ordering.
	 * 
	 * @param image the input image
	 * @param mask the mask
	 * @param se the structuring element
	 * @param vo the vectorial ordering
	 * @return the output image
	 */
	public static Image exec(Image image, Image mask,BooleanImage se,VectorialOrdering vo) {
		return (Image) new VectorialReconstructionByDilation().process(image,mask,se,vo);
	}

	/**
	 * Constructor
	 * 
	 */
	public VectorialReconstructionByDilation() {

		super();
		super.inputs = "inputImage,mask,se,vo";
		super.outputs = "outputImage";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException { 

		Image tmp = inputImage.copyImage(true);
		try {

			do {
				outputImage = tmp;
				tmp = (Image) new VectorialGeodesicDilation().process( outputImage, mask, se, vo );
			} while (!(Boolean)new Equal().process(outputImage, tmp));
		} catch (PelicanException e) {
			e.printStackTrace();
		}
	}

}
