package fr.unistra.pelican.algorithms.morphology.gray.hitormiss;


import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.morphology.gray.GrayDilation;
import fr.unistra.pelican.algorithms.morphology.gray.GrayErosion;

/**
 * Grayscale hit-or-miss transform according to Ronse's definition
 * 
 * @author Jonathan Weber
 */
public class GrayHitOrMissByRonse extends Algorithm
{
	
	/**
	 * Input image
	 */
	public Image input;
	
	/**
	 * Foreground structuring element
	 */
	public BooleanImage seFG;
	
	/**
	 * Background structuring element
	 */
	public BooleanImage seBG;

	/**
	 * Output
	 */
	public Image output;

	/**
	 * Constructor
	 * 
	 */
	public GrayHitOrMissByRonse()	{
		super.inputs = "input,seFG,seBG";
		super.outputs = "output";
	}

	/*
	 * (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch()
	{
		output = input.copyImage(false);

		Image erosionFG = GrayErosion.exec(input,seFG);
		Image dilationBG = GrayDilation.exec(input,seBG);
			
		for(int b = 0; b < input.getBDim(); b++){
			for(int x = 0; x < input.getXDim(); x++){
				for(int y = 0; y < input.getYDim(); y++){

					if ( !input.isPresentXYB( x,y,b ) ) { 

						output.setPixelXYBByte(x,y,b,0);
						continue;
					}

					int eFG = erosionFG.getPixelXYBByte(x,y,b);
					int dBG = dilationBG.getPixelXYBByte(x,y,b);
					
					if (dBG < eFG) output.setPixelXYBByte(x,y,b,eFG);
					else output.setPixelXYBByte(x,y,b,0);
				}
			}
		}
	}

	/**
	 * grayscale hit-or-miss transform according to Ronse's definition
	 * @param Input input image
	 * @param seFG foreground structuring element
	 * @param seBG background structuring element
	 * @return output image
	 */
	public static Image exec(Image input, BooleanImage seFG,BooleanImage seBG)
	{
		return (Image) new GrayHitOrMissByRonse().process(input, seFG, seBG);
	}
}