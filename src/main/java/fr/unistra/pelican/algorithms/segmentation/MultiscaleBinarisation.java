package fr.unistra.pelican.algorithms.segmentation;

import java.util.ArrayList;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.InvalidNumberOfParametersException;
import fr.unistra.pelican.InvalidTypeOfParameterException;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.conversion.RGBToHSV;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.multiscale.Pyramid;

/**
 * binarize an image and return a boolean image. it is based on a multiscale
 * hue-based analysis Work on all format. Lefevre et al. CGIV 2002
 * 
 * @author lefevre
 * 
 */
public class MultiscaleBinarisation extends Algorithm {
	public Image inputImage;

	public int depth;

	public double THR1;

	public double THR2;

	public boolean adaptive;

	public int returned;

	public boolean small;

	public BooleanImage outputImage;

	private final static boolean DEBUG = false;

	/**
	 * Constructor
	 * 
	 */
	public MultiscaleBinarisation() {

		super();
		super.inputs = "inputImage,depth";
		super.outputs = "outputImage";
		
		;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = new BooleanImage(inputImage.getXDim(), inputImage
				.getYDim(), inputImage.getZDim(), inputImage.getTDim(), 1);
		Pyramid input = new Pyramid(inputImage, depth, true);
		Pyramid labels = new Pyramid(outputImage, depth, false);
		BooleanImage current, previous;
		Image model;
		double bg[], values[], diff;
		boolean label;
		// int k=(int)Math.pow(2,input.getDepth()-1);
		// Compute hue model at lowest scale
		model = input.getTop();
		try {
			model = (Image) new RGBToHSV().process(model);
		} catch (PelicanException ex) {
			throw new AlgorithmException(ex.getMessage());
		}
		bg = computeLocalHueStats(model, 0, model.getXDim() - 1, 0, model
				.getYDim() - 1, null);
		if (bg == null)
			return;
		current = (BooleanImage) labels.getTop();
		current.fill(true);
		// Performs the processing on a coarse-to-fine basis (top to bottom of
		// the pyramid)
		for (int d = input.getDepth() - 2; d >= 0; d--) {
			previous = current;
			current = (BooleanImage) labels.getScale(d);
			model = input.getScale(d);
			// Convert in HSV
			try {
				model = (Image) new RGBToHSV().process(model);
			} catch (PelicanException ex) {
				throw new AlgorithmException(ex.getMessage());
			}
			// Perform local matching
			for (int x = 0; x < previous.getXDim(); x++)
				for (int y = 0; y < previous.getYDim(); y++)
					for (int z = 0; z < previous.getZDim(); z++)
						for (int t = 0; t < previous.getTDim(); t++) {
							// If corresponding lowest scale region is
							// background, propagate it
							if (previous.getPixelBoolean(x, y, z, t, 0) == false)
								label = false;
							// Otherwise compute hue and perform matching
							else {
								values = computeLocalHueStats(model, 2 * x,
										2 * x + 1, 2 * y, 2 * y + 1, null);
								// if hue cannot be computed, we have to check
								// at finer scale
								if (values == null)
									label = true;
								else {
									// compare mean values
									diff = Math.abs(values[0] - bg[0]);
									if (diff > Math.PI)
										diff = 2 * Math.PI - diff;
									// check both diff with mean and local range
									if (diff > 2 * Math.PI * THR1
											|| values[1] > 2 * Math.PI * THR2)
										label = true;
									else
										label = false;
								}
							}
							// propagate labels
							current.setPixelBoolean(2 * x, 2 * y, z, t, 0,
									label);
							current.setPixelBoolean(2 * x, 2 * y + 1, z, t, 0,
									label);
							current.setPixelBoolean(2 * x + 1, 2 * y, z, t, 0,
									label);
							current.setPixelBoolean(2 * x + 1, 2 * y + 1, z, t,
									0, label);
						}
			// recompute background model if adaptive flag is set
			if (adaptive) {
				bg = computeLocalHueStats(model, 0, model.getXDim() - 1, 0,
						model.getYDim() - 1, current);
				if (bg == null)
					return;
			}
		}
		if (small)
			outputImage = (BooleanImage) labels.getScale(returned);
		else
			outputImage = (BooleanImage) labels.extractImage(returned);
		if (DEBUG)
			try {
				new Viewer2D().process(input.convertToImage(), "input");
				new Viewer2D().process(labels.convertToImage(), "output");
			} catch (PelicanException ex) {
				throw new AlgorithmException(ex.getMessage());
			}
	}

	private double[] computeLocalHueStats(Image img, int xmin, int xmax,
			int ymin, int ymax, BooleanImage mask) {
		if (mask != null
				&& (img.getXDim() != mask.getXDim()
						|| img.getYDim() != mask.getYDim()
						|| img.getZDim() != mask.getZDim() || img.getTDim() != mask
						.getTDim()))
			return null;
		double res[] = new double[2];
		double mean;
		double range;
		double max = 0;
		double min = 1;
		double sin = 0;
		double cos = 0;
		double angle;
		double valid = (xmax - xmin + 1) * (ymax - ymin + 1) * img.getZDim()
				* img.getTDim();
		// Analysis of each pixel of the image
		for (int x = xmin; x <= xmax; x++)
			for (int y = ymin; y <= ymax; y++)
				for (int z = 0; z < img.getZDim(); z++)
					for (int t = 0; t < img.getTDim(); t++) {
						if (mask != null && mask.getPixelBoolean(x, y, z, t, 0))
							valid--;
						else {
							// check if value or saturation are null
							if (img.getPixelDouble(x, y, z, t, 2) == 0
									|| img.getPixelDouble(x, y, z, t, 1) == 0)
								valid--;
							// if hue is valid, compute stats
							else {
								if (img.getPixelDouble(x, y, z, t, 0) < min)
									min = img.getPixelDouble(x, y, z, t, 0);
								if (img.getPixelDouble(x, y, z, t, 0) > max)
									max = img.getPixelDouble(x, y, z, t, 0);
								angle = 2 * Math.PI
										* img.getPixelDouble(x, y, z, t, 0);
								sin += Math.sin(angle);
								cos += Math.cos(angle);
							}
						}
					}
		// Check validity
		if (valid == 0)
			return null;
		// Compute the hue mean
		if (cos == 0)
			mean = 0;
		else
			mean = Math.atan(sin / cos);
		if (cos < 0)
			mean += Math.PI;
		// Compute the hue range
		double dmin, dmax;
		dmax = Math.abs(2 * Math.PI * max - mean);
		if (dmax > Math.PI)
			dmax = 2 * Math.PI - dmax;
		dmin = Math.abs(mean - 2 * Math.PI * min);
		if (dmin > Math.PI)
			dmin = 2 * Math.PI - dmin;
		range = dmin + dmax;
		if (dmin == 0 || dmax == 0)
			range *= 2;
		// Generates results
		res[0] = mean;
		res[1] = range;
		return res;
	}

	/**
	 * Static fonction that binarize an image.
	 * 
	 * @param image
	 * @param threshold
	 * @return image binarized
	 * @throws InvalidTypeOfParameterException
	 * @throws AlgorithmException
	 * @throws InvalidNumberOfParametersException
	 */
	public static BooleanImage process(Image image, Integer depth,
			Double thrMatching, Double thrRange, Boolean adaptive,
			Integer returnedScale, Boolean small)
			throws InvalidTypeOfParameterException, AlgorithmException,
			InvalidNumberOfParametersException {
		MultiscaleBinarisation binarisation = new MultiscaleBinarisation();
		ArrayList input = new ArrayList();
		input.add(image);
		input.add(depth);
		input.add(thrMatching);
		input.add(thrRange);
		input.add(adaptive);
		input.add(returnedScale);
		input.add(small);
		binarisation.setInput(input);
		binarisation.launch();
		return (BooleanImage) binarisation.getOutput().get(0);
	}

	public static void main(String args[]) throws PelicanException {
		Image input = (Image) new ImageLoader().process("samples/foot.png");
		new Viewer2D().process(input, "original");
		BooleanImage res = MultiscaleBinarisation.process(input, 6, 1.0 / 36,
				1.0 / 36, true, 3, false);
		new Viewer2D().process(res, "resultat");
	}

}
