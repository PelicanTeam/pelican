package fr.unistra.pelican.algorithms.io;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.InvalidNumberOfParametersException;
import fr.unistra.pelican.InvalidTypeOfParameterException;
import fr.unistra.pelican.gui.Draw2D;

/**
 * The ImageBuilder class allows the user to draw markers with or without a
 * background image calling fr.unistra.pelican.gui.Draw2D
 * 
 * @author Florent Sollier, Jonathan Weber
 * 
 */
public class ImageBuilder extends Algorithm {

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
	public ByteImage output;

	/**
	 * An estimation of the number of labels
	 */
	public int labels;
	
	/**
	 * Background image
	 */
	public Image inputImage;
	
	/**
	 * predefined Markers image
	 */
	public ByteImage predefMarkers=null;

	/**
	 * This boolean informs if there is any background image or not
	 */
	private static boolean background;

	/**
	 * Name of the background image file
	 */
	public String filename=null;

	/**
	 * reference to the Draw2D object
	 */
	private Draw2D d2d;

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
	public ImageBuilder() {
		super.inputs = "inputImage";
		super.options ="filename,predefMarkers";
		super.outputs = "output,labels";

	}

	/**
	 * Method which launch the algorithm
	 */
	public void launch() throws AlgorithmException {

		if(predefMarkers==null)
			d2d = new Draw2D(inputImage, filename);
		else
			d2d = new Draw2D(inputImage, filename, predefMarkers);
		labels=d2d.labels();
		output = d2d.output;

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
	public static ByteImage exec(int sizeX, int sizeY, String filename) {
		return (ByteImage) new ImageBuilder().process(new ByteImage(sizeX, sizeY,
				1, 1, 3), filename);
	}

	public static ByteImage exec(int sizeX, int sizeY) {
		return (ByteImage) new ImageBuilder().process(new ByteImage(sizeX, sizeY,
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
	public static ByteImage exec(Image inputImage, String filename) {
		return (ByteImage) new ImageBuilder().process(inputImage, filename);
	}

	public static ByteImage exec(Image inputImage) {
		return (ByteImage) new ImageBuilder().process(inputImage);
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
	public static ByteImage exec(Image inputImage, String filename, ByteImage markers) {
		return (ByteImage) new ImageBuilder().process(inputImage, filename, markers);
	}
}