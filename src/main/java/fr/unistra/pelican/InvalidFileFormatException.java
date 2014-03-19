package fr.unistra.pelican;

/**
 * This class represents the exception thrown in cases in invalid input files.
 * 
 *
 */
public class InvalidFileFormatException extends PelicanException
{
	/**
	 * 
	 *
	 */
	public InvalidFileFormatException()
	{
		super();
	}

	/**
	 * 
	 * @param s
	 */
	public InvalidFileFormatException(String s)
	{
		super(s);
	}
}
