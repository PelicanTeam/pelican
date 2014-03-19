package fr.unistra.pelican.algorithms.io;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;

public class MultipleImageSave extends Algorithm {

	// Verbose mode
	boolean verbose = true;

	/**
	 * Second input parameter
	 */
	public String filename;

	/**
	 * Third input parameter
	 */
	public String extension;

	/**
	 * First input parameter
	 */
	public Image input;

	/**
	 * Constructor
	 * 
	 */
	public MultipleImageSave() {

		super();
		super.inputs = "input,filename,extension";
		super.outputs = "";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		// generate filenames
		int nbf = input.getTDim();
		int nbc = Integer.toString(nbf).length();
		int nbc2;
		StringBuffer zeros;
		String file;
		// verbose mode
		if (verbose)
			System.out.print("Directory Save in Progress:");
		int lg = nbf;
		if (lg > 9)
			lg /= 9;
		else
			lg = 1;
		// iterative saving
		for (int i = 0; i < nbf; i++) {
			if (verbose)
				if (i % lg == 0)
					System.out.print(i / lg);
			zeros = new StringBuffer();
			nbc2 = Integer.toString(i).length();
			for (int j = 0; j < nbc - nbc2; j++)
				zeros.append('0');
			file = filename + zeros.toString() + i + extension;
			// performs saving
			try {
				new ImageSave().process(input.getImage4D(i, Image.T), file);
			} catch (PelicanException ex) {
				throw new AlgorithmException("load error with file : " + file);
			}
		}
		if (verbose)
			System.out.println();
	}
	
	/**
	 * 
	 * @param input Image to be saved.
	 * @param filename Location where the image will be saved.
	 * @param extension Extension the image will take.
	 */
	public static void exec(Image input, String filename, String extension) {
		new MultipleImageSave().process(input,filename,extension);
	}

}
