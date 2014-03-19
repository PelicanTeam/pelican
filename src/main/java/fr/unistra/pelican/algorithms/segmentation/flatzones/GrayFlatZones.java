package fr.unistra.pelican.algorithms.segmentation.flatzones;

import java.util.ArrayList;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.util.Point4D;


/**
 * Only deal with Gray Level Image. X,Y,Z,T dimensions are taken into account.
 * 
 * Algorithm is mine and quick made, probably better implementation exists but this one is quite fast.
 * 
 * @author Jonathan Weber
 *
 */
public class GrayFlatZones extends Algorithm {

	/**
	 * Image to process
	 */
	public Image inputImage;
	
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
	
	private int pixelValue;
	
	private int locX;
	private int locY;
	private int locZ;
	private int locT;
	
	private int pixelIndex;
	private int outputIndex;
	
	private int i;
	
	private int currentLabel;
	
	private Point4D currentPoint;

	
	/**
	 * Constructor
	 * 
	 */
	public GrayFlatZones() 
	{
		super();
		super.inputs = "inputImage,neighbourhood";
		super.outputs = "outputImage";
	}
	
	@Override
	public void launch() throws AlgorithmException 
	{
		if(inputImage.getBDim()==1)
		{
			xDim = inputImage.getXDim();
			yDim = inputImage.getYDim();
			zDim = inputImage.getZDim();
			tDim = inputImage.getTDim();
			
			neighboursToExpand = new ArrayList<Point4D>();
			
			outputImage = inputImage.newIntegerImage();
			outputImage.fill(INITVALUE);
		
			currentLabel=-1;
			pixelIndex=outputImage.size();
		
			for(int t=tDim;--t>=0;)
				for(int z=zDim;--z>=0;)
					for(int y=yDim;--y>=0;)
						for(int x=xDim;--x>=0;)
							if(outputImage.getPixelInt(--pixelIndex)==INITVALUE)
							{
								outputImage.setPixelInt(pixelIndex, ++currentLabel);
								addUnlabelledNeighboursRespectToKValueToQueue(x, y, z, t);
								while(neighboursToExpand.size()!=0)
								{
									expandCurrentLabelTo(neighboursToExpand.remove(0));
								}
							}						
		}
		else
		{
			throw new AlgorithmException("inputImage must be a gray level image");
		}
	}
	
	private final void expandCurrentLabelTo(Point4D pixel)
	{
				outputImage.setPixelXYZTInt(pixel.x, pixel.y, pixel.z, pixel.t, currentLabel);
				addUnlabelledNeighboursRespectToKValueToQueue(pixel.x, pixel.y, pixel.z, pixel.t);		
	}
	
	private final void addUnlabelledNeighboursRespectToKValueToQueue(int x, int y, int z, int t)
	{
		pixelValue = inputImage.getPixelXYZTByte(x, y, z, t);
		for(i=neighbourhood.length;--i>=0;)
		{
			currentPoint = neighbourhood[i];
			locX = x + currentPoint.x;
			locY = y + currentPoint.y;
			locZ = z + currentPoint.z;
			locT = t + currentPoint.t;
			if(locX>=0&&locY>=0&&locZ>=0&&locT>=0&&locX<xDim&&locY<yDim&&locZ<zDim&&locT<tDim)
			{
				outputIndex = outputImage.getLinearIndexXYZT_(locX, locY, locZ, locT);
				if(outputImage.getPixelInt(outputIndex)==INITVALUE)
				{
					if(pixelValue==inputImage.getPixelByte(outputIndex))
					{
						neighboursToExpand.add(new Point4D(locX, locY, locZ, locT));
						outputImage.setPixelInt(outputIndex,INQUEUE);
					}
				}
			}
		}	
	}
	
	public static IntegerImage exec(Image inputImage, Point4D[] neighbourhood) 
	{
		return (IntegerImage)new GrayFlatZones().process(inputImage,neighbourhood);
	}
}
