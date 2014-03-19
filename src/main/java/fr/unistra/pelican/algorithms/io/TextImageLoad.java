/**
 * 
 */
package fr.unistra.pelican.algorithms.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;

/**
 * Load an image save as text. Additional information has to be supplied.
 * 
 * @author Lefevre
 * 
 */
public class TextImageLoad extends Algorithm {

	/**
	 * Path to the file to be read
	 */
	public String filename;

	/**
	 * x dimension of result
	 */
	public int xdim;

	/**
	 * y dimension of result
	 */
	public int ydim;

	/**
	 * b dimension of result
	 */
	public int zdim;

	/**
	 * b dimension of result
	 */
	public int tdim;

	/**
	 * b dimension of result
	 */
	public int bdim;

	/**
	 * (optional) type of result
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
	 * Result
	 */
	public Image output;

	/**
	 * 
	 */
	public TextImageLoad() {
		super.inputs = "filename,xdim,ydim,zdim,tdim,bdim";
		super.options = "type,partial";
		super.outputs = "output";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	@Override
	public void launch() throws AlgorithmException {
		File f = new File(filename);
		try {
			BufferedReader br = new BufferedReader(new FileReader(f));
			String line;
			int i = 0;
			switch (type) {
			case BOOLEAN:
				output = new BooleanImage(xdim, ydim, zdim, tdim, bdim);
				break;
			case BYTE:
				output = new ByteImage(xdim, ydim, zdim, tdim, bdim);
				break;
			case INTEGER:
				output = new IntegerImage(xdim, ydim, zdim, tdim, bdim);
				break;
			case DOUBLE:
				output = new DoubleImage(xdim, ydim, zdim, tdim, bdim);
				break;
			}
			int size = xdim * ydim * zdim * tdim * bdim;
			while ((line = br.readLine()) != null) {
				String[] toks = line.split(" |\\t");
				for (String t : toks)
					if (!"".equals(t)) {
						if (i == size) {
							if (partial)
								break;
							else
								throw new AlgorithmException(
										"Dimension errors with file "
												+ f
												+ ". User specified "
												+ xdim
												+ " * "
												+ ydim
												+ " * "
												+ zdim
												+ " * "
												+ tdim
												+ " * "
												+ bdim
												+ " but file contains more than "
												+ size + " values.");
						}
						switch (type) {
						case BOOLEAN:
							// output.setPixelBoolean(i++,
							// Boolean.parseBoolean(t));
							output.setPixelBoolean(i++, t.equals("1") ? true
									: false);
							break;
						case BYTE:
							output.setPixelByte(i++, Integer.parseInt(t));
							break;
						case INTEGER:
							output.setPixelInt(i++, Integer.parseInt(t));
							break;
						case DOUBLE:
							output.setPixelDouble(i++, Double.parseDouble(t));
							break;
						}

					}

			}
			if (i != size)
				throw new AlgorithmException("Dimension errors with file " + f
						+ ". User specified " + xdim + " * " + ydim + " * "
						+ zdim + " * " + tdim + " * " + bdim
						+ " but file contains less than " + size + " values.");
			br.close();
		} catch (FileNotFoundException e) {
			throw new AlgorithmException("Error opening file " + f, e);
		} catch (IOException e) {
			throw new AlgorithmException("Error reading file " + f, e);
		} catch (NumberFormatException e) {
			throw new AlgorithmException("Error reading file " + f
					+ " : value can not be parsed.", e);
		}

	}

	public static Image exec(String filename, int xdim, int ydim, int zdim,
			int tdim, int bdim) {
		return (Image) (new TextImageLoad().process(filename, xdim, ydim, zdim,
				tdim, bdim));
	}

	public static Image exec(String filename, int xdim, int ydim, int zdim,
			int tdim, int bdim, int type) {
		return (Image) (new TextImageLoad().process(filename, xdim, ydim, zdim,
				tdim, bdim, type));
	}

	public static Image exec(String filename, int xdim, int ydim, int zdim,
			int tdim, int bdim, boolean partial) {
		return (Image) (new TextImageLoad().process(filename, xdim, ydim, zdim,
				tdim, bdim, partial));
	}

	public static Image exec(String filename, int xdim, int ydim, int zdim,
			int tdim, int bdim, int type, boolean partial) {
		return (Image) (new TextImageLoad().process(filename, xdim, ydim, zdim,
				tdim, bdim, type, partial));
	}

}
