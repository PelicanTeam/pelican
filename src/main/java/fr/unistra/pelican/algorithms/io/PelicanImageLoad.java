package fr.unistra.pelican.algorithms.io;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.zip.GZIPInputStream;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;

/**
 * Loads images in PELICAN binary format (with serialization and optionally
 * compression).
 * 
 * @author Lefevre
 */
public class PelicanImageLoad extends Algorithm {

	/**
	 * Filename of the image
	 */
	public String filename;

	/**
	 * (optional) compression flag
	 */
	public boolean compression = true;

	/**
	 * Image to load
	 */
	public Image output;

	/**
	 * Constructor
	 * 
	 */
	public PelicanImageLoad() {
		super.inputs = "filename";
		super.options = "compression";
		super.outputs = "output";
	}

	public void launch() throws AlgorithmException {
		// Load the image.
		try {
			ObjectInputStream f = null;
			if (compression)
				f = new ObjectInputStream(new GZIPInputStream(new FileInputStream(
					filename)));
			else
				f = new ObjectInputStream(new FileInputStream(filename));
			output = (Image) f.readObject();
			f.close();
		} catch (IOException ex) {
			throw new AlgorithmException("file reading error with file: " + filename,ex);
		} catch (ClassNotFoundException ex) {
			throw new AlgorithmException("file reading error with file: " + filename,ex);
		}

	}

	/**
	 * Loads images in PELICAN binary format (with serialization and compression).
	 * 
	 * @param path
	 *          Directory of the image to be loaded.
	 * @return Image in PELICAN binary format.
	 */
	public static Image exec(String filename) {
		return (Image) new PelicanImageLoad().process(filename);
	}

	/**
	 * Loads images in PELICAN binary format (with serialization and optionally
	 * compression).
	 * 
	 * @param path
	 *          Directory of the image to be loaded.
	 * @param compression
	 *          Flag to enable or disable compression
	 * @return Image in PELICAN binary format.
	 */
	public static Image exec(String filename, boolean compression) {
		return (Image) new PelicanImageLoad().process(filename, compression);
	}

}
