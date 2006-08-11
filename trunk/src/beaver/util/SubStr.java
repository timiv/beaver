package beaver.util;

import beaver.CharScanner;

public class SubStr
{
	char[] chars;
	int    offset;
	int    length;
	
	public SubStr(char[] chars, int offset, int length)
	{
		this.chars  = chars;
		this.offset = offset;
		this.length = length;
	}
	
	public SubStr(String str)
	{
		this.chars = str.toCharArray();
		this.length = chars.length;
	}
	
	/**
	 * Removes specified number of characters from both ends of the substring
	 * 
	 * @param n number of characters to remove
	 */
	public void trim(int n)
	{
		offset += n;
		length -= n * 2;
	}
	
	/**
	 * Trims 1 character from both ends of the substring.
	 * This is an "optimized" version of the general case, and this is what 
	 * typically is used.
	 */
	public void trim1()
	{
		offset += 1;
		length -= 2;
	}
	
	/**
	 * Probes whether a substring is empty.
	 * 
	 * @return true if a substring references empty set of characters
	 */
	public boolean isEmpty()
	{
		return length == 0;
	}
	
	/**
	 * Reads substring's first character, returns it and trims the substring so
	 * that its second character becomes first
	 * 
	 * @return a first character of a substring 
	 */
	public char readChar()
	{
		if (length == 0)
			return CharScanner.EOF_SENTINEL;
			
		char c = chars[offset];
		
		offset += 1;
		length -= 1;
		
		return c;
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
		
		if ( this.isEmpty() || (d = Character.digit(this.readChar(), radix)) < 0 )
			throw new IllegalStateException("Illegal digit character");
		
		return d;
	}
	
	/**
	 * Returns next, possibly unescaped first, character.
	 * 
	 * @return next character
	 */
	public char readNextChar()
	{
		char c = this.readChar();
		
		if ( c == '\\' && !this.isEmpty() )
		{
			switch ( c = this.readChar() )
			{
				case 'b': return '\b';
				case 'f': return '\f';
				case 'n': return '\n';
				case 'r': return '\r';
				case 't': return '\t';
				case 'x':
				{
					return (char) (this.readDigit(16) << 4 | this.readDigit(16));
				}
				case 'u':
				{
					return (char) (this.readDigit(16) << 12 | this.readDigit(16) <<  8 | 
							       this.readDigit(16) <<  4 | this.readDigit(16));
				}
			}
		}
		return c;
	}

	/**
	 * Returns Substring as a String. No escaping is performed. Used for testing only.
	 */
	public String toString()
	{
		return new String(chars, offset, length);
	}
	
	/**
	 * Returns a marker that points to the first character.
	 * Used in tests only.
	 */
	public int getMark()
	{
		return offset;
	}
	
	/**
	 * Resets a substring so that previously marked character becomes its first character. 
	 * Used in tests only.
	 * 
	 * @param mark character marker previously returned by getMark
	 */
	public void reset(int mark)
	{
		int d = offset - mark;
		offset = mark;
		length += d;
	}
}
