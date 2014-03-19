package fr.unistra.pelican.util.vectorial.orders;

import java.util.Arrays;
import java.util.Comparator;

import fr.unistra.pelican.util.vectorial.VectorPixel;

/*

TODO

- size control
- use longs instead of ints => up to 7 channels with fast comparing..i need more time HAL.

*/

/**
 * This class represents a vector ordering scheme based on bit interlacing.. as proposed by Channussot.
 * Negative values are not supported.
 */

public class BitMixOrdering implements VectorialOrdering,Comparator
{
	private int doubleSize = 64;
	//private boolean byteFlag = false;

	/*
	public BitMixOrdering(boolean byteFlag)
	{
		doubleSize = Long.toBinaryString(Double.doubleToLongBits(Double.MAX_VALUE)).length() + 1;
		this.byteFlag = byteFlag;
	}*/

	/**
	 * Default constructor
	 */
	public BitMixOrdering()
	{
		doubleSize = Long.toBinaryString(Double.doubleToLongBits(Double.MAX_VALUE)).length() + 1;
	}

	/*
	public void setByteFlag(boolean byteFlag)
	{
		this.byteFlag = byteFlag;
	}*/

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
				
			}else
				throw new ClassCastException();

		}catch(ClassCastException ex){
			ex.printStackTrace();
		}

		/*
		// our double array is in fact an array of bytes..groovy!
		// so in the case of a tristumulus byte color, the bytes can be
		// placed in an int and get naturally compared.
		if(byteFlag == true){
			long vector1 = 0;
			long vector2 = 0;

			//etc

			if(vector1 > vector2) return 1;
			else if(vector1 < vector2) return -1;
			else return 0;
		}*/

		int n = p1.length;
		int totalBits = doubleSize * n;
		int rest,len;

		double[] d1 = p1;
		double[] d2 = p2;

		// check for negative values
		for(int j = 0; j < n; j++){
			if(d1[j] < 0.0 || d2[j] < 0.0){
				double min = Double.MAX_VALUE;
				double max = -Double.MAX_VALUE;
				
				for(int i = 0; i < n; i++){
					if(d1[i] < min) min = d1[i];
					if(d1[i] > max) max = d1[i];
				}

				for(int i = 0; i < n; i++){
					if(d2[i] < min) min = d2[i];
					if(d2[i] > max) max = d2[i];
				}

				// min < 0.0
				double trans = Math.abs(min) + 1;

				if(trans > Double.MAX_VALUE - Math.abs(max)){
					System.err.println("cannot process pixel values");
					// throw something... FIXME
				}else{
					d1 = new double[n];
					d2 = new double[n];

					for(int i = 0; i < n; i++)
						d1[i] = p1[i] + trans;
					for(int i = 0; i < n; i++)
						d2[i] = p2[i] + trans;
				}
			}
		}

		StringBuffer s1 = new StringBuffer(totalBits);
		StringBuffer s2 = new StringBuffer(totalBits);
		s1.setLength(totalBits);
		s2.setLength(totalBits);

		StringBuffer[] sArray1 = new StringBuffer[n];
		StringBuffer[] sArray2 = new StringBuffer[n];

		for(int i = 0; i < n; i++){
			String s = Long.toBinaryString(Double.doubleToLongBits(d1[i]));
			len = s.length();

			sArray1[i] = new StringBuffer(s);

			// pad with zeros if necessary
			if(len < doubleSize){
				rest = doubleSize - len;
				for(int j = 0; j < rest; j++)
					sArray1[i].insert(0,'0');

			}

			s = Long.toBinaryString(Double.doubleToLongBits(d2[i]));
			len = s.length();

			sArray2[i] = new StringBuffer(s);

			// pad with zeros if necessary
			if(len < doubleSize){
				rest = doubleSize - len;
				for(int j = 0; j < rest; j++)
					sArray2[i].insert(0,'0');

			}
		}


		// combine each StringBuffer into one big buffer.
		for(int i = 0; i < doubleSize; i++){
			for(int j = 0; j < n; j++){
				s1.setCharAt(i * n + j,sArray1[j].charAt(i));
				s2.setCharAt(i * n + j,sArray2[j].charAt(i));
			}
		}

		// it is also possible to use BigIntegers
		// but the strings are already suitable for a
		// lexicographical comparison..and besides, it's faster.
		for(int i = 0; i < totalBits; i++){
			char c1 = s1.charAt(i);
			char c2 = s2.charAt(i);

			if(c1 == '0' && c2 == '1')
				return -1;

			else if(c1 == '1' && c2 == '0')
				return 1;
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
