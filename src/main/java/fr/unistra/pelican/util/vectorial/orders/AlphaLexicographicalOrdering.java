package fr.unistra.pelican.util.vectorial.orders;

import java.awt.Point;
import java.util.Arrays;
import java.util.Comparator;

import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.vectorial.VectorPixel;

/**
 * This class represents an alpha-lexicographical ordering scheme for double valued arrays and derivatives.
 * ...very experimental...
 * 
 * @author E.A.
 *
 */

public class AlphaLexicographicalOrdering implements VectorialOrdering,Comparator
{
	private double alpha = 0.0;
	private Image cimg;
	
	public AlphaLexicographicalOrdering(double alpha)
	{
		this.alpha = alpha;
	}
	
	/**
	 * Produces a marginally clustered image 
	 * such as every cluster is comprised of pixels
	 * within an interval of length alpha...
	 * HIGHLY NOT OPTIMIZED..in fact the only way to write it 
	 * worse is to use bubble sort..
	 * @param img the image to cluster
	 * @param flag whether to start clustering from maximal (true) or minima (false) values 
	 * @return clustered image
	 */
	public Image preprocess(Image img,boolean flag)
	{
		cimg = new ByteImage(img);
		
		int bdim = img.getBDim();
		int ydim = img.getXDim();
		int xdim = img.getYDim();
		int size = xdim * ydim;
		
		for(int b = 0; b < bdim - 1; b++){
			// get the channel..
			IndexedPixels[] pixels = new IndexedPixels[size];
			
			// fill the array...so that we can sort it..
			for(int x = 0; x < xdim; x++)
				for(int y = 0; y < ydim; y++)
					pixels[y * xdim + x] = new IndexedPixels(img.getPixelXYBDouble(x,y,b),x,y);
			
			Arrays.sort(pixels);
			
			// it's in ascending order...
			// now the clustering direction depends on the flag
			// for now..only min..meaning we start from the bottom..the smallest..
			// so as the smallest value has a nice group
			int label = 0;
			
			// for every pixel..
			for(int i = 0; i < size; i++){
				// set the current label..
				cimg.setPixelByte(pixels[i].x,pixels[i].y,0,0,b,label);
				
				// get all pixels within the alpha interval
				int j = i+1;
				if(i < size - 1){
					while(j < size && Math.abs(pixels[j].d - pixels[i].d) <= alpha){
						cimg.setPixelByte(pixels[j].x,pixels[j].y,0,0,b,label);
						j++;
					}
				}
				i = j - 1;
				label++;
			}
			System.err.println(label);
		}
		
		cimg.setImage4D(img.getImage4D(bdim - 1,Image.B),bdim - 1,Image.B);
		
		return cimg;
	}
	
	private class IndexedPixels implements Comparable
	{
		double d;
		int x;
		int y;

		IndexedPixels(double d,int x,int y)
		{
			this.d = d; this.x = x; this.y = y;
		}

		public int compareTo(Object o){
			IndexedPixels d = (IndexedPixels)o;

			if(this.d < d.d) return -1;
			else if(this.d > d.d) return 1;
			else return 0;
		}
	}
	
	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#max(double[][])
	 */
	public double[] max(double[][] p)
	{
		double[] max = p[0];

		for(int i = 1; i < p.length; i++){
			if(this.compare(max,p[i]) < 0) max = p[i];
		}

		return max;
	}
	
	public Point max(Point[] p)
	{
		Point max = p[0];

		for(int i = 1; i < p.length; i++){
			if(this.compare(max,p[i]) < 0) max = p[i];
		}

		return max;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#min(double[][])
	 */
	public double[] min(double[][] p)
	{
		double[] min = p[0];

		for(int i = 1; i < p.length; i++){
			if(this.compare(min,p[i]) > 0) min = p[i];
		}

		return min;
	}

	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#rank(double[][], int)
	 */
	public double[] rank(double[][] p,int r)
	{
		Arrays.sort(p,this);

		return p[r];
	}

	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#order(fr.unistra.pelican.util.vectorial.VectorPixel[])
	 */
	public VectorPixel[] order(VectorPixel[] v)
	{
		Arrays.sort(v,this);

		return v;
	}
	
	/**
	 * Compares the given arguments according to this ordering
	 * 
	 * @param o1 first double valued array or vector pixel
	 * @param o2 second double valued array or vector pixel
	 * @return 1,-1 or 0 if o1 is respectively superior, inferior or equal to o2 
	 */
	public int compare(Object o1,Object o2)
	{
		double[] p1 = null,p2 = null;
		Point r1 = null,r2 = null;

		try{
			if(o1.getClass().getName().equals("[D")){
				p1 = (double[])o1;
				p2 = (double[])o2;
			}else if(o1.getClass().getName().equals("fr.unistra.pelican.util.vectorial.VectorPixel")){
				p1 = ((VectorPixel)o1).getVector();
				p2 = ((VectorPixel)o2).getVector();
				
			}else if(o1.getClass().getName().equals("java.awt.Point")){
				r1 = (Point)o1;
				r2 = (Point)o2;
				
			}else throw new ClassCastException();

		}catch(ClassCastException ex){
			ex.printStackTrace();
		}

		if(r1 != null && r2 != null){
			p1 = cimg.getVectorPixelXYZTDouble(r1.x,r1.y,0,0);
			p2 = cimg.getVectorPixelXYZTDouble(r2.x,r2.y,0,0);
		}
			
		for(int i = 0; i < p1.length; i++){
			if(p1[i] < p2[i]) return -1;
			else if(p1[i] > p2[i]) return 1;
		}
			
		return 0;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#max(double[], double[])
	 */
	public double[] max(double[] p,double[] r)
	{
		if(compare(p,r) == 1) return p;
		else return r;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#min(double[], double[])
	 */
	public double[] min(double[] p,double[] r)
	{
		if(compare(p,r) == 1) return r;
		else return p;
	}
}
