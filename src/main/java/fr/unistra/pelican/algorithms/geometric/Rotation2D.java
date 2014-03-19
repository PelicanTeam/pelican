package fr.unistra.pelican.algorithms.geometric;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.InvalidParameterException;
import fr.unistra.pelican.util.Tools;

/**
 * This class performs a 2D rotation of variable angle with a given
 * interpolation method
 * 
 * The methods available are NOINTERPOLATION, BILINEARINTERPOLATION and BICUBICINTERPOLATION
 * 
 * @author Jonathan Weber, Lefevre, Benjamin Perret (BICUBICINTERPOLATION)
 */
public class Rotation2D extends Algorithm {

	/**
	 * The input image
	 */
	public Image inputImage;

	/**
	 * The rotation angle (in degrees)
	 */
	public double angle;

	/**
	 * The interpolation method
	 */
	public int interpolation;

	/**
	 * The output image
	 */
	public Image outputImage;

	/**
	 * Constant representing the NOINTERPOLATION method
	 */
	public static final int NOINTERPOLATION = 0;

	/**
	 * Constant representing the BILINEARINTERPOLATION method
	 */
	public static final int BILINEARINTERPOLATION = 1;

	/**
	 * Constant representing the BICUBICINTERPOLATION method
	 */
	public static final int BICUBICINTERPOLATION = 2;
	
	/**
	 * Default constructor
	 */
	public Rotation2D() {
		super.inputs = "inputImage,angle,interpolation";
		super.outputs = "outputImage";

	}

	/**
	 * performs a 2D rotation of variable angle with a given interpolation
	 * method
	 * 
	 * @param inputImage
	 *            The input image
	 * @param angledegree
	 *            The rotation angle (in degrees)
	 * @param interpolation
	 *            The interpolation method
	 * @return The output image
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Image> T exec(T inputImage, double angledegree,
			int interpolation) {
		return (T) new Rotation2D().process(inputImage, angledegree,
				interpolation);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() {
		double angleradian = Math.toRadians(angle);
		double xinput = inputImage.getXDim();
		double yinput = inputImage.getYDim();
		double tcos = Math.cos(-angleradian);
		double tsin = Math.sin(-angleradian);
		double atcos = Math.cos(angleradian);
		double atsin = Math.sin(angleradian);
		double newX=xinput * Math.abs(tcos) + yinput*Math.abs(tsin);
		double newY=xinput * Math.abs(tsin) + yinput*Math.abs(tcos);
		int xoutput = (int) Math.ceil(xinput * Math.abs(tcos) + yinput
				* Math.abs(tsin));
		int youtput = (int) Math.ceil(xinput * Math.abs(tsin) + yinput
				* Math.abs(tcos));
		// ugly hack to avoid dimension increases due to numerical errors...
		//if(newX-xoutput < 0.000001)
		//	xoutput--;
		//if(newY-youtput < 0.000001)
		//	youtput--;
		int xm = inputImage.getXDim() / 2;
		int ym = inputImage.getYDim() / 2;
		int xrap = (xoutput - inputImage.getXDim()) / 2;
		int yrap = (youtput - inputImage.getYDim()) / 2;
		outputImage = inputImage.newInstance(xoutput, youtput, inputImage
				.getZDim(), inputImage.getTDim(), inputImage.getBDim());
		outputImage.fill(0.);
		for (int b = 0; b < outputImage.getBDim(); b++)
			for (int t = 0; t < outputImage.getTDim(); t++)
				for (int z = 0; z < outputImage.getZDim(); z++)
					for (int y = -yrap; y < outputImage.getYDim() - yrap; y++)
						for (int x = -xrap; x < outputImage.getXDim() - xrap; x++) {
							switch (interpolation) {
							case Rotation2D.NOINTERPOLATION:
								int xout = (int) Math.round((x - xm) * atcos
										+ (y - ym) * atsin + xm);
								int yout = (int) Math.round(-(x - xm) * atsin
										+ (y - ym) * atcos + ym);
								if (xout >= 0 && xout < inputImage.getXDim()
										&& yout >= 0
										&& yout < inputImage.getYDim()) {
									outputImage.setPixelXYZTBDouble(x + xrap, y
											+ yrap, z, t, b, inputImage
											.getPixelXYZTBDouble(xout, yout, z,
													t, b));
								}
								break;
							case Rotation2D.BILINEARINTERPOLATION:
								double xoutd = ((x - xm) * atcos + (y - ym)
										* atsin + xm);
								double youtd = (-(x - xm) * atsin + (y - ym)
										* atcos + ym);
								if (xoutd >= 0
										&& xoutd < inputImage.getXDim() - 1
										&& youtd >= 0
										&& youtd < inputImage.getYDim() - 1) {
									int xout_floor = (int) Math.floor(xoutd);
									int yout_floor = (int) Math.floor(youtd);
									int xout_ceil = (int) Math.ceil(xoutd);
									int yout_ceil = (int) Math.ceil(youtd);
									double interpolation = 0;
									interpolation += inputImage
											.getPixelXYZTBDouble(xout_floor,
													yout_floor, z, t, b);
									interpolation += inputImage
											.getPixelXYZTBDouble(xout_floor,
													yout_ceil, z, t, b);
									interpolation += inputImage
											.getPixelXYZTBDouble(xout_ceil,
													yout_floor, z, t, b);
									interpolation += inputImage
											.getPixelXYZTBDouble(xout_ceil,
													yout_ceil, z, t, b);
									interpolation = interpolation / 4;
									outputImage.setPixelXYZTBDouble(x + xrap, y
											+ yrap, z, t, b, interpolation);
								}
								break;
							case Rotation2D.BICUBICINTERPOLATION:
								xoutd = ((x - xm) * atcos + (y - ym)
										* atsin + xm);
								youtd = (-(x - xm) * atsin + (y - ym)
										* atcos + ym);
								if (xoutd >= 0
										&& xoutd < inputImage.getXDim() - 1
										&& youtd >= 0
										&& youtd < inputImage.getYDim() - 1) {
									
									double interpolation = Tools.getBiCubicInterpolation(inputImage, xoutd, youtd, z, t, b);
									outputImage.setPixelXYZTBDouble(x + xrap, y
											+ yrap, z, t, b, interpolation);
								}
								break;
							default:
								throw new InvalidParameterException(
										"Bad Argument for interpolation");
							}
						}
	}

}
