package fr.unistra.pelican.algorithms.segmentation.flatzones;

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
public class ColorFlatZones extends Algorithm {

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
	
	private int locX;
	private int locY;
	private int locZ;
	private int locT;
	
	private int i;
	private int x;
	private int y;
	private int z;
	private int t;
	
	private int pixelR, pixelG,pixelB;
	
	private int pixelIndex;
	private int neighbourIndex;
	private int outputIndex;
	private int labelImageIndex;
	
	private int currentLabel;
	
	private Point4D currentPoint;

	
	/**
	 * Constructor
	 * 
	 */
	public ColorFlatZones() 
	{
		super();
		super.inputs = "inputImage,neighbourhood";
		super.outputs = "outputImage";
	}
	
	@Override
	public void launch() throws AlgorithmException 
	{
		if(inputImage.getBDim()==3)
		{
			xDim = inputImage.getXDim();
			yDim = inputImage.getYDim();
			zDim = inputImage.getZDim();
			tDim = inputImage.getTDim();
		
			neighboursToExpand = new ArrayList<Point4D>();		
		
			outputImage= new IntegerImage(xDim,yDim,zDim,tDim,1);
			outputImage.fill(INITVALUE);
		
			currentLabel=-1;
			labelImageIndex=outputImage.size();
		
			for(t=tDim;--t>=0;)
				for(z=zDim;--z>=0;)
					for(y=yDim;--y>=0;)
						for(x=xDim;--x>=0;)
							if(outputImage.getPixelInt(--labelImageIndex)==INITVALUE)
							{
								outputImage.setPixelInt(labelImageIndex, ++currentLabel);
								addUnlabelledNeighboursRespectToKValueToQueue(x, y, z, t);
								while(neighboursToExpand.size()!=0)
								{
									expandCurrentLabelTo(neighboursToExpand.remove(0));
								}
							}						
		}
		else
		{
			throw new AlgorithmException("InputImage must be a color image");
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
					neighbourIndex = outputIndex*3;
					if(pixelR == inputImage.getPixelByte(neighbourIndex)&& pixelG == inputImage.getPixelByte(++neighbourIndex) && pixelB == inputImage.getPixelByte(++neighbourIndex))
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
		return (IntegerImage)new ColorFlatZones().process(inputImage,neighbourhood);
	}
}

