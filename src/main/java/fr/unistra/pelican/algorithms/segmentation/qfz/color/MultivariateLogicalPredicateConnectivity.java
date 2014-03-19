package fr.unistra.pelican.algorithms.segmentation.qfz.color;

import java.util.ArrayList;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.util.FIFOQueue;
import fr.unistra.pelican.util.Point4D;
import fr.unistra.pelican.util.qfz.MultivariateAlphaLogicalPredicate;
import fr.unistra.pelican.util.qfz.MultivariateLogicalPredicate;

/**
 * This class  computes the Quasi-Flat Zones by logical predicates connectivity on ByteImage
 * 
 * Takes mask into account
 * 
 * @author Jonathan Weber
 * 
 */
public class MultivariateLogicalPredicateConnectivity extends Algorithm {

	/**
	 * Image to be processed
	 */
	public ByteImage inputImage;
	
	/**
	 * Neighbourhoud under consideration
	 */
	public Point4D[] neighbourhood;
	
	/**
	 * Predicate for alpha
	 */
	public MultivariateAlphaLogicalPredicate alphaPred;
	
	/**
	 * ArrayList of logical predicates
	 */
	public ArrayList<MultivariateLogicalPredicate> predicates;
	
	/**
	 * Quasi-Flat Zones obtained by Logical Predicates Connectivity
	 */
	public IntegerImage QFZ;
	
	private static final int UNLABELLED=-1;
	private static final int INQUEUE=-2;
	
	private int XDim;
	private int YDim;
	private int ZDim;
	private int TDim;
	private int currentX;
	private int currentY;
	private int currentZ;
	private int currentT;
	
	private int currentLabel=0;
	
	private FIFOQueue<Point4D> fifoQ;
	
	private ArrayList<Point4D> currentZQP;	
	
	private MultivariateLogicalPredicate[] localPredicates;
	private MultivariateLogicalPredicate[] globalPredicates;
	private MultivariateLogicalPredicate[] allPredicates;
	
	public MultivariateLogicalPredicateConnectivity()
	{
		super.inputs = "inputImage,alphaPred,predicates,neighbourhood";
		super.outputs="QFZ";
	}
	
	@Override
	public void launch() throws AlgorithmException 
	{
		XDim = inputImage.getXDim();
		YDim = inputImage.getYDim();
		ZDim = inputImage.getZDim();
		TDim = inputImage.getTDim();
		int nbLoc=0,nbGlo=0;
		allPredicates=new MultivariateLogicalPredicate[predicates.size()];
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
		fifoQ = new FIFOQueue<Point4D>();
		QFZ = inputImage.newIntegerImage(XDim,YDim,ZDim,TDim,1);
		QFZ.fill(UNLABELLED);
		if(inputImage.getMask()==null||inputImage.getMask().isEmpty())
		{
			for(currentT=0;currentT<TDim;currentT++)
				for(currentZ=0;currentZ<ZDim;currentZ++)
					for(currentY=0;currentY<YDim;currentY++)
						for(currentX=0;currentX<XDim;currentX++)
						{
							computeQFZ();
						}
		}
		else
		{
			for(currentT=0;currentT<TDim;currentT++)
				for(currentZ=0;currentZ<ZDim;currentZ++)
					for(currentY=0;currentY<YDim;currentY++)
						for(currentX=0;currentX<XDim;currentX++)
						{
							if(inputImage.isPresentXYZT(currentX,currentY,currentZ,currentT))
									computeQFZ();
						}
		}
	}
	
	private final void computeQFZ()
	{
		if(QFZ.getPixelXYZTInt(currentX,currentY,currentZ,currentT)==UNLABELLED)
		{
			currentLabel++;
			alphaPred.resetCurrentAlpha();
			currentZQP = new ArrayList<Point4D>();
			for(int i=0;i<allPredicates.length;i++)
				allPredicates[i].resetData();
			addPixelToCurrentZQP(currentX,currentY,currentZ,currentT);
			addUnlabelledAlphaNeighboursToFIFO(currentX,currentY,currentZ,currentT);
		}
		boolean zQPValide=false;
		do{
			while(fifoQ.size()!=0)
			{
				Point4D currentPixel = fifoQ.pop();
				addPixelToCurrentZQP(currentPixel.x, currentPixel.y, currentPixel.z, currentPixel.t);
				if(checkLocalPredicates())
				{
					addUnlabelledAlphaNeighboursToFIFO(currentPixel.x,currentPixel.y,currentPixel.z,currentPixel.t);
				}
				else
				{
					alphaPred.predicateViolationUpdate();
					resetCurrentZQP();
				}
			}

			if(checkGlobalPredicates())
			{
				alphaPred.predicateValidationUpdate();
				if(alphaPred.isFinalAlpha())
					zQPValide=true;
				else
					resetCurrentZQP();
			} else
			{
				alphaPred.predicateViolationUpdate();
				resetCurrentZQP();
			}
		}while(!zQPValide);
	}
	
	private final void addUnlabelledAlphaNeighboursToFIFO(int x, int y, int z, int t)
	{
		int[] pixelValues = inputImage.getVectorPixelXYZTByte(x, y, z, t);
		if(inputImage.getMask()==null||inputImage.getMask().isEmpty())
		{
			for(int i=0;i<neighbourhood.length;i++)
			{
				int locX = x + neighbourhood[i].x;
				int locY = y + neighbourhood[i].y;
				int locZ = z + neighbourhood[i].z;
				int locT = t + neighbourhood[i].t;
				if(!QFZ.isOutOfBoundsXYZT(locX,locY,locZ,locT))
				{
					if(QFZ.getPixelXYZTInt(locX, locY, locZ, locT)!=currentLabel
							&&QFZ.getPixelXYZTInt(locX, locY, locZ, locT)!=INQUEUE && alphaPred.check(pixelValues, inputImage.getVectorPixelXYZTByte(locX, locY, locZ, locT)))
					{
						if(QFZ.getPixelXYZTInt(locX, locY, locZ, locT)==UNLABELLED)
						{
							fifoQ.add(new Point4D(locX,locY,locZ,locT));
							QFZ.setPixelXYZTInt(locX, locY, locZ, locT,INQUEUE);
						}
						else
						{
							alphaPred.predicateViolationUpdate();
							resetCurrentZQP();
							break;
						}
					}				
				} 
			}
		}
		else
		{
			for(int i=0;i<neighbourhood.length;i++)
			{
				int locX = x + neighbourhood[i].x;
				int locY = y + neighbourhood[i].y;
				int locZ = z + neighbourhood[i].z;
				int locT = t + neighbourhood[i].t;
				if(!QFZ.isOutOfBoundsXYZT(locX,locY,locZ,locT)&&inputImage.isPresentXYZT(locX,locY,locZ,locT))
				{
					if(QFZ.getPixelXYZTInt(locX, locY, locZ, locT)!=currentLabel
							&&QFZ.getPixelXYZTInt(locX, locY, locZ, locT)!=INQUEUE && alphaPred.check(pixelValues, inputImage.getVectorPixelXYZTByte(locX, locY, locZ, locT)))
					{
						if(QFZ.getPixelXYZTInt(locX, locY, locZ, locT)==UNLABELLED)
						{
							fifoQ.add(new Point4D(locX,locY,locZ,locT));
							QFZ.setPixelXYZTInt(locX, locY, locZ, locT,INQUEUE);
						}
						else
						{
							alphaPred.predicateViolationUpdate();
							resetCurrentZQP();
							break;
						}
					}				
				} 
			}
		}
	}
	
	private final void addPixelToCurrentZQP(int tX,int tY,int tZ,int tT)
	{
		QFZ.setPixelXYZTInt(tX, tY, tZ, tT, currentLabel);
		currentZQP.add(new Point4D(tX,tY,tZ,tT));
		for(int i=0;i<allPredicates.length;i++)
		{
			allPredicates[i].updatePredicateData(inputImage,QFZ,alphaPred,tX, tY, tZ, tT,currentLabel, neighbourhood);
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
	
	private final void resetCurrentZQP()
	{		
		Point4D tmp;
		int tabLength = currentZQP.size();
		Point4D[] tab = new Point4D[tabLength];
		tab = currentZQP.toArray(tab);
		currentZQP.clear();
		for(int i=0;i<tabLength;i++)
		{
			tmp = tab[i];
			QFZ.setPixelXYTInt(tmp.x,tmp.y,tmp.t,UNLABELLED);
		}
		while(!fifoQ.isEmpty())
		{
			tmp = fifoQ.pop();
			QFZ.setPixelXYZTInt(tmp.x,tmp.y,tmp.z,tmp.t,UNLABELLED);
		}
		for(int i=0;i<allPredicates.length;i++)
		{
			allPredicates[i].resetData();
		}
		addPixelToCurrentZQP(currentX,currentY,currentZ,currentT);
		addUnlabelledAlphaNeighboursToFIFO(currentX,currentY,currentZ,currentT);
	}
	
	public final static IntegerImage exec (Image inputImage, MultivariateAlphaLogicalPredicate alphaPred, ArrayList<MultivariateLogicalPredicate> predicates, Point4D[] neighbourhood )
	{
		return (IntegerImage) new MultivariateLogicalPredicateConnectivity().process(inputImage,alphaPred, predicates,neighbourhood);
	}

}

