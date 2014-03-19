package fr.unistra.pelican.util.vectorial.orders;



import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.statistics.PCA;
import fr.unistra.pelican.util.vectorial.VectorPixel;

/**
 * This class represents a vector ordering scheme capable of computing the
 * extrema of a given set of vectors by first calculating their eigen vectors and then
 * ordering them lexicographically according to their projections on these dimensions, with
 * the dimension order being determined by their (i.e. the dimensions') variances.
 * 
 * @author E.A.
 *
 */

public class PCALexicographicalOrdering implements VectorialOrdering
{
	private double[][] newVectors;

	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#max(double[][])
	 */
	public double[] max(double[][] p)
	{
		preprocess(p);
		
		int max = 0;

		for(int i = 1; i < p.length; i++){
			if(this.compare(newVectors[max],newVectors[i]) < 0) max = i;
		}

		return p[max];
	}

	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#max(double[][])
	 */
	public double[] min(double[][] p)
	{
		preprocess(p);
		
		int min = 0;

		for(int i = 1; i < p.length; i++){
			if(this.compare(newVectors[min],newVectors[i]) > 0) min = i;
		}

		return p[min];
	}

	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#rank(double[][], int)
	 */
	public double[] rank(double[][] p,int r)
	{
		preprocess(p);
		
		System.err.println("rank not supported");

		return null;
	}

	// the main man...
	private void preprocess(double[][] p)
	{
		// simdi yapilmasi gereken tum pikselleri bor goruntu haline
		// getirip statistics.PCA ya yollamak..
		
		// DIKKAT...eger RGB ise tabi hepsini yollarsin
		// ama isin icine H giriyorsa mesela HSY de o zaman sadece S ve Y
		// kisitli tutalim olayi ve H oldugu gibi kalsin...neyse..once RGB
		Image tmpImg = new DoubleImage(p.length,1,1,1,3);
		
		int bdim = p[0].length;
		
		for(int b = 0; b < bdim; b++)
			for(int x = 0; x < p.length; x++)
				tmpImg.setPixelXYBDouble(x,0,b,p[x][b]);
		
		tmpImg = (Image)(new PCA().processOne(0,tmpImg));
		
		newVectors = new double[p.length][p[0].length];
		
		for(int b = 0; b < bdim; b++)
			for(int x = 0; x < p.length; x++)
				newVectors[x][b] = tmpImg.getPixelXYBDouble(x,0,b);
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
