package fr.unistra.pelican.algorithms.geometric;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.Image;

/**
 * This class swaps two dimensions (among X, Y, Z, T and B) of an input image
 * 
 * @author Lefevre
 */

//TODO : manage the image center modification.

public class SwapDimensions extends Algorithm {

	/**
	 * The input image
	 */
	public Image input;

	/**
	 * The first dimension to swap with
	 */
	public int dim1;

	/**
	 * The second dimension to swap with
	 */
	public int dim2;

	/**
	 * The output image
	 */
	public Image output;

	private int dimX;

	private int dimY;

	private int dimZ;

	private int dimT;

	private int dimB;

	/**
	 * Default constructor
	 */
	public SwapDimensions() {
		super.inputs = "input,dim1,dim2";
		super.outputs = "output";
		
	}

	/**
	 * Swaps two dimensions (among X, Y, Z, T and B) of an input image
	 * @param input The input image
	 * @param dim1 The first dimension to swap with
	 * @param dim2 The second dimension to swap with
	 * @return The output image
	 */
	public static Image exec(Image input, int dim1, int dim2) {
		return (Image) new SwapDimensions().process(input,dim1,dim2);
	}
	
	/*
	 * (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() {
		setDim();
		output = input.newInstance(dimX, dimY, dimZ, dimT, dimB);
		for (int z = 0; z < output.getZDim(); z++)
			for (int t = 0; t < output.getTDim(); t++)
				for (int b = 0; b < output.getBDim(); b++)
					for (int x = 0; x < output.getXDim(); x++)
						for (int y = 0; y < output.getYDim(); y++)
							output.setPixelDouble(x, y, z, t, b, getPixel(x, y,
									z, t, b));
	}

	private void setDim() {
		dimX = input.getXDim();
		dimY = input.getYDim();
		dimZ = input.getZDim();
		dimT = input.getTDim();
		dimB = input.getBDim();
		if (dim1 == Image.X) {
			if (dim2 == Image.Y) {
				dimX = input.getYDim();
				dimY = input.getXDim();
			}
			if (dim2 == Image.Z) {
				dimX = input.getZDim();
				dimZ = input.getXDim();
			}
			if (dim2 == Image.T) {
				dimX = input.getTDim();
				dimT = input.getXDim();
			}
			if (dim2 == Image.B) {
				dimX = input.getBDim();
				dimB = input.getXDim();
			}
		}
		if (dim1 == Image.Y) {
			if (dim2 == Image.X) {
				dimY = input.getXDim();
				dimX = input.getYDim();
			}
			if (dim2 == Image.Z) {
				dimY = input.getZDim();
				dimZ = input.getYDim();
			}
			if (dim2 == Image.T) {
				dimY = input.getTDim();
				dimT = input.getYDim();
			}
			if (dim2 == Image.B) {
				dimY = input.getBDim();
				dimB = input.getYDim();
			}
		}
		if (dim1 == Image.Z) {
			if (dim2 == Image.X) {
				dimZ = input.getXDim();
				dimX = input.getZDim();
			}
			if (dim2 == Image.Y) {
				dimZ = input.getYDim();
				dimY = input.getZDim();
			}
			if (dim2 == Image.T) {
				dimZ = input.getTDim();
				dimT = input.getZDim();
			}
			if (dim2 == Image.B) {
				dimZ = input.getBDim();
				dimB = input.getZDim();
			}
		}
		if (dim1 == Image.T) {
			if (dim2 == Image.X) {
				dimT = input.getXDim();
				dimX = input.getTDim();
			}
			if (dim2 == Image.Y) {
				dimT = input.getYDim();
				dimY = input.getTDim();
			}
			if (dim2 == Image.Z) {
				dimT = input.getZDim();
				dimZ = input.getTDim();
			}
			if (dim2 == Image.B) {
				dimT = input.getBDim();
				dimB = input.getTDim();
			}
		}
		if (dim1 == Image.B) {
			if (dim2 == Image.X) {
				dimB = input.getXDim();
				dimX = input.getBDim();
			}
			if (dim2 == Image.Y) {
				dimB = input.getYDim();
				dimY = input.getBDim();
			}
			if (dim2 == Image.Z) {
				dimB = input.getZDim();
				dimZ = input.getBDim();
			}
			if (dim2 == Image.T) {
				dimB = input.getTDim();
				dimT = input.getBDim();
			}
		}
	}

	private double getPixel(int x, int y, int z, int t, int b) {
		if (dim1 == Image.X) {
			if (dim2 == Image.Y)
				return input.getPixelDouble(y, x, z, t, b);
			if (dim2 == Image.Z)
				return input.getPixelDouble(z, y, x, t, b);
			if (dim2 == Image.T)
				return input.getPixelDouble(t, y, z, x, b);
			if (dim2 == Image.B)
				return input.getPixelDouble(b, y, z, t, x);
		}
		if (dim1 == Image.Y) {
			if (dim2 == Image.X)
				return input.getPixelDouble(y, x, z, t, b);
			if (dim2 == Image.Z)
				return input.getPixelDouble(x, z, y, t, b);
			if (dim2 == Image.T)
				return input.getPixelDouble(x, t, z, y, b);
			if (dim2 == Image.B)
				return input.getPixelDouble(x, b, z, t, y);
		}
		if (dim1 == Image.Z) {
			if (dim2 == Image.X)
				return input.getPixelDouble(z, y, x, t, b);
			if (dim2 == Image.Y)
				return input.getPixelDouble(x, z, y, t, b);
			if (dim2 == Image.T)
				return input.getPixelDouble(x, y, t, z, b);
			if (dim2 == Image.B)
				return input.getPixelDouble(x, y, b, t, z);
		}
		if (dim1 == Image.T) {
			if (dim2 == Image.X)
				return input.getPixelDouble(t, y, z, x, b);
			if (dim2 == Image.Y)
				return input.getPixelDouble(x, t, z, y, b);
			if (dim2 == Image.Z)
				return input.getPixelDouble(x, y, t, z, b);
			if (dim2 == Image.B)
				return input.getPixelDouble(x, y, z, b, t);
		}
		if (dim1 == Image.B) {
			if (dim2 == Image.X)
				return input.getPixelDouble(b, y, z, t, x);
			if (dim2 == Image.Y)
				return input.getPixelDouble(x, b, z, t, y);
			if (dim2 == Image.Z)
				return input.getPixelDouble(x, y, b, t, z);
			if (dim2 == Image.T)
				return input.getPixelDouble(x, y, z, b, t);
		}
		return 0;
	}

}
