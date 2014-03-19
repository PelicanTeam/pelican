package fr.unistra.pelican.algorithms.segmentation;

import java.util.Arrays;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;

/**
 * This algorithm performs a classic K-Means
 * 
 * Works in ND and with any number of bands
 * 
 * @author Jonathan Weber
 *
 */
public class KMeans extends Algorithm {

	/**
	 * Input Image
	 */
	public Image inputImage;
	
	/**
	 * Number of clusters
	 */
	public int k;
	
	/**
	 * Number max of iterations
	 */
	public int maxIter=Integer.MAX_VALUE;
	
	/**
	 * Label image
	 */
	public IntegerImage outputImage;
	
	private int[][] centroids;
	private int[] clusterSize;
	private IntegerImage currentMap;
	private int bDim;
	private int outputSize;
	
	public KMeans()
	{
		super.inputs="inputImage,k";
		super.options="maxIter";
		super.outputs="outputImage";
	}
	
	@Override
	public void launch() throws AlgorithmException 
	{
		bDim = inputImage.getBDim();
		currentMap = inputImage.newIntegerImage(inputImage.getXDim(), inputImage.getYDim(), inputImage.getZDim(), inputImage.getTDim(), 1);
		outputSize = currentMap.size();
		centroids = new int[k][bDim];
		clusterSize = new int[k];
		int iterations=0;
		//Initialize cluster
		for(int currentk=0;currentk<k;currentk++)
		{
			int pixel = ((int)(Math.random()*outputSize))*bDim;
			for(int b=0;b<bDim;b++)
			{
				centroids[currentk][b] = inputImage.getPixelByte(pixel+b);
			}			
		}
		affectPixelsToCluster();
		while(!outputImage.equals(currentMap)&&iterations<maxIter)
		{
			computeCentroids();
			affectPixelsToCluster();
			iterations++;
		}
	}
	
	public void affectPixelsToCluster()
	{
		outputImage = currentMap;
		currentMap = outputImage.newIntegerImage();
		double dMin;
		int[] currentValue = new int[3];
		int currentAssignedCluster=-1;
		for(int i=0;i<outputSize;i++)
		{
			dMin = Double.MAX_VALUE;
			int pixelLoc = i*bDim;
			for(int b=0;b<bDim;b++)
			{
				currentValue[b]=inputImage.getPixelByte(pixelLoc);
				pixelLoc++;
			}
			for(int currentk=0;currentk<k;currentk++)
			{
				double distance=0;
				for(int b=0;b<bDim;b++)
				{
					double attributeDistance = currentValue[b]-centroids[currentk][b];
					distance+= attributeDistance*attributeDistance;
				}
				if(distance<dMin)
				{
					dMin=distance;
					currentAssignedCluster=currentk;
				}
			}
			currentMap.setPixelInt(i,currentAssignedCluster);
		}
	}
	
	public void computeCentroids()
	{
		for(int currentk=0;currentk<k;currentk++)
			Arrays.fill(centroids[currentk], 0);
		Arrays.fill(clusterSize,0);
		for(int i=0;i<outputSize;i++)
		{
			int currentk = currentMap.getPixelInt(i);
			clusterSize[currentk]++;
			int pixelLoc = i*bDim;
			for(int b=0;b<bDim;b++)
			{
				centroids[currentk][b]+=inputImage.getPixelByte(pixelLoc);
				pixelLoc++;
			}			
		}
		for(int currentk=0;currentk<k;currentk++)
			for(int b=0;b<bDim;b++)
			{
				if(clusterSize[currentk]!=0)
					centroids[currentk][b]/=clusterSize[currentk];
			}
	}
	
	/**
	 * Performs a K-Means
	 * 
	 * @param inputImage
	 * @param k
	 * @return label image
	 */
	public static IntegerImage exec (Image inputImage,int k)
	{
		return (IntegerImage) new KMeans().process(inputImage,k);
	}
	
	/**
	 * Performs a K-Means
	 * 
	 * @param inputImage
	 * @param k
	 * @param maxIter 
	 * @return label image
	 */
	public static IntegerImage exec (Image inputImage,int k,int maxIter)
	{
		return (IntegerImage) new KMeans().process(inputImage,k,maxIter);
	}

}
