package fr.unistra.pelican.algorithms.io;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.JMFVideo;

/**
 * Experimental video Opener (works only the AVI format, for further detail
 * please contact the author).
 * It allows to choice how many frames will be store in the stack
 * 
 */

public class JMFVideoOpener extends Algorithm {

	/**
	 * Input image.
	 */
	public String filename;

	/**
	 * Output image.
	 */
	public JMFVideo outputImage;
	
	/**
	 * Constructor
	 * 
	 */
	public JMFVideoOpener() {

		super();
		super.inputs = "filename";
		super.outputs = "outputImage";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		
		outputImage = new JMFVideo(filename);
	}

	/**
	 * Experimental video opener.
	 * 
	 * @param filename
	 *            Directory of the video
	 * @return the loaded video
	 */
	public static JMFVideo exec(String filename) {
		return (JMFVideo) new JMFVideoOpener().process(filename);
	}	
}
