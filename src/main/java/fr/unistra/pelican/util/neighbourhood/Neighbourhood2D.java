package fr.unistra.pelican.util.neighbourhood;

import java.awt.Point;

/**
 * Static class which returns array of Point according to a certain neighbourhood
 * @author Jonathan Weber
 */
public class Neighbourhood2D {
	
	/**
	 * Returns the point of the 4-neighbourhood
	 * (-1,0),(0,-1),(1,0),(0,1) 
	 * @return 4-neighbourhood
	 */
	public static Point[] get4Neighboorhood()
	{
		Point[] neighbourhood = {new Point(-1,0),new Point(0,-1),new Point(1,0),new Point(0,1)};
		return neighbourhood;
	}
	
	/**
	 * Returns the point of the 8-neighbourhood
	 * (-1,-1),(0,-1),(1,-1),(-1,0),(1,0),(-1,1),(0,1),(1,1)
	 * @return 8-neighbourhood
	 */
	public static Point[] get8Neighboorhood()
	{
		Point[] neighbourhood = {new Point(-1,-1),new Point(0,-1),new Point(1,-1),new Point(-1,0),new Point(1,0),new Point(-1,1),new Point(0,1),new Point(1,1)};
		return neighbourhood;
	}

}
