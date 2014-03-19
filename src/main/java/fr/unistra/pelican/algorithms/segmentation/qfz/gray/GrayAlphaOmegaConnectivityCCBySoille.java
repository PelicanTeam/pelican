package fr.unistra.pelican.algorithms.segmentation.qfz.gray;

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
import fr.unistra.pelican.util.Point4D;
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
public class GrayAlphaOmegaConnectivityCCBySoille extends Algorithm {

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
	public Point4D[] connectivity;
	
	/**
	 * Label image of quasi-flat zones
	 */
	public IntegerImage lbl;
	
	/**
	 * Constructor
	 */
	public GrayAlphaOmegaConnectivityCCBySoille() 
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
		int zDim = inputImage.getZDim();
		int tDim = inputImage.getTDim();
		
		int mincc;
		int maxcc;
		int lblval;
		int rlcrt;
		int rlval;
		int rcrt=0;
		
		TreeMap<Integer,ArrayList<Point4D>> pq = new TreeMap<Integer,ArrayList<Point4D>>();
		Stack<Point4D> stack = new Stack<Point4D>();
		
		lbl = inputImage.newIntegerImage();
		ByteImage rl = inputImage.newByteImage();
		rl.fill(Byte.MAX_VALUE);

		
		//Start Soille's Algorithm
		lblval=1;
		//For each pixel
		for(int t=0;t<tDim;t++)
		{
			for(int z=0;z<zDim;z++)
			{
				for(int y=0;y<yDim;y++)
				{
					for(int x=0;x<xDim;x++)
					{
						// If pixel non-already labbelled
						if(lbl.getPixelXYZTInt(x, y, z, t)==0)
						{
							lbl.setPixelXYZTInt(x, y, z, t, lblval);
							int fp = inputImage.getPixelXYZTByte(x, y, z, t);
							mincc = fp;
							maxcc= fp;
							rlcrt = alpha;
							// For each neighbour of the pixel
							for(Point4D q:connectivity)
							{
								int qX = x+q.x;
								int qY = y+q.y;
								int qZ = z+q.z;
								int qT = t+q.t;
								// If q is in the image
								if(qX>=0 && qY>=0 && qZ>=0 && qT>=0 && qX<xDim && qY<yDim && qZ<zDim && qT<tDim)
								{
									rlval=Math.abs(fp-inputImage.getPixelXYZTByte(qX, qY, qZ, qT));
									if(lbl.getPixelXYZTInt(qX, qY, qZ, qT)>0)
									{
										if(rlcrt>=rlval)
										{
											rlcrt=rlval-1;
										}
										continue;
									}
									if(rlval<=rlcrt)
									{
										rl.setPixelXYZTByte(qX, qY, qZ, qT, rlval);
										if(pq.containsKey(rlval))
										{
											pq.get(rlval).add(new Point4D(qX,qY,qZ,qT));
										} else
										{
											ArrayList<Point4D> tmp = new ArrayList<Point4D>();
											tmp.add(new Point4D(qX,qY,qZ,qT));
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
								ArrayList<Point4D> tmp = pq.get(datumPrio);
								Point4D datumPoint = tmp.remove(0);
								if(tmp.isEmpty())
								{
									pq.pollFirstEntry();
								}
								if(lbl.getPixelXYZTInt(datumPoint.x, datumPoint.y, datumPoint.z, datumPoint.t)>0)
								{
									continue;
								}
								if(datumPrio>rcrt)
								{
									while(!stack.isEmpty())
									{
										Point4D stackPoint =stack.pop();
										lbl.setPixelXYZTInt(stackPoint.x, stackPoint.y, stackPoint.z, stackPoint.t, lblval);
									}
									rcrt = datumPrio;
									if(lbl.getPixelXYZTInt(datumPoint.x, datumPoint.y, datumPoint.z, datumPoint.t)>0)
									{
										continue;
									}
								}
								stack.add(datumPoint);
								int datumVal = inputImage.getPixelXYZTByte(datumPoint.x, datumPoint.y, datumPoint.z, datumPoint.t);
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
									for(Point4D pp:stack)
									{
										rl.setPixelXYZTByte(pp.x, pp.y, pp.z, pp.t, 255);
									}
									stack.clear();
									Collection<ArrayList<Point4D>> pointsLists = pq.values();
									for(ArrayList<Point4D> pl:pointsLists)
									{
										for(Point4D p:pl)
										{
											rl.setPixelXYZTByte(p.x, p.y, p.z, p.t, 255);
										}
									}
									pq.clear();;
									break;
								}
								for(Point4D q:connectivity)
								{
									int qX = datumPoint.x+q.x;
									int qY = datumPoint.y+q.y;
									int qZ = datumPoint.z+q.z;
									int qT = datumPoint.t+q.t;
									// If q is in the image
									if(qX>=0 && qY>=0 && qZ>=0 && qT>=0 && qX<xDim && qY<yDim && qZ<zDim && qT<tDim)
									{
										rlval = Math.abs(datumVal-inputImage.getPixelXYZTByte(qX, qY, qZ, qT));
										int lblQ = lbl.getPixelXYZTInt(qX, qY, qZ, qT);
										if(lblQ>0 && lblQ!=lblval && rlcrt>=rlval)
										{
											rlcrt=rlval-1;
											if(rcrt>rlcrt)
											{
												for(Point4D pp:stack)
												{
													rl.setPixelXYZTByte(pp.x, pp.y, pp.z, pp.t, 255);
												}
												stack.clear();
												Collection<ArrayList<Point4D>> pointsLists = pq.values();
												for(ArrayList<Point4D> pl:pointsLists)
												{
													for(Point4D p:pl)
													{
														rl.setPixelXYZTByte(p.x, p.y, p.z, p.t, 255);
													}
												}
												pq.clear();
												break;
											}
											continue;
										}
										if(rlval>rlcrt || rlval>=rl.getPixelXYZTByte(qX, qY, qZ, qT))
										{
											continue;
										} else if (rlval<rl.getPixelXYZTByte(qX, qY, qZ, qT))
										{
											rl.setPixelXYZTByte(qX, qY, qZ, qT, rlval);
											if(pq.containsKey(rlval))
											{
												pq.get(rlval).add(new Point4D(qX,qY,qZ,qT));
											} else
											{
												ArrayList<Point4D> tmp2 = new ArrayList<Point4D>();
												tmp2.add(new Point4D(qX,qY,qZ,qT));
												pq.put(rlval, tmp2);
											}
										}										
									}
								}								
							}
							while(!stack.isEmpty())
							{
								Point4D stackPoint =stack.pop();
								lbl.setPixelXYZTInt(stackPoint.x, stackPoint.y, stackPoint.z, stackPoint.t, lblval);
							}
							lblval++;
						}
					}
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
	public static IntegerImage exec(Image inputImage, int alpha, int omega, Point4D[] connectivity) 
	{
		return (IntegerImage)new GrayAlphaOmegaConnectivityCCBySoille().process(inputImage,alpha,omega,connectivity);
	}
}
