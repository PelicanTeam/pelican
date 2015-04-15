package fr.unistra.pelican.util.neighbourhood;

import fr.unistra.pelican.util.Point4D;

/**
 * Static class which returns array of Point4D according to a certain neighbourhood
 * @author Jonathan Weber
 */
public class Neighbourhood4D {
	
	/**
	 * Returns the point of the 4-neighbourhood
	 * (-1,0,0,0),(0,-1,0,0),(1,0,0,0),(0,1,0,0) 
	 * @return 4-neighbourhood
	 */
	public static Point4D[] get4Neighboorhood()
	{
		Point4D[] neighbourhood = {new Point4D(-1,0,0,0),new Point4D(0,-1,0,0),new Point4D(1,0,0,0),new Point4D(0,1,0,0)};
		return neighbourhood;
	}
	
	/**
	 * Returns the point of the 8-neighbourhood
	 * (-1,-1,0,0),(0,-1,0,0),(1,-1,0,0),(-1,0,0,0),(1,0,0,0),(-1,1,0,0),(0,1,0,0),(1,1,0,0)
	 * @return 8-neighbourhood
	 */
	public static Point4D[] get8Neighboorhood()
	{
		Point4D[] neighbourhood = {new Point4D(-1,-1,0,0),new Point4D(0,-1,0,0),new Point4D(1,-1,0,0),new Point4D(-1,0,0,0),new Point4D(1,0,0,0),new Point4D(-1,1,0,0),new Point4D(0,1,0,0),new Point4D(1,1,0,0)};
		return neighbourhood;
	}

	/**
	 * Returns the point of the 6-temporalneighbourhood
	 * (-1,0,0,0),(0,-1,0,0),(1,0,0,0),(0,1,0,0),(0,0,0,-1),(0,0,0,1) 
	 * @return 6-temoralneighbourhood
	 */
	public static Point4D[] get6TemporalNeighboorhood()
	{
		Point4D[] neighbourhood = {new Point4D(-1,0,0,0),new Point4D(0,-1,0,0),new Point4D(1,0,0,0),new Point4D(0,1,0,0),new Point4D(0,0,0,1),new Point4D(0,0,0,-1)};
		return neighbourhood;
	}
	
	/**
	 * Returns the point of the 10-temporalneighbourhood
	 * (-1,-1,0,0),(0,-1,0,0),(1,-1,0,0),(-1,0,0,0),(1,0,0,0),(-1,1,0,0),(0,1,0,0),(1,1,0,0),(0,0,0,1),(0,0,0,-1)
	 * @return 10-temporalneighbourhood
	 */
	public static Point4D[] get10TemporalNeighboorhood()
	{
		Point4D[] neighbourhood = {new Point4D(-1,-1,0,0),new Point4D(0,-1,0,0),new Point4D(1,-1,0,0),new Point4D(-1,0,0,0),new Point4D(1,0,0,0),new Point4D(-1,1,0,0),new Point4D(0,1,0,0),new Point4D(1,1,0,0),new Point4D(0,0,0,1),new Point4D(0,0,0,-1)};
		return neighbourhood;
	}
	
	/**
	 * Returns the point of the 26-temporalneighbourhood
	 * (-1,-1,0,0),(0,-1,0,0),(1,-1,0,0),(-1,0,0,0),(1,0,0,0),(-1,1,0,0),(0,1,0,0),(1,1,0,0),
	 * (-1,-1,0,1),(0,-1,0,1),(1,-1,0,1),(-1,0,0,1),(0,0,0,1),(1,0,0,1),(-1,1,0,1),(0,1,0,1),(1,1,0,1),
	 * (-1,-1,0,-1),(0,-1,0,-1),(1,-1,0,-1),(-1,0,0,-1),(0,0,0,-1),(1,0,0,-1),(-1,1,0,-1),(0,1,0,-1),(1,1,0,-1)
	 * @return 26-temporalneighbourhood
	 */
	public static Point4D[] get26TemporalNeighboorhood()
	{
		Point4D[] neighbourhood = {new Point4D(-1,-1,0,0),new Point4D(0,-1,0,0),new Point4D(1,-1,0,0),new Point4D(-1,0,0,0),new Point4D(1,0,0,0),new Point4D(-1,1,0,0),new Point4D(0,1,0,0),new Point4D(1,1,0,0),
				new Point4D(-1,-1,0,1),new Point4D(0,-1,0,1),new Point4D(1,-1,0,1),new Point4D(-1,0,0,1),new Point4D(0,0,0,1),new Point4D(1,0,0,1),new Point4D(-1,1,0,1),new Point4D(0,1,0,1),new Point4D(1,1,0,1),
				new Point4D(-1,-1,0,-1),new Point4D(0,-1,0,-1),new Point4D(1,-1,0,-1),new Point4D(-1,0,0,-1),new Point4D(0,0,0,-1),new Point4D(1,0,0,-1),new Point4D(-1,1,0,-1),new Point4D(0,1,0,-1),new Point4D(1,1,0,-1)};
		return neighbourhood;
	}
	
	/**
	 * Returns the point of the 26-neighbourhood
	 * (-1,-1,0,0),(0,-1,0,0),(1,-1,0,0),(-1,0,0,0),(1,0,0,0),(-1,1,0,0),(0,1,0,0),(1,1,0,0),
	 * (-1,-1,1,0),(0,-1,1,0),(1,-1,1,0),(-1,0,1,0),(0,0,1,0),(1,0,1,0),(-1,1,1,0),(0,1,1,0),(1,1,1,0),
	 * (-1,-1,-1,0),(0,-1,-1,0),(1,-1,-1,0),(-1,0,-1,0),(0,0,-1,0),(1,0,-1,0),(-1,1,-1,0),(0,1,-1,0),(1,1,-1,0)
	 * @return 26-neighbourhood
	 */
	public static Point4D[] get26Neighboorhood()
	{
		Point4D[] neighbourhood = {new Point4D(-1,-1,0,0),new Point4D(0,-1,0,0),new Point4D(1,-1,0,0),new Point4D(-1,0,0,0),new Point4D(1,0,0,0),new Point4D(-1,1,0,0),new Point4D(0,1,0,0),new Point4D(1,1,0,0),
				new Point4D(-1,-1,1,0),new Point4D(0,-1,1,0),new Point4D(1,-1,1,0),new Point4D(-1,0,1,0),new Point4D(0,0,1,0),new Point4D(1,0,1,0),new Point4D(-1,1,1,0),new Point4D(0,1,1,0),new Point4D(1,1,1,0),
				new Point4D(-1,-1,-1,0),new Point4D(0,-1,-1,0),new Point4D(1,-1,-1,0),new Point4D(-1,0,-1,0),new Point4D(0,0,-1,0),new Point4D(1,0,-1,0),new Point4D(-1,1,-1,0),new Point4D(0,1,-1,0),new Point4D(1,1,-1,0)};
		return neighbourhood;
	}
	
	/**
	 * Returns the point of the 18-temporalneighbourhood
	 * (-1,-1,0,0),(0,-1,0,0),(1,-1,0,0),(-1,0,0,0),(1,0,0,0),(-1,1,0,0),(0,1,0,0),(1,1,0,0),
	 * (0,-1,0,1),(-1,0,0,1),(0,0,0,1),(1,0,0,1),(0,1,0,1),
	 * (0,-1,0,-1),(-1,0,0,-1),(0,0,0,-1),(1,0,0,-1),(0,1,0,-1)
	 * @return 18-temporalneighbourhood
	 */
	public static Point4D[] get18TemporalNeighboorhood()
	{
		Point4D[] neighbourhood = {new Point4D(-1,-1,0,0),new Point4D(0,-1,0,0),new Point4D(1,-1,0,0),new Point4D(-1,0,0,0),new Point4D(1,0,0,0),new Point4D(-1,1,0,0),new Point4D(0,1,0,0),new Point4D(1,1,0,0),
				new Point4D(0,-1,0,1),new Point4D(-1,0,0,1),new Point4D(0,0,0,1),new Point4D(1,0,0,1),new Point4D(0,1,0,1),
				new Point4D(0,-1,0,-1),new Point4D(-1,0,0,-1),new Point4D(0,0,0,-1),new Point4D(1,0,0,-1),new Point4D(0,1,0,-1)};
		return neighbourhood;
	}
	
	/**
	 * Returns the point of the 2-temporal neighbourhood
	 * (0,0,0,-1),(0,0,0,1) 
	 * @return 2-temoralneighbourhood
	 */
	public static Point4D[] get2TemporalNeighboorhood()
	{
		Point4D[] neighbourhood = {new Point4D(0,0,0,1),new Point4D(0,0,0,-1)};
		return neighbourhood;
	}
	
	/**
	 * Returns the point of the semi 2-temporal neighbourhood
	 * (0,0,0,1) 
	 * @return semi 2-temoralneighbourhood
	 */
	public static Point4D[] getSemi2TemporalNeighboorhood()
	{
		Point4D[] neighbourhood = {new Point4D(0,0,0,1)};
		return neighbourhood;
	}
	
	/**
	 * Returns the point of the semi 8-neighbourhood
	 * (1,0,0,0),(-1,1,0,0),(0,1,0,0),(1,1,0,0)
	 * @return semi 8-neighbourhood
	 */
	public static Point4D[] getSemi8Neighboorhood()
	{
		Point4D[] neighbourhood = {new Point4D(1,0,0,0),new Point4D(-1,1,0,0),new Point4D(0,1,0,0),new Point4D(1,1,0,0)};
		return neighbourhood;
	}
	
	/**
	 * Returns the point of the semi 10-temporal-neighbourhood
	 * (1,0,0,0),(-1,1,0,0),(0,1,0,0),(1,1,0,0),(0,0,0,1)
	 * @return semi 8-neighbourhood
	 */
	public static Point4D[] getSemi10TemporalNeighboorhood()
	{
		Point4D[] neighbourhood = {new Point4D(1,0,0,0),new Point4D(-1,1,0,0),new Point4D(0,1,0,0),new Point4D(1,1,0,0),new Point4D(0,0,0,1)};
		return neighbourhood;
	}
	
	/**
	 * Returns the point of the semi 4-neighbourhood
	 * (1,0,0,0),(0,1,0,0) 
	 * @return semi 4-neighbourhood
	 */
	public static Point4D[] getSemi4Neighboorhood()
	{
		Point4D[] neighbourhood = {new Point4D(1,0,0,0),new Point4D(0,1,0,0)};
		return neighbourhood;
	}
}
