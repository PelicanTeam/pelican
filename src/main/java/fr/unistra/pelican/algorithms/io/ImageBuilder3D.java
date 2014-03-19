package fr.unistra.pelican.algorithms.io;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.InvalidNumberOfParametersException;
import fr.unistra.pelican.InvalidTypeOfParameterException;
import fr.unistra.pelican.gui.Draw3D;

/**
 * The ImageBuilder class allows the user to draw 3D markers with a background image 
 */
public class ImageBuilder3D extends Algorithm {

	/***************************************************************************
	 * 
	 * 
	 * Attributes
	 * 
	 * 
	 **************************************************************************/

	/**
	 * The marker image
	 */
	public IntegerImage output;

	/**
	 * An estimation of the number of labels
	 */
	public int labels;
	
	/**
	 * Background image
	 */
	public Image inputImage;
	
	/**
	 * Predefined Markers image
	 */
	public IntegerImage predefMarkers=null;

	/**
	 * Name of the background image file
	 */
	public String filename=null;

	/**
	 * reference to the DrawVideo object
	 */
	private Draw3D im3DBuild;

	/***************************************************************************
	 * 
	 * 
	 * Methods
	 * 
	 * 
	 **************************************************************************/

	/**
	 * Constructor
	 * 
	 */
	public ImageBuilder3D() {
		super.inputs = "inputImage";
		super.options ="filename,predefMarkers";
		super.outputs = "output,labels";

	}

	/**
	 * Method which launch the algorithm
	 */
	public void launch() throws AlgorithmException {

		if(predefMarkers==null)
			im3DBuild = new Draw3D(inputImage, filename);
		else
			im3DBuild = new Draw3D(inputImage, filename, predefMarkers);
		labels=im3DBuild.labels();
		output = im3DBuild.output;

	}

	/**
	 * Exec without a background image
	 * 
	 * @param sizeX
	 *            Width of the marker image
	 * @param sizeY
	 *            Height of the marker image
	 * @param filename
	 *            name of the image to be treated.
	 * @return the marker image
	 * @throws InvalidTypeOfParameterException
	 * @throws AlgorithmException
	 * @throws InvalidNumberOfParametersException
	 */
	public static IntegerImage exec(int sizeX, int sizeY, String filename) {
		return (IntegerImage) new ImageBuilder3D().process(new IntegerImage(sizeX, sizeY,
				1, 1, 3), filename);
	}

	public static IntegerImage exec(int sizeX, int sizeY) {
		return (IntegerImage) new ImageBuilder3D().process(new IntegerImage(sizeX, sizeY,
				1, 1, 3));
	}

	/**
	 * Exec with a background image
	 * 
	 * @param inputImage
	 *            Image to be treated.
	 * @param filename
	 *            name of the image to be treated.
	 * @return the marker image
	 * @throws InvalidTypeOfParameterException
	 * @throws AlgorithmException
	 * @throws InvalidNumberOfParametersException
	 */
	public static IntegerImage exec(Image inputImage, String filename) {
		return (IntegerImage) new ImageBuilder3D().process(inputImage, filename);
	}

	public static IntegerImage exec(Image inputImage) {
		return (IntegerImage) new ImageBuilder3D().process(inputImage);
	}

	/**
	 * Exec with a background image and a predefined marker image
	 * 
	 * @param inputImage
	 *            Image to be treated.
	 * @param filename
	 *            name of the image to be treated.
	 * @param markers
	 * 			  predefined marker image.
	 * @return the marker image
	 * @throws InvalidTypeOfParameterException
	 * @throws AlgorithmException
	 * @throws InvalidNumberOfParametersException
	 */
	public static IntegerImage exec(Image inputImage, String filename, IntegerImage markers) {
		return (IntegerImage) new ImageBuilder3D().process(inputImage, filename, markers);
	}
}