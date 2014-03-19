package fr.unistra.pelican.algorithms.segmentation.labels;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.IntegerImage;

/**
 * This class returns a boolean image representing where 
 * the value is equal to the specific label
 * 
 * @author Jonathan Weber
 */
public class FromSpecificLabelToBooleanImage extends Algorithm {

	/**
	 * Input image
	 */
	public IntegerImage inputImage;
	
	/**
	 * Label under consideration
	 */
	public int specificLabel;

	/**
	 * Output image
	 */
	public BooleanImage outputImage;


	/**
	 * Constructor
	 * 
	 */
	public FromSpecificLabelToBooleanImage() {

		super.inputs = "inputImage,specificLabel";
		super.outputs = "outputImage";

	}


	public static BooleanImage exec(IntegerImage inputImage, int specificLabel) {
		return (BooleanImage) new FromSpecificLabelToBooleanImage().process(inputImage,specificLabel);
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
			if(inputImage.getPixelInt(i)==specificLabel)
			{
				outputImage.setPixelBoolean(i, true);
			} else
			{
				outputImage.setPixelBoolean(i, false);
			}
		}

	}

}
