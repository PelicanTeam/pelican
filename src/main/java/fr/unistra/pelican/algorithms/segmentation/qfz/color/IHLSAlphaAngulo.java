package fr.unistra.pelican.algorithms.segmentation.qfz.color;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.arithmetic.AdditionConstantChecked;
import fr.unistra.pelican.algorithms.segmentation.ManualThresholding;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToRandomColors;
import fr.unistra.pelican.algorithms.segmentation.qfz.gray.GrayAlphaConnectivityCC;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.Point4D;



public class IHLSAlphaAngulo extends Algorithm 
{
	
	public DoubleImage inputImage;
	
	public int alpha;
	
	public int saturationThresh;
	
	public Point4D[] neighbourhood;
	
	public IntegerImage outputImage;
	
	public IHLSAlphaAngulo()
	{
		super.inputs="inputImage,alpha,saturationThresh,neighbourhood";
		super.outputs="outputImage";
	}
	
	public void launch() 
	{
		int xDim = inputImage.getXDim();
		int yDim = inputImage.getYDim();
		int zDim = inputImage.getZDim();
		int tDim = inputImage.getTDim();
		
		outputImage = new IntegerImage(xDim,yDim,zDim,tDim,1);
		
		IntegerImage alphaY = GrayAlphaConnectivityCC.exec(inputImage.getImage4D(0, Image.B), alpha, neighbourhood);
		
		IntegerImage alphaH = HueAlphaConnectivity.exec(inputImage.getImage4D(2, Image.B), alpha, neighbourhood, 2*Math.PI);
		
		alphaH = (IntegerImage)AdditionConstantChecked.exec(alphaH, alphaY.maximumInt()+1,AdditionConstantChecked.INTMODE);
		
		BooleanImage threshedS = ManualThresholding.exec(inputImage.getImage4D(1, Image.B), ((double)saturationThresh)/255.);
		
		for(int i=0;i<outputImage.size();i++)
			if(threshedS.getPixelBoolean(i))
				outputImage.setPixelInt(i, alphaH.getPixelInt(i));
			else
				outputImage.setPixelInt(i, alphaY.getPixelInt(i));
	}
	
	public static IntegerImage exec(DoubleImage inputImage, int alpha, int saturationThresh,Point4D[] neighbourhood)
	{
		return (IntegerImage) new IHLSAlphaAngulo().process(inputImage,alpha,saturationThresh, neighbourhood);
	}
	
}
