/**
 * 
 */
package fr.unistra.pelican.util;

/**
 * Interface for the disposable design pattern.
 * <p>
 * When the dispose function is called on a disposable object, it must freed all resources it owns, i.e.
 * - calling dispose on disposable references it owns
 * - closing streams
 * - nulling references
 * <p>
 * The contract is that once the object is disposed it must not be used except from the dispose method that can be called several times.
 * Calling a method or using a field of a disposed object must be considered as a programming error.
 * 
 * @author Benjamin Perret
 *
 */
public interface Disposable {

	/**
	 * Dispose all resources owned by the object.
	 * After this method is called, no other method (except the dispose method) is intended to work properly on the given object.
	 */
	public void dispose();
}
