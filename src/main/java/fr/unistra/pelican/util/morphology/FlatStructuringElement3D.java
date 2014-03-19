package fr.unistra.pelican.util.morphology;

import fr.unistra.pelican.util.Point3D;
import fr.unistra.pelican.util.Point4D;

import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;

/**
 * Utility class to create images representing structuring elements
 * 
 * @author lefevre
 * 
 */
public class FlatStructuringElement3D {


	/**
	 * Create a cross line of radius radius (plus the pixel at the cross).
	 * 
	 * @param radius
	 * @return
	 */
	public static BooleanImage createCrossFlatStructuringElement(
			int radius) {
		BooleanImage se = new BooleanImage(
				2 * radius + 1, 2 * radius + 1, 2 * radius + 1, 1,1);
		se.resetCenter();
		for (int i = 0; i < 2 * radius + 1; i++) {
			se.setPixelXYZBoolean(i, radius, radius, true);
			se.setPixelXYZBoolean(radius, i, radius, true);
			se.setPixelXYZBoolean(radius, radius, i, true);
		}
		return se;
	}
	
	/**
	 * Create a temporal cross line of radius radius (plus the pixel at the cross).
	 * 
	 * @param radius
	 * @return
	 */
	public static BooleanImage createTemporalCrossFlatStructuringElement(
			int radius) {
		BooleanImage se = new BooleanImage(
				2 * radius + 1, 2 * radius + 1, 1, 2 * radius + 1,1);
		se.resetCenter();
		for (int i = 0; i < 2 * radius + 1; i++) {
			se.setPixelXYTBoolean(i, radius, radius, true);
			se.setPixelXYTBoolean(radius, i, radius, true);
			se.setPixelXYTBoolean(radius, radius, i, true);
		}
		return se;
	}
	
	/**
	 * Create a strange Structuring Element forme by two cones (with square base, strange, I said !)
	 * with base in the middle temporal frame of the SE.
	 *  
	 * @param baseWidth
	 * @return the strange SE
	 */
	public static BooleanImage createTwoTemporalConesWithBaseInMiddleFrame(int baseWidth)
	{
		BooleanImage se = new BooleanImage(baseWidth, baseWidth, 1, baseWidth + 1, 1);
		se.resetCenter();
		int xDim = se.getXDim();
		int yDim = se.getYDim();
		int tDim = se.getTDim();
		int xCenter = se.getCenter().x;
		int yCenter = se.getCenter().y;
		int tCenter = se.getCenter().t;
		double maxDistFromCenter = Math.sqrt(xCenter*xCenter+yCenter*yCenter);
		//Base filling
		for(int y=0; y<yDim; y++)
			for(int x=0; x<yDim; x++)
			{
				se.setPixelXYTBoolean(x, y, baseWidth/2, true);
			}
		//Cone filling
		for(int t=0;t<baseWidth/2;t++)
			for(int y=0; y<yDim; y++)
				for(int x=0; x<xDim; x++)
				{
					double distFromSESpatialCenterNormalized = Math.sqrt((x-xCenter)*(x-xCenter)+(y-yCenter)*(y-yCenter))/maxDistFromCenter;
					double distTemporalFromCenterNormalized = 1-((double)tCenter-(double)t)/(double)tCenter; 
					if(distFromSESpatialCenterNormalized<=distTemporalFromCenterNormalized)
					{
						se.setPixelXYTBoolean(x, y, t, true);
						se.setPixelXYTBoolean(x, y, tDim-1-t, true);
					}
				}
		
		return se;
	}

	/**
	 * Create a circle of radius radius (plus the pixel at the cross).
	 * 
	 * @param radius
	 * @return
	 */
	public static BooleanImage createCircleFlatStructuringElement(
			int radius) {
		BooleanImage se = new BooleanImage(
				2 * radius + 1, 2 * radius + 1, 2 * radius + 1, 1,1);
		se.resetCenter();
		for (int i = 0; i < 2 * radius + 1; i++) {
			for (int j = 0; j < 2 * radius + 1; j++)
				for (int k = 0; k < 2 * radius + 1; k++) {
					if (Math
							.sqrt(Math.pow(i - radius, 2)
									+ Math.pow(j - radius, 2)
									+ Math.pow(k - radius, 2)) <= radius + 0.000001)
						se.setPixelXYZBoolean(i, j, k, true);
				}
		}
		return se;
	}

	/**
	 * @return The transpose of the structuring element.
	 */
public static BooleanImage transpose(BooleanImage se) {
		BooleanImage res = new BooleanImage(se.ydim, se.xdim, se.zdim,
			se.tdim, se.bdim);
		for (int k = 0; k < res.zdim; k++)
			for (int j = 0; j < res.ydim; j++)
			for (int i = 0; i < res.xdim; i++)
				res.setPixelXYZBoolean(i, j, k,se.getPixelXYZBoolean(k,j, i));
		Point4D p = se.getCenter();
		res.setCenter(new Point4D(p.y, p.x, p.z, p.t));
		return res;
	}

	
	/**
	 * Create a square of edge's length size. Center is at (size/2, size/2).
	 * 
	 * @param size
	 * @return a square SE
	 */
	public static BooleanImage createSquareFlatStructuringElement(
			int size) {
		BooleanImage se = new BooleanImage(size, size,
				size, 1,1);
		se.fill(true);
		return se;
	}
	
	/**
	 * Create a temporal structuring element for 10-connectivity
	 * 
	 * @param size
	 * @return a square SE
	 */
	public static BooleanImage createTemporal10ConnectivityFlatStructuringElement()
	{
		BooleanImage se = new BooleanImage(3, 3, 1, 3,1);
		se.fill(false);
		se.resetCenter();
		se.setPixelXYTBoolean(1, 1, 0, true);
		se.setPixelXYTBoolean(1, 1, 2, true);
		se.setPixelXYTBoolean(0, 0, 1, true);
		se.setPixelXYTBoolean(0, 1, 1, true);
		se.setPixelXYTBoolean(0, 2, 1, true);
		se.setPixelXYTBoolean(1, 0, 1, true);
		se.setPixelXYTBoolean(1, 1, 1, true);
		se.setPixelXYTBoolean(1, 2, 1, true);
		se.setPixelXYTBoolean(2, 0, 1, true);
		se.setPixelXYTBoolean(2, 1, 1, true);
		se.setPixelXYTBoolean(2, 2, 1, true);		
		return se;
	}
	
	public static void print(BooleanImage se) {
		for (int k = 0; k < se.zdim; k++) {
			for (int j = 0; j < se.ydim; j++) {
			for (int i = 0; i < se.xdim; i++) {
				if (se.getCenter().x == i && se.getCenter().y == j && se.getCenter().z == k) {
					System.out.print("+ ");
				} else if (se.getPixelXYZBoolean(i,j,k)) {
					System.out.print("0 ");
				} else {
					System.out.print("  ");
				}
			}
			System.out.println();
			}
			System.out.println("\n---");
		}
	}

}