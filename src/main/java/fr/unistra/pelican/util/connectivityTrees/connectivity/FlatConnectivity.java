/**
 * 
 */
package fr.unistra.pelican.util.connectivityTrees.connectivity;

import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.Point3D;

/**
 * Connectivity is defined by the flat zone of a given image connected in the sens of a trivial connectivity
 * 
 * @author Benjamin Perret
 *
 */
public class FlatConnectivity extends Connectivity3D {

	/**
	 * Image defining flat zones
	 */
	private Image flatAreas;
	
	/**
	 * Base connectivity
	 */
	private TrivialConnectivity neighbourhoodSystem;

	
	private double actualValue;
	
	private Point3D nextOne= new Point3D();
	
	public void setCurrentPoint(int x, int y, int z)
	{
		super.setCurrentPoint(x, y,z);
		actualValue=flatAreas.getPixelXYZDouble(x, y,z);
	}
	
	/**
	 * 
	 */
	public FlatConnectivity(Image flatAreas, TrivialConnectivity neighbourhoodSystem) {
		this.flatAreas=flatAreas;
		this.neighbourhoodSystem=neighbourhoodSystem;
	}

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.CC.Connectivity.Connectivity2D#computeNextPointForIterator()
	 */
	@Override
	protected void computeNextPointForIterator() {
		//int c=0;
		nextPointForIterator.setLocation(nextOne);
		while (neighbourhoodSystem.hasNext())
		{
			Point3D p=neighbourhoodSystem.next();
			//System.out.println("neib " +p);
			if(p.x>=0 && p.y>=0 && p.x < flatAreas.xdim && p.y<flatAreas.ydim && p.z >=0 && p.z < flatAreas.zdim && flatAreas.getPixelXYZDouble(p.x, p.y,p.z) == actualValue)
			{
			//	nNumber=c;
			//	System.out.println("neib " +p +" ok");
				hasMoreNeigbours=true;
				nextOne.setLocation(p);
				return;
			}
			//c++;
		}
		hasMoreNeigbours= false;

	}

	
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.CC.Connectivity.Connectivity2D#getConnectedNeighbours(int, int)
	 */
	@Override
	public Point3D[] getConnectedNeighbours(int x, int y, int z) {
		int c = 0;
		neighbourhoodSystem.setCurrentPoint(x,y);
		for (Point3D p : neighbourhoodSystem) {
			if (p.x >= 0 && p.y >= 0 && p.x < flatAreas.xdim
					&& p.y < flatAreas.ydim
					&& p.z >=0 && p.z < flatAreas.zdim && flatAreas.getPixelXYZDouble(p.x, p.y,p.z) == actualValue) {
				c++;
			}

		}

		Point3D [] list = new Point3D[c];
		c=0;
		for (Point3D p : neighbourhoodSystem) {
			if (p.x >= 0 && p.y >= 0 && p.x < flatAreas.xdim
					&& p.y < flatAreas.ydim
					&& p.z >=0 && p.z < flatAreas.zdim && flatAreas.getPixelXYZDouble(p.x, p.y,p.z) == actualValue) {
				list[c]=new Point3D(p.x,p.y,p.z);
				c++;
				
			}

		}
		return list;
	}

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.CC.Connectivity.Connectivity2D#initializeIterator()
	 */
	@Override
	protected void initializeIterator() {
		neighbourhoodSystem.setCurrentPoint(currentX, currentY, currentZ);
		neighbourhoodSystem.initializeIterator();
		computeNextPointForIterator();
		//
	}

}
