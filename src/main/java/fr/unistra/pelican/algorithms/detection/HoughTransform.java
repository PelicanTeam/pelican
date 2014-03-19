/**
 * 
 */
package fr.unistra.pelican.algorithms.detection;

import java.util.Arrays;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.arithmetic.Blending;
import fr.unistra.pelican.algorithms.edge.Sobel;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.morphology.connected.ExtractMinimaOrMaxima;
import fr.unistra.pelican.algorithms.morphology.connected.ExtractMinimaOrMaxima.Operation;
import fr.unistra.pelican.algorithms.morphology.gray.GrayMedian;
import fr.unistra.pelican.algorithms.visualisation.MViewer;
import fr.unistra.pelican.util.IMath;
import fr.unistra.pelican.util.Line;
import fr.unistra.pelican.util.Tools;
import fr.unistra.pelican.util.connectivityTrees.connectivity.TrivialConnectivity;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 * <p>Performs a Hough transform of the XY space for line detection. Result is the accumulation buffer in (r-theta) space.
 * <br>Result x dim is theta dim taking value in [0;pi]
 * <br>Result y dim is r dim taking value in [0; sqrt(xdim^2+ydim^2)]
 * <p>Values of input image is supposed to be all positives.
 * <p>deltaTheta specifies quantization step of theta, default value is 0.01.
 * <br>deltaR specifies quantization step of r; default value is 1.0
 * <p>deltaTheta can be easily retrieved from result dimension but deltaR must be stored in buffer properties under key R_SCALE
 * 
 * <p>static method getLineFromBuffer is here to convert a point coordinate in r-theta space into a line in xy space
 * 
 * @author Benjamin Perret
 *
 */
public class HoughTransform extends Algorithm {

	/**
	 * Key to store the deltaR parameter in accumulation buffer properties.
	 * This value is necessary to obtain right results with function getLineFromBuffer
	 */
	public static final String R_SCALE="HOUGH_TRANSFORM_R_SCALE";
	
	/**
	 * Input image
	 */
	public Image image;
	
	/**
	 * resolution for theta quantization
	 */
	public double deltaTheta=0.01;
	
	/**
	 * resolution of the r quantization
	 */
	public double deltaR=1.0;
	
	/**
	 * Result : accumulation buffer.
	 */
	public DoubleImage accumulator ;
	
	/**
	 * Are we working in a cylinder space (y dimension is wrapped)
	 */
	public boolean cylinderSpace=false;
	
	/**
	 * To avoid disprecancies in calculus for nearly vertical lines, an angle limit is defined. 
	 * Under this limit line is assumed to be vertical and wrapping is not considered.
	 * The angle is the angle between a vertical line and the considered line.
	 * 
	 */
	public double angleLimitForWrapping=0.3;
	
	/**
	 * 
	 */
	public HoughTransform() {
		super();
		super.inputs="image";
		super.options="deltaR,deltaTheta,cylinderSpace,angleLimitForWrapping";
		super.outputs="accumulator";
	}

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	@Override
	public void launch() throws AlgorithmException {
		if(image.bdim>1 || image.tdim>1 || image.zdim>1)
		{
			System.err.println("Hough Transform warning : I will only process XY dims, ZTB are ignored. Input image is " +image);
		}
		
		int ydim = (int)Math.ceil(Math.sqrt(image.xdim*image.xdim+image.ydim*image.ydim)/deltaR);
		int xdim = (int)Math.ceil(Math.PI/deltaTheta);
		accumulator=new DoubleImage(xdim,2*ydim,1,1,1);
		accumulator.properties.put(R_SCALE, deltaR);
		double cos [] =new double[xdim];
		double sin [] =new double[xdim];
		int c=0;
		for(double theta=-Tools.piD2; theta <= Tools.piD2;theta+=deltaTheta)
		{
			cos[c]=Math.cos(theta);
			sin[c]=Math.sin(theta);
			c++;
		}
		for(int y=0;y<image.ydim;y++)
		{
			for(int x=0;x<image.xdim;x++)
			{
				double v=image.getPixelXYDouble(x, y);
				if(v>0)
				{
					c=0;
					for(double theta=-Tools.piD2; theta <= Tools.piD2;theta+=deltaTheta)
					{
						double r=(((double)y)*cos[c]+((double)x)*sin[c]);
						if(cylinderSpace)
						{
							if(Math.abs(theta-Tools.piD2)>angleLimitForWrapping && Math.abs(theta+Tools.piD2)>angleLimitForWrapping)
							{
								// y=ax+b 	
								double b=r/cos[c];
								// space wrapping
								double b2=Tools.modulo(b, image.ydim); // compute wrapping
								if(b2!=b) // wrapping occured?
								{
									double k=b2/b; // simple geometry
									double r2=r*k;
									b=b2;
									r=r2;
									
								}
								
							}
							int ri=(int)((r/deltaR+ydim +0.5));
							accumulator.setPixelXYDouble(c,ri , v+accumulator.getPixelXYDouble(c, ri));
						}
						else {
							int ri=(int)((r/deltaR+ydim +0.5));
							accumulator.setPixelXYDouble(c,ri , v+accumulator.getPixelXYDouble(c, ri));
						}
						
						
						c++;
					}
				}
			}
		}
		
		

	}

	/**
	 * Get a line from a point in the theta-r space of the accumulation buffer
	 * @param accumulationBuffer accumulationBuffer, result of HoughTransform
	 * @param rCoord rCoord(y) of the point in the theta-r space
	 * @param thetaCoord thetaCoord(x)  of the point in the theta-r space
	 * @return corresponding line in xy space
	 */
	public static Line getLineFromBuffer(DoubleImage accumulationBuffer, int thetaCoord, int rCoord)
	{
		double deltaR=1.0;
		Object o=accumulationBuffer.properties.get(R_SCALE);
		if(o != null && o instanceof Double)
		{
			deltaR=(Double)o;
		
		}
		double theta=(Math.PI/(double)(accumulationBuffer.xdim))*(double)thetaCoord-Tools.piD2;
		double r=(rCoord-accumulationBuffer.ydim/2)*deltaR;
		//System.out.println("theta " +theta + " dist " +r );
		Line l;
		if(theta==0.0)
		{
			System.out.println("ligne verticale");
			l=new Line((int)r,0,(int)r,(int)((accumulationBuffer.ydim/2)*deltaR));
		}else{
			double a=-Math.sin(theta)/Math.cos(theta);
			double b=r/Math.cos(theta);
			int x1=0;
			int y1=(int)(b+0.5);
			
			int x2=(int)((accumulationBuffer.ydim)*deltaR);
			if(y1>x2)
			{
				y1=x2;
				x1=(int)((y1-b)/a);
			}else if(y1<-x2)
			{
				y1=-x2;
				x1=(int)((y1-b)/a);	
			}
			int y2=(int)(a*x2+b);
			if(y2>x2)
			{
				y2=x2;
				x2=(int)((y2-b)/a);
			}else if(y2<-x2)
			{
				y2=-x2;
				x2=(int)((y2-b)/a);	
			}
			l=new Line(x1,y1,x2,y2);
		}
		return l;
		
	}
	
	public static DoubleImage exec(Image image)
	{
		return(DoubleImage) new HoughTransform().process(image);
	}
	
	public static DoubleImage exec(Image image,double deltaR, double deltaTheta)
	{
		return(DoubleImage) new HoughTransform().process(image,deltaR,deltaTheta);
	}
	
	public static DoubleImage exec(Image image,double deltaR, double deltaTheta,boolean cylinderSpace)
	{
		return(DoubleImage) new HoughTransform().process(image,deltaR,deltaTheta,cylinderSpace);
	}
	
	public static DoubleImage exec(Image image, double deltaR)
	{
		return(DoubleImage) new HoughTransform().process(image,deltaR);
	}
	


}
