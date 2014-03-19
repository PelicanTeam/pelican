/**
 * 
 */
package fr.unistra.pelican.util.connectivityTrees.connectivity;

import java.awt.Point;
import java.util.Iterator;

import fr.unistra.pelican.util.Point3D;

/**
 * Abstract class to handle various type of connectivity.
 * 
 * The idea of this class is to provide an iterator of points directly connected to a given point (i.e. point that is in the neighbourhood and satisfy connectivity criteria).
 * 
 * Because the class is itself the iterator you must use several instances in overlapped loops. 
 * 
 * Warning the iterator always return same point and only modify its coordinate to save computation time
 * 
 * @author Benjamin Perret
 *
 */
public abstract class Connectivity3D implements Iterator<Point3D>, Iterable<Point3D>{

	/**
	 * Center point x coordinate, iterator will iterate over pixels directly connected to this one
	 */
	protected int currentX;
	
	/**
	 * Center point y coordinate, iterator will iterate over pixels directly connected to this one
	 */
	protected int currentY;
	
	/**
	 * Center point z coordinate, iterator will iterate over pixels directly connected to this one
	 */
	protected int currentZ;
	
	/**
	 * hum... what can it be
	 */
	protected boolean hasMoreNeigbours=false;
	
	/**
	 * yeah really!
	 */
	protected Point3D nextPointForIterator=new Point3D();
	
	/**
	 * All directly connected points to current point 
	 * @return
	 */
	public  Point3D [] getConnectedNeighbours(){
		return getConnectedNeighbours(currentX,currentY,currentZ);
	}
	
	/**
	 * All directly connected points to given point
	 * @param x
	 * @param y
	 * @return
	 */
	public Point3D [] getConnectedNeighbours(int x, int y)
	{
		return getConnectedNeighbours(x, y, 0);
	}
	
	/**
	 * All directly connected points to given point
	 * @param x
	 * @param y
	 * @return
	 */
	public abstract Point3D [] getConnectedNeighbours(int x, int y,int z);
	
	/**
	 * Change iterator center point
	 * @param x
	 * @param y
	 */
	public void setCurrentPoint(int x, int y){
		setCurrentPoint(x,y,0);
	}
	
	public void setCurrentPoint(Point3D p){
		setCurrentPoint(p.x,p.y,p.z);
	}
	
	
	
	/**
	 * Change iterator center point
	 * @param x
	 * @param y
	 */
	public void setCurrentPoint(int x, int y,int z){
		currentX=x;
		currentY=y;
	}
	
	/**
	 * Initialize iterator
	 */
	protected abstract void initializeIterator();
	
	/**
	 * Compute next point iterator will return and update hasMoreNeigbours flag
	 */
	protected abstract void computeNextPointForIterator();
	
	/**
	 * Not handled
	 */
	public void remove(){}

	
	public boolean hasNext() {
		return hasMoreNeigbours;
	}

	
	public Point3D next() {
		computeNextPointForIterator();
		return nextPointForIterator;
	}

	
	public Iterator<Point3D> iterator() {
		initializeIterator();
		
		return this;
	}
	
	
	
	
}
