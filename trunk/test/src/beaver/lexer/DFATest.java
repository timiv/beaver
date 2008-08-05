package beaver.lexer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import beaver.UnexpectedCharacterException;

public class DFATest
{
	@Test
    public void testDecimalScanner() throws UnexpectedCharacterException
    {
	    DFAInterpreter i = new DFAInterpreter(new DFA(new NFA(RegExpTestFixtures.getDecimalScanner())));
	    CharReader str = new CharReader("\\t123.456\\r\\n567.89 098.48\\n");
	    assertEquals(2, i.next(str));
	    assertEquals("\t", i.text());
	    assertEquals(1, i.next(str));
	    assertEquals("123.456", i.text());
	    assertEquals(2, i.next(str));
	    assertEquals("\r", i.text());
	    assertEquals(2, i.next(str));
	    assertEquals("\n", i.text());
	    assertEquals(1, i.next(str));
	    assertEquals("567.89", i.text());
	    assertEquals(2, i.next(str));
	    assertEquals(" ", i.text());
	    assertEquals(1, i.next(str));
	    assertEquals("098.48", i.text());
	    assertEquals(2, i.next(str));
	    assertEquals("\n", i.text());
	    assertEquals(0, i.next(str));
	    assertEquals(0, i.next(str));
    }
	
	@Test 
	public void testIntegerScanner() throws UnexpectedCharacterException
	{
		NFA nfa = new NFA(RegExpTestFixtures.getIntegerScanner()); 
		DFA dfa = new DFA(nfa);
	    DFAInterpreter i = new DFAInterpreter(dfa);
	    CharReader str = new CharReader("\t123\n-456 23\n\t0x234\n");
	    assertEquals(2, i.next(str));
	    assertEquals("\t", i.text());
	    assertEquals(1, i.next(str));
	    assertEquals("123", i.text());
	    assertEquals(2, i.next(str));
	    assertEquals("\n", i.text());
	    assertEquals(1, i.next(str));
	    assertEquals("-456", i.text());
	    assertEquals(2, i.next(str));
	    assertEquals(" ", i.text());
	    assertEquals(1, i.next(str));
	    assertEquals("23", i.text());
	    assertEquals(2, i.next(str));
	    assertEquals("\n", i.text());
	    assertEquals(2, i.next(str));
	    assertEquals("\t", i.text());
	    assertEquals(1, i.next(str));
	    assertEquals("0x234", i.text());
	    assertEquals(2, i.next(str));
	    assertEquals("\n", i.text());
	    assertEquals(0, i.next(str));
	}

	@Test(expected=UnexpectedCharacterException.class)
    public void testIntegerScannerWithBadInput() throws UnexpectedCharacterException
	{
	    NFA nfa = new NFA(RegExpTestFixtures.getIntegerScanner());
	    //nfa.start.accept(new NFAPrinter());
		DFA dfa = new DFA(nfa);
		//DFAPrinter.print(dfa);
		DFAInterpreter i = new DFAInterpreter(dfa);
	    CharReader str = new CharReader("\ntest\n");
	    assertEquals(2, i.next(str));
	    assertEquals("\n", i.text());
	    System.out.println(i.next(str) + "=[" + i.text() + "]");
	}
	
}
