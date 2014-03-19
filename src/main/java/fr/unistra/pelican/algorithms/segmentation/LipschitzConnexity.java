package fr.unistra.pelican.algorithms.segmentation;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.morphology.generalGray.GeneralGrayDilation;
import fr.unistra.pelican.algorithms.morphology.generalGray.GeneralGrayErosion;
import fr.unistra.pelican.util.Tools;
import fr.unistra.pelican.util.morphology.GrayStructuringElement;

/**
 * Implementation of the LipschitzConnexity presented in J. Serra
 * A Lattice Approach to Image Segmentation, Journal of Mathematical 
 * Imaging and Vision, Volume 24 ,  Issue 1,  (January 2006)
 * Pages: 83 - 130   
 * @author Jonathan Weber, Benjamin Perret (Universal Brocking-Class Heroe)
 */

public class LipschitzConnexity extends Algorithm {

	/**
	 * Image to be processed
	 */
	public Image inputImage;
	
	/**
	 * Slope of the cone
	 */
	public double slope;
	
	/**
	 * Radius of the cone
	 */
	public int radius;
	
	/**
	 * Type of the Lipschitz connexity
	 */
	public int type=byErosion;
	
	/**
	 * LipschitzConnexity Image
	 */
	public Image outputImage;
	
	public static final int byErosion = 0;
	public static final int byOpening = 1;
	
	public LipschitzConnexity()
	{
		super.inputs = "inputImage,slope,radius";
		super.options = "type";
		super.outputs = "outputImage";
	}
	
	
	@Override
	public void launch() throws AlgorithmException {
		
		//Constructing the SE
		GrayStructuringElement gse = GrayStructuringElement.createConeToZeroStructuringElement(radius, slope);

		outputImage = inputImage.copyImage(false);
		outputImage.fill(0);
		switch(type)
		{
			case byErosion: Image erosion = GeneralGrayErosion.exec(inputImage,gse);
							Image dilation = GeneralGrayDilation.exec(inputImage,gse);
							//Viewer2D.exec(erosion,"Erosion");
							//Viewer2D.exec(dilation,"Dilation");
							for(int i=0;i<inputImage.size();i++)
							{
								if(Tools.relativeDoubleEquality(inputImage.getPixelDouble(i),erosion.getPixelDouble(i))&&Tools.relativeDoubleEquality(inputImage.getPixelDouble(i),dilation.getPixelDouble(i)))
								{
									outputImage.setPixelDouble(i, 1.);
								}
							}
							break;
		}
		
		

	}
	
	/**
	 * 
	 * @param inputImage Image to be processed
	 * @param slope Slope of the cone (assumed to be given as byte value in [0;255])
	 * @param radius Radius of the cone
	 * @return LipschitzConnexity Image
	 */
	public static Image exec(Image inputImage, int slope, int radius)
	{
		return (Image) new LipschitzConnexity().process(inputImage, slope*DoubleImage.byteToDouble, radius);
	}
	
	/**
	 * 
	 * @param inputImage Image to be processed
	 * @param slope Slope of the cone
	 * @param radius Radius of the cone
	 * @return LipschitzConnexity Image
	 */
	public static Image exec(Image inputImage, double slope, int radius)
	{
		return (Image) new LipschitzConnexity().process(inputImage, slope, radius);
	}

}
