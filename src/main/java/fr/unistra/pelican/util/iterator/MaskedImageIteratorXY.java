/**
 * 
 */
package fr.unistra.pelican.util.iterator;

import fr.unistra.pelican.Image;

/**
 * @author Benjamin Perret
 *
 */
public class MaskedImageIteratorXY extends ImageIteratorXY {

	public MaskedImageIteratorXY(Image image) {
		this(image, 0, 0, 0);
	}
	
	public MaskedImageIteratorXY(Image image, int z, int t, int b) {
		super(image, z, t, b);
	}

	/**	Returns <tt>true</tt> if the iteration has more elements. ( In other words, returns 
	 *	<tt>true</tt> if next would return an element rather than throwing an exception. )
	 *	@return <tt>true</tt> if the iterator has more elements.
	 */
	public boolean hasNext() { 
//System.out.println(this.xOffset +" " +this.yOffset+" " +this.zOffset+" " +this.tOffset+" " + this.bOffset );
		while (hasNext && !image.isPresentXYZTB(this.xOffset,this.yOffset,this.zOffset,this.tOffset, this.bOffset ))
		{
			//System.out.println(this.xOffset +" " +this.yOffset+" " +this.zOffset+" " +this.tOffset+" " + this.bOffset );
			forward();
		}
		
		
		return hasNext;
	}
	
}
