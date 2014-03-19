package fr.unistra.pelican.algorithms.morphology.vectorial.gradient;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.morphology.gray.GrayGradient;

/**
 * This class computes a barycentric gradient using the saturation weighted combination of
 * luminance and hue gradients; the input is assumed to be a polar colour
 * image.
 * 
 * @author Abdullah
 */
public class ColourHSYWeightedGradient extends Algorithm
{
	/**
	 * the input image
	 */
	public Image input;

	/**
	 * the structuring element
	 */
	public BooleanImage se;

	/**
	 * type of weighting, possible values : NOWEIGHT,LINEAR,NOT_ADAPTIVE,ADAPTIVE,COMBINED
	 */
	public int weight_type;

	/**
	 * the output image
	 */
	public Image output;

	/**
	 * the slope of the sigmoid weight
	 */
	public double slope;

	/**
	 * the offset of the sigmoid weight
	 */
	public double offset;

	/**
	 * no weight whatsoever
	 */
	public static final int NOWEIGHT = 0;

	/**
	 * linear weighting with saturation a la angulo
	 */
	public static final int LINEAR = 1;
	
	/**
	 * TODO : not yet supported
	 */
	public static final int NOT_ADAPTIVE = 2;

	/**
	 * sigmoid of saturation
	 */
	public static final int ADAPTIVE = 3;

	/**
	 * combination of saturation and luminance
	 */
	public static final int COMBINED = 4;
	
	/**
	 * 
	 * @param input the input image
	 * @param se the structuring element
	 * @param weight_type type of gradient
	 * @param slope slope of sigmoidal transition
	 * @param offset offset of sigmoidal transition
	 * @return the output image
	 */
	public static Image exec(Image input,BooleanImage se,Integer weight_type,Double slope,Double offset)
	{
		return (Image) new ColourHSYWeightedGradient().process(input,se,weight_type,slope,offset);
	}

	/**
	 * Constructor
	 * 
	 */
	public ColourHSYWeightedGradient() {

		super();
		super.inputs = "input,se,weight_type,slope,offset";
		super.outputs = "output";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		// get the luminance gradient
		Image img = input.getImage4D( 2,Image.B );
		img.setMask( input.getMask() );
		Image luminanceGradient = (Image) new GrayGradient().process( img , se);

		// get the hue gradient
		Image hueGradient = (Image) new HueGradient().process(input, se);

		//output=new ByteImage(inputImage.getXDim(), inputImage.getYDim(),1,1,1);
		output = input.newInstance(input.getXDim(), input.getYDim(), 1, 1, 1);

		for (int x = 0; x < input.getXDim(); x++) {
			for (int y = 0; y < input.getYDim(); y++) {

				if ( !input.isPresentXYZT( x,y,0,0 ) ) continue;

				// set the output as abarycentric combination
				double[] p = input.getVectorPixelXYZTDouble(x, y, 0, 0);

				double alpha = 0.0;

				switch (weight_type) {
				case 0:
					alpha = 0.5;
					break;
				case 1:
					alpha = p[1];
					break;
				case 2:
					alpha = 1 / (1 + Math.exp(-1 * slope * (p[1] - offset)));
					break;
				case 3:
					alpha = 1 / (1 + Math.exp(-1 * slope * (p[1] - offset)));
					break;
				case 4:
					if (p[2] <= 0.5)
						alpha = (1 / (1 + Math.exp(-10 * (p[2] - 0.25))) - 0.182) / 0.788;
					else
						alpha = (1 / (1 + Math.exp(10 * (p[2] - 0.75))) - 0.182) / 0.788;
					alpha = 1 / (1 + Math.exp(-1 * slope * (p[1] - offset)))
							* alpha;
					break;
				default:
					throw new AlgorithmException("Invalid weight type");
				}

				double lum = luminanceGradient.getPixelXYDouble(x, y);
				double hue = hueGradient.getPixelXYDouble(x, y);

				// double combined = (1 - alpha) * lum + alpha * (Math.exp(hue)
				// - 1)/(Math.exp(1) - 1);
				double combined = (1 - alpha) * lum + alpha * hue;
				// double combined = (alpha <= 0.5)?lum:hue;

				output.setPixelXYDouble(x, y, combined);
			}
		}
	}
}
