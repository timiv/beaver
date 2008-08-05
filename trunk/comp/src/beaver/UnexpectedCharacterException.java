package beaver;

/**
 * Exception that is used by a Scanner to signal that an unexpected character is encountered.
 * This exception is only useful if an application uses Scanner directly (without a Parser),
 * as a Parser intercepts it, converts to a callback and continues calling a scanner expecting
 * to eventually reach recognizable tokens.
 * 
 * @author Alexander Demenchuk
 */
public class UnexpectedCharacterException extends Exception
{
	private static final long serialVersionUID = 3026616579354239715L;

	public UnexpectedCharacterException() { super(); }
}