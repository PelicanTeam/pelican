package fr.unistra.pelican.util.vectorial.orders;


import java.util.Vector;

import fr.unistra.pelican.util.Tools;
import fr.unistra.pelican.util.vectorial.VectorPixel;

/**
 * This class represents a vector median computing scheme based on marginal 2d convex hulls
 * ..designed for RGB
 * 
 * based on quickhull...but not yet finished..
 * 
 * @author E.A.
 *
 */

public class ConvexHullBasedMedian implements VectorialOrdering
{
	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#max(double[][])
	 */
	public double[] max(double[][] p)
	{
		return null;
	}

	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#min(double[][])
	 */
	public double[] min(double[][] p)
	{
		return null;
	}

	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#rank(double[][], int)
	 */
	public double[] rank(double[][] p,int r)
	{
		return median(p);
	}
	
	private double[] median(double[][] p)
	{
		// once her ikili boyut icin ch ler hesaplanirlar
		// ch noktalari bulunur ve yok edilirler..kalanlar uzerinden de vmedian yapilir...
		// once sadece iki boyut icin deneyelim...mesela RG icin bakalim vmf i gecebilecek mi...
		
		// buraya ch nin uyelerini yerlestirelim
		Vector points = new Vector();
		
		// hepsi
		Vector hepsi = new Vector();
		for(int i = 0; i < p.length; i++)
			hepsi.add(p[i]);
		
		// once en uctakileri bul...en sol ve en sag.
		// r = x = 0 ve g = y = 1
		int sol = 0;
		int sag = 0;
		for(int i = 1; i < p.length; i++){
			if(p[i][0] < p[sol][0]) sol = i;
			if(p[i][0] > p[sag][0]) sag = i;
		}
		// bunlar ch ye ait..
		points.add(p[sol]);
		points.add(p[sag]);
		
		// simdi de sol-sag arasi dogrunun iki yanindaki tum degerleri iki kume haline getirelim
		Vector kume1 = yukariKume(p[sol],p[sag],hepsi);
		Vector kume2 = asagiKume(p[sol],p[sag],hepsi);
		
		// sonra da quickhull i cagiralim
		points.addAll(quickHull(p[sol],p[sag],kume1));
		points.addAll(quickHull(p[sol],p[sag],kume2));
		
		// once dogru hesapladigindan emin ol sonra da ch olmayanlardan medianini sec.
		return null;
	}
	
	private Vector quickHull(double[] a,double[] b,Vector kume)
	{
		Vector v = new Vector();
		
		if(kume.size() == 0) return v;
		
		int ind = 0;
		double dist = 0.0;
		
		// ab dogrusundan en uzak benegi bul
		for(int i = 0; i < kume.size(); i++){
			double[] p = (double[])kume.get(i);
			
			double ap = Math.sqrt((a[0] - p[0]) * (a[0] - p[0]) + (a[1] - p[1]) * (a[1] - p[1]));
			double bp = Math.sqrt((b[0] - p[0]) * (b[0] - p[0]) + (b[1] - p[1]) * (b[1] - p[1]));
			double ab = Math.sqrt((b[0] - a[0]) * (b[0] - a[0]) + (b[1] - a[1]) * (b[1] - a[1]));
			
			double alan = ap * bp;
			
			if(dist > alan / ab){
				dist = alan / ab;
				ind = i;
			}
		}
		double[] p = (double[])kume.get(ind);
		v.add(p);
		v.add(quickHull(a,p,yukariKume(a,p,kume)));
		v.add(quickHull(a,p,asagiKume(a,p,kume)));
		
		return v;
	}
	
	private Vector asagiKume(double[] p1,double[] p2,Vector r)
	{
		Vector v = new Vector();
		
		for(int i = 0; i < r.size(); i++){
			double[] p = (double[])r.get(i);
			if((p[0] != p1[0] || p[1] != p1[1]) && (p[0] != p2[0] || p[1] != p2[1])){
				// vektorel carpima bak bakalim...ve arti olanlari buraya al
				double[] a = new double[3];
				a[0] = p[0] - p1[0];
				a[1] = p[1] - p1[1];
				a[2] = 0.0;
				
				double[] b = new double[3];
				b[0] = p2[0] - p1[0];
				b[1] = p2[1] - p1[1];
				b[2] = 0.0;
				
				double[] vproduct = Tools.vectorProduct(a,b);
				
				if(vproduct[2] >= 0.0) v.add(p[i]);
			}
		}
		
		return v;
	}
	
	private Vector yukariKume(double[] p1,double[] p2,Vector r)
	{
		Vector v = new Vector();
		
		for(int i = 0; i < r.size(); i++){
			double[] p = (double[])r.get(i);
			if((p[0] != p1[0] || p[1] != p1[1]) && (p[0] != p2[0] || p[1] != p2[1])){
				// vektorel carpima bak bakalim...ve arti olanlari buraya al
				double[] a = new double[3];
				a[0] = p[0] - p1[0];
				a[1] = p[1] - p1[1];
				a[2] = 0.0;
				
				double[] b = new double[3];
				b[0] = p2[0] - p1[0];
				b[1] = p2[1] - p1[1];
				b[2] = 0.0;
				
				double[] vproduct = Tools.vectorProduct(a,b);
				
				if(vproduct[2] < 0.0) v.add(p[i]);
			}
		}
		
		return v;
	}

	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#order(fr.unistra.pelican.util.vectorial.VectorPixel[])
	 */
	public VectorPixel[] order(VectorPixel[] v)
	{
		return null;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#max(double[], double[])
	 */
	public double[] max(double[] p,double[] r)
	{
		return p;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#min(double[], double[])
	 */
	public double[] min(double[] p,double[] r)
	{
		return r;
	}
}
