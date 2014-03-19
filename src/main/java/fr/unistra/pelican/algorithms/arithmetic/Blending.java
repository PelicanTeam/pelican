package fr.unistra.pelican.algorithms.arithmetic;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.InvalidParameterException;

/**
 * Blend two image with a blending coeficient. 
 * outputPixel = inputPixel1 * coef + inputPixel2 * (1.0 - coef) 
 * The outputImage format is the same as inputImage1.
 * 
 * 
 * @author ?,Jonathan Weber, Benjamin Perret
 */
public class Blending extends Algorithm {

	/**
	 * First input image.
	 */
	public Image inputImage1;

	/**
	 * Second input image.
	 */
	public Image inputImage2;

	/**
	 * Blending coefficient
	 */
	public double coef;
	
	
	/**
	 * Ignore pixels of image 2 which values in all bands are equal to 0. This option is for markers. 
	 */
	public boolean ignoreBackgroundOfInputImage2=false;
	
	/**
	 * Blended image
	 */
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public Blending() {

		super();
		super.inputs = "inputImage1,inputImage2,coef";
		super.options = "ignoreBackgroundOfInputImage2";
		super.outputs = "outputImage";
	
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		if(!Image.haveSameDimensions(inputImage1, inputImage2))
			throw(new InvalidParameterException("The two image must have the same dimensions"));
		else
		{
			outputImage = inputImage1.copyImage(false);
			outputImage.setMask( inputImage1.getMask() );
			if(!ignoreBackgroundOfInputImage2)
			{
				int size = inputImage1.size();
				for (int i = 0; i < size; ++i)
					if ( inputImage2.isPresent(i) )
					outputImage.setPixelDouble(i,
						(inputImage1.getPixelDouble(i) * coef + inputImage2
							.getPixelDouble(i)* (1.0 - coef)));
					else outputImage.setPixelDouble( i, inputImage1.getPixelDouble(i)*coef );
			}
			else
			{
				
				for(int x=0;x<inputImage1.getXDim();x++)
					for(int y=0;y<inputImage1.getYDim();y++)
						for(int z=0;z<inputImage1.getZDim();z++)
							for(int t=0;t<inputImage1.getTDim();t++)
							{
								boolean considered=false;
								for(int b=0;b<inputImage1.getBDim();b++)
									if ( inputImage2.isPresent(x,y,z,t,b) )
									if(inputImage2.getPixelDouble(x,y,z,t,b)>0)
										considered=true;
								if(considered)
									for(int b=0;b<inputImage1.getBDim();b++)
										outputImage.setPixelDouble(x,y,z,t,b,(inputImage1.getPixelDouble(x,y,z,t,b) * coef + inputImage2.getPixelDouble(x,y,z,t,b)* (1.0 - coef)));
								else
									for(int b=0;b<inputImage1.getBDim();b++)
										outputImage.setPixelDouble(x,y,z,t,b,inputImage1.getPixelDouble(x,y,z,t,b));
									
							}
			}

		}
	}
	
	/**
	 * Blend two image with a blending coeficient. 
	 * outputPixel = inputPixel1 * coef + inputPixel2 * (1.0 - coef) 
	 * The outputImage format is the same as inputImage1.
	 * 
	 * @param inputImage1
	 *            First of the two additioned images.
	 * @param inputImage2
	 *            Second of the two additioned images.
	 * @param coef Blending coefficient
	 * @param ignoreBackgroundOfInputImage2 option to ignore background
	 * @return outputImage blends of the 2 input images with respect to coef.
	 */
	public static Image exec(Image inputImage1, Image inputImage2, double coef,boolean ignoreBackgroundOfInputImage2) {
		return (Image) new Blending().process(inputImage1,
				inputImage2,coef,ignoreBackgroundOfInputImage2);
	}
	
	/**
	 * Blend two image with a blending coeficient. 
	 * outputPixel = inputPixel1 * coef + inputPixel2 * (1.0 - coef) 
	 * The outputImage format is the same as inputImage1.
	 * 
	 * @param inputImage1
	 *            First of the two additioned images.
	 * @param inputImage2
	 *            Second of the two additioned images.
	 * @param coef Blending coefficient
	 * @return outputImage blends of the 2 input images with respect to coef.
	 */
	public static Image exec(Image inputImage1, Image inputImage2, double coef) {
		return (Image) new Blending().process(inputImage1,
				inputImage2,coef);
	}
}
