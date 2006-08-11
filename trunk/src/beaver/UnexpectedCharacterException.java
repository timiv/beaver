package beaver;

/**
 * Exception that is used by a Scanner to signal abount an encounter of unexpected characters.
 * This exception is only meaningful if an application uses Scanner directly (without a Parser),
 * as Parsers intercept it, convert to a callback and continue calling parser hoping to get eventually
 * to recognizable tokens.
 * 
 * @author Alexander Demenchuk
 */
public class UnexpectedCharacterException extends Exception
{
	private static final long serialVersionUID = 3026616579354239715L;

	public UnexpectedCharacterException() { super(); }
}