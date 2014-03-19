package fr.unistra.pelican.algorithms.io;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.remotesensing.HdrReader;

/**
 * Loads hdr images used in remote sensing (ByteImage).
 * 
 * @author
 */

public class HdrImageLoad extends Algorithm {
	
	/**
	 * Input parameter
	 */
	public String filename;

	/**
	 * Output parameter
	 */
	public Image output;

	/**
	 * Constructor
	 * 
	 */
	public HdrImageLoad() {

		super();
		super.inputs = "filename";
		super.outputs = "output";
		
	}

	public void launch() {
		// Load the image.
		// RawImage source = PicTool.load(filename);

		// output = source.getPelicanImage();
		output = new HdrReader().getPelicanImage(filename); // TODO utiliser
															// plus tard une
															// méthode statique

	}
	/**
	 *  Loads hdr images used in remote sensing (ByteImage).
	 * 
	 * @param filename Filename of the hrd image.
	 * @return The hdr image.
	 */
	public static Image exec(String filename) {
		return (Image) new HdrImageLoad().process(filename);
	}
}