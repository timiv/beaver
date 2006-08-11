package beaver;

/**
 * Exception that parser throws when it cannot recover from the syntax error.
 * 
 * @author Alexander Demenchuk
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
