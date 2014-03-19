package fr.unistra.pelican.util.vectorial.orders;


import java.util.Vector;

import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
//import fr.unistra.pelican.algorithms.experimental.abdullah.ConvexHull;
import fr.unistra.pelican.algorithms.morphology.binary.hitormiss.BinaryConvexHull;
import fr.unistra.pelican.util.vectorial.VectorPixel;

/**
 * This class represents a vector median computing scheme based on marginal 2d convex hulls
 * ..designed for RGB
 * 
 * @author E.A.
 *
 */

public class ConvexHullBasedMedian2 implements VectorialOrdering
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
		
		Image img = new ByteImage(260,260,1,1,1);
		
		Vector[] nonch = new Vector[3];
		
		for(int b = 0; b < 3; b++){
			nonch[b] = new Vector();
			img.fill(0.0);
		
			for(int i = 0; i < p.length; i++)
				img.setPixelXYByte((int)(p[i][b]*255)+2,(int)(p[i][(b+1)%3]*255)+2,255);
		
			img = (Image) new BinaryConvexHull().process(img);
		
			for(int i = 0; i < p.length; i++){
				int pixel = img.getPixelXYByte((int)(p[i][b]*255)+2,(int)(p[i][(b+1)%3]*255)+2);
				if(pixel == 0) nonch[b].add(p[i]);
			}
		}
		
		// simdi de her birinin CH ye ait olmayanlarini ele alalim
		// yalniz dikkat...her vektor en fazla bir defa yer almali kumede...aci olacak.
		Vector toplu = new Vector();
		
		// ilki tmm..sira diger ikide
		toplu.addAll(nonch[0]);
		
		// digeri...
		int tmpSize = nonch[1].size();
		for(int i = 0; i < tmpSize; i++){
			double[] tmpP = (double[])nonch[1].get(i);
			
			int tmpSize2 = toplu.size();
			boolean flag = false;
			
			for(int j = 0; j < tmpSize2; j++){
				double[] tmpP2 = (double[])toplu.get(j);
				if(tmpP2[0] == tmpP[0] && tmpP2[1] == tmpP[1] && tmpP2[2] == tmpP[2]) flag = true;
			}
			if(flag == false) toplu.add(tmpP);
		}
		
		// ve ucuncu
		tmpSize = nonch[2].size();
		for(int i = 0; i < tmpSize; i++){
			double[] tmpP = (double[])nonch[2].get(i);
			
			int tmpSize2 = toplu.size();
			boolean flag = false;
			
			for(int j = 0; j < tmpSize2; j++){
				double[] tmpP2 = (double[])toplu.get(j);
				if(tmpP2[0] == tmpP[0] && tmpP2[1] == tmpP[1] && tmpP2[2] == tmpP[2]) flag = true;
			}
			if(flag == false) toplu.add(tmpP);
		}
		
		
		int size = toplu.size();
		
		//System.err.println(size);
		
		VectorialOrdering vo = new CumulativeDistanceOrdering();
		
		if(size == 0)
			return vo.min(p);
		else{
			double[][] r = new double[size][3];
			
			for(int i = 0; i < size; i++)
				r[i] = (double[])toplu.get(i);
			
			return vo.min(r);
		}
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
