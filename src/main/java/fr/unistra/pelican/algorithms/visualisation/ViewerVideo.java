package fr.unistra.pelican.algorithms.visualisation;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.InvalidNumberOfParametersException;
import fr.unistra.pelican.InvalidTypeOfParameterException;
import fr.unistra.pelican.gui.FrameVideo;

/**
 * ViewerVideo is responsible for the visualization of video in motion
 * 
 * @author Jonathan Weber
 */
public class ViewerVideo extends Algorithm {
	
	/**
	 * Input parameter.
	 */
	public Image input;

	/**
	 * Optional input parameter.
	 */
	public String title="Pelican Video Viewer";
	public double frameRate=25;
	public boolean onLoop=true;

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		if(input.getTDim()>1)
			new FrameVideo(input, title, input.isColor(),frameRate,onLoop);
		else
			Viewer2D.exec(input,title);
	}

	/**
	 * Constructor
	 * 
	 */
	public ViewerVideo() {

		super();
		super.inputs = "input";
		super.options="title,frameRate,onLoop";
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
		new ViewerVideo().process(input);
	}
	
	/**
	 * Visualisation of video
	 * 
	 * @param image Image to be viewed.
	 * @param label Title of the image.
	 * @param rate of video frames per second
	 * @param set if the video looped
	 * @throws InvalidTypeOfParameterException
	 * @throws AlgorithmException
	 * @throws InvalidNumberOfParametersException
	 */
	public static void exec(Image input, String label, double frameRate,boolean onLoop)
		throws InvalidTypeOfParameterException, AlgorithmException,
		InvalidNumberOfParametersException {
		new ViewerVideo().process(input, label, frameRate,onLoop);
	}

	/**
	 * Visualisation of video
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
		new ViewerVideo().process(input, label);
	}
}