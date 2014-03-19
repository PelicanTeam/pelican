package fr.unistra.pelican.algorithms.spatial;

import java.awt.Point;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.morphology.GrayStructuringElement;

/**
 * Standard convolution for linear filters
 * 
 * @author Lefevre
 */
public class Convolution extends Algorithm {
	
	/**
	 * Input image
	 */
	public Image input;
	
	/**
	 * Structuring element used for the convolution
	 */
	public GrayStructuringElement kernel;

	/**
	 * Output image
	 */
	public Image output;

	/**
	 * Constructor
	 * 
	 */
	public Convolution() {

		super();
		super.inputs = "input,kernel";
		super.outputs = "output";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		int xDim = input.getXDim();
		int yDim = input.getYDim();
		int bDim = input.getBDim();

		// reflect...
		kernel = kernel.getReflection();

		// create empty output
		output = input.copyImage(true);

		//TODO: use a different SE for each band
		Point[] points = kernel.getPoints().get(0);

		// convolve and fill it up

		for (int b = 0; b < bDim; b++) {
			for (int x = 0; x < xDim; x++) {
				for (int y = 0; y < yDim; y++) {

					double gcc = 0.0;
					double syc = 0.0;

					for (int i = 0; i < points.length; i++) {
						int _x = x - kernel.getCenter().x + points[i].x;
						int _y = y - kernel.getCenter().y + points[i].y;

						if (_x < 0 || _x >= xDim || _y < 0 || _y >= yDim)
							continue;

						// rows,columns
						double p = kernel.getValue(points[i].x, points[i].y);

						gcc += p * input.getPixelXYBDouble(_x, _y, b);
						syc += p;
					}
					

					output.setPixelXYBDouble(x, y, b, gcc / (double) syc);
				}
			}
		}
	}
	
	/**
	 * Standard convolution for linear filters
	 * @param input Input image
	 * @param kernel Structuring element used for the convolution
	 * @return Output image
	 */
	public static <T extends Image> T exec(T input, GrayStructuringElement kernel) {
		return (T) new Convolution().process(input,kernel);
	}
}
