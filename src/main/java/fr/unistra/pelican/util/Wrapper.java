package fr.unistra.pelican.util;

/**
 * Generic wrapping class for any type of object
 * Useful to wrap Integer, Double, ... objects whose value can't be modified
 * @author Benjamin Perret
 *
 * @param <T> Type of wrapped object
 */
public class Wrapper<T> {
	
	/**
	 * The object
	 */
	private T value;
	
	/**
	 * Default constructor, object value is set to null
	 */
	public Wrapper()
	{
		this.value=null;
	}
	
	/**
	 * Wrap object value
	 * @param value
	 */
	public Wrapper(T value)
	{
		this.value=value;
	}
	
	/**
	 * Create copy of wrapper, warning wrapped object is not copied, new wrapper will only reference the same object:
	 * @param iw
	 */
	public Wrapper(Wrapper<T> iw)
	{
		this.value=iw.value;
	}

	/**
	 * Get wrapped object
	 * @return
	 */
	public T getValue() {
		return value;
	}

	/**
	 * Modify wrapped object
	 * @param value
	 */
	public void setValue(T value) {
		this.value = value;
	}
	
	public String toString(){
		return value.toString();
	}
	
}
