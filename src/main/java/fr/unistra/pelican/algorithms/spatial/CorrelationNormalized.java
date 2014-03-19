package fr.unistra.pelican.algorithms.spatial;

import java.awt.Point;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.morphology.GrayStructuringElement;
/**
 * Implementation of the correlation for object-matching.
 * 
 * 
 * Be careful, results are in [-1;1]
 * 
 * @author Jonathan Weber
 *
 */
public class CorrelationNormalized extends Algorithm {
	
	/**
	 * Input image
	 */
	public Image input;
	
	/**
	 * Structuring element used for the correlation
	 */
	public GrayStructuringElement template;

	/**
	 * Output image
	 */
	public DoubleImage output;
	
	/**
	 * Constructor
	 * 
	 */
	public CorrelationNormalized() {

		super();
		super.inputs = "input,template";
		super.outputs = "output";
		
	}

	@Override
	public void launch() throws AlgorithmException {
		int xDim = input.getXDim();
		int yDim = input.getYDim();
		int zDim = input.getZDim();
		int tDim = input.getTDim();
		int bDim = input.getBDim();
		
		output = new DoubleImage(xDim,yDim,zDim,tDim,bDim);
		output.fill(0.);
		
		//Compute the mean of the template
		double meanTemplateValue=0.;
		double count=0;
		Point[] points = template.getPoints().get(0);
		
		for(int i=0;i<points.length;i++)
		{
			meanTemplateValue+=template.getPixelXYDouble(points[i].x,points[i].y);
			count++;
		}
		meanTemplateValue=meanTemplateValue/count;
		
		for(int t=0;t<tDim;t++)
			for(int z=0;z<zDim;z++)
				for(int y=0;y<yDim;y++)
					for(int x=0;x<xDim;x++)
						for(int b=0;b<bDim;b++)
						output.setPixelXYZTBDouble(x, y, z, t, b, getCorrelationValue(x, y, z, t, b, points, meanTemplateValue, xDim, yDim));
	}
		
	private final double getCorrelationValue(int x,int y, int z, int t, int b, Point[] points, double meanTemplateValue, int xDim, int yDim)
	{
		//Compute the mean of the image under the template
		double meanImageValue=0.;
		double count=0;
		
		for(int i=0;i<points.length;i++)
		{
			int xVal=x+points[i].x-template.getCenter().x;
			int yVal=y+points[i].y-template.getCenter().y;
			if(xVal>=0&&xVal<xDim&&yVal>=0&&yVal<yDim)
			{
				count++;
				meanImageValue+=input.getPixelXYZTBDouble(xVal,yVal,z,t,b);
			}else
			{
				//return -1.;
			}
		}							

		meanImageValue=meanImageValue/count;
		
		double numerator=0.; 	//numerator
		double denominator1=0.; //left denominator
		double denominator2=0.; //right denominator
		for(int i=0;i<points.length;i++)
		{
			int xVal=x+points[i].x-template.getCenter().x;
			int yVal=y+points[i].y-template.getCenter().y;
			if(xVal>=0&&xVal<xDim&&yVal>=0&&yVal<yDim)
			{
				double templateValueNormalized=(template.getPixelXYDouble(points[i].x,points[i].y)-meanTemplateValue);
				double imageValueNormalized = (input.getPixelXYZTBDouble(xVal,yVal,z,t,b)-meanImageValue);
				numerator+=templateValueNormalized*imageValueNormalized;
				denominator1+=imageValueNormalized*imageValueNormalized;
				denominator2+=templateValueNormalized*templateValueNormalized;
			}
		}
		
		if(denominator1==0.||denominator2==0.)
		{
			return -1.;
		}else
		{
			return numerator/Math.sqrt(denominator1*denominator2);
		}
	}
	
	
	/**
	 * Normalized correlation for template matching purpose
	 * @param input Input image
	 * @param template Structuring element used for the correlation
	 * @return Output image
	 */
	public static DoubleImage exec(Image input, GrayStructuringElement template) {
		return (DoubleImage) new CorrelationNormalized().process(input,template);
	}

}
