package fr.unistra.pelican.util.vectorial.orders;


/**
 * This class represents the marginal vector ordering.
 * 
 * @author Lefevre
 *
 */

public class MarginalOrdering implements VectorialOrdering
{

	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#max(double[][])
	 */
	public double[] max(double[][] p)
	{
		double[] dmax = new double[p[0].length];
		
		for(int i = 0; i < p[0].length; i++){
			dmax[i] = p[0][i];
			for(int j = 1; j < p.length; j++)
				if(p[j][i] > dmax[i]) dmax[i] = p[j][i];
		}
		
		return dmax;		
	}

	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#min(double[][])
	 */
	public double[] min(double[][] p)
	{
		double[] dmin= new double[p[0].length];
		
		for(int i = 0; i < p[0].length; i++){
			dmin[i] = p[0][i];
			for(int j = 1; j < p.length; j++)
				if(p[j][i] < dmin[i]) dmin[i] = p[j][i];
		}
		
		return dmin;		
	}

	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#rank(double[][], int)
	 */
	public double[] rank(double[][] p,int r)
	{
		double [] drank = new double[p.length];
		return drank;
		
	}

}
