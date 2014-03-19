package fr.unistra.pelican.algorithms.geometric;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.Image;

/**
 * This class performs an image padding, i.e. it enlarges the image with null or
 * border values
 * 
 * 
 * 
 * @author Lefevre, Perret
 */

public class Padding extends Algorithm {

	/**
	 * The input image
	 */
	public Image input;

	/**
	 * The new value for X dimension, -1 to keep the original one
	 */
	public Integer rx;

	/**
	 * The new value for Y dimension, -1 to keep the original one
	 */
	public Integer ry;

	/**
	 * The new value for Z dimension, -1 to keep the original one
	 */
	public Integer rz;

	/**
	 * The new value for T dimension, -1 to keep the original one
	 */
	public Integer rt;

	/**
	 * The new value for B dimension, -1 to keep the original one
	 */
	public Integer rb;

	/**
	 * The (positive) shift for X dimension
	 */
	public Integer dx = 0;

	/**
	 * The (positive) shift for Y dimension
	 */
	public Integer dy = 0;

	/**
	 * The (positive) shift for Z dimension
	 */
	public Integer dz = 0;

	/**
	 * The (positive) shift for T dimension
	 */
	public Integer dt = 0;

	/**
	 * The (positive) shift for B dimension
	 */
	public Integer db = 0;

	/**
	 * The method used in the padding process
	 */
	public Integer paddingMethod;

	/**
	 * The output image
	 */
	public Image output;

	/**
	 * Constant representing the null padding: fill missing values by 0
	 */
	public final static int NULL = 0;

	/**
	 * Constant representing the null padding: fill missing values by border
	 * ones
	 */
	public final static int BORDERS = 1;
	
	/**
	 * Constant representing the null padding: fill missing values by mirroring
	 * ones
	 */
	public final static int MIRROR = 2;

	/**
	 * Default constructor
	 */
	public Padding() {
		super.inputs = "input,rx,ry,rz,rt,rb,paddingMethod";
		super.options = "dx,dy,dz,dt,db";
		super.outputs = "output";
		
	}

	/**
	 * performs an image padding, i.e. it enlarges the image with null or border
	 * values
	 * 
	 * @param input
	 *            The input image
	 * @param rx
	 *            The new value for X dimension, -1 to keep the original one
	 * @param ry
	 *            The new value for Y dimension, -1 to keep the original one
	 * @param rz
	 *            The new value for Z dimension, -1 to keep the original one
	 * @param rt
	 *            The new value for T dimension, -1 to keep the original one
	 * @param rb
	 *            The new value for B dimension, -1 to keep the original one
	 * @param paddingMethod
	 *            The method used in the padding process: NULL, BORDERS
	 * @return The output image
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Image> T exec(T input, int rx, int ry, int rz, int rt,
			int rb, int paddingMethod) {
		return (T) new Padding().process(input, rx, ry, rz, rt, rb,
				paddingMethod);
	}

	/**
	 * performs an image padding, i.e. it enlarges the image with null or border
	 * values
	 * 
	 * @param input
	 *            The input image
	 * @param rx
	 *            The new value for X dimension, -1 to keep the original one
	 * @param ry
	 *            The new value for Y dimension, -1 to keep the original one
	 * @param rz
	 *            The new value for Z dimension, -1 to keep the original one
	 * @param rt
	 *            The new value for T dimension, -1 to keep the original one
	 * @param rb
	 *            The new value for B dimension, -1 to keep the original one
	 * @param paddingMethod
	 *            The method used in the padding process: NULL, BORDERS
	 * @param dx
	 *            The (positive) shift for X dimension
	 * @param dy
	 *            The (positive) shift for Y dimension
	 * @param dz
	 *            The (positive) shift for Z dimension
	 * @param dt
	 *            The (positive) shift for B dimension
	 * @param db
	 *            The (positive) shift for T dimension
	 * @return The output image
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Image> T exec(T input, int rx, int ry, int rz, int rt,
			int rb, int paddingMethod, int dx, int dy, int dz, int dt, int db) {
		return (T) new Padding().process(input, rx, ry, rz, rt, rb,
				paddingMethod, dx, dy, dz, dt, db);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() {
		int xdim = input.getXDim();
		int ydim = input.getYDim();
		int zdim = input.getZDim();
		int tdim = input.getTDim();
		int bdim = input.getBDim();
		if (rx == -1)
			rx = input.getXDim();
		if (ry == -1)
			ry = input.getYDim();
		if (rz == -1)
			rz = input.getZDim();
		if (rt == -1)
			rt = input.getTDim();
		if (rb == -1)
			rb = input.getBDim();
		output = input.newInstance(rx, ry, rz, rt, rb);
		output.copyAttributes(input);
		if (paddingMethod == NULL) {
			for (int z = 0; z < rz; z++)
				for (int t = 0; t < rt; t++)
					for (int b = 0; b < rb; b++)
						for (int y = 0; y < ry; y++)
							for (int x = 0; x < rx; x++)
							 {
								if (x < xdim + dx && y < ydim +dy && z < zdim + dz
										&& t < tdim + dt && b < bdim + db && x >= dx
										&& y >= dy && z >= dz && t >= dt
										&& b >= db)
									output.setPixelDouble(x, y, z
											, t , b , input
											.getPixelDouble(x - dx, y - dy, z -dz , t - dt , b - db));
								else
									output.setPixelDouble(x, y, z, t, b, 0);
							}
		} else if (paddingMethod == BORDERS) {
			int xx, yy, zz, tt, bb;
			for (int z = 0; z < rz; z++)
				for (int t = 0; t < rt; t++)
					for (int b = 0; b < rb; b++)
						for (int y = 0; y < ry; y++)
							for (int x = 0; x < rx; x++)
							 {
								if (x < xdim + dx && y < ydim +dy && z < zdim + dz
										&& t < tdim + dt && b < bdim + db && x >= dx
										&& y >= dy && z >= dz && t >= dt
										&& b >= db)
									output.setPixelDouble(x, y, z
											, t , b , input
											.getPixelDouble(x - dx, y - dy, z -dz , t - dt , b - db));
								else {
									if (x < dx)
										xx = 0;
									else
										xx = Math.min(xdim - 1, x);
									if (y < dy)
										yy = 0;
									else
										yy = Math.min(ydim - 1, y);
									if (z < dz)
										zz = 0;
									else
										zz = Math.min(zdim - 1, z);
									if (t < dt)
										tt = 0;
									else
										tt = Math.min(tdim - 1, t);
									if (b < db)
										bb = 0;
									else
										bb = Math.min(bdim - 1, b);
									output
											.setPixelDouble(x, y, z, t, b,
													input.getPixelDouble(xx,
															yy, zz, tt, bb));
								}
							}
		}else if (paddingMethod == MIRROR) {
			int xx, yy, zz, tt, bb;
			for (int z = 0; z < rz; z++)
				for (int t = 0; t < rt; t++)
					for (int b = 0; b < rb; b++)
						for (int y = 0; y < ry; y++)
							for (int x = 0; x < rx; x++)
							 {
								if (x < xdim + dx && y < ydim +dy && z < zdim + dz
										&& t < tdim + dt && b < bdim + db && x >= dx
										&& y >= dy && z >= dz && t >= dt
										&& b >= db)
									output.setPixelDouble(x, y, z
											, t , b , input
											.getPixelDouble(x - dx, y - dy, z -dz , t - dt , b - db));
								else {
									if (x < dx) {
										if (((dx - x - 1) / xdim) % 2 == 0)

											xx = (dx - x - 1) % xdim;
										// System.out.println("x " +x + "xx "
										// +xx );

										else
											xx = xdim - (dx - x) % xdim - 1;

									} else if (x >= dx + xdim) {
										if (((x - dx - xdim) / xdim) % 2 == 0)

											xx = xdim - (x - dx - xdim) % xdim
													- 1;

										else
											xx = (x - dx - xdim) % xdim;

									} else {
										xx = x - dx;
									}
									if (y < dy) {
										if (((dy - y - 1) / ydim) % 2 == 0)
											yy = (dy - y - 1) % ydim;
										else
											yy = ydim - (dy - y) % ydim - 1;
									} else if (y >= dy + ydim) {
										if (((y - dy - ydim) / ydim) % 2 == 0)
											yy = ydim - (y - dy - ydim) % ydim
													- 1;
										else
											yy = (y - dy - ydim) % ydim;
									} else
										yy = y - dy;
									if (z < dz) {
										if (((dz - z - 1) / zdim) % 2 == 0)
											zz = (dz - z - 1) % zdim;
										else
											zz = zdim - (dz - z) % zdim - 1;
									} else if (z >= dz + zdim) {
										if (((z - dz - zdim) / zdim) % 2 == 0)
											zz = zdim - (z - dz - zdim) % zdim
													- 1;
										else
											zz = (z - dz - zdim) % zdim;
									} else
										zz = z - dz;
									if (t < dt) {
										if (((dt - t - 1) / tdim) % 2 == 0)
											tt = (dt - t - 1) % tdim;
										else
											tt = tdim - (dt - t) % tdim - 1;
									} else if (t >= dt + tdim) {
										if (((t - dt - tdim) / tdim) % 2 == 0)
											tt = tdim - (t - dt - tdim) % tdim
													- 1;
										else
											tt = (t - dt - tdim) % tdim;
									} else
										tt = t - dt;
									if (b < db) {
										if (((db - b - 1) / bdim) % 2 == 0)
											bb = (db - b - 1) % bdim;
										else
											bb = bdim - (db - b) % bdim - 1;
									} else if (b >= db + bdim) {
										if (((b - db - bdim) / bdim) % 2 == 0)
											bb = bdim - (b - db - bdim) % bdim
													- 1;
										else
											bb = (b - db - bdim) % bdim;
									} else
										bb = b - db;
									
									output
											.setPixelDouble(x, y, z, t, b,
													input.getPixelDouble(xx,
															yy, zz, tt, bb));
								}
							}
		}

	}
	
	
}
