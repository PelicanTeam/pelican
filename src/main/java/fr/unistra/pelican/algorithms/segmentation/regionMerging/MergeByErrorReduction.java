package fr.unistra.pelican.algorithms.segmentation.regionMerging;

import java.util.Vector;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;

/**
 * A classical region merger. Merge each connected regions if their mean value (Euclidian distance)
 * is below threshold.
 * 
 * @author Sébastien Derivaux, Jonathan Weber
 */
public class MergeByErrorReduction extends Algorithm {

	// Inputs parameters
	public Image labelImage;

	public Image baseImage;

	public double threshold;

	// Outputs parameters
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public MergeByErrorReduction() {

		super();
		super.inputs = "labelImage,baseImage,threshold";
		super.outputs = "outputImage";
		
		;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = baseImage.copyImage(false);

		Vector<double[]> labels = new Vector<double[]>();

		// Grab all pixels values
		int xDim = outputImage.getXDim();
		int yDim = outputImage.getYDim();
		int zDim = outputImage.getZDim();
		int tDim = outputImage.getTDim();
		int bDim = outputImage.getBDim();
		for (int x = 0; x < xDim; x++)
			for (int y = 0; y < yDim; y++) 
				for (int z=0; z<zDim;z++)
					for(int t=0;t<tDim;t++)
					{
						int label = labelImage.getPixelXYZTBInt(x, y,z,t, 0);
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
							value[b + 1] += baseImage.getPixelXYZTBDouble(x, y,z,t, b);
						labels.set(label, value);
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
				for (int z=0; z<zDim;z++)
					for(int t=0;t<tDim;t++)
					{
						int label = labelImage.getPixelXYZTBInt(x, y, z, t, 0);
						double value[] = labels.get(label);
						if (value == null)
							throw new AlgorithmException(
								"Label with no pixel but in use????");
						for (int b = 0; b < bDim; ++b)
							outputImage.setPixelXYZTBDouble(x, y, z, t, b, value[b + 1]);
					}

	}
	
	public static Image exec(Image labelImage, Image baseImage, double threshold) {
		return (Image)new MergeByErrorReduction().process(labelImage, baseImage, threshold);
	}

}
