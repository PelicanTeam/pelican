package fr.unistra.pelican;

import java.io.Serializable;
import java.util.Arrays;

/**
 * This class represents a byte valued image with pixels values in the interval
 * [0,255] Range for an byte pixel is [0; 255]. Note that the internal storage
 * go from Byte.MIN_VALUE to Byte.MAX_VALUE, so a + Byte.MIN_VALUE shit is
 * applied.
 * 
 * @author PELICAN team
 * @version 1.0
 */

public class ByteImage extends Image implements Serializable {
	

	/**
	 * Pixel data array
	 */

	private byte[] pixels;

	/**
	 * Serial version ID
	 */

	private static final long serialVersionUID = 4L;
	
	/**
	 * Constructs a ByteImage
	 */
	protected ByteImage(){
		super();
	}

	/**
	 * Constructs a ByteImage identical to the given argument
	 * 
	 * @param image
	 *          ByteImage to copy
	 */

	public ByteImage(ByteImage image) {
		super(image);
		this.pixels = (byte[]) image.pixels.clone();
	}

	/**
	 * Constructs a ByteImage identical to the given argument
	 * 
	 * @param image
	 *          Image to copy
	 */
	public ByteImage(Image image) {
		this(image, true);
	}

	/**
	 * Constructs a ByteImage identical to the given argument
	 * 
	 * @param image
	 *          Image to copy
	 * @param copyData
	 *          if and only if it is set to true are the pixels copied
	 */
	public ByteImage(Image image, boolean copyData) {
		super(image);
		this.pixels = new byte[image.getXDim() * image.getYDim() * image.getZDim()
			* image.getTDim() * image.getBDim()];
		if (copyData == true)
			for (int i = 0; i < pixels.length; i++)
				setPixelByte(i, image.getPixelByte(i));
	}

	/**
	 * Constructs a ByteImage from the given argument. The pixels are copied if
	 * and only of ''copy'' is set to true.
	 * 
	 * @param image
	 *          ByteImage to copy
	 * @param copyData
	 *          if and only if it is set to true are the pixels copied
	 */
	public ByteImage(ByteImage image, boolean copyData) {
		super(image);

		if (copyData == true)
			this.pixels = (byte[]) image.pixels.clone();
		else
			this.pixels = new byte[image.getXDim() * image.getYDim()
				* image.getZDim() * image.getTDim() * image.getBDim()];
	}

	/**
	 * Constructs a ByteImage with the given dimensions
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

	public ByteImage(int xdim, int ydim, int zdim, int tdim, int bdim) {
		super(xdim, ydim, zdim, tdim, bdim);
		this.pixels = new byte[xdim * ydim * zdim * tdim * bdim];
	}

	/**
	 * Sets all the pixel values to the given value
	 * 
	 * @param b
	 *          Desired value for the pixels
	 */

	public void fill(byte b) {
		Arrays.fill(pixels, (byte) b);
	}

	@Override
	public void fill(double d) {
		this.fill(doubleToSignedByte(d));		
	}

	/**
	 * Creates a copy of this ByteImage
	 * 
	 * @return an axact copy of this ByteImage
	 */

	public ByteImage copyImage(boolean copyData) {
		return new ByteImage(this, copyData);
	}

	/**
	 * Creates a new instance of ByteImage
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
		return new ByteImage(xdim, ydim, zdim, tdim, bdim);
	}

	/**
	 * Checks if the image is empty, i.e. it contains only 0 pixels
	 * 
	 * @return true if the image is empty
	 */

	public boolean isEmpty() {
		for (int p = 0; p < pixels.length; p++)
			if (pixels[p] > 0)
				return false;
		return true;
	}

	/**
	 * Duplicates the given channel to all the available dimensions
	 * 
	 * @param band
	 *          channel to duplicate
	 */

	public void duplicateBand(int band) {
		for (int x = 0; x < this.xdim; x++)
			for (int y = 0; y < this.ydim; y++)
				for (int z = 0; z < this.zdim; z++)
					for (int t = 0; t < this.tdim; t++)
						for (int b = 0; b < this.bdim; b++)
							if (b != band)
								setPixelByte(x, y, z, t, b, getPixelByte(x, y, z, t, band));
	}

	/**
	 * Extracts the desired channel as mono-channel ByteImage
	 * 
	 * @param b
	 *          channel to extract
	 * @return the resulting mono-channel ByteImage
	 */
	@Deprecated
	public ByteImage getBand(int b) {
		ByteImage im = new ByteImage(xdim, ydim, zdim, tdim, 1);

		for (int x = 0; x < this.xdim; x++)
			for (int y = 0; y < this.ydim; y++)
				for (int z = 0; z < this.zdim; z++)
					for (int t = 0; t < this.tdim; t++)
						im.setPixelByte(x, y, z, t, 0, this.getPixelByte(x, y, z, t, b));
		return im;
	}

	/**
	 * Sets the given channel number to the given mono-channel ByteImage
	 * 
	 * @param b
	 *          channel to set
	 * @param im
	 *          ByteImage to set the channel to
	 */
	@Deprecated
	public void setBand(int b, ByteImage im) {
		for (int x = 0; x < this.xdim; x++)
			for (int y = 0; y < this.ydim; y++)
				for (int z = 0; z < this.zdim; z++)
					for (int t = 0; t < this.tdim; t++)
						setPixelByte(x, y, z, t, b, im.getPixelByte(x, y, z, t, 0));
	}
	
	/**
	 * Computes the pixel sum of the image
	 * 
	 * @return the pixel sum of the image
	 */
	public long getSum()
	{
		long sum=0;
		for(int i=this.size()-1;i>=0;i--)
		{
			sum+=this.getPixelByte(i);
		}
		return sum;
	}

	/**
	 * Computes the pixel sum of a given channel
	 * 
	 * @param b
	 *          channel to compute
	 * @return the pixel sum of the given channel
	 */

	public long getPixelSum(int b) {
		long sum = 0;

		for (int x = 0; x < this.xdim; x++) {
			for (int y = 0; y < this.ydim; y++)
				sum += this.getPixelXYBByte(x, y, b);
		}

		return sum;
	}

	/**
	 * Computes the pixel average of a given channel
	 * 
	 * @param b
	 *          channel to compute
	 * @return the pixel sum of the given channel
	 */

	public double getPixelAverage(int b) {
		long sum = this.getPixelSum(b);
		long cnt = this.xdim * this.ydim;

		return sum / cnt;
	}

	/**
	 * Extracts the frames between the given limits
	 * 
	 * @param t1
	 *          lower frame limit
	 * @param t2
	 *          upper frame limit
	 * @return the resulting ByteImage between the given two limits
	 */
	@Deprecated
	public ByteImage getFrames(int t1, int t2) {
		if (t1 < 0 || t2 > this.getTDim() || t2 < t1)
			return new ByteImage(this);

		ByteImage im = new ByteImage(this.xdim, this.ydim, this.zdim, t2 - t1 + 1,
			this.bdim);
		im.copyAttributes(this);

		for (int x = 0; x < xdim; x++)
			for (int y = 0; y < ydim; y++)
				for (int z = 0; z < zdim; z++)
					for (int b = 0; b < bdim; b++)
						for (int t = 0; t < t2 - t1 + 1; t++)
							im.setPixelByte(x, y, z, t, b, this.getPixelByte(x, y, z, t1 + t,
								b));

		return im;
	}

	/**
	 * Extracts the frames at the give position
	 * 
	 * @param t
	 *          frame to extract
	 * @return the resulting monoframe ByteImage at the given position
	 */
	@Deprecated
	public ByteImage getFrame(int t) {
		ByteImage im = new ByteImage(this.xdim, this.ydim, this.zdim, 1, this.bdim);
		im.copyAttributes(this);


		for (int x = 0; x < this.xdim; x++)
			for (int y = 0; y < this.ydim; y++)
				for (int z = 0; z < this.zdim; z++)
					for (int b = 0; b < this.bdim; b++)
						im.setPixelByte(x, y, z, 0, b, this.getPixelByte(x, y, z, t, b));
		return im;
	}

	/**
	 * Extracts a frame from the given view axis and position
	 * 
	 * @param f
	 *          frame number to extract
	 * @param axis
	 *          the view axis (Image.X,Image.Y,Image.Z)
	 * @return the resulting monoframe ByteImage at the given position from the
	 *         given view point
	 */
	@Deprecated
	public ByteImage getFrame(int f, int axis) {
		ByteImage im = null;

		switch (axis) {
		case Image.Y: /* Y axis */
			im = new ByteImage(xdim, zdim, 1, 1, 1);

			if (f > ydim)
				f = ydim - 1;
			else if (f < 0)
				f = 0;

			for (int x = 0; x < xdim; x++)
				for (int z = 0; z < zdim; z++)
					im.setPixelXYByte(x, z, this.getPixelXYZByte(x, f, z));
			break;

		case Image.X: /* X axis */
			im = new ByteImage(ydim, zdim, 1, 1, 1);

			if (f > xdim)
				f = xdim - 1;
			else if (f < 0)
				f = 0;

			for (int y = 0; y < ydim; y++)
				for (int z = 0; z < zdim; z++)
					im.setPixelXYByte(y, z, this.getPixelXYZByte(f, y, z));
			break;

		case Image.Z: /* Z axis */
			im = new ByteImage(xdim, ydim, 1, 1, 1);

			if (f > zdim)
				f = zdim - 1;
			else if (f < 0)
				f = 0;

			for (int x = 0; x < xdim; x++)
				for (int y = 0; y < im.ydim; y++)
					im.setPixelXYByte(x, y, this.getPixelXYZByte(x, y, f));
			break;
		}

		return im;
	}

	/**
	 * Sets the given time frame to the given ByteImage
	 * 
	 * @param t
	 *          time frame to set
	 * @param im
	 *          ByteImage to set the time frame to
	 */
	@Deprecated
	public void setFrame(int t, ByteImage im) {
		for (int x = 0; x < xdim; x++)
			for (int y = 0; y < ydim; y++)
				for (int z = 0; z < zdim; z++)
					for (int b = 0; b < bdim; b++)
						setPixelByte(x, y, z, t, b, im.getPixelByte(x, y, z, 0, b));
	}

	/**
	 * Computes the total number of pixels in all dimensions
	 * 
	 * @return the number of pixels
	 */

	public int size() {
		return pixels.length;
	}

	/**
	 * Gets a copy of the pixel array
	 * 
	 * @return a copy of the pixel array
	 */

	public byte[] getPixels() {
		return pixels;
	}

	/**
	 * Sets the pixels to the copy of the given array
	 * 
	 * @param values
	 *          pixel array to copy
	 */

	public void setPixels(byte[] values) {
		pixels = (byte[]) values.clone();
	}
	
	/**
	 * Sets the pixels to the copy of the given array
	 * 
	 * @param values
	 *          pixel array to copy
	 */

	public void setPixelsUnsafe(byte[] values) {
		pixels = values;
	}

	/**
	 * Compares with the given ByteImage
	 * 
	 * @param im
	 *          image to compare
	 * @return <code>true</code> if and only if the given image has the same
	 *         pixel values as this image
	 */

	public boolean equals(Image im) {
		if (im==null  || !(im instanceof ByteImage))
			return false;

		if (!haveSameDimensions(this, im))
			return false;

		int size = size();
		for (int i = 0; i < size; i++)
			if (im.getPixelByte(i) != getPixelByte(i))
				return false;

		return true;
	}

	/**
	 * Computes the number of different pixels divided by the total size
	 * 
	 * @param im
	 *          image to compare
	 * @return the number of different pixels divided by total size or -1 if the
	 *         images have different dimensions
	 */

	public double nbDifferentPixels(ByteImage im) {
		double ctr = 0.0;

		if (!haveSameDimensions(this, im))
			return -1;

		for (int i = 0; i < size(); i++)
			if ((im.getPixelByte(i) != this.getPixelByte(i)))
				ctr++;

		return ctr / size();
	}

	/**
	 * Computes the difference ratio of the given image with this image
	 * 
	 * @param im
	 *          image to compare
	 * @return the number of different pixels or -1 if the images have different
	 *         dimensions
	 */

	public double differenceRatio(ByteImage im) {
		double ctr = 0.0;

		if (!haveSameDimensions(this, im))
			return -1.0;

		for (int i = 0; i < size(); i++)
			ctr += Math.abs(im.getPixelByte(i) - this.getPixelByte(i));

		return ctr / size();
	}

	@Override
	public double getPixelDouble(int loc) {
		return signedByteToDouble(pixels[loc]);
		//return DoubleImage.byteToDouble * (double) (pixels[loc] + 128);
	}

	@Override
	public int getPixelInt(int loc) {
		return signedByteToInt(pixels[loc]);
		//return pixels[loc] << 24;
	}

	@Override
	public int getPixelByte(int loc) {
		return signedByteToUnsignedByte(pixels[loc]);
		//return pixels[loc] - Byte.MIN_VALUE;
	}

	@Override
	public boolean getPixelBoolean(int loc) {
		return signedByteToBoolean(pixels[loc]);
		//return pixels[loc] >= 0 ? true : false;
	}

	@Override
	public void setPixelDouble(int loc, double value) {
		pixels[loc] = doubleToSignedByte(value);
		//pixels[loc] = (byte) Math.round(doubleToByte * value - 128);
	}

	@Override
	public void setPixelInt(int loc, int value) {
		pixels[loc] = intToSignedByte(value);
		//pixels[loc] = (byte) (value >> 24);
	}

	@Override
	public void setPixelByte(int loc, int value) {
		pixels[loc] = unsignedByteToSignedByte(value);
		//pixels[loc] = (byte) (value + Byte.MIN_VALUE);
	}

	@Override
	public void setPixelBoolean(int loc, boolean value) {
		pixels[loc] = booleanToSignedByte(value);
		//pixels[loc] = value ? Byte.MAX_VALUE : Byte.MIN_VALUE;
	}

	@Override
	public void setPixel(Image input, int x1, int y1, int z1, int t1, int b1,
		int x2, int y2, int z2, int t2, int b2) {
		this.setPixelByte(x1, y1, z1, t1, b1, input
			.getPixelByte(x2, y2, z2, t2, b2));
	}
	
	@Override
	public final int[] getVectorPixelByte(int index) {
		int[] vector = new int[bdim];
		
		for (int b = 0; b < bdim; b++)
			vector[b] = pixels[index++]-Byte.MIN_VALUE;

		return vector;
	}

	/**
	 * Convert the ByteImage to a IntegerImage without modifying the values 
	 * @return	a byte image
	 */
	public IntegerImage copyToIntegerImage() {
		IntegerImage i=new IntegerImage(this,false);
		for (int p=0;p<i.size();p++)
			i.setPixelInt(p,getPixelByte(p));
		return i;
	}
	
	/**
	 * Computes the minimum value of the image
	 * @return
	 * 		the signed Byte representation of the minimum
	 */
	public byte minimum(){
		byte val = Byte.MAX_VALUE;
		for (int p = 0; p < size(); p++)
			if ( pixels[p] < val)
				val = pixels[p];
		return val;
		
	}
	
	/**
	 * Computes the maximum value of the image
	 * @return
	 * 		the signed Byte representation of the maximum
	 */
	public byte maximum(){
		byte val = Byte.MIN_VALUE;
		for (int p = 0; p < size(); p++)
			if ( pixels[p] > val)
				val = pixels[p];
		return val;
	}
	
	/**
	 * Computes the minimum value of the image in the specified band
	 * @param band
	 * 		Band.
	 * @return
	 * 		the signed Byte representation of the minimum in the specified band
	 */
	public byte minimum(int band){
		byte val = Byte.MAX_VALUE;
		for (int p = band; p < size(); p+=this.getBDim())
			if ( pixels[p] < val)
				val = pixels[p];
		return val;
		
	}
	
	/**
	 * Computes the maximum value of the image in the specified band
	 * @param band
	 * 		Band.
	 * @return
	 * 		the signed Byte representation of the maximum in the specified band
	 */
	public byte maximum(int band){
		byte val = Byte.MIN_VALUE;
		for (int p = band; p < size(); p+=this.getBDim())
			if ( pixels[p] > val)
				val = pixels[p];
		return val;
	}

	@Override
	public boolean maximumBoolean() {
		return signedByteToBoolean(this.maximum());
	}

	@Override
	public int maximumByte() {
		return signedByteToUnsignedByte(this.maximum());
	}

	@Override
	public double maximumDouble() {
		return signedByteToDouble(this.maximum());
	}

	@Override
	public double maximumDouble(int band) {
		return signedByteToDouble(this.maximum(band));
	}

	@Override
	public double maximumDoubleIgnoreNonRealValues(int band) {
		return signedByteToDouble(this.maximum(band));
	}

	@Override
	public int maximumInt() {
		return signedByteToInt(this.maximum());
	}

	@Override
	public boolean minimumBoolean() {
		return signedByteToBoolean(this.minimum());
	}

	@Override
	public int minimumByte() {
		return signedByteToUnsignedByte(this.minimum());
	}

	@Override
	public double minimumDouble() {
		return signedByteToDouble(this.minimum());
	}

	@Override
	public double minimumDouble(int band) {
		return signedByteToDouble(this.minimum(band));
	}

	@Override
	public double minimumDoubleIgnoreNonRealValues(int band) {
		return signedByteToDouble(this.minimum(band));
	}

	@Override
	public int minimumInt() {
		return signedByteToInt(this.minimum());
	}	
}
