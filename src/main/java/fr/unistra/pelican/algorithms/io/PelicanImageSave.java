package fr.unistra.pelican.algorithms.io;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPOutputStream;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;

/**
 * Saves images in PELICAN binary format (with serialization and optionally
 * compression).
 * 
 * @author Lefevre
 */
public class PelicanImageSave extends Algorithm {

	/**
	 * Image to be saved
	 */
	public Image input;

	/**
	 * Filename of the image
	 */
	public String filename;

	/**
	 * (optional) compression flag
	 */
	public boolean compression = true;

	/**
	 * Constructor
	 * 
	 */
	public PelicanImageSave() {
		super.inputs = "input,filename";
		super.options = "compression";
		super.outputs = "";

	}

	public void launch() throws AlgorithmException {
		// Save the image.
		try {
			ObjectOutputStream f = null;
			if (compression)
				f = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(
					filename)));
			else
				f = new ObjectOutputStream(new FileOutputStream(filename));
			f.writeObject(input);
			f.close();			
		} catch (IOException ex) {
			System.err.println(ex.getMessage());
			throw new AlgorithmException("file writing error with file: " + filename);
		}

	}

	/**
	 * Saves images in PELICAN binary format (with serialization and compression)
	 * 
	 * @param input
	 *          Image to be saved
	 * @param filename
	 *          Filename of the image
	 */
	public static void exec(Image input, String filename) {
		new PelicanImageSave().process(input, filename);
	}

	/**
	 * Saves images in PELICAN binary format (with serialization and optionally
	 * compression).
	 * 
	 * @param input
	 *          Image to be saved
	 * @param filename
	 *          Filename of the image
	 * @param compression
	 *          Flag to enable or disable compression
	 */
	public static void exec(Image input, String filename, boolean compression) {
		new PelicanImageSave().process(input, filename, compression);
	}

}
