/***
 * Beaver: compiler front-end construction toolkit                       
 * Copyright (c) 2003-2007 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */
package beaver;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;

public abstract class CharScanner extends Scanner
{
	/**
	 * Buffer for input symbols. 
	 */
	protected char[] text;
	
	/**
	 * Index of the end of input symbols.  
	 */
	protected int limit;
	
	/**
	 * Index of the first symbol in the current token.
	 */
	protected int start;
	
	/**
	 * Index of the symbol recognizer is looking at.
	 */
	protected int cursor;
	
	/**
	 * Index of the symbol to which the recognizer will backtrack if necessary.
	 */
	protected int marker;

	/**
	 * Index of the fist symbol of the trailing context. Recognizer uses it to reset 
	 * the cursor after the trailing context is matched. 
	 */
	protected int ctxptr;

	/**
	 * "Index" of the first character of the current line.
	 * The character itself might be shifted out of the buffer. In this case the
	 * offset will be negative.
	 * This fields is used to derive a position on the line where the recognized
	 * token starts.
	 */
	protected int offset;
	
	/**
	 * A source line number;
	 */
	protected int lineNum;
	
	/**
	 * A "source" of symbols. 
	 */
	private Reader input;
	
	protected CharScanner(Reader inputReader, int minTextLen)
	{
		input = inputReader;
		text = new char[(minTextLen * 2 | 0xFF) + 1];
		lineNum = 1;
	}
	
	/**
	 * This method will be generated.
	 * 
	 * @return an ID of the next token from the text.
	 * @throws UnexpectedCharacterException - if an unexpected character is encountered
	 */
	public abstract int next() throws UnexpectedCharacterException, IOException;
	
	/**
	 * Returns characters of the just recognized token, which ID was returned by next().
	 * 
	 * @return text of the recognized token
	 */
	public String getValue()
	{
		return new String(text, start, cursor - start);
	}
	
	/**
	 * Returns number of characters in the just recognized token, which ID was returned by next().
	 * 
	 * @return length of the recognized token
	 */
	public int getLength()
	{
		return cursor - start;
	}
	
	/**
	 * Returns a line number of the current token.

	 * @return line number where the last token was found
	 */
	public int getLine()
	{
		return lineNum;
	}
	
	/**
	 * Returns a column number of the token's first symbol.
	 * 
	 * @return position of the first symbol of the recognized token
	 */
	public int getColumn()
	{
		return start - offset + 1;
	}
	
	/** 
	 * Scanner calls this method when the text buffer needs refilling.
	 * 
	 * @param n minimum number of characters that needs to be put into the text buffer
	 * @param cursor active cursor (internal next() variable)
	 * 
	 * @return adjusted active cursor
	 */
	protected int fill(int n, int cursor) throws IOException
	{
		int nChars = limit - start;
		if (nChars > 0)
		{
			System.arraycopy(text, start, text, 0, nChars);
			limit  -= start;
			cursor -= start;
			marker -= start;
			ctxptr -= start;
			offset -= start;
			start = 0;
		}
		nChars = input.read(text, limit, text.length - limit);
		if (nChars > 0)
		{
			limit += nChars;
		}
		else
		{
			nChars = 0;
		}
		if (nChars < n)
		{
			n -= nChars;
			Arrays.fill(text, limit, limit + n, EOF_SENTINEL);
			limit += n;
		}
		return cursor;
	}

	public static final char EOF_SENTINEL = '\ufffe';
}
