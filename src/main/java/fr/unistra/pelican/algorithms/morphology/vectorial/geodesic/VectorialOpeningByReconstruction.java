package fr.unistra.pelican.algorithms.morphology.vectorial.geodesic;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.morphology.vectorial.VectorialErosion;
import fr.unistra.pelican.util.vectorial.orders.VectorialOrdering;

/**
 * This class performs a vectorial opening by reconstruction with the given structuring
 * element, mask and vectorial ordering. Works on double precision.
 * 
 * @author Abdullah
 */
public class VectorialOpeningByReconstruction extends Algorithm
{
	/**
	 * The input image
	 */
	public Image inputImage;

	/**
	 * The structuring element
	 */
	public BooleanImage se;

	/**
	 * The vectorial ordering
	 */
	public VectorialOrdering vo;

	/**
	 * the size of reconstruction
	 */
	public int size=1;

	/**
	 * the output image
	 */
	public Image outputImage;
	
	/**
	 * This class performs a vectorial opening by reconstruction with the given structuring element, mask and vectorial ordering. Works on double precision.
	 * 
	 * @param image the input image
	 * @param se the structuring element
	 * @param vo the vectorial ordering
	 * @return the vectorially opened by reconstruction image
	 */
	public static Image exec(Image inputImage, BooleanImage se,VectorialOrdering vo) {
		return (Image) new VectorialOpeningByReconstruction().process(inputImage,se,vo);
	}

	/**
	 * This class performs a vectorial opening by reconstruction with the given structuring element, mask and vectorial ordering. Works on double precision.
	 * 
	 * @param image the input image
	 * @param se the structuring element
	 * @param vo the vectorial ordering
	 * @param size the size of reconstruction
	 * @return the vectorially opened by reconstruction image
	 */
	public static Image exec(Image inputImage, BooleanImage se,VectorialOrdering vo,Integer size) {
		return (Image) new VectorialOpeningByReconstruction().process(inputImage,se,vo,size);
	}

	/**
	 * Constructor
	 * 
	 */
	public VectorialOpeningByReconstruction() {
		super.inputs = "inputImage,se,vo";
		super.options="size";
		super.outputs = "outputImage";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		try {
			if (size <= 0)
				throw new AlgorithmException("Size argument must be at least 1");

			outputImage = inputImage;

			for (int i = 0; i < size; i++)
				outputImage = (Image) new VectorialErosion().process(outputImage, se, vo);

			outputImage = (Image) new FastVectorialReconstruction().process(
					outputImage, inputImage, vo );

		} catch (PelicanException e) { e.printStackTrace(); }

	}

}
