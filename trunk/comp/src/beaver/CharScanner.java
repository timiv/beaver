package beaver;

import java.io.IOException;
import java.io.Reader;

/**
 * A Scanner that scans character streams
 */
public abstract class CharScanner extends Scanner
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
		this.text = new char[(maxTokenLength * 2 | 0xfff) + 1];
		this.line = 1;
	}

	protected CharScanner(Reader in)
	{
		this.in = in;
		this.text = new char[0x1000];
		this.line = 1;
	}

	/**
	 * Returns characters of the current, just recognized, token.
	 * 
	 * @return text of the recognized token
	 */
	public Object getTokenText()
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
	
	/**
	 * When a new line sequence is recognized a scanner advances the current line
	 * number by calling "newLine" as an event.
	 * Note, the line number and offset are changed before the new-line token is returned,
	 * if it is returned, thus it'll look as if the new-line token position is at the beginning
	 * of the next line.
	 * Let's consider this a feature ;-) as this approach allows tracking a position
	 * in the source with the least amount of overhead.
	 */
	protected void newLine()
	{
		line++;
		offset = cursor;
	}
}
