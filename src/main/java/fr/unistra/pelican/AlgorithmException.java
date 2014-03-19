package fr.unistra.pelican;

/**
 * This class represents the general exception thrown by algorithms in cases of unexpected faults.
 *
 */
public class AlgorithmException extends PelicanException
{
	/**
	 * 
	 */
	public AlgorithmException()
	{
		super();
	}

	/**
	 * @param s
	 */
	public AlgorithmException(String s)
	{
		super(s);
	}

	/**
	 * @param s
	 * @param e
	 */
	public AlgorithmException(String s, Throwable e) {
		super(s, e);
		
	}
	
	
}
