package beaver;

/**
 * Exception that parsers throw when they cannot recover from a syntax error.
 */
public class SyntaxErrorException extends Exception
{
	SyntaxErrorException()
	{
		super("unexpected token");
	}
	
	SyntaxErrorException(String msg)
	{
		super(msg);
	}
	
	private static final long serialVersionUID = 2431065657267491348L;
}
