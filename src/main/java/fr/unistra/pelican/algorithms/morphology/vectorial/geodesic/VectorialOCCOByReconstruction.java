package fr.unistra.pelican.algorithms.morphology.vectorial.geodesic;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.util.vectorial.orders.VectorialOrdering;

/**
 * This class performs a vectorial OCCO by reconstruction. Attention to the averaging step.
 * It does not preserve the original vectors.
 * 
 * @author Abdullah
 */
public class VectorialOCCOByReconstruction extends Algorithm
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
	 * The vector ordering
	 */
	public VectorialOrdering vo;

	/**
	 * the size of reconstructions
	 */
	public int size;

	/**
	 * the output image
	 */
	public Image outputImage;
	
	/**
	 * This method performs a vectorial OCCO by reconstruction.
	 * 
	 * @param image the input image
	 * @param se the structuring element
	 * @param vo the vectorial ordering
	 * @param size the size of reconstruction
	 * @return the output image
	 */
	public static Image exec(Image image, BooleanImage se,VectorialOrdering vo,Integer size) {
		return (Image) new VectorialOCCOByReconstruction().process(image,se,vo,size);
	}

	/**
	 * Constructor
	 * 
	 */
	public VectorialOCCOByReconstruction() {

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
			outputImage = (Image) 
				new VectorialOpeningByReconstruction().process( inputImage, se, vo, size );
			outputImage = (Image) 
				new VectorialClosingByReconstruction().process( outputImage, se, vo, size );

			// closing then opening
			Image tmp = (Image) 
				new VectorialClosingByReconstruction().process( inputImage, se, vo, size );
			tmp = (Image) new VectorialOpeningByReconstruction().process( tmp, se, vo, size );

			// Merge by mean.
			int size = inputImage.size();

			for (int i = 0; i < size; i++) { 

				if ( !inputImage.isPresent(i) ) continue;

				double p1 = outputImage.getPixelDouble(i);
				double p2 = tmp.getPixelDouble(i);

				outputImage.setPixelDouble( i, ( p1+p2 ) / 2.0 );
			}
		} catch ( PelicanException e ) { e.printStackTrace(); }

	}

}
