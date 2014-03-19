package fr.unistra.pelican.util.largeImages;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;

import fr.unistra.pelican.Image;
import fr.unistra.pelican.LargeBooleanImage;
import fr.unistra.pelican.LargeByteImage;
import fr.unistra.pelican.LargeDoubleImage;
import fr.unistra.pelican.LargeIntegerImage;
import fr.unistra.pelican.PelicanException;

/**
 * This Class is used to define useful methods for large images.</br> It also
 * contains some constants for default memory managing.
 * 
 *@see fr.unistra.pelican.util.largeImages.LargeImageInterface
 */
public class LargeImageUtil {

	/**
	 * DEFAULT_MEMORY_FREE_RATIO is used to set the threshold of the low memory
	 * event.
	 */
	public static final double DEFAULT_MEMORY_FREE_RATIO = 0.1;

	/**
	 * DEFAULT_DIRECTORY is where all files used by Pelican will be created (set
	 * null to create them in the temporary directory)
	 */
	private static final File DEFAULT_DIRECTORY = null;

	/**
	 * DEFAULT_DISCARD_NUMBER is the number of unit which will be discarded when
	 * low memory event occurs
	 */
	public static final int DEFAULT_DISCARD_NUMBER = 10;

	/**
	 * DEFAULT_NUMBER_OF_UNITS is used to set the size of units. Size of units
	 * is maximized under the constraint that DEFAULT_NUMBER_OF_UNITS units can
	 * be stored in the JVM memory.
	 */
	public static final int DEFAULT_NUMBER_OF_UNITS = 50;

	/**
	 * Indicates how many bytes are used by java to store a boolean.
	 */
	public static final int BOOLEAN_DATALENGTH = 1;

	/**
	 * Indicates how many bytes are used by java to store a byte.
	 */
	public static final int BYTE_DATALENGTH = 1;

	/**
	 * Indicates how many bytes are used by java to store an integer.
	 */
	public static final int INTEGER_DATALENGTH = 4;

	/**
	 * Indicates how many bytes are used by java to store a double.
	 */
	public static final int DOUBLE_DATALENGTH = 8;

	/**
	 * @see fr.unistra.pelican.util.largeImages.LargeImageInterface#calculate()
	 */
	public static final void calculate(LargeImageInterface largeIm) {
		largeIm.computeUnitLength();
		largeIm.computeUnitDim();
	}

	/**
	 * @see fr.unistra.pelican.util.largeImages.LargeImageInterface#computeUnitLength()
	 */
	public static final void computeUnitLength(LargeImageInterface largeIm) {
		ByteArrayOutputStream byteArray = null;
		ObjectOutputStream objOut = null;
		try {
			try {
				byteArray = new ByteArrayOutputStream();
				objOut = new ObjectOutputStream(byteArray);
				Unit currentUnit = largeIm.newUnit();
				objOut.writeObject(currentUnit);
				largeIm.setUnitLength(byteArray.size());
			} finally {
				if (byteArray != null) {
					byteArray.close();
				}
				if (objOut != null) {
					objOut.close();
				}
			}
		} catch (IOException e) {
			throw new PelicanException("Unable to determine unitlenght");
		}
	}

	/**
	 * @see fr.unistra.pelican.util.largeImages.LargeImageInterface#computeUnitDim()
	 */
	public static final void computeUnitDim(LargeImageInterface largeIm) {
		long size = (long) largeIm.getXDim() * (long) largeIm.getYDim()
				* (long) largeIm.getZDim() * (long) largeIm.getTDim()
				* (long) largeIm.getBDim();
		largeIm
				.setUnitDim((int) (((size - 1) >> largeIm.getUnitPowerSize()) + 1));
	}

	/**
	 * @see fr.unistra.pelican.util.largeImages.LargeImageInterface#createFile()
	 */
	public static final void createFile(LargeImageInterface largeIm) {
		try {
			largeIm.setFile(File.createTempFile("largImage", largeIm.getWorkingFileSuffix(),
					LargeImageUtil.DEFAULT_DIRECTORY));
			largeIm.getFile().deleteOnExit();

			RandomAccessFile raAccess = new RandomAccessFile(largeIm.getFile(),"rw");

			try {
				raAccess.setLength((long) largeIm.getUnitLength()
						* (long) largeIm.getUnitDim());
			} finally {
				if (raAccess != null) {
					raAccess.close();
				}
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			throw new PelicanException("Unable to work in file "
					+ largeIm.getFile().getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
			throw new PelicanException("Unable to work in file "
					+ largeIm.getFile().getAbsolutePath());
		}
	}

	/**
	 * @see fr.unistra.pelican.util.largeImages.LargeImageInterface#setUnit(Unit,
	 *   int, boolean)
	 */
	public static final void setUnit(LargeImageInterface largeIm, Unit currentUnit,
			int currentId, boolean modified) {
		
			currentUnit.setModified(modified);
			currentUnit.setId(currentId);
			currentUnit.setParentImage(largeIm);
			currentUnit.computeOffsets();
			
		LargeImageMemoryManager.getInstance().lock.lock();
		try{
			largeIm.putUnitIntoMap(currentId, currentUnit);
			LargeImageMemoryManager.getInstance().notifyUsage(largeIm.getMemoryId(), currentId);
		}finally{
			LargeImageMemoryManager.getInstance().lock.unlock();
		}
	}

	/**
	 * @see fr.unistra.pelican.util.largeImages.LargeImageInterface#discardUnit(int)
	 */
	public static final void discardUnit(LargeImageInterface largeIm, int currentId) {
		LargeImageMemoryManager.getInstance().lock.lock();
		try{
			Unit currentUnit = largeIm.getMap().remove(currentId);
			if (currentUnit == null) {
				throw new PelicanException("It tried to discard an unit which was not in memory");
			}
			currentUnit.discard();
		}finally{
			LargeImageMemoryManager.getInstance().lock.unlock();
		}
	}

	/**
	 * @see fr.unistra.pelican.util.largeImages.LargeImageInterface#loadAnUnit(int)
	 */
	public static final Unit loadAnUnit(LargeImageInterface largeIm, int id) {
		Unit inputUnit;
		if (id >= largeIm.getUnitDim()) {
			throw new PelicanException("there is no " + id
					+ "th unit in this image");
		}
		try {
			FileInputStream fileInput = new FileInputStream(largeIm.getFile()
					.getAbsolutePath());
			try {
				fileInput.skip((long) id * (long) largeIm.getUnitLength());
				ObjectInputStream objectInput = new ObjectInputStream(fileInput);
				inputUnit = (Unit) objectInput.readObject();
				if (inputUnit instanceof EmptyUnit) {
					inputUnit = largeIm.newUnit();
				}
				largeIm.setUnit(inputUnit, id, false);

			} finally {
				if (fileInput != null) {
					fileInput.close();
				}
			}
		} catch (IOException e) {			
			e.printStackTrace();
			throw new PelicanException("Unable to work in the file "
					+ largeIm.getFile().getAbsolutePath());
		} catch (ClassNotFoundException e) {
			throw new PelicanException("Unit not found in "
					+ largeIm.getFile().getAbsolutePath());
		}
		return inputUnit;
	}

	/**
	 * @see fr.unistra.pelican.util.largeImages.LargeImageInterface#getAnUnit(int)
	 */
	public static final Unit getAnUnit(LargeImageInterface largeIm, int id) {
		LargeImageMemoryManager.getInstance().lock.lock();
		Unit res = null;
		try{
			res = largeIm.getMap().get(id);			
			if (res == null) {
				res = largeIm.loadAnUnit(id);
			}
		}finally{
			LargeImageMemoryManager.getInstance().lock.unlock();
		}
		return res;
	}

	/**
	 * @see fr.unistra.pelican.util.largeImages.LargeImageInterface#fillFile()
	 */
	public static final void fillFile(LargeImageInterface largeIm) {
		Unit emptyUnit = new EmptyUnit();
		emptyUnit.setModified();
		emptyUnit.setParentImage(largeIm);
		for (int i = 0; i < largeIm.getUnitDim(); i++) {
			emptyUnit.setId(i);
			emptyUnit.setModified();			
			emptyUnit.discard();
		}
	}

	/**
	 * @see fr.unistra.pelican.util.largeImages.LargeImageInterface#close()
	 */
	public static final void close(LargeImageInterface largeIm) {
		largeIm.getFile().delete();
	}

	/**
	 * @param i
	 * @see fr.unistra.pelican.util.largeImages.LargeImageInterface#computeUnitSize(int)
	 */
	public static final void computeUnitSize(LargeImageInterface largeIm,
			int maxSize, int dataLength) {
		if (maxSize <= 0) {
			// if no argument is specified we use default settings
			long maxMem = LargeImageMemoryManager.getInstance().getMaxTenuredMemory();
			//Runtime run = Runtime.getRuntime();
			//long maxMem = run.maxMemory();
			long size = maxMem / LargeImageUtil.DEFAULT_NUMBER_OF_UNITS;
			int j = LargeImageUtil.computePowerOfTwo(size / dataLength);
			largeIm.initializeUnitPowerSize(j);
		} else {
			long area = maxSize << 20;
			int j = LargeImageUtil.computePowerOfTwo(area / dataLength);
			largeIm.initializeUnitPowerSize(j);
		}
	}

	/**
	 * @see fr.unistra.pelican.util.largeImages.LargeImageInterface#saveData()
	 */
	public static final void saveData(LargeImageInterface largeIm) {
		LargeImageMemoryManager.getInstance().lock.lock();
		try{
			for (Unit currentUnit : largeIm.getMap().values()) {
				currentUnit.discard();
			}
		}finally{
			LargeImageMemoryManager.getInstance().lock.unlock();
		}
	}

	/**
	 * Returns the higher power of two inferior to the given number.
	 * 
	 * @param i
	 *      Maximal value for the power of two
	 * @return The power of two corresponding
	 */
	public static final int computePowerOfTwo(long i) {
		if (i < 0) {
			throw new PelicanException(
					"Can not compute power of two for negative numbers ");
		}
		long size = i;
		int j = -1;
		while (size != 0) {
			size = size >> 1;
			j++;
		}
		return j;
	}

	/*
	 * **********************************************************************
	 */

	/**
	 * @see fr.unistra.pelican.Image#volume()
	 */
	public static final double volume(LargeImageInterface largeIm) {
		double v = 0;
		for (long p = 0; p < largeIm.size(); p++) {
			if (largeIm.isPresent(p)) {
				v += largeIm.getPixelDouble(p);
			}
		}
		return v;
	}

	/**
	 * This method may not be accurate with a lot of pixels
	 * 
	 * @see fr.unistra.pelican.Image#volumeByte()
	 */
	public static final int volumeByte(LargeImageInterface largeIm) {
		// TODO
		System.err.println("VolumeByte was called on a large image, the result may be unaccurate");
		int v = 0;
		for (long p = 0; p < largeIm.size(); p++) {
			if (largeIm.isPresent(p)) {
				v += largeIm.getPixelByte(p);
			}
		}
		return v;
	}

	/**
	 * Gets the LinearIndex (z=t=b=0).
	 * 
	 * @param largeIm
	 *      Image for which the linear index must be computed
	 * @param x
	 *      horizontal index
	 * @param y
	 *      vertical index
	 * @return the linear index
	 */
	public static final long getLinearIndexXY___(LargeImageInterface largeIm, int x, int y) {
		return largeIm.getLongBDim()* ((long) x + largeIm.getLongXDim() * (long) y);
	}

	/**
	 * Gets the linear index (t=b=0).
	 * 
	 * @param largeIm
	 *      Image for which the linear index must be computed
	 * @param x
	 *      horizontal index
	 * @param y
	 *      vertical index
	 * @param z
	 *      depth index
	 * @return the linear index
	 */
	public static final long getLinearIndexXYZ__(LargeImageInterface largeIm, int x, int y, int z) {
		return largeIm.getLongBDim()* ((long) x + largeIm.getLongXDim()* ((long) y + largeIm.getLongYDim() * (long) z));
	}

	/**
	 * Gets the linear index (z=t=0).
	 * 
	 * @param largeIm
	 *      Image for which the linear index must be computed
	 * @param x
	 *      horizontal index
	 * @param y
	 *      vertical index
	 * @param b
	 *      band index
	 * @return the linear index
	 */
	public static final long getLinearIndexXY__B(LargeImageInterface largeIm, int x, int y, int b) {
		return (long) b + largeIm.getLongBDim()	* ((long) x + largeIm.getLongXDim() * (long) y);
	}

	/**
	 * Gets the linear index (z=b=0).
	 * 
	 * @param largeIm
	 *      Image for which the linear index must be computed
	 * @param x
	 *      horizontal index
	 * @param y
	 *      vertical index
	 * @param t
	 *      time index
	 * @return the linear index
	 */
	public static final long getLinearIndexXY_T_(LargeImageInterface largeIm, int x,
			int y, int t) {
		return largeIm.getLongBDim() * ((long) x + largeIm.getLongXDim() * ((long) y + largeIm.getLongYDim() * ((long) t * largeIm.getLongZDim())));
	}

	/**
	 * Gets the linear index (b=0).
	 * 
	 * @param largeIm
	 *      Image for which the linear index must be computed
	 * @param x
	 *      horizontal index
	 * @param y
	 *      vertical index
	 * @param z
	 *      depth index
	 * @param t
	 *      time index
	 * @return the linear index
	 */
	public static final long getLinearIndexXYZT_(LargeImageInterface largeIm, int x, int y, int z, int t) {
		return largeIm.getLongBDim() * ((long) x + largeIm.getLongXDim() * ((long) y + largeIm.getLongYDim() * ((long) z + (long) t * largeIm.getLongZDim())));
	}

	/**
	 * Gets the linear index (t=0).
	 * 
	 * @param largeIm
	 *      Image for which the linear index must be computed
	 * @param x
	 *      horizontal index
	 * @param y
	 *      vertical index
	 * @param z
	 *      depth index
	 * @param b
	 *      band index
	 * @return the linear index
	 */
	public static final long getLinearIndexXYZ_B(LargeImageInterface largeIm, int x, int y, int z, int b) {
		return (long) b	+ largeIm.getLongBDim()	* ((long) x + largeIm.getLongXDim()	* ((long) y + largeIm.getLongYDim() * (long) z));
	}

	/**
	 * Gets the linear index (z=0).
	 * 
	 * @param largeIm
	 *      Image for which the linear index must be computed
	 * @param x
	 *      horizontal index
	 * @param y
	 *      vertical index
	 * @param t
	 *      time index
	 * @param b
	 *      band index
	 * @return the linear index
	 */
	public static final long getLinearIndexXY_TB(LargeImageInterface largeIm, int x, int y, int t, int b) {
		return (long) b	+ largeIm.getLongBDim()	* ((long) x + largeIm.getLongXDim()	* ((long) y + largeIm.getLongYDim()	* ((long) t * largeIm.getLongZDim())));
	}

	/**
	 * Gets the linear index.
	 * 
	 * @param largeIm
	 *      Image for which the linear index must be computed
	 * @param x
	 *      horizontal index
	 * @param y
	 *      vertical index
	 * @param z
	 *      depth index
	 * @param t
	 *      time index
	 * @param b
	 *      band index
	 * @return the linear index
	 */
	public static final long getLinearIndexXYZTB(LargeImageInterface largeIm, int x, int y, int z, int t, int b) {
		return (long) b	+ largeIm.getLongBDim()	* ((long) x + largeIm.getLongXDim()	* ((long) y + largeIm.getLongYDim()	* ((long) z + (long) t * largeIm.getLongZDim())));
	}

	public static final double getPixelXYDouble(LargeImageInterface largeIm, int x,
			int y) {
		return largeIm.getPixelDouble(getLinearIndexXY___(largeIm, x, y));
	}

	public static final double getPixelXYZDouble(LargeImageInterface largeIm, int x,
			int y, int z) {
		return largeIm.getPixelDouble(getLinearIndexXYZ__(largeIm, x, y, z));
	}

	public static final double getPixelXYBDouble(LargeImageInterface largeIm, int x,
			int y, int b) {
		return largeIm.getPixelDouble(getLinearIndexXY__B(largeIm, x, y, b));
	}

	public static final double getPixelXYTDouble(LargeImageInterface largeIm, int x,
			int y, int t) {
		return largeIm.getPixelDouble(getLinearIndexXY_T_(largeIm, x, y, t));
	}

	public static final double getPixelXYZTDouble(LargeImageInterface largeIm, int x,
			int y, int z, int t) {
		return largeIm.getPixelDouble(getLinearIndexXYZT_(largeIm, x, y, z, t));
	}

	public static final double getPixelXYZBDouble(LargeImageInterface largeIm, int x,
			int y, int z, int b) {
		return largeIm.getPixelDouble(getLinearIndexXYZ_B(largeIm, x, y, z, b));
	}

	public static final double getPixelXYTBDouble(LargeImageInterface largeIm, int x,
			int y, int t, int b) {
		return largeIm.getPixelDouble(getLinearIndexXY_TB(largeIm, x, y, t, b));
	}

	public static final double getPixelXYZTBDouble(LargeImageInterface largeIm,
			int x, int y, int z, int t, int b) {
		return largeIm.getPixelDouble(getLinearIndexXYZTB(largeIm, x, y, z, t,
				b));
	}

	public static final void setPixelXYDouble(LargeImageInterface largeIm, int x,
			int y, double value) {
		largeIm.setPixelDouble(getLinearIndexXY___(largeIm, x, y), value);
	}

	public static final void setPixelXYZDouble(LargeImageInterface largeIm, int x,
			int y, int z, double value) {
		largeIm.setPixelDouble(getLinearIndexXYZ__(largeIm, x, y, z), value);
	}

	public static final void setPixelXYBDouble(LargeImageInterface largeIm, int x,
			int y, int b, double value) {
		largeIm.setPixelDouble(getLinearIndexXY__B(largeIm, x, y, b), value);
	}

	public static final void setPixelXYTDouble(LargeImageInterface largeIm, int x,
			int y, int t, double value) {
		largeIm.setPixelDouble(getLinearIndexXY_T_(largeIm, x, y, t), value);
	}

	public static final void setPixelXYZTDouble(LargeImageInterface largeIm, int x,
			int y, int z, int t, double value) {
		largeIm.setPixelDouble(getLinearIndexXYZT_(largeIm, x, y, z, t), value);
	}

	public static final void setPixelXYZBDouble(LargeImageInterface largeIm, int x,
			int y, int z, int b, double value) {
		largeIm.setPixelDouble(getLinearIndexXYZ_B(largeIm, x, y, z, b), value);
	}

	public static final void setPixelXYTBDouble(LargeImageInterface largeIm, int x,
			int y, int t, int b, double value) {
		largeIm.setPixelDouble(getLinearIndexXY_TB(largeIm, x, y, t, b), value);
	}

	public static final void setPixelXYZTBDouble(LargeImageInterface largeIm, int x,
			int y, int z, int t, int b, double value) {
		largeIm.setPixelDouble(getLinearIndexXYZTB(largeIm, x, y, z, t, b),
				value);
	}

	public static final int getPixelXYInt(LargeImageInterface largeIm, int x, int y) {
		return largeIm.getPixelInt(getLinearIndexXY___(largeIm, x, y));
	}

	public static final int getPixelXYZInt(LargeImageInterface largeIm, int x, int y,
			int z) {
		return largeIm.getPixelInt(getLinearIndexXYZ__(largeIm, x, y, z));
	}

	public static final int getPixelXYBInt(LargeImageInterface largeIm, int x, int y,
			int b) {
		return largeIm.getPixelInt(getLinearIndexXY__B(largeIm, x, y, b));
	}

	public static final int getPixelXYTInt(LargeImageInterface largeIm, int x, int y,
			int t) {
		return largeIm.getPixelInt(getLinearIndexXY_T_(largeIm, x, y, t));
	}

	public static final int getPixelXYZTInt(LargeImageInterface largeIm, int x,
			int y, int z, int t) {
		return largeIm.getPixelInt(getLinearIndexXYZT_(largeIm, x, y, z, t));
	}

	public static final int getPixelXYZBInt(LargeImageInterface largeIm, int x,
			int y, int z, int b) {
		return largeIm.getPixelInt(getLinearIndexXYZ_B(largeIm, x, y, z, b));
	}

	public static final int getPixelXYTBInt(LargeImageInterface largeIm, int x,
			int y, int t, int b) {
		return largeIm.getPixelInt(getLinearIndexXY_TB(largeIm, x, y, t, b));
	}

	public static final int getPixelXYZTBInt(LargeImageInterface largeIm, int x,
			int y, int z, int t, int b) {
		return largeIm.getPixelInt(getLinearIndexXYZTB(largeIm, x, y, z, t, b));
	}

	public static final void setPixelXYInt(LargeImageInterface largeIm, int x, int y,
			int value) {
		largeIm.setPixelInt(getLinearIndexXY___(largeIm, x, y), value);
	}

	public static final void setPixelXYZInt(LargeImageInterface largeIm, int x,
			int y, int z, int value) {
		largeIm.setPixelInt(getLinearIndexXYZ__(largeIm, x, y, z), value);
	}

	public static final void setPixelXYBInt(LargeImageInterface largeIm, int x,
			int y, int b, int value) {
		largeIm.setPixelInt(getLinearIndexXY__B(largeIm, x, y, b), value);
	}

	public static final void setPixelXYTInt(LargeImageInterface largeIm, int x,
			int y, int t, int value) {
		largeIm.setPixelInt(getLinearIndexXY_T_(largeIm, x, y, t), value);
	}

	public static final void setPixelXYZTInt(LargeImageInterface largeIm, int x,
			int y, int z, int t, int value) {
		largeIm.setPixelInt(getLinearIndexXYZT_(largeIm, x, y, z, t), value);
	}

	public static final void setPixelXYZBInt(LargeImageInterface largeIm, int x,
			int y, int z, int b, int value) {
		largeIm.setPixelInt(getLinearIndexXYZ_B(largeIm, x, y, z, b), value);
	}

	public static final void setPixelXYTBInt(LargeImageInterface largeIm, int x,
			int y, int t, int b, int value) {
		largeIm.setPixelInt(getLinearIndexXY_TB(largeIm, x, y, t, b), value);
	}

	public static final void setPixelXYZTBInt(LargeImageInterface largeIm, int x,
			int y, int z, int t, int b, int value) {
		largeIm.setPixelInt(getLinearIndexXYZTB(largeIm, x, y, z, t, b), value);
	}

	public static final int getPixelXYByte(LargeImageInterface largeIm, int x, int y) {
		return largeIm.getPixelByte(getLinearIndexXY___(largeIm, x, y));
	}

	public static final int getPixelXYZByte(LargeImageInterface largeIm, int x,
			int y, int z) {
		return largeIm.getPixelByte(getLinearIndexXYZ__(largeIm, x, y, z));
	}

	public static final int getPixelXYBByte(LargeImageInterface largeIm, int x,
			int y, int b) {
		return largeIm.getPixelByte(getLinearIndexXY__B(largeIm, x, y, b));
	}

	public static final int getPixelXYTByte(LargeImageInterface largeIm, int x,
			int y, int t) {
		return largeIm.getPixelByte(getLinearIndexXY_T_(largeIm, x, y, t));
	}

	public static final int getPixelXYZTByte(LargeImageInterface largeIm, int x,
			int y, int z, int t) {
		return largeIm.getPixelByte(getLinearIndexXYZT_(largeIm, x, y, z, t));
	}

	public static final int getPixelXYZBByte(LargeImageInterface largeIm, int x,
			int y, int z, int b) {
		return largeIm.getPixelByte(getLinearIndexXYZ_B(largeIm, x, y, z, b));
	}

	public static final int getPixelXYTBByte(LargeImageInterface largeIm, int x,
			int y, int t, int b) {
		return largeIm.getPixelByte(getLinearIndexXY_TB(largeIm, x, y, t, b));
	}

	public static final int getPixelXYZTBByte(LargeImageInterface largeIm, int x,
			int y, int z, int t, int b) {
		return largeIm
				.getPixelByte(getLinearIndexXYZTB(largeIm, x, y, z, t, b));
	}

	public static final void setPixelXYByte(LargeImageInterface largeIm, int x,
			int y, int value) {
		largeIm.setPixelByte(getLinearIndexXY___(largeIm, x, y), value);
	}

	public static final void setPixelXYZByte(LargeImageInterface largeIm, int x,
			int y, int z, int value) {
		largeIm.setPixelByte(getLinearIndexXYZ__(largeIm, x, y, z), value);
	}

	public static final void setPixelXYBByte(LargeImageInterface largeIm, int x,
			int y, int b, int value) {
		largeIm.setPixelByte(getLinearIndexXY__B(largeIm, x, y, b), value);
	}

	public static final void setPixelXYTByte(LargeImageInterface largeIm, int x,
			int y, int t, int value) {
		largeIm.setPixelByte(getLinearIndexXY_T_(largeIm, x, y, t), value);
	}

	public static final void setPixelXYZTByte(LargeImageInterface largeIm, int x,
			int y, int z, int t, int value) {
		largeIm.setPixelByte(getLinearIndexXYZT_(largeIm, x, y, z, t), value);
	}

	public static final void setPixelXYZBByte(LargeImageInterface largeIm, int x,
			int y, int z, int b, int value) {
		largeIm.setPixelByte(getLinearIndexXYZ_B(largeIm, x, y, z, b), value);
	}

	public static final void setPixelXYTBByte(LargeImageInterface largeIm, int x,
			int y, int t, int b, int value) {
		largeIm.setPixelByte(getLinearIndexXY_TB(largeIm, x, y, t, b), value);
	}

	public static final void setPixelXYZTBByte(LargeImageInterface largeIm, int x,
			int y, int z, int t, int b, int value) {
		largeIm
				.setPixelByte(getLinearIndexXYZTB(largeIm, x, y, z, t, b),
						value);
	}

	public static final boolean getPixelXYBoolean(LargeImageInterface largeIm, int x,
			int y) {
		return largeIm.getPixelBoolean(getLinearIndexXY___(largeIm, x, y));
	}

	public static final boolean getPixelXYZBoolean(LargeImageInterface largeIm,
			int x, int y, int z) {
		return largeIm.getPixelBoolean(getLinearIndexXYZ__(largeIm, x, y, z));
	}

	public static final boolean getPixelXYBBoolean(LargeImageInterface largeIm,
			int x, int y, int b) {
		return largeIm.getPixelBoolean(getLinearIndexXY__B(largeIm, x, y, b));
	}

	public static final boolean getPixelXYTBoolean(LargeImageInterface largeIm,
			int x, int y, int t) {
		return largeIm.getPixelBoolean(getLinearIndexXY_T_(largeIm, x, y, t));
	}

	public static final boolean getPixelXYZTBoolean(LargeImageInterface largeIm,
			int x, int y, int z, int t) {
		return largeIm
				.getPixelBoolean(getLinearIndexXYZT_(largeIm, x, y, z, t));
	}

	public static final boolean getPixelXYZBBoolean(LargeImageInterface largeIm,
			int x, int y, int z, int b) {
		return largeIm
				.getPixelBoolean(getLinearIndexXYZ_B(largeIm, x, y, z, b));
	}

	public static final boolean getPixelXYTBBoolean(LargeImageInterface largeIm,
			int x, int y, int t, int b) {
		return largeIm
				.getPixelBoolean(getLinearIndexXY_TB(largeIm, x, y, t, b));
	}

	public static final boolean getPixelXYZTBBoolean(LargeImageInterface largeIm,
			int x, int y, int z, int t, int b) {
		return largeIm.getPixelBoolean(getLinearIndexXYZTB(largeIm, x, y, z, t,
				b));
	}

	public static final void setPixelXYBoolean(LargeImageInterface largeIm, int x,
			int y, boolean value) {
		largeIm.setPixelBoolean(getLinearIndexXY___(largeIm, x, y), value);
	}

	public static final void setPixelXYZBoolean(LargeImageInterface largeIm, int x,
			int y, int z, boolean value) {
		largeIm.setPixelBoolean(getLinearIndexXYZ__(largeIm, x, y, z), value);
	}

	public static final void setPixelXYBBoolean(LargeImageInterface largeIm, int x,
			int y, int b, boolean value) {
		largeIm.setPixelBoolean(getLinearIndexXY__B(largeIm, x, y, b), value);
	}

	public static final void setPixelXYTBoolean(LargeImageInterface largeIm, int x,
			int y, int t, boolean value) {
		largeIm.setPixelBoolean(getLinearIndexXY_T_(largeIm, x, y, t), value);
	}

	public static final void setPixelXYZTBoolean(LargeImageInterface largeIm, int x,
			int y, int z, int t, boolean value) {
		largeIm
				.setPixelBoolean(getLinearIndexXYZT_(largeIm, x, y, z, t),
						value);
	}

	public static final void setPixelXYZBBoolean(LargeImageInterface largeIm, int x,
			int y, int z, int b, boolean value) {
		largeIm
				.setPixelBoolean(getLinearIndexXYZ_B(largeIm, x, y, z, b),
						value);
	}

	public static final void setPixelXYTBBoolean(LargeImageInterface largeIm, int x,
			int y, int t, int b, boolean value) {
		largeIm
				.setPixelBoolean(getLinearIndexXY_TB(largeIm, x, y, t, b),
						value);
	}

	public static final void setPixelXYZTBBoolean(LargeImageInterface largeIm, int x,
			int y, int z, int t, int b, boolean value) {
		largeIm.setPixelBoolean(getLinearIndexXYZTB(largeIm, x, y, z, t, b),
				value);
	}

	/*
	 * Mask management**************************************************
	 */

	public static final boolean isInMask(LargeImageInterface largeIm, int loc) {
		return largeIm.getPixelBoolean(loc);
	}

	public static final boolean isInMask(LargeImageInterface largeIm, long loc) {
		return largeIm.getPixelBoolean(loc);
	}

	public static final boolean isPresent(LargeImageInterface largeIm, int loc) {
		return ((Image) largeIm).mask.isInMask(loc);
	}

	public static final boolean isPresent(LargeImageInterface largeIm, long loc) {
		return ((Image) largeIm).mask.isInMask(loc);
	}

	public static final int getNumberOfPresentPixel(LargeImageInterface largeIm) {
		throw new PelicanException(
				"Large Image does not support getNumberOfPresentPixel since it returns an int");
	}

	public static final int getNumberOfPresentPixel(LargeImageInterface largeIm,
			int band) {
		throw new PelicanException(
				"Large Image does not support getNumberOfPresentPixel since it returns an int");
	}
	
	/*
	 * Constructors *****************************************************
	 */

	public static LargeBooleanImage newBooleanImage(LargeImageInterface largeIm,boolean copyData){
		return new LargeBooleanImage((Image)largeIm,copyData);
	}
	
	public static LargeBooleanImage newBooleanImage(int x, int y, int z, int t, int b){
		return new LargeBooleanImage(x,y,z,t,b);
	}
	
	public static LargeByteImage newByteImage(LargeImageInterface largeIm,boolean copyData){
		return new LargeByteImage((Image)largeIm,copyData);
	}
	
	public static LargeByteImage newByteImage(int x, int y, int z, int t, int b){
		return new LargeByteImage(x,y,z,t,b);
	}

	public static LargeIntegerImage newIntegerImage(LargeImageInterface largeIm,boolean copyData){
		return new LargeIntegerImage((Image)largeIm,copyData);
	}
	
	public static LargeIntegerImage newIntegerImage(int x, int y, int z, int t, int b){
		return new LargeIntegerImage(x,y,z,t,b);
	}

	public static LargeDoubleImage newDoubleImage(LargeImageInterface largeIm,boolean copyData){
		return new LargeDoubleImage((Image)largeIm,copyData);
	}

	public static LargeDoubleImage newDoubleImage(int x, int y, int z, int t, int b){
		return new LargeDoubleImage(x,y,z,t,b);
	}
	
}
