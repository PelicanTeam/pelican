package fr.unistra.pelican.algorithms.segmentation.regionMerging;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.InvalidNumberOfParametersException;
import fr.unistra.pelican.InvalidTypeOfParameterException;
import fr.unistra.pelican.algorithms.arithmetic.DeleteSmallValues;
import fr.unistra.pelican.algorithms.arithmetic.EuclideanNorm;
import fr.unistra.pelican.algorithms.histogram.ContrastStretch;
import fr.unistra.pelican.algorithms.morphology.gray.GrayGradient;
import fr.unistra.pelican.algorithms.segmentation.Watershed;
import fr.unistra.pelican.algorithms.segmentation.labels.DeleteFrontiers;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToColorByMeanValue;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 * Maybe cf S. Beucher. Watershed, hierarchical segmentation and waterfall
 * algorithm. In Mathematical Morphology and its Applications to Image
 * Processing, Proc. ISMM 94, pages 69-76, Fontainebleau, France, 1994. Kluwer
 * Ac. Publ.
 * 
 * @author Sébastien Derivaux
 */
public class MergeUsingWatershed extends Algorithm {

	// Inputs parameters
	public Image labelImage;

	public Image spaceImage;

	public double threshold;

	// Outputs parameters
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public MergeUsingWatershed() {

		super();
		super.inputs = "labelImage,spaceImage,threshold";
		super.outputs = "outputImage";
		

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = labelImage.copyImage(false);

		try {

			Image values = (Image) new LabelsToColorByMeanValue().process(labelImage,
					spaceImage);

			BooleanImage se3 = FlatStructuringElement2D
					.createSquareFlatStructuringElement(3);

			// Compute the gradient on each band
			values = (Image) new GrayGradient().process(values, se3);

			// Compute the euclidian distance of the gradient
			values = (Image) new EuclideanNorm().process(values);

			values = (Image) new ContrastStretch().process(values);

			// Reduce the gradient image with hmin to remove useless local
			// minima
			values = (Image) new DeleteSmallValues().process(values, threshold);

			Image process = (Image) new Watershed().process(values);
			// Process a watershed transformation
			values = process;

			values = (Image) new DeleteFrontiers().process(values);

			outputImage = values;

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
	
	public static Image exec(Image labelImage, Image spaceImage, double threshold) {
		return (Image)new MergeUsingWatershed().process(labelImage, spaceImage, threshold);
	}

//	public static void main(String[] args) {
//		String file = "./samples/remotesensing1";
//		if (args.length > 0)
//			file = args[0];
//
//		try {
//			// Load the image
//			Image source = (Image) new ImageLoader().process(file + ".png");
//			Image regions = (Image) new RegionsLoader().process(file);
//
//			System.out.println("RegionBuilderWatershedClassical of " + file);
//
//			// Create regions
//			Image result = (Image) new RegionBuilderWatershedClassical()
//					.process(source, 0.15);
//
//			// View it
//			new Viewer2D().process(new DrawFrontiersOnImage().process(source,
//					new FrontiersFromSegmentation().process(result)), "" + file
//					+ " after watershed");
//			// Viewer2D.exec(LabelsToColorByMeanValue.process(result,
//			// source), "RegionBuilderWatershedClassical of " + file);
//			System.out.println(new EvalSegmentation().process(result, regions));
//
//			result = (Image) new MergeUsingWatershed().process(result, source, 0.05);
//
//			new Viewer2D().process(new DrawFrontiersOnImage().process(source,
//					new FrontiersFromSegmentation().process(result)), "" + file
//					+ " after region merging 1");
//			System.out.println(new EvalSegmentation().process(result, regions));
//
//			result = (Image) new MergeUsingWatershed().process(result, source, 0.05);
//
//			new Viewer2D().process(new DrawFrontiersOnImage().process(source,
//					new FrontiersFromSegmentation().process(result)), "" + file
//					+ " after region merging 2");
//			System.out.println(new EvalSegmentation().process(result, regions));
//
//			result = (Image) new MergeUsingWatershed().process(result, source, 0.05);
//
//			new Viewer2D().process(new DrawFrontiersOnImage().process(source,
//					new FrontiersFromSegmentation().process(result)), "" + file
//					+ " after region merging 3");
//			System.out.println(new EvalSegmentation().process(result, regions));
//
//			result = (Image) new MergeUsingWatershed().process(result, source, 0.05);
//
//			new Viewer2D().process(new DrawFrontiersOnImage().process(source,
//					new FrontiersFromSegmentation().process(result)), "" + file
//					+ " after region merging 4");
//			System.out.println(new EvalSegmentation().process(result, regions));
//
//		} catch (InvalidTypeOfParameterException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (AlgorithmException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InvalidNumberOfParametersException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

}
