package fr.unistra.pelican.algorithms.segmentation.labels;

import java.util.ArrayList;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.IntegerImage;

/**
 * This class returns a boolean image representing where 
 * the value is equal to the specific labels
 * 
 * TODO : optimized
 * 
 * @author Jonathan Weber
 */
public class FromSpecificLabelsToBooleanImage extends Algorithm {

	/**
	 * Input image
	 */
	public IntegerImage inputImage;
	
	/**
	 * Labels under consideration
	 */
	public ArrayList<Integer> specificLabels;

	/**
	 * Output image
	 */
	public BooleanImage outputImage;


	/**
	 * Constructor
	 * 
	 */
	public FromSpecificLabelsToBooleanImage() {

		super.inputs = "inputImage,specificLabels";
		super.outputs = "outputImage";

	}


	public static BooleanImage exec(IntegerImage inputImage, ArrayList<Integer> specificLabel) {
		return (BooleanImage) new FromSpecificLabelsToBooleanImage().process(inputImage,specificLabel);
	}



	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */

	public void launch() throws AlgorithmException {
		outputImage = new BooleanImage(inputImage.getXDim(),inputImage.getYDim(),inputImage.getZDim(),inputImage.getTDim(),1);
		for(int i=0;i<outputImage.size();i++)
		{
			if(specificLabels.contains(inputImage.getPixelInt(i)))
			{
				outputImage.setPixelBoolean(i, true);
			} else
			{
				outputImage.setPixelBoolean(i, false);
			}
		}

	}

}
