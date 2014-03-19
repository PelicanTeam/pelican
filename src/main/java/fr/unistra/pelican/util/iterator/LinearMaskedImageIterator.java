package fr.unistra.pelican.util.iterator;

import fr.unistra.pelican.Image;



/**	
 *	Allows smooth linear iteration on a masked image.
 *	@author Régis Witz
 */
public class LinearMaskedImageIterator extends LinearImageIterator { 

	public LinearMaskedImageIterator( Image image ) { 

		super( image );
	}

	/**	Returns <tt>true</tt> if the iteration has more elements. ( In other words, returns 
	 *	<tt>true</tt> if next would return an element rather than throwing an exception. )
	 *	@return <tt>true</tt> if the iterator has more elements.
	 */
	public boolean hasNext() { 

		boolean present = this.image.isPresent( this.offset );
		boolean okay = this.offset < this.image.size();
		if ( okay && !present ) { 
			this.offset++;
			okay = this.hasNext();
		}
		return okay;
	}

	/**	Returns the next element in the iteration. Calling this method repeatedly until the 
	 *	{@link #hasNext()} method returns <tt>false</tt> will return each element in the underlying 
	 *	collection exactly once.
	 *
	 *	@return Next element in the iteration.
	 */
	public Integer next() { 

		Integer next = new Integer( this.offset );
		this.offset++;
		return next;
	}
}
