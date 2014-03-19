package fr.unistra.pelican.algorithms.segmentation.labels;

import java.util.Vector;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;


/**
 * This classe convert an label image to an image. For each label it associate
 * the mean value of the covered zone on a base image. The output image has the
 * same format as the base image. Only the first band of label image is taken in
 * account. Work on double precision. Dimensions: X, Y, Z, T
 * 
 * TODO : Optimize computational cost
 * 
 * @author Sébastien Derivaux, Jonathan Weber
 */
public class LabelsToColorByMeanValue extends Algorithm {

	/**
	 * The label image
	 */
	public IntegerImage labelImage;

	/**
	 * The base image
	 */
	public Image baseImage;
	
	/**
	 * Do not affect color to label 0
	 */
	public boolean ignoreBackground=false;

	/**
	 * The resulting image
	 */
	public Image outputImage;

	/**
	 * Constructor
	 */
	public LabelsToColorByMeanValue() {
		super.inputs = "labelImage,baseImage";
		super.options="ignoreBackground";
		super.outputs = "outputImage";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = baseImage.copyImage(false);
		outputImage.copyAttributes(baseImage);
		Vector<double[]> labels = new Vector<double[]>();

		// Grab all pixels values
		int xDim = outputImage.getXDim();
		int yDim = outputImage.getYDim();
		int zDim = outputImage.getZDim();
		int tDim = outputImage.getTDim();
		int bDim = outputImage.getBDim();
		for (int t = 0; t < tDim; t++)
			for (int z = 0; z < zDim; z++)
				for (int y = 0; y < yDim; y++)
					for (int x = 0; x < xDim; x++)				
					 {
						int label = labelImage.getPixelXYZTBInt(x, y, z, t, 0);
						if(!ignoreBackground || label!=0)
						{
							if (labels.size() <= label)
								labels.setSize(label + 1);
							// value[0] = number of pixels
							// value[b] = sum of pixels with this label for band b
							double value[] = labels.get(label);
							if (value == null) {
								value = new double[bDim + 1];
							}
							value[0] = value[0] + 1.0;
							for (int b = 0; b < bDim; ++b)
								value[b + 1] += baseImage.getPixelXYZTBDouble(x, y,
										z, t, b);
							labels.set(label, value);
						}
					}

		// Calculate the mean.
		for (int i = 0; i < labels.size(); i++)
			for (int b = 0; b < bDim; ++b) {
				double value[] = labels.get(i);
				if (value == null)
					continue;
				value[b + 1] = value[b + 1] / value[0];
			}

		// Set each pixel value
		for (int x = 0; x < xDim; x++)
			for (int y = 0; y < yDim; y++)
				for (int z = 0; z < zDim; z++)
					for (int t = 0; t < tDim; t++) {
						int label = labelImage.getPixelXYZTBInt(x, y, z, t, 0);
						if(!ignoreBackground || label!=0)
						{
							double value[] = labels.get(label);
							if (value == null)
								throw new AlgorithmException(
								"Label with no pixel but in use????");
							for (int b = 0; b < bDim; ++b)
								outputImage.setPixelXYZTBDouble(x, y, z, t, b,
										value[b + 1]);
						}
					}
	}

	/**
	 * Colorizes a label image by replacing each label by the average colour of
	 * its related region
	 * 
	 * @param label
	 * @param source
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static<T extends Image> T exec(IntegerImage label, T source) {
		return (T) new LabelsToColorByMeanValue().process(label, source);
	}
	
	@SuppressWarnings("unchecked")
	public static<T extends Image> T  exec(IntegerImage label, T source, boolean ignoreBackground) {
		return (T) new LabelsToColorByMeanValue().process(label, source,ignoreBackground);
	}

}
