package fr.unistra.pelican.algorithms.visualisation;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.gui.Frame3x2D;

/**
 * ThreeDimensional(x,y,z) images are shown in 3 displays : face view (XY),left
 * view (ZY) and bottom view (XZ).
 * 
 * @author Matthieu Sablier
 */
public class Viewer3x2D extends Algorithm {

	/**
	 * First input parameter.
	 */
	public Image input;

	/**
	 * Second input parameter.
	 */
	public String title="";

	/**
	 * Constructor
	 * 
	 */
	public Viewer3x2D() {
		super.inputs = "input";
		super.options="title";
		super.outputs = "";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		new Frame3x2D(input, title, input.isColor());
	}

	/**
	 * ThreeDimensional(x,y,z) images are shown in 3 displays : face view
	 * (XY),left view (ZY) and bottom view (XZ).
	 * 
	 * @param input Image to be viewed.
	 * @param title Title of the image.
	 */
	public static void exec(Image input, String title) {
		new Viewer3x2D().process(input, title);
	}

	public static void exec(Image input) {
		new Viewer3x2D().process(input);
	}

}