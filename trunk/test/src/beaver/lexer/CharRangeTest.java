package beaver.lexer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CharRangeTest
{

	@Test
	public void testCharRangeAddAndStringRepr()
	{
		CharRange range = new CharRange(new CharSpan('0', '9'));
		assertEquals("0-9", range.toString());
		range.add(new CharSpan('A', 'F'));
		assertEquals("0-9A-F", range.toString());
		range.add(new CharSpan('D', 'H'));
		assertEquals("0-9A-H", range.toString());
		range.add(new CharSpan('W', 'Z'));
		assertEquals("0-9A-HW-Z", range.toString());
		range.add(new CharSpan('E', 'X'));
		assertEquals("0-9A-Z", range.toString());
	}

	@Test
	public void testCharRangeContructorFromString()
	{
		CharRange range = new CharRange(new CharReader("abct-udefgh-klmnopq-sv-z"));
		assertEquals("a-z", range.toString());
		assertEquals(new CharRange(new CharSpan('a', 'z')), range);
		
		range = new CharRange(new CharReader("a"));
		assertEquals("a", range.toString());
		
		range = new CharRange(new CharReader("0-9."));
		assertEquals(".0-9", range.toString());
		
		range = new CharRange(new CharReader("^0-9"));
		assertEquals("\\x00-/:-\\ufffe", range.toString());
		
		range = new CharRange(new CharReader("^\\x00-/:-@[-`{-\\ufffe"));
		assertEquals("0-9A-Za-z", range.toString());
	}

	@Test
	public void testAddChar()
	{
		CharRange range = new CharRange();
		range.add('a');
		assertEquals("a", range.toString());
		range.add('g');
		assertEquals("ag", range.toString());
		range.add('c');
		assertEquals("acg", range.toString());
		range.add('b');
		assertEquals("a-cg", range.toString());
		range.add('f');
		assertEquals("a-cfg", range.toString());
		range.add('e');
		assertEquals("a-ce-g", range.toString());
		range.add('d');
		assertEquals("a-g", range.toString());
	}
	
	@Test
	public void testAccept()
	{
		CharRange range = new CharRange(new CharReader("abct-udefgh-klmnopq-sv-z"));
		final int[] counter = new int[] { 0 };
		final char[] bounds = new char[] { '\uffff', '\0' };
		range.accept(new CharVisitor()
		{
			public void visit(char c)
			{
				counter[0]++;
				if (bounds[0] > c)
					bounds[0] = c;
				if (bounds[1] < c)
					bounds[1] = c;
			}
		});
		assertEquals(26, counter[0]);
		assertEquals('a', bounds[0]);
		assertEquals('z', bounds[1]);
	}
	
	@Test
	public void testSubtraction_ReduceSpans()
	{
		CharRange range = new CharRange(new CharReader("a-ps-z"));
		
		range.sub(new CharRange(new CharReader("l-u")));
		assertEquals("a-kv-z", range.toString());
		
		range.sub(new CharRange(new CharReader("a-dx-z")));
		assertEquals("e-kvw", range.toString());
		
		range.sub(new CharRange(new CharReader("a-z")));
		assertEquals("", range.toString());
	}
	
	@Test
	public void testSubtraction_RemoveSpans()
	{
		CharRange range = new CharRange(new CharReader("a-ps-z"));
		
		range.sub(new CharRange(new CharReader("a-p")));
		assertEquals("s-z", range.toString());
		
		range.sub(new CharRange(new CharReader("s-z")));
		assertEquals("", range.toString());
	}
	
	@Test
	public void testSubtraction_SplitSpans()
	{
		CharRange range = new CharRange(new CharReader("a-z"));
		
		range.sub(new CharRange(new CharReader("k-mt-v")));
		assertEquals("a-jn-sw-z", range.toString());	
	}
}
