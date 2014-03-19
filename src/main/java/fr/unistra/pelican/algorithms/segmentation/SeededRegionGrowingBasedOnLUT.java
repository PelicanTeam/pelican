package fr.unistra.pelican.algorithms.segmentation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.util.Point4D;

import fr.unistra.pelican.util.lut.ThreeBandByteDistanceLUT;

/**
 * This class performs the seed region growing according to
 * the algorithm presented in R. Adams and L. Bischof. 
 * Seeded region growing. IEEE Transaction on Pattern
 * Analysis and Machine Intelligence, 16(6) :641–647, 1994.
 * 
 * @author Jonathan Weber
 *
 */
public class SeededRegionGrowingBasedOnLUT extends Algorithm {
	
	/**
	 * Original Image
	 */
	public Image inputImage;
	
	/**
	 * Seeds image
	 * 
	 * Label seed from 0 to Integer.MAX_VALUE
	 * Rest of the image set to SeededRegionGrowingBasedOnLUT.UNLABELED
	 * 
	 */
	public IntegerImage seeds;
	
	/**
	 * Neighbourhood taken into account
	 */
	public Point4D[] neighbourhood;
	
	/**
	 * Pre-computed LUT for distance value
	 */
	public ThreeBandByteDistanceLUT lut;
	
	/**
	 * Option
	 * 
	 * Indicates if the SRG frontiers 
	 * have to be merged into regions
	 * 
	 */
	public boolean mergeFrontiers=false;
	
	/**
	 * Seed region growing result
	 * 
	 * Frontiers label is the highest one (if not merged into regions)
	 */
	public IntegerImage outputImage;
	
	public static final int UNLABELED=SeededRegionGrowing.UNLABELED;
	public static final int IGNORE=SeededRegionGrowing.IGNORE;
	public static int FRONTIER;
	
	public SeededRegionGrowingBasedOnLUT()
	{
		super.inputs="inputImage,seeds,neighbourhood,lut";
		super.options="mergeFrontiers";
		super.outputs="outputImage";
	}
	
	@Override
	public void launch() throws AlgorithmException 
	{
		int xDim = inputImage.getXDim();
		int yDim = inputImage.getYDim();
		int zDim = inputImage.getZDim();
		int tDim = inputImage.getTDim();
		int bDim = inputImage.getBDim();
		outputImage=seeds.copyImage(true);
		int size = outputImage.size();
		double[] pointDelta = new double[size];
		Arrays.fill(pointDelta, Double.MAX_VALUE);
		FRONTIER=outputImage.maximumInt()+1;
		
		if(bDim!=3)
		{
			throw(new PelicanException("Number of bands ("+bDim+") not supported yet !"));
		}
		
		//Initialize region means
		RegionMeans[] means = new RegionMeans[outputImage.maximumInt()+1];
		for(int i=0;i<means.length;i++)
			means[i]= new RegionMeans();
		for(int i=0;i<size;i++)
			if(outputImage.getPixelInt(i)!=UNLABELED&&outputImage.getPixelInt(i)!=IGNORE)
			{
				means[outputImage.getPixelInt(i)].addPixel(inputImage.getVectorPixelByte(i*bDim));
			}		
		//Init graph
		
		// Init SSL
		TreeMap<Double,ArrayList<Point4D>> ssl = new TreeMap<Double,ArrayList<Point4D>>();
		for(int t=0;t<tDim;t++)
			for(int z=0;z<zDim;z++)
				for(int y=0;y<yDim;y++)
					for(int x=0;x<xDim;x++)
					{
						if(outputImage.getPixelXYZTInt(x, y, z, t)==UNLABELED)
						{
							double delta = Double.MAX_VALUE;
							for(Point4D n : neighbourhood)
							{
								int locX = x+n.x;
								int locY = y+n.y;
								int locZ = z+n.z;
								int locT = t+n.t;
								if(!outputImage.isOutOfBoundsXYZT(locX, locY, locZ, locT))
								{
									int label=outputImage.getPixelXYZTInt(locX, locY, locZ, locT);
									if(label!=UNLABELED&&label!=IGNORE)
									{
										int[] dist = new int[3];
										for(int i=0;i<3;i++)
										{
											dist[i]=(int)Math.abs(inputImage.getPixelXYZTBByte(x, y, z, t,i)-means[label].getMean()[i]);
										}
										delta=Math.min(delta, lut.get(dist[0], dist[1], dist[2]));
									}
								}
							}
							if(delta!=Double.MAX_VALUE)
							{
								addToSSL(new Point4D(x,y,z,t),delta,ssl,pointDelta);
							}
						}
					}
		//Main Algorithm
		int count=0;
		while(!ssl.isEmpty())
		{
			count++;
			/*if(count%10000==0)
				System.out.println(count+"/"+seeds.size());*/
			Point4D point = getFirstFromSSL(ssl);
			if(seeds.getPixelXYZTInt(point.x, point.y, point.z, point.t)==UNLABELED)
			{
				int currentLabel=-1;
				boolean frontierFlag=false;
				for(Point4D n : neighbourhood)
				{
					int locX = point.x+n.x;
					int locY = point.y+n.y;
					int locZ = point.z+n.z;
					int locT = point.t+n.t;
					if(!outputImage.isOutOfBoundsXYZT(locX, locY, locZ, locT))
					{
						int neighbourLabel = outputImage.getPixelXYZTInt(locX,locY,locZ,locT);
						if(neighbourLabel!=UNLABELED&&neighbourLabel!=FRONTIER&&neighbourLabel!=IGNORE)
						{
							if(currentLabel==-1)
							{
								currentLabel=neighbourLabel;
							}
							else if(currentLabel!=neighbourLabel)
							{
								frontierFlag=true;
								break;
							}
						}
					}
				}
				if(frontierFlag)
				{
					outputImage.setPixelXYZTInt(point.x, point.y, point.z, point.t, FRONTIER);
					//System.out.println("Frontier : "+FRONTIER);
				}
				else
				{
					outputImage.setPixelXYZTInt(point.x, point.y, point.z, point.t, currentLabel);
					//System.out.println("Label : "+currentLabel);
					means[currentLabel].addPixel(inputImage.getVectorPixelXYZTByte(point.x, point.y, point.z, point.t));
					for(Point4D n : neighbourhood)
					{
						int locX = point.x+n.x;
						int locY = point.y+n.y;
						int locZ = point.z+n.z;
						int locT = point.t+n.t;
						if(!outputImage.isOutOfBoundsXYZT(locX, locY, locZ, locT))
						{
							if(outputImage.getPixelXYZTInt(locX, locY, locZ, locT)==UNLABELED)
							{
								int[] dist = new int[3];
								for(int i=0;i<3;i++)
								{
									dist[i]=(int)Math.abs(inputImage.getPixelXYZTBByte(locX, locY, locZ, locT,i)-means[currentLabel].getMean()[i]);
								}							
								addToSSL(new Point4D(locX, locY, locZ, locT),lut.get(dist[0], dist[1], dist[2]),ssl,pointDelta);
							}
						}
					}
				}
			}
		}
		//In some particular configurations there are still unlabeled pixels
		//here we put all these pixels to frontier values
		for(int i=0;i<outputImage.size();i++)
		{
			if(outputImage.getPixelInt(i)==UNLABELED)
			{
				outputImage.setPixelInt(i, FRONTIER);
			}
		}
		if(mergeFrontiers)
		{
			do
			{
			BooleanImage frontiers = outputImage.newBooleanImage();
			for(int i=0;i<frontiers.size();i++)
				if(outputImage.getPixelInt(i)==FRONTIER)
					frontiers.setPixelBoolean(i, true);
				else
					frontiers.setPixelBoolean(i, false);
			int currentIndex=0;
			for(int t=0;t<tDim;t++)
				for(int z=0;z<zDim;z++)
					for(int y=0;y<yDim;y++)
						for(int x=0;x<xDim;x++,currentIndex++)
						{
							if(frontiers.getPixelBoolean(currentIndex))
							{
								int[] pixelValue = inputImage.getVectorPixelByte(currentIndex*bDim);
								int currentLabel=FRONTIER;
								double currentDelta=Double.MAX_VALUE;
								for(Point4D n : neighbourhood)
								{
									int locX = x+n.x;
									int locY = y+n.y;
									int locZ = z+n.z;
									int locT = t+n.t;
									if(locX>=0&&locX<xDim&&locY>=0&&locY<yDim&&locZ>=0&&locZ<zDim&&locT>=0&&locT<tDim)
									{
										int locIndex = outputImage.getLinearIndexXYZT_(locX, locY, locZ, locT);
										if(!frontiers.getPixelBoolean(locIndex)&&outputImage.getPixelInt(locIndex)!=IGNORE)
										{
											int[] neighbourValue = inputImage.getVectorPixelByte(locIndex*bDim);
											double locDelta = lut.get(Math.abs(pixelValue[0]-neighbourValue[0]),Math.abs(pixelValue[1]-neighbourValue[1]),Math.abs(pixelValue[2]-neighbourValue[2]));
											if(locDelta<currentDelta)
											{
												currentLabel=outputImage.getPixelInt(locIndex);
												currentDelta=locDelta;
											}
										}
									}
								}
								outputImage.setPixelInt(currentIndex, currentLabel);
							}
						}
			}while(outputImage.maximumInt()==FRONTIER);
		}
	}
	
	private void addToSSL(Point4D point, double delta,TreeMap<Double,ArrayList<Point4D>> ssl, double[] pointDelta)
	{
		boolean addPoint=true;
		if(pointDelta[outputImage.getLinearIndexXYZT_(point.x, point.y, point.z, point.t)]!=Double.MAX_VALUE)
		{
			if(delta<pointDelta[outputImage.getLinearIndexXYZT_(point.x, point.y, point.z, point.t)])
			{
				ArrayList<Point4D> tmp = ssl.get(pointDelta[outputImage.getLinearIndexXYZT_(point.x, point.y, point.z, point.t)]);
				tmp.remove(point);
				if(tmp.isEmpty())
				{
					ssl.remove(pointDelta[outputImage.getLinearIndexXYZT_(point.x, point.y, point.z, point.t)]);
				}
			}
			else
			{
				addPoint=false;
			}
		}
		if(addPoint)
		{
			pointDelta[outputImage.getLinearIndexXYZT_(point.x, point.y, point.z, point.t)]=delta;
			if(ssl.containsKey(delta))
			{
				ssl.get(delta).add(point);
			} else
			{
				ArrayList<Point4D> tmp = new ArrayList<Point4D>();
				tmp.add(point);
				ssl.put(delta, tmp);
			}
		}		
	}
	
	private Point4D getFirstFromSSL(TreeMap<Double,ArrayList<Point4D>> ssl)
	{
		ArrayList<Point4D> tmp = ssl.get(ssl.firstKey());
		Point4D point = tmp.remove(0);
		if(tmp.isEmpty())
		{
			ssl.pollFirstEntry();
		}
		return point;
	}
	
	/**
	 * Perform a seed region growing
	 * 
	 * @param inputImage Original Image
	 * @param seeds Seeds image
	 * @param neighbourhood Neighbourhood taken into account
	 * @return segmentation
	 */
	public static final IntegerImage exec(Image inputImage, IntegerImage seeds, Point4D[] neighbourhood, ThreeBandByteDistanceLUT lut)
	{
		return (IntegerImage)new SeededRegionGrowingBasedOnLUT().process(inputImage,seeds,neighbourhood,lut);
	}
	/**
	 * Perform a seed region growing
	 * 
	 * @param inputImage Original Image
	 * @param seeds Seeds image
	 * @param neighbourhood Neighbourhood taken into account
	 * @param mergeFrontiers true if the frontiers have to be merged into regions
	 * @return segmentation
	 */
	public static final IntegerImage exec(Image inputImage, IntegerImage seeds, Point4D[] neighbourhood, ThreeBandByteDistanceLUT lut, boolean mergeFrontiers)
	{
		return (IntegerImage)new SeededRegionGrowingBasedOnLUT().process(inputImage,seeds,neighbourhood,lut,mergeFrontiers);
	}
	
	private class RegionMeans
	{
		double[] sumOfPixelValues;
		double numberOfPixels;
		
		
		public RegionMeans()
		{
			sumOfPixelValues=new double[3];
			numberOfPixels=0.;
			Arrays.fill(sumOfPixelValues, 0.);
		}
		
		public double[] getMean()
		{
			double[] means = new double[3];
			for(int i=0;i<3;i++)
				means[i]=sumOfPixelValues[i]/numberOfPixels;
			return means;
		}
		
		public void addPixel(int[] pixelValues)
		{
			numberOfPixels++;
			for(int i=0;i<3;i++)
				sumOfPixelValues[i]+=pixelValues[i];
		}
	}

}
