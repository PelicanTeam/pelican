package fr.unistra.pelican;

/**
 * This class represents the exception thrown by algorithms in cases where the parameter number
 * does not equal the expected quantity
 * 
 *
 */
public class InvalidNumberOfParametersException extends PelicanException
{
	/**
	 * 
	 *
	 */
	public InvalidNumberOfParametersException()
	{
		super();
	}

	/**
	 * 
	 * @param s
	 */
	public InvalidNumberOfParametersException(String s)
	{
		super(s);
	}
}
