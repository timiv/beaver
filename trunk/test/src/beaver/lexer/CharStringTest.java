package beaver.lexer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CharStringTest
{

	@Test
	public void testTrim()
	{
		CharReader str = new CharReader("abcdef");
		str.trim(3);
		assertTrue(str.isEmpty());
	}

	@Test
	public void testTrim1()
	{
		CharReader str = new CharReader("abcdef");
		str.trim1();
		str.trim1();
		str.trim1();
		assertTrue(str.isEmpty());
	}

	@Test
	public void testIsEmpty()
	{
		CharReader str = new CharReader("");
		assertTrue(str.isEmpty());
	}

	@Test
	public void testReadChar()
	{
		CharReader str = new CharReader("ab\\t\\x63\\u0064");
		assertEquals('a',  (char) str.readChar());
		assertEquals('b',  (char) str.readChar());
		assertEquals('\t', (char) str.readChar());
		assertEquals('c',  (char) str.readChar());
		assertEquals('d',  (char) str.readChar());
	}

	@Test
	public void testReadDigit()
	{
		CharReader str = new CharReader("25AF");
		assertEquals(2,   str.readDigit(10));
		assertEquals(5,   str.readDigit(10));
		assertEquals(10,  str.readDigit(16));
		assertEquals(15,  str.readDigit(16));
	}
	
	@Test
	public void testReadText()
	{
	    CharReader str = new CharReader("\\t123.456\\r\\n567.89 098.48\\b");
		assertEquals('\t', (char) str.readChar());
		assertEquals('1',  (char) str.readChar());
		assertEquals('2',  (char) str.readChar());
		assertEquals('3',  (char) str.readChar());
		assertEquals('.',  (char) str.readChar());
		assertEquals('4',  (char) str.readChar());
		assertEquals('5',  (char) str.readChar());
		assertEquals('6',  (char) str.readChar());
		assertEquals('\r', (char) str.readChar());
		assertEquals('\n', (char) str.readChar());
		assertEquals('5',  (char) str.readChar());
		assertEquals('6',  (char) str.readChar());
		assertEquals('7',  (char) str.readChar());
		assertEquals('.',  (char) str.readChar());
		assertEquals('8',  (char) str.readChar());
		assertEquals('9',  (char) str.readChar());
		assertEquals(' ',  (char) str.readChar());
		assertEquals('0',  (char) str.readChar());
		assertEquals('9',  (char) str.readChar());
		assertEquals('8',  (char) str.readChar());
		assertEquals('.',  (char) str.readChar());
		assertEquals('4',  (char) str.readChar());
		assertEquals('8',  (char) str.readChar());
		assertEquals('\b', (char) str.readChar());
		assertEquals(-1,   str.readChar());
	    
	}

}
