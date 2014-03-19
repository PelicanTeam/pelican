package fr.unistra.pelican.algorithms.segmentation.qfz.color;

import java.util.ArrayList;
import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.util.Point4D;


/**
 * This class performs a fast color alpha-connexity algorithm according to
 * P. Soille, Constrained Connectivity for Hierarchical Image Partitioning and Simplification
 * IEEE TPAMI, vol 30, no 7, July 2006
 * 
 * Alpha value is used in byte precision.
 * 
 * This method is applicable on image with n bands
 * 
 * @author Jonathan Weber
 *
 */
public class ColorAlphaConnectivitySoille extends Algorithm {

	/**
	 * Image to process
	 */
	public Image inputImage;
	
	/**
	 * alpha values for local range
	 */
	public int[] alpha;
	
	/**
	 * Connectivity used to determine the flat zones
	 */
	public Point4D[] neighbourhood;;
	
	/**
	 * Flat Zones labels
	 */
	public IntegerImage outputImage;
	
	private static final int INITVALUE = -2;
	private static final int INQUEUE = -1;
	
	private ArrayList<Point4D> neighboursToExpand;
	
	private int xDim;
	private int yDim;
	private int zDim;
	private int tDim;
	private int bDim;
	
	private int currentLabel;

	
	/**
	 * Constructor
	 * 
	 */
	public ColorAlphaConnectivitySoille() 
	{
		super();
		super.inputs = "inputImage,alpha,neighbourhood";
		super.outputs = "outputImage";
	}
	
	@Override
	public void launch() throws AlgorithmException {
		
		xDim = inputImage.getXDim();
		yDim = inputImage.getYDim();
		zDim = inputImage.getZDim();
		tDim = inputImage.getTDim();
		bDim = inputImage.getBDim();
		
		if(bDim!=alpha.length)
		{
			throw new PelicanException("Number of bands of alpha ["+alpha.length+"] is different from number of bands of the inputImage ["+bDim+"]");
		}
		
		neighboursToExpand = new ArrayList<Point4D>();		
		
		outputImage= new IntegerImage(xDim,yDim,zDim,tDim,1);
		outputImage.fill(INITVALUE);
		
		currentLabel=-1;
		
		for(int t=tDim;--t>=0;)
			for(int z=zDim;--z>=0;)
				for(int y=yDim;--y>=0;)
					for(int x=xDim;--x>=0;)
					{
						if(outputImage.getPixelXYZTInt(x, y, z, t)==INITVALUE)
						{
							outputImage.setPixelXYZTInt(x, y, z, t, ++currentLabel);
							addUnlabelledNeighboursRespectToKValueToQueue(x, y, z, t);
							while(neighboursToExpand.size()!=0)
							{
								expandCurrentLabelTo(neighboursToExpand.remove(0));
							}
						}
					}		
	}
	
	private final void expandCurrentLabelTo(Point4D pixel)
	{
				outputImage.setPixelXYZTInt(pixel.x, pixel.y, pixel.z, pixel.t, currentLabel);
				addUnlabelledNeighboursRespectToKValueToQueue(pixel.x, pixel.y, pixel.z, pixel.t);		
	}
	
	private final void addUnlabelledNeighboursRespectToKValueToQueue(int x, int y, int z, int t)
	{
		int[] pixelValue = new int[bDim];
		for(int i=0;i<bDim;i++)
			pixelValue[i]=inputImage.getPixelXYZTBByte(x, y, z, t,i);
		
		for(int i=neighbourhood.length;--i>=0;)
		{
			int locX = x + neighbourhood[i].x;
			int locY = y + neighbourhood[i].y;
			int locZ = z + neighbourhood[i].z;
			int locT = t + neighbourhood[i].t;
			if(locX>=0&&locY>=0&&locZ>=0&&locT>=0&&locX<xDim&&locY<yDim&&locZ<zDim&&locT<tDim)
			{
				if(outputImage.getPixelXYZTInt(locX, locY, locZ, locT)==INITVALUE)
				{
					boolean alphaIsRespected = true;
						for(int j=0;j<bDim;j++)
						{
							if(Math.abs(pixelValue[j]-inputImage.getPixelXYZTBByte(locX, locY, locZ, locT,j))>alpha[j])
							{
								alphaIsRespected = false;
							}
						}
					
					if(alphaIsRespected)
					{
						neighboursToExpand.add(new Point4D(locX, locY, locZ, locT));
						outputImage.setPixelXYZTInt(locX, locY, locZ, locT,INQUEUE);
					}
				}
			}
		}	
	}
	
	public static IntegerImage exec(Image inputImage, int[] alpha, Point4D[] neighbourhood) 
	{
		return (IntegerImage)new ColorAlphaConnectivitySoille().process(inputImage,alpha,neighbourhood);
	}
}
