package fr.unistra.pelican.algorithms.segmentation.qfz.color;

import java.util.ArrayList;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.util.FIFOQueue;
import fr.unistra.pelican.util.Point4D;
import fr.unistra.pelican.util.qfz.MultivariateAlphaLogicalPredicate;
import fr.unistra.pelican.util.qfz.MultivariateLogicalPredicate;
import fr.unistra.pelican.util.qfz.MultivariateScalarAlphaLogicalPredicate;

/**
 * This class applied multivariate logical predicate connectivity on region
 * from a pre-segmentation instead of directly on pixels 
 *
 * Only works with Scalar Alpha for now.
 * 
 * TODO : Dichotomic alpha management and vectorial alpha management 
 * 
 * @author Jonathan Weber
 *
 */

public class MultivariateLogicalPredicateConnectivityAppliedOnRegion extends Algorithm
{	
	
	/**
	 * Original video
	 */
	public Image originalImage;
	
	/**
	 * Segmented video
	 */
	public IntegerImage segmentationImage;
	
	/**
	 * Neighbourhoud under consideration
	 */
	public Point4D[] neighbourhood;
	
	/**
	 * Predicate for alpha
	 */
	public MultivariateScalarAlphaLogicalPredicate alphaPred;
	
	/**
	 * ArrayList of logical predicates
	 */
	public ArrayList<MultivariateLogicalPredicate> predicates;
	
	/**
	 * Unsafe parameter
	 */
	public boolean unsafe=false;
	
	/**
	 * Resulting segmented video
	 */
	public IntegerImage mergedSegmentationImage;
	
	private final int UNLABELLED=-1;
	private final int INQUEUE=-2;
	
	public FIFOQueue<Region> fifoQ;
	public ArrayList<Region> currentQFZ;
	public Region currentRegion;
	public int currentNewLabel;
	
	private MultivariateLogicalPredicate[] localPredicates;
	private MultivariateLogicalPredicate[] globalPredicates;
	private MultivariateLogicalPredicate[] allPredicates;
	
	
	@Override
	public void launch()
	{
		int videoSize = segmentationImage.size();
		int nbRegions = segmentationImage.maximumInt()+1;
		int xDim = originalImage.getXDim();
		int yDim = originalImage.getYDim();
		int zDim = originalImage.getZDim();
		int tDim = originalImage.getTDim();
		int bDim = originalImage.getBDim();
		fifoQ = new FIFOQueue<Region>();
		currentQFZ = new ArrayList<Region>();
		alphaPred.resetCurrentAlpha();
		allPredicates=new MultivariateLogicalPredicate[predicates.size()];
		int nbLoc=0,nbGlo=0;
		for(int i=0;i<allPredicates.length;i++)
		{
			allPredicates[i]=predicates.get(i);
			if(allPredicates[i].isLocal())
			{
				nbLoc++;
			}
			else
			{
				nbGlo++;
			}
		}
		localPredicates= new MultivariateLogicalPredicate[nbLoc];
		globalPredicates= new MultivariateLogicalPredicate[nbGlo];
		nbLoc=0;
		nbGlo=0;
		for(int i=0;i<allPredicates.length;i++)
		{
			if(allPredicates[i].isLocal())
			{
				localPredicates[nbLoc++]=allPredicates[i];
			}
			else
			{
				globalPredicates[nbGlo++]=allPredicates[i];
			}
		}
		//Create regions and add to the graph
		Region[] listOfRegions = new Region[nbRegions];
		int[] nbPixelsOfRegion = new int[nbRegions];
		for(int t=0;t<tDim;t++)
			for(int z=0;z<zDim;z++)
				for(int y=0;y<yDim;y++)
					for(int x=0;x<xDim;x++)
					{
						int regionLabel = segmentationImage.getPixelXYZTInt(x,y,z,t);
						if(listOfRegions[regionLabel]==null)
						{
							listOfRegions[regionLabel]= new Region(regionLabel,originalImage.getVectorPixelXYZTByte(x, y, z, t));
							nbPixelsOfRegion[regionLabel]=1;
						} else
						{
							int[] values = originalImage.getVectorPixelXYZTByte(x, y, z, t);
							for(int b=0;b<bDim;b++)
								listOfRegions[regionLabel].values[b]+=values[b];
							nbPixelsOfRegion[regionLabel]++;
						}
					}
					for(int i=0;i<nbRegions;i++)
						if(listOfRegions[i]!=null)
							for(int b=0;b<bDim;b++)
							{
								listOfRegions[i].values[b]=(int) Math.round(listOfRegions[i].values[b]/(double)nbPixelsOfRegion[i]);
							}
					nbPixelsOfRegion=null;
		//Add and compute edge
		for(int t=0;t<tDim;t++)
			for(int z=0;z<zDim;z++)
				for(int y=0;y<yDim;y++)
					for(int x=0;x<xDim;x++)
						for(Point4D neighbour: neighbourhood)
						{
							int locX = x + neighbour.x;
							int locY = y + neighbour.y;
							int locZ = z + neighbour.z;
							int locT = t + neighbour.t;
							if(!originalImage.isOutOfBoundsXYZT(locX,locY,locZ,locT))
							{
								Region region1 = listOfRegions[segmentationImage.getPixelXYZTInt(x,y,z,t)];
								Region region2 = listOfRegions[segmentationImage.getPixelXYZTInt(locX,locY,locZ,locT)];
								if(region1!=region2&&!region1.isConnectedTo(region2)&&alphaPred.check(region1.values, region2.values))
								{
									double distance = alphaPred.getDistance(region1.values, region2.values);
									region1.addConnection(region2, distance);
									region2.addConnection(region1, distance);
								}
							}				
						}
		currentNewLabel=0;
		//Regional Merging Algorithm Alpha Omega
		for(Region regionTmp : listOfRegions)
			if(regionTmp !=null &&regionTmp.newLabel==UNLABELLED)
			{
				alphaPred.resetCurrentAlpha();
				for(int i=0;i<allPredicates.length;i++)
				{
					allPredicates[i].resetData();
				}
				currentRegion=regionTmp;
				currentQFZ.clear();
				currentRegion.newLabel=currentNewLabel;
				currentQFZ.add(currentRegion);
				checkNeighboursAndAddToFIFO(currentRegion);
				boolean zQPValide=false;
				do{
					while(!fifoQ.isEmpty())
					{
						Region newRegion = fifoQ.pop();
						newRegion.newLabel=UNLABELLED;
						for(int i=0;i<allPredicates.length;i++)
						{
							allPredicates[i].updatePredicateDataForMerging(newRegion);
						}
						if(checkLocalPredicates())
						{
							currentQFZ.add(newRegion);
							newRegion.newLabel=currentNewLabel;
							checkNeighboursAndAddToFIFO(newRegion);
						}
						else
						{
							reinitQFZ();					
						}				
					}
					if(checkGlobalPredicates())
					{
						zQPValide=true;
					} else
					{
						reinitQFZ();
					}
				}while(!zQPValide);
				currentNewLabel++;
			}
		//Apply the merging
		if(unsafe)
		{
			for(int i=0;i<videoSize;i++)
			{
				segmentationImage.setPixelInt(i, listOfRegions[segmentationImage.getPixelInt(i)].newLabel);
			}
			mergedSegmentationImage=segmentationImage;
		}
		else
		{
			mergedSegmentationImage=segmentationImage.copyImage(false);
			for(int i=0;i<videoSize;i++)
			{				
				mergedSegmentationImage.setPixelInt(i, listOfRegions[segmentationImage.getPixelInt(i)].newLabel);
			}
		}
	}

	void checkNeighboursAndAddToFIFO(Region region)
	{
		for(Connection connection : region.connections)
		{
			if(connection.distance<=alphaPred.getCurrentAlpha())
			{
				if(connection.neighbour.newLabel!=INQUEUE)
				{
					if(connection.neighbour.newLabel==UNLABELLED)
					{
						connection.neighbour.newLabel=INQUEUE;
						fifoQ.push(connection.neighbour);
					}
					else if(connection.neighbour.newLabel!=currentNewLabel)
					{
						reinitQFZ();
						break;
					}
				}
			}
		}
	}

	private final boolean checkLocalPredicates()
	{
		for(int i=0;i<localPredicates.length;i++)
		{
			if(!localPredicates[i].check(alphaPred))
				return false;
		}		
		return true;
	}
	
	private final boolean checkGlobalPredicates()
	{
		for(int i=0;i<globalPredicates.length;i++)
		{
			if(!globalPredicates[i].check(alphaPred))
				return false;
		}		
		return true;
	}

	private void reinitQFZ() 
	{
		alphaPred.decreaseCurrentAlpha();
		for(Region region : currentQFZ)
		{
			region.newLabel=UNLABELLED;
		}
		currentQFZ.clear();
		currentQFZ.add(currentRegion);
		currentRegion.newLabel=currentNewLabel;
		for(int i=0;i<allPredicates.length;i++)
		{
			allPredicates[i].resetData();
		}
		for(Region region : fifoQ)
		{
			region.newLabel=UNLABELLED;
		}
		fifoQ.clear();
		checkNeighboursAndAddToFIFO(currentRegion);
	}


	public MultivariateLogicalPredicateConnectivityAppliedOnRegion()
	{
		super.inputs="originalImage,segmentationImage,alphaPred,predicates,neighbourhood";
		super.options="unsafe";
		super.outputs="mergedSegmentationImage";
	}

	public static IntegerImage exec(Image originalImage, IntegerImage segmentationImage, MultivariateAlphaLogicalPredicate alphaPred, ArrayList<MultivariateLogicalPredicate> predicates, Point4D[] neighbourhood )
	{
		return (IntegerImage) new MultivariateLogicalPredicateConnectivityAppliedOnRegion().process(originalImage,segmentationImage,alphaPred, predicates,neighbourhood);
	}

	public static IntegerImage exec(Image originalImage, IntegerImage segmentationImage, MultivariateAlphaLogicalPredicate alphaPred, ArrayList<MultivariateLogicalPredicate> predicates, Point4D[] neighbourhood , boolean unsafe)
	{
		return (IntegerImage) new MultivariateLogicalPredicateConnectivityAppliedOnRegion().process(originalImage,segmentationImage,alphaPred, predicates,neighbourhood,unsafe);
	}

	public class Region
	{
		protected int oldLabel;
		protected int[] values;
		protected int newLabel=UNLABELLED;
		protected ArrayList<Connection> connections;
		
		public Region(int oldLabel, int[] value)
		{
			this.oldLabel=oldLabel;
			this.values=value;
			connections= new ArrayList<Connection>();
		}
		
		public int[] getValues()
		{
			return values;
		}
		
		public void addConnection(Region neighbour, double distance)
		{
			connections.add(new Connection(neighbour,distance));
		}
		
		public final boolean isConnectedTo(Region region)
		{
			for(Connection connection: connections)
			{
				if(connection.neighbour==region)
				{
					return true;
				}
			}
			return false;
		}
	}
	
	public class Connection
	{
		protected Region neighbour;
		protected double distance;
		
		public Connection(Region neighbour, double distance)
		{
			this.neighbour=neighbour;
			this.distance=distance;
		}
	}
}
