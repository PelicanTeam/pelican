package fr.unistra.pelican.util.vectorial.orders;


import fr.unistra.pelican.util.Tools;

/**
 * This class represents a set based vector ordering scheme
 * where the most distant pixels are chosen as extrema.
 * 
 * it could also be used as a principal axis approximation scheme..?!?!
 * 
 * @author E.A.
 *
 */

public class ExtremeOrdering implements VectorialOrdering
{
	private double[] max;
	private double[] min;

	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#max(double[][])
	 */
	public double[] max(double[][] p)
	{
		preprocess(p);

		return max;
	}

	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#min(double[][])
	 */
	public double[] min(double[][] p)
	{
		preprocess(p);

		return min;
	}

	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#rank(double[][], int)
	 */
	public double[] rank(double[][] p,int r)
	{
		System.err.println("no ranking implemented");
		
		return null;
	}

	private void preprocess(double[][] p)
	{
		int p1 = 0;
		int p2 = 0;
		double dist = 0.0;
		
		
		for(int i = 0; i < p.length; i++){
			for(int j = 0; j < p.length; j++){
				double tmp = Tools.euclideanDistance(p[i],p[j]);
				
				if(tmp > dist){
					dist = tmp;
					p1 = i;
					p2 = j;
				}
			}
		}
		
		// en uzak iki benegi elde ettik boylece
		// hangisi daha buyuk..ilk boyutu daha buyuk olan..
		if(p[p1][0] > p[p2][0]){
			max = p[p1];
			min = p[p2];
		}
		else{
			max = p[p2];
			min = p[p1];
		}
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
