package fr.unistra.pelican.algorithms.morphology.vectorial.hitormiss;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.arithmetic.Inversion;
import fr.unistra.pelican.algorithms.conversion.ColourDistanceImage;
import fr.unistra.pelican.algorithms.morphology.gray.hitormiss.GrayUnconstrainedHitOrMiss;

/**
 * A hit or miss operator for color images with respect to a SINGLE reference color.
 * 
 * Based on color reduction in combination with Soille's unconstrained hit-or-miss.
 *  
 * 12/12/2007
 * 
 * @author Abdullah
 * @version 0.1
 */
public class ColorHitOrMiss extends Algorithm
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
	 * The reference color
	 */
	public double[] ref;

	/**
	 * Output
	 */
	public Image output;

	/**
	 * Constructor
	 * 
	 */
	public ColorHitOrMiss()
	{
		super();
		super.inputs = "input,seFG,,seBG,ref";
		super.outputs = "output";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch()
	{	
		Image distanceImage = ColourDistanceImage.exec(input,(int)(ref[0]*255),(int)(ref[1]*255),(int)(ref[2]*255));
		Image inverseDistance = Inversion.exec(distanceImage);
		
		output = GrayUnconstrainedHitOrMiss.exec(inverseDistance,seFG,seBG);
	}

	/**
	 * Unconstrained grayscale hit-or-miss transform
	 * @param Input input image
	 * @param se composite structuring element
	 * @return output image
	 */
	public static Image exec(Image Input, BooleanImage seFG,BooleanImage seBG, double[] ref)
	{
		return (Image) new ColorHitOrMiss().process(Input,seFG,seBG,ref);
	}
}