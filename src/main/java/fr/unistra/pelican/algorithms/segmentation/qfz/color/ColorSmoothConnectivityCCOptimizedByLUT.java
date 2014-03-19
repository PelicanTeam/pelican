package fr.unistra.pelican.algorithms.segmentation.qfz.color;

import java.util.ArrayList;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.util.Point4D;
import fr.unistra.pelican.util.lut.ThreeBandByteDistanceLUT;

/**
 * Compute connected components of the image according to smooth connectivity defined by local range alpha.
 * 
 * The distance measure used is introduced by a pre-computed LUT.
 * 
 * Alpha value is used in double precision but in byte range [0;255].
 * 
 * Only deal with color images. X,Y,Z,T dimensions are taken into account.
 * 
 * Algorithm is mine and quick made, probably better implementation exists but this one is quite fast.
 * 
 * @author Jonathan Weber
 *
 */
public class ColorSmoothConnectivityCCOptimizedByLUT extends Algorithm{
	
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
	public Point4D[] neighbourhood;
	
	/**
	 * LUT representing the specified distance
	 */
	public ThreeBandByteDistanceLUT lut;
	
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
	
	private int locX;
	private int locY;
	private int locZ;
	private int locT;
	
	private boolean isExpandPossible;
	private int pixelIndex;
	private int pixelR;
	private int pixelG;
	private int pixelB;
	
	private Point4D neighbourPixel;
	private int neighbourIndex;
	
	private double[] distanceLut;
	
	/**
	 * Constructor
	 * 
	 */
	public ColorSmoothConnectivityCCOptimizedByLUT() 
	{
		super();
		super.inputs = "inputImage,alpha,neighbourhood,lut";
		super.options = "";
		super.outputs = "outputImage";
	}
	
	@Override
	public void launch() throws AlgorithmException {
		
		xDim = inputImage.getXDim();
		yDim = inputImage.getYDim();
		zDim = inputImage.getZDim();
		tDim = inputImage.getTDim();
		
		distanceLut = lut.lut;
		
		neighboursToExpand = new ArrayList<Point4D>();		
		
		// Transforming image in grey level if not
		if(inputImage.getBDim()==3)
		{
			outputImage= inputImage.newIntegerImage(xDim,yDim,zDim,tDim,1);
			outputImage.fill(INITVALUE);
			
			currentLabel=-1;
			int index = outputImage.size();
			for(int t=tDim;--t>=0;)
				for(int z=zDim;--z>=0;)
					for(int y=yDim;--y>=0;)
						for(int x=xDim;--x>=0;)
						{
							
							if(outputImage.getPixelInt(--index)==INITVALUE)
							{
								outputImage.setPixelInt(index, ++currentLabel);
								addUnlabelledNeighboursRespectToKValueToQueue(x, y, z, t);
								while(neighboursToExpand.size()!=0)
								{
									expandCurrentLabelTo(neighboursToExpand.get(0));
									neighboursToExpand.remove(0);
								}
							}
						}
		}
		else
		{
			throw new AlgorithmException("inputImage must be a color image");
		}
	}
	
	private void expandCurrentLabelTo(Point4D pixel)
	{		
			ArrayList<Point4D> neighboursWithCurrentLabel = getNeighboursWithCurrentLabels(pixel.x, pixel.y, pixel.z, pixel.t);
			isExpandPossible=true;
			pixelIndex = inputImage.getLinearIndexXYZT_(pixel.x, pixel.y, pixel.z, pixel.t);
			pixelR = inputImage.getPixelByte(pixelIndex);
			pixelG = inputImage.getPixelByte(pixelIndex+1);
			pixelB = inputImage.getPixelByte(pixelIndex+2);
			
			for(int i=neighboursWithCurrentLabel.size();--i>=0;)
			{
				neighbourPixel = neighboursWithCurrentLabel.get(i);
				neighbourIndex = inputImage.getLinearIndexXYZT_(neighbourPixel.x, neighbourPixel.y, neighbourPixel.z, neighbourPixel.t);
				if(distanceLut[(Math.abs(pixelR - inputImage.getPixelByte(neighbourIndex))<<8|Math.abs(pixelG - inputImage.getPixelByte(neighbourIndex+1)))<<8|Math.abs(pixelB - inputImage.getPixelByte(neighbourIndex+2))]>alpha)
				{
					isExpandPossible=false;
				} 				
			}
			if(isExpandPossible)
			{
				outputImage.setPixelInt(pixelIndex/3, currentLabel);
				addUnlabelledNeighboursRespectToKValueToQueue(pixel.x, pixel.y, pixel.z, pixel.t);
			}
			else
			{
				outputImage.setPixelInt(pixelIndex/3, INITVALUE);
			}
		
	}
	
	private ArrayList<Point4D> getNeighboursWithCurrentLabels(int x,int y, int z, int t)
	{
		ArrayList<Point4D> neighboursWithCurrentLabel = new ArrayList<Point4D>();
		for(int i=neighbourhood.length;--i>=0;)
		{
			locX = x + neighbourhood[i].x;
			locY = y + neighbourhood[i].y;
			locZ = z + neighbourhood[i].z;
			locT = t + neighbourhood[i].t;
			if(locX>=0&&locY>=0&&locZ>=0&&locT>=0&&locX<xDim&&locY<yDim&&locZ<zDim&&locT<tDim)
			{
				if(outputImage.getPixelXYZTInt(locX, locY, locZ, locT)==currentLabel)
				{
					neighboursWithCurrentLabel.add(new Point4D(locX, locY, locZ, locT));					
				}
			}
		}				
		return neighboursWithCurrentLabel;
	}
	
	private void addUnlabelledNeighboursRespectToKValueToQueue(int x, int y, int z, int t)
	{
		pixelIndex = inputImage.getLinearIndexXYZT_(x, y, z, t);
		pixelR = inputImage.getPixelByte(pixelIndex);
		pixelG = inputImage.getPixelByte(pixelIndex+1);
		pixelB = inputImage.getPixelByte(pixelIndex+2);		
		
		for(int i=neighbourhood.length;--i>=0;)
		{
			locX = x + neighbourhood[i].x;
			locY = y + neighbourhood[i].y;
			locZ = z + neighbourhood[i].z;
			locT = t + neighbourhood[i].t;
			if(locX>=0&&locY>=0&&locZ>=0&&locT>=0&&locX<xDim&&locY<yDim&&locZ<zDim&&locT<tDim)
			{
				neighbourIndex = inputImage.getLinearIndexXYZT_(locX, locY, locZ, locT);
				if(outputImage.getPixelInt(neighbourIndex/3)==INITVALUE)
				{
					if(distanceLut[(Math.abs(pixelR - inputImage.getPixelByte(neighbourIndex))<<8|Math.abs(pixelG - inputImage.getPixelByte(neighbourIndex+1)))<<8|Math.abs(pixelB - inputImage.getPixelByte(neighbourIndex+2))]<=alpha)
					{
						neighboursToExpand.add(new Point4D(locX, locY, locZ, locT));
						outputImage.setPixelInt(neighbourIndex/3,INQUEUE);
					}
				}
			}
		}	
	}
	
	/**
	 * 
	 * @param inputImage
	 * @param alpha  local range in byte precision
	 * @param neighbourhood neighbourhood under consideration in Point4D array
	 * @return
	 */
	public static IntegerImage exec(Image inputImage, double alpha, Point4D[] neighbourhood,ThreeBandByteDistanceLUT lut) 
	{
		return (IntegerImage)new ColorSmoothConnectivityCCOptimizedByLUT().process(inputImage,alpha,neighbourhood,lut);
	}

}

