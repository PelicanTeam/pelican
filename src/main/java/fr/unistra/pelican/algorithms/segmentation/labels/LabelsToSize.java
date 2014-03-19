package fr.unistra.pelican.algorithms.segmentation.labels;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.InvalidNumberOfParametersException;
import fr.unistra.pelican.InvalidTypeOfParameterException;
import fr.unistra.pelican.algorithms.arithmetic.DeleteSmallValues;
import fr.unistra.pelican.algorithms.arithmetic.EuclideanNorm;
import fr.unistra.pelican.algorithms.histogram.ContrastStretch;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.morphology.gray.GrayGradient;
import fr.unistra.pelican.algorithms.segmentation.Watershed;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 * The value of each pixel of the output image is the size of the region it
 * belong in the input image. Max region size is Integer.MAX_VALUE. Dimensions:
 * X, Y
 * @author Sébastien Derivaux
 */
public class LabelsToSize extends Algorithm {

	// Inputs parameters
	/**
	 * Input Image
	 */
	public Image inputImage;

	// Outputs parameters
	/**
	 * result
	 */
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public LabelsToSize() {

		super();
		super.inputs = "inputImage";
		super.outputs = "outputImage";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		try {

			int[] regionSize = (int[]) new RegionSize().process(inputImage);

			outputImage = new IntegerImage(inputImage, false);

			for (int i = 0; i < outputImage.size(); i++)
				outputImage.setPixelInt(i,
						regionSize[inputImage.getPixelInt(i)]);

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

	/**
	 * The value of each pixel of the output image is the size of the region it
	 * belong in the input image. Max region size is Integer.MAX_VALUE. Dimensions:
	 * @param inputImage
	 * @return
	 */
	public static Image exec(Image inputImage) {
		return (Image)new LabelsToSize().process(inputImage);
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
			new Viewer2D().process(new ContrastStretch()
					.process(new LabelsToSize().process(work)),
					"Segmentation size of " + file);

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
