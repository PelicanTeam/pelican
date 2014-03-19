package fr.unistra.pelican.algorithms.segmentation.qfz.color;

import java.util.ArrayList;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.util.Point4D;


/**
 * Compute alpha connectivity on a mono band Hue Image.
 * 
 * Alpha must be in [0;360]
 * 
 * @author Jonathan Weber
 *
 */
public class HueAlphaConnectivity extends Algorithm {

	/**
	 * Image to process
	 */
	public Image inputImage;
	
	/**
	 * alpha value for local range
	 * 
	 * must be between 0 and 360
	 * 
	 */
	public int alpha;
	
	/**
	 * Connectivity used to determine the flat zones
	 */
	public Point4D[] neighbourhood;;
	
	/**
	 * Range of the angles in the image
	 * 
	 * ie 1 is for [0;1], 2Pi is for [0;2Pi]
	 * 
	 * No negative angle are managed
	 * 
	 */
	public double imageRange;
	
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
	
	private int currentLabel;

	
	/**
	 * Constructor
	 * 
	 */
	public HueAlphaConnectivity() 
	{
		super();
		super.inputs = "inputImage,alpha,neighbourhood,imageRange";
		super.outputs = "outputImage";
	}
	
	@Override
	public void launch() throws AlgorithmException {
		
		xDim = inputImage.getXDim();
		yDim = inputImage.getYDim();
		zDim = inputImage.getZDim();
		tDim = inputImage.getTDim();
		
		neighboursToExpand = new ArrayList<Point4D>();		
		
		outputImage= new IntegerImage(xDim,yDim,zDim,tDim,1);
		outputImage.fill(INITVALUE);
		if(inputImage.getBDim()!=1)
		{
			throw (new PelicanException("Hue image must be monoband !"));
		} 
		
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
		int pixelValue = (int) Math.round(inputImage.getPixelXYZTDouble(x, y, z, t)/imageRange*360);
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
					int locPixelValue = (int) Math.round(inputImage.getPixelXYZTDouble(locX, locY, locZ, locT)/imageRange*360);
					int dist=Math.abs(pixelValue-locPixelValue);
					if(dist>180)
						dist=360-dist;
					if(dist<=alpha)
					{
						neighboursToExpand.add(new Point4D(locX, locY, locZ, locT));
						outputImage.setPixelXYZTInt(locX, locY, locZ, locT,INQUEUE);
					}
				}
			}
		}	
	}
	
	public static IntegerImage exec(Image inputImage, int alpha, Point4D[] neighbourhood, double imageRange) 
	{
		return (IntegerImage)new HueAlphaConnectivity().process(inputImage,alpha,neighbourhood,imageRange);
	}
}
