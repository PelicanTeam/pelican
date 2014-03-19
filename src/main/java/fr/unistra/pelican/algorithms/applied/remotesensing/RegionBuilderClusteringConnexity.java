package fr.unistra.pelican.algorithms.applied.remotesensing;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.InvalidNumberOfParametersException;
import fr.unistra.pelican.InvalidTypeOfParameterException;
import fr.unistra.pelican.algorithms.segmentation.flatzones.BooleanConnectedComponentsLabeling;
import fr.unistra.pelican.algorithms.segmentation.weka.WekaSegmentationKmeans;



/** 
 * Create regions using Kmeans algorithm on original image.
 * 
 * @author Sbastien Derivaux, Jonathan Weber
 * 
 */
public class RegionBuilderClusteringConnexity extends Algorithm {
	
	/**
	 * Image to be processed
	 */
	public Image inputImage;
	
	/**
	 * Number of clusters
	 */
	public int nbClusters;
	
	/**
	 * Resulting clustering
	 */
	public Image outputImage;

	
	/**
  	 * Constructor
  	 *
  	 */
	public RegionBuilderClusteringConnexity() {		
		
		super();		
		super.inputs = "inputImage,nbClusters";		
		super.outputs = "outputImage";		
		
	}
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {

		Image work;
		try {
						
			work = (Image) new  WekaSegmentationKmeans().process(inputImage, nbClusters);

			work = (Image) new BooleanConnectedComponentsLabeling().process(work);
				
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
	 * Create regions using Kmeans algorithm on original image.
	 * @param inputImage image to be processed
	 * @param nbClusters number of clusters
	 * @return K-Means classification
	 */
	public static Image exec (Image inputImage, Integer nbClusters)
	{
		return (Image) new RegionBuilderClusteringConnexity().process(inputImage,nbClusters);
	}
}