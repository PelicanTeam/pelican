package fr.unistra.pelican;

/**
 * This class represents the overall exception used by the PELICAN framework and is subclassed
 * by all other exceptions in use.
 * 
 *
 */
public class PelicanException extends RuntimeException
{	
	/**
	 * 
	 *
	 */
	public PelicanException()
	{
		super();
	}

	/**
	 * 
	 * @param s
	 */
	public PelicanException(String s)
	{
		super(s);
	}
	
	/**
	 * 
	 * @param s
	 */
	public PelicanException(String s, Throwable e)
	{
		super(s, e);
	}

	public PelicanException(Throwable cause) {
		super(cause);
	}
}
