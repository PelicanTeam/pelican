package fr.unistra.pelican.algorithms.segmentation.qfz.gray;

import java.util.ArrayList;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.conversion.AverageChannels;
import fr.unistra.pelican.algorithms.conversion.RGBToGray;
import fr.unistra.pelican.util.FIFOQueue;
import fr.unistra.pelican.util.Point4D;
import fr.unistra.pelican.util.qfz.GrayLogicalPredicate;

/**
 * This class  computes the Quasi-Flat Zones by logical predicates connectivity
 * 
 * @author Jonathan Weber
 * 
 */
public class GrayLogicalPredicateConnectivity extends Algorithm {

	/**
	 * Image to be processed
	 */
	public ByteImage inputImage;
	
	/**
	 * Neighbourhoud under consideration
	 */
	public Point4D[] neighbourhood;
	
	/**
	 * ArrayList of logical predicates
	 */
	public ArrayList<GrayLogicalPredicate> predicates;
	
	/**
	 * alpha value for alpha-connectivity
	 */
	public int alpha=-1;
	
	/**
	 * Quasi-Flat Zones obtained by Logical Predicates Connectivity
	 */
	public IntegerImage QFZ;
	
	private static final int UNLABELLED=-1;
	
	private int XDim;
	private int YDim;
	private int ZDim;
	private int TDim;
	private int currentX;
	private int currentY;
	private int currentZ;
	private int currentT;
	
	private int alphac;
	private int currentLabel=0;
	private int alphaMax;
	private int alphaMin;
	
	
	private FIFOQueue<Point4D> fifoQ;
	
	private ArrayList<Point4D> currentZQP;	
	
	private ArrayList<GrayLogicalPredicate> localPredicates;
	private ArrayList<GrayLogicalPredicate> globalPredicates;
	
	private boolean finalAlpha=false;
	
	public GrayLogicalPredicateConnectivity()
	{
		super.inputs = "inputImage,predicates,neighbourhood";
		super.options ="alpha";
		super.outputs="QFZ";
	}
	
	@Override
	public void launch() throws AlgorithmException 
	{
		//Convert inputImage in grey image if not
		if(inputImage.getBDim()==3)
		{
			inputImage = (ByteImage)RGBToGray.exec(inputImage);
		} else if(inputImage.getBDim()!=1)
		{
			inputImage = (ByteImage)AverageChannels.exec(inputImage);
		}
		XDim = inputImage.getXDim();
		YDim = inputImage.getYDim();
		ZDim = inputImage.getZDim();
		TDim = inputImage.getTDim();
		if(alpha<0)
		{
			alpha=255;
		}
		localPredicates= new ArrayList<GrayLogicalPredicate>();
		globalPredicates= new ArrayList<GrayLogicalPredicate>();
		for(int i=0;i<predicates.size();i++)
		{
			if(predicates.get(i).getType()==GrayLogicalPredicate.LOCALPREDICATE)
			{
				localPredicates.add(predicates.get(i));
			}
			else
			{
				globalPredicates.add(predicates.get(i));
			}
		}
		fifoQ = new FIFOQueue<Point4D>();
		QFZ = inputImage.newIntegerImage();
		QFZ.fill(UNLABELLED);
		for(currentT=0;currentT<TDim;currentT++)
			for(currentZ=0;currentZ<ZDim;currentZ++)
				for(currentY=0;currentY<YDim;currentY++)
					for(currentX=0;currentX<XDim;currentX++)
					{
						if(QFZ.getPixelXYZTInt(currentX,currentY,currentZ,currentT)==UNLABELLED)
						{
							currentLabel++;
							resetAlpha();
							currentZQP = new ArrayList<Point4D>();
							for(int i=0;i<predicates.size();i++)
								predicates.get(i).resetData();
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
									predicateViolationUpdate();
									resetCurrentZQP();
								}
							}

							if(checkGlobalPredicates())
							{
								predicateValidationUpdate();
								if(finalAlpha)
									zQPValide=true;
								else
									resetCurrentZQP();
							} else
							{
								predicateViolationUpdate();
								resetCurrentZQP();
							}
						}while(!zQPValide);
					}
	}
	
	private final void addUnlabelledAlphaNeighboursToFIFO(int x, int y, int z, int t)
	{
		int pixelValue = inputImage.getPixelXYZTByte(x, y, z, t);
		for(int i=0;i<neighbourhood.length;i++)
		{
			int locX = x + neighbourhood[i].x;
			int locY = y + neighbourhood[i].y;
			int locZ = z + neighbourhood[i].z;
			int locT = t + neighbourhood[i].t;
			Point4D currentNeighbour = new Point4D(locX,locY,locZ,locT);
			if(!QFZ.isOutOfBoundsXYZT(locX,locY,locZ,locT))
			{
				if(Math.abs(pixelValue-inputImage.getPixelXYZTByte(locX, locY, locZ, locT))<=alphac
						&&QFZ.getPixelXYZTInt(locX, locY, locZ, locT)!=currentLabel
						&&!fifoQ.contains(currentNeighbour))
				{
					if(QFZ.getPixelXYZTInt(locX, locY, locZ, locT)==UNLABELLED)
					{
						fifoQ.add(new Point4D(locX,locY,locZ,locT));
					}
					else
					{
						predicateViolationUpdate();
						resetCurrentZQP();
						break;
					}
				}				
			} 
		}
	}
	
	private void predicateViolationUpdate()
	{
		alphaMax=alphac;
		alphac=(alphaMax+alphaMin)/2;
	}
	
	private void predicateValidationUpdate()
	{
		if(alphac+1==alphaMax)
		{
			finalAlpha=true;
		}
		else
		{
			alphaMin=alphac;
			alphac=(alphaMax+alphaMin)/2;
		}
	}
	
	private void resetAlpha()
	{
		alphac=alpha;
		finalAlpha=false;
		alphaMax=alpha+1;
		alphaMin=0;
	}
	
	
	private final void addPixelToCurrentZQP(int tX,int tY,int tZ,int tT)
	{
		QFZ.setPixelXYZTInt(tX, tY, tZ, tT, currentLabel);
		currentZQP.add(new Point4D(tX,tY,tZ,tT));
		for(int i=0;i<predicates.size();i++)
		{
			predicates.get(i).updatePredicateData(inputImage,QFZ,alpha,alphac,tX, tY, tZ, tT,currentLabel, neighbourhood);
		}
		/*System.out.print("c ZQP : ");
		for(int i=0;i<currentZQP.size();i++)
		{
			Point4D p = currentZQP.get(i);
			System.out.print("("+p.x+","+p.y+") ");
		}
		System.out.println("=> "+checkLocalPredicates());*/
	}
	
	private final boolean checkLocalPredicates()
	{
		for(int i=0;i<localPredicates.size();i++)
		{
			if(!localPredicates.get(i).check(alphac))
				return false;
		}		
		return true;
	}
	
	private final boolean checkGlobalPredicates()
	{
		for(int i=0;i<globalPredicates.size();i++)
		{
			if(!globalPredicates.get(i).check(alphac))
				return false;
		}		
		return true;
	}
	
	private final void resetCurrentZQP()
	{		
		while(currentZQP.size()!=0)
		{
			Point4D tmp = currentZQP.remove(0);
			QFZ.setPixelXYZTInt(tmp.x,tmp.y,tmp.z,tmp.t,UNLABELLED);
		}
		for(int i=0;i<predicates.size();i++)
		{
			predicates.get(i).resetData();
		}
		addPixelToCurrentZQP(currentX,currentY,currentZ,currentT);
		fifoQ = new FIFOQueue<Point4D>();
		addUnlabelledAlphaNeighboursToFIFO(currentX,currentY,currentZ,currentT);
	}
	
	public final static IntegerImage exec (Image inputImage, ArrayList<GrayLogicalPredicate> predicates, Point4D[] neighbourhood )
	{
		return (IntegerImage) new GrayLogicalPredicateConnectivity().process(inputImage,predicates,neighbourhood);
	}
	
	public final static IntegerImage exec (Image inputImage, ArrayList<GrayLogicalPredicate> predicates, Point4D[] neighbourhood, int alpha )
	{
		return (IntegerImage) new GrayLogicalPredicateConnectivity().process(inputImage,predicates,neighbourhood, alpha);
	}

}
