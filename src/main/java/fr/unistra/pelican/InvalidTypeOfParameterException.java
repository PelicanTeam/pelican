package fr.unistra.pelican;

/**
 * This class represents the exception thrown by algorithms in cases where the received argument
 * is not of the required type.
 *
 */
public class InvalidTypeOfParameterException extends PelicanException
{
	public InvalidTypeOfParameterException()
	{
		super();
	}

	public InvalidTypeOfParameterException(String s)
	{
		super(s);
	}
}
