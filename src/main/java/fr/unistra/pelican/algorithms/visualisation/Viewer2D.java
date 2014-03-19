package fr.unistra.pelican.algorithms.visualisation;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.InvalidNumberOfParametersException;
import fr.unistra.pelican.InvalidTypeOfParameterException;
import fr.unistra.pelican.gui.Frame2D;

/**
 * Viewer2D is responsible for the visualisation of all 2 dimensional images
 * that is with no special 3d requirements. According to the Z,T and B
 * dimensions of the image in question the resulting window will include the
 * corresponding sliders. Only the images with the color flag set and with
 * exactly 3 channels will be visualised in color. Non byte valued images will
 * be reduced to bytes.
 * 
 * @author Erchan Aptoula
 */
public class Viewer2D extends Algorithm {
	
	/**
	 * Input parameter.
	 */
	public Image input;

	/**
	 * Optional input parameter.
	 */
	public String title;

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		new Frame2D(input, title, input.isColor());
	}

	/**
	 * Constructor
	 * 
	 */
	public Viewer2D() {

		super();
		super.inputs = "input";
		super.options="title";
		super.outputs = "";
		

	}

	/**
	 * Visualisation of all 2 dimensional images
	 * 
	 * @param image Image to be viewed.
	 * @throws InvalidTypeOfParameterException
	 * @throws AlgorithmException
	 * @throws InvalidNumberOfParametersException
	 */
	public static void exec(Image input)
			throws InvalidTypeOfParameterException, AlgorithmException,
			InvalidNumberOfParametersException {
		new Viewer2D().process(input);
	}
	
	/**
	 * Visualisation of all 2 dimensional images
	 * 
	 * @param image Image to be viewed.
	 * @param label Title of the image.
	 * @throws InvalidTypeOfParameterException
	 * @throws AlgorithmException
	 * @throws InvalidNumberOfParametersException
	 */
	public static void exec(Image input, String label)
		throws InvalidTypeOfParameterException, AlgorithmException,
		InvalidNumberOfParametersException {
		new Viewer2D().process(input, label);
	}

}