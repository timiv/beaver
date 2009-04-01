package beaver;

import java.io.IOException;

/**
 * A contract between Scanners and Parsers   
 */
public abstract class Scanner
{
	/**
	 * Scanner scans the input trying to match it to one of the known tokens.
	 * 
	 * @return ID of the next token found in the input
	 * @throws UnexpectedCharacterException when input can be matched to any known token
	 * @throws IOException when input cannot be read at all
	 */
	public abstract int getNextToken() throws UnexpectedCharacterException, IOException;
	
	/**
	 * After a token is recognized the matched input can be retrieved as it's (token's)
	 * "text".
	 * 
	 * @return Content (characters) of the last matched token. It will be a String or a
	 *         array or bytes depending on a scanner type. 
	 */
	public abstract Object getTokenText();
	
	/**
	 * Line number where the last token has been found. Line numbers are incremented
	 * by a generated scanner if specification makes special provisions for this.
	 * Otherwise line number is always returned as 1, i.e. the input is treated as
	 * one long line.
	 * 
	 * @return line number where the last token has been found
	 */
	public abstract int getTokenLine();
	
	/**
	 * Position of the first "character" of the last token on the line. Character scanners
	 * assume that the first column on a line has number 1, binary scanners use 0.
	 * 
	 * @return position of the first "character" of the last token on the current line.
	 */
	public abstract int getTokenColumn();
}
