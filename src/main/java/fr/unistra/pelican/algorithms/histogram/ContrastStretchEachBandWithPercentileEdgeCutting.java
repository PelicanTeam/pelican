package fr.unistra.pelican.algorithms.histogram;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;

/**
 * Stretch the contrast of an image so that the minimal intensity pixel (in
 * double format) for each band is 0.0 and the maximal intensity is 1.0.
 * It also cut extremities values according to a fixed percentage [0.0;1.0] 
 * 
 * Maybe it work on image that have initial range outside [0.0;1.0].
 * 
 * This algorithm use the darker and brighter pixel in frames and temporality
 * (?) for each band and apply the same stretch factor for all pixels of the
 * same band.
 * 
 * @author Jonathan Weber
 * 
 */
public class ContrastStretchEachBandWithPercentileEdgeCutting extends Algorithm {

	/**
	 * Image to process.
	 */
	public Image inputImage;
	
	/**
	 * Percent of points to cut at each edge of the space of value
	 */
	public double percent;

	/**
	 * Output parameter.
	 */
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public ContrastStretchEachBandWithPercentileEdgeCutting() {

		super();
		super.inputs = "inputImage,percent";
		super.outputs = "outputImage";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = inputImage.copyImage(false);
		
		int xDim = outputImage.getXDim();
		int yDim = outputImage.getYDim();
		int zDim = outputImage.getZDim();
		int tDim = outputImage.getTDim();
		int bDim = outputImage.getBDim();
		
		for(int b=0;b<bDim;b++)
		{
			double[] histo = (double[]) new Histogram().process(inputImage.getImage4D(b, Image.B),true);
			
			
			double newMin=0;
			double newMax=255;
			double histoSum=0;
			for(int i=0;i<256;i++)
			{
				histoSum+=histo[i];
				if(histoSum>percent)
				{
					newMin=i;i=256;
				}				
			}
			histoSum=1;
			for(int i=255;i>=0;i--)
			{
				histoSum-=histo[i];
				if(histoSum<1-percent)
				{
					newMax=i;i=-1;
				}
			}
			newMin/=255.;
			newMax/=255.;
			
			for(int t=0;t<tDim;t++)
				for(int z=0;z<zDim;z++)
					for(int y=0;y<yDim;y++)
						for(int x=0;x<xDim;x++)
						{
							double value=inputImage.getPixelXYZTBDouble(x,y,0,0,b);
							if(value<newMin)
								value=newMin;
							if(value>newMax)
								value=newMax;
							outputImage.setPixelXYZTBDouble(x, y, 0, 0, b, value);
						}
		}
		outputImage= (Image) new ContrastStretchEachBands().process(outputImage);		
	}

	/**
	 * *Stretch the contrast of an image so that the minimal intensity pixel (in
	 * double format) for each band is 0.0 and the maximal intensity is 1.0.
	 * It also cut extremities values according to a fixed percentage [0.0;1.0]
	 * 
	 * @param inputImage Image to be stretched.
	 * @param percent percent of edge pixels to cut
	 * @return The contrast stretched image.
	 */
	public static Image exec(Image inputImage, double percent) {
		return (Image) new ContrastStretchEachBandWithPercentileEdgeCutting().process(inputImage, percent);
	}
}
