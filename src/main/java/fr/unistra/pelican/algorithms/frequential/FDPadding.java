/**
 * 
 */
package fr.unistra.pelican.algorithms.frequential;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;


/**
 * Performs zero padding in the frequency domain. 
 * Zero filling is done is the inner part of the image instead of the border
 * 
 * 
 * @author Benjamin Perret
 *
 */
public class FDPadding extends Algorithm {
	
	
	/**
	 * Input Image in frequency domain
	 */
	public Image inputImage;
	
	/**
	 * Output Image in frequency domain
	 */
	public Image outputImage;

	/**
	 * New x dimension
	 */
	public int newX;
	
	/**
	 * New y dimension
	 */
	public int newY;
	
	/**
	 * 
	 */
	public FDPadding() {
		super.inputs = "inputImage,newX,newY";
		super.outputs = "outputImage";
	}

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	@Override
	public void launch() throws AlgorithmException {
		int xdim=inputImage.xdim;
		int ydim=inputImage.ydim;
		int tdim=inputImage.tdim;
		int zdim=inputImage.zdim;
		int bdim=inputImage.bdim;
		int diffX=newX-xdim;
		int diffY=newY-ydim;
		
		if(diffX < 0 || diffY<0)
			throw new AlgorithmException("New dimensions cannot be smaller than original one.");
		
		
		outputImage = inputImage.newInstance(newX, newY, zdim, tdim, bdim);

		int cutX= xdim/2;
		int cutY= ydim/2;
		for(int b=0;b<bdim;b++)
			for(int t=0;t<tdim;t++)
				for(int z=0;z<zdim;z++)
				{
					for(int y=0;y<cutY;y++)
					{
						for(int x=0;x<cutX;x++)
							outputImage.setPixelDouble(x, y, z, t, b, inputImage.getPixelDouble(x, y, z, t, b));
						for(int x=cutX;x<xdim;x++)
							outputImage.setPixelDouble(x + diffX, y, z, t, b, inputImage.getPixelDouble(x, y, z, t, b));
					}
					for(int y=cutY;y<ydim;y++)
					{
						for(int x=0;x<cutX;x++)
							outputImage.setPixelDouble(x, y + diffY, z, t, b, inputImage.getPixelDouble(x, y, z, t, b));
						for(int x=cutX;x<xdim;x++)
							outputImage.setPixelDouble(x + diffX, y + diffY, z, t, b, inputImage.getPixelDouble(x, y, z, t, b));
					}
				}
		
	}
	
	/**
	 * Performs zero padding in the frequency domain.
	 * 
	 * @param <T> Image type (double?)
	 * @param inputImage Input Image in the frequency domain
	 * @param newX New X dimension
	 * @param newY New y Dimension
	 * @return Padded image
	 */
	@SuppressWarnings("unchecked")
	public static <T> T exec(T inputImage, int newX, int newY)
	{
		return (T)(new FDPadding()).process(inputImage,newX,newY);
	}

	
	
}
