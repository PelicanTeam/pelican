package fr.unistra.pelican.algorithms.applied.remotesensing;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.InvalidNumberOfParametersException;
import fr.unistra.pelican.InvalidTypeOfParameterException;
import fr.unistra.pelican.algorithms.histogram.ContrastStretch;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.io.RegionsLoader;
import fr.unistra.pelican.algorithms.io.SamplesLoader;
import fr.unistra.pelican.algorithms.morphology.gray.GrayMedian;
import fr.unistra.pelican.algorithms.segmentation.EvalSegmentation;
import fr.unistra.pelican.algorithms.segmentation.labels.DrawFrontiersOnImage;
import fr.unistra.pelican.algorithms.segmentation.labels.FilteringLabels;
import fr.unistra.pelican.algorithms.segmentation.labels.FrontiersFromSegmentation;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToColorByMeanValue;
import fr.unistra.pelican.algorithms.segmentation.weka.WekaSoftClassification5NN;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;


/** 
 * Create regions using a soft 5NN algorithm then watershed algorithm, then a supervised region classification.
 * Settings are :
 * - samples : training set, boolean image, each band is for a class, white pixel are samples.
 * - hmin : reduction value of the gradient image to limit oversegmentation
 * 
 * @author Sebastien Derivaux 
 */
public class RegionBuilderSoftClassificationWatershedClassification extends Algorithm {
	
	// Inputs parameters
	public Image inputImage;
	public Image samples;
	public double threshold;
	
	// Outputs parameters
	public Image outputImage;

	/**
  	 * Constructor
  	 *
  	 */
	public RegionBuilderSoftClassificationWatershedClassification() {		
		
		super();		
		super.inputs = "inputImage,samples,threshold";		
		super.outputs = "outputImage";		
		
	}
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {

		Image work;
		try {
						
			work = (Image) new WekaSoftClassification5NN().process(inputImage, samples);
			
			Image probas = (Image) new ContrastStretch().process(work);

			work = (Image) new RegionBuilderWatershedClassical().process(probas, threshold);
				
			
			BooleanImage labelFilter = FlatStructuringElement2D.createSquareFlatStructuringElement(5);
			work = (Image) new FilteringLabels().process(work, labelFilter);
			
			
			work = (Image) new LabelsToColorByMeanValue().process(work, probas);		
			work = (Image) new RegionBuilderClassificationConnexity().process(work, samples);
			
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
	 * Create regions using a soft 5NN algorithm then watershed algorithm, then a supervised region classification.
	 * Settings are :
	 * @param samples : training set, boolean image, each band is for a class, white pixel are samples.
	 * @param threshold hmin : reduction value of the gradient image to limit oversegmentation
	 */
	public Image exec(Image inputImage, Image samples, double threshold) {
		return (Image)new RegionBuilderSoftClassificationWatershedClassification().process(inputImage, samples, threshold);
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
			Image regions = (Image) new RegionsLoader().process(file);

			System.out.println("RegionBuilderSoftClassificationWatershed of " + file);

			source = (Image) new GrayMedian().process(source, se3);
			
			// Create regions
			Image result = (Image) new RegionBuilderSoftClassificationWatershedClassification().process(source, samples, 0.75);

			
			// View it
			new Viewer2D().process(new DrawFrontiersOnImage().process(source, new FrontiersFromSegmentation().process(result)), "RegionBuilderSoftClassificationWatershedClassification of " + file);
			new Viewer2D().process(new LabelsToColorByMeanValue().process(result, source), "RegionBuilderSoftClassificationWatershedClassification of " + file);
			System.out.println(new EvalSegmentation().process(result, regions));

			
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
