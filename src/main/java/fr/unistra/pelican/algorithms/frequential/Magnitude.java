/**
 * 
 */
package fr.unistra.pelican.algorithms.frequential;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;

/**
 * Compute magnitude image from real and imaginary part;
 * 
 * By default, result is put in inputImageRe, you can change this behavior with option "inPplace"
 * 
 * @author Benjamin Perret
 *
 */
public class Magnitude extends Algorithm {

	/**
	 * Input image real part
	 */
	public DoubleImage inputImageRe;
	
	/**
	 * Input image im part
	 */
	public DoubleImage inputImageIm;
	
	/**
	 * Input image im part
	 */
	public DoubleImage outputImage;
	
	/**
	 * if option is true, result is put in inputImageRe. If false new space is alocated for result
	 */
	public boolean inPlace=true;
	
	/**
	 * 
	 */
	public Magnitude() {
		super.inputs="inputImageRe,inputImageIm";
		super.options="inPlace";
		super.outputs="outputImage";
	}

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	@Override
	public void launch() throws AlgorithmException {
		if(!Image.haveSameDimensions(inputImageIm, inputImageRe))
			throw new AlgorithmException("Images must have same dimensions!");
		
		if(inPlace)
			outputImage=inputImageRe;
		else outputImage=(DoubleImage)inputImageRe.copyImage(false);
		
		for(int i=0;i<inputImageIm.size();i++)
		{
			double a= inputImageIm.getPixelDouble(i);
			double b= inputImageRe.getPixelDouble(i);
			outputImage.setPixelDouble(i, Math.sqrt(a*a+b*b));
		}



	}

	/**
	 * Compute magnitude image from real and im part
	 * result is put in real input image
	 * @param inputImageRe real image
	 * @param inputImageIm imaginary image
	 * @return magnitude 
	 */
	public static DoubleImage exec(DoubleImage inputImageRe, DoubleImage inputImageIm)
	{
		return (DoubleImage)(new Magnitude()).process(inputImageRe,inputImageIm);
	}
	
	/**
	 * Compute magnitude image from real and im part
	 * result is put in real input image
	 * @param inputImageRe real image
	 * @param inputImageIm imaginary image
	 * @param inPlace, set inPlace parameter
	 * @return magnitude 
	 */
	public static DoubleImage exec(DoubleImage inputImageRe, DoubleImage inputImageIm, boolean inPlace)
	{
		return (DoubleImage)(new Magnitude()).process(inputImageRe,inputImageIm, inPlace);
	}
	
	/**
	 * Compute magnitude image from real and im part
	 * result is put in real input image
	 * @param inputImages first element is real part, second is imaginary part
	 * @return magnitude
	 */
	public static DoubleImage exec(DoubleImage [] inputImages)
	{
		return (DoubleImage)(new Magnitude()).process(inputImages[0],inputImages[1]);
	}
	
	/**
	 * Compute magnitude image from real and im part
	 * result is put in real input image
	 * @param inputImages first element is real part, second is imaginary part
	 * @param inPlace, set inPlace parameter
	 * @return magnitude
	 */
	public static DoubleImage exec(DoubleImage [] inputImages,boolean inPlace)
	{
		return (DoubleImage)(new Magnitude()).process(inputImages[0],inputImages[1],inPlace);
	}
	
}
