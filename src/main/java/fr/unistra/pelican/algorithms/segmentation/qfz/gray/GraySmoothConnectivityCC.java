package fr.unistra.pelican.algorithms.segmentation.qfz.gray;

import java.util.ArrayList;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.conversion.AverageChannels;
import fr.unistra.pelican.algorithms.conversion.RGBToGray;
import fr.unistra.pelican.util.Point4D;

/**
 * Compute connected components of the image according to smooth connectivity defined by local range alpha.
 * 
 * Omega is optional parameter which induces a maximum variance for the pixels values of a same connected component
 * 
 * Alpha and omega values are used in byte precision.
 * 
 * Only deal with Gray Levels, non-gray images will be transformed. X,Y,Z,T dimensions are taken into account.
 * 
 * Algorithm is mine and quick made, probably better implementation exists but this one is quite fast.
 * 
 * 
 * @author Jonathan Weber
 *
 */
public class GraySmoothConnectivityCC extends Algorithm {

	/**
	 * Image to process
	 */
	public Image inputImage;
	
	/**
	 * alpha value for local range
	 */
	public int alpha;
	
	/**
	 * optional omega value for global range
	 */
	public int omega=-1;
	
	/**
	 * Connectivity used to determine the flat zones
	 */
	public Point4D[] neighbourhood;
	
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
	private int minValueOfCC;
	private int maxValueOfCC;
	
	
	/**
	 * Constructor
	 * 
	 */
	public GraySmoothConnectivityCC() 
	{
		super();
		super.inputs = "inputImage,alpha,neighbourhood";
		super.options = "omega";
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
		// Transforming image in grey level if not
		if(inputImage.getBDim()==3)
		{
			inputImage = RGBToGray.exec(inputImage);
		} else if(inputImage.getBDim()!=1)
		{
			inputImage = AverageChannels.exec(inputImage);
		}
		
		currentLabel=-1;
		
		for(int t=0;t<tDim;t++)
			for(int z=0;z<zDim;z++)
				for(int y=0;y<yDim;y++)
					for(int x=0;x<xDim;x++)
					{
						if(outputImage.getPixelXYZTInt(x, y, z, t)==INITVALUE)
						{
							outputImage.setPixelXYZTInt(x, y, z, t, ++currentLabel);
							if(omega>-1)
							{
								minValueOfCC = inputImage.getPixelXYZTByte(x, y, z, t);
								maxValueOfCC = inputImage.getPixelXYZTByte(x, y, z, t);
							}
							addUnlabelledNeighboursRespectToKValueToQueue(x, y, z, t);
							while(neighboursToExpand.size()!=0)
							{
								expandCurrentLabelTo(neighboursToExpand.get(0));
								neighboursToExpand.remove(0);
							}
						}
					}		
	}
	
	private void expandCurrentLabelTo(Point4D pixel)
	{
		
			ArrayList<Point4D> neighboursWithCurrentLabel = getNeighboursWithCurrentLabels(pixel.x, pixel.y, pixel.z, pixel.t);
			boolean isExpandPossible=true;
			int pixelValue = inputImage.getPixelXYZTByte(pixel.x, pixel.y, pixel.z, pixel.t);
			for(int i=0;i<neighboursWithCurrentLabel.size();i++)
			{
				Point4D neighbourPixel = neighboursWithCurrentLabel.get(i);
				if(Math.abs(pixelValue-inputImage.getPixelXYZTByte(neighbourPixel.x, neighbourPixel.y, neighbourPixel.z, neighbourPixel.t))>alpha)
				{
					isExpandPossible=false;
				} 				
			}
			if(omega>-1&&isExpandPossible)
			{
				if(pixelValue>maxValueOfCC)
				{
					if(pixelValue-minValueOfCC>omega)
					{
						isExpandPossible=false;
					}
					else
					{
						maxValueOfCC=pixelValue;
					}
				} 
				else if(pixelValue<minValueOfCC)
				{
					if(maxValueOfCC-pixelValue>omega)
					{
						isExpandPossible=false;
					}
					else
					{
						minValueOfCC=pixelValue;
					}
				}
			}
			if(isExpandPossible)
			{
				outputImage.setPixelXYZTInt(pixel.x, pixel.y, pixel.z, pixel.t, currentLabel);
				addUnlabelledNeighboursRespectToKValueToQueue(pixel.x, pixel.y, pixel.z, pixel.t);
			}
			else
			{
				outputImage.setPixelXYZTInt(pixel.x, pixel.y, pixel.z, pixel.t, INITVALUE);
			}
		
	}
	
	private ArrayList<Point4D> getNeighboursWithCurrentLabels(int x,int y, int z, int t)
	{
		ArrayList<Point4D> neighboursWithCurrentLabel = new ArrayList<Point4D>();
		for(int i=0;i<neighbourhood.length;i++)
		{
			int locX = x + neighbourhood[i].x;
			int locY = y + neighbourhood[i].y;
			int locZ = z + neighbourhood[i].z;
			int locT = t + neighbourhood[i].t;
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
		int pixelValue = inputImage.getPixelXYZTByte(x, y, z, t);
		for(int i=0;i<neighbourhood.length;i++)
		{
			int locX = x + neighbourhood[i].x;
			int locY = y + neighbourhood[i].y;
			int locZ = z + neighbourhood[i].z;
			int locT = t + neighbourhood[i].t;
			if(locX>=0&&locY>=0&&locZ>=0&&locT>=0&&locX<xDim&&locY<yDim&&locZ<zDim&&locT<tDim)
			{
				if(outputImage.getPixelXYZTInt(locX, locY, locZ, locT)==INITVALUE)
				{
					if(Math.abs(pixelValue-inputImage.getPixelXYZTByte(locX, locY, locZ, locT))<=alpha)
					{
						neighboursToExpand.add(new Point4D(locX, locY, locZ, locT));
						outputImage.setPixelXYZTInt(locX, locY, locZ, locT,INQUEUE);
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
	public static IntegerImage exec(Image inputImage, int alpha, Point4D[] neighbourhood) 
	{
		return (IntegerImage)new GraySmoothConnectivityCC().process(inputImage,alpha,neighbourhood);
	}
	
	/**
	 * 
	 * @param inputImage
	 * @param alpha local range in byte precision
	 * @param neighbourhood neighbourhood under consideration in Point4D array
	 * @param omega global range in byte precision
	 * @return
	 */
	public static IntegerImage exec(Image inputImage, int alpha, Point4D[] neighbourhood, int omega) 
	{
		return (IntegerImage)new GraySmoothConnectivityCC().process(inputImage, alpha, neighbourhood, omega);
	}

}
