package beaver.lexer;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.junit.Test;

import beaver.CharScanner;
import beaver.UnexpectedCharacterException;

public class CharScannerGeneratorTest
{
	private static final int EOF = Short.MAX_VALUE;
	
	static class NoSuchScannerException extends RuntimeException
	{
        private static final long serialVersionUID = -8923308417615916686L;

		NoSuchScannerException(Throwable t)
		{
			super(t);
		}
	}

	private static CharScanner getScanner(final String scannerName, Reader input)
	{
		return getScanner(scannerName, "beaver/CharScanner", input);
	}
	
	private static CharScanner getScanner(final String scannerName, final String superClass, Reader input)
	{
		try
		{
			ClassLoader ldr = new ClassLoader()
			{
				public Class<?> loadClass(String name) throws ClassNotFoundException
				{
					if (name.equals(scannerName))
					{
						try
						{
							Method getScannerRegExp = RegExpTestFixtures.class.getDeclaredMethod("get" + scannerName);
							RegExp re = (RegExp) getScannerRegExp.invoke(null);
							NFA nfa = new NFA(re);
							DFA dfa = new DFA(nfa);
							byte[] bc = new CharScannerGenerator(dfa, EOF).compile(name, superClass);
							
							// DFAPrinter.print(dfa);
							// new ClassReader(bc).accept(new TraceClassVisitor(new PrintWriter(System.out)), 0);
							
							return defineClass(name, bc, 0, bc.length);
						}
						catch (Exception e)
						{
							throw new ClassNotFoundException(name, e);
						}
					}
					return super.loadClass(name);
				}
			};
			Class<?> intScannerClass = ldr.loadClass(scannerName);
			Constructor<?> constr = intScannerClass.getConstructor(java.io.Reader.class);
			return (CharScanner) constr.newInstance(input);
		}
		catch (Exception e)
		{
			throw new NoSuchScannerException(e);
		}
	}

	@Test(expected=UnexpectedCharacterException.class)
	public void testCompile() throws UnexpectedCharacterException, IOException
	{
		CharScanner scanner = getScanner("IntegerScanner", new StringReader(" 23 -99 0x89AF 0xabctest"));

		assertEquals(2, scanner.getNextToken());
		assertEquals(" ", scanner.getTokenText());
		assertEquals(1, scanner.getNextToken());
		assertEquals("23", scanner.getTokenText());
		assertEquals(2, scanner.getNextToken());
		assertEquals(" ", scanner.getTokenText());
		assertEquals(1, scanner.getNextToken());
		assertEquals("-99", scanner.getTokenText());
		assertEquals(2, scanner.getNextToken());
		assertEquals(" ", scanner.getTokenText());
		assertEquals(1, scanner.getNextToken());
		assertEquals("0x89AF", scanner.getTokenText());
		assertEquals(2, scanner.getNextToken());
		assertEquals(" ", scanner.getTokenText());
		assertEquals(1, scanner.getNextToken());
		assertEquals("0xabc", scanner.getTokenText());

		scanner.getNextToken();
	}

	@Test
	public void testCalculatorScanner() throws UnexpectedCharacterException, IOException
	{
		CharScanner scanner = getScanner("CalculatorScanner", new StringReader(" set acc = 23.98 * 0.478 \r\n print acc+25.67\n"));
		
		assertEquals(1, scanner.getNextToken());
		assertEquals(" ", scanner.getTokenText());
		assertEquals(3, scanner.getNextToken());
		assertEquals("set", scanner.getTokenText());
		assertEquals(1, scanner.getNextToken());
		assertEquals(" ", scanner.getTokenText());
		assertEquals(10, scanner.getNextToken());
		assertEquals("acc", scanner.getTokenText());
		assertEquals(1, scanner.getNextToken());
		assertEquals(" ", scanner.getTokenText());
		assertEquals(8, scanner.getNextToken());
		assertEquals("=", scanner.getTokenText());
		assertEquals(1, scanner.getNextToken());
		assertEquals(" ", scanner.getTokenText());
		assertEquals(9, scanner.getNextToken());
		assertEquals("23.98", scanner.getTokenText());
		assertEquals(1, scanner.getNextToken());
		assertEquals(" ", scanner.getTokenText());
		assertEquals(6, scanner.getNextToken());
		assertEquals("*", scanner.getTokenText());
		assertEquals(1, scanner.getNextToken());
		assertEquals(" ", scanner.getTokenText());
		assertEquals(9, scanner.getNextToken());
		assertEquals("0.478", scanner.getTokenText());
		assertEquals(1, scanner.getNextToken());
		assertEquals(" ", scanner.getTokenText());
		assertEquals(1, scanner.getNextToken());
		assertEquals("\r", scanner.getTokenText());
		assertEquals(1, scanner.getNextToken());
		assertEquals("\n", scanner.getTokenText());
		assertEquals(1, scanner.getNextToken());
		assertEquals(" ", scanner.getTokenText());
		assertEquals(2, scanner.getNextToken());
		assertEquals("print", scanner.getTokenText());
		assertEquals(1, scanner.getNextToken());
		assertEquals(" ", scanner.getTokenText());
		assertEquals(10, scanner.getNextToken());
		assertEquals("acc", scanner.getTokenText());
		assertEquals(4, scanner.getNextToken());
		assertEquals("+", scanner.getTokenText());
		assertEquals(9, scanner.getNextToken());
		assertEquals("25.67", scanner.getTokenText());
		assertEquals(1, scanner.getNextToken());
		assertEquals("\n", scanner.getTokenText());
		assertEquals(EOF, scanner.getNextToken());
		assertEquals(EOF, scanner.getNextToken());
		assertEquals(EOF, scanner.getNextToken());
	}
	
	@Test
	public void testSimpleCalculatorScanner() throws UnexpectedCharacterException, IOException
	{
		CharScanner scanner = getScanner("SimpleCalculatorScanner", new StringReader(" a1 = 23.98 * 0.478; b_2 = (a1 - 9.78) / 2.78; b_2 * a1 + b_2 / a1 \n"));
		
		assertEquals(10, scanner.getNextToken());
		assertEquals("a1", scanner.getTokenText());
		assertEquals(6, scanner.getNextToken());
		assertEquals("=", scanner.getTokenText());
		assertEquals(9, scanner.getNextToken());
		assertEquals("23.98", scanner.getTokenText());
		assertEquals(4, scanner.getNextToken());
		assertEquals("*", scanner.getTokenText());
		assertEquals(9, scanner.getNextToken());
		assertEquals("0.478", scanner.getTokenText());
		assertEquals(1, scanner.getNextToken());
		assertEquals(";", scanner.getTokenText());
		assertEquals(10, scanner.getNextToken());
		assertEquals("b_2", scanner.getTokenText());
		assertEquals(6, scanner.getNextToken());
		assertEquals("=", scanner.getTokenText());
		assertEquals(7, scanner.getNextToken());
		assertEquals("(", scanner.getTokenText());
		assertEquals(10, scanner.getNextToken());
		assertEquals("a1", scanner.getTokenText());
		assertEquals(3, scanner.getNextToken());
		assertEquals("-", scanner.getTokenText());
		assertEquals(9, scanner.getNextToken());
		assertEquals("9.78", scanner.getTokenText());
		assertEquals(8, scanner.getNextToken());
		assertEquals(")", scanner.getTokenText());
		assertEquals(5, scanner.getNextToken());
		assertEquals("/", scanner.getTokenText());
		assertEquals(9, scanner.getNextToken());
		assertEquals("2.78", scanner.getTokenText());
		assertEquals(1, scanner.getNextToken());
		assertEquals(";", scanner.getTokenText());
		assertEquals(10, scanner.getNextToken());
		assertEquals("b_2", scanner.getTokenText());
		assertEquals(4, scanner.getNextToken());
		assertEquals("*", scanner.getTokenText());
		assertEquals(10, scanner.getNextToken());
		assertEquals("a1", scanner.getTokenText());
		assertEquals(2, scanner.getNextToken());
		assertEquals("+", scanner.getTokenText());
		assertEquals(10, scanner.getNextToken());
		assertEquals("b_2", scanner.getTokenText());
		assertEquals(5, scanner.getNextToken());
		assertEquals("/", scanner.getTokenText());
		assertEquals(10, scanner.getNextToken());
		assertEquals("a1", scanner.getTokenText());
		assertEquals(EOF, scanner.getNextToken());
		assertEquals(EOF, scanner.getNextToken());
		assertEquals(EOF, scanner.getNextToken());
	}
	
	@Test
	public void testLogicCalculatorScanner() throws UnexpectedCharacterException, IOException
	{
		CharScanner scanner = getScanner("LogicCalculatorScanner", new StringReader(" android and oracle or not note \n"));
		
		assertEquals(4, scanner.getNextToken());
		assertEquals("android", scanner.getTokenText());
		assertEquals(1, scanner.getNextToken());
		assertEquals("and", scanner.getTokenText());
		assertEquals(4, scanner.getNextToken());
		assertEquals("oracle", scanner.getTokenText());
		assertEquals(2, scanner.getNextToken());
		assertEquals("or", scanner.getTokenText());
		assertEquals(3, scanner.getNextToken());
		assertEquals("not", scanner.getTokenText());
		assertEquals(4, scanner.getNextToken());
		assertEquals("note", scanner.getTokenText());
		assertEquals(EOF, scanner.getNextToken());
	}
	
	public static abstract class LogicCalculatorScannerBase extends CharScanner
	{
		protected LogicCalculatorScannerBase(Reader src)
		{
			super(src);
		}
	}

	@Test
	public void testLogicCalculatorScannerWithEvents() throws UnexpectedCharacterException, IOException
	{
		CharScanner scanner = getScanner( "LogicCalculatorScannerWithEvents"
				                        , "beaver/lexer/CharScannerGeneratorTest$LogicCalculatorScannerBase"
				                        , new StringReader(" android\r\n and oracle \r\n or not note \r\ndone"));
		
		assertEquals(4, scanner.getNextToken());
		assertEquals("android", scanner.getTokenText());
		assertEquals(1, scanner.getTokenLine());
		assertEquals(2, scanner.getTokenColumn());

		assertEquals(1, scanner.getNextToken());
		assertEquals("and", scanner.getTokenText());
		assertEquals(2, scanner.getTokenLine());
		assertEquals(2, scanner.getTokenColumn());
		
		assertEquals(4, scanner.getNextToken());
		assertEquals("oracle", scanner.getTokenText());
		assertEquals(2, scanner.getTokenLine());
		assertEquals(6, scanner.getTokenColumn());
		
		assertEquals(2, scanner.getNextToken());
		assertEquals("or", scanner.getTokenText());
		assertEquals(3, scanner.getTokenLine());
		assertEquals(2, scanner.getTokenColumn());
		
		assertEquals(3, scanner.getNextToken());
		assertEquals("not", scanner.getTokenText());
		assertEquals(3, scanner.getTokenLine());
		assertEquals(5, scanner.getTokenColumn());
		
		assertEquals(4, scanner.getNextToken());
		assertEquals("note", scanner.getTokenText());
		assertEquals(3, scanner.getTokenLine());
		assertEquals(9, scanner.getTokenColumn());

		assertEquals(4, scanner.getNextToken());
		assertEquals("done", scanner.getTokenText());
		assertEquals(4, scanner.getTokenLine());
		assertEquals(1, scanner.getTokenColumn());
		
		assertEquals(EOF, scanner.getNextToken());
	}
	
	@Test
	public void testTextScanner() throws UnexpectedCharacterException, IOException
	{
		CharScanner scanner = getScanner( "TextScanner", new StringReader(" \"abc\" \"\\a\" \"\\\\\" "));

		assertEquals(1, scanner.getNextToken());
		assertEquals("\"abc\"", scanner.getTokenText());

		assertEquals(1, scanner.getNextToken());
		assertEquals("\"\\a\"", scanner.getTokenText());

		assertEquals(1, scanner.getNextToken());
		assertEquals("\"\\\\\"", scanner.getTokenText());
		
		assertEquals(EOF, scanner.getNextToken());
	}		
}
