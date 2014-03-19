package fr.unistra.pelican.algorithms.segmentation.qfz.filtering;

import java.util.Arrays;

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
/***
 * This class is an implementation of the transition regions detection and deletion
 * process, proposed by Soille and Grazzini in
 * Constrained Connectivity and Transition Regions in ISMM 2009, LNCS 5720,
 * pp. 59-69, 2009
 * 
 * Only work on gray and RGB color
 * 
 * @author Jonathan Weber
 *
 */
public class TransitionRegionSuppression extends Algorithm {
	
	/**
	 * Input segmentation
	 */
	public IntegerImage inputSegmentation;
	
	/**
	 * Input image
	 */
	public Image inputImage;
	
	/**
	 * Connectivity under consideration for SRG
	 */
	public Point4D[] connectivity;
	
	/**
	 * Segmentation without transition regions
	 */
	public IntegerImage outputSegmentation;
	
	public TransitionRegionSuppression()
	{
		super.inputs="inputSegmentation,inputImage,connectivity";
		super.options="";
		super.outputs="outputSegmentation";
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
		outputSegmentation = inputSegmentation.newIntegerImage(true);
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
		// Check which region is a transition region
		boolean[] isRegionATransitionRegion = new boolean[inputSegmentation.maximumInt()+1];
		Arrays.fill(isRegionATransitionRegion, true);
		for(int i=0;i<bandSize;i++)
		{
			if(extremumMap.getPixelBoolean(i))
			{
				isRegionATransitionRegion[inputSegmentation.getPixelInt(i)]=false;
			}
		}
		/*int regionTransSum=0;
		for(int i=0;i<isRegionATransitionRegion.length;i++)
			if(isRegionATransitionRegion[i])
				regionTransSum++;*/
		//Set transitions regions to a SRG unlabeled
		for(int i=0;i<bandSize;i++)
		{
			if(isRegionATransitionRegion[inputSegmentation.getPixelInt(i)])
			{
				outputSegmentation.setPixelInt(i, SeededRegionGrowing.UNLABELED);	
			}
		}
		// Launch SRG process
		if(bDim==1)
		{
			outputSegmentation = SeededRegionGrowing.exec(inputImage, outputSegmentation, connectivity,true);
		} else if(bDim==3)
		{
			ThreeBandByteDistanceLUT lut=ThreeBandByteDistanceLUT.getClassicalL2LUT();
			outputSegmentation = SeededRegionGrowingBasedOnLUT.exec(inputImage, outputSegmentation, connectivity,lut,true);
		} else
		{
			throw new PelicanException("This number of bands ("+bDim+") is not managed for now");
		}
	}
	
	public static IntegerImage exec (IntegerImage inputSegmentation, Image inputImage, Point4D[] connectivity)
	{
		return (IntegerImage) new TransitionRegionSuppression().process(inputSegmentation, inputImage, connectivity);
	}

}
