package fr.unistra.pelican.util.largeImages;

import java.io.File;
import java.util.HashMap;

import fr.unistra.pelican.LargeBooleanImage;
import fr.unistra.pelican.LargeByteImage;
import fr.unistra.pelican.LargeDoubleImage;
import fr.unistra.pelican.LargeIntegerImage;

/**
 * This Interface is designed for using large images with Pelican.</br> Theses
 * large images which can not be fully loaded in memory are cut into small
 * units. </br> When the JVM runs short of memory it will store some of the
 * units on a mass storage device to allow the algorithm to continue.</br>
 * 
 * There is a few more methods needed getPixel,setPixel which depends on the
 * type of pixel used. Do not forget to implement readObject to make your images
 * compatible with PelicanImageSave and PelicanImageLoad
 * 
 * 
 *@see fr.unistra.pelican.util.largeImages.LargeImageUtil
 */
public interface LargeImageInterface {
	
	/**
	 * Gets the horizontal dimension as a long.
	 * @return
	 * 			the horizontal dimension as a long
	 */
	public long getLongXDim();

	/**
	 * Gets the vertical dimension as a long.
	 * @return
	 * 			the vertical dimension as a long
	 */
	public long getLongYDim();

	/**
	 * Gets the depth dimension as a long.
	 * @return
	 * 			the depths dimension as a long
	 */
	public long getLongZDim();

	/**
	 * Gets the time dimension as a long.
	 * @return
	 * 			the time dimension as a long
	 */
	public long getLongTDim();
	
	/**
	 * Gets the channel dimension as a long.
	 * @return
	 * 			the channel dimension as a long
	 */
	public long getLongBDim();

	/**
	 * Sets the dimensions, do not forget to update size
	 */
	public void setDim(int x, int y, int z, int t, int b);

	/**
	 * Sets the size value so it has not to be calculated again.
	 * @param newSize
	 * 			the size of the image as a long
	 */
	public void setSize(long newSize);

	/**
	 * Sets the Unit Dimension to the given value. </br> Unit dimension
	 * indicates how many unit are contained in the image.
	 * 
	 * @param newUnitDim
	 *            new unit dimension value
	 */
	public void setUnitDim(int newUnitDim);

	/**
	 * Sets the Unit Length to the given value</br> Unit length indicates how
	 * many bytes an unit takes once serialized
	 * 
	 * @param newUnitLength
	 *            new unit length value
	 */
	public void setUnitLength(long newUnitLength);

	/**
	 * Initializes the unit power size for this image.</br> The unit size
	 * indicates how many pixels are contained in each unit in power of two.
	 * 
	 * @param newSize
	 *            Power of two corresponding to the size.
	 */
	public void initializeUnitPowerSize(int newSize);

	/**
	 * Gets the unit size.
	 * 
	 * @return the number of pixels in each unit
	 */
	public int getUnitSize();

	/**
	 * Gets the unit size in power of two.
	 * 
	 * @return the number of pixels in each unit in power of two
	 */
	public int getUnitPowerSize();

	/**
	 * Gets the unit dimension for the LargeImage.
	 * 
	 * @return the number of units in this LargeImage
	 */
	public int getUnitDim();

	/**
	 * Gets the unit length.
	 * 
	 * @return the number of bytes needed to serialize an unit
	 */
	public long getUnitLength();

	/**
	 * Gets the memory id of the largeImage.
	 * 
	 * @return the memory id used to identify the LargeImage in the
	 *         LargeImageMemoryManager
	 */
	public int getMemoryId();

	/**
	 * Gets the file where the LargeImage is stored.
	 * 
	 * @return the file where the LargeImage is stored
	 */
	public File getFile();

	/**
	 * Sets the file with which the LargeImage will work .
	 * 
	 * @param fichier
	 *            file in which the LargeImage must save and load its units
	 */
	public void setFile(File fichier);

	/**
	 * Calculates and sets the unitLength and unitDim and size of the
	 * LargeImage.</br> It needs unitSize and the five xdim, ydim, zdim, tdim
	 * and bdim to be set.</br> For usual LargeImage, use the LargeImageUtil
	 * implementation : LargeImageUtil.calculate(this);
	 */
	public void calculate();

	/**
	 * Creates the file the LargeImage will use to load and save its units.</br>
	 * It needs unitDim and unitLength to be set so it could be useful to call
	 * calculate just before.</br> For usual LargeImage, use the LargeImageUtil
	 * implementation : LargeImageUtil.createFile(this);
	 */
	public void createFile();

	/**
	 * Sets an unit in the LargeImages unitMap.</br> For usual LargeImage, use
	 * the LargeImageUtil implementation :
	 * LargeImageUtil.setUnit(this,currentUnit,currentId);
	 * 
	 * @param currentUnit
	 *            Unit to be put in the HashMap
	 * @param currentId
	 *            Place that the unit must take in the LargeImage
	 * @param modified
	 *            Indicates whether the unit has to be saved on file when
	 *            discarded or not
	 */
	public void setUnit(Unit currentUnit, int currentId, boolean modified);

	/**
	 * Discards the unit corresponding to the given id.</br> It checks if it is
	 * not trying to discard an unit which is not in memory.</br> The decision
	 * whether the unit must be saved or just thrown away is made in the
	 * Unit.discard() method.</br> For usual LargeImage, use the LargeImageUtil
	 * implementation : LargeImageUtil.discardUnit(this,currentId);</br>
	 * 
	 * @param currentId
	 *            id of the unit which should be discarded
	 */
	public void discardUnit(int currentId);

	/**
	 * Loads an unit from the file of the LargeImage and put it into the
	 * unitMap</br> For usual LargeImage, use the LargeImageUtil implementation
	 * : return LargeImageUtil.loadAnUnit(this,id);
	 * 
	 * @param id
	 *            number of the unit to be loaded
	 * @return the unit which has been loaded
	 */
	public Unit loadAnUnit(int id);

	/**
	 * Gets an unit of the LargeImage. If the unit is not in the unitMap it
	 * loads it with LoadAnUnit.</br> For usual LargeImage, use the
	 * LargeImageUtil implementation : return LargeImageUtil.getAnUnit(this,id);
	 * 
	 * @param id
	 *            index of the unit to return
	 * @return the unit at the given index
	 */
	public Unit getAnUnit(int id);

	/**
	 * Fills the file with empty units so the loadAnUnit method will not
	 * fail.</br> For usual LargeImage, use the LargeImageUtil implementation :
	 * LargeImageUtil.fillFile(this);
	 */
	public void fillFile();

	/**
	 * Closes the ressources used by the LargeImage.</br> For usual LargeImage,
	 * use the LargeImageUtil implementation :LargeImageUtil.close(this);</br>
	 * LargeImageUtil implementation deletes the file used by the
	 * LargeImage.</br>
	 */
	public void close();

	/**
	 * Calculates and sets the unit length according to the type of units and
	 * the unit size.</br> For usual LargeImage, use the LargeImageUtil
	 * implementation :LargeImageUtil.computeUnitLength(this);
	 */
	public void computeUnitLength();

	/**
	 * Calculates and sets the unit dimension of the LargeImage.</br> For usual
	 * LargeImage, use the LargeImageUtil implementation :
	 * LargeImageUtil.computeUnitDim(this);
	 */
	public void computeUnitDim();

	/**
	 * Computes and sets the unit size in order to make the units fits into the
	 * unitArea.</br> If unitArea is negative, it computes the unit size with
	 * default values contained in LargeImageUtil.</br> For usual LargeImage,
	 * use the LargeImageUtil implementation :
	 * LargeImageUtil.computeUnitSize(this,unitArea,pixelLength);
	 * 
	 * @param unitArea
	 *            Size in byte that the unit array should not exceed
	 */
	public void computeUnitSize(int unitArea);

	/**
	 * Puts the unit into the unitMap of the LargeImage. </br> Implementation
	 * should check if the unit fits with the Image in order to avoid fatal
	 * error.
	 * 
	 * @param currentId
	 *            index where the unit must be added
	 * @param currentUnit
	 *            unit to be put into the map
	 */
	public void putUnitIntoMap(int currentId, Unit currentUnit);

	/**
	 * Gets the HashMap of the large Image
	 * 
	 * @return the HashMap of the largeImage
	 */
	public HashMap<Integer, Unit> getMap();

	/**
	 * Creates an new empty unit with the type of pixel and size according to
	 * the LargeImage attributes
	 * 
	 * @return A new Unit.
	 */
	public Unit newUnit();

	/**
	 * Sets the pixels at the given location to the given value as double
	 * 
	 * @param loc
	 *            index of the pixel to modify
	 * @param value
	 *            desired value of the pixel as double
	 */
	public void setPixelDouble(long loc, double value);

	/**
	 * Sets the pixels at the given location to the given value as int
	 * 
	 * @param loc
	 *            index of the pixel to modify
	 * @param value
	 *            desired value of the pixel as int
	 */
	public void setPixelInt(long loc, int value);

	/**
	 * Sets the pixels at the given location to the given value as byte
	 * 
	 * @param loc
	 *            index of the pixel to modify
	 * @param value
	 *            desired value of the pixel as byte
	 */
	public void setPixelByte(long loc, int value);

	/**
	 * Sets the pixels at the given location to the given value as boolean
	 * 
	 * @param loc
	 *            index of the pixel to modify
	 * @param value
	 *            desired value of the pixel as boolean
	 */
	public void setPixelBoolean(long loc, boolean value);

	/**
	 * Gets the value of the pixel in the given location as double
	 * 
	 * @param loc
	 *            the index of the desired pixel
	 * @return the value of the pixel in the given location as double
	 */
	public double getPixelDouble(long loc);

	/**
	 * Gets the value of the pixel in the given location as int
	 * 
	 * @param loc
	 *            the index of the desired pixel
	 * @return the value of the pixel in the given location as int
	 */
	public int getPixelInt(long loc);

	/**
	 * Gets the value of the pixel in the given location as byte
	 * 
	 * @param loc
	 *            the index of the desired pixel
	 * @return the value of the pixel in the given location as byte
	 */
	public int getPixelByte(long loc);

	/**
	 * Gets the value of the pixel in the given location as boolean
	 * 
	 * @param loc
	 *            the index of the desired pixel
	 * @return the value of the pixel in the given location as boolean
	 */
	public boolean getPixelBoolean(long loc);

	/**
	 * Gets the size of the Image
	 * 
	 * @return the total number of pixels in the image
	 */
	public int size();

	/**
	 * Saves all units contained in the HashMap
	 */
	public void saveData();

	/**
	 * Returns the file suffix for this large image, for instance JMFVideo must
	 * return ".avi"
	 * 
	 * @return
	 */
	public String getWorkingFileSuffix();

	/**
	 * Methods inherited from Image
	 */
	public int getXDim();

	public int getYDim();

	public int getZDim();

	public int getTDim();

	public int getBDim();

	/**
	 * Methods inherited from Image (must override)
	 */

	public void setXDim(int xdim);

	public void setYDim(int ydim);

	public void setZDim(int zdim);

	public void setTDim(int tdim);

	public void setBDim(int bdim);

	public void fill(double b);

	public double volume();

	public int volumeByte();

	public abstract double getPixelDouble(int loc);

	public abstract int getPixelInt(int loc);

	public abstract int getPixelByte(int loc);

	public abstract boolean getPixelBoolean(int loc);

	public abstract void setPixelDouble(int loc, double value);

	public abstract void setPixelInt(int loc, int value);

	public abstract void setPixelByte(int loc, int value);

	public abstract void setPixelBoolean(int loc, boolean value);

	public double getPixelDouble(int x, int y, int z, int t, int b);

	public double getPixelXYDouble(int x, int y);

	public double getPixelXYZDouble(int x, int y, int z);

	public double getPixelXYBDouble(int x, int y, int b);

	public double getPixelXYTDouble(int x, int y, int t);

	public double getPixelXYZTDouble(int x, int y, int z, int t);

	public double getPixelXYZBDouble(int x, int y, int z, int b);

	public double getPixelXYTBDouble(int x, int y, int t, int b);

	public double getPixelXYZTBDouble(int x, int y, int z, int t, int b);

	public void setPixelDouble(int x, int y, int z, int t, int b, double value);

	public void setPixelXYDouble(int x, int y, double value);

	public void setPixelXYZDouble(int x, int y, int z, double value);

	public void setPixelXYBDouble(int x, int y, int b, double value);

	public void setPixelXYTDouble(int x, int y, int t, double value);

	public void setPixelXYZTDouble(int x, int y, int z, int t, double value);

	public void setPixelXYZBDouble(int x, int y, int z, int b, double value);

	public void setPixelXYTBDouble(int x, int y, int t, int b, double value);

	public void setPixelXYZTBDouble(int x, int y, int z, int t, int b,
			double value);

	public int getPixelInt(int x, int y, int z, int t, int b);

	public int getPixelXYInt(int x, int y);

	public int getPixelXYZInt(int x, int y, int z);

	public int getPixelXYBInt(int x, int y, int b);

	public int getPixelXYTInt(int x, int y, int t);

	public int getPixelXYZTInt(int x, int y, int z, int t);

	public int getPixelXYZBInt(int x, int y, int z, int b);

	public int getPixelXYTBInt(int x, int y, int t, int b);

	public int getPixelXYZTBInt(int x, int y, int z, int t, int b);

	public void setPixelInt(int x, int y, int z, int t, int b, int value);

	public void setPixelXYInt(int x, int y, int value);

	public void setPixelXYZInt(int x, int y, int z, int value);

	public void setPixelXYBInt(int x, int y, int b, int value);

	public void setPixelXYTInt(int x, int y, int t, int value);

	public void setPixelXYZTInt(int x, int y, int z, int t, int value);

	public void setPixelXYZBInt(int x, int y, int z, int b, int value);

	public void setPixelXYTBInt(int x, int y, int t, int b, int value);

	public void setPixelXYZTBInt(int x, int y, int z, int t, int b, int value);

	public int getPixelByte(int x, int y, int z, int t, int b);

	public int getPixelXYByte(int x, int y);

	public int getPixelXYZByte(int x, int y, int z);

	public int getPixelXYBByte(int x, int y, int b);

	public int getPixelXYTByte(int x, int y, int t);

	public int getPixelXYZTByte(int x, int y, int z, int t);

	public int getPixelXYZBByte(int x, int y, int z, int b);

	public int getPixelXYTBByte(int x, int y, int t, int b);

	public int getPixelXYZTBByte(int x, int y, int z, int t, int b);

	public void setPixelByte(int x, int y, int z, int t, int b, int value);

	public void setPixelXYByte(int x, int y, int value);

	public void setPixelXYZByte(int x, int y, int z, int value);

	public void setPixelXYBByte(int x, int y, int b, int value);

	public void setPixelXYTByte(int x, int y, int t, int value);

	public void setPixelXYZTByte(int x, int y, int z, int t, int value);

	public void setPixelXYZBByte(int x, int y, int z, int b, int value);

	public void setPixelXYTBByte(int x, int y, int t, int b, int value);

	public void setPixelXYZTBByte(int x, int y, int z, int t, int b, int value);

	public boolean getPixelBoolean(int x, int y, int z, int t, int b);

	public boolean getPixelXYBoolean(int x, int y);

	public boolean getPixelXYZBoolean(int x, int y, int z);

	public boolean getPixelXYBBoolean(int x, int y, int b);

	public boolean getPixelXYTBoolean(int x, int y, int t);

	public boolean getPixelXYZTBoolean(int x, int y, int z, int t);

	public boolean getPixelXYZBBoolean(int x, int y, int z, int b);

	public boolean getPixelXYTBBoolean(int x, int y, int t, int b);

	public boolean getPixelXYZTBBoolean(int x, int y, int z, int t, int b);

	public void setPixelBoolean(int x, int y, int z, int t, int b, boolean value);

	public void setPixelXYBoolean(int x, int y, boolean value);

	public void setPixelXYZBoolean(int x, int y, int z, boolean value);

	public void setPixelXYBBoolean(int x, int y, int b, boolean value);

	public void setPixelXYTBoolean(int x, int y, int t, boolean value);

	public void setPixelXYZTBoolean(int x, int y, int z, int t, boolean value);

	public void setPixelXYZBBoolean(int x, int y, int z, int b, boolean value);

	public void setPixelXYTBBoolean(int x, int y, int t, int b, boolean value);

	public void setPixelXYZTBBoolean(int x, int y, int z, int t, int b,
			boolean value);

	/*
	 * Mask management**************************************************
	 */

	public boolean isInMask(int loc);

	public boolean isInMask(long loc);

	public boolean isPresent(int loc);

	public boolean isPresent(long loc);

	public int getNumberOfPresentPixel();

	public int getNumberOfPresentPixel(int band);
	
	
	/*
	 * Constructors ********************************************************
	 */
	
	public LargeBooleanImage newBooleanImage();

	public LargeBooleanImage newBooleanImage(boolean copyData);

	public LargeBooleanImage newBooleanImage(int x, int y, int z, int t, int b);

	public LargeByteImage newByteImage();

	public LargeByteImage newByteImage(boolean copyData);

	public LargeByteImage newByteImage(int x, int y, int z, int t, int b);

	public LargeIntegerImage newIntegerImage();

	public LargeIntegerImage newIntegerImage(boolean copyData);
	
	public LargeIntegerImage newIntegerImage(int x, int y, int z, int t, int b);

	public LargeDoubleImage newDoubleImage();
	
	public LargeDoubleImage newDoubleImage(boolean copyData);
	
	public LargeDoubleImage newDoubleImage(int x, int y, int z, int t, int b);
}
