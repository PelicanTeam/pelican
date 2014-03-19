package fr.unistra.pelican;

import java.io.Serializable;
import java.util.Arrays;

/**
 * This class represents a double valued image Range for a double pixel is
 * [0.0; 1.0]
 * 
 * @version 1.0
 */

public class DoubleImage extends Image implements Serializable {

	/**
	 * Pixel data array
	 */

	protected double[] pixels;

	/**
	 * Serial version ID
	 */

	private static final long serialVersionUID = 6L;

	/**
	 * Constructs a DoubleImage
	 */
	protected DoubleImage(){
		super();
	}
	
	/**
	 * Constructs a DoubleImage identical to the given argument
	 * 
	 * @param image
	 *          DoubleImage to copy
	 */

	public DoubleImage(DoubleImage image) {
		super(image);
		this.pixels = (double[]) image.pixels.clone();
	}

	/**
	 * Constructs a DoubleImage identical to the given argument
	 * 
	 * @param image
	 *          Image to copy
	 */
	public DoubleImage(Image image) {
		this(image, true);
	}

	/**
	 * Constructs a DoubleImage identical to the given argument
	 * 
	 * @param image
	 *          Image to copy
	 * @param copyData
	 *          if and only if it is set to true are the pixels copied
	 */
	public DoubleImage(Image image, boolean copyData) {
		super(image);
		this.pixels = new double[image.getXDim() * image.getYDim()
			* image.getZDim() * image.getTDim() * image.getBDim()];
		if (copyData == true)
			for (int i = 0; i < pixels.length; i++)
				setPixelDouble(i, image.getPixelDouble(i));
	}

	/**
	 * Constructs a ByteImage from the given argument. The pixels are copied if
	 * and only of ''copy'' is set to true.
	 * 
	 * @param image
	 *          ByteImage to copy
	 * @param copy
	 *          if and only if it is set to true are the pixels copied
	 */
	public DoubleImage(DoubleImage image, boolean copy) {
		super(image);

		if (copy == true)
			this.pixels = (double[]) image.pixels.clone();
		else
			this.pixels = new double[image.getXDim() * image.getYDim()
				* image.getZDim() * image.getTDim() * image.getBDim()];
	}

	/**
	 * Constructs a DoubleImage with the given dimensions
	 * 
	 * @param xdim
	 *          the horizontal dimension
	 * @param ydim
	 *          the vertical dimension
	 * @param zdim
	 *          the depth
	 * @param tdim
	 *          the frame number
	 * @param bdim
	 *          the channel number
	 */

	public DoubleImage(int xdim, int ydim, int zdim, int tdim, int bdim) {
		super(xdim, ydim, zdim, tdim, bdim);
		this.pixels = new double[xdim * ydim * zdim * tdim * bdim];
	}

	/**
	 * Creates a new instance of DoubleImage
	 * 
	 * @param xdim
	 *          the horizontal dimension
	 * @param ydim
	 *          the vertical dimension
	 * @param zdim
	 *          the depth
	 * @param tdim
	 *          the frame number
	 * @param bdim
	 *          the channel number
	 */
	public Image newInstance(int xdim, int ydim, int zdim, int tdim, int bdim) {
		return new DoubleImage(xdim, ydim, zdim, tdim, bdim);
	}

	/**
	 * Sets all the pixel values to the given value
	 * 
	 * @param b
	 *          Desired value for the pixels
	 */

	public void fill(double b) {
		Arrays.fill(pixels, b);
	}

	/**
	 * Sets all the pixel values of the given band to the given value
	 * 
	 * @param band
	 * @param b
	 *          Desired value for the pixels
	 */

	public void fill(int band, double b) {
		for (int i = band; i < size(); i += bdim) {
			pixels[i] = b;
		}
	}

	/**
	 * Gets a copy of the pixel array
	 * 
	 * @return a copy of the pixel array
	 */

	public double[] getPixels() {
		return (double[]) pixels.clone();
	}

	/**
	 * Gets  the pixel array (not a copy !), use it at your own risk.
	 * 
	 *  Note that you will probably be cursed by the daemon of border effect for 7 generations ! ahahahah!
	 * 
	 * 
	 * @return the pixel array
	 */

	public double[] getPixelsUnsafe() {
		return pixels;
	}
	
	/**
	 * Sets the pixels to the copy of the given array
	 * 
	 * @param values
	 *          pixel array to copy
	 */

	public void setPixels(double[] values) {
		pixels = (double[]) values.clone();
	}

	/**
	 * Sets the pixels to the given array, no checks are made on array length, 
	 * use it at your own risk.
	 * 
	 *  Note that you will probably be cursed by the daemon of border effect for 7 generations ! ahahahah!
	 * 
	 * @param values
	 *          pixel array to copy
	 */

	public void setPixelsUnsafe(double[] values) {
		pixels =  values;
	}
	
	/**
	 * Compares with the given DoubleImage
	 * 
	 * @param im
	 *          image to compare
	 * @return <code>true</code> if and only if the given image has the same
	 *         pixel values as this image
	 */

	public boolean equals(Image im) {

		if (im==null  || !(im instanceof DoubleImage))
			return false;

		if (!haveSameDimensions(this, im))
			return false;

		int size = size();
		for (int i = 0; i < size; i++) {
			if (im.getPixelDouble(i) != getPixelDouble(i))
				return false;
		}

		return true;
	}

	/**
	 * Computes the number of different pixels divided by the total size
	 * 
	 * @param im
	 *          image to compare
	 * @return the number of different pixels divided by the total size or -1.0 if
	 *         the images have different dimensions
	 */

	public double nbDifferentPixels(DoubleImage im) {
		double ctr = 0.0;
		int size = size();

		if (!haveSameDimensions(this, im))
			return -1.0;

		for (int i = 0; i < size; i++) {
			if ((im.getPixelDouble(i) != getPixelDouble(i)))
				ctr++;
		}

		return ctr / size;
	}

	/**
	 * Scales all channels and frames independently to [0,1] for visualisation
	 * purposes. As the relative contrast gets messed up, even if the channel in
	 * question is already in [0,1] it is stretched all the same.
	 * 
	 * @return the scaled image
	 */
	public DoubleImage scaleToZeroOneIndep() {
		DoubleImage d = (DoubleImage) this.copyImage(false);

		// the scaling must be realized on XYZ volumes for every channel/frame
		// separately
		for (int b = 0; b < bdim; b++)
			for (int t = 0; t < tdim; t++) {
				double min = Double.MAX_VALUE;
				double max = Double.NEGATIVE_INFINITY;

				// get the extrema
				for (int z = 0; z < zdim; z++)
					for (int x = 0; x < xdim; x++)
						for (int y = 0; y < ydim; y++) {
							double tmp = this.getPixelXYZTBDouble(x, y, z, t, b);
							if (min > tmp)
								min = tmp;
							if (max < tmp)
								max = tmp;
						}

				double dist = max - min;

				if (max != min) {
					for (int z = 0; z < zdim; z++)
						for (int x = 0; x < xdim; x++)
							for (int y = 0; y < ydim; y++) {
								double tmp = this.getPixelXYZTBDouble(x, y, z, t, b);
								tmp = (tmp - min) / dist;
								d.setPixelXYZTBDouble(x, y, z, t, b, tmp);
							}

				} else {
					for (int z = 0; z < zdim; z++)
						for (int x = 0; x < xdim; x++)
							for (int y = 0; y < ydim; y++)
								d.setPixelXYZTBDouble(x, y, z, t, b, min);
				}
			}

		return d;
	}

	/**
	 * Scales all values to [0,1] for visualisation purposes.
	 * 
	 * @return the scaled image
	 */
	public DoubleImage scaleToZeroOne() {
		DoubleImage d = (DoubleImage) this.copyImage(false);

		double min = Double.MAX_VALUE;
		double max = Double.NEGATIVE_INFINITY;

		for (int p = 0; p < size(); p++) {
			double tmp = this.getPixelDouble(p);
			if (min > tmp)
				min = tmp;
			if (max < tmp)
				max = tmp;
		}
		double dist = max - min;
		if (max != min) {
			for (int p = 0; p < size(); p++) {
				double tmp = this.getPixelDouble(p);
				tmp = (tmp - min) / dist;
				d.setPixelDouble(p, tmp);
			}
		} else
			System.err.println("Scaling error");
		return d;
	}

	/**
	 * Slides all values to [0,inf] independently for each channel...or should
	 * it???
	 * 
	 * @param img
	 *          input image
	 * @return the resulting image
	 */
	public static DoubleImage slide(DoubleImage img) {
		DoubleImage output = null;

		int bdim = img.getBDim();
		int tdim = img.getTDim();
		int zdim = img.getZDim();
		int xdim = img.getXDim();
		int ydim = img.getYDim();

		// the sliding must be realized for every channel/frame separately
		for (int b = 0; b < bdim; b++) {
			for (int t = 0; t < tdim; t++) {
				for (int z = 0; z < zdim; z++) {
					double min = Double.MAX_VALUE;

					// get the minimum
					for (int x = 0; x < xdim; x++) {
						for (int y = 0; y < ydim; y++) {
							double tmp = img.getPixelXYZTBDouble(x, y, z, t, b);
							if (min > tmp)
								min = tmp;
						}
					}

					if (min < 0.0) {
						output = (DoubleImage) img.copyImage(false);
						for (int x = 0; x < xdim; x++)
							for (int y = 0; y < ydim; y++) {
								double d = img.getPixelXYZTBDouble(x, y, z, t, b);
								output.setPixelXYZTBDouble(x, y, z, t, b, d - min);
							}
					} else
						output = (DoubleImage) img.copyImage(true);
				}
			}
		}

		return output;
	}

	/**
	 * Replaces every pixel with its absolute value
	 * 
	 * @param img
	 *          input image
	 * @return the resulting image
	 */
	public static DoubleImage abs(DoubleImage img) {
		DoubleImage output = (DoubleImage) img.copyImage(false);

		int size = img.size();

		for (int i = 0; i < size; i++)
			output.setPixelDouble(i, Math.abs(img.getPixelDouble(i)));

		return output;
	}

	@Override
	public DoubleImage copyImage(boolean copyData) {
		return new DoubleImage(this, copyData);
	}

	@Override
	public double getPixelDouble(int loc) {
		return pixels[loc];
	}

	@Override
	public int getPixelInt(int loc) {
		return doubleToInt(pixels[loc]);
		//return (int) (IntegerImage.doubleToInt * (pixels[loc] - 0.5));
	}

	@Override
	public int getPixelByte(int loc) {
		return doubleToUnsignedByte(pixels[loc]);
		//return (int) Math.round(ByteImage.doubleToByte * pixels[loc]);
	}

	@Override
	public boolean getPixelBoolean(int loc) {
		return doubleToBoolean(pixels[loc]);
		//return (pixels[loc] >= 0.5) ? true : false;
	}

	@Override
	public void setPixelDouble(int loc, double value) {
		pixels[loc] = value;
	}

	@Override
	public void setPixelInt(int loc, int value) {
		pixels[loc] = intToDouble(value);
		//pixels[loc] = intToDouble * (double) value + intToDoubleOffset;
	}

	@Override
	public void setPixelByte(int loc, int value) {
		pixels[loc] = unsignedByteToDouble(value);
		//pixels[loc] = byteToDouble * (double) value;

	}

	@Override
	public void setPixelBoolean(int loc, boolean value) {
		pixels[loc] = booleanToDouble(value);
		//pixels[loc] = value ? 1.0 : 0.0;
	}

	@Override
	public int size() {
		return pixels.length;
	}

	@Override
	public void setPixel(Image input, int x1, int y1, int z1, int t1, int b1,
		int x2, int y2, int z2, int t2, int b2) {
		this.setPixelDouble(x1, y1, z1, t1, b1, input.getPixelDouble(x2, y2, z2,
			t2, b2));
	}
	
	/**
	 * Computes the maximum value of the image
	 * @return
	 * 		a double representation of the maximum
	 */
	public double maximum() {
		double val = Double.NEGATIVE_INFINITY;
		for (int p = 0; p < size(); p++)
			if (getPixelDouble(p) > val)
				val = getPixelDouble(p);
		return val;
	}
	
	/**
	 * Computes the minimum value of the image
	 * @return
	 * 		a double representation of the minimum
	 */
	public double minimum() {
		double val = Double.MAX_VALUE;
		for (int p = 0; p < size(); p++)
			if (getPixelDouble(p) < val)
				val = getPixelDouble(p);
		return val;
	}
	
	/**
	 * Computes the maximum value of the image in the specified band
	 * @param band
	 * 		Band.
	 * @return
	 * 		the double representation of the maximum in the specified band
	 */
	public double maximum(int band) {
		double val = Double.NEGATIVE_INFINITY;
		for (int p = band; p < size(); p+=this.getBDim())
			if (getPixelDouble(p) > val)
				val = getPixelDouble(p);
		return val;
	}
	
	/**
	 * Computes the minimum value of the image in the specified band
	 * @param band
	 * 		Band.
	 * @return
	 * 		a double representation of the minimum in the specified band
	 */
	public double minimum(int band) {
		double val = Double.MAX_VALUE;
		for (int p = band; p < size(); p+=this.getBDim())
			if (getPixelDouble(p) < val)
				val = getPixelDouble(p);
		return val;
	}

	/**
	 * Computes the maximum value of the image in the specified band (Ignore NaN and  Infinite values)
	 * @param band
	 * 		Band.
	 * @return
	 * 		a double representation of the maximum in the specified band
	 */
	public double maximumIgnoreNonRealValues(int band) {
		double val = Double.NEGATIVE_INFINITY;
		for (int p = band; p < size(); p+=this.getBDim())
			if (getPixelDouble(p) > val)
				val = getPixelDouble(p);
		return val;
	}
	
	/**
	 * Computes the minimum value of the image in the specified band (Ignore NaN and  Infinite values)
	 * @param band
	 * 		Band.
	 * @return
	 * 		a double representation of the minimum in the specified band
	 */
	public double minimumIgnoreNonRealValues(int band) {
		double val = Double.MAX_VALUE;
		for (int p = band; p < size(); p+=this.getBDim())
			if (getPixelDouble(p) < val)
				val = getPixelDouble(p);
		return val;
	}

	@Override
	public double maximumDouble() {
		return this.maximum();
	}

	@Override
	public double maximumDouble(int band) {
		return this.maximum(band);
	}

	@Override
	public double maximumDoubleIgnoreNonRealValues(int band) {
		return this.maximumIgnoreNonRealValues(band);
	}

	@Override
	public double minimumDouble() {
		return this.minimum();
	}

	@Override
	public double minimumDouble(int band) {
		return this.minimum(band);
	}

	@Override
	public double minimumDoubleIgnoreNonRealValues(int band) {
		return this.minimumIgnoreNonRealValues(band);
	}

	@Override
	public boolean maximumBoolean() {
		return doubleToBoolean(this.maximumDouble());
	}

	@Override
	public int maximumByte() {
		return doubleToUnsignedByte(this.maximumDouble());
	}

	@Override
	public int maximumInt() {
		return doubleToInt(this.maximumDouble());
	}

	@Override
	public boolean minimumBoolean() {
		return doubleToBoolean(this.minimumDouble());
	}

	@Override
	public int minimumByte() {
		return doubleToUnsignedByte(this.minimumDouble());
	}

	@Override
	public int minimumInt() {
		return doubleToInt(this.minimumDouble());
	}
}
