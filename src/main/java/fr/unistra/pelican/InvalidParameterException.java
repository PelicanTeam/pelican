package fr.unistra.pelican;

/**
 * This class represents the exception thrown in cases in parameters have wrong values.
 * 
 *
 */
public class InvalidParameterException extends PelicanException
{
	public InvalidParameterException()
	{
		super();
	}

	public InvalidParameterException(String s)
	{
		super(s);
	}

	public InvalidParameterException(String s, Exception e)
	{
		super(s, e);
	}
}