package fr.unistra.pelican;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

/** This class represents an integer valued image
 * Range for an integer pixel is [Integer.MIN_VALUE; Integer.MAX_VALUE]
 * @author PELICAN team
 * @version 1.0
 */

public class IntegerImage extends Image implements Serializable
{

	/**
	 *	Pixel data array
	 */

	private int[] pixels;

	/**
	 *	Serial version ID
	 */

	private static final long serialVersionUID = 3L;
	
	/**
	 * Constructs an IntegerImage
	 */
	protected IntegerImage(){
		super();
	}

	/**
	 * Constructs an IntegerImage identical to the given argument
	 * @param	image	IntegerImage to copy
	 */

	public IntegerImage(IntegerImage image)
	{
		super(image);
		this.pixels = (int[])image.pixels.clone();
	}
	
	/**
	 * Constructs a IntegerImage identical to the given argument
	 * @param	image	Image to copy
	 */
	public IntegerImage(Image image)
	{
		this(image, true);
	}
	
	/**
	 * Constructs a IntegerImage identical to the given argument
	 * @param	image	Image to copy
	 * @param	copyData	if and only if it is set to true are the pixels copied
	 */
	public IntegerImage(Image image, boolean copyData)
	{
		super(image);
		this.pixels = new int[image.getXDim() * image.getYDim() * image.getZDim() * image.getTDim() * image.getBDim()];
		if(copyData == true)
			for(int i = 0; i < pixels.length; i++)
				setPixelInt(i, image.getPixelInt(i)); 
	}
	
	/**
	 * Constructs an IntegerImage from the given argument. The pixels are copied if and only of ''copy'' is set to true.
	 * @param	image	IntegerImage to copy
	 * @param	copyData	if and only if it is set to true are the pixels copied
	 */

	public IntegerImage(IntegerImage image, boolean copyData)
	{
            super(image);

		if(copyData == true)
			this.pixels = (int[])image.pixels.clone();
		else
			this.pixels = new int[image.getXDim() * image.getYDim() * image.getZDim() * image.getTDim() * image.getBDim()];
	}

	/**
	 * Constructs an IntegerImage with the given dimensions
	 * @param	xdim	the horizontal dimension
	 * @param	ydim	the vertical dimension
	 * @param	zdim	the depth
	 * @param	tdim	the frame number
	 * @param	bdim	the channel number
	 */

	public IntegerImage(int xdim, int ydim, int zdim, int tdim, int bdim)
	{
		super(xdim,ydim,zdim,tdim,bdim);
		this.pixels = new int[xdim * ydim * zdim * tdim * bdim];
	}
	
	
	/**
	 * Sets the pixels to the copy of the given array
	 * @param	values	pixel array to copy
	 */

	public void setPixels(int[] values)
	{
		pixels = (int[])values.clone();
	}

	/**
	 * Sets all the pixel values to the given value
	 * @param	b	Desired value for the pixels
	 */

	public void fill(int b)
	{
		Arrays.fill(pixels, b);
	}

	@Override
	public void fill(double d) {
		this.fill(doubleToInt(d));		
	}

	/**
	 * Creates a copy of this IntegerImage
	 * @return	an exact copy of this IntegerImage
	 */

	public IntegerImage copyImage(boolean copyData)
	{
		return new IntegerImage(this,copyData);
	}

	/**
	 * Creates a new instance of IntegerImage
	 * @param	xdim	the horizontal dimension
	 * @param	ydim	the vertical dimension
	 * @param	zdim	the depth
	 * @param	tdim	the frame number
	 * @param	bdim	the channel number
	 */
	public Image newInstance(int xdim,int ydim,int zdim,int tdim,int bdim) {
		return new IntegerImage(xdim,ydim,zdim,tdim,bdim);
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
	 * Convert the IntegerImage to a ByteImage without modifying the values 
	 * @return	a byte image
	 */
	public ByteImage copyToByteImage() {
		ByteImage b=new ByteImage(this,false);
		for (int p=0;p<b.size();p++)
			b.setPixelByte(p,getPixelInt(p));
		return b;
	}

	/**
	 * Convert the IntegerImage to a ByteImage using the scaleToVisibleRange method 
	 * @return	a byte image
	 */
	public ByteImage convertToByteImage() {
		IntegerImage i=scaleToVisibleRange();
		ByteImage b=new ByteImage(i,false);
		for (int p=0;p<b.size();p++)
			b.setPixelByte(p,i.getPixelByte(p));
		return b;
	}
	
	/**
	 * Scales all values to [MIN_VALUE,MAX_VALUE] for visualisation purposes.
	 * 
	 * @return the scaled image
	 */
	public IntegerImage scaleToVisibleRange() { //TODO Fusion with convertToByteImage and check that nothing goes wrong without the byte cast
		IntegerImage d = (IntegerImage) this.copyImage(false);

		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;

		for (int p = 0; p < size(); p++) {
			int tmp = this.getPixelInt(p);
			if (min > tmp)
				min = tmp;
			if (max < tmp)
				max = tmp;
		}
		//int dist = max - min;
		long dist = (long)max - (long)min;
		double value;
		if (max != min) {
			for (int p = 0; p < size(); p++) {
				int tmp = this.getPixelInt(p);
				//tmp = (tmp - min) * 255 / dist;
				//tmp = (int)((double) (tmp - min) / dist * 255);
				value = ((double) ((long)tmp - (long)min) / dist);
				d.setPixelDouble(p, value);
			}
		} else
			System.err.println("Scaling error");
		return d;
	}

//	/**
//	 * Scales all channels and frames independently to [0,255] for visualisation purposes.
//	 * As the relative contrast gets messed up, even if the channel in question is already in [0,255] it is stretched all the same. 
//	 * @return	an integer image with pixel values in [0,255]
//	 */
//	public IntegerImage scaleToVisibleRange()
//	{
//		IntegerImage d = (IntegerImage)this.copyImage(false);
//		
//		// the scaling must be realized on XYZ volumes for every channel/frame separately
//		for(int b = 0; b < bdim; b++)
//			for(int t = 0; t < tdim; t++){
//				int min = Integer.MAX_VALUE;
//				int max = Integer.MIN_VALUE;
//					
//				// get the extrema
//				for(int z = 0; z < zdim; z++)					
//					for(int x = 0; x < xdim; x++)
//						for(int y = 0; y < ydim; y++){
//							int tmp = this.getPixelXYZTBInt(x,y,z,t,b);
//							if(min > tmp) min = tmp;
//							if(max < tmp) max = tmp;
//						}
//					
//					int dist = max - min;
//					
//					if(max != min){
//						for(int z = 0; z < zdim; z++)						
//							for(int x = 0; x < xdim; x++)
//								for(int y = 0; y < ydim; y++){
//									int tmp = this.getPixelXYZTBInt(x,y,z,t,b);
//									//tmp = (tmp - min) * 255 / dist;
//									tmp = (int)((double) (tmp - min) / dist * 255);
//									d.setPixelXYZTBByte(x,y,z,t,b,tmp);
//								}
//						
//					}else{
//						for(int z = 0; z < zdim; z++)						
//							for(int x = 0; x < xdim; x++)
//								for(int y = 0; y < ydim; y++)
//									d.setPixelXYZTBInt(x,y,z,t,b,min);
//					}
//				}
//		
//		return d;
//	}

	/**
	 * Scales all values to [0,1] for visualisation purposes.
	 * TODO delete this method if it is as useless as it seems
	 * @return the scaled image
	 */
	public DoubleImage normalise() {
		DoubleImage d = new DoubleImage(this,false);

		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;

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
				double tmp = this.getPixelInt(p);
				tmp = (tmp - min) / dist;
				d.setPixelDouble(p, tmp);
			}
		} else
			System.err.println("Scaling error");
		return d;
	}

	
//	/**
//	 * Scales all channels and frames independently to [0,1] for normalisation purposes.
//	 * As the relative contrast gets messed up, even if the channel in question is already in [0,1] it is stretched all the same. 
//	 * @return	a double image with pixel values in [0,1]
//	 */
//	public DoubleImage normalise()
//	{
//		DoubleImage d = new DoubleImage(this,false);
//		
//		// the scaling must be realized on XYZ volumes for every channel/frame separately
//		for(int b = 0; b < bdim; b++)
//			for(int t = 0; t < tdim; t++){
//				int min = Integer.MAX_VALUE;
//				int max = Integer.MIN_VALUE;
//					
//				// get the extrema
//				for(int z = 0; z < zdim; z++)					
//					for(int x = 0; x < xdim; x++)
//						for(int y = 0; y < ydim; y++){
//							int tmp = this.getPixelXYZTBInt(x,y,z,t,b);
//							if(min > tmp) min = tmp;
//							if(max < tmp) max = tmp;
//						}
//					
//					int dist = max - min;
//					
//					if(max != min){
//						for(int z = 0; z < zdim; z++)						
//							for(int x = 0; x < xdim; x++)
//								for(int y = 0; y < ydim; y++){
//									double tmp = this.getPixelXYZTBInt(x,y,z,t,b);
//									//tmp = (tmp - min) * 255 / dist;
//									tmp = (double) (tmp - min) / dist;
//									d.setPixelXYZTBDouble(x,y,z,t,b,tmp);
//								}
//						
//					}else{
//						for(int z = 0; z < zdim; z++)						
//							for(int x = 0; x < xdim; x++)
//								for(int y = 0; y < ydim; y++)
//									d.setPixelXYZTBDouble(x,y,z,t,b,0);
//					}
//				}
//		
//		return d;
//	}

	
	@Override
	public double getPixelDouble(int loc) {
		return intToDouble(pixels[loc]);
		//return DoubleImage.intToDouble*(double)pixels[loc] + 0.5;
	}

	@Override
	public int getPixelInt(int loc) {		
		return (pixels[loc]);
	}

	@Override
	public int getPixelByte(int loc) {
		return intToUnsignedByte(pixels[loc]);
		//return (pixels[loc] >> 24) - Byte.MIN_VALUE;
	}

	@Override
	public boolean getPixelBoolean(int loc) {
		return intToBoolean(pixels[loc]);
		//return (pixels[loc] > Integer.MAX_VALUE/2) ? true : false;
	}

	@Override
	public void setPixelDouble(int loc, double value) {
		pixels[loc] = doubleToInt(value);
		//pixels[loc] = (int)((value - 0.5) * doubleToInt); 
	}

	@Override
	public void setPixelInt(int loc, int value) {		
		pixels[loc] = value;
	}

	@Override
	public void setPixelByte(int loc, int value) {
		pixels[loc] = unsignedByteToInt(value);
		//pixels[loc] = (value  + Byte.MIN_VALUE) << 24;
	}

	@Override
	public void setPixelBoolean(int loc, boolean value) {
		pixels[loc]= booleanToInt(value);
		//pixels[loc] = value ? Integer.MAX_VALUE : 0;
	}

	@Override
	public boolean equals(Image im) {
		if(im==null  || !(im instanceof IntegerImage))
			return false;
		
		if(!haveSameDimensions(this,im))
			return false;

		int size = size();
		for(int i = 0; i < size; i++)
			if(im.getPixelInt(i) != getPixelInt(i))
				return false;

		return true;
	}
	

	@Override
	public void setPixel(Image input, int x1, int y1, int z1, int t1, int b1,
		int x2, int y2, int z2, int t2, int b2) {
		this.setPixelInt(x1, y1, z1, t1, b1, input
			.getPixelInt(x2, y2, z2, t2, b2));
	}
	
	/**
	 * Computes the minimum value of the image
	 * @return
	 * 		the integer representation of the minimum
	 */
	public int minimum(){
		int val = Integer.MAX_VALUE;
		for (int p = 0; p < size(); p++)
			if (getPixelInt(p) < val)
				val = getPixelInt(p);
		return val;
	}
	
	/**
	 * Computes the maximum value of the image
	 * @return
	 * 		the integer representation of the maximum
	 */
	public int maximum(){
		int val = Integer.MIN_VALUE;
		for (int p = 0; p < size(); p++)
			if (getPixelInt(p) > val)
				val = getPixelInt(p);
		return val;
	}
	
	/**
	 * Computes the minimum value of the image in the specified band
	 * @param band
	 * 		Band.
	 * @return
	 * 		the integer representation of the minimum in the specified band
	 */
	public int minimum(int band){
		int val = Integer.MAX_VALUE;
		for (int p = band; p < size(); p+=this.getBDim())
			if (getPixelInt(p) < val)
				val = getPixelInt(p);
		return val;
	}
	
	/**
	 * Computes the maximum value of the image in the specified band
	 * @param band
	 * 		Band.
	 * @return
	 * 		the integer representation of the maximum in the specified band
	 */
	public int maximum(int band){
		int val = Integer.MIN_VALUE;
		for (int p = band; p < size(); p+=this.getBDim())
			if (getPixelInt(p) > val)
				val = getPixelInt(p);
		return val;
	}
	
	/**
	 * Regroup the labels values to avoid unused label values
	 * in label values range.
	 * 
	 * Do not modified label 0 due to its special use in certain algorithm
	 */
	public void regroupLabels()
	{
		int nbLabels=this.maximumInt()+1;
		boolean[] isLabelUsed = new boolean[nbLabels];
		Arrays.fill(isLabelUsed, false);
		for(int i=1;i<this.size();i++)
		{
			isLabelUsed[this.getPixelInt(i)]=true;
		}
		int lut[] = new int[nbLabels];
		lut[0]=0;
		int currentLabel=1;
		for(int i=1;i<nbLabels;i++)
		{
			if(isLabelUsed[i])
				lut[i]=currentLabel++;
		}
		for(int i=1;i<this.size();i++)
		{
			this.setPixelInt(i,lut[this.getPixelInt(i)]);
		}
	}
	
	/**
	 * Computes the number of different labels used in the image
	 */
	public int getNumberOfUsedLabels()
	{
		boolean[] isLabelUsed = new boolean[this.maximumInt()+1];
		Arrays.fill(isLabelUsed, false);
		if(this.getMask()==null||this.getMask().isEmpty())
		{
			for(int i=0;i<this.size();i++)
			{
				isLabelUsed[this.getPixelInt(i)]=true;
			}
		}
		else
		{
			for(int i=0;i<this.size();i++)
			{
				if(this.isPresent(i))
					isLabelUsed[this.getPixelInt(i)]=true;
			}
		}
		int numberOfUsedLabels=0;
		for(int i=0;i<isLabelUsed.length;i++)
		{
			if(isLabelUsed[i])
				numberOfUsedLabels++;
		}
		return numberOfUsedLabels;
	}
	
	/**
	 * Return an arraylist of used labels (multiband not managed)
	 */
	public ArrayList<Integer> getUsedLabels()
	{
		ArrayList<Integer> usedLabels = new ArrayList<Integer>();
		for(int i=0;i<this.size();i++)
		{
			if(!usedLabels.contains(this.getPixelInt(i)))
			{
				usedLabels.add(this.getPixelInt(i));
			}
		}
		return usedLabels;
	}
	
	@Override
	public boolean maximumBoolean() {
		return intToBoolean(this.maximum());
	}

	@Override
	public int maximumByte() {
		return intToUnsignedByte(this.maximum());
	}

	@Override
	public double maximumDouble() {
		return intToDouble(this.maximum());
	}

	@Override
	public double maximumDouble(int band) {
		return intToDouble(this.maximum(band));
	}

	@Override
	public double maximumDoubleIgnoreNonRealValues(int band) {
		return intToDouble(this.maximum(band));
	}

	@Override
	public int maximumInt() {
		return this.maximum();
	}
	
	@Override
	public boolean minimumBoolean() {
		return intToBoolean(this.minimum());
	}

	@Override
	public int minimumByte() {
		return intToUnsignedByte(this.minimum());
	}

	@Override
	public double minimumDouble() {
		return intToDouble(this.minimum());
	}

	@Override
	public double minimumDouble(int band) {
		return intToDouble(this.minimum(band));
	}

	@Override
	public double minimumDoubleIgnoreNonRealValues(int band) {
		return intToDouble(this.minimum(band));
	}

	@Override
	public int minimumInt() {
		return this.minimum();
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
	 *         
	 *  TODO : correct the bug or not ? possible side-effect ...
	 */
	public final int[] getVectorPixelByte(int index) 
	{
		int[] vector = new int[bdim];		
		for (int b = 0; b < bdim; b++)
			vector[b] = pixels[index++];
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
	public int[] getVectorPixelInt(int index) {
		int[] vector = new int[bdim];		
		for (int b = 0; b < bdim; b++)
			vector[b] = pixels[index++];
		return vector;
	}
}