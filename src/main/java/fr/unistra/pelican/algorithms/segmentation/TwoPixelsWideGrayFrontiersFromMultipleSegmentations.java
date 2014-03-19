package fr.unistra.pelican.algorithms.segmentation;

import java.util.ArrayList;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.util.Point4D;

/**
 * This class builds the two-pixels wide gray frontiers obtained
 * from multiple segmentations according to a certain neighbourhood
 *  
 * @author Jonathan Weber
 */
public class TwoPixelsWideGrayFrontiersFromMultipleSegmentations extends Algorithm {

	/**
	 * The input image
	 */
	public ArrayList<IntegerImage> segmentations;

	/**
	 * The specified neighbourhood
	 */
	public Point4D[] neighbours;

	/**
	 * The output image
	 */
	public ByteImage outputImage;

	/**
	 * Constructor
	 */
	public TwoPixelsWideGrayFrontiersFromMultipleSegmentations() {
		super.inputs = "segmentations,neighbours";
		super.outputs = "outputImage";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = segmentations.get(0).newByteImage();
		outputImage.fill(0);

		int xDim = outputImage.getXDim();
		int yDim = outputImage.getYDim();
		int zDim = outputImage.getZDim();
		int tDim = outputImage.getTDim();

		
		int neighboursLength = neighbours.length;
		Point4D currentNeighbour;
		int pX,pY,pZ,pT;
		int currentValue;
		for(int seg=0;seg<segmentations.size();seg++)
		{
			IntegerImage currentSeg = segmentations.get(seg);
			int currentIndex = -1;
			for(int t = 0; t< tDim; t++)
				for (int z = 0; z < zDim; z++)
					for (int y = 0; y < yDim; y++)
						for (int x = 0; x < xDim; x++)						
						{
							currentIndex++;
							currentValue = currentSeg.getPixelInt(currentIndex);
							for(int i=0;i<neighboursLength;i++)
							{
								currentNeighbour = neighbours[i];
								pX = x + currentNeighbour.x;
								pY = y + currentNeighbour.y;
								pZ = z + currentNeighbour.z;
								pT = t + currentNeighbour.t;							
								if(pX>=0 && pY>=0 && pZ>=0 && pT>=0 && pX<xDim && pY<yDim && pZ<zDim && pT<tDim)
								{
									if(currentValue != currentSeg.getPixelXYZTInt(pX, pY, pZ, pT))
									{
										outputImage.setPixelByte(currentIndex, outputImage.getPixelByte(currentIndex)+1);
										break;
									}
								}							
							}						
						}
		}
	}


	/**
	 * Return an BooleanImage of the two pixels wide gray frontiers of multiple
	 * segmentations according to a specific neighbourhood
	 */
	public static ByteImage exec(ArrayList<IntegerImage> segmentations, Point4D[] neighbours) {
		return (ByteImage) new TwoPixelsWideGrayFrontiersFromMultipleSegmentations().process(segmentations,neighbours);
	}
}


