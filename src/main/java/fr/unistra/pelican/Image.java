package fr.unistra.pelican;

import java.awt.Point;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import fr.unistra.pelican.util.Disposable;
import fr.unistra.pelican.util.Pixel;
import fr.unistra.pelican.util.Point3D;
import fr.unistra.pelican.util.Point4D;
import fr.unistra.pelican.util.PointVideo;
import fr.unistra.pelican.util.Tools;
import fr.unistra.pelican.util.iterator.AbstractImageIterator;
import fr.unistra.pelican.util.iterator.ImageIterator;
import fr.unistra.pelican.util.iterator.ImageIteratorXY;
import fr.unistra.pelican.util.iterator.MaskedImageIterator;
import fr.unistra.pelican.util.iterator.MaskedImageIteratorXY;
import fr.unistra.pelican.util.mask.Mask;
import fr.unistra.pelican.util.mask.MaskStack;


/**
 * This class provides default implementation for Images. Standard behavior such
 * as internal cursor and attributes modifications are defined here. Pixels data
 * is <b><i>not</i></b> included.
 * 
 * TODO: Write the fills methods
 * 
 * @author PELICAN team
 * @version 1.0
 */

public abstract class Image implements Serializable, Mask, Iterable<Pixel>, Disposable {
	
	/**
	 * Conversion from [Integer.MIN_VALUE, Integer.MAX_VALUE] to [0.0, 1.0]
	 */
	public static final double intToDouble = 1.0 / ((double) Integer.MAX_VALUE - (double) Integer.MIN_VALUE);

	/**
	 * Conversion from [Integer.MIN_VALUE, Integer.MAX_VALUE] to [0.0, 1.0]
	 */
	public static final double intToDoubleOffset = 0.5;

	/**
	 * Conversion from [Byte.MIN_VALUE, Byte.MAX_VALUE] to [0.0, 1.0]
	 */
	public static final double byteToDouble = 1.0 / ((double) Byte.MAX_VALUE - (double) Byte.MIN_VALUE);
	
	/**
	 * Conversion from [0.0, 1.0] to [0, 255]
	 */
	public static final double doubleToByte = ((double) Byte.MAX_VALUE - (double) Byte.MIN_VALUE);
	
	/**
	 * Conversion from [0.0, 1.0] to [Integer.MIN_VALUE, Integer.MAX_VALUE] 
	 */
	public static final double doubleToInt = ((double)Integer.MAX_VALUE - (double)Integer.MIN_VALUE);
	
	/**
	 * Constant denoting the horizontal dimension
	 */
	public final static int X = 0;

	/**
	 * Constant denoting the vertical dimension
	 */
	public final static int Y = 1;

	/**
	 * Constant denoting the depth dimension
	 */
	public final static int Z = 2;

	/**
	 * Constant denoting the time dimension
	 */
	public final static int T = 3;

	/**
	 * Constant denoting the channel dimension
	 */
	public final static int B = 4;

	/**
	 * Constant denoting an image type with no particular data organisation
	 */
	public final static int RAW = 0;

	/**
	 * Type of the image as given by the JAI framework
	 */

	public int type;

	/**
	 * Name of the image.
	 */

	private String name;

	/**
	 * Whether the image is colored or not
	 */

	public boolean color;


	/**
	 * The horizontal dimension (origin at left)
	 */
	public int xdim;

	/**
	 * The vertical dimension (origin at top)
	 */

	public int ydim;

	/**
	 * The third dimension (depth; with origin incident to the horizontal and
	 * vertical ones)
	 */

	public int zdim;

	/**
	 * The number of frames
	 */

	public int tdim;

	/**
	 * The number of channels
	 */

	public int bdim;


	/**
	 * A properties map, use it as you want.
	 */

	public Map<String, Object> properties = new TreeMap<String, Object>();

	/**
	 * The center of the image (relevant for structuring element) 
	 */
	protected Point4D center=null; 
	
	/**
	 * pixel mask holder
	 * TODO : Set it private
	 */
	public MaskStack mask;
	
	
	/**
	 * Serial version ID
	 */
	private static final long serialVersionUID = 1L; // should we update it ? for sure yes

	/**
	 * Copies the attributes of the given image to this image
	 * 
	 * @param im
	 *          the source image
	 */

	public final void copyAttributes(Image im) {
		this.type = im.type;
		this.color = im.color;
		this.name=im.name;
		/* note by Regis, 02.2009 :
		* a mask is not built to be modified, so we do not clone it.
		* if my hypothesis is later modified, do not forget to update 
		* the copyImage() method accordingly.
		* - Edit by BP, 03.2009 :
		* Stacks are not shared anymore, references are copied from 
		* the original stack to the new one. Even if the hypothesis of 
		* masks seen as constants may still be true, the stack should 
		* be allowed to vary freely with no border effect.
		*/
		if(im.mask!=null)
			for(Mask m:im.mask)
				this.pushMask(m);
		
		/*
		 * All properties are copied by reference
		 */
		properties.putAll(im.properties);
		
	}

	/**
	 * Default constructor
	 */

	public Image() {
		mask=new MaskStack();
	}

	/**
	 * Constructs an Image identical to the given argument
	 * 
	 * @param image
	 *          image to clone
	 */

	public Image(Image image) {
		this(image.xdim, image.ydim, image.zdim, image.tdim, image.bdim);
		this.copyAttributes(image);
	}

	/**
	 * Constructs an Image with the given dimensions
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

	public Image(int xdim, int ydim, int zdim, int tdim, int bdim) {
		this();
		this.setDim(xdim, ydim, zdim, tdim, bdim);
	}

	/**
	 * Put all references to null to avoid confusing loops for the garbage collector.
	 * All references of class Image of the properties and the mask stack will be disposed too!
	 * 
	 * Do not use this if you do not suffer from memory licks!
	 */
	public void dispose(){
		if(mask!=null)
		{
			for(Mask m:mask)
			{
				if(m instanceof Disposable)
					((Disposable)m).dispose();
			}
			mask.clear();
			mask=null;
		}
		if(properties!=null)
		{
			for(Entry<String, Object> e:properties.entrySet())
			{
				if(e.getValue() instanceof Disposable)
					((Disposable)e.getValue()).dispose();
			}
			properties.clear();
			properties=null;
		}
	
	}
	
	/**
	 * Sets the value of the color field
	 * 
	 * @param b
	 *          desired color flag
	 */

	public final void setColor(boolean b) {
		color = b;
	}



	/**
	 * Gets the value of the color field
	 * 
	 * @return the color flag
	 */

	public final boolean isColor() {
		return color;
	}


	/**
	 * Sets the dimensions to the given values
	 * 
	 * @param x
	 *          horizontal dimension
	 * @param y
	 *          vertical dimension
	 * @param z
	 *          depth
	 * @param t
	 *          frame number
	 * @param b
	 *          channel number
	 */

	public void setDim(int x, int y, int z, int t, int b) {
		this.setXDim(x);
		this.setYDim(y);
		this.setZDim(z);
		this.setTDim(t);
		this.setBDim(b);
		resetCenter();
	}

	/**
	 * Image copying method intented to be overridden by subsclasses
	 * 
	 * @param copyData
	 *          If set to true pixels are also copied
	 * @return an axact copy of the this Image
	 */
	public abstract Image copyImage(boolean copyData);

	/**
	 * Image creating method intented to be overridden by subclasses
	 * 
	 * @param xdim
	 *          horizontal dimension
	 * @param ydim
	 *          vertical dimension
	 * @param zdim
	 *          depth
	 * @param tdim
	 *          frame number
	 * @param bdim
	 *          channel number
	 * @return an instance of the appropriate subclass
	 */
	public abstract Image newInstance(int xdim, int ydim, int zdim, int tdim,
		int bdim);

	/**
	 * Image comparison method intended to be overridden by subclasses
	 * 
	 * @param im
	 *          image to compare with
	 * @return true if both images have same content
	 */
	public abstract boolean equals(Image im);


	/**
	 * Image comparison method intended to be used instead of generic equals method defined in class Object
	 * 
	 * @param obj Object (image) to compare with
	 * @return true if both images have same content
	 */
	public final boolean equals(Object obj) {
		if(obj == null || !(obj instanceof Image) )
			return false;
		return equals((Image)obj);
	}

	
	/**
	 * Sets the horizontal dimension to the given value
	 * 
	 * @param xdim
	 *          the desired horizontal dimension
	 * @todo change array dim ...
	 */

	public void setXDim(int xdim) {
		this.xdim = xdim;
	}

	/**
	 * Sets the vertical dimension to the given value
	 * 
	 * @param ydim
	 *          the desired vertical dimension
	 * @todo change array dim ...
	 */

	public void setYDim(int ydim) {
		this.ydim = ydim;
	}

	/**
	 * Sets the depth dimension to the given value
	 * 
	 * @param zdim
	 *          the desired depth dimension
	 * @todo change array dim ...
	 */

	public void setZDim(int zdim) {
		this.zdim = zdim;
	}

	/**
	 * Sets the time dimension to the given value
	 * 
	 * @param tdim
	 *          the desired time dimension
	 * @todo change array dim ...
	 */

	public void setTDim(int tdim) {
		this.tdim = tdim;
	}

	/**
	 * Sets the channel dimension to the given value
	 * 
	 * @param bdim
	 *          the desired channel dimension
	 * @todo change array dim ...
	 */

	public void setBDim(int bdim) {
		this.bdim = bdim;
		
	}

	/**
	 * Gets the horizontal dimension
	 * 
	 * @return the horizontal dimension
	 */

	public final int getXDim() {
		return xdim;
	}

	/**
	 * Gets the vertical dimension
	 * 
	 * @return the vertical dimension
	 */

	public final int getYDim() {
		return ydim;
	}

	/**
	 * Gets the depth dimension
	 * 
	 * @return the depth dimension
	 */

	public final int getZDim() {
		return zdim;
	}

	/**
	 * Gets the time dimension
	 * 
	 * @return the time dimension
	 */

	public final int getTDim() {
		return tdim;
	}

	/**
	 * Gets the channel dimension
	 * 
	 * @return the channel dimension
	 */

	public final int getBDim() {
		return bdim;
	}


	/**
	 * Compares the dimensions of given two Images.
	 * 
	 * @param im1
	 *          first Image
	 * @param im2
	 *          second image
	 * @return <code>true</code> if and only if the given images have the equal
	 *         corresponding dimension values
	 */

	public static boolean haveSameDimensions(Image im1, Image im2) {
		if ((im1.getXDim() != im2.getXDim()) || (im1.getYDim() != im2.getYDim())
			|| (im1.getZDim() != im2.getZDim()) || (im1.getBDim() != im2.getBDim())
			|| (im1.getTDim() != im2.getTDim()))
			return false;

		return true;
	}

	/**
	 * Compares all dimensions of two given Images, except for the frame number
	 * 
	 * @param im1
	 *          first Image
	 * @param im2
	 *          second Image
	 * @return <code>true</code> if and only if the given images have equal
	 *         horizontal, vertical, depth and channel dimensions
	 */

	public static boolean haveSameDimensionsXYZB(Image im1, Image im2) {
		if ((im1.getXDim() != im2.getXDim()) || (im1.getYDim() != im2.getYDim())
			|| (im1.getZDim() != im2.getZDim()) || (im1.getBDim() != im2.getBDim()))
			return false;

		return true;
	}

	/**
	 * Computes the total number of pixels in all dimensions
	 * 
	 * @return the number of pixels
	 */

	public abstract int size();

	/**
	 * Sets all the pixel values to the given value
	 * 
	 * @param b
	 *          Desired value for the pixels
	 */

	public void fill(double b) {
		for (int i = 0; i < size(); i++)
			setPixelDouble(i, b);
	}

	/**
	 * Computes the minimum value of the image
	 * 
	 * @return a double representation of the minimum
	 */
	public double minimumDouble() {
		double val = Double.MAX_VALUE;
		for (int p = 0; p < size(); p++)
			if (getPixelDouble(p) < val)
				val = getPixelDouble(p);
		return val;
	}

	/**
	 * Computes the maximum value of the image
	 * 
	 * @return a double representation of the maximum
	 */
	public double maximumDouble() {
		double val = Double.MIN_VALUE;
		for (int p = 0; p < size(); p++)
			if (getPixelDouble(p) > val)
				val = getPixelDouble(p);
		return val;
	}
	
	/**
	 * Computes the minimum value of the image in specified band
	 * 
	 * 
	 * @param band band number
	 * @return a double representation of the minimum in specified band
	 */
	public double minimumDouble(int band) {
		double val = Double.MAX_VALUE;
		for (int p = band; p < size(); p+=bdim)
			if (getPixelDouble(p) < val)
				val = getPixelDouble(p);
		return val;
	}

	/**
	 * Computes the minimum value of the image in specified band (ignores NaN and Infinite values)
	 * 
	 * 
	 * @param band band number
	 * @return a double representation of the minimum in specified band
	 */
	public double minimumDoubleIgnoreNonRealValues(int band) {
		double val = Double.MAX_VALUE;
		for (int p = band; p < size(); p+=bdim)
		{
			double v=getPixelDouble(p);
			if (Tools.isValue(v) && v < val)
				val = v;
		}
		return val;
	}
	
	/**
	 * Computes the maximum value of the image in specified band (ignores NaN and Infinite values)
	 * @param band Band.
	 * 
	 * @return a double representation of the maximum in specified band
	 */
	public double maximumDoubleIgnoreNonRealValues(int band) {
		double val = Double.MIN_VALUE;
		for (int p = band; p < size(); p+=bdim)
		{
			double v=getPixelDouble(p);
			if (Tools.isValue(v) && v > val)
				val = v;
		}
			
		return val;
	}
	
	/**
	 * Computes the maximum value of the image in specified band
	 * @param band Band.
	 * 
	 * @return a double representation of the maximum in specified band
	 */
	public double maximumDouble(int band) {
		double val = Double.MIN_VALUE;
		for (int p = band; p < size(); p+=bdim)
			if (getPixelDouble(p) > val)
				val = getPixelDouble(p);
		return val;
	}

	/**
	 * Computes the minimum value of the image
	 * 
	 * @return an integer representation of the minimum
	 */
	public int minimumInt() {
		int val = Integer.MAX_VALUE;
		for (int p = 0; p < size(); p++)
			if (getPixelInt(p) < val)
				val = getPixelInt(p);
		return val;
	}

	/**
	 * Computes the maximum value of the image
	 * 
	 * @return an integer representation of the maximum
	 */
	public int maximumInt() {
		int val = Integer.MIN_VALUE;
		for (int p = 0; p < size(); p++)
			if (getPixelInt(p) > val)
				val = getPixelInt(p);
		return val;
	}
	
	/**
	 * Computes the maximum value of the image under a mask
	 * 
	 * @return an integer representation of the maximum under the mask
	 */
	public int maximumInt(BooleanImage mask) {
		int val = Integer.MIN_VALUE;
		for (int p = 0; p < size(); p++)
			if (mask.getPixelBoolean(p)&&getPixelInt(p) > val)
				val = getPixelInt(p);
		return val;
	}

	/**
	 * Computes the minimum value of the image
	 * 
	 * @return a byte representation of the minimum
	 */
	public int minimumByte() {
		int val = 255;
		for (int p = 0; p < size(); p++)
			if (getPixelByte(p) < val)
				val = getPixelByte(p);
		return val;
	}

	/**
	 * Computes the maximum value of the image
	 * 
	 * @return a byte representation of the maximum
	 */
	public int maximumByte() {
		int val = 0;
		for (int p = 0; p < size(); p++)
			if (getPixelByte(p) > val)
				val = getPixelByte(p);
		return val;
	}

	/**
	 * Computes the minimum value of the image
	 * 
	 * @return a boolean representation of the minimum
	 */
	public boolean minimumBoolean() {
		for (int p = 0; p < size(); p++)
			if (!getPixelBoolean(p))
				return false;
		return true;
	}

	/**
	 * Computes the maximum value of the image
	 * 
	 * @return a boolean representation of the maximum
	 */
	public boolean maximumBoolean() {
		for (int p = 0; p < size(); p++)
			if (getPixelBoolean(p))
				return true;
		return false;
	}

	/**
	 * Computes the volume of the image, i.e. the sum of its pixel values
	 * 
	 * @return a double representation of the volume
	 */
	public double volume() {
		double v = 0;
		for (int p = 0; p < size(); p++)
			if ( isPresent(p) ) v += getPixelDouble(p);
		return v;
	}

	public int volumeByte() {
		int v = 0;
		for (int p = 0; p < size(); p++)
			if ( isPresent(p) ) v += getPixelByte(p);
		return v;
	}

	/**
	 * Convert coordinate in the XYZTB system in the internal linear coordinate system.
	 * @param x
	 * @param y
	 * @param z
	 * @param t
	 * @param b
	 * @return linear coordinate
	 * @deprecated Use getLinearIndexXYZTB(int x, int y, int z, int t, int b) instead
	 */
	@Deprecated
	public int getLinearIndex(int x, int y, int z, int t, int b)
	{
		return b + bdim * (x + xdim * (y + ydim * (z + t * zdim)));
	}


	/**
	 * Gets the LinearIndex (z=t=b=0).
	 * 
	 * @param x
	 *            horizontal index
	 * @param y
	 *            vertical index
	 * @return the linear index
	 */
	public final int getLinearIndexXY___( int x,int y) {
		return bdim * ( x + xdim * y);
	}

	/**
	 * Gets the linear index (t=b=0).
	 * 
	 * @param x
	 *            horizontal index
	 * @param y
	 *            vertical index
	 * @param z
	 *            depth index
	 * @return the linear index
	 */
	public final int getLinearIndexXYZ__(int x,int y, int z) {
		return bdim * ( x + xdim * ( y + ydim * z));
	}

	/**
	 * Gets the linear index (z=t=0).
	 * 
	 * @param x
	 *            horizontal index
	 * @param y
	 *            vertical index
	 * @param b
	 *            band index
	 * @return the linear index
	 */
	public final int getLinearIndexXY__B(int x,int y, int b) {
		return b + bdim * ( x + xdim * y);
	}

	/**
	 * Gets the linear index (z=b=0).
	 * 
	 * @param x
	 *            horizontal index
	 * @param y
	 *            vertical index
	 * @param t
	 *            time index
	 * @return the linear index
	 */
	public final int getLinearIndexXY_T_(int x,int y, int t) {
		return bdim * ( x + xdim * ( y + ydim * ( t * zdim)));
	}

	/**
	 * Gets the linear index (b=0).
	 * 
	 * @param largeIm
	 *            Image for which the linear index must be computed
	 * @param x
	 *            horizontal index
	 * @param y
	 *            vertical index
	 * @param z
	 *            depth index
	 * @param t
	 *            time index
	 * @return the linear index
	 */
	public final int getLinearIndexXYZT_(int x, int y, int z, int t) {
		return bdim * ( x + xdim * ( y + ydim * ( z + t * zdim)));
	}

	/**
	 * Gets the linear index (t=0).
	 * 
	 * @param x
	 *            horizontal index
	 * @param y
	 *            vertical index
	 * @param z
	 *            depth index
	 * @param b
	 *            band index
	 * @return the linear index
	 */
	public final int getLinearIndexXYZ_B(int x,int y, int z, int b) {
		return b + bdim * ( x + xdim * ( y + ydim * z ));
	}

	/**
	 * Gets the linear index (z=0).
	 * 
	 * @param x
	 *            horizontal index
	 * @param y
	 *            vertical index
	 * @param t
	 *            time index
	 * @param b
	 *            band index
	 * @return the linear index
	 */
	public final int getLinearIndexXY_TB(int x,int y, int t, int b) {
		return b + bdim * ( x + xdim * ( y + ydim * ( t * zdim)));
	}

	/**
	 * Gets the linear index.
	 * 
	 * @param x
	 *            horizontal index
	 * @param y
	 *            vertical index
	 * @param z
	 *            depth index
	 * @param t
	 *            time index
	 * @param b
	 *            band index
	 * @return the linear index
	 */
	public final int getLinearIndexXYZTB(int x, int y, int z, int t, int b) {
		return b + bdim * ( x + xdim * ( y + ydim * ( z + t * zdim)));
	}
	
	/**
	 * Gets the value of the pixel in the given location as double
	 * 
	 * @param loc
	 *          the index of the desired pixel
	 * @return the value of the pixel in the given location as double
	 */

	public abstract double getPixelDouble(int loc);
	public double getPixelDouble(long loc){
		return getPixelDouble((int)loc);
	}

	/**
	 * Gets the value of the pixel in the given location as int
	 * 
	 * @param loc
	 *          the index of the desired pixel
	 * @return the value of the pixel in the given location as int
	 */

	public abstract int getPixelInt(int loc);
	public int getPixelInt(long loc){
		return getPixelInt((int)loc);
	}

	/**
	 * Gets the value of the pixel in the given location as byte
	 * 
	 * @param loc
	 *          the index of the desired pixel
	 * @return the value of the pixel in the given location as byte
	 */

	public abstract int getPixelByte(int loc);
	public int getPixelByte(long loc){
		return getPixelByte((int)loc);
	}

	/**
	 * Gets the value of the pixel in the given location as bool
	 * 
	 * @param loc
	 *          the index of the desired pixel
	 * @return the value of the pixel in the given location as bool
	 */

	public abstract boolean getPixelBoolean(int loc);
	public boolean getPixelBoolean(long loc) {
		return getPixelBoolean((int)loc);
	}

	/**
	 * Gets the value of the pixels in the given location as a vector across all
	 * channels
	 * 
	 * @param x
	 *          the horizontal index
	 * @param y
	 *          the vertical index
	 * @param z
	 *          depth position of the desired pixel
	 * @return the value of the pixels in the given location as a vector across
	 *         all channels
	 */
	public final double[] getVectorPixelXYZDouble(int x, int y, int z) {
		double[] vector = new double[bdim];

		for (int b = 0; b < bdim; b++)
			vector[b] = getPixelXYZBDouble(x, y, z,  b);

		return vector;
	}
	
	/**
	 * Gets the value of the pixels in the given location as a vector across all
	 * channels
	 * 
	 * @param x
	 *          the horizontal index
	 * @param y
	 *          the vertical index
	 * @param z
	 *          depth position of the desired pixel
	 * @param t
	 *          time position of the desired pixel
	 * @return the value of the pixels in the given location as a vector across
	 *         all channels
	 */
	public final double[] getVectorPixelXYZTDouble(int x, int y, int z, int t) {
		double[] vector = new double[bdim];

		for (int b = 0; b < bdim; b++)
			vector[b] = getPixelXYZTBDouble(x, y, z, t, b);

		return vector;
	}

	/**
	 * Gets the value of the pixels in the given location as a vector across all
	 * channels
	 * 
	 * @param x
	 *          the horizontal index
	 * @param y
	 *          the vertical index
	 * @param z
	 *          depth position of the desired pixel
	 * @param t
	 *          time position of the desired pixel
	 * @return the value of the pixels in the given location as a vector across
	 *         all channels
	 */
	public final int[] getVectorPixelXYZTInt(int x, int y, int z, int t) {
		int[] vector = new int[bdim];

		for (int b = 0; b < bdim; b++)
			vector[b] = getPixelXYZTBInt(x, y, z, t, b);

		return vector;
	}
	
	/**
	 * Gets the value of the pixels in the given location as a vector across all
	 * channels
	 * 
	 * @param x
	 *          the horizontal index
	 * @param y
	 *          the vertical index
	 * @param z
	 *          depth position of the desired pixel
	 * @param t
	 *          time position of the desired pixel
	 * @return the value of the pixels in the given location as a vector across
	 *         all channels
	 */
	public final int[] getVectorPixelXYZTByte(int x, int y, int z, int t) {
		int[] vector = new int[bdim];

		for (int b = 0; b < bdim; b++)
			vector[b] = getPixelXYZTBByte(x, y, z, t, b);

		return vector;
	}
	
	
	
	
	
	/**
	 * Gets the value of the pixels in the given location as a vector across all
	 * channels
	 * 
	 * @param x
	 *          the horizontal index
	 * @param y
	 *          the vertical index
	 * @param t
	 *          time position of the desired pixel
	 * @return the value of the pixels in the given location as a vector across
	 *         all channels
	 */
	public final double[] getVectorPixelXYZTDouble(int x, int y, int t) {
		double[] vector = new double[bdim];

		for (int b = 0; b < bdim; b++)
			vector[b] = getPixelXYTBDouble(x, y, t, b);

		return vector;
	}

	/**
	 * Gets the value of the pixels in the given location as a vector across all
	 * channels
	 * 
	 * @param x
	 *          the horizontal index
	 * @param y
	 *          the vertical index
	 * @param t
	 *          time position of the desired pixel
	 * @return the value of the pixels in the given location as a vector across
	 *         all channels
	 */
	public final int[] getVectorPixelXYTInt(int x, int y, int t) {
		int[] vector = new int[bdim];

		for (int b = 0; b < bdim; b++)
			vector[b] = getPixelXYTBInt(x, y, t, b);

		return vector;
	}
	
	/**
	 * Gets the value of the pixels in the given location as a vector across all
	 * channels
	 * 
	 * @param x
	 *          the horizontal index
	 * @param y
	 *          the vertical index
	 * @param t
	 *          time position of the desired pixel
	 * @return the value of the pixels in the given location as a vector across
	 *         all channels
	 */
	public final int[] getVectorPixelXYTByte(int x, int y, int t) {
		int[] vector = new int[bdim];
		
		int index = getLinearIndexXY_T_(x,y,t);
		
		for (int b = 0; b < bdim; b++)
			vector[b] = getPixelByte(index++);

		return vector;
	}
	
	/**
	 * Gets the value of the pixels in the given location as a vector across all
	 * channels
	 * 
	 * @param index
	 *          linear index of the first band pixel
	 * @return the value of the pixels in the given location as a vector across
	 *         all channels
	 */
	public int[] getVectorPixelByte(int index) {
		int[] vector = new int[bdim];
		
		for (int b = 0; b < bdim; b++)
			vector[b] = getPixelByte(index++);

		return vector;
	}
	
	/**
	 * Gets the value of the pixels in the given location as a vector across all
	 * channels
	 * 
	 * @param index
	 *          linear index of the first band pixel 
	 * @return the value of the pixels in the given location as a vector across
	 *         all channels
	 */
	public int[] getVectorPixelInt(int index) {
		int[] vector = new int[bdim];
		
		for (int b = 0; b < bdim; b++)
			vector[b] = getPixelInt(index++);

		return vector;
	}

	/**
	 * Gets the value of the pixel in the given location as double
	 * 
	 * @param x
	 *          horizontal position of the desired pixel
	 * @param y
	 *          vertical position of the desired pixel
	 * @param z
	 *          depth position of the desired pixel
	 * @param t
	 *          time position of the desired pixel
	 * @param b
	 *          channel number of the desired pixel
	 * @return the value of the pixel in the given location as double
	 */
	public double getPixelDouble(int x, int y, int z, int t, int b) {
		return getPixelDouble(getLinearIndexXYZTB(x,y,z,t,b));
	}

	/**
	 * Gets the value of the pixel in the given location
	 * 
	 * @param x
	 *          horizontal position of the desired pixel
	 * @param y
	 *          vertical position of the desired pixel
	 * @return the value of the pixel in the given location
	 */

	public double getPixelXYDouble(int x, int y) {
		return getPixelDouble(getLinearIndexXY___(x,y));
	}

	/**
	 * Gets the value of the pixel in the given location
	 * 
	 * @param x
	 *          horizontal position of the desired pixel
	 * @param y
	 *          vertical position of the desired pixel
	 * @param z
	 *          depth position of the desired pixel
	 * @return the value of the pixel in the given location
	 */

	public double getPixelXYZDouble(int x, int y, int z) {
		return getPixelDouble(getLinearIndexXYZ__(x,y, z));
	}

	/**
	 * Gets the value of the pixel in the given location
	 * 
	 * @param x
	 *          horizontal position of the desired pixel
	 * @param y
	 *          vertical position of the desired pixel
	 * @param b
	 *          channel number of the desired pixel
	 * @return the value of the pixel in the given location
	 */

	public double getPixelXYBDouble(int x, int y, int b) {
		return getPixelDouble(getLinearIndexXY__B(x,y,b));
	}

	/**
	 * Gets the value of the pixel in the given location
	 * 
	 * @param x
	 *          horizontal position of the desired pixel
	 * @param y
	 *          vertical position of the desired pixel
	 * @param t
	 *          time position of the desired pixel
	 * @return the value of the pixel in the given location
	 */

	public double getPixelXYTDouble(int x, int y, int t) {
		return getPixelDouble(getLinearIndexXY_T_(x,y,t));
	}

	/**
	 * Gets the value of the pixel in the given location
	 * 
	 * @param x
	 *          horizontal position of the desired pixel
	 * @param y
	 *          vertical position of the desired pixel
	 * @param z
	 *          depth position of the desired pixel
	 * @param t
	 *          time position of the desired pixel
	 * @return the value of the pixel in the given location
	 */

	public double getPixelXYZTDouble(int x, int y, int z, int t) {
		return getPixelDouble(getLinearIndexXYZT_(x,y,z,t));
	}

	/**
	 * Gets the value of the pixel in the given location
	 * 
	 * @param x
	 *          horizontal position of the desired pixel
	 * @param y
	 *          vertical position of the desired pixel
	 * @param z
	 *          depth position of the desired pixel
	 * @param b
	 *          channel number of the desired pixel
	 * @return the value of the pixel in the given location
	 */

	public double getPixelXYZBDouble(int x, int y, int z, int b) {
		return getPixelDouble(getLinearIndexXYZ_B(x,y,z,b));
	}

	/**
	 * Gets the value of the pixel in the given location
	 * 
	 * @param x
	 *          horizontal position of the desired pixel
	 * @param y
	 *          vertical position of the desired pixel
	 * @param t
	 *          time position of the desired pixel
	 * @param b
	 *          channel number of the desired pixel
	 * @return the value of the pixel in the given location
	 */

	public double getPixelXYTBDouble(int x, int y, int t, int b) {
		return getPixelDouble(getLinearIndexXY_TB(x,y,t,b));
	}

	/**
	 * Gets the value of the pixel in the given location
	 * 
	 * @param x
	 *          horizontal position of the desired pixel
	 * @param y
	 *          vertical position of the desired pixel
	 * @param z
	 *          depth position of the desired pixel
	 * @param t
	 *          time position of the desired pixel
	 * @param b
	 *          channel number of the desired pixel
	 * @return the value of the pixel in the given location
	 */

	public double getPixelXYZTBDouble(int x, int y, int z, int t, int b) {
		return getPixelDouble(getLinearIndexXYZTB(x,y,z,t,b));
	}

	/**
	 * Sets the pixels at the given location to the given value as double
	 * 
	 * @param loc
	 *          index of the pixel to modify
	 * @param value
	 *          desired value of the pixel as double
	 */
	public abstract void setPixelDouble(int loc, double value);
	public void setPixelDouble(long loc,double value){
		setPixelDouble((int)loc,value);
	}
	
	/**
	 * Sets the pixels at the given location to the given value as int
	 * 
	 * @param loc
	 *          index of the pixel to modify
	 * @param value
	 *          desired value of the pixel as int
	 */

	public abstract void setPixelInt(int loc, int value);
	public void setPixelInt(long loc,int value){
		setPixelInt((int)loc, value);
	}
	
	
	/**
	 * Sets the pixels at the given location to the given value as byte
	 * 
	 * @param loc
	 *          index of the pixel to modify
	 * @param value
	 *          desired value of the pixel as byte
	 */

	public abstract void setPixelByte(int loc, int value);
	public void setPixelByte(long loc,int value){
		setPixelByte((int)loc,value);
	}
	
	/**
	 * Sets the pixels at the given location to the given value as boolean
	 * 
	 * @param loc
	 *          index of the pixel to modify
	 * @param value
	 *          desired value of the pixel as boolean
	 */

	public abstract void setPixelBoolean(int loc, boolean value);
	public void setPixelBoolean(long loc,boolean value){
		setPixelBoolean((int)loc,value);
	}

	/**
	 * Sets the value of the pixels in the given location as a vector across all
	 * channels
	 * 
	 * @param x
	 *          the horizontal index
	 * @param y
	 *          the vertical index
	 * @param z
	 *          depth position of the pixel
	 * @param t
	 *          time position of the pixel
	 * @param value
	 *          the desired vectorial value
	 */
	public final void setVectorPixelXYZTDouble(int x, int y, int z, int t,
		double[] value) {
		for (int b = 0; b < bdim; b++)
			setPixelXYZTBDouble(x, y, z, t, b, value[b]);
	}

	/**
	 * Sets the value of the pixel in the given location as double
	 * 
	 * @param x
	 *          horizontal position of the pixel
	 * @param y
	 *          vertical position of the pixel
	 * @param z
	 *          depth position of the pixel
	 * @param t
	 *          time position of the pixel
	 * @param b
	 *          channel number of the pixel
	 * @param value
	 *          desired value of the pixel at the given location as double
	 */
	public void setPixelDouble(int x, int y, int z, int t, int b, double value) {
		setPixelDouble(getLinearIndexXYZTB(x,y,z,t,b), value);
	}

	/**
	 * Sets the value of the pixel in the given location as double
	 * 
	 * @param x
	 *          horizontal position of the pixel
	 * @param y
	 *          vertical position of the pixel
	 * @param value
	 *          desired value of the pixel at the given location as double
	 */

	public void setPixelXYDouble(int x, int y, double value) {
		setPixelDouble(getLinearIndexXY___(x,y), value);
	}

	/**
	 * Sets the value of the pixel in the given location as double
	 * 
	 * @param x
	 *          horizontal position of the pixel
	 * @param y
	 *          vertical position of the pixel
	 * @param z
	 *          depth position of the pixel
	 * @param value
	 *          desired value of the pixel at the given location as double
	 */

	public void setPixelXYZDouble(int x, int y, int z, double value) {
		setPixelDouble(getLinearIndexXYZ__(x,y, z), value);
	}

	/**
	 * Sets the value of the pixel in the given location as double
	 * 
	 * @param x
	 *          horizontal position of the pixel
	 * @param y
	 *          vertical position of the pixel
	 * @param b
	 *          channel number of the pixel
	 * @param value
	 *          desired value of the pixel at the given location as double
	 */

	public void setPixelXYBDouble(int x, int y, int b, double value) {
		setPixelDouble(getLinearIndexXY__B(x,y,b), value);
	}

	/**
	 * Sets the value of the pixel in the given location as double
	 * 
	 * @param x
	 *          horizontal position of the pixel
	 * @param y
	 *          vertical position of the pixel
	 * @param t
	 *          time position of the pixel
	 * @param value
	 *          desired value of the pixel at the given location as double
	 */

	public void setPixelXYTDouble(int x, int y, int t, double value) {
		setPixelDouble( getLinearIndexXY_T_(x,y,t), value);
	}

	/**
	 * Sets the value of the pixel in the given location as double
	 * 
	 * @param x
	 *          horizontal position of the pixel
	 * @param y
	 *          vertical position of the pixel
	 * @param z
	 *          depth position of the pixel
	 * @param t
	 *          time position of the pixel
	 * @param value
	 *          desired value of the pixel at the given location as double
	 */

	public void setPixelXYZTDouble(int x, int y, int z, int t, double value) {
		setPixelDouble(getLinearIndexXYZT_(x,y,z,t),
			value);
	}

	/**
	 * Sets the value of the pixel in the given location as double
	 * 
	 * @param x
	 *          horizontal position of the pixel
	 * @param y
	 *          vertical position of the pixel
	 * @param z
	 *          depth position of the pixel
	 * @param b
	 *          channel number of the pixel
	 * @param value
	 *          desired value of the pixel at the given location as double
	 */

	public void setPixelXYZBDouble(int x, int y, int z, int b, double value) {
		setPixelDouble(getLinearIndexXYZ_B(x,y,z,b),
			value);
	}

	/**
	 * Sets the value of the pixel in the given location as double
	 * 
	 * @param x
	 *          horizontal position of the pixel
	 * @param y
	 *          vertical position of the pixel
	 * @param t
	 *          time position of the pixel
	 * @param b
	 *          channel number of the pixel
	 * @param value
	 *          desired value of the pixel at the given location as double
	 */

	public void setPixelXYTBDouble(int x, int y, int t, int b, double value) {
		setPixelDouble(getLinearIndexXY_TB(x,y,t,b),
			value);
	}

	/**
	 * Sets the value of the pixel in the given location as double
	 * 
	 * @param x
	 *          horizontal position of the pixel
	 * @param y
	 *          vertical position of the pixel
	 * @param z
	 *          depth position of the pixel
	 * @param t
	 *          time position of the pixel
	 * @param b
	 *          channel number of the pixel
	 * @param value
	 *          desired value of the pixel at the given location as double
	 */

	public void setPixelXYZTBDouble(int x, int y, int z, int t, int b,
		double value) {
		setPixelDouble(getLinearIndexXYZTB(x,y,z,t,b), value);
	}



	/**
	 * Gets the value of the pixel in the given location as int
	 * 
	 * @param x
	 *          horizontal position of the desired pixel
	 * @param y
	 *          vertical position of the desired pixel
	 * @param z
	 *          depth position of the desired pixel
	 * @param t
	 *          time position of the desired pixel
	 * @param b
	 *          channel number of the desired pixel
	 * @return the value of the pixel in the given location as int
	 */
	public int getPixelInt(int x, int y, int z, int t, int b) {
		return getPixelInt(getLinearIndexXYZTB(x,y,z,t,b));
	}

	/**
	 * Gets the value of the pixel in the given location as int
	 * 
	 * @param x
	 *          horizontal position of the desired pixel
	 * @param y
	 *          vertical position of the desired pixel
	 * @return the value of the pixel in the given location as int
	 */

	public int getPixelXYInt(int x, int y) {
		return getPixelInt(getLinearIndexXY___(x,y));
	}

	/**
	 * Gets the value of the pixel in the given location as int
	 * 
	 * @param x
	 *          horizontal position of the desired pixel
	 * @param y
	 *          vertical position of the desired pixel
	 * @param z
	 *          depth position of the desired pixel
	 * @return the value of the pixel in the given location as int
	 */

	public int getPixelXYZInt(int x, int y, int z) {
		return getPixelInt(getLinearIndexXYZ__(x,y, z));
	}

	/**
	 * Gets the value of the pixel in the given location as int
	 * 
	 * @param x
	 *          horizontal position of the desired pixel
	 * @param y
	 *          vertical position of the desired pixel
	 * @param b
	 *          channel number of the desired pixel
	 * @return the value of the pixel in the given location as int
	 */

	public int getPixelXYBInt(int x, int y, int b) {
		return getPixelInt(getLinearIndexXY__B(x,y,b));
	}

	/**
	 * Gets the value of the pixel in the given location as int
	 * 
	 * @param x
	 *          horizontal position of the desired pixel
	 * @param y
	 *          vertical position of the desired pixel
	 * @param t
	 *          time position of the desired pixel
	 * @return the value of the pixel in the given location as int
	 */

	public int getPixelXYTInt(int x, int y, int t) {
		return getPixelInt( getLinearIndexXY_T_(x,y,t));
	}

	/**
	 * Gets the value of the pixel in the given location as int
	 * 
	 * @param x
	 *          horizontal position of the desired pixel
	 * @param y
	 *          vertical position of the desired pixel
	 * @param z
	 *          depth position of the desired pixel
	 * @param t
	 *          time position of the desired pixel
	 * @return the value of the pixel in the given location as int
	 */

	public int getPixelXYZTInt(int x, int y, int z, int t) {
		return getPixelInt(getLinearIndexXYZT_(x,y,z,t));
	}

	/**
	 * Gets the value of the pixel in the given location as int
	 * 
	 * @param x
	 *          horizontal position of the desired pixel
	 * @param y
	 *          vertical position of the desired pixel
	 * @param z
	 *          depth position of the desired pixel
	 * @param b
	 *          channel number of the desired pixel
	 * @return the value of the pixel in the given location as int
	 */

	public int getPixelXYZBInt(int x, int y, int z, int b) {
		return getPixelInt(getLinearIndexXYZ_B(x,y,z,b));
	}

	/**
	 * Gets the value of the pixel in the given location as int
	 * 
	 * @param x
	 *          horizontal position of the desired pixel
	 * @param y
	 *          vertical position of the desired pixel
	 * @param t
	 *          time position of the desired pixel
	 * @param b
	 *          channel number of the desired pixel
	 * @return the value of the pixel in the given location as int
	 */

	public int getPixelXYTBInt(int x, int y, int t, int b) {
		return getPixelInt(getLinearIndexXY_TB(x,y,t,b));
	}

	/**
	 * Gets the value of the pixel in the given location as int
	 * 
	 * @param x
	 *          horizontal position of the desired pixel
	 * @param y
	 *          vertical position of the desired pixel
	 * @param z
	 *          depth position of the desired pixel
	 * @param t
	 *          time position of the desired pixel
	 * @param b
	 *          channel number of the desired pixel
	 * @return the value of the pixel in the given location as int
	 */

	public int getPixelXYZTBInt(int x, int y, int z, int t, int b) {
		return getPixelInt(getLinearIndexXYZTB(x,y,z,t,b));
	}

	/**
	 * Sets the value of the pixel in the given location as int
	 * 
	 * @param x
	 *          horizontal position of the pixel
	 * @param y
	 *          vertical position of the pixel
	 * @param z
	 *          depth position of the pixel
	 * @param t
	 *          time position of the pixel
	 * @param b
	 *          channel number of the pixel
	 * @param value
	 *          desired value of the pixel at the given location as int
	 */
	public void setPixelInt(int x, int y, int z, int t, int b, int value) {
		setPixelInt(getLinearIndexXYZTB(x,y,z,t,b), value);
	}

	/**
	 * Sets the value of the pixel in the given location as int
	 * 
	 * @param x
	 *          horizontal position of the pixel
	 * @param y
	 *          vertical position of the pixel
	 * @param value
	 *          desired value of the pixel at the given location as int
	 */

	public void setPixelXYInt(int x, int y, int value) {
		setPixelInt(getLinearIndexXY___(x,y), value);
	}

	/**
	 * Sets the value of the pixel in the given location as int
	 * 
	 * @param x
	 *          horizontal position of the pixel
	 * @param y
	 *          vertical position of the pixel
	 * @param z
	 *          depth position of the pixel
	 * @param value
	 *          desired value of the pixel at the given location as int
	 */

	public void setPixelXYZInt(int x, int y, int z, int value) {
		setPixelInt(getLinearIndexXYZ__(x,y,z), value);
	}

	/**
	 * Sets the value of the pixel in the given location as int
	 * 
	 * @param x
	 *          horizontal position of the pixel
	 * @param y
	 *          vertical position of the pixel
	 * @param b
	 *          channel number of the pixel
	 * @param value
	 *          desired value of the pixel at the given location as int
	 */

	public void setPixelXYBInt(int x, int y, int b, int value) {
		setPixelInt(getLinearIndexXY__B(x,y,b), value);
	}

	/**
	 * Sets the value of the pixel in the given location as int
	 * 
	 * @param x
	 *          horizontal position of the pixel
	 * @param y
	 *          vertical position of the pixel
	 * @param t
	 *          time position of the pixel
	 * @param value
	 *          desired value of the pixel at the given location as int
	 */

	public void setPixelXYTInt(int x, int y, int t, int value) {
		setPixelInt(getLinearIndexXY_T_(x,y,t), value);
	}

	/**
	 * Sets the value of the pixel in the given location as int
	 * 
	 * @param x
	 *          horizontal position of the pixel
	 * @param y
	 *          vertical position of the pixel
	 * @param z
	 *          depth position of the pixel
	 * @param t
	 *          time position of the pixel
	 * @param value
	 *          desired value of the pixel at the given location as int
	 */

	public void setPixelXYZTInt(int x, int y, int z, int t, int value) {
		setPixelInt(getLinearIndexXYZT_(x,y,z,t), value);
	}

	/**
	 * Sets the value of the pixels in the given location as a vector across all
	 * channels
	 * 
	 * @param x
	 *          the horizontal index
	 * @param y
	 *          the vertical index
	 * @param z
	 *          depth position of the pixel
	 * @param t
	 *          time position of the pixel
	 * @param value
	 *          the desired vectorial value
	 */
	public final void setVectorPixelXYZTInt(int x, int y, int z, int t, int[] value) {
		for (int b = 0; b < bdim; b++)
			setPixelXYZTBInt(x, y, z, t, b, value[b]);
	}

	/**
	 * Sets the value of the pixel in the given location as int
	 * 
	 * @param x
	 *          horizontal position of the pixel
	 * @param y
	 *          vertical position of the pixel
	 * @param z
	 *          depth position of the pixel
	 * @param b
	 *          channel number of the pixel
	 * @param value
	 *          desired value of the pixel at the given location as int
	 */

	public void setPixelXYZBInt(int x, int y, int z, int b, int value) {
		setPixelInt(getLinearIndexXYZ_B(x,y,z,b), value);
	}

	/**
	 * Sets the value of the pixel in the given location as int
	 * 
	 * @param x
	 *          horizontal position of the pixel
	 * @param y
	 *          vertical position of the pixel
	 * @param t
	 *          time position of the pixel
	 * @param b
	 *          channel number of the pixel
	 * @param value
	 *          desired value of the pixel at the given location as int
	 */

	public void setPixelXYTBInt(int x, int y, int t, int b, int value) {
		setPixelInt(getLinearIndexXY_TB(x,y,t,b), value);
	}

	/**
	 * Sets the value of the pixel in the given location as int
	 * 
	 * @param x
	 *          horizontal position of the pixel
	 * @param y
	 *          vertical position of the pixel
	 * @param z
	 *          depth position of the pixel
	 * @param t
	 *          time position of the pixel
	 * @param b
	 *          channel number of the pixel
	 * @param value
	 *          desired value of the pixel at the given location as int
	 */

	public void setPixelXYZTBInt(int x, int y, int z, int t, int b, int value) {
		setPixelInt(getLinearIndexXYZTB(x,y,z,t,b), value);
	}

	/**
	 * Gets the value of the pixel in the given location as byte
	 * 
	 * @param x
	 *          horizontal position of the desired pixel
	 * @param y
	 *          vertical position of the desired pixel
	 * @param z
	 *          depth position of the desired pixel
	 * @param t
	 *          time position of the desired pixel
	 * @param b
	 *          channel number of the desired pixel
	 * @return the value of the pixel in the given location as byte
	 */
	public int getPixelByte(int x, int y, int z, int t, int b) {
		return getPixelByte(getLinearIndexXYZTB(x,y,z,t,b));
	}

	/**
	 * Gets the value of the pixel in the given location as byte
	 * 
	 * @param x
	 *          horizontal position of the desired pixel
	 * @param y
	 *          vertical position of the desired pixel
	 * @return the value of the pixel in the given location as byte
	 */

	public int getPixelXYByte(int x, int y) {
		return getPixelByte(getLinearIndexXY___(x,y));
	}

	/**
	 * Gets the value of the pixel in the given location as byte
	 * 
	 * @param x
	 *          horizontal position of the desired pixel
	 * @param y
	 *          vertical position of the desired pixel
	 * @param z
	 *          depth position of the desired pixel
	 * @return the value of the pixel in the given location as byte
	 */

	public int getPixelXYZByte(int x, int y, int z) {
		return getPixelByte(getLinearIndexXYZ__(x,y,z));
	}

	/**
	 * Gets the value of the pixel in the given location as byte
	 * 
	 * @param x
	 *          horizontal position of the desired pixel
	 * @param y
	 *          vertical position of the desired pixel
	 * @param b
	 *          channel number of the desired pixel
	 * @return the value of the pixel in the given location as byte
	 */

	public int getPixelXYBByte(int x, int y, int b) {
		return getPixelByte(getLinearIndexXY__B(x,y,b));
	}

	/**
	 * Gets the value of the pixel in the given location as byte
	 * 
	 * @param x
	 *          horizontal position of the desired pixel
	 * @param y
	 *          vertical position of the desired pixel
	 * @param t
	 *          time position of the desired pixel
	 * @return the value of the pixel in the given location as byte
	 */

	public int getPixelXYTByte(int x, int y, int t) {
		return getPixelByte(getLinearIndexXY_T_(x,y,t));
	}

	/**
	 * Gets the value of the pixel in the given location as byte
	 * 
	 * @param x
	 *          horizontal position of the desired pixel
	 * @param y
	 *          vertical position of the desired pixel
	 * @param z
	 *          depth position of the desired pixel
	 * @param t
	 *          time position of the desired pixel
	 * @return the value of the pixel in the given location as byte
	 */

	public int getPixelXYZTByte(int x, int y, int z, int t) {
		return getPixelByte(getLinearIndexXYZT_(x,y,z,t));
	}

	/**
	 * Gets the value of the pixel in the given location as byte
	 * 
	 * @param x
	 *          horizontal position of the desired pixel
	 * @param y
	 *          vertical position of the desired pixel
	 * @param z
	 *          depth position of the desired pixel
	 * @param b
	 *          channel number of the desired pixel
	 * @return the value of the pixel in the given location as byte
	 */

	public int getPixelXYZBByte(int x, int y, int z, int b) {
		return getPixelByte(getLinearIndexXYZ_B(x,y,z,b));
	}

	/**
	 * Gets the value of the pixel in the given location as byte
	 * 
	 * @param x
	 *          horizontal position of the desired pixel
	 * @param y
	 *          vertical position of the desired pixel
	 * @param t
	 *          time position of the desired pixel
	 * @param b
	 *          channel number of the desired pixel
	 * @return the value of the pixel in the given location as byte
	 */

	public int getPixelXYTBByte(int x, int y, int t, int b) {
		return getPixelByte(getLinearIndexXY_TB(x,y,t,b));
	}

	/**
	 * Gets the value of the pixel in the given location as byte
	 * 
	 * @param x
	 *          horizontal position of the desired pixel
	 * @param y
	 *          vertical position of the desired pixel
	 * @param z
	 *          depth position of the desired pixel
	 * @param t
	 *          time position of the desired pixel
	 * @param b
	 *          channel number of the desired pixel
	 * @return the value of the pixel in the given location as byte
	 */

	public int getPixelXYZTBByte(int x, int y, int z, int t, int b) {
		return getPixelByte(getLinearIndexXYZTB(x,y,z,t,b));
	}

	/**
	 * Sets the value of the pixel in the given location as byte
	 * 
	 * @param x
	 *          horizontal position of the pixel
	 * @param y
	 *          vertical position of the pixel
	 * @param z
	 *          depth position of the pixel
	 * @param t
	 *          time position of the pixel
	 * @param b
	 *          channel number of the pixel
	 * @param value
	 *          desired value of the pixel at the given location as byte
	 */
	public void setPixelByte(int x, int y, int z, int t, int b, int value) {
		setPixelByte(getLinearIndexXYZTB(x,y,z,t,b), value);
	}

	/**
	 * Sets the value of the pixel in the given location as byte
	 * 
	 * @param x
	 *          horizontal position of the pixel
	 * @param y
	 *          vertical position of the pixel
	 * @param value
	 *          desired value of the pixel at the given location as byte
	 */

	public void setPixelXYByte(int x, int y, int value) {
		setPixelByte(getLinearIndexXY___(x,y), value);
	}

	/**
	 * Sets the value of the pixel in the given location as byte
	 * 
	 * @param x
	 *          horizontal position of the pixel
	 * @param y
	 *          vertical position of the pixel
	 * @param z
	 *          depth position of the pixel
	 * @param value
	 *          desired value of the pixel at the given location as byte
	 */

	public void setPixelXYZByte(int x, int y, int z, int value) {
		setPixelByte(getLinearIndexXYZ__(x,y,z), value);
	}

	/**
	 * Sets the value of the pixel in the given location as byte
	 * 
	 * @param x
	 *          horizontal position of the pixel
	 * @param y
	 *          vertical position of the pixel
	 * @param b
	 *          channel number of the pixel
	 * @param value
	 *          desired value of the pixel at the given location as byte
	 */

	public void setPixelXYBByte(int x, int y, int b, int value) {
		setPixelByte(getLinearIndexXY__B(x,y,b), value);
	}

	/**
	 * Sets the value of the pixel in the given location as byte
	 * 
	 * @param x
	 *          horizontal position of the pixel
	 * @param y
	 *          vertical position of the pixel
	 * @param t
	 *          time position of the pixel
	 * @param value
	 *          desired value of the pixel at the given location as byte
	 */

	public void setPixelXYTByte(int x, int y, int t, int value) {
		setPixelByte(getLinearIndexXY_T_(x,y,t), value);
	}

	/**
	 * Sets the value of the pixel in the given location as byte
	 * 
	 * @param x
	 *          horizontal position of the pixel
	 * @param y
	 *          vertical position of the pixel
	 * @param z
	 *          depth position of the pixel
	 * @param t
	 *          time position of the pixel
	 * @param value
	 *          desired value of the pixel at the given location as byte
	 */

	public void setPixelXYZTByte(int x, int y, int z, int t, int value) {
		setPixelByte(getLinearIndexXYZT_(x,y,z,t), value);
	}

	/**
	 * Sets the value of the pixels in the given location as a vector across all
	 * channels
	 * 
	 * @param x
	 *          the horizontal index
	 * @param y
	 *          the vertical index
	 * @param z
	 *          depth position of the pixel
	 * @param t
	 *          time position of the pixel
	 * @param value
	 *          the desired vectorial value
	 */
	public final void setVectorPixelXYZTByte(int x, int y, int z, int t, int[] value) {
		for (int b = 0; b < bdim; b++)
			setPixelXYZTBByte(x, y, z, t, b, value[b]);
	}

	/**
	 * Sets the value of the pixel in the given location as byte
	 * 
	 * @param x
	 *          horizontal position of the pixel
	 * @param y
	 *          vertical position of the pixel
	 * @param z
	 *          depth position of the pixel
	 * @param b
	 *          channel number of the pixel
	 * @param value
	 *          desired value of the pixel at the given location as byte
	 */

	public void setPixelXYZBByte(int x, int y, int z, int b, int value) {
		setPixelByte(getLinearIndexXYZ_B(x,y,z,b), value);
	}

	/**
	 * Sets the value of the pixel in the given location as byte
	 * 
	 * @param x
	 *          horizontal position of the pixel
	 * @param y
	 *          vertical position of the pixel
	 * @param t
	 *          time position of the pixel
	 * @param b
	 *          channel number of the pixel
	 * @param value
	 *          desired value of the pixel at the given location as byte
	 */

	public void setPixelXYTBByte(int x, int y, int t, int b, int value) {
		setPixelByte(getLinearIndexXY_TB(x,y,t,b), value);
	}

	/**
	 * Sets the value of the pixel in the given location as byte
	 * 
	 * @param x
	 *          horizontal position of the pixel
	 * @param y
	 *          vertical position of the pixel
	 * @param z
	 *          depth position of the pixel
	 * @param t
	 *          time position of the pixel
	 * @param b
	 *          channel number of the pixel
	 * @param value
	 *          desired value of the pixel at the given location as byte
	 */

	public void setPixelXYZTBByte(int x, int y, int z, int t, int b, int value) {
		setPixelByte(getLinearIndexXYZTB(x,y,z,t,b), value);
	}

	/**
	 * Gets the value of the pixel in the given location as boolean
	 * 
	 * @param x
	 *          horizontal position of the desired pixel
	 * @param y
	 *          vertical position of the desired pixel
	 * @param z
	 *          depth position of the desired pixel
	 * @param t
	 *          time position of the desired pixel
	 * @param b
	 *          channel number of the desired pixel
	 * @return the value of the pixel in the given location as boolean
	 */
	public boolean getPixelBoolean(int x, int y, int z, int t, int b) {
		return getPixelBoolean(getLinearIndexXYZTB(x,y,z,t,b));
	}

	/**
	 * Gets the value of the pixel in the given location as boolean
	 * 
	 * @param x
	 *          horizontal position of the desired pixel
	 * @param y
	 *          vertical position of the desired pixel
	 * @return the value of the pixel in the given location as boolean
	 */

	public boolean getPixelXYBoolean(int x, int y) {
		return getPixelBoolean(getLinearIndexXY___(x,y));
	}

	/**
	 * Gets the value of the pixel in the given location as boolean
	 * 
	 * @param x
	 *          horizontal position of the desired pixel
	 * @param y
	 *          vertical position of the desired pixel
	 * @param z
	 *          depth position of the desired pixel
	 * @return the value of the pixel in the given location as boolean
	 */

	public boolean getPixelXYZBoolean(int x, int y, int z) {
		return getPixelBoolean(getLinearIndexXYZ__(x,y,z));
	}

	/**
	 * Gets the value of the pixel in the given location as boolean
	 * 
	 * @param x
	 *          horizontal position of the desired pixel
	 * @param y
	 *          vertical position of the desired pixel
	 * @param b
	 *          channel number of the desired pixel
	 * @return the value of the pixel in the given location as boolean
	 */

	public boolean getPixelXYBBoolean(int x, int y, int b) {
		return getPixelBoolean(getLinearIndexXY__B(x,y,b));
	}

	/**
	 * Gets the value of the pixel in the given location as boolean
	 * 
	 * @param x
	 *          horizontal position of the desired pixel
	 * @param y
	 *          vertical position of the desired pixel
	 * @param t
	 *          time position of the desired pixel
	 * @return the value of the pixel in the given location as boolean
	 */

	public boolean getPixelXYTBoolean(int x, int y, int t) {
		return getPixelBoolean(getLinearIndexXY_T_(x,y,t));
	}

	/**
	 * Gets the value of the pixel in the given location as boolean
	 * 
	 * @param x
	 *          horizontal position of the desired pixel
	 * @param y
	 *          vertical position of the desired pixel
	 * @param z
	 *          depth position of the desired pixel
	 * @param t
	 *          time position of the desired pixel
	 * @return the value of the pixel in the given location as boolean
	 */

	public boolean getPixelXYZTBoolean(int x, int y, int z, int t) {
		return getPixelBoolean(getLinearIndexXYZT_(x,y,z,t));
	}

	/**
	 * Gets the value of the pixel in the given location as boolean
	 * 
	 * @param x
	 *          horizontal position of the desired pixel
	 * @param y
	 *          vertical position of the desired pixel
	 * @param z
	 *          depth position of the desired pixel
	 * @param b
	 *          channel number of the desired pixel
	 * @return the value of the pixel in the given location as boolean
	 */

	public boolean getPixelXYZBBoolean(int x, int y, int z, int b) {
		return getPixelBoolean(getLinearIndexXYZ_B(x,y,z,b));
	}

	/**
	 * Gets the value of the pixel in the given location as boolean
	 * 
	 * @param x
	 *          horizontal position of the desired pixel
	 * @param y
	 *          vertical position of the desired pixel
	 * @param t
	 *          time position of the desired pixel
	 * @param b
	 *          channel number of the desired pixel
	 * @return the value of the pixel in the given location as boolean
	 */

	public boolean getPixelXYTBBoolean(int x, int y, int t, int b) {
		return getPixelBoolean(getLinearIndexXY_TB(x,y,t,b));
	}

	/**
	 * Gets the value of the pixel in the given location as boolean
	 * 
	 * @param x
	 *          horizontal position of the desired pixel
	 * @param y
	 *          vertical position of the desired pixel
	 * @param z
	 *          depth position of the desired pixel
	 * @param t
	 *          time position of the desired pixel
	 * @param b
	 *          channel number of the desired pixel
	 * @return the value of the pixel in the given location as boolean
	 */

	public boolean getPixelXYZTBBoolean(int x, int y, int z, int t, int b) {
		return getPixelBoolean(getLinearIndexXYZTB(x,y,z,t,b));
	}

	/**
	 * Sets the value of the pixel in the given location as boolean
	 * 
	 * @param x
	 *          horizontal position of the pixel
	 * @param y
	 *          vertical position of the pixel
	 * @param z
	 *          depth position of the pixel
	 * @param t
	 *          time position of the pixel
	 * @param b
	 *          channel number of the pixel
	 * @param value
	 *          desired value of the pixel at the given location as boolean
	 */
	public void setPixelBoolean(int x, int y, int z, int t, int b, boolean value) {
		setPixelBoolean(getLinearIndexXYZTB(x,y,z,t,b), value);
	}

	/**
	 * Sets the value of the pixel in the given location as boolean
	 * 
	 * @param x
	 *          horizontal position of the pixel
	 * @param y
	 *          vertical position of the pixel
	 * @param value
	 *          desired value of the pixel at the given location as boolean
	 */

	public void setPixelXYBoolean(int x, int y, boolean value) {
		setPixelBoolean(getLinearIndexXY___(x,y), value);
	}

	/**
	 * Sets the value of the pixel in the given location as boolean
	 * 
	 * @param x
	 *          horizontal position of the pixel
	 * @param y
	 *          vertical position of the pixel
	 * @param z
	 *          depth position of the pixel
	 * @param value
	 *          desired value of the pixel at the given location as boolean
	 */

	public void setPixelXYZBoolean(int x, int y, int z, boolean value) {
		setPixelBoolean(getLinearIndexXYZ__(x,y,z), value);
	}

	/**
	 * Sets the value of the pixel in the given location as boolean
	 * 
	 * @param x
	 *          horizontal position of the pixel
	 * @param y
	 *          vertical position of the pixel
	 * @param b
	 *          channel number of the pixel
	 * @param value
	 *          desired value of the pixel at the given location as boolean
	 */

	public void setPixelXYBBoolean(int x, int y, int b, boolean value) {
		setPixelBoolean(getLinearIndexXY__B(x,y,b), value);
	}

	/**
	 * Sets the value of the pixel in the given location as boolean
	 * 
	 * @param x
	 *          horizontal position of the pixel
	 * @param y
	 *          vertical position of the pixel
	 * @param t
	 *          time position of the pixel
	 * @param value
	 *          desired value of the pixel at the given location as boolean
	 */

	public void setPixelXYTBoolean(int x, int y, int t, boolean value) {
		setPixelBoolean(getLinearIndexXY_T_(x,y,t), value);
	}

	/**
	 * Sets the value of the pixel in the given location as boolean
	 * 
	 * @param x
	 *          horizontal position of the pixel
	 * @param y
	 *          vertical position of the pixel
	 * @param z
	 *          depth position of the pixel
	 * @param t
	 *          time position of the pixel
	 * @param value
	 *          desired value of the pixel at the given location as boolean
	 */

	public void setPixelXYZTBoolean(int x, int y, int z, int t, boolean value) {
		setPixelBoolean( getLinearIndexXYZT_(x,y,z,t), value);
	}

	/**
	 * Sets the value of the pixel in the given location as boolean
	 * 
	 * @param x
	 *          horizontal position of the pixel
	 * @param y
	 *          vertical position of the pixel
	 * @param z
	 *          depth position of the pixel
	 * @param b
	 *          channel number of the pixel
	 * @param value
	 *          desired value of the pixel at the given location as boolean
	 */

	public void setPixelXYZBBoolean(int x, int y, int z, int b, boolean value) {
		setPixelBoolean(getLinearIndexXYZ_B(x,y,z,b),value);
	}

	/**
	 * Sets the value of the pixel in the given location as boolean
	 * 
	 * @param x
	 *          horizontal position of the pixel
	 * @param y
	 *          vertical position of the pixel
	 * @param t
	 *          time position of the pixel
	 * @param b
	 *          channel number of the pixel
	 * @param value
	 *          desired value of the pixel at the given location as boolean
	 */

	public void setPixelXYTBBoolean(int x, int y, int t, int b, boolean value) {
		setPixelBoolean(getLinearIndexXY_TB(x,y,t,b),value);
	}

	/**
	 * Sets the value of the pixel in the given location as boolean
	 * 
	 * @param x
	 *          horizontal position of the pixel
	 * @param y
	 *          vertical position of the pixel
	 * @param z
	 *          depth position of the pixel
	 * @param t
	 *          time position of the pixel
	 * @param b
	 *          channel number of the pixel
	 * @param value
	 *          desired value of the pixel at the given location as boolean
	 */

	public void setPixelXYZTBBoolean(int x, int y, int z, int t, int b,
		boolean value) {
		setPixelBoolean(getLinearIndexXYZTB(x,y,z,t,b), value);
	}

	/**
	 * Copy an image pixel
	 * 
	 * @param input
	 *          source image
	 * @param x1
	 *          source x position
	 * @param y1
	 *          source y position
	 * @param z1
	 *          source z position
	 * @param t1
	 *          source t position
	 * @param b1
	 *          source b position
	 * @param x2
	 *          destination x position
	 * @param y2
	 *          destination y position
	 * @param z2
	 *          destination z position
	 * @param t2
	 *          destination t position
	 * @param b2
	 *          destination b position
	 */
	public abstract void setPixel(Image input, int x1, int y1, int z1, int t1,
		int b1, int x2, int y2, int z2, int t2, int b2);

	/**
	 * Copy an image pixel
	 * 
	 * @param input
	 *          source image
	 * @param x
	 *          x position
	 * @param y
	 *          y position
	 * @param z
	 *          z position
	 * @param t
	 *          t position
	 * @param b
	 *          b position
	 */
	public final void setPixel(Image input, int x, int y, int z, int t, int b) {
		setPixel(input, x, y, z, t, b, x, y, z, t, b);
	}

	/**
	 * Gets as a monochannel ByteImage a given dimension
	 * 
	 * @param z
	 *          depth dimension
	 * @param t
	 *          time dimension
	 * @param b
	 *          channel number
	 * @return the monochannel ByteImage corresponding to the given arguments
	 */

	@Deprecated
	public ByteImage getByteChannelZTB(int z, int t, int b) {
		ByteImage tmp = new ByteImage(xdim, ydim, 1, 1, 1);

		for (int x = 0; x < xdim; x++) {
			for (int y = 0; y < ydim; y++)
				tmp.setPixelXYByte(x, y, getPixelXYZTBByte(x, y, z, t, b));
		}
		return tmp;
	}

	/**
	 * TEST SABLIER slice ZY channel Gets as a monochannel ByteImage a given
	 * dimension
	 * 
	 * @author M. Sablier
	 * @param x
	 *          depth dimension
	 * @param t
	 *          time dimension
	 * @param b
	 *          channel number
	 * @return the monochannel ByteImage corresponding to the given arguments
	 */

	@Deprecated
	public ByteImage getByteChannelXTB(int x, int t, int b) {
		ByteImage tmp = new ByteImage(zdim, ydim, 1, 1, 1);

		for (int z = 0; z < zdim; z++) {
			for (int y = 0; y < ydim; y++)
				tmp.setPixelXYByte(z, y, getPixelXYZTBByte(x, y, z, t, b));
		}
		return tmp;
	}

	/**
	 * TEST SABLIER slice XZ channel Gets as a monochannel ByteImage a given
	 * dimension
	 * 
	 * @author M. Sablier
	 * @param y
	 *          depth dimension
	 * @param t
	 *          time dimension
	 * @param b
	 *          channel number
	 * @return the monochannel ByteImage corresponding to the given arguments
	 */

	@Deprecated
	public ByteImage getByteChannelYTB(int y, int t, int b) {
		ByteImage tmp = new ByteImage(xdim, zdim, 1, 1, 1);

		for (int x = 0; x < xdim; x++) { // inversion x<->z : pas d'incidence
			// sur le placement des values,
			for (int z = 0; z < zdim; z++)
				// moins efficace (?) en lecture memoire de l'image 5D ori-dans
				// Frame=img
				tmp.setPixelXYByte(x, z, getPixelXYZTBByte(x, y, z, t, b));
		}
		return tmp;
	}

	/**
	 * Returns the tristumulus color image of the given time and depth dimensions
	 * 
	 * @param z
	 *          depth dimension
	 * @param t
	 *          time dimension
	 * @return the tristumulus color ByteImage corresponding to the given
	 *         arguments or null if channel number is not 3
	 */
	@Deprecated
	public ByteImage getColorByteChannelZT(int z, int t) {
		if (bdim != 3)
			return null;

		ByteImage tmp = new ByteImage(xdim, ydim, 1, 1, 3);

		for (int b = 0; b < bdim; b++) {
			for (int x = 0; x < xdim; x++) {
				for (int y = 0; y < ydim; y++)
					tmp.setPixelXYBByte(x, y, b, getPixelXYZTBByte(x, y, z, t, b));
			}
		}

		return tmp;
	}

	/**
	 * TEST SABLIER slice ZY color Returns the tristumulus color image of the
	 * given time and depth dimensions
	 * 
	 * @author M. Sablier
	 * @param x
	 *          depth dimension
	 * @param t
	 *          time dimension
	 * @return the tristumulus color ByteImage corresponding to the given
	 *         arguments or null if channel number is not 3
	 */

	@Deprecated
	public ByteImage getColorByteChannelXT(int x, int t) {
		if (bdim != 3)
			return null;

		ByteImage tmp = new ByteImage(zdim, ydim, 1, 1, 3);

		for (int b = 0; b < bdim; b++) {
			for (int z = 0; z < zdim; z++) {
				for (int y = 0; y < ydim; y++)
					tmp.setPixelXYBByte(z, y, b, getPixelXYZTBByte(x, y, z, t, b));
			}
		}

		return tmp;
	}

	/**
	 * TEST SABLIER slice XZ color Returns the tristumulus color image of the
	 * given time and depth dimensions
	 * 
	 * @author M. Sablier
	 * @param y
	 *          depth dimension
	 * @param t
	 *          time dimension
	 * @return the tristumulus color ByteImage corresponding to the given
	 *         arguments or null if channel number is not 3
	 */

	@Deprecated
	public ByteImage getColorByteChannelYT(int y, int t) {
		if (bdim != 3)
			return null;

		ByteImage tmp = new ByteImage(xdim, zdim, 1, 1, 3);

		for (int b = 0; b < bdim; b++) {
			for (int x = 0; x < xdim; x++) {
				for (int z = 0; z < zdim; z++)
					tmp.setPixelXYBByte(x, z, b, getPixelXYZTBByte(x, y, z, t, b));
			}
		}

		return tmp;
	}

	/**
	 * Returns the (possibly 3-D and multivalued) image of the given time
	 * 
	 * @param t
	 *          time dimension
	 * @return ByteImage corresponding to the given argument
	 */

	@Deprecated
	public Image getByteFrame(int t) {
		Image tmp = new ByteImage(xdim, ydim, zdim, 1, bdim);

		for (int b = 0; b < bdim; b++)
			for (int z = 0; z < zdim; z++)
				for (int x = 0; x < xdim; x++)
					for (int y = 0; y < ydim; y++)
						tmp.setPixelXYZBByte(x, y, z, b, getPixelXYZTBByte(x, y, z, t, b));
		return tmp;
	}

	/**
	 * Sets the (possibly 3-D and multivalued) image of the given time Does
	 * nothing if the parameter image size is incompatible with the image
	 * 
	 * @param t
	 *          time dimension
	 * @param img
	 *          corresponding to the given argument
	 */
	@Deprecated
	public void setFrame(int t, Image img) {
		if (img.getXDim() == xdim && img.getYDim() == ydim && img.getZDim() == zdim
			&& img.getBDim() == bdim)

			for (int b = 0; b < bdim; b++)
				for (int z = 0; z < zdim; z++)
					for (int x = 0; x < xdim; x++)
						for (int y = 0; y < ydim; y++)
							setPixelXYZTBDouble(x, y, z, t, b, img.getPixelXYZBDouble(x, y,
								z, b));
	}

	/**
	 * Returns the (possibly 3-D+t) image of the given band
	 * 
	 * @param b
	 *          band dimension
	 * @return Image corresponding to the given argument
	 */

	@Deprecated
	public Image getByteChannel(int b) {
		Image tmp = new ByteImage(xdim, ydim, zdim, tdim, 1);

		for (int t = 0; t < tdim; t++)
			for (int z = 0; z < zdim; z++)
				for (int x = 0; x < xdim; x++)
					for (int y = 0; y < ydim; y++)
						tmp.setPixelXYZTByte(x, y, z, t, getPixelXYZTBByte(x, y, z, t, b));
		return tmp;
	}

	/**
	 * Sets the (possibly 3-D+t) image of the given band Does nothing if the
	 * parameter image size is incompatible with the image
	 * 
	 * @param b
	 *          band dimension
	 * @param img
	 *          corresponding to the given argument
	 */
	@Deprecated
	public void setChannel(int b, Image img) {
		if (img.getXDim() == xdim && img.getYDim() == ydim && img.getZDim() == zdim
			&& img.getTDim() == tdim)

			for (int t = 0; t < tdim; t++)
				for (int z = 0; z < zdim; z++)
					for (int x = 0; x < xdim; x++)
						for (int y = 0; y < ydim; y++)
							setPixelXYZTBDouble(x, y, z, t, b, img.getPixelXYZTDouble(x, y,
								z, t));
	}

	/**
	 * Sets the (possibly multiband and temporal) image of the given band Does
	 * nothing if the parameter image size is incompatible with the image
	 * 
	 * @param z
	 *          slice dimension
	 * @param img
	 *          corresponding to the given argument
	 */
	@Deprecated
	public void setSlice(int z, Image img) {
		if (img.getXDim() == xdim && img.getYDim() == ydim && img.getTDim() == tdim
			&& img.getBDim() == bdim)
			for (int b = 0; b < bdim; b++)
				for (int t = 0; t < tdim; t++)
					for (int x = 0; x < xdim; x++)
						for (int y = 0; y < ydim; y++)
							setPixelXYZTBDouble(x, y, z, t, b, img.getPixelXYTBDouble(x, y,
								z, b));
	}

	/**
	 * Converts the 5-D image to an array of 4-D images
	 * 
	 * @param dim
	 *          the indice of dimension to be used as the array dimension (among
	 *          X, Y, Z, T, B)
	 * @return the array of 4-D images
	 */
	public final Image[] to4DArray(int dim) {
		Image tab[] = null;
		switch (dim) {
		case X:
			tab = new Image[xdim];
			for (int i = 0; i < tab.length; i++)
				tab[i] = getImage4D(i, X);
			break;
		case Y:
			tab = new Image[ydim];
			for (int i = 0; i < tab.length; i++)
				tab[i] = getImage4D(i, Y);
			break;
		case Z:
			tab = new Image[zdim];
			for (int i = 0; i < tab.length; i++)
				tab[i] = getImage4D(i, Z);
			break;
		case T:
			tab = new Image[tdim];
			for (int i = 0; i < tab.length; i++)
				tab[i] = getImage4D(i, T);
			break;
		case B:
			tab = new Image[bdim];
			for (int i = 0; i < tab.length; i++)
				tab[i] = getImage4D(i, B);
			break;
		}
		return tab;
	}

	/**
	 * Returns a 2-D image (X x Y) at a specif (Z,T,B) position
	 * 
	 * @param z
	 *          the Z position
	 * @param t
	 *          the T position
	 * @param b
	 *          the B position
	 * @return the 2D image corresponding to the Z,T,B position
	 */
	public final Image getImage2D(int z, int t, int b) {
		Image res = newInstance(xdim, ydim, 1, 1, 1);
		for (int x = 0; x < xdim; x++)
			for (int y = 0; y < ydim; y++)
				res.setPixel(this, x, y, 0, 0, 0, x, y, z, t, b);
		// res.setPixelXYDouble(x, y, getPixelDouble(x, y, z, t, b));
		return res;
	}

	/**
	 * Set a 2-D image (X x Y) at a specif (Z,T,B) position
	 * 
	 * @param tmp the input image (2-D)
	 * @param z the Z position
	 * @param t the T position
	 * @param b the B position
	 */
	public final void setImage2D(Image tmp, int z, int t, int b) {
		for (int x = 0; x < tmp.xdim; x++)
			for (int y = 0; y < tmp.ydim; y++)
				// setPixelDouble(x, y, z, t, b, tmp.getPixelXYDouble(x, y));
				setPixel(tmp, x, y, z, t, b, x, y, 0, 0, 0);
	}

	/**
	 * Returns a 4-D image by discarding one dimension
	 * 
	 * @param i
	 *          the indice to select
	 * @param dim
	 *          the indice of the dimension to discard (among X, Y, Z, T, B)
	 * @return Image corresponding to the given argument
	 */
	public final Image getImage4D(int i, int dim) {
		Image tmp = null;
		switch (dim) {
		case X:
			tmp = newInstance(1, ydim, zdim, tdim, bdim);
			for (int x = 0; x < tmp.xdim; x++)
				for (int y = 0; y < tmp.ydim; y++)
					for (int z = 0; z < tmp.zdim; z++)
						for (int t = 0; t < tmp.tdim; t++)
							for (int b = 0; b < tmp.bdim; b++)
								tmp.setPixel(this, x, y, z, t, b, i, y, z, t, b);
			// tmp.setPixelDouble(x, y, z, t, b, getPixelDouble(i, y, z, t, b));
			break;
		case Y:
			tmp = newInstance(xdim, 1, zdim, tdim, bdim);
			for (int x = 0; x < tmp.xdim; x++)
				for (int y = 0; y < tmp.ydim; y++)
					for (int z = 0; z < tmp.zdim; z++)
						for (int t = 0; t < tmp.tdim; t++)
							for (int b = 0; b < tmp.bdim; b++)
								tmp.setPixel(this, x, y, z, t, b, x, i, z, t, b);
			// tmp.setPixelDouble(x, y, z, t, b, getPixelDouble(x, i, z, t, b));
			break;
		case Z:
			tmp = newInstance(xdim, ydim, 1, tdim, bdim);
			for (int x = 0; x < tmp.xdim; x++)
				for (int y = 0; y < tmp.ydim; y++)
					for (int z = 0; z < tmp.zdim; z++)
						for (int t = 0; t < tmp.tdim; t++)
							for (int b = 0; b < tmp.bdim; b++)
								// tmp.setPixelDouble(x, y, z, t, b, getPixelDouble(x, y, i, t,
								// b));
								tmp.setPixel(this, x, y, z, t, b, x, y, i, t, b);
			break;
		case T:
			tmp = newInstance(xdim, ydim, zdim, 1, bdim);
			for (int x = 0; x < tmp.xdim; x++)
				for (int y = 0; y < tmp.ydim; y++)
					for (int z = 0; z < tmp.zdim; z++)
						for (int t = 0; t < tmp.tdim; t++)
							for (int b = 0; b < tmp.bdim; b++)
								// tmp.setPixelDouble(x, y, z, t, b, getPixelDouble(x, y, z, i,
								// b));
								tmp.setPixel(this, x, y, z, t, b, x, y, z, i, b);
			break;
		case B:
			tmp = newInstance(xdim, ydim, zdim, tdim, 1);
			for (int x = 0; x < tmp.xdim; x++)
				for (int y = 0; y < tmp.ydim; y++)
					for (int z = 0; z < tmp.zdim; z++)
						for (int t = 0; t < tmp.tdim; t++)
							for (int b = 0; b < tmp.bdim; b++)
								// tmp.setPixelDouble(x, y, z, t, b, getPixelDouble(x, y, z, t,
								// i));
								tmp.setPixel(this, x, y, z, t, b, x, y, z, t, i);
			break;
		}
		return tmp;
	}

	/**
	 * Set a part of the image as a 4-D image
	 * 
	 * @param tmp
	 *          the input image (has to be 4-D)
	 * @param i
	 *          the indice to select
	 * @param dim
	 *          the indice of the dimension to consider
	 * 
	 * TODO: extend to 5-D inputs
	 */
	public final void setImage4D(Image tmp, int i, int dim) {
		switch (dim) {
		case X:
			if (tmp.getYDim() == ydim && tmp.getZDim() == zdim
				&& tmp.getTDim() == tdim && tmp.getBDim() == bdim)
				for (int x = 0; x < tmp.xdim; x++)
					for (int y = 0; y < tmp.ydim; y++)
						for (int z = 0; z < tmp.zdim; z++)
							for (int t = 0; t < tmp.tdim; t++)
								for (int b = 0; b < tmp.bdim; b++)
									// setPixelDouble(i, y, z, t, b, tmp.getPixelDouble(x, y, z,
									// t,b));
									this.setPixel(tmp, i, y, z, t, b, x, y, z, t, b);
			break;
		case Y:
			if (tmp.getXDim() == xdim && tmp.getZDim() == zdim
				&& tmp.getTDim() == tdim && tmp.getBDim() == bdim)
				for (int x = 0; x < tmp.xdim; x++)
					for (int y = 0; y < tmp.ydim; y++)
						for (int z = 0; z < tmp.zdim; z++)
							for (int t = 0; t < tmp.tdim; t++)
								for (int b = 0; b < tmp.bdim; b++)
									// setPixelDouble(x, i, z, t, b, tmp.getPixelDouble(x, y, z,
									// t,b));
									this.setPixel(tmp, x, i, z, t, b, x, y, z, t, b);
			break;
		case Z:
			if (tmp.getXDim() == xdim && tmp.getYDim() == ydim
				&& tmp.getTDim() == tdim && tmp.getBDim() == bdim)
				for (int x = 0; x < tmp.xdim; x++)
					for (int y = 0; y < tmp.ydim; y++)
						for (int z = 0; z < tmp.zdim; z++)
							for (int t = 0; t < tmp.tdim; t++)
								for (int b = 0; b < tmp.bdim; b++)
									// setPixelDouble(x, y, i, t, b, tmp.getPixelDouble(x, y, z,
									// t,b));
									this.setPixel(tmp, x, y, i, t, b, x, y, z, t, b);
			break;
		case T:
			if (tmp.getXDim() == xdim && tmp.getYDim() == ydim
				&& tmp.getZDim() == zdim && tmp.getBDim() == bdim)
				for (int x = 0; x < tmp.xdim; x++)
					for (int y = 0; y < tmp.ydim; y++)
						for (int z = 0; z < tmp.zdim; z++)
							for (int t = 0; t < tmp.tdim; t++)
								for (int b = 0; b < tmp.bdim; b++)
									// setPixelDouble(x, y, z, i, b, tmp.getPixelDouble(x, y, z,
									// t,b));
									this.setPixel(tmp, x, y, z, i, b, x, y, z, t, b);
			break;
		case B:
			if (tmp.getXDim() == xdim && tmp.getYDim() == ydim
				&& tmp.getZDim() == zdim && tmp.getTDim() == tdim)
				for (int x = 0; x < tmp.xdim; x++)
					for (int y = 0; y < tmp.ydim; y++)
						for (int z = 0; z < tmp.zdim; z++)
							for (int t = 0; t < tmp.tdim; t++)
								for (int b = 0; b < tmp.bdim; b++)
									// setPixelDouble(x, y, z, t, i, tmp.getPixelDouble(x, y, z,
									// t,b));
									this.setPixel(tmp, x, y, z, t, i, x, y, z, t, b);
			break;
		}
	}

	/**
	 * Create a new image with
	 * 
	 * @param index
	 *          index in the chosen dimension
	 * @param nb
	 *          number of duplication
	 * @param dim
	 *          the dimension to duplicate
	 * @return the duplicate dimension
	 */
	public final Image duplicateDimension(int index, int nb, int dim) {

		Image duplicated = null;
		switch (dim) {
		case Image.X:
			duplicated = this.newInstance(nb, this.getYDim(), this.getZDim(), this
				.getTDim(), this.getBDim());
			for (int x = 0; x < nb; x++)
				for (int y = 0; y < this.getYDim(); y++)
					for (int z = 0; z < this.getZDim(); z++)
						for (int t = 0; t < this.getTDim(); t++)
							for (int b = 0; b < this.getBDim(); b++)
								// duplicated.setPixelDouble(x, y, z, t, b,
								// this.getPixelDouble(index, y, z, t, b));
								duplicated.setPixel(this, x, y, z, t, b, index, y, z, t, b);
			break;
		case Image.Y:
			duplicated = this.newInstance(this.getXDim(), nb, this.getZDim(), this
				.getTDim(), this.getBDim());
			for (int y = 0; y < nb; y++)
				for (int x = 0; x < this.getXDim(); x++)
					for (int z = 0; z < this.getZDim(); z++)
						for (int t = 0; t < this.getTDim(); t++)
							for (int b = 0; b < this.getBDim(); b++)
								// duplicated.setPixelDouble(x, y, z, t, b,
								// this.getPixelDouble(x,index, z, t, b));
								duplicated.setPixel(this, x, y, z, t, b, x, index, z, t, b);
			break;
		case Image.Z:
			duplicated = this.newInstance(this.getXDim(), this.getYDim(), nb, this
				.getTDim(), this.getBDim());
			for (int z = 0; z < nb; z++)
				for (int x = 0; x < this.getXDim(); x++)
					for (int y = 0; y < this.getYDim(); y++)
						for (int t = 0; t < this.getTDim(); t++)
							for (int b = 0; b < this.getBDim(); b++)
								// duplicated.setPixelDouble(x, y, z, t, b,
								// this.getPixelDouble(x,y, index, t, b));
								duplicated.setPixel(this, x, y, z, t, b, x, y, index, t, b);
			break;
		case Image.T:
			duplicated = this.newInstance(this.getXDim(), this.getYDim(), this
				.getZDim(), nb, this.getBDim());
			for (int t = 0; t < nb; t++)
				for (int x = 0; x < this.getXDim(); x++)
					for (int y = 0; y < this.getYDim(); y++)
						for (int z = 0; z < this.getZDim(); z++)
							for (int b = 0; b < this.getBDim(); b++)
								// duplicated.setPixelDouble(x, y, z, t,
								// b,this.getPixelDouble(x,y, z, index, b));
								duplicated.setPixel(this, x, y, z, t, b, x, y, z, index, b);
			break;
		case Image.B:
			duplicated = this.newInstance(this.getXDim(), this.getYDim(), this
				.getZDim(), this.getTDim(), nb);
			for (int b = 0; b < nb; b++)
				for (int x = 0; x < this.getXDim(); x++)
					for (int y = 0; y < this.getYDim(); y++)
						for (int z = 0; z < this.getZDim(); z++)
							for (int t = 0; t < this.getTDim(); t++)
								// duplicated.setPixelDouble(x, y, z, t, b,
								// this.getPixelDouble(x,y, z, t, index));
								duplicated.setPixel(this, x, y, z, t, b, x, y, z, t, index);
			break;
		}
		return duplicated;
	}

	/**
	 * Set a property to this image
	 * 
	 * @param key
	 * @param data
	 */
	public final void setProperty(String key, Object data) {
		this.properties.put(key, data);
	}

	/**
	 * Get a property of this image
	 * 
	 * @param key
	 * @return the property
	 */
	public final Object getProperty(String key) {
		return this.properties.get(key);
	}

	/**
	 * Remove a property of this image
	 * 
	 * @param key
	 */
	public final void removeProperty(String key) {
		this.properties.remove(key);
	}

	/**
	 * Return all properties. Don't use at home.
	 * 
	 * @return all properties
	 */
	public final Map<String, Object> getProperties() {
		return properties;
	}

	/**
	 * Set all properties. Don't use at home.
	 * 
	 * @param properties
	 */
	public final void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	/**
	 * Return the name of the image.
	 * 
	 * @return the name of the image
	 */
	public final String getName() {
		return name;
	}

	/**
	 * Set the name of the image
	 * 
	 * @param name
	 */
	public final void setName(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public final String toString() {
		return this.getClass() + " " + this.getXDim() + "x" + this.getYDim() + "x"
			+ this.getZDim() + "x" + this.getTDim() + "x" + this.getBDim() + " "
			+ "(Object: "+super.toString()+" )";
	}
	
	/**
	 * Return the center of the image
	 * @return the center of the image
	 */
	public final Point4D getCenter() {
		return center;
	}
	
	/**
	 * Set the center of the image 
	 * @param center the new center of the image
	 */
	public final void setCenter(Point4D center) {
		this.center=center;
	}

	/**
	 * Set the center of the image 
	 * @param center the 2D new center of the image
	 */
	public void setCenter(Point center) {
		this.center=new Point4D(center);
	}
	
	/**
	 * Reset the center of the image to the central position 
	 */
	public void resetCenter() {
		this.center=new Point4D(xdim/2,ydim/2,zdim/2,tdim/2);
	}

	/**
	 * Revert the center of the image, useful for even sized structuring elements 
	 */
	public final void revertCenter() {
		this.center=new Point4D(xdim-1-center.x,ydim-1-center.y,zdim-1-center.z,tdim-1-center.t);
	}
	
	/**
	 * Compare the image center to another 
	 * @param im the image to be compared
	 * @return true if the centers of the two images are equals
	 */
	public final boolean haveSameCenter(Image im) {
		return center.equals(im.getCenter());
	}

	
	/*
	 * Mask management
	 ****************************************************/
	
	/*
	 * (non-Javadoc)
	 * @see fr.unistra.pelican.util.mask.Mask#isInMask(int)
	 */
	
	public boolean isInMask(int loc){
		return getPixelBoolean(loc);
	}
	public boolean isInMask(long loc){
		return getPixelBoolean(loc);
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see fr.unistra.pelican.util.mask.Mask#isInMask(int, int, int, int, int)
	 */
	
	public final boolean isInMask(int x, int y, int z, int t, int b){
		return getPixelXYZTBBoolean(x, y, z, t, b);
	}

	/*
	 * (non-Javadoc)
	 * @see fr.unistra.pelican.util.mask.Mask#isInMaskXY(int, int)
	 */
	
	public final boolean isInMaskXY(int x, int y){
		return getPixelXYBoolean(x, y);
	}

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.util.mask.Mask#isInMaskXYZ(int, int, int)
	 */
	
	public final boolean isInMaskXYZ(int x, int y, int z) {
		return getPixelXYZBoolean(x, y, z);
	}

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.util.mask.Mask#isInMaskXYB(int, int, int)
	 */
	
	public final boolean isInMaskXYB(int x, int y, int b) {
		return getPixelXYBBoolean(x, y,  b);
	}

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.util.mask.Mask#isInMaskXYT(int, int, int)
	 */
	
	public final boolean isInMaskXYT(int x, int y, int t){
		return getPixelXYTBoolean(x, y, t);
	}

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.util.mask.Mask#isInMaskXYZT(int, int, int, int)
	 */
	
	public final boolean isInMaskXYZT(int x, int y, int z, int t){
		return getPixelXYZTBoolean(x, y, z, t);
	}

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.util.mask.Mask#isInMaskXYZB(int, int, int, int)
	 */
	
	public final boolean isInMaskXYZB(int x, int y, int z, int b) {
		return getPixelXYZBBoolean(x, y, z,  b);
	}

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.util.mask.Mask#isInMaskXYTB(int, int, int, int)
	 */
	
	public final boolean isInMaskXYTB(int x, int y, int t, int b){
		return getPixelXYTBBoolean(x, y, t, b);
	}

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.util.mask.Mask#isInMaskXYZTB(int, int, int, int, int)
	 */
	
	public final boolean isInMaskXYZTB(int x, int y, int z, int t, int b) {
		return getPixelXYZTBBoolean(x, y, z, t, b);
	}

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.util.mask.Mask#cloneMask()
	 */
	
	public final Mask cloneMask()
	{
		return copyImage(true);
	}
	
	/**
	 * Push mask on the local mask stack
	 * @param m new mask
	 */
	public final void pushMask(Mask m)
	{
		if(m!=null)mask.push(m);
	}
	
	/**
	 * Pop mask on the top of the mask stack (peek and remove)
	 * @return top mask, null if mask stack is empty
	 */
	public final Mask popMask()
	{
		return (mask.isEmpty())?null:mask.pop();
	}
	
	/**
	 * Peek mask on the top of the mask stack (peek and leave)
	 * @return top mask, null if mask stack is empty
	 */
	public final Mask peekMask()
	{
		return (mask.isEmpty())?null:mask.peek();
	}

	/**
	 * Get mask stack used by this image
	 * @return the mask
	 */
	public final MaskStack getMask() {
		return mask;
	}

	/**
	 * Set a new mask stack for this image
	 * @param mask the mask to set (a new MaskStack is created if null)
	 */
	public final void setMask(MaskStack mask) {
		if(mask!=null) this.mask = mask;
		else this.mask=new MaskStack();
	}
	
	/**
	 * Test if pixel at given location is under mask or not
	 * @param loc linear location of pixel
	 * @return false if pixel is masked, true otherwise
	 */
	public boolean isPresent(int loc)
	{
		return mask.isInMask(loc);
		
	}
	public boolean isPresent(long loc){
		return isPresent((int)loc);
	}
	
	/**
	 * Test if pixel at given location is under mask or not
	 * @param p Location
	 * @return false if pixel is masked, true otherwise
	 */
	public boolean isPresent(Pixel p)
	{
		return mask.isInMask(p.x,p.y,p.z,p.t,p.b);
	}
	
	/**
	 * Test if pixel at given location is under mask or not
	 * 
	 * @param x
	 *          horizontal position of the desired pixel
	 * @param y
	 *          vertical position of the desired pixel
	 * @param z
	 *          depth position of the desired pixel
	 * @param t
	 *          time position of the desired pixel
	 * @param b
	 *          channel number of the desired pixel
	 * @return false if pixel is masked, true otherwise
	 */

	public final boolean isPresent(int x, int y, int z, int t, int b) {
		return mask.isInMask(x,y,z,t,b);
	}

	/**	Gets the value of the pixel in the given location as boolean
	 *
	 *	@param x horizontal position of the desired pixel
	 *	@param y vertical position of the desired pixel
	 *	@return the value of the pixel in the given location as boolean
	 */
	public final boolean isPresentXY( int x, int y ) { 

	//	if ( this.isOutOfBoundsXY( x,y ) ) return false;
		return mask.isInMaskXY( x,y );
	}

	/**	Gets the value of the pixel in the given location as boolean
	 *
	 *	@param x horizontal position of the desired pixel
	 *	@param y vertical position of the desired pixel
	 *	@param z depth position of the desired pixel
	 *	@return the value of the pixel in the given location as boolean
	 */
	public final boolean isPresentXYZ( int x, int y, int z ) { 

	//	if ( this.isOutOfBoundsXYZ( x,y,z ) ) return false;
		return mask.isInMaskXYZ(x,y,z);
	}

	/**	Gets the value of the pixel in the given location as boolean
	 *
	 *	@param x horizontal position of the desired pixel
	 *	@param y vertical position of the desired pixel
	 *	@param b channel number of the desired pixel
	 *	@return the value of the pixel in the given location as boolean
	 */
	public final boolean isPresentXYB( int x, int y, int b ) { 

	//	if ( this.isOutOfBoundsXYB( x,y,b ) ) return false;
		return mask.isInMaskXYB(x,y,b);
	}

	/**	Gets the value of the pixel in the given location as boolean
	 *
	 *	@param x horizontal position of the desired pixel
	 *	@param y vertical position of the desired pixel
	 *	@param t time position of the desired pixel
	 *	@return the value of the pixel in the given location as boolean
	 */

	public final boolean isPresentXYT(int x, int y, int t) { 

	//	if ( this.isOutOfBoundsXYT( x,y,t ) ) return false;
		return mask.isInMaskXYT(x,y,t);
	}

	/**
	 * Gets the value of the pixel in the given location as boolean
	 * 
	 * @param x horizontal position of the desired pixel
	 * @param y vertical position of the desired pixel
	 * @param z depth position of the desired pixel
	 * @param t time position of the desired pixel
	 * @return the value of the pixel in the given location as boolean
	 */

	public final boolean isPresentXYZT( int x, int y, int z, int t ) { 

	//	if ( this.isOutOfBoundsXYZT( x,y,z,t ) ) return false;
		return mask.isInMaskXYZT( x,y,z,t );
	}

	/**	Gets the value of the pixel in the given location as boolean
	 * 
	 *	@param x horizontal position of the desired pixel
	 *	@param y vertical position of the desired pixel
	 *	@param z depth position of the desired pixel
	 *	@param b channel number of the desired pixel
	 *	@return the value of the pixel in the given location as boolean
	 */

	public final boolean isPresentXYZB( int x, int y, int z, int b ) { 

	//	if ( this.isOutOfBoundsXYZB( x,y,z,b ) ) return false;
		return mask.isInMaskXYZB( x,y,z,b );
	}

	/**	Gets the value of the pixel in the given location as boolean
	 * 
	 *	@param x horizontal position of the desired pixel
	 *	@param y vertical position of the desired pixel
	 *	@param t time position of the desired pixel
	 *	@param b channel number of the desired pixel
	 *	@return the value of the pixel in the given location as boolean
	 */

	public final boolean isPresentXYTB( int x, int y, int t, int b ) { 

	//	if ( this.isOutOfBoundsXYTB( x,y,t,b ) ) return false;
		return mask.isInMaskXYTB( x,y,t,b );
	}

	/**	Gets the value of the pixel in the given location as boolean
	 * 
	 *	@param x horizontal position of the desired pixel
	 *	@param y vertical position of the desired pixel
	 *	@param z depth position of the desired pixel
	 *	@param t time position of the desired pixel
	 *	@param b channel number of the desired pixel
	 *	@return the value of the pixel in the given location as boolean
	 */

	public final boolean isPresentXYZTB( int x, int y, int z, int t, int b ) { 

	//	if ( this.isOutOfBoundsXYZTB( x,y,z,t,b ) ) return false;
		return mask.isInMaskXYZTB( x,y,z,t,b );
	}
	
	/**
	 * Get number of pixels not currently masked
	 * @return number of pixels not currently masked
	 */
	public int getNumberOfPresentPixel() {
		int nb = 0;
		for(int i=0;i<size();i++)
			if (isPresent(i))
				nb++;
		return nb;
	}
	
	/**
	 * Get number of pixels not currently masked in a specified band
	 * @param band band number
	 * @return number of pixels not currently masked in the specified band
	 */
	public int getNumberOfPresentPixel(int band)
	{
		int nb=0;
		
		for(int i=band;i<size();i+=bdim)
			if (isPresent(i))
				nb++;
		return nb;
		
	}
	
	/*
	 * END - Mask management
	 ****************************************************/

	public final boolean isOutOfBoundsXY( int x, int y ) { 

		return  	x < 0 || x >= this.xdim 
				 || y < 0 || y >= this.ydim 
				;
	}
	public final boolean isOutOfBoundsXYZ( int x, int y, int z ) { 

		return  	x < 0 || x >= this.xdim 
				 || y < 0 || y >= this.ydim 
				 || z < 0 || z >= this.zdim 
				;
	}
	public final boolean isOutOfBoundsXYT( int x, int y, int t ) { 

		return  	x < 0 || x >= this.xdim 
				 || y < 0 || y >= this.ydim 
				 || t < 0 || t >= this.tdim 
				;
	}
	public final boolean isOutOfBoundsXYB( int x, int y, int b ) { 

		return  	x < 0 || x >= this.xdim 
				 || y < 0 || y >= this.ydim 
				 || b < 0 || b >= this.bdim 
				;
	}
	public final boolean isOutOfBoundsXYZT( int x, int y, int z, int t ) { 

		return  	x < 0 || x >= this.xdim 
				 || y < 0 || y >= this.ydim 
				 || z < 0 || z >= this.zdim 
				 || t < 0 || t >= this.tdim 
				;
	}
	public final boolean isOutOfBoundsXYZB( int x, int y, int z, int b ) { 

		return  	x < 0 || x >= this.xdim 
				 || y < 0 || y >= this.ydim 
				 || z < 0 || z >= this.zdim 
				 || b < 0 || b >= this.bdim 
				;
	}
	public final boolean isOutOfBoundsXYTB( int x, int y, int t, int b ) { 

		return  	x < 0 || x >= this.xdim 
				 || y < 0 || y >= this.ydim 
				 || t < 0 || t >= this.tdim 
				 || b < 0 || b >= this.bdim 
				;
	}
	public final boolean isOutOfBoundsXYZTB( int x, int y, int z, int t, int b ) { 

		return  	x < 0 || x >= this.xdim 
				 || y < 0 || y >= this.ydim 
				 || z < 0 || z >= this.zdim 
				 || t < 0 || t >= this.tdim 
				 || b < 0 || b >= this.bdim 
				;
	}
	public final boolean isOutOfBounds( int x, int y, int z, int t, int b ) { 

		return  	x < 0 || x >= this.xdim 
				 || y < 0 || y >= this.ydim 
				 || z < 0 || z >= this.zdim 
				 || t < 0 || t >= this.tdim 
				 || b < 0 || b >= this.bdim 
				;
	}

	public final boolean isOutOfBounds( int p ) { 

		return ( p >= 0 && p < this.xdim * this.ydim * this.zdim * this.tdim * this.bdim );
	}
	
	public final boolean isOutOfBounds( Point p )
	{
		return isOutOfBoundsXY(p.x,p.y);
	}
	
	public final boolean isOutOfBounds( Point3D p )
	{
		return isOutOfBoundsXYZ(p.x,p.y,p.z);
	}
	
	public final boolean isOutOfBounds( PointVideo p )
	{
		return isOutOfBoundsXYT(p.x,p.y,p.t);
	}
	
	public final boolean isOutOfBounds( Point4D p )
	{
		return isOutOfBoundsXYZT(p.x,p.y,p.z,p.t);
	}
	
	/**
	 * A very simple function to test if we can easily avoid  tests of presence of pixel
	 * @return ( this.getMask() == null || this.getMask().isEmpty() )
	 */
	public final boolean isMasked()
	{
		return ( this.getMask() == null || this.getMask().isEmpty() );
	}


	  ////////////////////////
	 // ITERABLE INTERFACE //
	////////////////////////

	

	public final Iterator<Pixel> iterator() { 

		boolean noMask = isMasked();
		Iterator<Pixel> iterator = null;
		if ( noMask ) iterator = new ImageIterator( this );
		else iterator = new MaskedImageIterator( this );
		return iterator;
	}
	
	/**
	 * Returns an iterator over an XY plane of the this image
	 * @param z
	 * @param t
	 * @param b
	 * @return
	 */
	public final AbstractImageIterator<Pixel> iterateOverXY(int z, int t, int b) { 

		boolean noMask = isMasked();
		AbstractImageIterator<Pixel> iterator = null;
		if ( noMask ) iterator = new ImageIteratorXY( this,z,t,b );
		else iterator = new MaskedImageIteratorXY( this,z,t,b );
		return iterator;
	}

	
	private ImageIteratorXY myIteratorXY=null;
	
	
	public final AbstractImageIterator<Pixel> iterateOverXYbis(int z, int t, int b) { 
		if(myIteratorXY==null)
			myIteratorXY=new ImageIteratorXY( this,z,t,b );
		else myIteratorXY.reInit(z, t, b);
		return myIteratorXY;
	}

	/**	Gets the value of the pixel at the given location as boolean.
	 *	@param p Pixel coordinates. Take care of image bounds !
	 *	@return Pixel value.
	 */
	public final boolean getPixelBoolean( Pixel p ) { 

		return this.getPixelXYZTBBoolean( p.x,p.y,p.z,p.t,p.b );
	}

	/**	Gets the value of the pixel at the given location as byte.
	 *	@param p Pixel coordinates. Take care of image bounds !
	 *	@return Pixel value.
	 */
	public final int getPixelByte( Pixel p ) { 

		return this.getPixelXYZTBByte( p.x,p.y,p.z,p.t,p.b );
	}

	/**	Gets the value of the pixel at the given location as integer.
	 *	@param p Pixel coordinates. Take care of image bounds !
	 *	@return Pixel value.
	 */
	public final int getPixelInt( Pixel p ) { 

		return this.getPixelXYZTBInt( p.x,p.y,p.z,p.t,p.b );
	}

	/**	Gets the value of the pixel at the given location as double.
	 *	@param p Pixel coordinates. Take care of image bounds !
	 *	@return Pixel value.
	 */
	public final double getPixelDouble( Pixel p ) { 

		return this.getPixelXYZTBDouble( p.x,p.y,p.z,p.t,p.b );
	}



	/**	Sets the value of the pixel at the given location to a boolean.
	 *	@param p Pixel coordinates. Take care of image bounds !
	 *	@param value Pixel desired value. 
	 */
	public final void setPixelBoolean( Pixel p, boolean value ) { 

		this.setPixelXYZTBBoolean( p.x,p.y,p.z,p.t,p.b, value );
	}

	/**	Sets the value of the pixel at the given location to a byte.
	 *	@param p Pixel coordinates. Take care of image bounds !
	 *	@param value Pixel desired value. 
	 */
	public final void setPixelByte( Pixel p, int value ) { 

		this.setPixelXYZTBByte( p.x,p.y,p.z,p.t,p.b, value );
	}

	/**	Sets the value of the pixel at the given location to an integer.
	 *	@param p Pixel coordinates. Take care of image bounds !
	 *	@param value Pixel desired value. 
	 */
	public final void setPixelInt( Pixel p, int value ) { 

		this.setPixelXYZTBInt( p.x,p.y,p.z,p.t,p.b, value );
	}

	/**	Sets the value of the pixel at the given location to a double.
	 *	@param p Pixel coordinates. Take care of image bounds !
	 *	@param value Pixel desired value. 
	 */
	public final void setPixelDouble( Pixel p, double value ) { 

		this.setPixelXYZTBDouble( p.x,p.y,p.z,p.t,p.b, value );
	}
	
	
	  ////////////////////////
	 //  DATA CONVERSION   //
	////////////////////////
	//Hey booleanToInt(true) doesn't return the same that signedByteToInt(127)
	//TODO
	
	
	public static final double booleanToDouble(boolean b){
		return b ? 1.0 : 0.0;
		//return b ? 1.0 : 0.0; 
	}
	public static final int booleanToInt(boolean b){
		//return b ? Integer.MAX_VALUE : 0;
		return b ? Integer.MAX_VALUE : Integer.MIN_VALUE; 
	}
	public static final double intToDouble(int i){
		return intToDouble * (double) i + intToDoubleOffset;
		//return intToDouble * (double) i + 0.5;		
	}
	public static final boolean intToBoolean(int i){
		//return (i > Integer.MAX_VALUE/2) ? true : false;
		return (i >= 0) ? true : false;	
	}	
	public static final int doubleToInt(double d){
		//return (int) (Image.doubleToInt * (d - 0.5));
		//return (int)((d - 0.5) * doubleToInt);
		return (int) (Image.doubleToInt * (d - intToDoubleOffset));
	}
	public static final boolean doubleToBoolean(double d){
		return (d >= 0.5) ? true : false;
		//return (d >= 0.5) ? true : false;
	}
	
	
	public static final int unsignedByteToInt(int b){
		return (b  + Byte.MIN_VALUE) << 24;
		//return b << 24;
	}	
	public static final double unsignedByteToDouble(int b){
		return byteToDouble * (double) b;
		//return byteToDouble * (double) (b + 128);
	}	
	public static final boolean unsignedByteToBoolean(int b){
		//return b >= 0 ? true : false;
		return (b >= 128) ? true : false;	
	}
		
	public static final int intToUnsignedByte(int i){
		return (i >> 24) - Byte.MIN_VALUE;
		//return (byte) (i >> 24);
	}
	public static final int doubleToUnsignedByte(double d){
		return (int) Math.round(Image.doubleToByte * d);
		//return (byte) Math.round(doubleToByte * d - 128);
	}
	public static final int booleanToUnsignedByte(boolean b){	
		//return b ? Byte.MAX_VALUE : Byte.MIN_VALUE;
		return b ? 255 : 0;		
	}	
	
	
	public static final int signedByteToInt(byte b){
		//return (b  + Byte.MIN_VALUE) << 24;
		return b << 24;
	}	
	public static final double signedByteToDouble(byte b){
		//return byteToDouble * (double) b;
		//return byteToDouble * (double) (b + 128);
		return byteToDouble * (double) (b - Byte.MIN_VALUE);
	}	
	public static final boolean signedByteToBoolean(byte b){
		return b >= 0 ? true : false;
		//return (b >= 128) ? true : false;	
	}
		
	public static final byte intToSignedByte(int i){
		//return (i >> 24) - Byte.MIN_VALUE;
		return (byte) (i >> 24);
	}
	public static final byte doubleToSignedByte(double d){
		//return (int) Math.round(Image.doubleToByte * d);
		//return (byte) Math.round(doubleToByte * d - 128);
		return (byte) Math.round(doubleToByte * d +Byte.MIN_VALUE);
	}
	public static final byte booleanToSignedByte(boolean b){	
		return b ? Byte.MAX_VALUE : Byte.MIN_VALUE;
		//return b ? 255 : 0;		
	}
	
	public static final int signedByteToUnsignedByte(byte b){
		return b-Byte.MIN_VALUE;
	}
	
	public static final byte unsignedByteToSignedByte(int i){
		return (byte) (i+Byte.MIN_VALUE);
	}
	
	  ////////////////////////
	 //    CONSTRUCTORS    //
	////////////////////////
	
	/**
	 * Creates a new empty BooleanImage with the same dimensions
	 * @return 
	 * 			A new BooleanImage 
	 */
	public BooleanImage newBooleanImage(){
		return new BooleanImage(this,false);
	}
	
	/**
	 * Creates a new Boolean Image with the same dimensions and copies data according to the argument copyData
	 * @param copyData
	 * 			Indicates whether the data must be copied
	 * @return
	 * 			A new BooleanImage
	 */
	public BooleanImage newBooleanImage(boolean copyData){
		return new BooleanImage(this,copyData);
	}
	
	/**
	 * Creates a new Boolean Image with the given dimensions
	 * @param x
	 * 			the horizontal dimension
	 * @param y
	 * 			the vertical dimension
	 * @param z
	 * 			the depth dimension
	 * @param t
	 * 			the time dimension
	 * @param b
	 * 			the band dimension
	 * @return
	 * 			A new BooleanImage
	 */
	public BooleanImage newBooleanImage(int x, int y, int z, int t, int b){
		return new BooleanImage(x,y,z,t,b);
	}
	
	
	/**
	 * Creates a new empty ByteImage with the same dimensions
	 * @return 
	 * 			A new ByteImage 
	 */
	public ByteImage newByteImage(){
		return new ByteImage(this,false);
	}
	
	/**
	 * Creates a new ByteImage with the same dimensions and copies data according to the argument copyData
	 * @param copyData
	 * 			Indicates whether the data must be copied
	 * @return
	 * 			A new ByteImage
	 */
	public ByteImage newByteImage(boolean copyData){
		return new ByteImage(this,copyData);
	}
	
	/**
	 * Creates a new Byte Image with the given dimensions
	 * @param x
	 * 			the horizontal dimension
	 * @param y
	 * 			the vertical dimension
	 * @param z
	 * 			the depth dimension
	 * @param t
	 * 			the time dimension
	 * @param b
	 * 			the band dimension
	 * @return
	 * 			A new ByteImage
	 */
	public ByteImage newByteImage(int x, int y, int z, int t, int b){
		return new ByteImage(x,y,z,t,b);
	}

	/**
	 * Creates a new empty IntegerImage with the same dimensions
	 * @return 
	 * 			A new IntegerImage 
	 */
	public IntegerImage newIntegerImage(){
		return new IntegerImage(this,false);
	}
	
	/**
	 * Creates a new IntegerImage with the same dimensions and copies data according to the argument copyData
	 * @param copyData
	 * 			Indicates whether the data must be copied
	 * @return
	 * 			A new IntegerImage
	 */
	public IntegerImage newIntegerImage(boolean copyData){
		return new IntegerImage(this,copyData);
	}
	
	/**
	 * Creates a new Integer Image with the given dimensions
	 * @param x
	 * 			the horizontal dimension
	 * @param y
	 * 			the vertical dimension
	 * @param z
	 * 			the depth dimension
	 * @param t
	 * 			the time dimension
	 * @param b
	 * 			the band dimension
	 * @return
	 * 			A new IntegerImage
	 */
	public IntegerImage newIntegerImage(int x, int y, int z, int t, int b){
		return new IntegerImage(x,y,z,t,b);
	}

	/**
	 * Creates a new empty DoubleImage with the same dimensions
	 * @return 
	 * 			A new DoubleImage 
	 */
	public DoubleImage newDoubleImage(){
		return new DoubleImage(this,false);
	}
	
	/**
	 * Creates a new DoubleImage with the same dimensions and copies data according to the argument copyData
	 * @param copyData
	 * 			Indicates whether the data must be copied
	 * @return
	 * 			A new DoubleImage
	 */
	public DoubleImage newDoubleImage(boolean copyData){
		return new DoubleImage(this,copyData);
	}
	
	/**
	 * Creates a new Double Image with the given dimensions
	 * @param x
	 * 			the horizontal dimension
	 * @param y
	 * 			the vertical dimension
	 * @param z
	 * 			the depth dimension
	 * @param t
	 * 			the time dimension
	 * @param b
	 * 			the band dimension
	 * @return
	 * 			A new DoubleImage
	 */
	public DoubleImage newDoubleImage(int x, int y, int z, int t, int b){
		return new DoubleImage(x,y,z,t,b);
	}
		
}