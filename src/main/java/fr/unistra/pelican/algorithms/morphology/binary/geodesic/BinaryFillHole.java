package fr.unistra.pelican.algorithms.morphology.binary.geodesic;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.arithmetic.Inversion;
import fr.unistra.pelican.algorithms.segmentation.flatzones.BooleanConnectedComponentsLabeling;


/**
 * This class fills hole in the image
 * 
 * @author ?, Jonathan Weber
 */
public class BinaryFillHole extends Algorithm {
	
	/**
	 * Constant for 4-connexity
	 */
	public static int CONNEXITY4 = BooleanConnectedComponentsLabeling.CONNEXITY4;

	/**
	 * Constant for 8-connexity
	 */
	public static int CONNEXITY8 = BooleanConnectedComponentsLabeling.CONNEXITY8;

	/**
	 * Image to be processed
	 */
	public Image inputImage;

	/**
	 * Result of the processing
	 */
	public Image outputImage;

	/**
	 * Choosen connexity
	 */
	public int connexity=CONNEXITY8;

	/**
	 * Constructor
	 * 
	 */
	public BinaryFillHole() {
		super.inputs = "inputImage";
		super.options="connexity";
		super.outputs = "outputImage";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		try {
			int xDim = inputImage.getXDim();
			int yDim = inputImage.getYDim();

			// create marker image
			BooleanImage marker = new BooleanImage( inputImage, false );

			for (int x = 0; x < xDim; x++) {
				for (int y = 0; y < yDim; y++) {
					boolean p = inputImage.getPixelXYBoolean(x, y);

					if (x == xDim - 1 || y == yDim - 1 || x == 0 || y == 0)
						marker.setPixelXYBoolean(x, y, p);
					else
						marker.setPixelXYBoolean(x, y, true);
				}
			}
			outputImage = Inversion.exec( 
					FastBinaryReconstruction.exec(
							Inversion.exec(marker), Inversion.exec(inputImage), connexity ) );
		} catch (PelicanException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method fills all the hole in the image
	 * @param Image to process
	 * @param connexity
	 * @return Resulting image of the binary fill hole processing
	 */
	public static Image exec(Image inputImage,int connexity)
	{
		return (Image) new BinaryFillHole().process(inputImage,connexity);
	}

	public static Image exec(Image inputImage)
	{
		return (Image) new BinaryFillHole().process(inputImage);
	}	
	
}
