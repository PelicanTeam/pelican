/**
 * 
 */
package fr.unistra.pelican.algorithms.segmentation.qfz.gray;

import java.util.Stack;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToColorByMeanValue;
import fr.unistra.pelican.algorithms.visualisation.MViewer;
import fr.unistra.pelican.gui.MultiViews.MultiView;
import fr.unistra.pelican.util.Point4D;
import fr.unistra.pelican.util.PriorityQueue;
import fr.unistra.pelican.util.Tools;
import fr.unistra.pelican.util.PriorityQueue.PrioritizedElement;
import fr.unistra.pelican.util.neighbourhood.Neighbourhood4D;

/**
 * Gray level connected component analysis with Soille's connectivity definition
 * 
 * - alpha is the local range limit: neighbor pixel p and q are alpha-Connected <=> |f(p)-f(q)|<=alpha
 * - pixels p q are alpha connected if their exists a set of pixels p_i forming a path from p to q and each p_i p_i+1 are alpha connected
 * - omega is the global rang: maximum range beetwen two pixel of a alpha,omega connected component is omega
 * 
 * 
 * Formally: Connected component of pixel p is the largest alpha'-CC of p with alpha'<=alpha and range of alpha'-CC <= omega
 * 
 * 
 * 
 * Work in double precision => give alpha and omega as double values !!!
 * 
 * Deal with X-Y-Z-T dim
 * 
 * @Article{         soille:constrained,
 *  author        = {Soille, P.},
 *  title         = {Constrained connectivity for hierarchical image
 *                  partitioning and simplification},
 *  journal       = pami,
 *  year          = {2008},
 *  doi           = {http://dx.doi.org/10.1109/TPAMI.2007.70817},
 *  volume        = {30},
 *  number        = {7},
 *  pages         = {1132-1145},
 *  month         = jul
 * }
 * 
 * @author Benjamin Perret, Jonathan Weber(multidimensionnality)
 *
 */
public class GrayCCSoille extends Algorithm {

	/**
	 * USE 4-neighborhood
	 */
	public static final int FOUR_NEIGHBORHOOD=1;
	
	/**
	 * USE 8-neighborhood
	 */
	public static final int EIGHT_NEIGHBORHOOD=2;
	
	/**
	 * USE 6-temoralneighborhood
	 */
	public static final int SIX_TEMPORAL_NEIGHBORHOOD=3;
	
	/**
	 * USE 10-temoralneighborhood
	 */
	public static final int TEN_TEMPORAL_NEIGHBORHOOD=4;
	
	/**
	 * Points in v4
	 */
	private static Point4D [] v4= Neighbourhood4D.get4Neighboorhood();
	
	/**
	 * Points in v8
	 */
	private static Point4D [] v8= Neighbourhood4D.get8Neighboorhood();
	
	/**
	 * Points in v6t
	 */
	private static Point4D [] v6t= Neighbourhood4D.get6TemporalNeighboorhood();
	
	/**
	 * Points in v10t
	 */
	private static Point4D [] v10t= Neighbourhood4D.get10TemporalNeighboorhood();
	
	/**
	 * Input image
	 */
	public Image inputImage;
	
	/**
	 * label image
	 */
	public IntegerImage label;
	
	/**
	 * local range image
	 */
	private DoubleImage localRange;
	
	/**
	 * Priority queue for growing algo
	 */
	private PriorityQueue<Point4D,Double> pq = new PriorityQueue<Point4D, Double>();
	
	/**
	 * Stack for labeling
	 */
	private Stack<Point4D> st = new Stack<Point4D>();
	
	/**
	 * alpha parameter: local range
	 */
	public double alpha;
	
	/**
	 * omega parameter: global range
	 */
	public double omega;
	
	/**
	 * Which neighborhood to use
	 */
	public int neighborhood=FOUR_NEIGHBORHOOD;
	
	/**
	 * Construct
	 */
	public GrayCCSoille() {
		super();
		super.inputs="inputImage,alpha,omega";
		super.options="neighborhood";
		super.outputs="label";
	}

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	@Override
	public void launch() throws AlgorithmException {
		int xdim=inputImage.xdim;
		int ydim=inputImage.ydim;
		int zdim=inputImage.zdim;
		int tdim=inputImage.tdim;
		
		label = new IntegerImage(xdim,ydim,zdim,tdim,1);
		localRange= new DoubleImage(xdim,ydim,zdim,tdim,1);
		localRange.fill(Double.POSITIVE_INFINITY);
		Point4D [] v;
		switch (neighborhood)
		{
		case FOUR_NEIGHBORHOOD:
			v = v4;
			break;
		case EIGHT_NEIGHBORHOOD:
			v = v8;
			break;
		case SIX_TEMPORAL_NEIGHBORHOOD:
			v = v6t;
			break;
		case TEN_TEMPORAL_NEIGHBORHOOD:
			v = v10t;
			break;
		default:
			v = v4;
			System.err.println("GrayCC: unknown constant for neighborhood: using  default 4-neighborhood!");
		}
		 
		
		int lblval=1;
		// for all points
		for(int t=0;t<tdim;t++)
		{
			for(int z=0;z<zdim;z++)
			{
				for(int y=0;y<ydim;y++)
				{
					for(int x=0;x<xdim;x++)
					{
						if(label.getPixelXYZTInt(x, y, z, t)==0) // if label is not assigned
						{
							label.setPixelXYZTInt(x, y, z, t,lblval); // assign current label
							double mincc=inputImage.getPixelXYZTDouble(x, y, z, t); // global range lower bound
							double maxcc=mincc; // global range upper bound
							double val=mincc; // current pixel value
							double rlcrt=alpha; // current local range
							for(Point4D p:v) // for all neighbors  
							{
								int xx = x + p.x;
								int yy = y + p.y;
								int zz = z + p.z;
								int tt = t + p.t;
								if(xx<0 || xx>=xdim || yy<0 || yy>=ydim || zz<0 || zz>=zdim || tt<0 || tt>=tdim) // check image bounds
									continue; 
								//System.out.println("hum");
								double val2 = inputImage.getPixelXYZTDouble(xx, yy, zz, tt); // neighbor value
								double rlval=Math.abs(val-val2); // local range value
								if(label.getPixelXYZTInt(xx, yy, zz, tt)>0) // if label already exist
								{
								
								//System.out.println("strange part " ); this must only be adapted for integer valued images
									//	if(rlcrt>=rlval) // if current local range > local range value
									//		{
									//		
									//	rlcrt=rlval-1.0; // if current local range =  local range value - 1.0; ??????
									//		}
									continue; // next neighbor
								}
								if(Tools.relativeDoubleCompare(rlval, rlcrt)<=0) // if local range <= current local range
								{
									localRange.setPixelXYZTDouble(xx, yy, zz, tt, rlval); // store neighbor local range
									pq.add(new Point4D(xx,yy,zz,tt), rlval); // add neighbor to pq at priority local range
									//System.out.println("add");
								}
							}
						
							double rcrt=0.0;//range 
							if(!pq.isEmpty())
								rcrt = pq.peek().getPriority(); // set range to minimum of local range with previous neighbors
						
							while (!pq.isEmpty()) //for all points with range smaller to limit
							{
								PrioritizedElement<Point4D, Double> datum = pq.pop();
								Point4D p=datum.getElement();
								int xx = p.x;
								int yy = p.y;
								int zz = p.z;
								int tt = p.t;
								//if(xx<0 || xx>=xdim || yy<0 || yy>=ydim || zz<0 || zz>=zdim || tt<0 || tt>=tdim)
								//	continue;
								double val2 = inputImage.getPixelXYZTDouble(xx, yy, zz, tt);
								if (label.getPixelXYZTInt(xx, yy, zz, tt)>0) //job is done for this pixel
									continue;
								if (Tools.relativeDoubleCompare(datum.getPriority(), rcrt)==1 )// if local range > range (increasing of range-cc is done)
								{
									while(!st.isEmpty()) // set all points on stack to current label
									{
										Point4D pp=st.pop();
										label.setPixelXYZTInt(pp.x, pp.y, pp.z, pp.t, lblval);
									}
									rcrt=datum.getPriority(); // set new range to local range
									if (label.getPixelXYZTInt(xx, yy, zz, tt)>0) // perhaps job is finished now 
										continue;
								}
							
								st.push(new Point4D(xx,yy,zz,tt)); // prepare pixel for labeling
							
								double vt=inputImage.getPixelXYZTDouble(xx, yy, zz, tt); //update lower and upper range bound 
							
							
								if(vt<mincc)
									mincc=vt;
								if(vt>maxcc)
									maxcc=vt;
							
								if (Tools.relativeDoubleCompare(maxcc-mincc, omega)==1 || Tools.relativeDoubleCompare(rcrt, rlcrt)==1 ) // if global range exceeded or range > local range
								{
									//System.out.println("clear" + mincc + " " + maxcc +"   " + rcrt +" " +rlcrt); // all done clear stack and pq, go to next label
									for(Point4D pp:st) 
									{
										localRange.setPixelXYZTDouble(pp.x, pp.y, pp.z, pp.t, Double.POSITIVE_INFINITY);
									}
									st.clear();
									for(PrioritizedElement<Point4D, Double> pe:pq.descendingKeySet())
									{
										Point4D pp=pe.getElement();
										localRange.setPixelXYZTDouble(pp.x, pp.y, pp.z, pp.t, Double.POSITIVE_INFINITY);
									}
									pq.clear();
									break;
								
								}
							
								for(Point4D q:v) // for all neighbors
								{
									int xx2 = xx + q.x;
									int yy2 = yy + q.y;
									int zz2 = zz + q.z;
									int tt2 = tt + q.t;
									if(xx2<0 || xx2>=xdim || yy2<0 || yy2>=ydim || zz2<0 || zz2>=zdim || tt2<0 || tt2>=tdim)
										continue;
									double val3 = inputImage.getPixelXYZTDouble(xx2, yy2, zz2, tt2);
									double rlval=Math.abs(val3-val2); // local range 
								
									int lblq=label.getPixelXYZTInt(xx2, yy2, zz2, tt2);
									if(lblq>0 && lblq!=lblval &&  Tools.relativeDoubleCompare(rlcrt, rlval)>=0 ) // if neighbor label exist and is different of current label and local rnge under range limit
									{
										rlcrt=rlval; // decrease range limit
										if( Tools.relativeDoubleCompare(rcrt, rlcrt) ==1) // if range > to range limit
										{ // labeling done, clear pq and stack 
											//System.out.println("clear2 ");
											for(Point4D pp:st)
											{
												localRange.setPixelXYZTDouble(pp.x, pp.y, pp.z, pp.t, Double.POSITIVE_INFINITY);
											}
											st.clear();
											for(PrioritizedElement<Point4D, Double> pe:pq.descendingKeySet())
											{
												Point4D pp=pe.getElement();
												localRange.setPixelXYZTDouble(pp.x, pp.y, pp.z, pp.t, Double.POSITIVE_INFINITY);
											}
											pq.clear();
											break;
										}
										continue; // goto next neighbor
									}
									double rlq=localRange.getPixelXYZTDouble(xx2, yy2, zz2, tt2); // retrieve saved local range 
									if(Tools.relativeDoubleCompare(rlval, rlcrt) ==1 || Tools.relativeDoubleCompare(rlval, rlq)>=0 ) // if local range > range limit or local range >= saved range
										continue; // goto next neighbor
									else if (Tools.relativeDoubleCompare(rlval, rlq)==-1) // local range < saved range
									{
										localRange.setPixelXYZTDouble(xx2, yy2, zz2, tt2,rlval); // save new local range
										pq.add(new Point4D(xx2,yy2,zz2, tt2), rlval); // insert pixel in pq
									}
								}
							
							}
							while( !st.isEmpty())
							{
								Point4D q = st.pop();
								label.setPixelXYZTInt(q.x, q.y, q.z, q.t, lblval);
								//System.out.println("set " + lblval);
							}
							lblval++;						
						}
					}
				}
			}
		}
	}
	
	/**
	 * Compute (alpha,omega)-connected components
	 * @param inputImage input image
	 * @param alpha local range parametre (double value)
	 * @param omega global range parametre (double value)
	 * @return label map
	 */
	public static IntegerImage exec(Image inputImage, double alpha, double omega)
	{
		return (IntegerImage)(new GrayCCSoille()).process(inputImage,alpha,omega);
	}
	
	/**
	 * Compute (alpha,omega)-connected components
	 * 
	 * @param inputImage input image
	 * @param alpha local range parametre (double value)
	 * @param omega global range parametre (double value)
	 * @param neighborhood neighboorhood constant: see final constants in this class
	 * @return label map
	 */
	public static IntegerImage exec(Image inputImage, double alpha, double omega, int neighborhood)
	{
		return (IntegerImage)(new GrayCCSoille()).process(inputImage,alpha,omega,neighborhood);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*double [] pixels ={1,3,8,7,8,8,2,2,1,9,8,8,9,1,1,0,4,1,1,2,5,1,1,9,3,4,2,6,3,2,7,9,9,1,1,1,0,8,4,9,6,7,0,2,9,3,8,5,9};
		Image im= new DoubleImage(7,7,1,1,1);
		for(int i=0;i<im.size();i++)
			im.setPixelDouble(i, pixels[i]);*/
		double [] pixels ={1,0,4,0,2,3};
		Image im= new DoubleImage(3,2,1,1,1);
		for(int i=0;i<im.size();i++)
			im.setPixelDouble(i, pixels[i]);
		
		
		
		//Image im =ImageLoader.exec("samples/lennaGray256.png");
		MultiView mv =MViewer.exec(im);
		IntegerImage op = GrayCCSoille.exec(im,1.0,1,GrayCCSoille.FOUR_NEIGHBORHOOD);
		mv.add(op);
		mv.add(LabelsToColorByMeanValue.exec(op,im));
	}

}
