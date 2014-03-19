package fr.unistra.pelican.algorithms.morphology.soft;




import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.morphology.GrayIntStructuringElement;


/**
 * Perform a soft OCCO filter with a flat structuring element.
 * 
 * @author Benjamin Perret
 *
 */
public class SoftOCCO extends Algorithm {
	/**
	 * Input image
	 */
	private Image inputImage;
	
	/**
	 * Rank Order
	 */
	private int seuil;
	
	/**
	 * Flat Structuring Element and weight map
	 */
	private GrayIntStructuringElement se;

	/**
	 * Result
	 */
	private Image outputImage;

	/**
	 * Default constructor
	 */
	public SoftOCCO()
	{
		super.inputs="inputImage,se,seuil";
		super.outputs="outputImage";
	}
	
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = SoftOpening.exec(inputImage, se, seuil);
		outputImage = SoftClosing.exec(outputImage, se, seuil);
		
		Image tmp=SoftClosing.exec(inputImage, se, seuil);
		tmp=SoftOpening.exec(tmp, se, seuil);
		
		
		for (int i = 0; i < inputImage.size(); i++) { 

			if ( !inputImage.isPresent(i) ) continue;

			double p1 = outputImage.getPixelDouble(i);
			double p2 = tmp.getPixelDouble(i);
			outputImage.setPixelDouble(i,(p1 + p2) / 2);
		}
	}

	
}