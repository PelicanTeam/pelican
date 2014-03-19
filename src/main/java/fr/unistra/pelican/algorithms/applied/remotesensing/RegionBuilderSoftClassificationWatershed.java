package fr.unistra.pelican.algorithms.applied.remotesensing;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.InvalidNumberOfParametersException;
import fr.unistra.pelican.InvalidTypeOfParameterException;
import fr.unistra.pelican.algorithms.histogram.ContrastStretch;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.io.SamplesLoader;
import fr.unistra.pelican.algorithms.morphology.gray.GrayMedian;
import fr.unistra.pelican.algorithms.segmentation.labels.DrawFrontiersOnImage;
import fr.unistra.pelican.algorithms.segmentation.labels.FrontiersFromSegmentation;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToColorByMeanValue;
import fr.unistra.pelican.algorithms.segmentation.regionMerging.MergeUsingWatershed;
import fr.unistra.pelican.algorithms.segmentation.weka.WekaSoftClassification5NN;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;


/** 
 * Create regions using a soft 5NN algorithm then watershed algorithm.
 * Settings are :
 * - samples : training set, boolean image, each band is for a class, white pixel are samples.
 * - hmin : reduction value of the gradient image to limit oversegmentation
 * - dynamics : reduction value of the gradient image to limit oversegmentation
 * - merging : connected regions with a Euclidian difference in spectral values less than merging are merged
 * @author Sebastien Derivaux
 */
public class RegionBuilderSoftClassificationWatershed extends Algorithm {

	// Inputs parameters
	public Image inputImage;
	public Image samples;
	public double hmin;
	public double dynamics;
	public double merging;

	// Outputs parameters
	public Image outputImage;

	/**
	 * Constructor
	 *
	 */
	public RegionBuilderSoftClassificationWatershed() {		

		super();		
		super.inputs = "inputImage,samples,hmin,dynamics,merging";		
		super.outputs = "outputImage";		

	}

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {

		Image work;
		try {

			Image proba = (Image) new WekaSoftClassification5NN().process(inputImage, samples);

			proba = (Image) new ContrastStretch().process(proba);

			BooleanImage se = FlatStructuringElement2D.createSquareFlatStructuringElement(5);
			proba = (Image) new GrayMedian().process(proba, se);

			//Viewer2D.exec(proba, "probas");
			work = RegionBuilderWatershedClassical.exec(proba, hmin, dynamics);

			// Another filtering step
			work = MergeUsingWatershed.exec(work, proba, merging);

			outputImage = work;

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
	 * Create regions using a soft 5NN algorithm then watershed algorithm.
	 * Settings are :
	 * @param samples : training set, boolean image, each band is for a class, white pixel are samples.
	 * @param hmin : reduction value of the gradient image to limit oversegmentation
	 * @param dynamics : reduction value of the gradient image to limit oversegmentation
	 * @param merging : connected regions with a Euclidian difference in spectral values less than merging are merged
	 */
	public static Image exec(Image image, Image samples, double hmin, double dynamics, double merging) throws InvalidTypeOfParameterException, AlgorithmException, InvalidNumberOfParametersException {
		RegionBuilderSoftClassificationWatershed algo = new RegionBuilderSoftClassificationWatershed();
		algo.inputImage = image;
		algo.samples = samples;
		algo.hmin = hmin;
		algo.dynamics = dynamics;
		algo.merging = dynamics;
		algo.launch();
		return algo.outputImage;		
	}

	public static void main(String[] args) {
		String file = "./samples/remotesensing1";
		if(args.length > 0)
			file = args[0];
		BooleanImage se3 = FlatStructuringElement2D.createSquareFlatStructuringElement(3);


		try {
			// Load the image
			Image source = (Image) new ImageLoader().process(file + ".png");
			Image samples = (Image) new SamplesLoader().process(file);


			source = (Image) new GrayMedian().process(source, se3);

			// Create regions
			Image result = (Image) new RegionBuilderSoftClassificationWatershed().process(source, samples, 0.2);

			// View it
			new Viewer2D().process(new DrawFrontiersOnImage().process(source, new FrontiersFromSegmentation().process(result)), "Segmentation of " + file);
			new Viewer2D().process(new LabelsToColorByMeanValue().process(result, source), "Segmentation of " + file);


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