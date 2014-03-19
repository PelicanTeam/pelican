package fr.unistra.pelican.algorithms.io;

import java.io.File;
import java.util.Arrays;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.PelicanException;

/**
 * Read multiple text files as a sequence or stack of images
 * 
 * @author
 */

public class MultipleTextImageLoad extends Algorithm {

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
	 * Second input parameter
	 */
	public int dim;

	/**
	 * x dimension of result
	 */
	public int xdim;

	/**
	 * y dimension of result
	 */
	public int ydim;

	/**
	 * z dimension of result
	 */
	public int zdim;

	/**
	 * t dimension of result
	 */
	public int tdim;

	/**
	 * b dimension of result
	 */
	public int bdim;

	/**
	 * type of result
	 */
	public int type = BYTE;

	/**
	 * (optional) flag to enable partial file read
	 */
	public boolean partial = false;

	public final static int BOOLEAN = 0;
	public final static int BYTE = 1;
	public final static int INTEGER = 2;
	public final static int DOUBLE = 3;

	/**
	 * Constructor
	 * 
	 */
	public MultipleTextImageLoad() {
		super.inputs = "filename,dim,xdim,ydim,zdim,tdim,bdim";
		super.options = "type,partial";
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

		int xdim2=xdim;
		int ydim2=ydim;
		int zdim2=zdim;
		int tdim2=tdim;
		int bdim2=bdim;
	
		switch (dim) {
		case Image.X:
			xdim2 = tab.length;
			break;
		case Image.Y:
			ydim2 = tab.length;
			break;
		case Image.Z:
			zdim2 = tab.length;
			break;
		case Image.T:
			tdim2 = tab.length;
			break;
		case Image.B:
			bdim2 = tab.length;
			break;
		}

		switch (type) {
		case BOOLEAN:
			output = new BooleanImage(xdim2, ydim2, zdim2, tdim2, bdim2);
			break;
		case BYTE:
			output = new ByteImage(xdim2, ydim2, zdim2, tdim2, bdim2);
			break;
		case INTEGER:
			output = new IntegerImage(xdim2, ydim2, zdim2, tdim2, bdim2);
			break;
		case DOUBLE:
			output = new DoubleImage(xdim2, ydim2, zdim2, tdim2, bdim2);
			break;
		}

		// process other images
		if (verbose)
			System.out.print("Directory Load in Progress:");

		int lg = tab.length;

		if (lg > 9)
			lg /= 9;
		else
			lg = 1;

		for (int i = 0; i < tab.length; i++) {
			if (verbose)
				if (i % lg == 0)
					System.out.print(i / lg);
			try {
				// System.out.println(tab[i].getPath());
				output.setImage4D(TextImageLoad.exec(tab[i].getPath(), xdim,
						ydim, zdim, tdim, bdim, type, partial), i, dim);
			} catch (PelicanException ex) {
				ex.printStackTrace();
				throw new AlgorithmException("load error with file : "
						+ tab[i].getPath());
			}
		}
		if (verbose)
			System.out.println();
	}

	public static Image exec(String filename, int dim, int xdim, int ydim,
			int zdim, int tdim, int bdim) {
		return (Image) new MultipleTextImageLoad().process(filename, dim, xdim,
				ydim, zdim, tdim, bdim);
	}

	public static Image exec(String filename, int dim, int xdim, int ydim,
			int zdim, int tdim, int bdim, int type) {
		return (Image) new MultipleTextImageLoad().process(filename, dim, xdim,
				ydim, zdim, tdim, bdim, type);
	}

	public static Image exec(String filename, int dim, int xdim, int ydim,
			int zdim, int tdim, int bdim, boolean partial) {
		return (Image) new MultipleTextImageLoad().process(filename, dim, xdim,
				ydim, zdim, tdim, bdim, partial);
	}

	public static Image exec(String filename, int dim, int xdim, int ydim,
			int zdim, int tdim, int bdim, int type, boolean partial) {
		return (Image) new MultipleTextImageLoad().process(filename, dim, xdim,
				ydim, zdim, tdim, bdim, type, partial);
	}

}
