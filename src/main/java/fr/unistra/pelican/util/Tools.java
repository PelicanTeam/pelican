package fr.unistra.pelican.util;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Vector;

import javax.media.jai.PlanarImage;
import javax.media.jai.RasterFactory;
import javax.media.jai.TiledImage;

import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.InvalidParameterException;
import fr.unistra.pelican.algorithms.conversion.AverageChannels;

/**
 * 
 * Class containing various static tools..
 * 
 * @author E.A, B.P.
 */
public  class Tools
{
	
	/**
	 * Never instanciate utility class
	 */
	private Tools(){
		
	}
	
	/**
	 * Static initializer, ensure that decimal format uses '.' as decimal separator (stupid localization)
	 */
	{
		DecimalFormatSymbols dfs=df.getDecimalFormatSymbols();
		dfs.setDecimalSeparator('.');
		df.setDecimalFormatSymbols(dfs); // stupid border effect protection of the get method
	}
	
	/**
	 * To print floating point numbers
	 */
	public static DecimalFormat df = new DecimalFormat("###.###");
	
	/**
	 * A (relatively) small number (10e-6)
	 */
	public static final double epsilon = 10e-6;
	
	/**
	 * Constant PI/2
	 */
	public static  final double piD2=Math.PI/2.0;
	
	/**
	 * Constant 2*PI
	 */
	public static  final double piX2=Math.PI*2.0;
	
	
	/**
	 * Each descriptor is considered as a bi-dimensional table of
	 * "size" lines. Each line represents an inner descriptor.
	 * 
	 * Each inner descriptor of p1, is compared against the inner descriptors
	 * of p2, and if they are similar above the fixed threshold, then a vote is cast for p2.
	 * 
	 * The total number of votes is returned.
	 * 
	 * @param p1 first descriptor
	 * @param p2 second descriptor
	 * @param size the size of each inner descriptor
	 * @param threshold the threshold above which a vote is cast
	 * @return the resulting number of votes for p2.
	 */
	public static double voteBasedDistance(double[] p1,double[] p2,int size,double threshold)
	{
		double votes = 0;
		
		if(p1.length != p2.length){
			System.err.println("Vote based distance: Incompatible descriptor lengths");
			return -1.0;
		}
		
		int numberOfDescriptors = p1.length/size;
		
		boolean[] flags = new boolean[numberOfDescriptors];
		
		for(int i = 0; i < numberOfDescriptors; i++){
			
			// get the dinner descriptor
			double[] descr1 = new double[size];
			for(int k = 0; k < size; k++)
				descr1[k] = p1[i * size + k];
			
			// now find the most similar inner descriptor of p2,
			// also above the given threshold...p2 can have at most once
			// the same interest point.
			double minDistance = Double.MAX_VALUE;
			int minIndex = -1;
			for(int j = 0; j < numberOfDescriptors; j++){
				if (flags[j] == true) continue;
				// get the dinner descriptor
				double[] descr2 = new double[size];
				for(int k = 0; k < size; k++)
					descr2[k] = p2[j * size + k];
				
				double distance = histogramDistance(descr1,descr2);
				
				if (distance < minDistance && distance <= threshold){
					minDistance = distance;
					minIndex = j;
				}
			}
			
			// if a suitable match has been detected then cast a vote
			if (minDistance < Double.MAX_VALUE){
				votes++;
				flags[minIndex] = true;
			}
		}
		
		return Integer.MAX_VALUE - votes;	// more votes => less distance
	}
	
	/**
	 * Computes the volume of an image
	 * 
	 * @param img the input image
	 * @param b the channel to process
	 * @return the binary volume
	 */
	public static double imageVolume(Image img,int b)
	{
		double volume = 0.0;
		
		for(int x = 0; x < img.getXDim(); x++){
			for(int y = 0; y < img.getYDim(); y++){
				volume += img.getPixelXYBDouble(x,y,b);
			}
		}
		
		return volume;
	}

	/**
	 * Standard histogram distance
	 * 
	 * Attention:The histograms have to have been normalized
	 * 
	 * @param p1 first histogram
	 * @param p2 second histogram 
	 * @return the histogram distance
	 */
	public static double histogramDistance( double[] p1,double[] p2)
	{
		double dist = 0.0;
		int bins = p1.length;
		if ( bins != p2.length ) { 

			System.err.println("Histogram Distance: Incompatible histogram bin numbers");
			return 1.0;
		}
		
		for ( int i = 0 ; i < bins ; i++ ) dist += Math.abs( p1[i]-p2[i] );
		if ( bins > 0 ) dist /= bins;
		
		return dist;
	}
	
	/**
	 * Pyramid match distance
	 * 
	 * @param p1 first histogram
	 * @param p2 second histogram
	 * @param scales the number of scales
	 * @param levelSize size of each pyramid level
	 * @return the histogram distance
	 */
	public static double pyramidMatchDistance(double[] p1,double[] p2,int scales,int levelSize)
	{
		double dist = 0.0;
		
		if(p1.length != p2.length){
			System.err.println("Histogram Distance: Incompatible histogram bin numbers");
			return -1.0;
		}
		
		for(int s = 0; s < scales; s++){
			for(int i = 0; i < levelSize; i++){
				int index = s * levelSize + i;
				dist += (1 / Math.pow(2.0,s)) * Math.abs(p1[index] - p2[index]) / (1 + p1[index] + p2[index]);
			}
		}
		
		return dist;
	}


	
	/**
	 * 
	 * @param p1
	 * @param p2
	 * @return the resulting distance
	 */
	public static double correlogramDistance(double[] p1,double[] p2)
	{
		double dist = 0.0;
		
		if(p1.length != p2.length){
			System.err.println("Correlogram Distance: Incompatible correlogram bin numbers");
			return -1.0;
		}
		
		for(int i = 0; i < p1.length; i++){
			dist += Math.abs(p1[i] - p2[i]) / (1 + p1[i] + p2[i]);
		}
		
		return dist;
	}
	
	/**
	 * Standard Euclidean distance
	 * @param p1
	 * @param p2
	 * @return the resulting distance
	 */
	public static double euclideanDistance(double[] p1,double[] p2)
	{
		double dist = 0.0;
		
		if(p1.length != p2.length){
			System.err.println("Euclidean Distance: Incompatible vector lengths");
			return -1.0;
		}
		
		for(int i = 0; i < p1.length; i++)
			dist += (p1[i] - p2[i]) * (p1[i] - p2[i]);

		return Math.sqrt(dist);
	}
	
	/**
	 * Compute distance between 2 angles, wrapping (2pi) of the angle space is considered 
	 * @param a
	 * @param b
	 * @return
	 */
	public static double angleDistance(double a, double b) {

		double angle = modulo((Math.abs(a - b)), piX2);

		if (angle > Math.PI)
			angle = piX2 - angle;

		return angle;
	}
	
	/**
	 * Compute distance between 2 angles, wrapping (pi) of the angle space is considered 
	 * @param a
	 * @param b
	 * @return
	 */
	public static double angleDistancePI(double a, double b) {

		double angle = modulo((Math.abs(a - b)), Math.PI);

		if (angle > piD2)
			angle = Math.PI - angle;

		return angle;
	}
	
	
	/**
	 * Colour distribution entropy distance
	 * @param p1
	 * @param p2
	 * @return the resulting distance
	 */
	public static double CDEDistance(double[] p1,double[] p2)
	{
		double dist = 0.0;
		
		if(p1.length != p2.length){
			System.err.println("CDE Distance: Incompatible vector lengths");
			return -1.0;
		}
		
		for(int i = 0; i < p1.length; i +=2){
			double histoDistance = 1.0 - Math.min(p1[i],p2[i]);
			
			double entropyDistance = 0.0;
			
			//if (Math.max(p1[i+1],p2[i+1]) != 0.0)
			entropyDistance = Math.min(p1[i+1],p2[i+1])/Math.max(p1[i+1],p2[i+1]);
			
			dist += histoDistance * entropyDistance;
		}
		
		return Math.sqrt(dist);
	}
	
	/**
	 * 
	 * @param p1
	 * @param p2
	 * @return the manhattan distance
	 */
	public static double manhattanDistance(double[] p1,double[] p2)
	{
		double dist = 0.0;
		
		if(p1.length != p2.length){
			System.err.println("ManhattanDistance: Incompatible vector lengths");
			return -1.0;
		}
		
		for(int i = 0; i < p1.length; i++)
			dist += Math.abs(p1[i] - p2[i]);
		
		return dist;
	}
	
	/**
	 * 
	 * @param p
	 * @return the L infinite norm of p
	 */
	public static double infiniteNorm(double[] p)
	{
		double norm = 0.0;
		
		for(int i = 0; i < p.length; i++)
			norm = Math.max(norm,Math.abs(p[i]));
		
		return norm;
	}
	
	/**
	 * 
	 * @param p1
	 * @param p2
	 * @return the infinite distance between vectors p1 and p2
	 */
	public static double infiniteDistance(double[] p1,double[] p2)
	{
		double dist = 0.0;
		
		if(p1.length != p2.length){
			System.err.println("ManhattanDistance: Incompatible vector lengths");
			return -1.0;
		}
		
		for(int i = 0; i < p1.length; i++)
			dist = Math.max(dist,Math.abs(p1[i] - p2[i]));
		
		return dist;
	}
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @param sigma
	 * @return the gaussian
	 */
	public static double Gaussian2D(int x,int y,double sigma)
	{
		return 1 / (2 * Math.PI * sigma * sigma) * Math.exp(-1 * (x * x + y * y)/(2 * sigma * sigma));
	}


	/**	Vector normalization.
	 *	@param v vector to normalize.
	 *	@return <tt>v</tt> normalized.
	 *	@author Régis Witz
	 */
	public static Double[] vectorNormalize( Double[] v ) { 

		double sum = 0.;
		for ( int i = 0 ; i < v.length ; i++ ) sum += v[i];
		Double[] vn = new Double[ v.length ];
		if ( sum != 0. ) 
			for ( int i = 0 ; i < v.length ; i++ ) vn[i] = v[i] / sum;
		return vn;
	}

	/**
	 * 3d only
	 * 
	 * @param p1 3d vector
	 * @param p2 3d vector
	 * @return the vectorial product of p1 and p2
	 */
	public static double[] vectorProduct(double[] p1,double[] p2)
	{
		double[] d = new double[3];
		
		d[0] = p1[1] * p2[2] - p1[2] * p2[1];
		d[1] = p1[2] * p2[0] - p1[0] * p2[2];
		d[2] = p1[0] * p2[1] - p1[1] * p2[0];
		
		return d;
	}
	
	/**
	 * vector division
	 * 
	 * @param p vector
	 * @param s divider
	 * @return the marginal division
	 */
	public static double[] vectorDivision(double[] p,double s)
	{
		double[] d = new double[p.length];
		
		for(int i = 0; i < d.length; i++)
			d[i] = p[i] / s;
		
		return d;
	}
	
	/**
	 * vector addition
	 * 
	 * @param p1 vector
	 * @param p2 vector
	 * @return the vectorial sum of p1 and p2
	 */
	public static double[] vectorSum(double[] p1,double[] p2)
	{
		double[] d = new double[p1.length];
		
		for(int i = 0; i < d.length; i++)
			d[i] = p1[i] + p2[i];
		
		return d;
	}
	
	/**
	 * 
	 * @param sat
	 * @return the saturation weighted hue
	 */
	public static double saturationWeightedHue(double sat)
	{
		return 1 / (1 + Math.exp(-10 * (sat - 0.5)));
	}
	
	/**
	 * 
	 * @param sat
	 * @param lum
	 * @return the saturation and luminance weighted hue
	 */
	public static double saturationLuminanceWeightedHue(double sat,double lum)
	{
		double satCoeff = 1 / (1 + Math.exp(-10 * (sat - 0.5)));
		double lumCoeff = 1 / (1 + Math.exp(-10 * (lum - 0.5)));
		
		return lumCoeff * satCoeff;
	}
	
	/**
	 * 
	 * @param p1
	 * @param p2
	 * @return the HSL distance
	 */
	public static double HSLDistance(double[] p1,double[] p2)
	{	
		if(p1.length != p2.length || p2.length != 3 || p1.length != 3){
			System.err.println("HSLDistance: Incompatible vector lengths");
			return -1.0;
		}
		
		double huedist = 2 * hueDistance(p1[0],p2[0]);
		double lumDist = (p1[2] - p2[2]) * (p1[2] - p2[2]);
		
		double alpha = 1/(1 + Math.exp(-5 * (p1[1] - 0.5))) * 1/(1 + Math.exp(-5 * (p2[1] - 0.5)));
		
		double dist = huedist * alpha + (1 - alpha) * lumDist;
		
		return Math.sqrt(dist);
	}
	
	/**
	 * 
	 * @param p
	 * @return the euclidean norm
	 */
	public static double euclideanNorm(double[] p)
	{
		double norm = 0.0;
		
		for(int i = 0; i < p.length; i++)
			norm += p[i] * p[i];
		
		return Math.sqrt(norm);
	}
	
	/**
	 * 
	 * @param p
	 * @return the euclidean norm
	 */
	public static double euclideanNorm(int[] p)
	{
		double norm = 0.0;
		
		for(int i = 0; i < p.length; i++)
			norm += p[i] * p[i];
		
		return Math.sqrt(norm);
	}
	
	/**
	 * Compute dot product of a and b;
	 * let a=[a1,a2,...,an] and b=[b1,b2,...,bm]
	 * Result is:
	 * 	- if n!=m => NaN and an error message is printed, perhaps shall we throw a runtime 
	 * 		exception trying to compute dot product of vectors of different size is clearly a design error.
	 *  - if n==m => a1*b1+a2*b2+...+an*bn
	 * 
	 * @param a
	 * @param b
	 * @return dot product
	 */
	public static double DotProduct(double [] a, double [] b)
	{
		double res=0.0;
		if(a.length!=b.length)
		{
			res=Double.NaN;
			System.err.println("Function 'DotProduct' was called with vectors of different sizes: value returned was NaN.\n Implied vectors were " + ArrayToolbox.printString(a) + " and "+ ArrayToolbox.printString(b));
		}
		for(int i=0;i<a.length;i++)
			res+=a[i]*b[i];
		return res;
	}
	
	
	/**
	 * 
	 * @param p1
	 * @param p2
	 * @return the vector difference
	 */
	public static double[] VectorDifference(double[] p1,double[] p2)
	{
		if(p1.length != p2.length){
			System.err.println("Incompatible vector lengths");
			return null;
		}
		
		double[] fark = new double[p1.length];
		
		for(int i = 0; i < p1.length; i++)
			fark[i] = p1[i] - p2[i];
		
		return fark;
	}
	
	public static boolean VectorIsNull(double []v)
	{
		for(double d :v)
			if(d!=0)return false;
		return true;
	}
	
	public static boolean relativeVectorIsNull(double []v)
	{
		for(double d :v)
			if(!relativeDoubleEquality(d, 0))return false;
		return true;
	}
	
	/**
	 * 
	 * @param p1
	 * @param p2
	 * @return the vector difference
	 */
	public static int[] VectorDifference(int[] p1,int[] p2)
	{
		if(p1.length != p2.length){
			System.err.println("Incompatible vector lengths");
			return null;
		}
		
		int[] diff = new int[p1.length];
		
		for(int i = 0; i < p1.length; i++)
			diff[i] = p1[i] - p2[i];
		
		return diff;
	}
	
	
	/**
	 * 
	 * @param p1
	 * @param p2
	 * @return the vector average
	 */
	public static double[] VectorAverage(double[] p1,double[] p2)
	{
		if(p1.length != p2.length){
			System.err.println("Incompatible vector lengths");
			return null;
		}
		
		double[] ort = new double[p1.length];
		
		for(int i = 0; i < p1.length; i++)
			ort[i] = (p1[i] + p2[i]) / 2.0;
		
		return ort;
	}
	
	/**
	 * parametrized comparison of doubles
	 * 
	 * Warning if you work with very large or very small number consider using
	 * relativeDoubleEquality.
	 * See http://www.cygnus-software.com/papers/comparingfloats/comparingfloats.htm 
	 * 
	 * @param d1
	 * @param d2
	 * @return the result of double comparison
	 */
	public static double doubleCompare(double d1,double d2)
	{
		double diff = d1 - d2;
		
		if(diff >= -1.0 * epsilon && diff <= epsilon) return 0;
		else if (diff > 0.0) return 1;
		else return -1;
		
	}
	
	/**
	 * Test if a double number is not NaN or PositiveInfinity or NegativeInfinity
	 * @param a
	 * @return
	 */
	public static boolean isValue(double a)
	{
		return !(Double.isInfinite(a) || Double.isNaN(a));
	}
	
	
	/**
	 * Robust floating point comparison, using integer representation. 
	 * By this way number A and B are said to be equal if it 
	 * exists less than 4*2048 numbers representable in the IEEE 754 format between A and B.
	 *
	 * @param A
	 * @param B
	 * @return relative double equality
	 */
	public static boolean relativeDoubleEquality(double A, double B)
	{
		return relativeDoubleEquality(A,B,2048*4);
	}
	
	/**
	 * Robust floating point comparison, using integer representation. 
	 * By this way number A and B are said to be equal if it 
	 * exists less than maxUlps numbers representable in the IEEE 754 format between A and B.
	 *
	 * There is not direct relation between maxUlps and the relative error abs((A-B)/A)
	 * but for example taking maxUlps=4*2048 will ensure that relative error is less than 10e-12
	 *  
	 *  
	 * @param A
	 * @param B
	 * @param maxUlps
	 * @return relative double equality
	 */
	public static boolean relativeDoubleEquality(double A, double B, long maxUlps)

	{

	    // Make sure maxUlps is non-negative and small enough that the

	    // default NAN won't compare as equal to anything.
		
	    if(!(maxUlps > 0 && maxUlps < 4L * 1024L * 1024L* 1024L * 1024L))
	    	maxUlps=2048*4;
	    
		// get integer representation in IEEE 754 format
	    long aInt = Double.doubleToLongBits(A);

	    // Make aInt lexicographically ordered as a twos-complement int

	    if (aInt < 0L)
	    	// no complement 2 representation
	        aInt = 0x8000000000000000L - aInt;

	    // Make bInt lexicographically ordered as a twos-complement int
	    // get integer representation in IEEE 754 format
	    
	    long bInt = Double.doubleToLongBits(B);

	    if (bInt < 0)
	    	// no complement 2 representation
	        bInt = 0x8000000000000000L - bInt;

	    long intDiff = Math.abs(aInt - bInt);

	    if (intDiff <= maxUlps)

	        return true;

	    return false;

	}
	
	
	/**
	 * Robust floating point comparison, using integer representation. 
	 * By this way number A and B are said to be equal if it 
	 * exists less than 4*2048 numbers representable in the IEEE 754 format between A and B.
	 *
	 * @param A
	 * @param B
	 * @return relative double equality
	 */
	public static boolean relativeDoubleArrayEquality(double [] A, double [] B)
	{
		return relativeDoubleArrayEquality(A,B,2048*4);
	}
	
	/**
	 * Robust floating point comparison, using integer representation. 
	 * By this way number A and B are said to be equal if it 
	 * exists less than maxUlps numbers representable in the IEEE 754 format between A and B.
	 *
	 * There is not direct relation between maxUlps and the relative error abs((A-B)/A)
	 * but for example taking maxUlps=4*2048 will ensure that relative error is less than 10e-12
	 *  
	 *  
	 * @param A
	 * @param B
	 * @param maxUlps
	 * @return relative double equality
	 */
	public static boolean relativeDoubleArrayEquality(double [] A, double [] B, long maxUlps)

	{
		if(A.length!=B.length)
			return false;
		for(int i=0;i<A.length;i++)
			if(!relativeDoubleEquality(A[i], B[i], maxUlps))
				return false;
		return true;
	
	}
	
	/**
	 * Robust floating point comparison, using integer representation. 
	 * Compared to Double.compare this function may say that two floating point numbers a 
	 * and are equal even if a==b is false.
	 * It is based on function "relativeDoubleEquality".
	 * 
	 * Return : 0 if relativeDoubleEquality(a,b)==true
	 * 			-1 if relativeDoubleEquality(a,b)==false and a<b
	 * 			1 otherwise
	 * @param a
	 * @param b
	 * @return Floating point comparison
	 */
	public static int relativeDoubleCompare(double a, double b)
	{
		int res;
		if(relativeDoubleEquality(a, b))
			res=0;
		else if(a<b)
			res=-1;
		else if(a>b)
			res=1;
		else {
			res=0;
			System.out.println("Very strange!!!");
		}
		return res;
	}
	
	/**
	 * Robust floating point comparison, using integer representation. 
	 * Compared to Double.compare this function may say that two floating point numbers a 
	 * and are equal even if a==b is false. 
	 * It is based on function "relativeDoubleEquality".
	 * 
	 * Return : 0 if relativeDoubleEquality(a,b)==true
	 * 			-1 if relativeDoubleEquality(a,b)==false and a<b
	 * 			1 otherwise
	 * @param a
	 * @param b
	 * @return Floating point comparison
	 */
	public static int relativeDoubleCompare(double a, double b, long maxUlps)
	{
		int res;
		if(relativeDoubleEquality(a, b,maxUlps))
			res=0;
		else if(a<b)
			res=-1;
		else if(a>b)
			res=1;
		else {
			res=0;
			System.out.println("Very strange!!!");
		}
		return res;
	}
	
	/**
	 * Compute relative difference of a and b defined as |a-b|/max(a,b) 
	 * It is assumed that a or b is different from 0, if this condition is not verified result will be 
	 * NaN is both a and b are equal to 0, +Infinity otherwise
	 * @param a a double number
	 * @param b another double number
	 * @return relative difference of a and b
	 */
	public static double relativeDifference(double a, double b)
	{
		return Math.abs(a-b)/Math.max(a, b);
	}
	
	/**
	 * Convert an angle expressed in degree to radian
	 * @param deg value in degree
	 * @return equivalent value in radian
	 */
	public static double degToRadian(double deg)
	{
		return Math.PI*deg/180.0;
	}
	
	/**
	 * Convert an angle expressed in radian to degree 
	 * @param deg value in radian
	 * @return equivalent value in degree
	 */
	public static double radToDegree(double deg)
	{
		return 180.0*deg/Math.PI;
	}
	
	/**
	 * For plotting angles in an XY chart. It considers that angles are given modulo pi and tries to produce an equivalent angle array minimizing derivative.
	 * 
	 * @param thetas list of angles given modulo pi
	 * @return a plottable equivalent version of input array
	 */
	public static double [] lineariseAnglesPIModulus(double [] thetas)
	{
		int l=thetas.length;
		
		if(0<l)
		{
			double v0=thetas[0];
			for(int i=1;i<l;i++)
			{
				
				double min=v0-piD2;
				double max=v0+piD2;
				double v=thetas[i];
				//System.out.print("v0 " + v0 + " min " + min + " max "+max +" v " +v);
				if(v < min)
				{
					double k=Math.ceil((min-v)/piD2);
					v+=k*piD2;
				}else if(max < v){
					double k=Math.ceil((v-max)/piD2);
					v-=k*piD2;
				}
				//System.out.println(" =\\ v " +v);
				thetas[i]=v;
				v0=v;
			}
		}
		
		return thetas;
	}
	
	/**
	 * Compute bilinear interpolation of given point in given image.
	 * If x coordinate is out  of image, x is set to the closest point of image, the interpolation is then just linear in y (idem for y)
	 * 
	 * @param im input image
	 * @param x x coordinate of point to interpolate
	 * @param y y coordinate of point to interpolate
	 * @param b computation is done in band b
	 * @return bilinear interpolation at given coordinates
	 */
	public static double getBilinearInterpolation(Image im, double x, double y,int b)
	{
		return getBilinearInterpolation(im, x, y, 0, 0, b);
	}
	
	
	/**
	 * Compute bilinear interpolation of given point in given image.
	 * If x coordinate is out  of image, x is set to the closest point of image, the interpolation is then just linear in y (idem for y)
	 * 
	 * @param im input image
	 * @param x x coordinate of point to interpolate
	 * @param y y coordinate of point to interpolate
	 * @param z computation is done in dim z
	 * @param t computation is done in frame t
	 * @param b computation is done in band b
	 * @return bilinear interpolation at given coordinates
	 */
	public static double getBilinearInterpolation(Image im, double x, double y,int z, int t,int b)
	{
		double res;
		double x1,x2,y1,y2;
		double fx1,fx2,fy1,fy2;
		double v11,v12,v21,v22;
		if(x<0)
		{
			x=x2=x1=0;
			fx1=x1-1;
			fx2=x;
		}else if(im.xdim-1<=x){
			x=x2=x1=im.xdim-1;
			fx1=x1-1;
			fx2=x;
		}else{
			fx1=x1=Math.floor(x);
			fx2=x2=x1+1;
		}
		if(y<0)
		{
			y=y2=y1=0;
			fy1=y1-1;
			fy2=y;
		}else if(im.ydim-1<=y){
			y=y2=y1=im.ydim-1;
			fy1=y1-1;
			fy2=y;
		}else{
			fy1=y1=Math.floor(y);
			fy2=y2=y1+1;
			
		}
		
		int xx1=(int)x1;
		int xx2=(int)x2;
		int yy1=(int)y1;
		int yy2=(int)y2;
		v11=im.getPixelDouble(xx1, yy1, z, t, b);
		v21=im.getPixelDouble(xx2, yy1, z, t, b);
		v12=im.getPixelDouble(xx1, yy2, z, t, b);
		v22=im.getPixelDouble(xx2, yy2, z, t, b);
		res=v11*(fx2-x)*(fy2-y)+v21*(x-fx1)*(fy2-y)+v12*(y-fy1)*(fx2-x)+v22*(y-fy1)*(x-fx1);
		return res;
	}
	
	/**
	 * Test if one of the pixel used by bilinear interpolation is masked
	 * @param im input image
	 * @param x x coordinate of point to interpolate
	 * @param y y coordinate of point to interpolate
	 * @return true if one the pixel used is masked
	 */
	public static boolean isBilinearInterpolationMasked(Image im, double x, double y,int b)
	{
		return isBilinearInterpolationMasked(im, x, y, 0, 0, b);
	}
	
	/**
	 * Test if one of the pixel used by bilinear interpolation is masked
	 * @param im input image
	 * @param x x coordinate of point to interpolate
	 * @param y y coordinate of point to interpolate
	 * @param z computation is done in dim z
	 * @param t computation is done in frame t
	 * @param b computation is done in band b
	 * @return true if one the pixel used is masked
	 */
	public static boolean isBilinearInterpolationMasked(Image im, double x, double y,int z, int t,int b)
	{
		double x1, x2, y1, y2;
		if (x < 0) {
			x = x2 = x1 = 0;
		} else if (im.xdim - 1 <= x) {
			x = x2 = x1 = im.xdim - 1;
		} else {
			x1 = Math.floor(x);
			x2 = x1 + 1;
		}
		
		if (y < 0) {
			y = y2 = y1 = 0;
		} else if (im.ydim - 1 <= y) {
			y = y2 = y1 = im.ydim - 1;
		} else {
			y1 = Math.floor(y);
			y2 = y1 + 1;
		}
		
		int xx1=(int)x1;
		int xx2=(int)x2;
		int yy1=(int)y1;
		int yy2=(int)y2;
		return !im.isPresent(xx1, yy1, z, t, b) || !im.isPresent(xx2, yy1, z, t, b) ||!im.isPresent(xx1, yy2, z, t, b) || !im.isPresent(xx2, yy2, z, t, b)  ;
	}
	
	/**
	 * 1D cubic Hermit spline interpolation, approximation of Catmull–Rom definition.
	 * Data are equally spaced on points -1 0 1 2, and interpolation is valid for x between 0 and 1
	 * @param x interpolation point between 0 and 1
	 * @param v1 value at point -1
	 * @param v2 value at point 0
	 * @param v3 value at point 1
	 * @param v4 value at point 2
	 * @return interpolation at point x
	 */
	public static double getCubicInterpolation1D(double x, double v1, double v2, double v3, double v4) throws InvalidParameterException
	{
		if(x<0.0 || 1.0 < x)
			throw new InvalidParameterException("Cubic interpolation is only valid between 0.0 and 1.0");
		double x2=x*x;
		double t1=x*((2.0-x)*x-1.0);
		double t2=x2*(3.0*x-5.0)+2.0;
		double t3=x*((4.0-3.0*x)*x+1.0);
		double t4=(x-1.0)*x2;
		return 0.5*(t1*v1+t2*v2+t3*v3+t4*v4);
	}
	
	/**
	 * 2D cubic Hermit spline interpolation over XY plane, approximation of Catmull–Rom definition.
	 * Computation is done in two times : first  1D cubic interpolation over y dim
	 * then 1D cubic interpolation over interpolated points at previous step. So each point is determined
	 * by the 16 surrounding points. 
	 * 
	 * @param im input image
	 * @param x x coordinate of interpolation point (between 0 and im.xdim-1)
	 * @param y y coordinate of interpolation point (between 0 and im.ydim-1)
	 * @param z z coordinate (no interpolation)
	 * @param t t coordinate (no interpolation)
	 * @param b b coordinate (no interpolation)
	 * @return bicubic interpolation of xy plane at point (x,y)
	 * @throws InvalidParameterException specified interpolation point is not in image domain
	 */
	public static double getBiCubicInterpolation(Image im, double x, double y, int z, int t,  int b) throws InvalidParameterException
	{
		if(x<0.0 || im.xdim-1 < x || y<0.0 || im.ydim-1 < y)
			throw new InvalidParameterException("BiCubic interpolation only defined for points in image domain.");
		
		int x1,x2,x3,x4;
		int y1,y2,y3,y4;
		
		x2=(int)Math.floor(x);
		x1=Math.max(0, x2-1);
		x3=Math.min(x2+1, im.xdim-1);
		x4=Math.min(x3+1, im.xdim-1);
		
		x-=x2;
		
		y2=(int)Math.floor(y);
		y1=Math.max(0, y2-1);
		y3=Math.min(y2+1, im.ydim-1);
		y4=Math.min(y3+1, im.ydim-1);
		
		y-=y2;
		
		double vt1,vt2,vt3,vt4;
		double vf1,vf2,vf3,vf4;
		vt1=im.getPixelDouble(x1, y1, z, t, b);
		vt2=im.getPixelDouble(x2, y1, z, t, b);
		vt3=im.getPixelDouble(x3, y1, z, t, b);
		vt4=im.getPixelDouble(x4, y1, z, t, b);
		vf1=getCubicInterpolation1D(x, vt1, vt2, vt3, vt4);
		
		vt1=im.getPixelDouble(x1, y2, z, t, b);
		vt2=im.getPixelDouble(x2, y2, z, t, b);
		vt3=im.getPixelDouble(x3, y2, z, t, b);
		vt4=im.getPixelDouble(x4, y2, z, t, b);
		vf2=getCubicInterpolation1D(x, vt1, vt2, vt3, vt4);
		
		vt1=im.getPixelDouble(x1, y3, z, t, b);
		vt2=im.getPixelDouble(x2, y3, z, t, b);
		vt3=im.getPixelDouble(x3, y3, z, t, b);
		vt4=im.getPixelDouble(x4, y3, z, t, b);
		vf3=getCubicInterpolation1D(x, vt1, vt2, vt3, vt4);
		
		vt1=im.getPixelDouble(x1, y4, z, t, b);
		vt2=im.getPixelDouble(x2, y4, z, t, b);
		vt3=im.getPixelDouble(x3, y4, z, t, b);
		vt4=im.getPixelDouble(x4, y4, z, t, b);
		vf4=getCubicInterpolation1D(x, vt1, vt2, vt3, vt4);
		
		return getCubicInterpolation1D(y, vf1, vf2, vf3, vf4);
	}
	
	/**
	 * Round a decimal number to the given precision (number of numbers after point)
	 * @param a A number to round
	 * @param dec Numbers of figures after decimal
	 * @return Truncated number 
	 */
	public static double round(double a, int dec)
	{
		double k=Math.pow(10.0, dec);
		return Math.round(a*k)/k;
	}
	
	/**
	 * The true modulo function (the % operator can return negative results... a negative remainder in an Euclidean division...)
	 * Return r in the system : a = k * b + r
	 * where k and r are integer and 0<=r<b 
	 * 
	 * @param a 
	 * @param b
	 * @return a modulo b, remainder of a divided by b in a generalized Euclidean division (to real number line)
	 */
	public static double modulo(double a, double b)
	{
		double m=a%b;
		if(m<0.0)
			m=Math.abs(b)+m;
		return m;
	}
	
	/**
	 * The divider of the generalized Euclidean division (to real number) of a by b
	 * Return k in the system : a = k * b + r
	 * where k and r are integer and 0<=r<b 
	 * 
	 * @param a denominator
	 * @param b divider
	 * @return 
	 */
	public static double euclidDiv(double a, double b)
	{
		double k=Double.NaN;
		if((a>=0.0 && b >0.0) || (a<=0.0 && b>0.0))
			k=Math.floor(a/b);
		else if(a<=0.0 && b <0)
			k=-Math.floor(a/-b);
		else if(a>=0 && b <0)
			k=-Math.floor(a/-b);
		return k;
	}
	
	
	/**
	 * computes the average angular value of two hue values
	 * by taking into account their periodicity.
	 * 
	 * @param h1
	 * @param h2
	 * @return the average hue
	 */
	public static double hueAverage(double h1,double h2)
	{
		double diff = Math.abs(h1 - h2);
		
		if(diff <= 0.5) return (h1 + h2)/2.0;
		else return hueAddition((h1 + h2)/2.0,0.5);
	}
	
	/**
	 * computes the hanbury defined distance between two hues 
	 * 
	 * @param h1
	 * @param h2
	 * @return the distance \in [0,0.5]
	 */
	public static double hueDistance(double h1,double h2)
	{
		double diff = Math.abs(h1 - h2);
		
		if(diff <= 0.5) return diff;
		else return 1.0 - diff;
	}
	
	/**
	 * perceptual hue distance.. 
	 * 
	 * @param h1
	 * @param h2
	 * @return the distance \in [0,0.5]
	 */
	public static double perceptualHueDistance(double h1,double h2)
	{
		double dist = 2 * hueDistance(h1,h2);
		
		if (dist > 0.5) dist = 0.5;
		
		return dist;
	}
	
	/**
	 * 
	 * @param histo
	 * @return the optimum threshold
	 */
	public static int optimumThreshold(double[] histo)
	{
		double[] variances = new double[histo.length-1];
		
		for(int i = 1; i < histo.length; i++){
			
			// number of pixels in each class
			double n1 = 0.0;
			double n2 = 0.0;
			
			// mean of each class
			double mean1 = 0.0;
			double mean2 = 0.0;

			for(int j = 0; j < i; j++){
				n1 += histo[j];
				mean1 += histo[j] * j;
			}
			
			for(int j = i; j < histo.length; j++){
				n2 += histo[j];
				mean2 += histo[j] * j;
			}
			
			if(n1 == 0 || n2 == 0) continue;
			
			mean1 = mean1 / n1;
			mean2 = mean2 / n2;
			
			// inner class variance..the official and the simplified versions
			variances[i - 1] = n1 * n2 * (mean1 - mean2) * (mean1 - mean2);
		}
		
		// get the maximum
		int max = 0;
		for(int i = 1; i < variances.length; i++){
			if(variances[i] > variances[max]) max = i;
		}
		
		return max+1;
	}
	
	/**
	 * Computes the minimal distance of a hue from multiple reference hues 
	 * 
	 * @param h1
	 * @param v
	 * @return the distance \in [0,0.5]
	 */
	public static double multipleHueDistance(double h1,Vector<double[]> v)
	{
		double distance = 1.0;
		
		for(int i = 0; i < v.size(); i++){
			double[] tmp = (double[])v.get(i);
			
			double diff = Math.abs(h1 - tmp[0]);

			if(diff > 0.5) diff = 1.0 - diff;
			
			if(diff < distance) distance = diff;
		}
		
		return distance;
	}
	
	/**
	 * computes the sum of two hues \in [0,1]
	 * 
	 * @param h1
	 * @param h2
	 * @return the sum of two taking into consideration their periodicity
	 */
	public static double hueAddition(double h1,double h2)
	{
		if(h1 + h2 <= 1.0) return h1 + h2;
		else return (h1 + h2) - 1.0;
	}
	
	/**
	 * computes the difference of two hues \in [0,1]
	 * 
	 * @param h1
	 * @param h2
	 * @return the sum of two taking into consideration their periodicity
	 */
	public static double hueDifference(double h1,double h2)
	{
		if(h1 - h2 >= 0.0) return h1 - h2;
		else return 1.0 + h1 - h2;
	}
	
	/**
	 * Compute the standard complement c for a pixel of a DoubleImage of value a:
	 * c = 1.0 - a;
	 * 
	 * @param a pixel value
	 * 
	 * @return natural complement for double image
	 */
	public static double standardComplement(double a)
	{
		return 1.0 - a;
	}
	
	/**
	 * Compute the standard complement c for a pixel of a IntegerImage of value a:
	 * c = - a;
	 * 
	 * @param a pixel value
	 * 
	 * @return natural complement for integer image
	 */
	public static int standardComplement(int a)
	{
		return - a;
	}
	
	/**
	 * Compute the standard complement c for a pixel of a ByteImage of value a:
	 * c = 255 - a;
	 * 
	 * @param a pixel value
	 * 
	 * @return natural complement for byte image
	 */
	public static byte standardComplement(byte a)
	{
		return  (byte)(((byte)255) - a);
	}
	
	/**
	 * Compute the standard complement c for a pixel of a BooleanImage of value a:
	 * c = !a;
	 * 
	 * @param a pixel value
	 * 
	 * @return natural complement for boolean image
	 */
	public static boolean standardComplement(boolean a)
	{
		return  !a;
	}
	
	/**
	 * Return the smallest integer that is greater or equal to argument and equal to a power of 2.
	 * 
	 * @param x number
	 * @return smallest integer power of 2 greater or equal to x
	 */
	public static int ceilP2(int x)
	{
		if(x<=0) return 0;
		int k=(int)Math.ceil(Math.log(x)/Math.log(2));
		return (int)Math.pow(2.0, k);
	}



	/**	Returns an image of type {@link java.awt.image.BufferedImage} "equal" to one of type 
	 *	{@link fr.unistra.pelican.Image}.
	 *	@param inputImage The image to convert.
	 *	@return A {@link java.awt.image.BufferedImage} "equal" to inputImage.
	 */
	public static BufferedImage pelican2Buffered( fr.unistra.pelican.Image inputImage ) { 

		if ( inputImage.getBDim() != 3 ) inputImage = 
			fr.unistra.pelican.algorithms.conversion.GrayToRGB.exec( 
			fr.unistra.pelican.algorithms.conversion.AverageChannels.exec( inputImage ) );

		int[] bandOffsets = { 0, 1, 2 };

		ByteImage tmp = (ByteImage) inputImage;
		if ( tmp.getZDim() > 1 ) tmp = (ByteImage) tmp.getImage4D( 0, fr.unistra.pelican.Image.Z );
		if ( tmp.getTDim() > 1 ) tmp = (ByteImage) tmp.getImage4D( 0, fr.unistra.pelican.Image.T );
		int size = tmp.size();
		byte[] tmp2 = new byte[ size ];
		for ( int i = 0 ; i < size ; i++ ) tmp2[i] = ( byte ) tmp.getPixelByte(i);
		DataBufferByte dbb = new DataBufferByte( tmp2,tmp.size() );
		SampleModel s = RasterFactory.createPixelInterleavedSampleModel(
						DataBuffer.TYPE_BYTE, 
						tmp.getXDim(), tmp.getYDim(), 3, 
						3 * tmp.getXDim(), bandOffsets );
		Raster r = RasterFactory.createWritableRaster( s, dbb, new java.awt.Point(0,0) );
		BufferedImage res = new BufferedImage(	tmp.getXDim(), 
												tmp.getYDim(), 
												BufferedImage.TYPE_3BYTE_BGR );
		res.setData(r);

		return res;
	}
	
	/**	Returns an image of type {@link java.awt.image.BufferedImage} "equal" to one of type 
	 *	{@link fr.unistra.pelican.Image}.
	 *	@param inputImage The image to convert can be in 5D.
	 *	@param t temporal index to use
	 *	@return A {@link java.awt.image.BufferedImage} "equal" to inputImage.
	 */
	public static BufferedImage pelican2BufferedT( fr.unistra.pelican.Image inputImage, int t ) { 

		if ( inputImage.getBDim() != 3) inputImage = 
			fr.unistra.pelican.algorithms.conversion.GrayToRGB.exec( 
			fr.unistra.pelican.algorithms.conversion.AverageChannels.exec( inputImage ) );

		int[] bandOffsets = { 0, 1, 2 };

		int xDim = inputImage.getXDim();
		int yDim = inputImage.getYDim();
		
		int size = xDim*yDim*3;
		int index = inputImage.getLinearIndexXY_T_(0, 0, t);
		byte[] tmp2 = new byte[ size ];
		for ( int i = 0 ; i < size ; i++ ) 
			tmp2[i] = ( byte ) inputImage.getPixelByte(index++);
			
		DataBufferByte dbb = new DataBufferByte( tmp2,size );
		SampleModel s = RasterFactory.createPixelInterleavedSampleModel(
						DataBuffer.TYPE_BYTE, 
						xDim, yDim, 3, 
						3 * xDim, bandOffsets );
		Raster r = RasterFactory.createWritableRaster( s, dbb, new java.awt.Point(0,0) );
		BufferedImage res = new BufferedImage(	xDim, 
												yDim, 
												BufferedImage.TYPE_3BYTE_BGR );
		res.setData(r);

		return res;
	}

	/**	Returns an image of type {@link java.awt.image.BufferedImage} "equal" to one of type 
	 *	{@link fr.unistra.pelican.Image}.
	 *	@param inputImage The image to convert can be in 5D.
	 *	@param z depth index to use
	 *	@return A {@link java.awt.image.BufferedImage} "equal" to inputImage.
	 */
	public static BufferedImage pelican2BufferedZ( fr.unistra.pelican.Image inputImage, int z ) { 

		if ( inputImage.getBDim() != 3) inputImage = 
			fr.unistra.pelican.algorithms.conversion.GrayToRGB.exec( 
			fr.unistra.pelican.algorithms.conversion.AverageChannels.exec( inputImage ) );

		int[] bandOffsets = { 0, 1, 2 };

		int xDim = inputImage.getXDim();
		int yDim = inputImage.getYDim();
		
		int size = xDim*yDim*3;
		int index = inputImage.getLinearIndexXYZ__(0, 0, z);
		byte[] tmp2 = new byte[ size ];
		for ( int i = 0 ; i < size ; i++ ) 
			tmp2[i] = ( byte ) inputImage.getPixelByte(index++);
			
		DataBufferByte dbb = new DataBufferByte( tmp2,size );
		SampleModel s = RasterFactory.createPixelInterleavedSampleModel(
						DataBuffer.TYPE_BYTE, 
						xDim, yDim, 3, 
						3 * xDim, bandOffsets );
		Raster r = RasterFactory.createWritableRaster( s, dbb, new java.awt.Point(0,0) );
		BufferedImage res = new BufferedImage(	xDim, 
												yDim, 
												BufferedImage.TYPE_3BYTE_BGR );
		res.setData(r);

		return res;
	}

	/**	Creates an empty TiledImage without an alpha channel
	 *	@param markers 
	 *	@param width
	 *	@param height
	 *	@return the TiledImage object
	 */
	public static TiledImage createGrayImage( ByteImage markers, int width, int height ) { 

		if ( markers != null ) 
			markers = AverageChannels.exec( markers );

		byte[] imageData = new byte[ width*height ];
		int count = 0;
		if ( markers != null ) 
			for ( int h = 0 ; h < height ; h++ ) 
				for ( int w = 0 ; w < width ; w++ ) 				
					imageData[ count++ ] = ( byte ) markers.getPixelXYByte( w,h );
		else
			// Fill with zeros. For testing, fill with something else ...
			for ( int w = 0 ; w < width ; w++ ) 
				for ( int h = 0 ; h < height ; h++ ) 
					imageData[ count++ ] = 0;
		DataBufferByte dbuffer = new DataBufferByte( imageData, width*height );
		SampleModel sampleModel = 
			RasterFactory.createBandedSampleModel( DataBuffer.TYPE_BYTE, width,height,1 );
		ColorModel colorModel = PlanarImage.createColorModel(sampleModel);
		Raster raster = RasterFactory.createWritableRaster( sampleModel,dbuffer,new Point(0,0) );

		TiledImage tiledImage = new TiledImage( 0,0, width,height, 0,0, sampleModel,colorModel );
		tiledImage.setData( raster );
		return tiledImage;
	}
	
	public static TiledImage createGrayImage( IntegerImage markers, int width, int height ) { 

		if ( markers != null ) 
			markers = AverageChannels.exec( markers );

		byte[] imageData = new byte[ width*height ];
		int count = 0;
		if ( markers != null ) 
			for ( int h = 0 ; h < height ; h++ ) 
				for ( int w = 0 ; w < width ; w++ ) 				
					imageData[ count++ ] = ( byte ) markers.getPixelXYInt( w,h );
		else
			// Fill with zeros. For testing, fill with something else ...
			for ( int w = 0 ; w < width ; w++ ) 
				for ( int h = 0 ; h < height ; h++ ) 
					imageData[ count++ ] = 0;
		DataBufferByte dbuffer = new DataBufferByte( imageData, width*height );
		SampleModel sampleModel = 
			RasterFactory.createBandedSampleModel( DataBuffer.TYPE_BYTE, width,height,1 );
		ColorModel colorModel = PlanarImage.createColorModel(sampleModel);
		Raster raster = RasterFactory.createWritableRaster( sampleModel,dbuffer,new Point(0,0) );

		TiledImage tiledImage = new TiledImage( 0,0, width,height, 0,0, sampleModel,colorModel );
		tiledImage.setData( raster );
		return tiledImage;
	}



	/**	(Used by SURF descriptor and his friends) 
	 *	@param d integer to round
	 *	@return rounding of <tt>d</tt> 
	 */
	public static int cvround( double d ) { 

		long res = Math.round( d );
		Long resL = new Long( res );
		return resL.intValue();
	}


	public static double mean(double [] a, int start, int end){
		double v=0.0;
		int nb=end-start;
		for(int i=start;i<end;i++)
			v+=a[i];
		return v/(double)nb;
	}
	
	public static double mean(double [] a){
		return mean(a,0,a.length);
	}
	
	public static double median(double ... a){
		Arrays.sort(a);
		return a[a.length/2];
	}
	
	public static long mean(long [] a, int start, int end){
		int v=0;
		int nb=end-start;
		for(int i=start;i<end;i++)
			v+=a[i];
		return v/nb;
	}
	
	public static long mean(long [] a){
		return mean(a,0,a.length);
	}
	
	public static double max(double [] a, int start, int end){
		double max=Double.NEGATIVE_INFINITY;
		
		for(int i=start;i<end;i++)
			if(a[i]>max)
				max=a[i];
		return max;
	}
	
	public static double variance(double[] a)
	{
		return variance(a,0,a.length);
	}
	
	public static double variance(double[] a, int start, int stop)
	{
		double mean = Tools.mean(a,start,stop);
		double variance = 0;
		int nb=stop-start;
		for(int i=start;i<stop;i++)
		{
			variance+=(a[i]-mean)*(a[i]-mean);
		}
		variance/=(double)nb;
		return variance;
	}
	
	public static double standardDeviation(double[] a)
	{
		return Math.sqrt(Tools.variance(a));
	}

	public static long variance(long[] a)
	{
		long mean = Tools.mean(a);
		long variance = 0;
		for(int i=0;i<a.length;i++)
		{
			variance+=(a[i]-mean)*(a[i]-mean);
		}
		variance/=a.length;
		return variance;
	}
	
	public static long standardDeviation(long[] a)
	{
		return Math.round(Math.sqrt(Tools.variance(a)));
	}
	
	
	public static double max(double ... a){
		return max(a,0,a.length);
	}
	
	public static int imax(double [] a, int start, int end){
		double max=Double.NEGATIVE_INFINITY;
		int imax=-1;
		for(int i=start;i<end;i++)
			if(a[i]>max)
			{
				max=a[i];
				imax=i;
			}
		return imax;
	}
	

	
	public static int imax(double [] a){
		return imax(a,0,a.length);
	}
	
	public static double min(double ... a){
		return min(a,0,a.length);
	}
	
	public static double min(double [] a, int start, int end){
		double min=Double.POSITIVE_INFINITY;
		
		for(int i=start;i<end;i++)
			if(a[i]<min)
				min=a[i];
		return min;
	}
	

	
	public static int imin(double [] a, int start, int end){
		double min=Double.NEGATIVE_INFINITY;
		int imin=-1;
		for(int i=start;i<end;i++)
			if(a[i]<min)
			{
				min=a[i];
				imin=i;
			}
		return imin;
	}
	
	public static int imin(double [] a){
		return imin(a,0,a.length);
	}
	
	public static double sum(double [] a){
		double v=0;
		for(int i=0;i<a.length;i++)
			v+=a[i];
		return v;
	}
	
	public static double [] removeZeros(double [] a)
	{
		int nb=0;
		for(double d:a)
			if(d!=0.0)
				nb++;
		double [] res=new double[nb];
		nb=0;
		for(double d:a)
			if(d!=0.0)
				res[nb++]=d;
		return res;
	}
	
	public static double [] abs(double [] a)
	{
		double [] res=a.clone();
		for(int i=0;i<res.length;i++)
			if(res[i]<0)
				res[i]=-res[i];
		return res;
	}
	
	public static double [] log(double [] a)
	{
		double [] res=new double[a.length];
		for(int i=0;i<res.length;i++)
				res[i]=Math.log(a[i]);
		return res;
	}
	
	
	public static double histogramDistance( Double[] p1, Double[] p2 ) { 

		if ( p1.length != p2.length ) return -1.;
		int size = p1.length;
		double[] v1 = new double[ size ];
		double[] v2 = new double[ size ];
		for ( int i = 0 ; i < size ; i++ ) { 

			v1[i] = p1[i].doubleValue();
			v2[i] = p2[i].doubleValue();
		}
		return histogramDistance( v1,v2 );
	}
	public static double pyramidMatchDistance( Double[] p1, Double[] p2, int a, int b ) { 

		if ( p1.length != p2.length ) return -1.;
		int size = p1.length;
		double[] v1 = new double[ size ];
		double[] v2 = new double[ size ];
		for ( int i = 0 ; i < size ; i++ ) { 

			v1[i] = p1[i].doubleValue();
			v2[i] = p2[i].doubleValue();
		}
		return pyramidMatchDistance( v1,v2, a,b );
	}
	public static double correlogramDistance( Double[] p1, Double[] p2 ) { 

		if ( p1.length != p2.length ) return -1.;
		int size = p1.length;
		double[] v1 = new double[ size ];
		double[] v2 = new double[ size ];
		for ( int i = 0 ; i < size ; i++ ) { 

			v1[i] = p1[i].doubleValue();
			v2[i] = p2[i].doubleValue();
		}
		return correlogramDistance( v1,v2 );
	}
	public static double euclideanDistance( Double[] p1, Double[] p2 ) { 

		if ( p1.length != p2.length ) return -1.;
		int size = p1.length;
		double[] v1 = new double[ size ];
		double[] v2 = new double[ size ];
		for ( int i = 0 ; i < size ; i++ ) { 

			v1[i] = p1[i].doubleValue();
			v2[i] = p2[i].doubleValue();
		}
		return euclideanDistance( v1,v2 );
	}


	public static double histogramIntersection(double[] p1,double[] p2)
	{
		double dist = 0.0;
		
		if(p1.length != p2.length){
			System.err.println("Histogram Distance: Incompatible histogram bin numbers");
			return -1.0;
		}
		
		for(int i = 0; i < p1.length; i++){
			dist += Math.min(p1[i],p2[i]);
		}
		
		return 1.0 - dist;
	}
	
	public static double LSHDistance(double[] p1,double[] p2)
	{	return HSLDistance(new double[]{p1[2],p1[1],p1[0]},new double[]{p2[2],p2[1],p2[0]});
	}
	
	/**
	 * Return the amount of memory (in Mo) currently used by the running program
	 * @return Memory used in Mo
	 */
	public static double getMemoryUsed()
	{
		Runtime runtime = Runtime.getRuntime();
		return (runtime.totalMemory()-runtime.freeMemory())/(1048576.);  // 1048576 = 1024 * 1024
	}
	
	public static double [] toDoubleArray(Object [] a)
	{
		double [] res= new double[a.length];
		for(int i=0;i<res.length;i++)
		{
			res[i]=((Number)a[i]).doubleValue();
		}
		return res;
	}
	
	public static int [] toIntArray(Object [] a)
	{
		int [] res= new int[a.length];
		for(int i=0;i<res.length;i++)
		{
			res[i]=((Number)a[i]).intValue();
		}
		return res;
	}
	
	public static byte [] toByteArray(Object [] a)
	{
		byte [] res= new byte[a.length];
		for(int i=0;i<res.length;i++)
		{
			res[i]=((Number)a[i]).byteValue();
		}
		return res;
	}
	
	public static short [] toShortArray(Object [] a)
	{
		short [] res= new short[a.length];
		for(int i=0;i<res.length;i++)
		{
			res[i]=((Number)a[i]).shortValue();
		}
		return res;
	}
	
	public static boolean [] toBooleanArray(Object [] a)
	{
		boolean [] res= new boolean[a.length];
		for(int i=0;i<res.length;i++)
		{
			res[i]=((Boolean)a[i]);
		}
		return res;
	}

}
