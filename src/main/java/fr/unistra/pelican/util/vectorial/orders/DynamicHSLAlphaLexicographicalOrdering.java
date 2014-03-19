package fr.unistra.pelican.util.vectorial.orders;

import java.util.Arrays;
import java.util.Comparator;

import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.util.Tools;


/**
 * This class represents a lexicographical ordering scheme for double valued pixels in the [0,1] interval.
 * The input pixels are considered in the HSL order..
 * 
 * The priority of the components is customizable either by modifying their order or by division 
 * with an argument alpha, aiming to decrease the dynamic range of the component. 
 * 
 * In this version the alpha component is variable between 1 and 2alpha, with the exact value
 * being determined in relation to the occurrence propability of an intensity value.
 * 
 * Here we have the threshold approach...if inferior to the histo avg then alpha = 10 if not alpha = 1
 * 
 * For now, only the first component can be "softened" with division (H is not yet supported for softening).
 * 
 * @author E.A.
 *
 */

public class DynamicHSLAlphaLexicographicalOrdering implements VectorialOrdering,Comparator
{
	private double alpha;
	private int order;
	private double refHue;
	private double[][] histo;				// must be normalized
	private double avg = 1 / 256.0;
	
	public static final int HLS = 0;
	public static final int HSL = 1;
	public static final int LSH = 2;
	public static final int LHS = 3;
	public static final int SHL = 4;
	public static final int SLH = 5;
	
	/**
	 * Alpha is to be chosen as if the pixels values were in [0,255]
	 * 
	 * @param alpha the rounding parameter
	 * @param order the prioritization order of the channels
	 * @param refHue the hue origin
	 */
	public DynamicHSLAlphaLexicographicalOrdering(double alpha,int order,double refHue,double[][] histo)
	{
		if(alpha > 0.0) this.alpha = alpha;
		else this.alpha = 1.0;
		
		this.order = order;
		this.refHue = refHue;
		this.histo = histo;
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
			}else throw new ClassCastException();

		}catch(ClassCastException ex){
			ex.printStackTrace();
		}
		
		int tmp1,tmp2;
		double dtmp1,dtmp2;
		
		switch(order){
			case LSH:
				// L rounded..
				if(alpha > 1.0){
					// first make an int of it so that we can find the correct histo bin.
					dtmp1 = Math.round(ByteImage.doubleToByte * p1[2]);
					double prop = histo[2][(int)dtmp1];
					
					if(Tools.doubleCompare(prop,0.0) <= 0) dtmp1 = Math.ceil(dtmp1 / alpha);
					else{
						double tmpAlpha = 1 / (1 + Math.exp(-250.0 * (prop - avg)));
						if(prop < avg) dtmp1 = Math.ceil(dtmp1 / alpha);
						else dtmp1 = dtmp1 / alpha;
					}
					
					// first make an int of it so that we can find the correct histo bin.
					dtmp2 = Math.round(ByteImage.doubleToByte * p2[2]);
					prop = histo[2][(int)dtmp2];
					
					if(Tools.doubleCompare(prop,0.0) <= 0) dtmp2 = Math.ceil(dtmp2 / alpha);
					else{
						double tmpAlpha = 1 / (1 + Math.exp(-250.0 * (prop - avg)));
						if(prop < avg) dtmp2 = Math.ceil(dtmp2 / alpha);
						else dtmp2 = dtmp2 / alpha;
					}
					
				
					if(dtmp1 < dtmp2) return -1;
					else if(dtmp1 > dtmp2) return 1;
				}else{
					if(p1[2] < p2[2]) return -1;
					else if(p1[2] > p2[2]) return 1;
				}
				
				// S natural ordering
				if(p1[1] < p2[1]) return -1;
				else if(p1[1] > p2[1]) return 1;
				
				dtmp1 = Math.abs(refHue - p1[0]);
				dtmp2 = Math.abs(refHue - p2[0]);
				
				if(dtmp1 > 0.5) dtmp1 = 1.0 - dtmp1;
				if(dtmp2 > 0.5) dtmp2 = 1.0 - dtmp2;
				
				// reverse the order direction so the "closer" to the reference is "bigger"..
				if(dtmp1 < dtmp2) return 1;
				else if(dtmp1 > dtmp2) return -1;
				
				return 0;
			case SLH:
				// S rounded..
				if(alpha > 1.0){
					tmp1 = (int)Math.ceil(p1[1] * 255.0 / alpha);
					tmp2 = (int)Math.ceil(p2[1] * 255.0 / alpha);
				
					if(tmp1 < tmp2) return -1;
					else if(tmp1 > tmp2) return 1;
				}else{
					if(p1[1] < p2[1]) return -1;
					else if(p1[1] > p2[1]) return 1;
				}
				
				// L natural ordering
				if(p1[2] < p2[2]) return -1;
				else if(p1[2] > p2[1]) return 1;
				
				dtmp1 = Math.abs(refHue - p1[0]);
				dtmp2 = Math.abs(refHue - p2[0]);
				
				if(dtmp1 > 0.5) dtmp1 = 1.0 - dtmp1;
				if(dtmp2 > 0.5) dtmp2 = 1.0 - dtmp2;
				
				// reverse the order direction so the "closer" to the reference is "bigger"..
				if(dtmp1 < dtmp2) return 1;
				else if(dtmp1 > dtmp2) return -1;
				
				return 0;
			case LHS:
				// L rounded..
				if(alpha > 1.0){
					tmp1 = (int)Math.ceil(p1[2] * 255.0 / alpha);
					tmp2 = (int)Math.ceil(p2[2] * 255.0 / alpha);
				
					if(tmp1 < tmp2) return -1;
					else if(tmp1 > tmp2) return 1;
				}else{
					if(p1[2] < p2[2]) return -1;
					else if(p1[2] > p2[2]) return 1;
				}
				
				dtmp1 = Math.abs(refHue - p1[0]);
				dtmp2 = Math.abs(refHue - p2[0]);
				
				if(dtmp1 > 0.5) dtmp1 = 1.0 - dtmp1;
				if(dtmp2 > 0.5) dtmp2 = 1.0 - dtmp2;
				
				// reverse the order direction so the "closer" to the reference is "bigger"..
				if(dtmp1 < dtmp2) return 1;
				else if(dtmp1 > dtmp2) return -1;
				
				// S natural ordering
				if(p1[1] < p2[1]) return -1;
				else if(p1[1] > p2[1]) return 1;
				
				return 0;
			case SHL:
				// S rounded..
				if(alpha > 1.0){
					tmp1 = (int)Math.ceil(p1[1] * 255.0 / alpha);
					tmp2 = (int)Math.ceil(p2[1] * 255.0 / alpha);
				
					if(tmp1 < tmp2) return -1;
					else if(tmp1 > tmp2) return 1;
				}else{
					if(p1[1] < p2[1]) return -1;
					else if(p1[1] > p2[1]) return 1;
				}
				
				dtmp1 = Math.abs(refHue - p1[0]);
				dtmp2 = Math.abs(refHue - p2[0]);
				
				if(dtmp1 > 0.5) dtmp1 = 1.0 - dtmp1;
				if(dtmp2 > 0.5) dtmp2 = 1.0 - dtmp2;
				
				// reverse the order direction so the "closer" to the reference is "bigger"..
				if(dtmp1 < dtmp2) return 1;
				else if(dtmp1 > dtmp2) return -1;
				
				// L natural ordering
				if(p1[2] < p2[2]) return -1;
				else if(p1[2] > p2[2]) return 1;
				
				return 0;
			case HSL:
				dtmp1 = Math.abs(refHue - p1[0]);
				dtmp2 = Math.abs(refHue - p2[0]);
				
				if(dtmp1 > 0.5) dtmp1 = 1.0 - dtmp1;
				if(dtmp2 > 0.5) dtmp2 = 1.0 - dtmp2;
				
				// reverse the order direction so the "closer" to the reference is "bigger"..
				if(dtmp1 < dtmp2) return 1;
				else if(dtmp1 > dtmp2) return -1;
				
				// S natural ordering
				if(p1[1] < p2[1]) return -1;
				else if(p1[1] > p2[1]) return 1;
				
				// L natural ordering
				if(p1[2] < p2[2]) return -1;
				else if(p1[2] > p2[2]) return 1;
				
				return 0;
			case HLS:
				dtmp1 = Math.abs(refHue - p1[0]);
				dtmp2 = Math.abs(refHue - p2[0]);
				
				if(dtmp1 > 0.5) dtmp1 = 1.0 - dtmp1;
				if(dtmp2 > 0.5) dtmp2 = 1.0 - dtmp2;
				
				// reverse the order direction so the "closer" to the reference is "bigger"..
				if(dtmp1 < dtmp2) return 1;
				else if(dtmp1 > dtmp2) return -1;
				
				// L natural ordering
				if(p1[2] < p2[2]) return -1;
				else if(p1[2] > p2[2]) return 1;
				
				// S natural ordering
				if(p1[1] < p2[1]) return -1;
				else if(p1[1] > p2[1]) return 1;
				
				return 0;
			default:
				System.err.println("not available");
				return 0;
		}
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
