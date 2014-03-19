package fr.unistra.pelican.algorithms.geometric;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;

/**
 * This class performs a spatial resampling of the images, and work in any
 * dimensions
 * 
 * @author Lefevre
 * 
 * TODO: améliorer la gestion des bords
 */
public class ResamplingByValue extends Algorithm {

	/**
	 * The input image
	 */
	public Image input;

	/**
	 * The resampling value on the dimension X
	 */
	public Integer vx;

	/**
	 * The resampling value on the dimension Y
	 */
	public Integer vy;
	
	/**
	 * The resampling value on the dimension Z
	 */
	public Integer vz;
	
	/**
	 * The resampling value on the dimension T
	 */
	public Integer vt;
	
	/**
	 * The resampling value on the dimension B
	 */
	public Integer vb;

	/**
	 * The resampling method : NEAREST, BILINEAR
	 */
	public Integer resamplingMethod;

	/**
	 * The output image
	 */
	public Image output;

	/**
	 * Constant to represent NEAREST method: Considers one pixel every X
	 */
	public final static int NEAREST = 0;

	/**
	 * Constant to represent BILINEAR method: Considers the average of X pixels
	 */
	public final static int BILINEAR = 1;
	
	/**
	 * Default constructor
	 */
	public ResamplingByValue() {
		super.inputs = "input,vx,vy,vz,vt,vb,resamplingMethod";
		super.outputs = "output";
		
	}
	
	/**
	 * Performs a spatial resampling of the images and work in any dimensions
	 * @param input The input image
	 * @param vx The resampling value on the dimension X
	 * @param vy The resampling value on the dimension Y
	 * @param vz The resampling value on the dimension Z
	 * @param vt The resampling value on the dimension T
	 * @param vb The resampling value on the dimension B
	 * @param resamplingMethod The resampling method : NEAREST, BILINEAR
	 * @return The output image
	 */
	public static Image exec(Image input, Integer vx, Integer vy, Integer vz, Integer vt, Integer vb, int resamplingMethod) {
		return (Image) new ResamplingByValue().process(input,vx,vy,vz,vt,vb,resamplingMethod);
	}
	
	@Override
	public void launch() throws AlgorithmException {
		double xdim = input.getXDim();
		double ydim = input.getYDim();
		double zdim = input.getZDim();
		double tdim = input.getTDim();
		double bdim = input.getBDim();
		double rx = vx.doubleValue()/xdim;
		double ry = vy.doubleValue()/ydim;
		double rz = vz.doubleValue()/zdim;
		double rt = vt.doubleValue()/tdim;
		double rb = vb.doubleValue()/bdim;
		output = input.newInstance(vx, vy, vz, vt, vb);
		output.copyAttributes(input);
		if (resamplingMethod == NEAREST) {
			if(input instanceof IntegerImage)
			{
				for (int z = 0; z < vz; z++)
					for (int t = 0; t < vt; t++)
						for (int b = 0; b < vb; b++)
							for (int x = 0; x < vx; x++)
								for (int y = 0; y < vy; y++)
									output.setPixelInt(x, y, z, t, b, input
											.getPixelInt((int) (x / rx),
													(int) (y / ry),
													(int) (z / rz),
													(int) (t / rt),
													(int) (b / rb)));
			} else
			{
				for (int z = 0; z < vz; z++)
					for (int t = 0; t < vt; t++)
						for (int b = 0; b < vb; b++)
							for (int x = 0; x < vx; x++)
								for (int y = 0; y < vy; y++)
									output.setPixelDouble(x, y, z, t, b, input
											.getPixelDouble((int) (x / rx),
													(int) (y / ry),
													(int) (z / rz),
													(int) (t / rt),
													(int) (b / rb)));
			}
		} else if (resamplingMethod == BILINEAR) {
			double sum, val, weight, norm;
			double px, py, pz, pt, pb;
			int mx, my, mz, mt, mb;
			for (int z = 0; z < vz; z++)
				for (int t = 0; t < vt; t++)
					for (int b = 0; b < vb; b++)
						for (int x = 0; x < vx; x++)
							for (int y = 0; y < vy; y++) {
								// cas particulier pour les pixels exacts
								if (x % rx == 0 && y % ry == 0 && z % rz == 0
										&& t % rt == 0 && b % rb == 0) {
									output.setPixelDouble(x, y, z, t, b, input
											.getPixelDouble((int) (x / rx),
													(int) (y / ry),
													(int) (z / rz),
													(int) (t / rt),
													(int) (b / rb)));
									continue;
								}
								sum = 0;
								norm = 0;
								px = x / rx;
								py = y / ry;
								pz = z / rz;
								pt = t / rt;
								pb = b / rb;
								// TODO: améliorer la gestion des bords
								mx = (rx == 1 || px >= xdim - 1 || x % rx == 0 ? 1
										: 2);
								my = (ry == 1 || py >= ydim - 1 || y % ry == 0 ? 1
										: 2);
								mz = (rz == 1 || pz >= zdim - 1 || z % rz == 0 ? 1
										: 2);
								mt = (rt == 1 || pt >= tdim - 1 || t % rt == 0 ? 1
										: 2);
								mb = (rb == 1 || pb >= bdim - 1 || b % rb == 0 ? 1
										: 2);
								for (int dt = 0; dt < mt; dt++)
									for (int dz = 0; dz < mz; dz++)
										for (int db = 0; db < mb; db++)
											for (int dy = 0; dy < my; dy++)
												for (int dx = 0; dx < mx; dx++) {
													val = input.getPixelDouble(
															(int) px + dx,
															(int) py + dy,
															(int) pz + dz,
															(int) pt + dt,
															(int) pb + db);
													weight = Math.pow(px-(int) (px + dx),2)
															+ Math.pow(py-(int) (py + dy),2)
															+ Math.pow(pz-(int) (pz + dz),2)
															+ Math.pow(pt- (int) (pt + dt),2)
															+ Math.pow(px- (int) (pb + db),2);
													sum += val / weight;
													norm += 1 / weight;
												}
								output
										.setPixelDouble(x, y, z, t, b, sum
												/ norm);
							}
		}
	}

}
