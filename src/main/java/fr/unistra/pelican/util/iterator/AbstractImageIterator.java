package fr.unistra.pelican.util.iterator;

import java.util.Iterator;

import fr.unistra.pelican.util.Pixel;

/**
 * All iterators over images should extend this class to ensure common properties of the 2 interfaces.
 * 
 * @author Benjamin Perret
 *
 */
public  abstract  class  AbstractImageIterator <T> implements Iterator<T>, Iterable<T> {

	
	
	@Override
	public Iterator<T> iterator() {
		return this; // why -- because!
	}

}
