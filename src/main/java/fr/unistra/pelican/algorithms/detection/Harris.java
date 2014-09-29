package fr.unistra.pelican.algorithms.detection;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.conversion.AverageChannels;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.Keypoint;
import fr.unistra.pelican.util.data.Corner;


/* Old imports
import java.util.Collections;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.algorithms.conversion.AverageChannels;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.morphology.gray.GrayInternGradient;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.Keypoint;
import fr.unistra.pelican.util.NumericValuedPoint;
import fr.unistra.pelican.util.Point4D;
import fr.unistra.pelican.util.Tools;
import fr.unistra.pelican.util.data.DoubleArrayData;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;
 */

/**
 * Harris Corner Detector
 * 		
 *  k = det(A) - k * trace(A)^2
 * 
 *  Where A is the second-moment matrix 
 * 
 *            | Lx²(x+dx,y+dy)    Lx.Ly(x+dx,y+dy) |
 *  A =  Sum  |                                    | * Gaussian(dx,dy)
 *      dx,dy | Lx.Ly(x+dx,y+dy)  Ly²(x+dx,y+dy)   |
 * 
 *  and k = a/(1+a)^2, 
 *  
 *  where "a" is the minimum ratio between the two eigenvalues
 *  for a point to be considered as a corner.
 *  
 * @author Xavier Philippeau (source : http://www.developpez.net/forums/d325133/general-developpement/algorithme-mathematiques/contribuez/image-detecteur-harris-imagej/#post3363731)
 * @author Julien Bidolet (adaptation for pelican)	
 */
public class Harris extends Algorithm {
	/** Input image */
	public Image image;

	/** Gaussian filter parameter*/
	public double sigma=1.2;

	/**Measure formula parameter*/
	public double k=0.06;

	/** Minimal distance between 2 corners*/
	public int spacing=8;

	/** Output corners list */
	public ArrayList<Keypoint> keypoints = new ArrayList<Keypoint>();


	/** precomputed values of the derivatives */
	private float[][] Lx2,Ly2,Lxy;

	/**
	 *  Constructor
	 */
	public Harris() {
		super.help="Performs Harris corner detection";
		super.inputs="image";
		super.outputs="keypoints";
		super.options="sigma,k,spacing";
	}

	/**
	 * Gaussian function
	 */
	private double gaussian(double x, double y, double sigma) {
		double sigma2 = sigma*sigma;
		double t = (x*x+y*y)/(2*sigma2);
		double u = 1.0/(2*Math.PI*sigma2);
		double e = u*Math.exp( -t );
		return e;
	}

	/**
	 * Sobel gradient 3x3
	 */
	private float[] sobel(int x, int y) {
		int v00=0,v01=0,v02=0,v10=0,v12=0,v20=0,v21=0,v22=0;

		int x0 = x-1, x1 = x, x2 = x+1;
		int y0 = y-1, y1 = y, y2 = y+1;
		if (x0<0) x0=0;
		if (y0<0) y0=0;
		if (x2>=image.xdim) x2=image.xdim-1;
		if (y2>=image.ydim) y2=image.ydim-1;

		v00=image.getPixelXYByte(x0, y0); v10=image.getPixelXYByte(x1, y0); v20=image.getPixelXYByte(x2, y0);
		v01=image.getPixelXYByte(x0, y1);                    				v21=image.getPixelXYByte(x2, y1);
		v02=image.getPixelXYByte(x0, y2); v12=image.getPixelXYByte(x1, y2); v22=image.getPixelXYByte(x2, y2);

		float sx = ((v20+2*v21+v22)-(v00+2*v01+v02))/(4*255f);
		float sy = ((v02+2*v12+v22)-(v00+2*v10+v20))/(4*255f);
		return new float[] {sx,sy};
	}


	/**
	 * Compute the 3 arrays Ix, Iy and Ixy
	 */
	private void computeDerivatives(double sigma){
		this.Lx2 = new float[image.xdim][image.ydim];
		this.Ly2 = new float[image.xdim][image.ydim];
		this.Lxy = new float[image.xdim][image.ydim];

		// gradient values: Gx,Gy
		float[][][] grad = new float[image.xdim][image.ydim][];
		for (int y=0; y<image.ydim; y++)
			for (int x=0; x<image.xdim; x++)
				grad[x][y] = sobel(x,y);

		// precompute the coefficients of the gaussian filter
		int radius = (int)(2*sigma);
		int window = 1+2*radius;
		float[][] gaussian = new float[window][window];
		for(int j=-radius;j<=radius;j++)
			for(int i=-radius;i<=radius;i++)
				gaussian[i+radius][j+radius]=(float)gaussian(i,j,sigma);

		// Convolve gradient with gaussian filter:
		//
		// Ix2 = (F) * (Gx^2)
		// Iy2 = (F) * (Gy^2)
		// Ixy = (F) * (Gx.Gy)
		//
		for (int y=0; y<image.ydim; y++) {
			for (int x=0; x<image.xdim; x++) {

				for(int dy=-radius;dy<=radius;dy++) {
					for(int dx=-radius;dx<=radius;dx++) {
						int xk = x + dx;
						int yk = y + dy;
						if (xk<0 || xk>=image.xdim) continue;
						if (yk<0 || yk>=image.ydim) continue;

						// gaussian weight
						double gw = gaussian[dx+radius][dy+radius];

						// convolution
						this.Lx2[x][y]+=gw*grad[xk][yk][0]*grad[xk][yk][0];
						this.Ly2[x][y]+=gw*grad[xk][yk][1]*grad[xk][yk][1];
						this.Lxy[x][y]+=gw*grad[xk][yk][0]*grad[xk][yk][1];
					}
				}
			}
		}
	}

	/**
	 * compute harris measure for a pixel
	 */
	private float harrisMeasure(int x, int y, float k) {
		// matrix elements (normalized)
		float m00 = this.Lx2[x][y]; 
		float m01 = this.Lxy[x][y];
		float m10 = this.Lxy[x][y];
		float m11 = this.Ly2[x][y];

		// Harris corner measure = det(M)-k.trace(M)^2
		return m00*m11 - m01*m10 - k*(m00+m11)*(m00+m11);
	}

	/**
	 * return true if the measure at pixel (x,y) is a local spatial Maxima
	 */
	private boolean isSpatialMaxima(float[][] hmap, int x, int y) {
		int n=8;
		int[] dx = new int[] {-1,0,1,1,1,0,-1,-1};
		int[] dy = new int[] {-1,-1,-1,0,1,1,1,0};
		double w =  hmap[x][y];
		for(int i=0;i<n;i++) {
			double wk = hmap[x+dx[i]][y+dy[i]];
			if (wk>=w) return false;
		}
		return true;
	}

	/**
	 * compute the Harris measure for each pixel of the image
	 */
	private float[][] computeHarrisMap(double k) {

		// Harris measure map
		float[][] harrismap = new float[image.xdim][image.ydim];

		// for each pixel in the image
		for (int y=0; y<image.ydim; y++) {
			for (int x=0; x<image.xdim; x++) {
				// compute the harris measure
				double h =  harrisMeasure(x,y,(float)k);
				if (h<=0) continue;
				// log scale
				h = 255 * Math.log(1+h) / Math.log(1+255);
				// store
				harrismap[x][y]=(float)h;
			}
		}

		return harrismap;
	}

	/**
	 * Perfom the Harris Corner Detection
	 * 
	 * @param sigma gaussian filter parameter 
	 * @param k parameter of the harris measure formula
	 * @param minDistance minimum distance between corners
	 * @return the orginal image marked with cross sign at each corner
	 */
	public void filter(double sigma, double k, int minDistance) {
		
		// precompute derivatives
		computeDerivatives(sigma);

		// Harris measure map
		float[][] harrismap = computeHarrisMap(k);
		ArrayList<Corner> corners = new ArrayList<Corner>();
		// for each pixel in the harrismap 
		for (int y=1; y<image.ydim-1; y++) {
			for (int x=1; x<image.xdim-1; x++) {
				// thresholding : harris measure > epsilon
				float h = harrismap[x][y];
				if (h<=1E-3) continue;
				// keep only a local maxima
				if (!isSpatialMaxima(harrismap, x, y)) continue;
				// add the corner to the list
				corners.add( new Corner(x,y,h) );
			}
		}

		// remove corners to close to each other (keep the highest measure)
		Iterator<Corner> iter = corners.iterator();
		while(iter.hasNext()) {
			Corner p = iter.next();
			for(Corner n:corners) {
				if (n==p) continue;
				int dist = (int)Math.sqrt( (p.getX()-n.getX())*(p.getX()-n.getX())+(p.getY()-n.getY())*(p.getY()-n.getY()) );
				if(dist>minDistance) continue;
				if (n.getH()<p.getH()) continue;
				iter.remove();
				break;
			}
		}
		keypoints = new ArrayList<Keypoint>();
		for (Corner p:corners) {
			keypoints.add(new Keypoint(p.getX(),p.getY()));
		}	
		
	}

	@Override
	public void launch() throws AlgorithmException {
		filter(sigma, k, spacing);
	}

	/**
	 * Perform Harris corner detection
	 * @param image Input image
	 * @return List of detected corners
	 */
	public static ArrayList<Keypoint> exec(Image image) {
		return (ArrayList<Keypoint>) new Harris().process(image);

	}

	/**
	 * Perform Harris corner detection
	 * @param image Input image
	 * @param gaussian Gaussian filter parameter
	 * @return List of detected corners
	 */
	public static ArrayList<Keypoint> exec(Image image,double gaussian) {
		return (ArrayList<Keypoint>) new Harris().process(image,gaussian);

	}
	/**
	 * Perform Harris corner detection
	 * @param image Input image
	 * @param gaussian Gaussian filter parameter
	 * @param k parameter of the harris measure formula
	 * @return List of detected corners
	 */
	public static List<Corner> exec(Image image,double gaussian,double k) {
		return (List<Corner>) new Harris().process(image,gaussian);

	}

	/**
	 * Perform Harris corner detection
	 * @param image Input image
	 * @param gaussian Gaussian filter parameter
	 * @param k parameter of the harris measure formula
	 * @param spacing Minimal space between two corners
	 * @return List of detected corners
	 */
	public static ArrayList<Keypoint> exec(Image image,double gaussian,double k,int spacing) {
		return (ArrayList<Keypoint>) new Harris().process(image,gaussian,spacing);

	}

	/**
	 * Perform Harris corner detection
	 * @param image Input image
	 * @param gaussian Gaussian filter parameter
	 * @param spacing Minimal space between two corners
	 * @return List of detected corners
	 */
	public static ArrayList<Keypoint> exec(Image image,double gaussian,int spacing) {
		return (ArrayList<Keypoint>) new Harris().process(image,gaussian,spacing);

	}

	/**
	 * Perform Harris corner detection
	 * @param image Input image
	 * @param spacing Minimal space between two corners
	 * @return List of detected corners
	 */
	public static ArrayList<Keypoint> exec(Image image,int spacing) {
		return (ArrayList<Keypoint>) new Harris().process(image,spacing);

	}

	public static void main(String[] args) {
		Image i = ImageLoader.exec("samples/lenna.png");
		i = AverageChannels.exec(i);

		ArrayList<Keypoint> keypoints = new Harris().exec(i);
		for (Keypoint k : keypoints) {
			// add the cross sign over the image
			for (int dt=-3; dt<=3; dt++) {
				if (k.x+dt>=0 && k.x+dt<i.xdim ) i.setPixelXYByte((int) k.x+dt,(int) k.y, 255);
				if (k.y+dt>=0 && k.y+dt<i.ydim) i.setPixelXYByte((int) k.x,(int) k.y+dt, 255);
			}
			//System.out.println("corner found at: "+p.x+","+p.y+" ("+p.h+")");
		}	
		Viewer2D.exec(i);


	}
}
/*
 * Old Version
 */
/**
 *	The classical Harris interest point detector
 *
 *	@author Erchan Aptoula
 *	@author Régis Witz ( recovery & customization )
 *//*

public class Harris extends Algorithm {

  *//**	Input (grayscale) image. *//*
	public Image input;
   *//**	Output image. *//*
	public ArrayList<Keypoint> output;

    *//**	Tunable sensitivity parameter. *//*
	public double alpha = 0.06; // \in [0.04,0.06]
     *//**	Number of points to keep. *//*
	public int nbpoints = 50;



	public Harris() {
		super.inputs = "input";
		super.options = "nbpoints,alpha";
		super.outputs = "output";

	}

	@SuppressWarnings("unchecked")
	public static ArrayList<Keypoint> exec( Image input ) { 
		return ( ArrayList<Keypoint> ) new Harris().process( input );
	}
	@SuppressWarnings("unchecked")
	public static ArrayList<Keypoint> exec( Image input, int nbpoints ) { 
		return ( ArrayList<Keypoint> ) new Harris().process( input, nbpoints );
	}
	@SuppressWarnings("unchecked")
	public static ArrayList<Keypoint> exec( Image input, int nbpoints, double alpha ) { 
		return ( ArrayList<Keypoint> ) new Harris().process( input, nbpoints, alpha );
	}

      *//**	@see fr.unistra.pelican.Algorithm#launch() *//*
	public void launch() throws AlgorithmException { 

		// horizontal differential
		BooleanImage seH = FlatStructuringElement2D.createHorizontalLineFlatStructuringElement(3);
		Image inputX = (Image) new GrayInternGradient().process( this.input, seH );

		Viewer2D.exec(inputX);

		// vertical differential
		BooleanImage seV = FlatStructuringElement2D.createVerticalLineFlatStructuringElement(3);
		Image inputY = (Image) new GrayInternGradient().process( this.input, seV );

		Viewer2D.exec(inputY);

		// 8-neighborhood
		BooleanImage se = FlatStructuringElement2D.createSquareFlatStructuringElement(3);
		Point4D[] points = se.foreground();

		double max= Double.NEGATIVE_INFINITY;

		ArrayList<NumericValuedPoint> tmp = new ArrayList<NumericValuedPoint>();
		double p;
		for ( int x = 0 ; x < input.getXDim() ; x++ ) { 
			for ( int y = 0; y < input.getYDim(); y++ ) { 

				// matrix elements
				double diffX2 = 0;
				double diffY2 = 0;
				double diffX = 0;
				double diffY = 0;

				for ( int i = 0 ; i < points.length ; i++ ) { 

					int valX = x - se.getCenter().x + points[i].y;
					int valY = y - se.getCenter().y + points[i].x;

					if ( valX >= 0 && valX < this.input.getXDim() 
					  && valY >= 0 && valY < this.input.getYDim() ) { 

						p = inputX.getPixelXYDouble(valX, valY);
						diffX2 += p * p;
						diffX += p;

						p = inputY.getPixelXYDouble(valX, valY);
						diffY2 += p * p;
						diffY += p;
					}
				}

				double determinant = diffX2*diffY2 - diffX*diffX * diffY*diffY;
				double trace = diffX2 + diffY2;

				double h = determinant - this.alpha * trace*trace;
				tmp.add( new NumericValuedPoint( x,y,h ) );

				if(h>max)
					max=h;
			}
		}

		Collections.sort( tmp );

		for(int i=tmp.size()-1; i > tmp.size()-51; i--)
		{
			System.out.println(tmp.get(i).getValue());
		}

		System.out.println("Max : "+max);

		this.output = new ArrayList<Keypoint>();
		for ( int i = 0 ; i < this.nbpoints ; i++ ) 
		//for(int i=tmp.size()-1; i > tmp.size()-this.nbpoints-1; i--)
		{
			DoubleArrayData data = new  DoubleArrayData();
//			data.setDescriptor( null );
			Double[] descriptor = new Double[1];
			descriptor[0] = tmp.get(i).getValue().doubleValue();
			data.setValues( descriptor );

			this.output.add( new Keypoint(tmp.get(i).getX(), tmp.get(i).getY(),data) );
		}
	}


	 public static void main(String[] args) { 

		Image img = ImageLoader.exec("samples/lenna256.png");
		Viewer2D.exec(img, "init");
		img = (Image) AverageChannels.exec(img);
		Viewer2D.exec(img, "AverageChannels");
		ArrayList<Keypoint> res = Harris.exec( img );

		for ( int p = 0 ; p < img.size(); p++ ) img.setPixelBoolean( p,false );
		Double[] values;
		for ( Keypoint key : res ) { 


//			values = ( Double[] ) key.data.getValues();
//			img.setPixelXYDouble( (int)key.x,(int)key.y,values[0] );
			img.setPixelXYDouble( (int)key.x,(int)key.y,1.0 );
		}
		Viewer2D.exec( img, "sonuc " );

		System.out.println( Tools.imageVolume( img, 0 ) );
	}

}*/


