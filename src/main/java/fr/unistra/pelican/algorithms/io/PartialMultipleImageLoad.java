package fr.unistra.pelican.algorithms.io;

import java.io.File;
import java.util.Arrays;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;

/**
 * Partial multiple image loading in a single directory.
 * 
 * @author
 * 
 */
public class PartialMultipleImageLoad extends Algorithm {

	// Verbose mode
	boolean verbose = true;

	/**
	 * First input parameter
	 */
	public String filename;

	/**
	 * Second input parameter
	 */
	public int start;

	/**
	 * Third input parameter
	 */
	public int duration;

	/**
	 * Output parameter
	 */
	public Image output;

	/**
	 * Constructor
	 * 
	 */
	public PartialMultipleImageLoad() {

		super();
		super.inputs = "filename,start,duration";
		super.outputs = "output";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		// check if the filename is a directory
		File dir = new File(filename);
		if (!dir.isDirectory())
			throw new AlgorithmException(
					"the input filename is not a directory");
		File tab[] = dir.listFiles();
		Arrays.sort(tab);
		Image img;
		// process first image
		try {
			img = (Image) new ImageLoader().process(tab[start].getPath());
		} catch (PelicanException ex) {
			throw new AlgorithmException("load error with file : "
					+ tab[start].getPath());
		}

		output = new ByteImage(img.getXDim(), img.getYDim(), img.getZDim(),
				duration, img.getBDim());
		output.copyAttributes(img);

		output.setImage4D(img, 0, Image.T);// setFrame(0,img);
		// process other images
		if (verbose)
			System.out.print("Directory Load in Progress:");
		int lg = duration;
		if (lg > 9)
			lg /= 9;
		else
			lg = 1;
		for (int i = 1; i < duration; i++) {
			if (verbose)
				if (i % lg == 0)
					System.out.print(i / lg);
			try {
				output.setImage4D((Image) new ImageLoader().process(tab[i
						+ start].getPath()), i, Image.T);// setFrame(i,ImageLoader.exec(tab[i+start].getPath()));
			} catch (PelicanException ex) {
				throw new AlgorithmException("load error with file : "
						+ tab[start + i].getPath());
			}
		}
		if (verbose)
			System.out.println();
	}

	/**
	 * Partial multiple image loading in a single directory.
	 * 
	 * @param filename
	 *            Directory name.
	 * @param start
	 *            Index of the first file to load.
	 * @param duration
	 *            Number of files to load.
	 * @return The output image.
	 */
	public static Image exec(String filename, int start, int duration) {
		return (Image) new PartialMultipleImageLoad().process(filename, start,
				duration);
	}

}
