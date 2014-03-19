package fr.unistra.pelican.util.iterator;

import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.Pixel;



/**	
 *	Allows smooth X,Y,Z,T,B iteration on a masked image.
 *	@author Régis Witz
 */
public class MaskedImageIterator extends ImageIterator { 

	public MaskedImageIterator( Image image ) { 

		super( image );
	}

	/**	Returns <tt>true</tt> if the iteration has more elements. ( In other words, returns 
	 *	<tt>true</tt> if next would return an element rather than throwing an exception. )
	 *	@return <tt>true</tt> if the iterator has more elements.
	 */
	public boolean hasNext() { 

		while (hasNext && !image.isPresentXYZTB(this.xOffset,this.yOffset,this.zOffset,this.tOffset, this.bOffset ))
		{
			forward();
		}
		
		/*boolean present = 
			this.image.isPresentXYZTB(	this.xOffset, 
										this.yOffset, 
										this.zOffset, 
										this.tOffset, 
										this.bOffset );
		boolean okay = this.getIndex() < this.image.size();
		if ( okay && !present ) { 
			this.forward();
			okay = this.hasNext();
		}*/
		return hasNext;
	}

//	/**	Returns the next element in the iteration. Calling this method repeatedly until the 
//	 *	{@link #hasNext()} method returns <tt>false</tt> will return each element in the underlying 
//	 *	collection exactly once.
//	 *	<p>
//	 *	<b>IMPORTANT NOTE :</b> Successive calls to this method always return a reference to the 
//	 *	<i>same</i> object. This should be of no consequence because in foreach loops, one reuses 
//	 *	always the same object. Remember, tough, if you want to keep up the {@link Pixel} 
//	 *	wich this method enumerate, you should use the {@link #clone} method before reinvoking this 
//	 *	method.
//	 *
//	 *	@return Next element in the iteration.
//	 */
//	public Pixel next() { 
//
////		boolean present = 
////			this.image.isPresentXYZTB(	this.xOffset, 
////										this.yOffset, 
////										this.zOffset, 
////										this.tOffset, 
////										this.bOffset );
////		if ( present ) { 
//
//			this.next.x = this.xOffset;
//			this.next.y = this.yOffset;
//			this.next.z = this.zOffset;
//			this.next.t = this.tOffset;
//			this.next.b = this.bOffset;
//
////		} else { 
////
////			this.forward();
////			this.next();
////		}
//		this.forward();
//		return next;
//	}
}
