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
 * Gray level connected component analysis
 * 
 * - alpha is the local range limit: neighbor pixel p and q are alpha-Connected <=> |f(p)-f(q)|<=alpha
 * - pixels p q are alpha connected if their exists a set of pixels p_i forming a path from p to q and each p_i p_i+1 are alpha connected
 * - omega is the global rang: maximum range beetwen two pixel of a alpha,omega connected component is omega
 * 
 * Contrary to Soille's there is no results unicity.
 * 
 * Work in byte precision => give alpha and omega as byte values 
 * 
 * Deal with X-Y-Z-T dim
 * 
 * @author Jonathan Weber
 *
 */
public class GrayAlphaOmegaConnectivityCC extends Algorithm 
{
	
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
	public int omega;
	
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
	 */
	public GrayAlphaOmegaConnectivityCC() 
	{
		super();
		super.inputs = "inputImage,alpha,omega,neighbourhood";
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
							minValueOfCC = inputImage.getPixelXYZTByte(x, y, z, t);
							maxValueOfCC = inputImage.getPixelXYZTByte(x, y, z, t);

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
		
			boolean isExpandPossible=true;
			int pixelValue = inputImage.getPixelXYZTByte(pixel.x, pixel.y, pixel.z, pixel.t);
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
	
	public static IntegerImage exec(Image inputImage, int alpha, int omega, Point4D[] neighbourhood) 
	{
		return (IntegerImage)new GrayAlphaOmegaConnectivityCC().process(inputImage,alpha,omega,neighbourhood);
	}
	

}
