package fr.unistra.pelican.algorithms.applied.remotesensing;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.InvalidNumberOfParametersException;
import fr.unistra.pelican.InvalidTypeOfParameterException;
import fr.unistra.pelican.algorithms.histogram.ContrastStretch;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.morphology.gray.GrayMedian;
import fr.unistra.pelican.algorithms.segmentation.labels.DrawFrontiersOnImage;
import fr.unistra.pelican.algorithms.segmentation.labels.FrontiersFromSegmentation;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToColorByMeanValue;
import fr.unistra.pelican.algorithms.segmentation.weka.WekaSoftSegmentationEM;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;


/** 
 * Create regions using a soft EM algorithm then watershed algorithm.
 * Settings are :
 * - nbClusters : number of cluster to make on the clustering phase.
 * - hmin : reduction value of the gradient image to limit oversegmentation
 * 
 * @author Sebastien Derivaux
 */
public class RegionBuilderSoftClusteringWatershed extends Algorithm {
	
	// Inputs parameters
	public Image inputImage;
	public int nbClusters;
	public double threshold;
	
	// Outputs parameters
	public Image outputImage;

	
	/**
  	 * Constructor
  	 *
  	 */
	public RegionBuilderSoftClusteringWatershed() {		
		
		super();		
		super.inputs = "inputImage,threshold,nbClusters";		
		super.outputs = "outputImage";		
		
	}
	
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {

		Image work;
		try {
						
			work = (Image) new WekaSoftSegmentationEM().process(inputImage, nbClusters);
			
			work = (Image) new ContrastStretch().process(work);

			work = RegionBuilderWatershedClassical.exec(work, threshold/2, threshold);
				
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
	 * Create regions using a soft EM algorithm then watershed algorithm.
	 * Settings are :
	 * @param nbClusters : number of cluster to make on the clustering phase.
	 * @param threshold (hmin) : reduction value of the gradient image to limit oversegmentation
	 */
	public Image exec(Image inputImage, double threshold, int nbClusters) {
		return (Image)new  RegionBuilderSoftClusteringWatershed().process(inputImage, threshold, nbClusters);
	}

	public static void main(String[] args) {
		String file = "./samples/remotesensing1.png";
		if(args.length > 0)
			file = args[0];
		BooleanImage se3 = FlatStructuringElement2D.createSquareFlatStructuringElement(3);

		
		try {
			// Load the image
			Image source = (Image) new ImageLoader().process(file);
	
			
			source = (Image) new GrayMedian().process(source, se3);
			
			// Create regions
			Image result = (Image) new RegionBuilderSoftClusteringWatershed().process(source, 4, 0.4);

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