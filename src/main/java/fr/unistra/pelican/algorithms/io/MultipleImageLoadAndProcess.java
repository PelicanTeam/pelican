package fr.unistra.pelican.algorithms.io;

import java.io.File;
import java.util.Arrays;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.geometric.BlockResampling2D;

/**
 * Modified in order to support additional dimensions and not only T.
 * 
 * @author
 */

public class MultipleImageLoadAndProcess extends Algorithm {
	
	private boolean verbose = true;
	
	/**
	 * First input parameter
	 */
	public String filename;

	/**
	 * Output parameter
	 */
	public Image output;

	/**
	 * Second input parameter.
	 */
	public int dim;

	/**
	 * Third input parameter
	 */
	public int processing = REDUCE;

	public static int REDUCE = 1;

	/**
	 * Constructor
	 * 
	 */
	public MultipleImageLoadAndProcess() {

		super();
		super.inputs = "filename,dim,processing";
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

		if (dim != Image.T && dim != Image.B)
			throw new AlgorithmException("Unsupported dimension");

		File tab[] = dir.listFiles();

		Arrays.sort(tab);

		Image img;

		// process first image
		try {
			// System.err.println("Loading " + tab[0].getPath());
			img = (Image) new ImageLoader().process(tab[0].getPath());
			if (processing == REDUCE)
				img = (Image) new BlockResampling2D().process(img, 8, 8, false);
		} catch (PelicanException ex) {
			throw new AlgorithmException("load error with file : "
					+ tab[0].getPath());
		}

		if (dim == Image.T)
			output = new ByteImage(img.getXDim(), img.getYDim(), img.getZDim(),
					tab.length, img.getBDim());
		else
			output = new ByteImage(img.getXDim(), img.getYDim(), img.getZDim(),
					img.getBDim(), tab.length);
		output.setImage4D(img, 0, dim);

		// process other images
		if (verbose)
			System.out.print("Directory Load in Progress:");

		int lg = tab.length;

		if (lg > 9)
			lg /= 9;
		else
			lg = 1;

		for (int i = 1; i < tab.length; i++) {
			if (verbose)
				if (i % lg == 0)
					System.out.print(i / lg);
			try {
				// System.err.println("Loading " + tab[i].getPath());
				img = (Image) new ImageLoader().process(tab[i].getPath());
				if (processing == REDUCE)
					img = (Image) new BlockResampling2D().process(img, 8, 8, false);
				output.setImage4D(img, i, dim);
			} catch (PelicanException ex) {
				throw new AlgorithmException("load error with file : "
						+ tab[i].getPath());
			}
		}
		if (verbose)
			System.out.println();
	}

	/**
	 * Modified in order to support additional dimensions and not only T.
	 * 
	 * @param filename Filename of the image to be loaded.
	 * @param dim Additonal dimensions of the image to be loaded.
	 * @param processing How the image has to processed.
	 * @return The image with additional dimensions.
	 */
	public static Image exec(String filename, int dim, int processing) {
		return (Image) new MultipleImageLoadAndProcess().process(filename, dim, processing);
	}
	
}
