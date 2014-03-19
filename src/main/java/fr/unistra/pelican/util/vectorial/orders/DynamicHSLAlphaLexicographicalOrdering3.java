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
 * Interesting new version...that works correctly...
 * each group of equality (obtained after dividing and rounding off with alpha) is further subdivided
 * according to their average frequency...low frequency => high division.
 * 
 * For now, only the first component can be "softened" with division (H is not yet supported for softening).
 * 
 * @author E.A.
 *
 */

public class DynamicHSLAlphaLexicographicalOrdering3 implements VectorialOrdering,Comparator
{
	private double alpha;
	private int order;
	private double refHue;
	private double[][] histo;				// must be normalized
	private double mean = 1 / 256.0;
	
	private double[] transformed;			// buna ihtiyacimiz var cunku bir onceki 
											// piksel degerinin kac oldugunu da bilmek zorundayiz.
											// tabi bunu yapabilmek icin de 256 farkli aydinlik duzeyi oldugunu
											// varsayiyorum...
	
	public static final int HLS = 0;
	public static final int HSL = 1;
	public static final int LSH = 2;
	public static final int LHS = 3;
	public static final int SHL = 4;
	public static final int SLH = 5;
	
	public static int syc = 0;
	
	/**
	 * Alpha is to be chosen as if the pixels values were in [0,255]
	 * 
	 * @param alpha the rounding parameter
	 * @param order the prioritization order of the channels
	 * @param refHue the hue origin
	 */
	public DynamicHSLAlphaLexicographicalOrdering3(double alpha,int order,double refHue,double[][] histo)
	{
		if(alpha > 1.0) this.alpha = alpha;
		else this.alpha = 1.0;
		
		// integer value!!!
		alpha = Math.round(alpha);
		
		this.order = order;
		this.refHue = refHue;
		this.histo = histo;
		
		// construct the transformed array
		transformed = new double[256];
		
		// first apply an alpha subquantization..
		// all values are now \in [0, ceilof 256/alpha]
		for(int i = 0; i < 256; i++)
			transformed[i] = Math.ceil(i/alpha);
		
		// now for every group of equality
		// compute their average frequency
		for(int i = 1; i < 256; i++){
			double avg = 0.0;
			
			int syc = 0;
			for(int k = i; (k < 256 && k < i + alpha); k++){
				avg += histo[2][k];
				syc++;
			}
			
			avg = avg / syc;
			
			// elimizde bu takimin ortalamasi var..
			// simdi de sigmoidden gecirelim
			double sigma = 1 / (1 + Math.exp(450 * (avg - mean))); // dusuk olasilik icin buyuk takimlar
			
			// her esitlik takiminin genisligi alpha dir.
			int width = (int)alpha;
			
			int birimSys = (int)Math.round(sigma * width);
			//System.err.println(birimSys + " birim");				
			
			if(birimSys <= 1){	// cok dusuk olasilik...tam basamak sayisi
				for(int k = i; (k < 256 && k < i + alpha); k++)
					transformed[k] = k/alpha;
				
			}else if(birimSys == width){		// cok yuksek olasilik..ayni kalsinlar
				for(int k = i; (k < 256 && k < i + alpha); k++)
					transformed[k] = Math.ceil(i/alpha);
				
			}else{		// aradaki durumlardan soz konusu...evet bakalim.
				int adimSys = (int)Math.ceil(width/birimSys);
				
				for(int m = 0; m < adimSys; m++){	// her adim icin
					for(int k = i + birimSys * m; (k < 256 && k < i + birimSys * (m+1)); k++)
						transformed[k] = (i + birimSys * (m+1) - 1)/alpha;
					
				}
				
			}
			
			i += alpha - 1;
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
					dtmp1 = Math.round(ByteImage.doubleToByte * p1[2]);
					dtmp1 = transformed[(int)dtmp1];
					
					dtmp2 = Math.round(ByteImage.doubleToByte * p2[2]);
					dtmp2 = transformed[(int)dtmp2];
					
				
					if(Tools.doubleCompare(dtmp1,dtmp2) == -1) return -1;
					else if(Tools.doubleCompare(dtmp1,dtmp2) == 1) return 1;
				}else{
					if(Tools.doubleCompare(p1[2],p2[2]) == -1) return -1;
					else if(Tools.doubleCompare(p1[2],p2[2]) == 1) return 1;
				}
				
				// S natural ordering
				if(Tools.doubleCompare(p1[1],p2[1]) == -1) return -1;
				else if(Tools.doubleCompare(p1[1],p2[1]) == 1) return 1;
				
				// saturation and luminance appear to be equal..
				// check the real luminance values before proceeding to hue
				if(Tools.doubleCompare(p1[2],p2[2]) < 0) return -1;
				else if(Tools.doubleCompare(p1[2],p2[2]) > 0) return 1;
				
				dtmp1 = Math.abs(refHue - p1[0]); 
				dtmp2 = Math.abs(refHue - p2[0]);

				if(Tools.doubleCompare(dtmp1,0.5) > 0) dtmp1 = 1.0 - dtmp1;
				if(Tools.doubleCompare(dtmp2,0.5) > 0) dtmp2 = 1.0 - dtmp2;
				
				dtmp1 = dtmp2 * 1 / (1 + Math.exp(-5 * (p1[1] - 0.5)));
				dtmp2 = dtmp2 * 1 / (1 + Math.exp(-5 * (p2[1] - 0.5)));
				
				// reverse the order direction so the "closer" to the reference is "bigger"..
				if(Tools.doubleCompare(dtmp1,dtmp2) == -1) return 1;
				else if(Tools.doubleCompare(dtmp1,dtmp2) == 1) return -1;
				
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
					dtmp1 = Math.round(ByteImage.doubleToByte * p1[2]);
					dtmp1 = transformed[(int)dtmp1];
					
					dtmp2 = Math.round(ByteImage.doubleToByte * p2[2]);
					dtmp2 = transformed[(int)dtmp2];
					
				
					if(Tools.doubleCompare(dtmp1,dtmp2) == -1) return -1;
					else if(Tools.doubleCompare(dtmp1,dtmp2) == 1) return 1;
				}else{
					if(Tools.doubleCompare(p1[2],p2[2]) == -1) return -1;
					else if(Tools.doubleCompare(p1[2],p2[2]) == 1) return 1;
				}
				
				// hue
				dtmp1 = Math.abs(refHue - p1[0]); 
				dtmp2 = Math.abs(refHue - p2[0]);

				if(Tools.doubleCompare(dtmp1,0.5) > 0) dtmp1 = 1.0 - dtmp1;
				if(Tools.doubleCompare(dtmp2,0.5) > 0) dtmp2 = 1.0 - dtmp2;
				
				dtmp1 = dtmp2 * 1 / (1 + Math.exp(-5 * (p1[1] - 0.5)));
				dtmp2 = dtmp2 * 1 / (1 + Math.exp(-5 * (p2[1] - 0.5)));
				
				// reverse the order direction so the "closer" to the reference is "bigger"..
				if(Tools.doubleCompare(dtmp1,dtmp2) == -1) return 1;
				else if(Tools.doubleCompare(dtmp1,dtmp2) == 1) return -1;
				
				// hue and luminance appear to be equal..
				// check the real luminance values before proceeding to hue
				if(p1[2] < p2[2]) return -1;
				else if(p1[2] > p2[2]) return 1;
				
				// S natural ordering
				if(Tools.doubleCompare(p1[1],p2[1]) == -1) return -1;
				else if(Tools.doubleCompare(p1[1],p2[1]) == 1) return 1;
				
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
