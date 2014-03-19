package fr.unistra.pelican.algorithms.segmentation.labels;

import java.util.Vector;
import fr.unistra.pelican.*;



/**
 * This class convert an label image to a color image. For each label it
 * associates the value of the valley in the base image. The output image has
 * the same format as the base image. Only the first band of label image is
 * taken in account. Work on double precision. Dimensions: X, Y.
 * 
 * Code based on LabelsToColorByMeanValue
 * 
 * FIXME if several valleys are found within a region, what to do ?
 * 
 * @author Lefevre
 */
public class LabelsToValleyColor extends Algorithm {

	/**
	 * The label image
	 */
	public Image labelImage;

	/**
	 * The base image
	 */
	public Image reliefImage;

	/**
	 * The base image
	 */
	public Image baseImage;

	/**
	 * The resulting image
	 */
	public Image outputImage;

	/**
	 * Constructor
	 */
	public LabelsToValleyColor() {
		super.inputs = "labelImage,reliefImage,baseImage";
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
					for (int x = 0; x < xDim; x++) {

						int label = labelImage.getPixelXYZTBInt(x, y, z, t, 0);
						if (labels.size() <= label)
							labels.setSize(label + 1);
						// value[0] = valley elevation
						// value[b] = valley color value with this label for band b
						double value[] = labels.get(label);
						// new pixel for this label
						if (value == null) {
							value = new double[bDim + 1];
							value[0]=reliefImage.getPixelXYZTBInt(x,y,z,t,0);
							for (int b = 0; b < bDim; b++) // ++b ?
								value[b + 1] = baseImage.getPixelXYZTBDouble(x, y,
										z, t, b);
							labels.set(label, value);
						}
						// check if new minimum
						else if (reliefImage.getPixelXYZTBInt(x,y,z,t,0)<value[0]) {
							value[0]=reliefImage.getPixelXYZTBInt(x,y,z,t,0);
							for (int b = 0; b < bDim; b++) // ++b ?
								value[b + 1] = baseImage.getPixelXYZTBDouble(x, y,
										z, t, b);
							labels.set(label, value);
						}						
					}

		// Set each pixel value
		for (int t = 0; t < tDim; t++)
			for (int z = 0; z < zDim; z++)
				for (int y = 0; y < yDim; y++)
					for (int x = 0; x < xDim; x++) {
						int label = labelImage.getPixelXYZTBInt(x, y, z, t, 0);
						double value[] = labels.get(label);
						if (value == null)
							throw new AlgorithmException(
									"Label with no pixel but in use????");
						for (int b = 0; b < bDim; ++b)
							outputImage.setPixelXYZTBDouble(x, y, z, t, b,
									value[b + 1]);
					}
	}

	/**
	 * Colorizes a label image by replacing each label by the colour of the
	 * valley in the related region
	 * 
	 * @param label the label image
	 * @param relief the relief image
	 * @param source the source (colour) image
	 * @return the new colour image
	 */
	public static Image exec(Image label, Image relief, Image source) {
		return (Image) new LabelsToValleyColor().process(label, relief, source);
	}

}
