package fr.unistra.pelican.algorithms.segmentation.qfz.gray;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;
import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.conversion.AverageChannels;
import fr.unistra.pelican.algorithms.conversion.RGBToGray;
import fr.unistra.pelican.util.Stack;

/**
 * Gray level connected component analysis with Soille's connectivity definition
 * 
 * - alpha is the local range limit: neighbor pixel p and q are alpha-Connected <=> |f(p)-f(q)|<=alpha
 * - pixels p q are alpha connected if their exists a set of pixels p_i forming a path from p to q and each p_i p_i+1 are alpha connected
 * - omega is the global rang: maximum range beetwen two pixel of a alpha,omega connected component is omega
 * 
 * Formally: Connected component of pixel p is the largest alpha'-CC of p with alpha'<=alpha and range of alpha'-CC <= omega
 * 
 * Work in byte precision => give alpha and omega as byte values 
 * 
 * Deal with X-Y-Z-T dim
 *
 * P. Soille. Constrained connectivity for hierarchical image partitioning and simplification.
 * Pattern Analysis and Machine Intelligence, 30(7) :1132-1145, july 2008.
 * http://dx.doi.org/10.1109/TPAMI.2007.70817
 * 
 * @author Jonathan Weber
 */
public class GrayAlphaOmegaConnectedComponentsBySoille2D extends Algorithm {

	/**
	 * Input image
	 */
	public Image inputImage;
	
	/**
	 * Local range alpha
	 */
	public int alpha;
	
	/**
	 * Global range alpha
	 */
	public int omega;
	
	/**
	 * Connectivity under consideration
	 */
	public Point[] connectivity;
	
	/**
	 * Label image of quasi-flat zones
	 */
	public IntegerImage lbl;
	
	/**
	 * Constructor
	 */
	public GrayAlphaOmegaConnectedComponentsBySoille2D() 
	{
		super();
		super.inputs = "inputImage,alpha,omega,connectivity";
		super.outputs = "lbl";
	}
	
	@Override
	public void launch() throws AlgorithmException 
	{
		//Convert inputImage in grey image if not
		if(inputImage.getBDim()==3)
		{
			inputImage = RGBToGray.exec(inputImage);
		} else if(inputImage.getBDim()!=1)
		{
			inputImage = AverageChannels.exec(inputImage);
		}
		
		// Initialize data
		int xDim = inputImage.getXDim();
		int yDim = inputImage.getYDim();
		
		int mincc;
		int maxcc;
		int lblval;
		int rlcrt;
		int rlval;
		int rcrt=0;
		
		TreeMap<Integer,ArrayList<Point>> pq = new TreeMap<Integer,ArrayList<Point>>();
		Stack<Point> stack = new Stack<Point>();
		
		lbl = inputImage.newIntegerImage();
		ByteImage rl = inputImage.newByteImage();
		rl.fill(Byte.MAX_VALUE);
		
		//Start Soille's Algorithm
		lblval=1;
		//For each pixel
				for(int y=0;y<yDim;y++)
				{
					for(int x=0;x<xDim;x++)
					{
						// If pixel non-already labeled
						if(lbl.getPixelXYInt(x, y)==0)
						{
							lbl.setPixelXYInt(x, y, lblval);
							int fp = inputImage.getPixelXYByte(x, y);
							mincc = fp;
							maxcc= fp;
							rlcrt = alpha;
							// For each neighbour of the pixel
							for(Point q:connectivity)
							{
								int qX = x+q.x;
								int qY = y+q.y;
								// If q is in the image
								if(qX>=0 && qY>=0 && qX<xDim && qY<yDim)
								{
									rlval=Math.abs(fp-inputImage.getPixelXYByte(qX, qY));
									if(lbl.getPixelXYInt(qX, qY)>0)
									{
										if(rlcrt>=rlval)
										{
											rlcrt=rlval-1;
										}
										continue;
									}
									if(rlval<=rlcrt)
									{
										rl.setPixelXYByte(qX, qY, rlval);
										if(pq.containsKey(rlval))
										{
											pq.get(rlval).add(new Point(qX,qY));
										} else
										{
											ArrayList<Point> tmp = new ArrayList<Point>();
											tmp.add(new Point(qX,qY));
											pq.put(rlval, tmp);
										}
									}
								}
							}
							rcrt=0;
							if(!pq.isEmpty())
							{
								rcrt = pq.firstKey();
							}
							while(!pq.isEmpty())
							{
								int datumPrio = pq.firstKey();
								ArrayList<Point> tmp = pq.get(datumPrio);
								Point datumPoint = tmp.remove(0);
								if(tmp.isEmpty())
								{
									pq.pollFirstEntry();
								}
								if(lbl.getPixelXYInt(datumPoint.x, datumPoint.y)>0)
								{
									continue;
								}
								if(datumPrio>rcrt)
								{
									while(!stack.isEmpty())
									{
										Point stackPoint =stack.pop();
										lbl.setPixelXYInt(stackPoint.x, stackPoint.y, lblval);
									}
									rcrt = datumPrio;
									if(lbl.getPixelXYInt(datumPoint.x, datumPoint.y)>0)
									{
										continue;
									}
								}
								stack.add(datumPoint);
								int datumVal = inputImage.getPixelXYByte(datumPoint.x, datumPoint.y);
								if(datumVal<mincc)
								{
									mincc=datumVal;
								}
								if(datumVal>maxcc)
								{
									maxcc=datumVal;
								}
								if(omega<(maxcc-mincc)||(rcrt>rlcrt))
								{
									for(Point pp:stack)
									{
										rl.setPixelXYByte(pp.x, pp.y, 255);
									}
									stack.clear();
									Collection<ArrayList<Point>> pointsLists = pq.values();
									for(ArrayList<Point> pl:pointsLists)
									{
										for(Point p:pl)
										{
											rl.setPixelXYByte(p.x, p.y, 255);
										}
									}
									pq.clear();
									//System.out.println("PQ clear");
									break;
								}
								for(Point q:connectivity)
								{
									int qX = datumPoint.x+q.x;
									int qY = datumPoint.y+q.y;
									// If q is in the image
									if(qX>=0 && qY>=0 && qX<xDim && qY<yDim)
									{
										rlval = Math.abs(datumVal-inputImage.getPixelXYByte(qX, qY));
										int lblQ = lbl.getPixelXYInt(qX, qY);
										if(lblQ>0 && lblQ!=lblval && rlcrt>=rlval)
										{
											rlcrt=rlval-1;
											if(rcrt>rlcrt)
											{
												for(Point pp:stack)
												{
													rl.setPixelXYByte(pp.x, pp.y, 255);
												}
												stack.clear();
												Collection<ArrayList<Point>> pointsLists = pq.values();
												for(ArrayList<Point> pl:pointsLists)
												{
													for(Point p:pl)
													{
														rl.setPixelXYByte(p.x, p.y, 255);
													}
												}
												pq.clear();
												//System.out.println("PQ clear");
												break;
											}
											continue;
										}
										if(rlval>rlcrt || rlval>=rl.getPixelXYByte(qX, qY))
										{
											continue;
										} else if (rlval<rl.getPixelXYByte(qX, qY))
										{
											rl.setPixelXYByte(qX, qY, rlval);
											if(pq.containsKey(rlval))
											{
												pq.get(rlval).add(new Point(qX,qY));
											} else
											{
												ArrayList<Point> tmp2 = new ArrayList<Point>();
												tmp2.add(new Point(qX,qY));
												pq.put(rlval, tmp2);
											}
										}										
									}
								}								
							}
							while(!stack.isEmpty())
							{
								Point stackPoint=stack.pop();
								lbl.setPixelXYInt(stackPoint.x, stackPoint.y, lblval);
							}
							//Viewer2D.exec(LabelsToRandomColors.exec(lbl),"Label : "+lblval);
							lblval++;
						}
					}
				}
			
		
	}
	
	/**
	 * Compute (alpha,omega)-connected components by Soille's Algorithm	 * 
	 * 
	 * @param inputImage input image
	 * @param alpha  local range
	 * @param omega  global range
	 * @param connectivity desired connectivity
	 * @return Labelled image of CCs
	 */
	public static IntegerImage exec(Image inputImage, int alpha, int omega, Point[] connectivity) 
	{
		return (IntegerImage)new GrayAlphaOmegaConnectedComponentsBySoille2D().process(inputImage,alpha,omega,connectivity);
	}
}

