package beaver;

import java.io.IOException;
import java.io.Reader;

/**
 * 
 * @author Alexander Demenchuk
 * 
 */
public abstract class CharScanner
{
	/**
	 * Input buffer
	 */
	protected char[] text;

	/**
	 * Index of the end of source characters in the text buffer. It points just beyond the last
	 * character that was read from the input.
	 */
	protected int    limit;

	/**
	 * Index of the first character of the recognized token.
	 */
	protected int    start;

	/**
	 * Index of the character recognizer is looking at. This character begins the next token, which
	 * the recognizer will scan next.
	 */
	protected int    cursor;

	/**
	 * "Index" of the first character on the current line. The character itself might be shifted out
	 * of the buffer. In this case the offset will be negative. This fields is used to derive a
	 * position on the line where the recognized token starts.
	 */
	private int      offset;

	/**
	 * A source line number;
	 */
	private int      line;

	/**
	 * The "source" of characters.
	 */
	private Reader   in;

	protected CharScanner(Reader in, int maxTokenLength)
	{
		this.in = in;
		this.text = new char[(maxTokenLength * 2 | 0xff) + 1];
		this.line = 1;
	}

	/**
	 * This method will be generated.
	 * 
	 * @return an ID of the next recognized token.
	 * @throws UnexpectedCharacterException -
	 *             if an unexpected character is encountered
	 */
	public abstract int getNextToken() throws UnexpectedCharacterException, IOException;

	/**
	 * Returns characters of the current, just recognized, token.
	 * 
	 * @return text of the recognized token
	 */
	public String getTokenText()
	{
		return new String(text, start, cursor - start);
	}

	/**
	 * Returns number of characters in the current, just recognized, token.
	 * 
	 * @return length of the recognized token
	 */
	public int getTokenLength()
	{
		return cursor - start;
	}

	/**
	 * Returns a line number of the current token.
	 * 
	 * @return line number where the last token was found
	 */
	public int getTokenLine()
	{
		return line;
	}

	/**
	 * Returns a column number of the token's first symbol.
	 * 
	 * @return position of the first symbol of the recognized token
	 */
	public int getTokenColumn()
	{
		return start - offset + 1;
	}

	/**
	 * Scanner calls this method when the text buffer needs refilling.
	 * 
	 * @return number of characters shifted out of the text buffer, this value will be used to
	 *         adjust getNextToken() local text buffer indexes.
	 */
	protected int fill() throws IOException
	{
		int shift = start;
		int numChars = limit - start;
		if (numChars > 0)
		{
			System.arraycopy(text, start, text, 0, numChars);

			limit -= shift;
			offset -= shift;
		}
		else
		{
			limit = offset = 0;
		}
		start = 0;

		numChars = in.read(text, limit, text.length - limit);
		if (numChars > 0)
		{
			limit += numChars;
		}
		if (limit < text.length)
		{
			// "terminate" the text with an invalid Unicode character
			text[limit] = '\uffff';
		}
		return shift;
	}
}
