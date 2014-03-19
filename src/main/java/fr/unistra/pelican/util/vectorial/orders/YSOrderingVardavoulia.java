package fr.unistra.pelican.util.vectorial.orders;

import java.util.Arrays;
import java.util.Comparator;

import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.vectorial.VectorPixel;

/**
 * This class represents a lexicographical LSH ordering scheme
 * that ignores the hue.
 * 
 * the dimensions of the input vectors are considered in the order : H S L (or Y)
 * 
 * @author E.A.
 *
 */

public class YSOrderingVardavoulia implements VectorialOrdering,Comparator
{		
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

		try{
			if(o1.getClass().getName().equals("[D")){
				p1 = (double[])o1;
				p2 = (double[])o2;
			}else if(o1.getClass().getName().equals("fr.unistra.pelican.util.vectorial.VectorPixel")){
				p1 = ((VectorPixel)o1).getVector();
				p2 = ((VectorPixel)o2).getVector();
				
			}else throw new ClassCastException();

		}catch(ClassCastException ex){
			ex.printStackTrace();
		}
		
		if(p1[2] < p2[2]) return -1;
		else if(p1[2] > p2[2]) return 1;
		
		// inverse comparison for saturation...
		// we favor white => racism in colour morphology...whats next?
		if(p1[1] < p2[1]) return 1;
		else if(p1[1] > p2[1]) return -1;
		
		
		
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
	
	/**
	 * ignoring hue
	 * @param img1
	 * @param img2
	 * @return
	 */
	public static Image Difference(Image img1,Image img2)
	{
		Image img = img1.copyImage(false);
		
		for(int b = 1; b < img1.getBDim(); b++){
			for(int x = 0; x < img1.getXDim(); x++){
				for(int y = 0; y < img1.getYDim(); y++){
					double p1 = img1.getPixelXYBDouble(x,y,b);
					double p2 = img2.getPixelXYBDouble(x,y,b);
					
					if(p1 - p2 < 0.0) img.setPixelXYBDouble(x,y,b,0.0);
					else img.setPixelXYBDouble(x,y,b,p1 - p2);
				}				
			}
		}
		
		return img;
	}
	
	/**
	 * ignoring hue
	 * @param img1
	 * @param img2
	 * @return
	 */
	public static Image Addition(Image img1,Image img2)
	{
		Image img = img1.copyImage(false);
		
		for(int b = 1; b < img1.getBDim(); b++){
			for(int x = 0; x < img1.getXDim(); x++){
				for(int y = 0; y < img1.getYDim(); y++){
					double p1 = img1.getPixelXYBDouble(x,y,b);
					double p2 = img2.getPixelXYBDouble(x,y,b);
					
					if(p1 + p2 > 1.0) img.setPixelXYBDouble(x,y,b,1.0);
					else img.setPixelXYBDouble(x,y,b,p1 + p2);
				}				
			}
		}
		
		return img;
	}
}
