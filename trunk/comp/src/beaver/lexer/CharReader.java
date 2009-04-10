package beaver.lexer;

public final class CharReader
{
	private char[] chars;
	private int    offset;
	private int    end;
	
	public CharReader(char[] chars, int offset, int length)
	{
		this.chars  = chars;
		this.offset = offset;
		this.end    = offset + length;
	}
	
	public CharReader(String str)
	{
		this.chars = str.toCharArray();
		this.end = chars.length;
	}
	
	/**
	 * Removes specified number of characters from both ends of the substring
	 * 
	 * @param n number of characters to remove
	 */
	public void trim(int n)
	{
		offset += n;
		end -= n;
	}
	
	/**
	 * Trims 1 character from both ends of the substring.
	 * This is an "optimized" version of the general case, and this is what 
	 * typically is used.
	 */
	public void trim1()
	{
		offset += 1;
		end -= 1;
	}
	
	/**
	 * Probes whether a substring is empty.
	 * 
	 * @return true if a substring references empty set of characters
	 */
	public boolean isEmpty()
	{
		return offset == end;
	}
	
	/**
	 * Reads next character of the substring and returns its numeric value if the character
	 * can represent a digit in the specified radix. 
	 * 
	 * @param radix the radix
	 * @return the numeric value represented by the next character in the specified radix.
	 */
	public int readDigit(int radix)
	{
		int d;

		if ( offset == end )
			throw new IllegalStateException("empty");
		
		d = Character.digit(readNextChar(), radix);
		if ( d < 0 )
			throw new IllegalStateException("Illegal digit");
		
		return d;
	}
	
	/**
	 * Returns next, possibly unescaped first, character.
	 * 
	 * @return next character
	 */
	public int readChar()
	{
		int c = readNextChar();
		
		if ( c == '\\' && offset < end )
		{
			switch ( c = readNextChar() )
			{
				case 'b': return '\b';
				case 'f': return '\f';
				case 'n': return '\n';
				case 'r': return '\r';
				case 't': return '\t';
				case 'x':
				{
					return readDigit(16) << 4 | readDigit(16);
				}
				case 'u':
				{
					c = readDigit(16) << 12 | readDigit(16) << 8 | readDigit(16) << 4 | readDigit(16);
					if (c == 0xffff)
					{
						throw new IllegalStateException("invalid Unicode decoded");
					}
					break;
				}
			}
		}
		return c;
	}
	
	/**
	 * Reads substring's first character, returns it and trims the substring so
	 * that its second character becomes first
	 * 
	 * @return a first character of a substring 
	 */
	private int readNextChar()
	{
		if ( offset == end )
			return -1;
			
		return chars[offset++];
	}
	
	/**
	 * Returns offset 
	 */
	public int mark()
	{
		return offset;
	}
	
	/**
	 * Resets current offset back to the marked position
	 * 
	 * @param mark
	 */
	public void reset(int mark)
	{
		if (mark < 0 || mark > offset)
			throw new IllegalArgumentException("out of range");
		offset = mark;
	}
	
	public static String escape(char c)
	{
		switch (c)
		{
			case '\b':	return "\\b";
			case '\f':	return "\\f";
			case '\n':	return "\\n";
			case '\r':	return "\\r";
			case '\t':	return "\\t";
		}
		if (c <= 0xf)
			return "\\x0" + Integer.toHexString(c);
		if (c <= ' ')
			return "\\x" + Integer.toHexString(c);
		if (c <= 0x7f)
			return String.valueOf(c);
		if (c <= 0xff)
			return "\\x" + Integer.toHexString(c);
		if (c <= 0xfff)
			return "\\u0" + Integer.toHexString(c);
		return "\\u" + Integer.toHexString(c);
	}
}