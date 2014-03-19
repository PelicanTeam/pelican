package fr.unistra.pelican.algorithms.morphology.vectorial.geodesic;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.arithmetic.Inversion;
import fr.unistra.pelican.algorithms.morphology.vectorial.VectorialErosion;
import fr.unistra.pelican.util.vectorial.orders.VectorialOrdering;

/**
 * This class performs a vectorial closing by reconstruction with the given structuring
 * element, mask and vectorial ordering. Works on double precision.
 * 
 * @author Abdullah
 */
public class VectorialClosingByReconstruction extends Algorithm
{
	/**
	 * the input image
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
	 * the size of the reconstruction, in other words, how many times should the input be dilated before reconstruction
	 */
	public int size;

	/**
	 * the output image
	 */
	public Image outputImage;
	
	/**
	 * This class performs a vectorial closing by reconstruction with the given structuring element, mask and vectorial ordering. Works on double precision.
	 * 
	 * @param image the input image
	 * @param se the structuring element
	 * @param vo the vectorial ordering
	 * @param size the size of reconstruction
	 * @return the vectorially closed by reconstruction image
	 */
	public static Image exec(Image image, BooleanImage se,VectorialOrdering vo,Integer size) {
		return (Image) new VectorialClosingByReconstruction().process(image,se,vo,size);
	}

	/**
	 * Constructor
	 * 
	 */
	public VectorialClosingByReconstruction() {

		super();
		super.inputs = "inputImage,se,vo,size";
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

			Image tmp=Inversion.exec(inputImage);
			outputImage = tmp.copyImage(true);
			
			for (int i = 0; i < size; i++)
				outputImage = (Image) new VectorialErosion().process(outputImage, se, vo);

			outputImage = (Image) new FastVectorialReconstruction().process(outputImage,
					tmp, vo);
			outputImage=Inversion.exec(outputImage);
		} catch (PelicanException e) {
			e.printStackTrace();
		}
	}

}
