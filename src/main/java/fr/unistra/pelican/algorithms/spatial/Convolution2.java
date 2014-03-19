/**
 * 
 */
package fr.unistra.pelican.algorithms.spatial;

import java.awt.Point;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

import fr.unistra.pelican.util.morphology.GrayStructuringElement;

/**
 * Performs convolution of an image with a mask (GrayStructuringElement) Result
 * is normalized (divided by the sum of structuring element values) if requested
 * Works on double precision
 * 
 * @author Benjamin Perret
 * 
 * TODO merge with convolution
 * 
 */
public class Convolution2 extends Algorithm {

	/**
	 * Constant parameter to request normalization
	 */
	public static int NORMALIZE = 0;

	/**
	 * Constant parameter to request no normalization
	 */
	public static int NO_NORMALIZE = 1;

	/**
	 * Input Image
	 */
	public Image inputImage;

	/**
	 * Input structuring element
	 */
	public GrayStructuringElement se;

	/**
	 * Input normalization argument
	 */
	public int normalize = 1;

	/**
	 * Output Image
	 */
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public Convolution2() {

		super();
		super.inputs = "inputImage,se";
		super.options="normalize";
		super.outputs = "outputImage";
		
	}

	/**
	 * Compute convolution for a given point
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param t
	 * @param b
	 * @return
	 */
	public double getVal(int x, int y, int z, int t, int b, boolean f) {
		double sum = 0.0;
		double tmp = 0.0;
		boolean flag = false;

		
		
		
		for (int j = 0; j < se.getYDim(); j++) 
			for (int i = 0; i < se.getXDim(); i++){
				/*
				 * Take inverse structuring element
				 */
				int valX = x - se.getCenter().x + i;
				int valY = y - se.getCenter().y + j;
				if (valX >= 0 && valX < inputImage.getXDim() && valY >= 0
						&& valY < inputImage.getYDim()) {
					tmp += inputImage.getPixelDouble(valX, valY, z, t, b)
							* se.getPixelXYDouble(i, j);
					if (f)
						sum += se.getPixelXYDouble(i, j);
					flag = true;
				}
			}
		if (f)
			tmp /= sum;
		// FIXME: Strange, if nothing is under the se, what is the right way?
		return (flag == true) ? tmp : inputImage.getPixelDouble(x, y, z, t, b);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = inputImage.copyImage(false);
		se = se.getReflection();
		int xDim = inputImage.getXDim();
		int yDim = inputImage.getYDim();
		int tDim = inputImage.getTDim();
		int bDim = inputImage.getBDim();
		int zDim = inputImage.getZDim();

		for (int b = 0; b < bDim; b++)
			for (int t = 0; t < tDim; t++)
				for (int z = 0; z < zDim; z++)
					for (int x = 0; x < xDim; x++)
						for (int y = 0; y < yDim; y++)
							outputImage.setPixelDouble(x, y, z, t, b, getVal(x,
									y, z, t, b, normalize == NORMALIZE));
	}

	/*public static void main(String[] args) {
		Image im = new DoubleImage(256, 256, 1, 1, 1);
		im.setPixelXYDouble(50, 50, 1.0);
		im.setPixelXYDouble(51, 50, 1.0);
		im.setPixelXYDouble(50, 51, 1.0);
		im.setPixelXYDouble(51, 51, 1.0);
		im.setPixelXYDouble(50, 52, 1.0);
		im.setPixelXYDouble(52, 50, 1.0);
		im.setPixelXYDouble(52, 51, 1.0);
		im.setPixelXYDouble(51, 52, 1.0);
		im.setPixelXYDouble(52, 52, 1.0);
		im.setPixelXYDouble(100, 100, 1.0);
		im.setPixelXYDouble(102, 100, 1.0);
		new Viewer2D().process(im, "op");
		
			GrayStructuringElement se = new GrayStructuringElement(21, 21,
				new Point(10, 10));
		double[] vals = GaussianMask.createLinearMask(21, 0.02);
		se.setPixels(vals);
		Image im2 = (Image) new Convolution2().process(im, se, Convolution2.NO_NORMALIZE);
		// Image im3=Convolution.process(im, se,Convolution.NORMALIZE);
		// Viewer2D.exec(se,"PSF");
		new Viewer2D().process(((DoubleImage) im2).scaleToZeroOne(), "NO_NORMALIZE");
		// Viewer2D.exec(((DoubleImage)im3).scaleToZeroOne(),"NORMALIZE");
	}*/
	
	/**
	 * Standard convolution for linear filters
	 * @param input Input image
	 * @param kernel Structuring element used for the convolution
	 * @return Output image
	 */
	public static Image exec(Image input, GrayStructuringElement kernel) {
		return (Image) new Convolution2().process(input,kernel);
	}
	
	
	/**
	 * Standard convolution for linear filters
	 * @param input Input image
	 * @param kernel Structuring element used for the convolution
	 * @param normalisation Specify if result is normalized
	 * @return Output image
	 */
	public static Image exec(Image input, GrayStructuringElement kernel, int normalisation) {
		return (Image) new Convolution2().process(input,kernel,normalisation);
	}
}
