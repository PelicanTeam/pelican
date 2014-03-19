package fr.unistra.pelican.algorithms.segmentation;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;


/**
 * This class performs a watershed segmentation using Multi-Degree Immersion Simulation
 * described in S. Peng and L. Gu. A novel implementation of watershed transform using
 * multi-degree immersion simulation. In 27th Annual International Conference
 * of the Engineering in Medicine and Biology Society, pages 1754-1757,2005.
 *  
 * It works by default on Byte resolution. The maximum number of created segment
 * is 2^31-1. It return an IntegerImage, the first segment as label
 * Integer.MIN_VALUE.
 * 
 * XY : 8-connectivity
 * XYT : 26-connectivity
 * XYZ : 26-connectivity
 * XYZTB : 80-connectivity, each band processed independantly
 * 
 * @author Jonathan Weber
 */
public class WatershedUsingMultiDegreeImmersion extends Algorithm {
	
	/**
	 * The input image
	 */
	public Image inputImage;


	/**
	 * The output image
	 */
	public Image outputImage;
	
	private int xDim;
	private int yDim;
	private int zDim;
	private int tDim;
	private int bDim;
		
	/**
	 * Constructor
	 */
	public WatershedUsingMultiDegreeImmersion() {
		super.inputs = "inputImage";
		super.options = "";
		super.outputs = "outputImage";

	}

	/**
	 * Performs a watershed segmentation Multi-Degree Immersion Simulation
	 * 
	 * @param inputImage The input image
	 * @return outputImage The output image
	 */
	public static IntegerImage exec(Image inputImage) {
		return (IntegerImage) new WatershedUsingMultiDegreeImmersion().process(inputImage);
	}
	
	@Override
	public void launch() throws AlgorithmException {
				
		Image newValues = inputImage.copyImage(false);
		xDim = inputImage.getXDim();
		yDim = inputImage.getYDim();
		zDim = inputImage.getZDim();
		tDim = inputImage.getTDim();
		bDim = inputImage.getBDim();
		
		
		if(xDim>1&&yDim>1&&zDim==1&&tDim==1&&bDim==1)//Spatial 2D case
		{
			for(int y=0;y<yDim;y++)
				for(int x=0;x<xDim;x++)
				{
					newValues.setPixelXYByte(x, y, Math.max(0, calculateDiffXY(x,y)));
				}
			outputImage = Watershed2.exec(newValues,Watershed2.CLOSESTVALUE);
		} else if (xDim>1&&yDim>1&&zDim==1&&tDim>1&&bDim==1) //Video case
		{
			for(int t=0;t<tDim;t++)
				for(int y=0;y<yDim;y++)
					for(int x=0;x<xDim;x++)
					{
						newValues.setPixelXYTByte(x, y, t, Math.max(0, calculateDiffXYT(x,y,t)));
					}
			outputImage = WatershedND.exec(newValues);
		} else if (xDim>1&&yDim>1&&zDim==1&&tDim>1&&bDim==1) //Spatial 3D case 
		{
			for(int z=0;z<zDim;z++)
				for(int y=0;y<yDim;y++)
					for(int x=0;x<xDim;x++)
					{
						newValues.setPixelXYTByte(x, y, z, Math.max(0, calculateDiffXYZ(x,y,z)));
					}
			outputImage = WatershedND.exec(newValues);
		} else //All the others cases but not optimized...
		{
			for(int t=0;t<tDim;t++)
				for(int z=0;z<zDim;z++)
					for(int y=0;y<yDim;y++)
						for(int x=0;x<xDim;x++)
							for(int b=0;b<bDim;b++)
							{
								newValues.setPixelXYZTBByte(x, y, z, t, b, Math.max(0, calculateDiffXYZTB(x,y,z,t,b)));
							}
			outputImage = WatershedND.exec(newValues);
		}
			
	}
	
	private int calculateDiffXY(int x, int y)
	{
		int diff=0;
		int pixelValue = inputImage.getPixelXYByte(x, y);
		for(int yNeighbour=-1;yNeighbour<=1;yNeighbour++)
			for(int xNeighbour=-1;xNeighbour<=1;xNeighbour++)
			{
				if(!(xNeighbour==0&&yNeighbour==0))
				{
					int _x=x+xNeighbour;
					int _y=y+yNeighbour;
					if(_x>=0&&_y>=0&&_x<xDim&&_y<yDim)
					{
						diff+= Math.abs(pixelValue - inputImage.getPixelXYByte(_x, _y));
					}
				}
			}
		diff/=8; // We take 8-connectivity into account
		return diff;
	}
	private int calculateDiffXYT(int x, int y, int t)
	{
		int diff=0;
		int pixelValue = inputImage.getPixelXYTByte(x, y, t);
		for(int tNeighbour=-1;tNeighbour<=1;tNeighbour++)
			for(int yNeighbour=-1;yNeighbour<=1;yNeighbour++)
				for(int xNeighbour=-1;xNeighbour<=1;xNeighbour++)
				{
					if(!(xNeighbour==0&&yNeighbour==0&&tNeighbour==0))
					{
						int _x=x+xNeighbour;
						int _y=y+yNeighbour;
						int _t=t+tNeighbour;
						if(_x>=0&&_y>=0&&_t>=0&&_x<xDim&&_y<yDim&&_t<tDim)
						{
							diff+= Math.abs(pixelValue - inputImage.getPixelXYTByte(_x, _y, _t));

						}
					}
				}
		diff/=26; // We take 26-connectivity into account
		return diff;
	}
	
	private int calculateDiffXYZ(int x, int y, int z)
	{
		int diff=0;
		int pixelValue = inputImage.getPixelXYTByte(x, y, z);
		for(int zNeighbour=-1;zNeighbour<=1;zNeighbour++)
			for(int yNeighbour=-1;yNeighbour<=1;yNeighbour++)
				for(int xNeighbour=-1;xNeighbour<=1;xNeighbour++)
				{
					if(!(xNeighbour==0&&yNeighbour==0&&zNeighbour==0))
					{
						int _x=x+xNeighbour;
						int _y=y+yNeighbour;
						int _z=z+zNeighbour;
						if(_x>=0&&_y>=0&&_z>=0&&_x<xDim&&_y<yDim&&_z<zDim)
						{
							diff+= Math.abs(pixelValue - inputImage.getPixelXYZByte(_x, _y, _z));

						}
					}
				}
		diff/=26; // We take 26-connectivity into account
		return diff;
	}
	
	private int calculateDiffXYZTB(int x, int y, int z, int t, int b)
	{
		int diff=0;
		int pixelValue = inputImage.getPixelXYTByte(x, y, z);
		for(int tNeighbour=-1;tNeighbour<=1;tNeighbour++)
			for(int zNeighbour=-1;zNeighbour<=1;zNeighbour++)
				for(int yNeighbour=-1;yNeighbour<=1;yNeighbour++)
					for(int xNeighbour=-1;xNeighbour<=1;xNeighbour++)
					{
						if(!(xNeighbour==0&&yNeighbour==0&&zNeighbour==0))
						{
							int _x=x+xNeighbour;
							int _y=y+yNeighbour;
							int _z=z+zNeighbour;
							int _t=t=t+tNeighbour;
							if(_x>=0&&_y>=0&&_z>=0&&_t>=0&&_x<xDim&&_y<yDim&&_z<zDim&&_t<tDim)
							{
								diff+= Math.abs(pixelValue - inputImage.getPixelXYZTBByte(_x, _y, _z, _t, b));
							}
						}
					}
		diff/=80; // We take 80-connectivity into account
		return diff;
	}
}
