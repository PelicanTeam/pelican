package fr.unistra.pelican.algorithms.geometric;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.Image;

/**
 * This class performs a 2D horizontal axial symmetry of the input image
 *  
 * @author Weber
 */
public class HorizontalAxialSymmetry2D extends Algorithm {

	/**
	 * The input image
	 */
	public Image input;
	
	/**
	 * The output image
	 */
	public Image output;

	/**
	 * Default constructor
	 *
	 */
	public HorizontalAxialSymmetry2D() {
		super.inputs = "input";
		super.outputs = "output";
		
	}

	/**
	 * Performs a 2D symmetry of the input image
	 * @param input The input image
	 * @return The output image
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Image> T exec(T input) {
		return (T) new HorizontalAxialSymmetry2D().process(input);
	}
	
	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() {
		int xdim=input.getXDim()-1;
		int ydim=input.getYDim()-1;
		output=input.copyImage(false);
		for(int b=0;b<input.bdim;b++)
			for(int x=0;x<=xdim;x++)
				for(int y=0;y<=ydim;y++)
					output.setPixelXYBDouble(x,y,b,input.getPixelXYBDouble(x,ydim-y,b));
	}

}
