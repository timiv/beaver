package beaver.lexer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

public class NFACompilerTest
{
	@Test
	public void testDecimalScanner()
	{
		RegExp re = RegExpTestFixtures.getDecimalScanner();
		CharSet cset = new CharSet(re);
		NFACompiler comp = new NFACompiler(cset);
		re.accept(comp);
		
		NFANode start = comp.getStart();
		assertTrue(start instanceof NFANode.Fork);
		
		NFANode.Fork n0 = (NFANode.Fork) start;
		assertTrue(n0.next instanceof NFANode.Char);
		
		NFANode.Char n1 = (NFANode.Char) n0.next;
		assertArrayEquals(new CharClass[] { (CharClass) cset.classes.get('0') }, n1.charClasses);
		assertTrue(n1.next instanceof NFANode.Fork);
		
		NFANode.Fork n2 = (NFANode.Fork) n1.next;
		assertTrue(n2.alt instanceof NFANode.Char);
		
		NFANode.Char n3 = (NFANode.Char) n2.alt;
		assertArrayEquals(new CharClass[] { (CharClass) cset.classes.get('0') }, n3.charClasses);
		assertSame(n2, n3.next); 
		
		assertTrue(n2.next instanceof NFANode.Fork);
		
		NFANode.Fork n4 = (NFANode.Fork) n2.next;
		assertTrue(n4.next instanceof NFANode.Char);
		
		NFANode.Char n5 = (NFANode.Char) n4.next;
		assertArrayEquals(new CharClass[] { (CharClass) cset.classes.get('.') }, n5.charClasses);
		assertTrue(n5.next instanceof NFANode.Char);

		NFANode.Char n6 = (NFANode.Char) n5.next;
		assertArrayEquals(new CharClass[] { (CharClass) cset.classes.get('0') }, n6.charClasses);	
		assertTrue(n6.next instanceof NFANode.Fork);
		
		NFANode.Fork n7 = (NFANode.Fork) n6.next;
		assertTrue(n7.alt instanceof NFANode.Char);
		
		NFANode.Char n8 = (NFANode.Char) n7.alt;
		assertArrayEquals(new CharClass[] { (CharClass) cset.classes.get('0') }, n8.charClasses);
		assertSame(n7, n8.next); 
		
		assertTrue(n7.next instanceof NFANode.Term);
		
		NFANode.Term n9 = (NFANode.Term) n7.next;
		assertSame(n9, n4.alt);
		assertEquals(1, n9.accept.id);
		assertNull(n9.next);
		
		assertTrue(n0.alt instanceof NFANode.Char);
		
		NFANode.Char n10 = (NFANode.Char) n0.alt;
		assertArrayEquals(new CharClass[] { (CharClass) cset.classes.get(' ') }, n10.charClasses);
		assertTrue(n10.next instanceof NFANode.Term);
		
		NFANode.Term n11 = (NFANode.Term) n10.next;
		assertEquals(2, n11.accept.id);
		assertNull(n11.next);
	}
	
	@Ignore
	@Test
	public void testIntegerScanner()
	{
		RegExp re = RegExpTestFixtures.getIntegerScanner();
		CharSet cset = new CharSet(re);
		NFACompiler comp = new NFACompiler(cset);
		re.accept(comp);
		
		NFANode start = comp.getStart();
		start.accept(new NFAPrinter());
	}
}
