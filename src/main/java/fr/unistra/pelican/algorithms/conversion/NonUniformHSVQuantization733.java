package fr.unistra.pelican.algorithms.conversion;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;

/**
 * Non uniform hue quantization for HSV
 * 
 * HSV 7-3-3
 * 
 * With the hue being non uniformly divided according to
 * 
 * Spatial Color Descriptor for Image Retrieval and Video Segmentation by Lee et al. 2003
 * 
 * 26/10/2007 (desperate for improvement...)
 * 
 *	MASK MANAGEMENT (by Régis) : 
 *	- input's mask becomes output's mask.
 *	- no modification on color calculation.
 * 
 * @author E.A
 *
 */
public class NonUniformHSVQuantization733 extends Algorithm
{	
	/**
	 * 
	 */
	public Image input;

	/**
	 * 
	 */
	public Image output;
	
	/**
	 * 
	 *
	 */
	public NonUniformHSVQuantization733()
	{

		super();
		super.inputs = "input";
		super.outputs = "output";
		
	}
	
	/**
	 * 
	 * @param input
	 * @return output image
	 */
	public static Image exec(Image input)
	{
		return (Image) new NonUniformHSVQuantization733().process(input);
	}

	public void launch() throws AlgorithmException
	{
		output = input.copyImage(true);
		this.output.setMask( this.input.getMask() );
		
		final double coeff = 360.0 / 255.0;
		
		for(int x = 0; x < input.getXDim(); x++){
			for(int y = 0; y < input.getYDim(); y++){
				int[] p = output.getVectorPixelXYZTByte(x,y,0,0);
				
				p[1] = (int)Math.floor(p[1] / 86.0);	// saturation \in [0,2]
				p[2] = (int)Math.floor(p[2] / 86.0);	// value \in [0,2]
				
				// while the hue is divided non uniformly

				int hue = (int)Math.floor(p[0] * coeff);

				// red (330-22)
				if (hue >= 330 || hue <= 22) hue = 0;
				
				// orange (22-45)
				else if (hue >= 22 && hue <= 45) hue = 1;
				
				// yellow (45-70)
				else if (hue >= 45 && hue <= 70) hue = 2;
				
				// green (70-155)
				else if (hue >= 70 && hue <= 155) hue = 3;
				
				// cyan (155-186)
				else if (hue >= 155 && hue <= 186) hue = 4;
				
				// blue (186-278)
				else if (hue >= 186 && hue <= 278) hue = 5;
				
				// purple (278-330)
				else if (hue >= 278 && hue <= 330) hue = 6;
				
				else
					System.err.println("sorun var " + hue);
				
				p[0] = hue;
				
				output.setVectorPixelXYZTByte(x,y,0,0,p);
			}
		}
	}
}
