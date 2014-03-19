/**
 * 
 */
package fr.unistra.pelican.util.connectivityTrees.connectivity;

import java.awt.Point;

import fr.unistra.pelican.util.Point3D;

/**
 * A trivial connectivity is simply defined by a set of points surrounding the actual point. 
 * It does not depend on image pixel values.
 * 
 * It includes 4-8-... neighborhood connectivity, use static methods to generate them.
 * 
 * @author Benjamin Perret
 *
 */
public class TrivialConnectivity extends Connectivity3D {

	public static TrivialConnectivity getHorizontalNeighbourhood(){
		Point3D [] neigbours={new Point3D(-1,0),new Point3D(1,0)};
		return new TrivialConnectivity(neigbours );
	}
	
	public static TrivialConnectivity getVerticalNeighbourhood(){
		Point3D [] neigbours={new Point3D(0,-1),new Point3D(0,1)};
		return new TrivialConnectivity(neigbours );
	}
	
	public static TrivialConnectivity getFourNeighbourhood(){
		Point3D [] neigbours={new Point3D(0,-1),new Point3D(-1,0),new Point3D(1,0),new Point3D(0,1)};
		return new TrivialConnectivity(neigbours );
	}
	
	public static TrivialConnectivity getHeightNeighbourhood(){
		Point3D [] neigbours={new Point3D(-1,-1),new Point3D(0,-1),new Point3D(1,-1),new Point3D(-1,0),new Point3D(1,0),new Point3D(-1,1),new Point3D(0,1),new Point3D(1,1)};
		return new TrivialConnectivity(neigbours );
	}
	
	private Point3D [] neigbours=null;

	
	public TrivialConnectivity(Point3D [] neigbours)
	{
		this.neigbours=neigbours;
	}
	
	private int nNumber=0;
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.CC.Connectivity.Connectivity2D#computeNextPointForIterator()
	 */
	@Override
	protected void computeNextPointForIterator() {
		nextPointForIterator.x=currentX+neigbours[nNumber].x;
		nextPointForIterator.y=currentY+neigbours[nNumber].y;
		nextPointForIterator.z=currentZ+neigbours[nNumber].z;
		//System.out.println("next point " + nextPointForIterator + "   " +nNumber + "  " + currentX + "  " + currentY);
		nNumber++;
		if(nNumber==neigbours.length)
			hasMoreNeigbours=false;
	}

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.CC.Connectivity.Connectivity2D#getConnectedNeighbours(int, int)
	 */
	@Override
	public Point3D[] getConnectedNeighbours(int x, int y, int z) {
		Point3D [] list = new Point3D[neigbours.length];
		for(int i=0;i<neigbours.length;i++)
			list[i]=new Point3D(x+neigbours[i].x,y+neigbours[i].y,z+neigbours[i].z);
		return list;
	}

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.CC.Connectivity.Connectivity2D#initializeIterator()
	 */
	@Override
	protected void initializeIterator() {
		nNumber=0;
		hasMoreNeigbours=true;
		
		
	}

}
