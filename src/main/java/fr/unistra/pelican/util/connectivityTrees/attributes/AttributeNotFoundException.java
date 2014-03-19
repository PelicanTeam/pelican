/**
 * 
 */
package fr.unistra.pelican.util.connectivityTrees.attributes;

import fr.unistra.pelican.PelicanException;


/**
 * 
 * User is asking for an attribute unfoundable.
 * @author Benjamin Perret
 *
 */
public class AttributeNotFoundException extends PelicanException {

	public AttributeNotFoundException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public AttributeNotFoundException(String s, Throwable e) {
		super(s, e);
		// TODO Auto-generated constructor stub
	}

	public AttributeNotFoundException(String s) {
		super(s);
		// TODO Auto-generated constructor stub
	}

	public AttributeNotFoundException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
