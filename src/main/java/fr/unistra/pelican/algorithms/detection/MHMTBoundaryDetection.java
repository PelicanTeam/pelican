package fr.unistra.pelican.algorithms.detection;

/**
 * Class for the extraction of boundary between two objects
 * 
 * @author Jonathan Weber
 * 
 */

import java.util.ArrayList;
import java.util.Arrays;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.histogram.ContrastStretchEachBands;
import fr.unistra.pelican.algorithms.morphology.vectorial.hitormiss.MHMT;
import fr.unistra.pelican.util.detection.MHMTDetectionParameters;
import fr.unistra.pelican.util.morphology.ValuedMonoBandFlatStructuringElement;

public class MHMTBoundaryDetection extends Algorithm {

	/**
	 * Image to compute
	 */
	public Image inputImage;

	/**
	 * Array of parameters for the MHMTBoundaryDetection
	 */

	public ArrayList<MHMTDetectionParameters> mhmtdp;
	
	/**
	 * Step of the rotation of the MHMT
	 */
	public double rotationStep=30.;

	/**
	 * Resulting image
	 */
	public Image outputImage;


	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException 
	{
		
		ValuedMonoBandFlatStructuringElement[] vmbfse = new ValuedMonoBandFlatStructuringElement[mhmtdp.size()];

		// Optimisation : tri des ES par leur taille
		MHMTDetectionParameters[]mhmtdp2=mhmtdp.toArray(new MHMTDetectionParameters[]{});
		Arrays.sort(mhmtdp2);
		
		
		for(int i=0;i<mhmtdp2.length;i++)
			vmbfse[i]=mhmtdp2[i].getValuedMonoBandFlatStructuringElement();
		
		//MHMT processing
		outputImage = (Image) new MHMT().process(inputImage, vmbfse, rotationStep);

		//Stretching the result for better view and use
		outputImage = (Image) new ContrastStretchEachBands().process(outputImage);
		
	}
	
	

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#setInput(java.util.Vector)
	/**
	 * Constructor
	 * 
	 */

	public MHMTBoundaryDetection() {

		super();
		super.inputs = "inputImage,mhmtdp";
		super.options = "rotationStep";
		super.outputs = "outputImage";
		
	}
	/**
	 * Method for the extraction of boundary between two objects
	 * @param inputImage Image to be computed
	 * @param mhmtdp Array of parameters for the MHMTBoundaryDetection
	 * @return Image with boundary
	 */
	public static Image exec(Image inputImage, ArrayList<MHMTDetectionParameters> mhmtdp)
	{
		return (Image) new MHMTBoundaryDetection().process(inputImage,mhmtdp);
	}

}