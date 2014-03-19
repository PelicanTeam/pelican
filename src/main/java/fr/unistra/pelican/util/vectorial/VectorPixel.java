package fr.unistra.pelican.util.vectorial;

/**
 * A class representing a double valued vectorial pixel, equipped with its two dimensional coordinates
 * 
 * @author E.A.
 *
 */

public class VectorPixel
{
	public double[] vector;
	public int x;
	public int y;
	
	/*
	 * Constructs a vectorial pixel with the given vector and coordinates
	 * 
	 * @param vector double valued pixel values
	 * @param x horizontal coordinate of the vector
	 * @param y vertical coordinate of the vector
	 */

	public VectorPixel(double[] vector,int x,int y)
	{
		this.x = x;
		this.y = y;
		this.vector = vector;
	}

	/*
	 * Returns the vector of the pixel
	 * 
	 * @return the double valued vector
	 */
	public double[] getVector()
	{
		return vector.clone();
	}
	
	/*
	 * Returns the horizontal coordinate of the vector
	 * 
	 * @return the horizontal coordinate of the vector
	 */
	
	public int getX()
	{
		return x;
	}
	
	/*
	 * Returns the vertical coordinate of the vector
	 * 
	 * @return the vertical coordinate of the vector
	 */
	public int getY()
	{
		return y;
	}
}