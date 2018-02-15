package fr.unistra.pelican.algorithms.morphology.vectorial.gradient;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.morphology.gray.GrayGradient;
/**
 * This class computes the gradient of a Lab image
 * 
 * @author Jonathan Weber
 * 
 */
public class LabGradient extends Algorithm {
	/**
	 * the input image
	 */
	public Image inputImage;

	/**
	 * the structuring element
	 */
	public BooleanImage se;

	/**
	 * the output image
	 */
	public Image outputImage;

	/**
	 * This class computes the gradient of a Lab image
	 * 
	 * @param inputImage
	 *          the input image
	 * @param se
	 *          the structuring element
	 * @return the output image
	 */
	public static Image exec(Image inputImage, BooleanImage se) {
		return (Image) new LabGradient().process(inputImage, se);
	}

	/**
	 * Constructor
	 * 
	 */
	public LabGradient() {

		super();
		super.inputs = "inputImage,se";
		super.outputs = "outputImage";

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = inputImage.newInstance(inputImage.getXDim(), inputImage
			.getYDim(), inputImage.getZDim(), inputImage.getTDim(), 1);

		try {
			Image gradient = (Image) new GrayGradient().process(inputImage, se);
			for ( int t = 0 ; t < inputImage.getTDim() ; t++ ) 
			for ( int z = 0 ; z < inputImage.getZDim() ; z++ ) 
			for ( int y = 0 ; y < inputImage.getYDim() ; y++ ) 
			for ( int x = 0 ; x < inputImage.getXDim() ; x++ ) { 

				double tmp = 0.0;
				for ( int b = 0 ; b < inputImage.getBDim() ; b++ ) 
					if ( inputImage.isPresent( x,y,z,t,b ) ) { 

						double tmp2 = gradient.getPixelDouble(x, y, z, t, b);
						tmp += tmp2 * tmp2;
					}
				tmp = Math.sqrt(tmp);
				if(tmp>1.)
					tmp=1.;
				outputImage.setPixelXYZTDouble(x, y, z, t, tmp);
			}

		} catch (PelicanException e) { e.printStackTrace(); }
	}

}
