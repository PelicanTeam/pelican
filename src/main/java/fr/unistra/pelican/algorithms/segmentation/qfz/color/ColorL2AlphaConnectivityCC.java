package fr.unistra.pelican.algorithms.segmentation.qfz.color;


import java.util.ArrayList;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.util.Point4D;


/**
 * Compute connected components of the image according to alpha connectivity defined by local range alpha.
 * 
 * Alpha value is used in double precision but in byte range [0;255] (due to the euclidian distance value).
 * 
 * Only deal with color images. X,Y,Z,T dimensions are taken into account.
 * 
 * Algorithm is mine and quick made, probably better implementation exists but this one is quite fast.
 * 
 * @author Jonathan Weber
 *
 */
public class ColorL2AlphaConnectivityCC extends Algorithm {

	/**
	 * Image to process
	 */
	public Image inputImage;
	
	/**
	 * alpha value for local range
	 */
	public double alpha;
	
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
	
	private int pixelR;
	private int pixelG;
	private int pixelB;
	private int distanceR;
	private int distanceG;
	private int distanceB;
	
	private int locX;
	private int locY;
	private int locZ;
	private int locT;
	
	private int x;
	private int y;
	private int z;
	private int t;
		
	private int i;
	
	private int pixelIndex;
	private int neighbourIndex;
	
	private int currentLabel;

	
	/**
	 * Constructor
	 * 
	 */
	public ColorL2AlphaConnectivityCC() 
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
		
		neighboursToExpand = new ArrayList<Point4D>();		
		
		outputImage= inputImage.newIntegerImage(xDim,yDim,zDim,tDim,1);
		outputImage.fill(INITVALUE);
		
		currentLabel=-1;
		
		for(t=tDim;--t>=0;)
			for(z=zDim;--z>=0;)
				for(y=yDim;--y>=0;)
					for(x=xDim;--x>=0;)
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
		pixelIndex = inputImage.getLinearIndexXYZT_(x, y, z, t);
		pixelR = inputImage.getPixelByte(pixelIndex);
		pixelG = inputImage.getPixelByte(++pixelIndex);
		pixelB = inputImage.getPixelByte(++pixelIndex);		
		for(i=neighbourhood.length;--i>=0;)
		{
			locX = x + neighbourhood[i].x;
			locY = y + neighbourhood[i].y;
			locZ = z + neighbourhood[i].z;
			locT = t + neighbourhood[i].t;
			if(locX>=0&&locY>=0&&locZ>=0&&locT>=0&&locX<xDim&&locY<yDim&&locZ<zDim&&locT<tDim)
			{
				if(outputImage.getPixelXYZTInt(locX, locY, locZ, locT)==INITVALUE)
				{
					neighbourIndex = inputImage.getLinearIndexXYZT_(locX, locY, locZ, locT);
					distanceR = pixelR - inputImage.getPixelByte(neighbourIndex);
					distanceG = pixelG - inputImage.getPixelByte(++neighbourIndex);
					distanceB = pixelB - inputImage.getPixelByte(++neighbourIndex);
					if(Math.sqrt(distanceR*distanceR+distanceG*distanceG+distanceB*distanceB)<=alpha)
					{
						neighboursToExpand.add(new Point4D(locX, locY, locZ, locT));
						outputImage.setPixelXYZTInt(locX, locY, locZ, locT,INQUEUE);
					}
				}
			}
		}	
	}
	
	public static IntegerImage exec(Image inputImage,double alpha, Point4D[] neighbourhood) 
	{
		return (IntegerImage)new ColorL2AlphaConnectivityCC().process(inputImage,alpha,neighbourhood);
	}
}

