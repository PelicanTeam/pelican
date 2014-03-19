package fr.unistra.pelican.algorithms.applied.remotesensing;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.InvalidNumberOfParametersException;
import fr.unistra.pelican.InvalidTypeOfParameterException;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.io.SamplesLoader;
import fr.unistra.pelican.algorithms.segmentation.labels.DrawFrontiersOnImage;
import fr.unistra.pelican.algorithms.segmentation.labels.FrontiersFromSegmentation;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToColorByMeanValue;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;


/** 
 * Create regions using the watershed algorithm then a classification using 5NN.
 * Settings are :
 * - hmin : reduction value of the gradient image to limit oversegmentation
 * - samples : training set, boolean image, each band is for a class, white pixel are samples.
 * 
 * @author Sebastien Derivaux 
 */
public class RegionBuilderWatershedClassification extends Algorithm {
	
	// Inputs parameters
	public Image inputImage;
	public double hmin;
	public Image samples;
	
	// Outputs parameters
	public Image outputImage;

	/**
  	 * Constructor
  	 *
  	 */
	public RegionBuilderWatershedClassification() {		
		
		super();		
		super.inputs = "inputImage,hmin,samples";		
		super.outputs = "outputImage";		
		
	}
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {

		Image work;
		try {
		
			work = (Image) new RegionBuilderWatershedClassical().process(inputImage, hmin);
			
			work = (Image) new LabelsToColorByMeanValue().process(work, inputImage);
		
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
	 * Create regions using the watershed algorithm then a classification using 5NN.
	 * Settings are :
	 * @param hmin : reduction value of the gradient image to limit oversegmentation
	 * @param samples : training set, boolean image, each band is for a class, white pixel are samples.
	 */
	public static Image exec(Image inputImage, double hmin, Image samples) {
		return (Image)new RegionBuilderWatershedClassification().process(inputImage, samples, hmin);
		
	}
	
	public static void main(String[] args) {
		String file = "./samples/remotesensing1";
		if(args.length > 0)
			file = args[0];
		
		try {
			// Load the image
			Image source = (Image) new ImageLoader().process(file + ".png");
			Image samples = (Image) new SamplesLoader().process(file);
	
			// Create regions
			Image result = (Image) new RegionBuilderWatershedClassification().process(source, 0.2, samples);

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