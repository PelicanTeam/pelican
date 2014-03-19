/**
 * 
 */
package fr.unistra.pelican.util.iterator;

import fr.unistra.pelican.Image;

/**
 * @author Benjamin Perret
 *
 */
public class ImageIteratorXY extends ImageIterator {


	public void reInit( int z, int t, int b )
	{
		this.xOffset=0;
		this.yOffset=0;
		this.bOffset=b;
		this.tOffset=t;
		this.zOffset=z;	
		hasNext=true;
	}
	
	/**
	 * Same as ImageIteratorXY(Image image,0,0,0)
	 * @param image
	 */
	public ImageIteratorXY(Image image) {
		this(image,0,0,0);
	}
	
	/**
	 * Iterate over XY plane at zdim=z, tdim=t and bdim=b
	 * @param image
	 * @param z
	 * @param t
	 * @param b
	 */
	public ImageIteratorXY(Image image, int z, int t, int b) {
		super(image);
		this.bOffset=b;
		this.tOffset=t;
		this.zOffset=z;		
	}
	
	protected void forward() {
		this.xOffset++;
		if (this.xOffset == this.xdim) {

			this.xOffset = 0;
			this.yOffset++;
			if (this.yOffset == this.ydim) {
				hasNext = false;
			}
		}
	}

}
