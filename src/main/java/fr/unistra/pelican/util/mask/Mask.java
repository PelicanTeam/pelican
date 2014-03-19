package fr.unistra.pelican.util.mask;


/**
 * Interface for a mask of missing pixels
 * 
 * 
 * @author Benjamin Perret
 *
 */
public interface Mask {

	
	
	/**
	 * Test if pixel is present in the given location
	 * @return presence
	 */
	public boolean isInMask(int loc);
	
	/**
	 * Test if pixel is present in the given location
	 * @return presence
	 */
	public boolean isInMask(long loc);
	
	
	
	/**
	 * Test if pixel is present in the given location
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
	 * @return presence
	 */
	public boolean isInMask(int x, int y, int z, int t, int b);

	/**
	 * Test if pixel is present in the given location
	 * 
	 * @param x
	 *          horizontal position of the desired pixel
	 * @param y
	 *          vertical position of the desired pixel
	 * @return presence
	 */
	public boolean isInMaskXY(int x, int y);

	/**
	 * Test if pixel is present in the given location
	 * 
	 * @param x
	 *          horizontal position of the desired pixel
	 * @param y
	 *          vertical position of the desired pixel
	 * @param z
	 *          depth position of the desired pixel
	 * @return presence
	 */
	public boolean isInMaskXYZ(int x, int y, int z) ;

	/**
	 * Test if pixel is present in the given location
	 * 
	 * @param x
	 *          horizontal position of the desired pixel
	 * @param y
	 *          vertical position of the desired pixel
	 * @param b
	 *          channel number of the desired pixel
	 * @return presence
	 */
	public boolean isInMaskXYB(int x, int y, int b) ;

	/**
	 * Test if pixel is present in the given location
	 * 
	 * @param x
	 *          horizontal position of the desired pixel
	 * @param y
	 *          vertical position of the desired pixel
	 * @param t
	 *          time position of the desired pixel
	 * @return presence
	 */
	public boolean isInMaskXYT(int x, int y, int t);

	/**
	 * Test if pixel is present in the given location
	 * 
	 * @param x
	 *          horizontal position of the desired pixel
	 * @param y
	 *          vertical position of the desired pixel
	 * @param z
	 *          depth position of the desired pixel
	 * @param t
	 *          time position of the desired pixel
	 * @return presence
	 */
	public boolean isInMaskXYZT(int x, int y, int z, int t);

	/**
	 * Test if pixel is present in the given location
	 * 
	 * @param x
	 *          horizontal position of the desired pixel
	 * @param y
	 *          vertical position of the desired pixel
	 * @param z
	 *          depth position of the desired pixel
	 * @param b
	 *          channel number of the desired pixel
	 * @return presence
	 */
	public boolean isInMaskXYZB(int x, int y, int z, int b) ;

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
	 * @return presence
	 */
	public boolean isInMaskXYTB(int x, int y, int t, int b);

	/**
	 * Test if pixel is present in the given location
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
	 * @return presence
	 */
	public boolean isInMaskXYZTB(int x, int y, int z, int t, int b) ;

	
	/**
	 * Return a copy of the Mask
	 * @return copy
	 */
	public Mask cloneMask();

	
}
