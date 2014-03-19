package fr.unistra.pelican.util;

import java.awt.Point;

import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;

/**
 * Some useful math function working on Pelican images, all calculus are done in double precision.
 * If the function name ends by F the result is put in a new Image of the same type as the first parameter.
 * In the other case, calculus are done INPLACE in the first argument.
 * 
 * @author Benjamin Perret
 *
 */
public abstract class IMath {

	private static SortedList<Double> sl=new SortedList<Double>();;
	
	public static <T extends Image> T  logF(T im)
	{
		T res=(T)im.copyImage(false);
		int si=im.size();
			for(int i=0;i<si;i++)
				if(im.isPresent(i))
						res.setPixelDouble(i,Math.log(im.getPixelDouble(i)));
		return res;
	}
	
	public static <T extends Image> T  log(T im)
	{
		int si=im.size();
		for(int i=0;i<si;i++)
			if(im.isPresent(i))
				im.setPixelDouble(i,Math.log(im.getPixelDouble(i)));
		return im;
	}
	
	public static <T extends Image> T  log10F(T im)
	{
		T res=(T)im.copyImage(false);
		int si=im.size();
			for(int i=0;i<si;i++)
				if(im.isPresent(i))
						res.setPixelDouble(i,Math.log10(im.getPixelDouble(i)));
		return res;
	}
	
	public static <T extends Image> T  log10(T im)
	{
		int si=im.size();
		for(int i=0;i<si;i++)
			if(im.isPresent(i))
				im.setPixelDouble(i,Math.log10(im.getPixelDouble(i)));
		return im;
	}
	
	public static <T extends Image> T  absF(T im)
	{
		T res=(T)im.copyImage(false);
		int si=im.size();
			for(int i=0;i<si;i++)
				if(im.isPresent(i))
						res.setPixelDouble(i,Math.abs(im.getPixelDouble(i)));
		return res;
	}
	
	public static <T extends Image> T  abs(T im)
	{
		int si=im.size();
		for(int i=0;i<si;i++)
			if(im.isPresent(i))
				im.setPixelDouble(i,Math.abs(im.getPixelDouble(i)));
		return im;
	}
	
	public static <T extends Image> T  square(T im)
	{
		int si=im.size();
		for(int i=0;i<si;i++)
			if(im.isPresent(i))
			{
				double v=im.getPixelDouble(i);
				im.setPixelDouble(i,v*v);
			}
				
		return im;
	}
	
	public static <T extends Image> T  squaref(T im)
	{
		T res=(T)im.copyImage(false);
		int si=im.size();
		for(int i=0;i<si;i++)
			if(im.isPresent(i))
				if(im.isPresent(i))
				{
					double v=im.getPixelDouble(i);
					res.setPixelDouble(i,v*v);
				}
		return res;
	}
	
	public static <T extends Image> T  sqrt(T im)
	{
		int si=im.size();
		for(int i=0;i<si;i++)
			if(im.isPresent(i))
				im.setPixelDouble(i,Math.sqrt(im.getPixelDouble(i)));
		return im;
	}
	
	public static <T extends Image> T  sqrtf(T im)
	{
		T res=(T)im.copyImage(false);
		int si=im.size();
		for(int i=0;i<si;i++)
			if(im.isPresent(i))
				res.setPixelDouble(i,Math.sqrt(im.getPixelDouble(i)));
		return res;
	}
	
	public static double sum(Image im)
	{
		double s=0.0;
		int si=im.size();
		for(int i=0;i<si;i++)
			if(im.isPresent(i))
				s+=im.getPixelDouble(i);
		return s;
	}
	
	public static double sum(Image im, int band)
	{
		double s=0.0;
		int si=im.size();
			for(int i=band;i<si;i+=im.bdim)
					if(im.isPresent(i))
						s+=im.getPixelDouble(i);
		return s;
	}
	
	public static double crossProduct(DoubleImage im1, DoubleImage im2)
	{
		double s=0.0;
		
		int si=im1.size();
		for(int i=0;i<si;i++)
			if(im1.isPresent(i))
				s+=im1.getPixelDouble(i)*im2.getPixelDouble(i);
		return s;
	}
	
	public static double crossProduct(DoubleImage im1, DoubleImage im2, int band)
	{
		double s=0.0;
		int si=im1.size();
			for(int i=band;i<si;i+=im1.bdim)
				if(im1.isPresent(i))
					s+=im1.getPixelDouble(i)*im2.getPixelDouble(i);

		return s;
	}
	
	public static  <T extends Image> T mult(T im, double k )
	{
		
		int si=im.size();
		for(int i=0;i<si;i++)
			if(im.isPresent(i))
				im.setPixelDouble(i,im.getPixelDouble(i)*k);
				
		return im;
	}
	
	public static  <T extends Image> T multf(T im, double k )
	{
		T res = (T)im.copyImage(false);
		int si=im.size();
		for(int i=0;i<si;i++) 
			if(im.isPresent(i))
				res.setPixelDouble(i,im.getPixelDouble(i)*k);
				
		return res;
	}
	
	public static <T extends Image> T mult(T im, double k, int band)
	{
		
		int si=im.size();
		for(int i=band;i<si;i+=im.bdim)
				if(im.isPresent(i))
					im.setPixelDouble(i,im.getPixelDouble(i)*k);
		return im;
	}
	
	public static  <T extends Image> T mult(T im, int k )
	{
		
		int si=im.size();
		for(int i=0;i<si;i++)
			if(im.isPresent(i))
				im.setPixelInt(i,im.getPixelInt(i)*k);
				
		return im;
	}
	

	
	public static <T extends Image> T mult(T im, int k, int band)
	{
		
		int si=im.size();
		for(int i=band;i<si;i+=im.bdim)
				if(im.isPresent(i))
					im.setPixelInt(i,im.getPixelInt(i)*k);
		return im;
	}
	
	public static <T extends Image> T mult(T im1, Image im2)
	{
		for(int i=0;i<im1.size();i++)
				im1.setPixelDouble(i,im1.getPixelDouble(i)*im2.getPixelDouble(i));
		return im1;
	}
	
	public static <T extends Image> T multf(T im1, Image im2)
	{
		T res=(T)im1.copyImage(false);
		for(int i=0;i<im1.size();i++)
				res.setPixelDouble(i,im1.getPixelDouble(i)*im2.getPixelDouble(i));
		return res;
	}
	
	public static <T extends Image> T div(T im1, Image im2)
	{
		for(int i=0;i<im1.size();i++)
				im1.setPixelDouble(i,im1.getPixelDouble(i)/im2.getPixelDouble(i));
		return im1;
	}
	
	public static <T extends Image> T divf(T im1, Image im2)
	{
		T res=(T)im1.copyImage(false);
		for(int i=0;i<im1.size();i++)
				res.setPixelDouble(i,im1.getPixelDouble(i)/im2.getPixelDouble(i));
		return res;
	}
	
	public static<T extends Image> T add(T im1, double val)
	{
		for(int i=0;i<im1.size();i++)
				im1.setPixelDouble(i,im1.getPixelDouble(i)+val);
		return im1;
	}
	
	public static<T extends Image> T addf(T im1, double val)
	{
		T res=(T)im1.copyImage(false);
		for(int i=0;i<im1.size();i++)
				res.setPixelDouble(i,im1.getPixelDouble(i)+val);
		return res;
	}
	
	public static<T extends Image> T add(T im1, int val)
	{
		for(int i=0;i<im1.size();i++)
				im1.setPixelInt(i,im1.getPixelInt(i)+val);
		return im1;
	}
	
	public static <T extends Image>  T add(T im1, Image im2)
	{

		for(int i=0;i<im1.size();i++)
				im1.setPixelDouble(i,im1.getPixelDouble(i)+im2.getPixelDouble(i));
		return im1;
	}
	
	public static <T extends Image>  T addf(T im1, Image im2)
	{
		T res=(T)im1.copyImage(false);
		for(int i=0;i<im1.size();i++)
				res.setPixelDouble(i,im1.getPixelDouble(i)+im2.getPixelDouble(i));
		return res;
	}
	
	public static <T extends Image>  T addM(T im1, Image im2)
	{

		for(int i=0;i<im1.size();i++)
			if(im1.isPresent(i))
				im1.setPixelDouble(i,im1.getPixelDouble(i)+im2.getPixelDouble(i));
		return im1;
	}
	
	public static <T extends Image>  T max(T im1, Image im2)
	{
		for(int i=0;i<im1.size();i++)
				im1.setPixelDouble(i,Math.max(im1.getPixelDouble(i),im2.getPixelDouble(i)));
		return im1;
	}
	
	public static <T extends Image>  T maxf(T im1, Image im2)
	{
		T res = (T)im1.copyImage(false);
		for(int i=0;i<im1.size();i++)
				res.setPixelDouble(i,Math.max(im1.getPixelDouble(i),im2.getPixelDouble(i)));
		return res;
	}
	
	public static <T extends Image>  T maxM(T im1, Image im2)
	{

		for(int i=0;i<im1.size();i++)
			if(im1.isPresent(i))
				im1.setPixelDouble(i,Math.max(im1.getPixelDouble(i),im2.getPixelDouble(i)));
		return im1;
	}
	
	public static <T extends Image> T add(T im1, Image im2,int band)
	{
		/*for(int b=0;b<im1.bdim;b++)
		for(int y=0;y<im1.ydim;y++)
			for(int x=0;x<im1.xdim;x++)
				if(im1.isPresent(x,y,b))*/
		for(int i=band;i<im1.size();i+=im1.bdim)
				im1.setPixelDouble(i,im1.getPixelDouble(i)+im2.getPixelDouble(i));
		return im1;
	}
	
	public static <T extends Image> T diffF(T im1, Image im2)
	{
		T im3=(T)im1.copyImage(false);
		int si=im1.size();
		for(int i=0;i<si;i++)
			if(im1.isPresent(i))
				im3.setPixelDouble(i,im1.getPixelDouble(i) -im2.getPixelDouble(i));
		return im3;
	}
	
	public static <T extends Image> T diff(T im1, Image im2)
	{
		int si=im1.size();
		for(int i=0;i<si;i++)
			if(im1.isPresent(i))
				im1.setPixelDouble(i,im1.getPixelDouble(i) -im2.getPixelDouble(i));
		return im1;
	}
	
	public static <T extends Image> T scaleToZeroOne(T im)
	{
		int si=im.size();
		for(int b=0;b<im.bdim;b++)
		{
			double [] mm=getMinMax(im,b);
			double min=mm[0];
			double diff=mm[1]-mm[0];
			for(int i=b;i<si;i+=im.bdim)
				im.setPixelDouble(i, (im.getPixelDouble(i)-min)/diff);
		}
		return im;
	}
	
	public static <T extends Image> T scaleToZeroOneF(T im)
	{
		int si=im.size();
		T im2=(T)im.copyImage(false);
		for(int b=0;b<im.bdim;b++)
		{
			double [] mm=getMinMax(im,b);
			double min=mm[0];
			double diff=mm[1]-mm[0];
			for(int i=b;i<si;i+=im.bdim)
				im2.setPixelDouble(i, (im.getPixelDouble(i)-min)/diff);
		}
		return im2;
	}
	
	public static double [] getMinMax(Image im)
	{
		int si=im.size();
		double [] res = new double[2];
		res[0]=Double.POSITIVE_INFINITY;
		res[1]=Double.NEGATIVE_INFINITY;
		for(int i=0;i<si;i++)
			if(im.isPresent(i))
			{
				res[0]=Math.min(res[0], im.getPixelDouble(i));
				res[1]=Math.max(res[1], im.getPixelDouble(i));
			}
		return res;
	}
	
	public static double [] getMinMax(Image im, int b)
	{
		int si=im.size();
		double [] res = new double[2];
		res[0]=Double.POSITIVE_INFINITY;
		res[1]=Double.NEGATIVE_INFINITY;
		for(int i=b;i<si;i+=im.bdim)
			if(im.isPresent(i))
			{
				res[0]=Math.min(res[0], im.getPixelDouble(i));
				res[1]=Math.max(res[1], im.getPixelDouble(i));
			}
		return res;
	}
	
	public static double  getMin(Image im, int b)
	{
		int si=im.size();
		double  res = Double.POSITIVE_INFINITY;
		
		for(int i=b;i<si;i+=im.bdim)
			if(im.isPresent(i))
			{
				res=Math.min(res, im.getPixelDouble(i));
			}
		return res;
	}
	
	public static Pixel getMaxPixel(Image im) {
		double max = Double.NEGATIVE_INFINITY;
		Pixel pmax = new Pixel();

		for (Pixel p : im) {
			if (im.isPresent(p)) {
				double v = im.getPixelDouble(p);
				if (v > max)
				{
					max=v;
					pmax.setLocation(p);
				}
			}
		}
		return pmax;

	}
	
	public static double  getMax(Image im, int b)
	{
		int si=im.size();
		double  res = Double.NEGATIVE_INFINITY;
		
		for(int i=b;i<si;i+=im.bdim)
			if(im.isPresent(i))
			{
				res=Math.max(res, im.getPixelDouble(i));
			}
		return res;
	}
	
	public static double  getMax(Image im)
	{
		int si=im.size();
		double  res = Double.NEGATIVE_INFINITY;
		
		for(int i=0;i<si;i++)
			if(im.isPresent(i))
			{
				res=Math.max(res, im.getPixelDouble(i));
			}
		return res;
	}
	
	public static Point getPointMax(DoubleImage im, int b)
	{
		
		double  res = Double.NEGATIVE_INFINITY;
		int i=0,j=0;
		for(int y=0;y<im.ydim;y++)
			for(int x=0;x<im.xdim;x++)
				if(im.isPresentXYB(x, y, b))
			{
					if(res<im.getPixelXYBDouble(x, y, b))
					{
						res=Math.max(res, im.getPixelXYBDouble(x, y, b));
						i=x;
						j=y;
					}
				
			}
		return new Point(i,j);
	}
	
	public static double getMedian(DoubleImage im)
	{
		sl.clear();
		//sl.ensureCapacity(im.size);
		
		for(int b=0;b<im.bdim;b++)
		{
			
			for(int y=0;y<im.ydim;y++)
			{
				
				for(int x=0;x<im.xdim;x++)
					if(im.isPresentXYB(x, y, b))
						sl.add(im.getPixelXYBDouble(x, y, b));
			}
		}
		return sl.get(sl.size()/2);
	}
	
	public static double getMedian(Image im, int band)
	{
		sl.clear();
		sl.ensureCapacity(im.size()/im.bdim);

		
		for(int y=0;y<im.ydim;y++)
		{
			
			for(int x=0;x<im.xdim;x++)
				if(im.isPresentXYB(x, y, band))
					sl.add(im.getPixelXYBDouble(x, y, band));
		}

		return sl.get(sl.size()/2);
	}
	
	/**
	 * Compute mean median and variance at the same time.
	 * @param im
	 * @param band
	 * @return
	 */
	public static double [] getStatistics(Image im, int band)
	{
		//sl.clear();
		//sl.ensureCapacity(im.size()/im.bdim);

		double s1=0.0;
		double s2=0.0;
		
		int nb=0;
		for(int y=0;y<im.ydim;y++)
		{
			
			for(int x=0;x<im.xdim;x++)
				if(im.isPresentXYB(x, y, band))
				{
					double v=im.getPixelXYBDouble(x, y, band);
					//sl.add(v);
					s1+=v;
					s2+=v*v;
					nb++;
				}
		}

		double [] res= new double[3];
		//int nb=sl.size();
		res[0]=(double)s1/(double)nb;
		//res[1]=sl.get(nb/2);
		res[1]=s2/(double)nb - res[0]*res[0];
		//System.out.println("mean " + res[0] + "  med: " + res[1] + "  var: " + res[2] + " nb " + nb);
		return res;
	}
	
	
	/**
	 * Compute mean
	 * @param im
	 * @param band
	 * @return
	 */
	public static double  getMean(Image im, int band)
	{int si=im.size();
		double s=0.0;
		int c=0;
		
		for(int i=band;i<si;i+=im.bdim)
			if(im.isPresent(i))
				{
					s+=im.getPixelDouble(i);
					c++;
				}
		return s/(double)c;
	}
	
	/**
	 * Compute mean
	 * @param im
	 * @param band
	 * @return
	 */
	public static double  getMean(Image im)
	{int si=im.size();
		double s=0.0;
		int c=0;
		
		for(int i=0;i<si;i++)
			if(im.isPresent(i))
				{
					s+=im.getPixelDouble(i);
					c++;
				}
		return s/(double)c;
	}
	
	
	/**
	 * Compute variance
	 * @param im
	 * @param band
	 * @return
	 */
	public static double  getVariance(Image im, int band)
	{int si=im.size();
		double s1=0.0;
		double s2=0.0;
		int c=0;
		
		for(int i=band;i<si;i+=im.bdim)
			if(im.isPresent(i))
				{
					double v=im.getPixelDouble(i);
					s1+=v;
					s2+=v*v;
					c++;
				}
		

		double m=s1/(double)c;
		double res=s2/(double)c - m*m;
		return res;
	}
	
	
	public static String printStatistics(Image im, int band)
	{
		
		String res = "Band " +band+" : " +"\n";
		res+=("Dimensions: " + im.xdim + "*" + im.ydim + " = " + (im.xdim*im.ydim) + " pixels."+"\n");
		double [] mm =getMinMax(im,band);
		double [] stat=getStatistics(im,band);
		res+=("Pixels under mask: " + sl.size() +"."+"\n");
		res+=("Pixel range: [" + mm[0] + ";" + mm[1] +"]."+"\n");
		res+=("Statistics: Mean=" + stat[0] +" Deviation=" + Math.sqrt(stat[1]) + " ."+"\n");
	return res;
	}
	
	public static String printStatistics(Image im)
	{
		String res =("----------------------\nStatistic information on image " + im.getName()+"\n");
		for(int b=0;b<im.bdim;b++)
		{
			res+=printStatistics(im,b);
			res+=("----------------------\n");
		}
		return res;
	}
	
}
