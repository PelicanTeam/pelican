package fr.unistra.pelican.util.iterator;

import java.util.Iterator;

import org.w3c.dom.views.AbstractView;

import fr.unistra.pelican.Image;



/**	
 *	Allows smooth linear iteration on an unmasked image.
 *	@author Régis Witz
 */
public class LinearImageIterator extends AbstractImageIterator<Integer> { 

	/**	Image on wich iteration is done. */
	protected Image image;
	/**	Offset of the iteration. */
	protected int offset;



	public LinearImageIterator( Image image ) { 

		this.image = image;
		this.offset = 0;
	}

	/**	Returns <tt>true</tt> if the iteration has more elements. ( In other words, returns 
	 *	<tt>true</tt> if next would return an element rather than throwing an exception. )
	 *	@return <tt>true</tt> if the iterator has more elements.
	 */
	public boolean hasNext() { 

		return this.offset < this.image.size();
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

	/**	Unsupported. */
	public void remove() {} 

}
