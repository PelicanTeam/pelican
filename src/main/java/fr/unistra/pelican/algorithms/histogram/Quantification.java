/**
 * 
 */
package fr.unistra.pelican.algorithms.histogram;



import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;

/**
 * Quantify image, works in double precision.
 * Beans will all have equal size.
 * Value given to a bean is equal to its mean value;
 * 
 * Each band is processed independently.
 * 
 * @author Benjamin Perret
 *
 */
public class Quantification extends Algorithm {

	/**
	 * Input image
	 */
	public Image inputImage;
	
	/**
	 * Output Image
	 */
	public Image outputImage;
	
	/**
	 * Number of quantification level
	 */
	public int beans;
	
	/**
	 * Do we need to put result in a new image (default is true)
	 */
	public boolean allocateNewMemory=true;
	
	/**
	 * image dimensions
	 */
	private int bdim,zdim,tdim,xdim,ydim;
	
	/**
	 * 
	 */
	public Quantification() {
		super.inputs="inputImage,beans";
		super.outputs="outputImage";
		super.options="allocateNewMemory";
	}

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	@Override
	public void launch() throws AlgorithmException {
		if(allocateNewMemory)
		{
			outputImage=inputImage.copyImage(false);
		}
		else { outputImage = inputImage; }
		bdim=inputImage.bdim;
		tdim=inputImage.tdim;
		zdim=inputImage.zdim;
		ydim=inputImage.ydim;
		xdim=inputImage.xdim;
		
		for(int b=0;b<bdim;b++)
			process(b);
		

	}
	
	private void process(int b)
	{
		double min=Double.POSITIVE_INFINITY;
		double max=Double.NEGATIVE_INFINITY;
		for(int t=0;t<tdim;t++)
			for(int z=0;z<zdim;z++)
				for(int y=0;y<ydim;y++)
					for(int x=0;x<xdim;x++)
					{
						double v=inputImage.getPixelDouble(x, y, z, t, b);
						if(v<min)
							min=v;
						if(v>max)
							max=v;
					}
		
		double bb = (double)beans;
		double step=(max-min)/bb;
		double [] steps=new double[beans];
		for(int i=0;i<beans;i++)
		{
			steps[i]=step/2.0 + ((double)i)*step;
		}
		
		
		for(int t=0;t<tdim;t++)
			for(int z=0;z<zdim;z++)
				for(int y=0;y<ydim;y++)
					for(int x=0;x<xdim;x++)
					{
						double v=inputImage.getPixelDouble(x, y, z, t, b)-min;
						int s= (int)Math.floor(v/step);
						if(s==beans) 
							s--;
						outputImage.setPixelDouble(x, y, z, t, b, steps[s]);
					}
		
	}

	@SuppressWarnings("unchecked")
	public static <T extends Image> T exec(T inputImage, int beans, boolean allocateNewMemory)
	{
		return (T)(new Quantification()).process(inputImage,beans,allocateNewMemory);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Image> T exec(T inputImage, int beans)
	{
		return (T)(new Quantification()).process(inputImage,beans);
	}

}
