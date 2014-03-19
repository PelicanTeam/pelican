package fr.unistra.pelican.algorithms.spatial;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.arithmetic.Minimum;
import fr.unistra.pelican.algorithms.morphology.gray.GrayExternGradient;
import fr.unistra.pelican.algorithms.morphology.gray.GrayInternGradient;
import fr.unistra.pelican.algorithms.segmentation.SeededRegionGrowing;
import fr.unistra.pelican.algorithms.segmentation.SeededRegionGrowingBasedOnLUT;
import fr.unistra.pelican.util.Point4D;
import fr.unistra.pelican.util.lut.ThreeBandByteDistanceLUT;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;
import fr.unistra.pelican.util.neighbourhood.Neighbourhood4D;

/**
 * This class implements the contract enhancement method
 * proposed by P. Soille in
 * P. Soille, Constrained connectivity for the processing of very-high-resolution satellite
 * images, International Journal of Remote Sensing, 2010, 31: 22, 5879 — 5893
 * 
 * Designed for 2D images, work with video and 3D images but local extremas are bad extracted if so
 * 
 * @author Jonathan Weber
 *
 */

public class ContrastEnhancementSoille extends Algorithm {
	
	public Image inputImage;
	
	public Point4D[] connectivity=Neighbourhood4D.get8Neighboorhood();
	
	public Image outputImage;

	public ContrastEnhancementSoille()
	{
		super.inputs="inputImage";
		super.options="connectivity";
		super.outputs="outputImage";
	}
	
	@Override
	public void launch() throws AlgorithmException 
	{
		// Initialize data
		int xDim = inputImage.getXDim();
		int yDim = inputImage.getYDim();
		int zDim = inputImage.getZDim();
		int tDim = inputImage.getTDim();
		int bDim = inputImage.getBDim();
		int bandSize = xDim*yDim*zDim*tDim;
		// Create Map of extremum pixels
		BooleanImage extremumMap=new BooleanImage(xDim,yDim,zDim,tDim,1);
		extremumMap.fill(false);
		for(int b=0;b<bDim;b++)
		{
			Image localInput = inputImage.getImage4D(b, Image.B);
			Image erosionGradient = GrayInternGradient.exec(localInput, FlatStructuringElement2D.createSquareFlatStructuringElement(3));
			Image dilationGradient = GrayExternGradient.exec(localInput, FlatStructuringElement2D.createSquareFlatStructuringElement(3));
			Image min = Minimum.exec(erosionGradient, dilationGradient);
			for(int i=0;i<bandSize;i++)
			{
				if(min.getPixelByte(i)==0)
				{
					extremumMap.setPixelBoolean(i, true);
				}
			}
		}
		int nbLabel=extremumMap.getSum();
		int[][] correspondingColor = new int[nbLabel][bDim];
		IntegerImage srg= new IntegerImage(xDim,yDim,zDim,tDim,1);
		int label=0;
		for(int t=0;t<tDim;t++)
			for(int z=0;z<zDim;z++)
				for(int y=0;y<yDim;y++)
					for(int x=0;x<xDim;x++)
					{
						if(extremumMap.getPixelXYZTBoolean(x,y,z,t))
						{
							for(int b=0;b<bDim;b++)
							{
								correspondingColor[label][b]=inputImage.getPixelXYZTBByte(x, y, z, t, b);								
							}
							srg.setPixelXYZTInt(x,y,z,t, label++);
						} else
						{
							srg.setPixelXYZTInt(x,y,z,t, SeededRegionGrowing.UNLABELED);
						}
					}
		if(bDim==1)
		{
			srg=SeededRegionGrowing.exec(inputImage, srg, connectivity, true);
		} else if(bDim==3)
		{
			ThreeBandByteDistanceLUT lut=ThreeBandByteDistanceLUT.getClassicalL2LUT();
			srg=SeededRegionGrowingBasedOnLUT.exec(inputImage, srg, connectivity, lut,true);
		} else
		{
			throw new PelicanException("This number of bands ("+bDim+") is not managed for now");
		}
		outputImage=inputImage.copyImage(false);
		for(int t=0;t<tDim;t++)
			for(int z=0;z<zDim;z++)
				for(int y=0;y<yDim;y++)
					for(int x=0;x<xDim;x++)
					{
						int pixelLabel=srg.getPixelXYZTInt(x, y, z, t);
						for(int b=0;b<bDim;b++)
						{
							outputImage.setPixelXYZTBByte(x, y, z, t, b, correspondingColor[pixelLabel][b]);
						}
					}
	}
	
	public static <T extends Image> T exec (T inputImage)
	{
		return (T) new ContrastEnhancementSoille().process(inputImage);
	}

}
