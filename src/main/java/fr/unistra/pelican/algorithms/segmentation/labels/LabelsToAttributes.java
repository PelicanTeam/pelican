package fr.unistra.pelican.algorithms.segmentation.labels;

import java.util.Vector;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.InvalidNumberOfParametersException;
import fr.unistra.pelican.InvalidTypeOfParameterException;
import fr.unistra.pelican.algorithms.arithmetic.DeleteSmallValues;
import fr.unistra.pelican.algorithms.arithmetic.EuclideanNorm;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.morphology.gray.GrayGradient;
import fr.unistra.pelican.algorithms.segmentation.Watershed;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 * This classe convert an label image to an image. For each label it associate
 * some attributes.
 * @author Sébastien Derivaux
 */
public class LabelsToAttributes extends Algorithm {

	// Inputs parameters
	public Image labelImage;

	public Image baseImage;

	// Outputs parameters
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public LabelsToAttributes() {

		super();
		super.inputs = "labelImage,baseImage";
		super.outputs = "outputImage";
		
		;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = new DoubleImage(baseImage.getXDim(), baseImage.getYDim(),
				1, 1, baseImage.getBDim() * 2);

		Vector<double[]> labels = new Vector<double[]>();

		// Grab all pixels values
		int xDim = outputImage.getXDim();
		int yDim = outputImage.getYDim();
		int bDim = outputImage.getBDim();
		for (int x = 0; x < xDim; x++)
			for (int y = 0; y < yDim; y++) {
				int label = labelImage.getPixelXYBInt(x, y, 0);
				if (labels.size() <= label)
					labels.setSize(label + 1);
				// value[0] = number of pixels
				// value[b+1] = sum of pixels with this label for band b
				double value[] = labels.get(label);
				if (value == null) {
					value = new double[bDim * 2 + 1];
				}
				value[0] = value[0] + 1.0;
				for (int b = 0; b < bDim; ++b)
					value[b + 1] += baseImage.getPixelXYBDouble(x, y, b);
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
			for (int y = 0; y < yDim; y++) {
				int label = labelImage.getPixelXYBInt(x, y, 0);
				double value[] = labels.get(label);
				if (value == null)
					throw new AlgorithmException(
							"Label with no pixel but in use????");
				for (int b = 0; b < bDim; ++b)
					outputImage.setPixelXYBDouble(x, y, b, value[b + 1]);
			}

		// Compuet stddev
		for (int x = 0; x < xDim; x++)
			for (int y = 0; y < yDim; y++) {
				int label = labelImage.getPixelXYBInt(x, y, 0);
				double value[] = labels.get(label);
				if (value == null)
					throw new AlgorithmException(
							"Label with no pixel but in use????");
				for (int b = 0; b < bDim; ++b)
					value[2 * bDim + 1] += value[b + 1]
							- baseImage.getPixelXYBDouble(x, y, b);
				labels.set(label, value);
			}

	}

	public static void main(String[] args) {
		String file = "samples/remotesensing1.png";
		if (args.length > 0)
			file = args[0];

		BooleanImage se3 = FlatStructuringElement2D
				.createSquareFlatStructuringElement(3);

		try {
			// Load the image
			Image source = (Image) new ImageLoader().process(file);
			new Viewer2D().process(source, "Image " + file);

			// Compute the gradient on each band
			Image work = (Image) new GrayGradient().process(source, se3);

			// Compute the euclidian distance of the gradient
			work = (Image) new EuclideanNorm().process(work);

			work = (Image) new DeleteSmallValues().process(work, 0.2);

			// Process a watershed transformation
			work = (Image) new Watershed().process(work);

			work = (Image) new DeleteFrontiers().process(work);

			// View it
			new Viewer2D().process(new LabelsToAttributes().process(work, source),
					"Segmentation of " + file);

		} catch (InvalidTypeOfParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidNumberOfParametersException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
