package fr.unistra.pelican.algorithms.segmentation;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.Point4D;


/**
 * This class builds the two-pixels wide frontiers obtained
 * from a segmentation according to a certain neighbourhood
 *  
 * @author Jonathan Weber
 */
public class TwoPixelsWideFrontiersFromSegmentation extends Algorithm {

	/**
	 * The input image
	 */
	public Image inputImage;

	/**
	 * The specified neighbourhood
	 */
	public Point4D[] neighbours;
	
	/**
	 * The output image
	 */
	public Image outputImage;

	/**
	 * Constructor
	 */
	public TwoPixelsWideFrontiersFromSegmentation() {
		super.inputs = "inputImage,neighbours";
		super.outputs = "outputImage";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = inputImage.newBooleanImage();
		outputImage.fill(0);

		int xDim = outputImage.getXDim();
		int yDim = outputImage.getYDim();
		int zDim = outputImage.getZDim();
		int tDim = outputImage.getTDim();
		
		int currentIndex = -1;
		int neighboursLength = neighbours.length;
		Point4D currentNeighbour;
		int pX,pY,pZ,pT;
		int currentValue;
		
		for(int t = 0; t< tDim; t++)
			for (int z = 0; z < zDim; z++)
				for (int y = 0; y < yDim; y++)
					for (int x = 0; x < xDim; x++)						
					{
						currentIndex++;
						currentValue = inputImage.getPixelInt(currentIndex);
						for(int i=0;i<neighboursLength;i++)
						{
							currentNeighbour = neighbours[i];
							pX = x + currentNeighbour.x;
							pY = y + currentNeighbour.y;
							pZ = z + currentNeighbour.z;
							pT = t + currentNeighbour.t;							
							if(pX>=0 && pY>=0 && pZ>=0 && pT>=0 && pX<xDim && pY<yDim && pZ<zDim && pT<tDim)
							{
								if(currentValue != inputImage.getPixelXYZTInt(pX, pY, pZ, pT))
								{
									outputImage.setPixelBoolean(currentIndex, true);
									break;
								}
							}							
						}						
					}
	}

	
	/**
	 * Return an BooleanImage of the two pixels wide frontiers of a segmentation
	 * according to a specific neighbourhood
	 */
	public static BooleanImage exec(Image segmentation, Point4D[] neighbours) {
		return (BooleanImage) new TwoPixelsWideFrontiersFromSegmentation().process(segmentation,neighbours);
	}
}

