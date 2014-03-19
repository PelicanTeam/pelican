package fr.unistra.pelican.util.neighbourhood;

import fr.unistra.pelican.util.Point3D;

/**
 * Static class which returns array of Point3D according to a certain neighbourhood
 * @author Jonathan Weber
 */
public class Neighbourhood3D {
	
	/**
	 * Returns the point of the 4-neighbourhood
	 * (-1,0,0),(0,-1,0),(1,0,0),(0,1,0) 
	 * @return 4-neighbourhood
	 */
	public static Point3D[] get4Neighboorhood()
	{
		Point3D[] neighbourhood = {new Point3D(-1,0,0),new Point3D(0,-1,0),new Point3D(1,0,0),new Point3D(0,1,0)};
		return neighbourhood;
	}
	
	/**
	 * Returns the point of the 8-neighbourhood
	 * (-1,-1,0),(0,-1,0),(1,-1,0),(-1,0,0),(1,0,0),(-1,1,0),(0,1,0),(1,1,0)
	 * @return 8-neighbourhood
	 */
	public static Point3D[] get8Neighboorhood()
	{
		Point3D[] neighbourhood = {new Point3D(-1,-1,0),new Point3D(0,-1,0),new Point3D(1,-1,0),new Point3D(-1,0,0),new Point3D(1,0,0),new Point3D(-1,1,0),new Point3D(0,1,0),new Point3D(1,1,0)};
		return neighbourhood;
	}

}
