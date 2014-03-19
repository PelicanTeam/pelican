/**
 * 
 */
package fr.unistra.pelican.algorithms.geometric;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.visualisation.MViewer;
import fr.unistra.pelican.util.Tools;

/**
 * Expand XY dimensions of an image. This can not be used for shrinking (at least this would be a very bad idea).
 * 
 * Possible interpolation methods are
 * 	- nearest neighbor
 *  - bilinear
 *  - bicubic spline (approximation of Catmull-Rom definition)
 *  
 *  @TODO add Lanczos interpolation method
 * 
 * @author Benjamin Perret
 *
 */
public class ExpandXY extends Algorithm {

	/**
	 * Supported interpolation method
	 * @author Benjamin Perret
	 *
	 */
	public static enum InterpolationMethod {Nearest, Bilinear, BiCubic};
	
	/**
	 * Input image
	 */
	public Image inputImage;
	
	/**
	 * x dim of output image
	 */
	public int newXDim;
	
	/**
	 * y dim of output image
	 */
	public int newYDim;
	
	/**
	 * Method used for interpolation (default is bicubic)
	 */
	public InterpolationMethod method=InterpolationMethod.BiCubic;
	
	/**
	 * output image
	 */
	public Image outputImage;
	
	/**
	 * increment in input image xdim
	 */
	private double dx;
	
	/**
	 * increment in input image ydim
	 */
	private double dy;
	
	/**
	 * rounding precision (number of digits after decimal point)
	 */
	private int precision=5;
	
	public ExpandXY(){
		super.inputs="inputImage,newXDim,newYDim";
		super.options="method";
		super.outputs="outputImage";
	}
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	@Override
	public void launch() throws AlgorithmException {
		if(newXDim <inputImage.xdim || newYDim < inputImage.ydim)
			throw new AlgorithmException("New image size must be strictly greater than input Image");
		outputImage= inputImage.newInstance(newXDim, newYDim, inputImage.zdim, inputImage.tdim, inputImage.bdim);

	
		
		switch (method){
		case Nearest:
			dx=((double)inputImage.xdim)/(double)(newXDim);
			dy=((double)inputImage.ydim)/(double)(newYDim);
			nearestNeighboor();
			break;
		case Bilinear:
			dx=((double)inputImage.xdim-1.0)/(double)(newXDim-1.0);
			dy=((double)inputImage.ydim-1.0)/(double)(newYDim-1.0);
			bilinear();
			break;
		case BiCubic:
			dx=((double)inputImage.xdim-1.0)/(double)(newXDim-1.0);
			dy=((double)inputImage.ydim-1.0)/(double)(newYDim-1.0);
			bicubic();
			break;
		
		}
	}
	
	private void nearestNeighboor()
	{
		
		for(int t=0;t<outputImage.tdim;t++)
		{
			for(int z=0;z<outputImage.zdim;z++)
			{
				for(int b=0;b<outputImage.bdim;b++)
				{
					double yOri=0.0;
					for(int y=0;y<outputImage.ydim;y++)
					{
						double xOri=0.0;
						for(int x=0;x<outputImage.xdim;x++)
						{
							double v=inputImage.getPixelDouble((int)Math.floor(xOri), (int)Math.floor(yOri), z, t, b);
							outputImage.setPixelDouble(x, y, z, t, b, v);
							xOri+=dx;
						}
						yOri+=dy;
					}
					
				}
			}
		}
	}

	private void bilinear()
	{
		
		for(int t=0;t<outputImage.tdim;t++)
		{
			for(int z=0;z<outputImage.zdim;z++)
			{
				for(int b=0;b<outputImage.bdim;b++)
				{
					double yOri=0.0;
					for(int y=0;y<outputImage.ydim;y++)
					{
						double xOri=0.0;
						for(int x=0;x<outputImage.xdim;x++)
						{
							//System.out.println("["+Tools.round(xOri,precision)+";"+Tools.round(yOri,precision)+"]");
							double v=Tools.getBilinearInterpolation(inputImage, Tools.round(xOri,precision), Tools.round(yOri,precision), z, t, b);
							outputImage.setPixelDouble(x, y, z, t, b, v);
							xOri+=dx;
						}
						yOri+=dy;
					}
					
				}
			}
		}
	}
	
	private void bicubic()
	{
		
		for(int t=0;t<outputImage.tdim;t++)
		{
			for(int z=0;z<outputImage.zdim;z++)
			{
				for(int b=0;b<outputImage.bdim;b++)
				{
					double yOri=0.0;
					for(int y=0;y<outputImage.ydim;y++)
					{
						double xOri=0.0;
						for(int x=0;x<outputImage.xdim;x++)
						{
							double v=Tools.getBiCubicInterpolation(inputImage, Tools.round(xOri,precision), Tools.round(yOri,precision), z, t, b);
							outputImage.setPixelDouble(x, y, z, t, b, v);
							xOri+=dx;
						}
						yOri+=dy;
					}
					
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Image> T exec(T inputImage, int newXDim, int newYDim){
		return (T)new ExpandXY().process(inputImage,newXDim, newYDim);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Image> T exec(T inputImage, int newXDim, int newYDim, InterpolationMethod method){
		return (T)new ExpandXY().process(inputImage,newXDim, newYDim,method);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*DoubleImage im = new DoubleImage(5,5,1,1,1);
		double [] vals={5.0,20.0,4.0,-40.0,-500,
				0.0,45.0,-3.0,-70.0,-600,
				-2.0,800,5,0,-40,
				-50,90,30,20,-80,
				200,0,20,-90,900};
		im.pixels=vals;
		im.setPixelXYDouble(0, 0, 0.0);
		im.setPixelXYDouble(1, 1, 50.0);
		im.setPixelXYDouble(0, 1, -5.0);
		im.setPixelXYDouble(1, 0, 5.0);
		int nx=100;
		int ny=100;
		*/
		Image im=ImageLoader.exec("samples/lenna512.png");
		int nx=100;
		int ny=100;
		Image near=ExpandXY.exec(im, nx, ny, InterpolationMethod.Nearest);
		Image bili=ExpandXY.exec(im, nx, ny, InterpolationMethod.Bilinear);
		Image bicu=ExpandXY.exec(im, nx, ny, InterpolationMethod.BiCubic);
		MViewer.exec(im,near,bili,bicu);
	}

}
