package fr.unistra.pelican.algorithms.applied.remotesensing;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.InvalidNumberOfParametersException;
import fr.unistra.pelican.InvalidTypeOfParameterException;
import fr.unistra.pelican.algorithms.segmentation.flatzones.BooleanConnectedComponentsLabeling;
import fr.unistra.pelican.algorithms.segmentation.weka.WekaClassification5NN;


/** 
 * Create regions using the 5NN classification algorithm on original image.
 * Settings are :
 * - samples : training set, boolean image, each band is for a class, white pixel are samples.
 * 
 * @author Sbastien Derivaux, Jonathan Weber
 */
public class RegionBuilderClassificationConnexity extends Algorithm {
	
	/**
	 * Image to be processed
	 */
	public Image inputImage;
	
	/**
	 * Training set image, each band is for a class
	 */
	public Image samples;
	
	/**
	 * Resulting picture
	 */
	public Image outputImage;

	/**
  	 * Constructor
  	 *
  	 */
	public RegionBuilderClassificationConnexity() {		
		
		super();		
		super.inputs = "inputImage,samples";		
		super.outputs = "outputImage";		
		
	}
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {

		try {
						
			outputImage = (Image) new WekaClassification5NN().process(inputImage, samples);
			
			outputImage = (Image) new BooleanConnectedComponentsLabeling().process(outputImage);
			
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
	 * This method creates regions using the 5NN classification algorithm on original image.
	 * @param inputImage Image to be processed
	 * @param samples Training set
	 * @return 5NN classified picture
	 */
	public static Image exec (Image inputImage, Image samples)
	{
		return (Image) new RegionBuilderClassificationConnexity().process(inputImage,samples);
	}
	
}
