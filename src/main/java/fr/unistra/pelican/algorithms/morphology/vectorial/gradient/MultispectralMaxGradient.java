package fr.unistra.pelican.algorithms.morphology.vectorial.gradient;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.morphology.gray.GrayGradient;

/**
 * This class computes the gradient of a multichannel image by taking the maximum of the
 * marginal gradients
 * 
 * @author Abdullah
 * 
 */
public class MultispectralMaxGradient extends Algorithm
{
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
	 * This class computes the gradient of a multichannel image by taking the maximum of themarginal gradients
	 * 
	 * @param image the input image
	 * @param se the structuring element
	 * @return the output image
	 */
	public static Image exec(Image inputImage,BooleanImage se)
	{
		return (Image) new MultispectralMaxGradient().process(inputImage,se);
	}

	/**
	 * Constructor
	 * 
	 */
	public MultispectralMaxGradient() {

		super();
		super.inputs = "inputImage,se";
		super.outputs = "outputImage";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException
	{
		//outputImage=new ByteImage(inputImage.getXDim(), inputImage.getYDim(),1,1,1);
		outputImage = inputImage.newInstance(inputImage.getXDim(), inputImage.getYDim(),inputImage.getZDim(),inputImage.getTDim(),1);

		try {

			Image gradient = (Image) new GrayGradient().process(inputImage, se);
			for ( int t = 0 ; t < inputImage.getTDim() ; t++ ) 
				for ( int z = 0 ; z < inputImage.getZDim() ; z++ ) 
					for ( int x = 0 ; x < inputImage.getXDim() ; x++ ) { 
						for ( int y = 0 ; y < inputImage.getYDim() ; y++ ) { 

							double max = -1 * Double.MAX_VALUE;
							for ( int b = 0 ; b < inputImage.getBDim() ; b++ ) { 

								if ( inputImage.isPresent( x,y,z,t,b ) ) { 

									double tmp = gradient.getPixelXYZTBDouble( x,y,z,t,b );
									if (tmp > max) max = tmp;
								}
							}
							outputImage.setPixelXYZTDouble( x,y,z,t, max );
						}
					}

		} catch ( PelicanException e ) { e.printStackTrace(); }
	}
}
