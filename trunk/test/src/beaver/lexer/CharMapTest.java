package beaver.lexer;

import static org.junit.Assert.*;

import org.junit.Test;

public class CharMapTest
{

	@Test
	public void testPutAndGet()
	{
		CharMap map = new CharMap();
		map.put('a', "a");
		assertEquals("a", map.get('a'));
		map.put('b', "b");
		assertEquals("b", map.get('b'));
		assertNull(map.get('c'));
	}

	@Test
	public void testClear()
	{
		CharMap map = new CharMap();
		map.put('a', "a");
		map.put('b', "b");
		map.clear();
		assertNull(map.get('a'));
		assertNull(map.get('b'));
	}

	@Test
	public void testForEachEntryAcceptEntryVisitor()
	{
		CharMap map = new CharMap();
		map.put('a', "a");
		map.put('b', "b");
		final int[] counters = new int[1];
		map.accept(new CharMap.EntryVisitor()
		{
			public void visit(char key, Object value)
            {
				String v = value.toString();
				assertEquals(1, v.length());
				assertEquals(key, value.toString().charAt(0));
				counters[0]++;
            }
		});
		assertEquals(2, counters[0]);
	}
	
	@Test
	public void testSize()
	{
		CharMap map = new CharMap();
		assertEquals(0, map.size());
		map.put('a', "a");
		assertEquals(1, map.size());
		map.put('b', "b");
		assertEquals(2, map.size());
		map.put('c', "b");
		assertEquals(3, map.size());
		map.clear();
		assertEquals(0, map.size());
	}

}
