package fr.unistra.pelican.algorithms.applied.remotesensing;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.InvalidNumberOfParametersException;
import fr.unistra.pelican.InvalidTypeOfParameterException;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.segmentation.labels.DrawFrontiersOnImage;
import fr.unistra.pelican.algorithms.segmentation.labels.FrontiersFromSegmentation;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToColorByMeanValue;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;


/** 
 * Create regions using the watershed algorithm then a clustering algorithm.
 * Settings are :
 * - hmin : reduction value of the gradient image to limit oversegmentation
 * - nbClusters : number of cluster for the clustering phase.
 * 
 * @author Sebastien Derivaux
 */
public class RegionBuilderWatershedClustering extends Algorithm {
	
	// Inputs parameters
	public Image inputImage;
	public double hmin;
	public int nbClusters;
	
	// Outputs parameters
	public Image outputImage;

	/**
  	 * Constructor
  	 *
  	 */
	public RegionBuilderWatershedClustering() {		
		
		super();		
		super.inputs = "inputImage,hmin,nbClusters";		
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
			
			work = (Image) new RegionBuilderClusteringConnexity().process(work, nbClusters);
			
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
	 * Create regions using the watershed algorithm then a clustering algorithm.
	 * Settings are :
	 * @param hmin : reduction value of the gradient image to limit oversegmentation
	 * @param nbClusters : number of cluster for the clustering phase.
	 */
	public Image exec(Image inputImage, double hmin, int nbClusters) {
		return (Image)new RegionBuilderWatershedClustering().process(inputImage, hmin, nbClusters);
	}
	
	public static void main(String[] args) {
		String file = "./samples/remotesensing1.png";
		if(args.length > 0)
			file = args[0];
		
		
		try {
			// Load the image
			Image source = (Image) new ImageLoader().process(file);
	
			// Create regions
			Image result = (Image) new RegionBuilderWatershedClustering().process(source, 0.20, 8);

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