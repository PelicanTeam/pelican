package fr.unistra.pelican.algorithms.arithmetic;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.mask.MaskStack;

/**
 * Combine many images with weighting coefficients. 
 * outputPixel = inputPixel1 * coef1 + inputPixel2 * coef2 + ... + inputPixeln * coefn 
 * 
 * Works on double precision.
 * The outputImage format is DoubleImage
 * 
 * @author  ?, Benjamin Perret
 */
public class LinearCombination extends Algorithm {

	/**
	 * Array of input images
	 */
	public Image[] inputImage;

	/**
	 * Array of weighting coefficients
	 */
	public Double[] coef;

	/**
	 * Combinaison image
	 */
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public LinearCombination() {
		super.inputs = "inputImage,coef";
		super.outputs = "outputImage";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		// check data types
		if (inputImage.length == 0 || coef.length == 0
				|| inputImage.length != coef.length)
			throw new AlgorithmException("Arrays are not of appropriate size");
		for (int k = 1; k < inputImage.length; k++)
			if (!Image.haveSameDimensions(inputImage[0], inputImage[k]))
				throw new AlgorithmException("Images are not all comparable");
		// generate output
		outputImage = new DoubleImage(inputImage[0].getXDim(), inputImage[0]
				.getYDim(), inputImage[0].getZDim(), inputImage[0].getTDim(),
				inputImage[0].getBDim());
		int size = inputImage[0].size();
		int nb = inputImage.length;
		double val;
		for (int i = 0; i < size; ++i) {
			val = 0;
			for (int k = 0; k < nb; k++)
				if ( inputImage[k].isPresent(i) )
				val += inputImage[k].getPixelDouble(i) * coef[k];
			outputImage.setPixelDouble(i, val);
		}

		MaskStack mask = new MaskStack( MaskStack.AND );
		for ( int k = 0 ; k < nb ; k++ ) mask.push( this.inputImage[k].getMask() );
		this.outputImage.setMask( mask );
	}
	
	/**
	 * Combine many images with weighting coefficients. 
	 * outputPixel = inputPixel1 * coef1 + inputPixel2 * coef2 + ... + inputPixeln * coefn 
	 * 
	 * Works on double precision.
	 * The outputImage format is DoubleImage
	 * 
	 * @param inputImage Array of images to combine
	 * @param coef Array of weights
	 * @return Linear combination of inputs images
	 */
	public static Image exec(Image [] inputImage, Double [] coef) {
		return (Image)new LinearCombination().process(inputImage, coef);
	}
}
