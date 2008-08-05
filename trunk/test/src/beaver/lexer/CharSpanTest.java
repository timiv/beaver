package beaver.lexer;

import static org.junit.Assert.*;

import org.junit.Test;

public class CharSpanTest
{

	@Test
	public void testSize()
	{
		CharSpan span = new CharSpan('a');
		assertEquals(1, span.size());
		span = new CharSpan('a', 'z');
		assertEquals(26, span.size());
	}

	@Test
	public void testEqualsCharSpan()
	{
		CharSpan span1 = new CharSpan('a', 'z');
		CharSpan span2 = new CharSpan('a', 'z');
		assertTrue(span1.equals(span2));
	}

	@Test
	public void testIntersects()
	{
		CharSpan span = new CharSpan('c', 'n');
		assertFalse(span.intersects(new CharSpan('a', 'b')));
		assertTrue(span.intersects(new CharSpan('a', 'c')));
		assertTrue(span.intersects(new CharSpan('a', 'f')));
		assertTrue(span.intersects(new CharSpan('k', 'w')));
		assertTrue(span.intersects(new CharSpan('n', 'w')));
		assertFalse(span.intersects(new CharSpan('o', 'w')));
		assertFalse(span.intersects(new CharSpan('r', 'w')));
	}

	@Test
	public void testIsAdjacentToSpan()
	{
		CharSpan span = new CharSpan('d', 'n');
		assertFalse(span.isAdjacentTo(new CharSpan('a', 'b')));
		assertTrue(span.isAdjacentTo(new CharSpan('a', 'c')));
		assertFalse(span.isAdjacentTo(new CharSpan('a', 'd')));
		assertFalse(span.isAdjacentTo(new CharSpan('n', 'w')));
		assertTrue(span.isAdjacentTo(new CharSpan('o', 'w')));
		assertFalse(span.isAdjacentTo(new CharSpan('p', 'w')));
	}

	@Test
	public void testIsSubsetOf()
	{
		CharSpan span = new CharSpan('c', 'e');
		assertTrue(span.isSubsetOf(new CharSpan('a', 'k')));
		assertTrue(span.isSubsetOf(new CharSpan('c', 'k')));
		assertTrue(span.isSubsetOf(new CharSpan('a', 'f')));
		assertFalse(span.isSubsetOf(new CharSpan('a', 'd')));
		assertFalse(span.isSubsetOf(new CharSpan('d', 'h')));
		assertFalse(span.isSubsetOf(new CharSpan('a', 'b')));
		assertFalse(span.isSubsetOf(new CharSpan('f', 'h')));
	}
	
	@Test
	public void testContains()
	{
		CharSpan span = new CharSpan('c', 'e');
		assertTrue(span.contains('c'));
		assertTrue(span.contains('d'));
		assertTrue(span.contains('e'));
		assertFalse(span.contains('b'));
		assertFalse(span.contains('f'));
	}
	
	@Test
	public void testIsAdjacentToChar()
	{
		CharSpan span = new CharSpan('c', 'e');
		assertTrue(span.isAdjacentTo('b'));
		assertTrue(span.isAdjacentTo('f'));
		assertFalse(span.isAdjacentTo('a'));
		assertFalse(span.isAdjacentTo('c'));
		assertFalse(span.isAdjacentTo('e'));
		assertFalse(span.isAdjacentTo('g'));
	}

	@Test
	public void testCompareSpan()
	{
		CharSpan span = new CharSpan('d', 'e');
		assertTrue(span.compare(new CharSpan('g', 'k')) < 0);
		assertTrue(span.compare(new CharSpan('a', 'b')) > 0);
		assertTrue(span.compare(new CharSpan('a', 'c')) == 0);
		assertTrue(span.compare(new CharSpan('a', 'd')) == 0);
		assertTrue(span.compare(new CharSpan('e', 'k')) == 0);
		assertTrue(span.compare(new CharSpan('f', 'k')) == 0);
	}

	@Test
	public void testCompareChar()
	{
		CharSpan span = new CharSpan('c', 'e');
		assertTrue(span.compare('a') > 0);
		assertTrue(span.compare('b') == 0);
		assertTrue(span.compare('c') == 0);
		assertTrue(span.compare('d') == 0);
		assertTrue(span.compare('e') == 0);
		assertTrue(span.compare('f') == 0);
		assertTrue(span.compare('g') < 0);
	}
	
	@Test
	public void testAddChar()
	{
		CharSpan span = new CharSpan('c', 'e');
		span.add('b');
		span.add('d');
		span.add('f');
		assertEquals(new CharSpan('b', 'f'), span);
	}
	
	@Test
	public void testUnion()
	{
		CharSpan span = new CharSpan('d', 'l');
		span.add(new CharSpan('k', 'o'));
		assertTrue(span.equals(new CharSpan('d', 'o')));
		span.add(new CharSpan('b', 'k'));
		assertTrue(span.equals(new CharSpan('b', 'o')));
		span.add(new CharSpan('p', 'q'));
		assertTrue(span.equals(new CharSpan('b', 'q')));
		span.add(new CharSpan('a', 'b'));
		assertTrue(span.equals(new CharSpan('a', 'q')));
		span.add(new CharSpan('f', 'n'));
		assertTrue(span.equals(new CharSpan('a', 'q')));
	}
	
	@Test
	public void testToString()
	{
		CharSpan span = new CharSpan('a');
		assertEquals("a", span.toString());
		span.add('b');
		assertEquals("ab", span.toString());
		span.add('c');
		assertEquals("a-c", span.toString());
	}
	
	@Test
	public void testAccept()
	{
		CharSpan span = new CharSpan('a', 'd');
		final int[] counter = new int[] { 0 };
		final char[] bounds = new char[] { '\uffff', '\0' };
		span.accept(new CharVisitor()
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
		assertEquals(4, counter[0]);
		assertEquals('a', bounds[0]);
		assertEquals('d', bounds[1]);
	}
	
}
