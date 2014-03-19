/**
 * 
 */
package fr.unistra.pelican.algorithms.spatial;

import java.awt.Point;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.frequential.FFT2;
import fr.unistra.pelican.algorithms.frequential.Magnitude;
import fr.unistra.pelican.algorithms.geometric.Crop2D;
import fr.unistra.pelican.algorithms.geometric.Padding;
import fr.unistra.pelican.algorithms.histogram.HistogramCorrection;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.spatial.Convolution;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.Tools;
import fr.unistra.pelican.util.morphology.GrayStructuringElement;

/**
 * Convolution is done by multiplying images in the frequency domain.
 * 
 * FFT convolution is much more faster than standard convolution, while producing exactly the same result.
 * 
 * Result size is same as input size.
 * 
 * Only x and y dim considered!
 * 
 * If you have to convolve images with same dimensions multiple times, 
 * save time by calling process() from the save Algorithm object.
 * 
 * If you use same kernel convolution multiple times only specify it the first time then call the process method and leave kernel null.
 * 
 * @author Benjamin Perret
 *
 */
public class ConvolveFFT extends Algorithm {

	/**
	 * Possible size for result:
	 * -SAME: result is put in inputImage
	 * -NEW: result is put in a new Image
	 * @author Benjamin Perret
	 *
	 */
	public enum ResultFormat {SAME, NEW};
	
	/**
	 * Input Image
	 */
	public DoubleImage inputImage;
	
	/**
	 * Convolution Kernel
	 */
	public DoubleImage kernel;

	/**
	 * For multiple convolution with same kernel
	 */
	private DoubleImage kernelSave;
	
	private DoubleImage kernelRef;
	
	private DoubleImage [] kernelFFT;
	
	private int xdimSave;
	
	private int ydimSave;
	
	private int xdim;
	
	private int ydim;
	
	private FFT2 fft= new FFT2();
	
	/**
	 * Result 
	 */
	public DoubleImage outputImage;
	
	/**
	 * Size of result.
	 */
	public ResultFormat resultFormat=ResultFormat.NEW;
	
	public ConvolveFFT()
	{
		super.inputs="inputImage,kernel";
		super.options="resultFormat";
		super.outputs="outputImage";
	}
	
	private void normalizeKernel()
	{
		double sum=kernel.volume();
		for(int i=0;i<kernel.size();i++)
		{
			kernel.setPixelDouble(i, kernel.getPixelDouble(i)/sum);
		}
		
	}
	
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	@Override
	public void launch() throws AlgorithmException {
		
		if(kernelSave != null && (kernel ==  null || kernelRef == kernel) )
			kernel=kernelSave;
		else{
			if(inputImage.bdim != 1 || inputImage.tdim != 1 || inputImage.zdim != 1 || kernel.bdim != 1 || kernel.tdim != 1 || kernel.zdim != 1 )
				throw new AlgorithmException("FFT convolve: t dim, b dim and z dim of input image and kernel must be one");
			normalizeKernel();
			outputImage=null;
		}
		if (outputImage == null || inputImage.xdim != xdimSave || inputImage.ydim != ydimSave)
		{
			System.out.println("reinit convolver");
			xdim = Tools.ceilP2(inputImage.xdim + kernel.xdim -1);
			ydim = Tools.ceilP2(inputImage.ydim + kernel.ydim -1);
			xdimSave = inputImage.xdim;
			ydimSave = inputImage.ydim;
			kernelRef = kernel;
			kernel = kernelSave = Padding.exec(kernel,xdim,ydim,-1,-1,-1,Padding.NULL,(xdim-kernel.xdim)/2,(ydim-kernel.ydim)/2,0,0,0);
			kernelFFT = (DoubleImage [])fft.process(kernel,null,false);
		}
		
		int sx=xdimSave-1;
		int sy=ydimSave-1;
		//Viewer2D.exec(HistogramCorrection.exec(inputImage));
		/*for(int y=0;y<inputImage.ydim;y++)
			for(int x=y;x<inputImage.xdim;x++)
			{
				
				double tmp=inputImage.getPixelXYDouble(x, y);
				inputImage.setPixelXYDouble(x,y,inputImage.getPixelXYDouble(sx-x, sy-y));
				inputImage.setPixelXYDouble(sx-x,sy-y,tmp);
			}*/
		
		//Viewer2D.exec(HistogramCorrection.exec(inputImage));
		Image image=Padding.exec(inputImage, xdim, ydim, -1, -1, -1, Padding.MIRROR);//, (xdim-inputImage.xdim)/2, (ydim-inputImage.ydim)/2, 0, 0, 0);
		//Viewer2D.exec(HistogramCorrection.exec(inputImage));
		
		DoubleImage [] imageFFT=(DoubleImage [])fft.process(image,null,false);
		
			for(int i=0;i<imageFFT[0].size();i++)
			{
				double a1=imageFFT[0].getPixelDouble(i);
				double b1=imageFFT[1].getPixelDouble(i);
				double a2=kernelFFT[0].getPixelDouble(i);
				double b2=kernelFFT[1].getPixelDouble(i);
				imageFFT[0].setPixelDouble(i,a1*a2-b1*b2);
				imageFFT[1].setPixelDouble(i,a1*b2+b1*a2);
			}
		outputImage = Magnitude.exec((DoubleImage [])fft.process(imageFFT[0], imageFFT[1], true));
		//outputImage = Crop2D.exec(outputImage, 0, 0, xdimSave, ydimSave);
		
		
		if(resultFormat.equals(ResultFormat.SAME))
		{
			for(int y=0;y<ydimSave;y++)
				for(int x=y;x<xdimSave;x++)
				{
					inputImage.setPixelXYDouble(x,y,outputImage.getPixelXYDouble(sx-x, sy-y));
					inputImage.setPixelXYDouble(sx-x,sy-y,outputImage.getPixelXYDouble(x, y));
					
				}
			outputImage=inputImage;
		}else {
			outputImage = Crop2D.exec(outputImage, 0, 0, xdimSave-1, ydimSave-1);
			//Viewer2D.exec(outputImage.scaleToZeroOne());
			for(int y=0;y<outputImage.ydim/2;y++)
				for(int x=y;x<outputImage.xdim;x++)
				{
					
					double tmp=outputImage.getPixelXYDouble(x, y);
					outputImage.setPixelXYDouble(x,y,outputImage.getPixelXYDouble(sx-x, sy-y));
					outputImage.setPixelXYDouble(sx-x,sy-y,tmp);
				}
			for(int y=outputImage.ydim/2;y<outputImage.ydim;y++)
				for(int x=y+1;x<outputImage.xdim;x++)
				{
					
					double tmp=outputImage.getPixelXYDouble(x, y);
					outputImage.setPixelXYDouble(x,y,outputImage.getPixelXYDouble(sx-x, sy-y));
					outputImage.setPixelXYDouble(sx-x,sy-y,tmp);
				}
		}
		
	}
	
	public static <T extends Image, Q extends Image> T exec(T inputImage, Q kernel)
	{
		return (T)(new ConvolveFFT()).process(inputImage,kernel);
	}
	
/*
	private static DoubleImage prepareKernel(DoubleImage kernel)
	{
		DoubleImage res=(DoubleImage)kernel.copyImage(false);
		int x2=res.xdim/2;
		int y2=res.ydim/2;
		
		for(int j=0;j<y2;j++)
			for(int i=0;i<x2;i++)
				res.setPixelXYDouble(i, j, kernel.getPixelXYDouble(i+x2, j+y2));
		
		for(int j=0;j<y2;j++)
			for(int i=x2;i<res.xdim;i++)
				res.setPixelXYDouble(i, j, kernel.getPixelXYDouble(i-x2, j+y2));
		
		for(int j=y2;j<res.ydim;j++)
			for(int i=0;i<x2;i++)
				res.setPixelXYDouble(i, j, kernel.getPixelXYDouble(i+x2, j-y2));
		
		for(int j=y2;j<res.ydim;j++)
			for(int i=x2;i<res.xdim;i++)
				res.setPixelXYDouble(i, j, kernel.getPixelXYDouble(i-x2, j-y2));
		
		return res;
	}
	*/
	
	/**
	 * @param args
	 */
	/*public static void main(String[] args) {
		int kernelSize=51;
		Point centre=new Point(kernelSize/2,kernelSize/2);
		double kernelVariance=10;
		
		DoubleImage image = new DoubleImage(ImageLoader.exec("samples/lennaGray256.png"));
		
		DoubleImage kernel = new DoubleImage(kernelSize,kernelSize,1,1,1);
		
		double sum=0.0;
		for(int j=0;j<kernelSize;j++)
			for(int i=0;i<kernelSize;i++)
			{
				double ii=i-centre.x;
				double jj=j-centre.y;
				double v=Math.exp(-0.5*(ii*ii+jj*jj)/kernelVariance);
				sum+=v;
				kernel.setPixelXYDouble(i, j, v);
			}
		
		for(int i=0;i<kernel.size();i++)
		{
			kernel.setPixelDouble(i, kernel.getPixelDouble(i)/sum);
		}
		
		
GrayStructuringElement kernel2= new GrayStructuringElement(kernel,new Point(kernel.xdim/2,kernel.ydim/2));
		ConvolveWithFFT algo = new ConvolveWithFFT();
		long t1= System.currentTimeMillis ();
		DoubleImage result = (DoubleImage)algo.process(image,kernel,ConvolveWithFFT.ResultFormat.NEW);
		long t2= System.currentTimeMillis ();
		DoubleImage result2 = (DoubleImage)algo.process(image,null,ConvolveWithFFT.ResultFormat.NEW);
		long t3= System.currentTimeMillis ();
		DoubleImage result3 = Convolution.exec(image,kernel2);
		long t4= System.currentTimeMillis ();
		System.out.println ("t1: " + (t2-t1) + "  t2 " + (t3-t2)+ "  t3 " + (t4-t3) );
		if(image==result && image==result2)
			System.out.println("ok");
		//Viewer2D.exec(HistogramCorrection.exec(image));
		Viewer2D.exec(HistogramCorrection.exec(result));
		Viewer2D.exec(HistogramCorrection.exec(result2),"res2");
		Viewer2D.exec(HistogramCorrection.exec(result3),"res3");
	}*/

	
	
	
}
