package beaver.lexer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class CharClassBuilderTest
{
	@Test
	public void testNumberScanner()
	{
		CharSet cset = new CharSet(RegExpTestFixtures.getDigitalScanner());
		assertEquals(2, cset.size);
		CharMap classes = cset.classes;
		assertEquals(14, classes.size());
		CharClass num = (CharClass) classes.get('0');
		assertSame(num, classes.get('1'));
		assertSame(num, classes.get('2'));
		assertSame(num, classes.get('3'));
		assertSame(num, classes.get('4'));
		assertSame(num, classes.get('5'));
		assertSame(num, classes.get('6'));
		assertSame(num, classes.get('7'));
		assertSame(num, classes.get('8'));
		assertSame(num, classes.get('9'));
		CharClass spc = (CharClass) classes.get(' ');
		assertNotSame(spc, num);
		assertSame(spc, classes.get('\t'));
		assertSame(spc, classes.get('\n'));
		assertSame(spc, classes.get('\r'));
	}
	
	@Test 
	public void testDecimalScanner()
	{
		CharSet cset = new CharSet(RegExpTestFixtures.getDecimalScanner());
		assertEquals(3, cset.size);
		CharMap classes = cset.classes;
		assertEquals(15, classes.size());
		CharClass num = (CharClass) classes.get('0');
		assertSame(num, classes.get('1'));
		assertSame(num, classes.get('2'));
		assertSame(num, classes.get('3'));
		assertSame(num, classes.get('4'));
		assertSame(num, classes.get('5'));
		assertSame(num, classes.get('6'));
		assertSame(num, classes.get('7'));
		assertSame(num, classes.get('8'));
		assertSame(num, classes.get('9'));
		CharClass spc = (CharClass) classes.get(' ');
		assertNotSame(spc, num);
		assertSame(spc, classes.get('\t'));
		assertSame(spc, classes.get('\n'));
		assertSame(spc, classes.get('\r'));
		CharClass dot = (CharClass) classes.get('.');
		assertNotSame(spc, dot);
		assertNotSame(num, dot);
	}
	
	@Test
	public void testIntegerScanner()
	{
		CharSet cset = new CharSet(RegExpTestFixtures.getIntegerScanner());
		assertEquals(6, cset.size);
		CharMap classes = cset.classes;
		assertEquals(28, classes.size());
		CharClass nonZeroDigit = (CharClass) classes.get('1');
		assertSame(nonZeroDigit, classes.get('2'));
		assertSame(nonZeroDigit, classes.get('3'));
		assertSame(nonZeroDigit, classes.get('4'));
		assertSame(nonZeroDigit, classes.get('5'));
		assertSame(nonZeroDigit, classes.get('6'));
		assertSame(nonZeroDigit, classes.get('7'));
		assertSame(nonZeroDigit, classes.get('8'));
		assertSame(nonZeroDigit, classes.get('9'));
		assertEquals(9, nonZeroDigit.cardinality);
		
		CharClass spc = (CharClass) classes.get(' ');
		assertSame(spc, classes.get('\t'));
		assertSame(spc, classes.get('\n'));
		assertSame(spc, classes.get('\r'));
		assertEquals(4, spc.cardinality);
		assertNotSame(spc, nonZeroDigit);
		
		CharClass zero = (CharClass) classes.get('0');
		assertEquals(1, zero.cardinality);
		assertNotSame(spc, zero);
		assertNotSame(nonZeroDigit, zero);
		
		CharClass hex = (CharClass) classes.get('x');
		assertEquals(1, hex.cardinality);
		assertNotSame(spc, hex);
		assertNotSame(nonZeroDigit, hex);
		assertNotSame(zero, hex);
		
		CharClass letter = (CharClass) classes.get('A');
		assertSame(letter, classes.get('B'));
		assertSame(letter, classes.get('C'));
		assertSame(letter, classes.get('D'));
		assertSame(letter, classes.get('E'));
		assertSame(letter, classes.get('F'));
		assertSame(letter, classes.get('a'));
		assertSame(letter, classes.get('b'));
		assertSame(letter, classes.get('c'));
		assertSame(letter, classes.get('d'));
		assertSame(letter, classes.get('e'));
		assertSame(letter, classes.get('f'));
		assertEquals(12, letter.cardinality);
	}
}
