package fr.unistra.pelican;

import java.awt.Point;
import java.io.Serializable;
import java.util.Arrays;

import fr.unistra.pelican.util.Point4D;

/** This class represents an Image of boolean values
 * A boolean pixel is false (black) or true (white).
 * @author PELICAN team
 * @version 1.0
 */

public class BooleanImage extends Image implements Serializable
{
	/**
	 * Pixel data array
	 */

	private boolean[] pixels;

	/**
	 *	Serial version ID
	 */

	private static final long serialVersionUID = 3L;

	
	/**
	 * Constructs a BooleanImage
	 */
	protected BooleanImage(){
		super();
	}
	
	/**
	 * Constructs a BooleanImage identical to the given argument
	 * @param	image	BooleanImage to copy
	 */

	public BooleanImage(BooleanImage image)
	{
		super(image);
		this.pixels = (boolean[])image.pixels.clone();	// deep copy with clone valid only for primitives.
	}
	
	/**
	 * Constructs a BooleanImage identical to the given argument
	 * @param	image	Image to copy
	 */
	public BooleanImage(Image image)
	{
		this(image, true);
	}
	
	/**
	 * Constructs a BooleanImage identical to the given argument
	 * @param	image	Image to copy
	 * @param	copyData	if and only if it is set to true are the pixels copied
	 */
	public BooleanImage(Image image, boolean copyData)
	{
		super(image);
		this.pixels = new boolean[image.getXDim() * image.getYDim() * image.getZDim() * image.getTDim() * image.getBDim()];
		if(copyData == true)
			for(int i = 0; i < pixels.length; i++)
				setPixelBoolean(i, image.getPixelBoolean(i)); 
	}
	
	/**
	 * Constructs a ByteImage from the given argument. The pixels are copied if and only of ''copy'' is set to true.
	 * @param	image	ByteImage to copy
	 * @param	copy	if and only if it is set to true are the pixels copied
	 */
	public BooleanImage(BooleanImage image, boolean copy)
	{
		super(image);

		if(copy == true)
			this.pixels = (boolean[])image.pixels.clone();
		else
			this.pixels = new boolean[image.getXDim() * image.getYDim() * image.getZDim() * image.getTDim() * image.getBDim()];
	}

	/**
	 * Constructs a BooleanImage with the given dimensions
	 * @param	xdim	the horizontal dimension
	 * @param	ydim	the vertical dimension
	 * @param	zdim	the depth
	 * @param	tdim	the frame number
	 * @param	bdim	the channel number
	 */

	public BooleanImage(int xdim,int ydim,int zdim,int tdim,int bdim)
	{
		super(xdim,ydim,zdim,tdim,bdim);
		this.pixels = new boolean[xdim * ydim * zdim * tdim * bdim];
	}


	/**
	 * Creates a copy of this BooleanImage
	 * @return	an axact copy of this BooleanImage
	 */

	public BooleanImage copyImage(boolean copyData)
	{
		return new BooleanImage(this, copyData);
	}

	/**
	 * Creates a new instance of BooleanImage
	 * @param	xdim	the horizontal dimension
	 * @param	ydim	the vertical dimension
	 * @param	zdim	the depth
	 * @param	tdim	the frame number
	 * @param	bdim	the channel number
	 */
	public Image newInstance(int xdim,int ydim,int zdim,int tdim,int bdim) {
		return new BooleanImage(xdim,ydim,zdim,tdim,bdim);
	}
	
	/**
	 * Sets all the pixel values to the given boolean
	 * @param	b	Desired value for the pixels
	 */

	public void fill(boolean b)
	{
		Arrays.fill(pixels,b);
	}

	@Override
	public void fill(double d) {
		this.fill(doubleToBoolean(d));	
	}

	/**
	 * Duplicates the given channel to all the available dimensions
	 * @param	band	channel to duplicate
	 */	

	public void duplicateBand(int band)
	{
		for(int x = 0; x < this.xdim; x++)
			for(int y = 0; y < this.ydim; y++)
				for(int z = 0; z < this.zdim; z++)
					for(int t = 0; t < this.tdim; t++)
						for(int b = 0; b < this.bdim; b++)
							if(b != band) setPixelBoolean(x,y,z,t,b,getPixelBoolean(x,y,z,t,band));
	}


	
	/**
	 * Checks if the image is empty, i.e. it contains only 0 pixels
	 * @return	true if the image is empty
	 */

	public boolean isEmpty()
	{
		for (int p=0;p<pixels.length;p++)
			if (pixels[p])
				return false;
		return true;
	}


	
	
	/**
	 * Computes the total number of pixels in all dimensions
	 * @return	the number of pixels
	 */

	public int size()
	{
		return pixels.length;
	}

	/**
	 * Gets a copy of the pixel array
	 * @return	a copy of the pixel array
	 */

	public boolean[] getPixels()
	{
		return (boolean[])pixels.clone();
	}

	/**
	 * Sets the pixels to the copy of the given array
	 * @param	values	pixel array to copy
	 */

	public void setPixels(boolean[] values)
	{
		pixels = (boolean[])values.clone();
	}


	/**
	 * Computes the complement image
	 * @return	the complement BooleanImage
	 */

	public BooleanImage getComplement()
	{
		BooleanImage im = new BooleanImage(this);

		for(int i = 0; i < size(); i++)
			im.setPixelBoolean(i,!pixels[i]);

		return im;
	}

	/**
	 * Computes the number of "true" pixels
	 * @return	the number of true pixels
	 */

	public int getSum()
	{
		int sum=0;
		for(int i = 0; i < size(); i++)
			if ( isPresent(i) && getPixelBoolean(i) ) sum++;
		return sum;
	}

	/**
	 * Extracts the positions of foreground pixels
	 * @return the array of foreground pixels
	 */
	public Point4D[] foreground() {
		int s=getSum();
		Point4D tab[]=new Point4D[s];
		int k=0;
		for (int t=0;t<tdim;t++)
			for (int z=0;z<zdim;z++)
				for (int y=0;y<ydim;y++)
					for (int x=0;x<xdim;x++)
						if ( isPresentXYZT( x,y,z,t ) && getPixelXYZTBoolean( x,y,z,t ) )
							tab[k++]=new Point4D(x,y,z,t);
		return tab;
	}

	/**
	 * Extracts the positions of foreground pixels
	 * @return the array of foreground pixels
	 */
	public Point[] foreground2D() {
		int s=getSum();
		Point tab[]=new Point[s];
		int k=0;
		for (int y=0;y<ydim;y++)
				for (int x=0;x<xdim;x++)
					if ( isPresentXY(x,y) && getPixelXYBoolean(x,y) )
							tab[k++]=new Point(x,y);
		return tab;
	}

	/**
	 * Compares with the given BooleanImage
	 * @param	im	image to compare
	 * @return	<code>true</code> if and only if the given image has the same pixel values as this image
	 */

	public boolean equals(Image im)
	{
		
		if(im==null  || !(im instanceof BooleanImage))
			return false;

		if(!haveSameDimensions(im,this))
			return false;

		int size = size();
		boolean impresent,thispresent;
		for(int i = 0; i < size; i++) { 

			impresent = im.isPresent(i);
			thispresent = this.isPresent(i);
			if ( !impresent && thispresent ) return false;
			if ( impresent && !thispresent ) return false;
			if ( impresent && thispresent )
				if ( im.getPixelBoolean(i) != pixels[i] ) return false;
		}

		return true;
	}
	
	@Override
	public double getPixelDouble(int loc) {
		return booleanToDouble(pixels[loc]);
		//return pixels[loc] ? 1.0 : 0.0; 
	}

	@Override
	public int getPixelInt(int loc) {
		return booleanToInt(pixels[loc]);
		//return pixels[loc] ? Integer.MAX_VALUE : Integer.MIN_VALUE; 
	}

	@Override
	public int getPixelByte(int loc) {
		return booleanToUnsignedByte(pixels[loc]);
		//return pixels[loc] ? 255 : 0;
	}

	@Override
	public boolean getPixelBoolean(int loc) {
		return pixels[loc];
	}

	@Override
	public void setPixelDouble(int loc, double value) {
		pixels[loc] = doubleToBoolean(value);
		//pixels[loc] = (value >= 0.5) ? true : false;		
	}

	@Override
	public void setPixelInt(int loc, int value) {
		pixels[loc] =intToBoolean(value);
		//pixels[loc] = (value >= 0) ? true : false;		
	}

	@Override
	public void setPixelByte(int loc, int value) {
		pixels[loc] = unsignedByteToBoolean(value);
		//pixels[loc] = (value >= 128) ? true : false;		
	}

	@Override
	public void setPixelBoolean(int loc, boolean value) {
		pixels[loc] = value;
	}

	@Override
	public void setPixel(Image input, int x1, int y1, int z1, int t1, int b1,
		int x2, int y2, int z2, int t2, int b2) {
		this.setPixelBoolean(x1, y1, z1, t1, b1, input
			.getPixelBoolean(x2, y2, z2, t2, b2));
	}

	/**
	 * Convert the BooleanImage to a ByteImage without modifying the values 
	 * @return	an ByteImage
	 */
	public ByteImage copyToByteImage() {
		ByteImage i=new ByteImage(this,false);
		for (int p=0;p<i.size();p++)
			i.setPixelByte(p,getPixelBoolean(p)?1:0);
		return i;
	}
	
	/**
	 * Convert the BooleanImage to a IntegerImage without modifying the values 
	 * @return	an IntegerImage
	 */
	public IntegerImage copyToIntegerImage() {
		IntegerImage i=new IntegerImage(this,false);
		for (int p=0;p<i.size();p++)
			i.setPixelInt(p,getPixelBoolean(p)?1:0);
		return i;
	}
	
	/**
	 * Gets the minimum from the whole image.
	 * @return
	 * 		The minimum as a boolean
	 */
	public boolean minimum(){
		for (int p = 0; p < size(); p++)
			if (!getPixelBoolean(p))
				return false;
		return true;
	}
	
	/**
	 * Gets the maximum from the whole image.
	 * @return
	 * 		The maximum as a boolean
	 */
	public boolean maximum(){
		for (int p = 0; p < size(); p++)
			if (getPixelBoolean(p))
				return true;
		return false;
	}

	/**
	 * Gets the minimum from a band of the image.
	 * @param band
	 * 		Band.
	 * @return
	 * 		The minimum of the selected band as a boolean
	 */
	public boolean minimum(int band){
		for (int p = band; p < size(); p+=this.getBDim())
			if (!getPixelBoolean(p))
				return false;
		return true;
	}
	
	/**
	 * Gets the maximum from a band of the image.
	 * @param band
	 * 		Band.
	 * @return
	 * 		The maximum of the selected band as a boolean
	 */
	public boolean maximum(int band){
		for (int p = band; p < size(); p+=this.getBDim())
			if (getPixelBoolean(p))
				return true;
		return false;
	}
	@Override
	public boolean maximumBoolean() {
		return this.maximum();
	}

	@Override
	public int maximumByte() {
		return booleanToUnsignedByte(this.maximum());
	}

	@Override
	public double maximumDouble() {
		return booleanToDouble(this.maximum());
	}

	@Override
	public double maximumDouble(int band) {
		return booleanToDouble(this.maximum(band));
	}

	@Override
	public double maximumDoubleIgnoreNonRealValues(int band) {
		return booleanToDouble(this.maximum(band));
	}

	@Override
	public int maximumInt() {
		return booleanToInt(this.maximum());
	}

	@Override
	public boolean minimumBoolean() {
		return this.minimum();
	}

	@Override
	public int minimumByte() {
		return booleanToUnsignedByte(this.minimum());
	}

	@Override
	public double minimumDouble() {
		return booleanToDouble(this.minimum());
	}

	@Override
	public double minimumDouble(int band) {
		return booleanToDouble(this.minimum(band));
	}

	@Override
	public double minimumDoubleIgnoreNonRealValues(int band) {
		return booleanToDouble(this.minimum(band));
	}

	@Override
	public int minimumInt() {
		return booleanToInt(this.minimum());
	}	
}

