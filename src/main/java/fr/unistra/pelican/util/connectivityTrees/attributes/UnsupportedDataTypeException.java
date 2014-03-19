/**
 * 
 */
package fr.unistra.pelican.util.connectivityTrees.attributes;

import fr.unistra.pelican.PelicanException;

/**
 * This exception is launched when trying to add an attribute to a tree whith an unsupported data type
 * 
 * @author Benjamin Perret
 *
 */
public class UnsupportedDataTypeException extends PelicanException {

	/**
	 * 
	 */
	public UnsupportedDataTypeException() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public UnsupportedDataTypeException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public UnsupportedDataTypeException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public UnsupportedDataTypeException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
