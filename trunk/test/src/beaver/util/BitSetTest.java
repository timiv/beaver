package beaver.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BitSetTest
{

	@Test
	public void testAddBitAtEdge()
	{
		BitSet set = new BitSet();
		int n = set.capacity();
		
		set.add(0);
		assertTrue(set.isSet(0));
		for ( int i = 1; i < n; i++ )
		{
			assertFalse(set.isSet(i));
		}
		assertEquals(1, set.size());
		
		n--;
		set.add(n);
		assertTrue(set.isSet(0));
		assertTrue(set.isSet(n));
		for ( int i = 1; i < n; i++ )
		{
			assertFalse(set.isSet(i));
		}
		assertEquals(2, set.size());
	}
	
	@Test
	public void testAddBitWithinBoundaries()
	{
		BitSet set = new BitSet();
		
		set.add(25);
		assertEquals(1, set.size());
		assertTrue(set.isSet(25));

		set.add(80);
		assertEquals(2, set.size());
		assertTrue(set.isSet(80));
		
		set.add(50);
		assertEquals(3, set.size());
		assertTrue(set.isSet(50));
	}

	@Test
	public void testAddBitBeyondBoundaries()
	{
		BitSet set = new BitSet(64);
		
		set.add(25);
		assertEquals(1, set.size());
		assertTrue(set.isSet(25));

		set.add(80);
		assertEquals(2, set.size());
		assertTrue(set.isSet(80));
		
		set.add(10);
		assertEquals(3, set.size());
		assertTrue(set.isSet(10));
		assertTrue(set.isSet(25));
		assertTrue(set.isSet(80));
		
		set.add(300);
		assertEquals(4, set.size());
		assertTrue(set.isSet(10));
		assertTrue(set.isSet(25));
		assertTrue(set.isSet(80));
		assertTrue(set.isSet(300));
	}
	

	@Test
	public void testAddRangeToEmptySet()
	{
		BitSet set = new BitSet();
		
		set.add(12,24);
		assertEquals(12, set.size());
		
		for ( int i = 0; i < 12; i++ )
		{
			assertFalse(set.isSet(i));
		}
		for ( int i = 12; i < 24; i++ )
		{
			assertTrue(set.isSet(i));
		}
		for ( int i = 24; i < 256; i++ )
		{
			assertFalse(set.isSet(i));
		}
	}
	
	@Test
	public void testAddRangeToNonEmptySet()
	{
		BitSet set = new BitSet();
		
		set.add(10);
		set.add(15);
		set.add(20);
		set.add(25);
		assertEquals(4, set.size());
		
		set.add(12,24);
		assertEquals(14, set.size());
		
		for ( int i = 0; i < 10; i++ )
		{
			assertFalse(set.isSet(i));
		}
		assertTrue(set.isSet(10));
		assertFalse(set.isSet(11));
		
		for ( int i = 12; i < 24; i++ )
		{
			assertTrue(set.isSet(i));
		}
		assertFalse(set.isSet(24));
		assertTrue(set.isSet(25));
		for ( int i = 26; i < 256; i++ )
		{
			assertFalse(set.isSet(i));
		}
	}	

	@Test
	public void testErase()
	{
		BitSet set = new BitSet();
		
		set.add(12,24);
		set.erase(15);
		set.erase(20);
		assertEquals(10, set.size());
		
		for ( int i = 12; i < 15; i++ )
		{
			assertTrue(set.isSet(i));
		}
		assertFalse(set.isSet(15));
		for ( int i = 16; i < 20; i++ )
		{
			assertTrue(set.isSet(i));
		}
		assertFalse(set.isSet(20));
		for ( int i = 21; i < 24; i++ )
		{
			assertTrue(set.isSet(i));
		}
	}

	@Test
	public void testClear()
	{
		BitSet set = new BitSet();
		
		set.add(0);
		set.add(10);
		set.add(12, 24);
		set.add(25);
		assertEquals(15, set.size());
		
		set.clear();
		assertTrue(set.isEmpty());
		assertEquals(0, set.size());
		
		int n = set.capacity();
		for ( int i = 0; i < n; i++ )
		{
			assertFalse(set.isSet(i));
		}
	}

	@Test
	public void testAddBitSetToEmptySet()
	{
		BitSet srcSet = new BitSet();
		
		srcSet.add(10);
		srcSet.add(12, 24);
		srcSet.add(25);
		
		BitSet dstSet = new BitSet();
		assertTrue(dstSet.isEmpty());
		dstSet.add(srcSet);
		assertFalse(dstSet.isEmpty());
		assertEquals(14, dstSet.size());
		
		for ( int i = 0; i < 10; i++ )
		{
			assertFalse(dstSet.isSet(i));
		}
		assertTrue(dstSet.isSet(10));
		assertFalse(dstSet.isSet(11));
		for ( int i = 12; i < 24; i++ )
		{
			assertTrue(dstSet.isSet(i));
		}
		assertFalse(dstSet.isSet(24));
		assertTrue(dstSet.isSet(25));
		
		int n = dstSet.capacity();
		for ( int i = 26; i < n; i++ )
		{
			assertFalse(dstSet.isSet(i));
		}
	}

	@Test
	public void testAddBitSetToFullSet()
	{
		BitSet srcSet = new BitSet();
		
		srcSet.add(10);
		srcSet.add(12, 24);
		srcSet.add(25);
		
		BitSet dstSet = new BitSet();
		
		srcSet.add(5);
		srcSet.add(15);
		srcSet.add(20);
		
		dstSet.add(srcSet);
		assertEquals(15, dstSet.size());
		
		for ( int i = 0; i < 5; i++ )
		{
			assertFalse(dstSet.isSet(i));
		}
		assertTrue(dstSet.isSet(5));
		for ( int i = 6; i < 10; i++ )
		{
			assertFalse(dstSet.isSet(i));
		}
		assertTrue(dstSet.isSet(10));
		assertFalse(dstSet.isSet(11));
		for ( int i = 12; i < 24; i++ )
		{
			assertTrue(dstSet.isSet(i));
		}
		assertFalse(dstSet.isSet(24));
		assertTrue(dstSet.isSet(25));
		
		int n = dstSet.capacity();
		for ( int i = 26; i < n; i++ )
		{
			assertFalse(dstSet.isSet(i));
		}
	}

	@Test
	public void testAddBitSetToFullSetWithDifferentBoundaries()
	{
		BitSet srcSet = new BitSet(32);
		
		srcSet.add(10);
		srcSet.add(12, 24);
		srcSet.add(25);
		
		BitSet dstSet = new BitSet(32);
		
		srcSet.add(50);
		srcSet.add(64, 72);
		srcSet.add(80);
		
		dstSet.add(srcSet);
		assertEquals(24, dstSet.size());
		
		for ( int i = 0; i < 10; i++ )
		{
			assertFalse(dstSet.isSet(i));
		}
		assertTrue(dstSet.isSet(10));
		assertFalse(dstSet.isSet(11));
		for ( int i = 12; i < 24; i++ )
		{
			assertTrue(dstSet.isSet(i));
		}
		assertFalse(dstSet.isSet(24));
		assertTrue(dstSet.isSet(25));
		for ( int i = 26; i < 50; i++ )
		{
			assertFalse(dstSet.isSet(i));
		}
		assertTrue(dstSet.isSet(50));
		for ( int i = 51; i < 64; i++ )
		{
			assertFalse(dstSet.isSet(i));
		}
		for ( int i = 64; i < 72; i++ )
		{
			assertTrue(dstSet.isSet(i));
		}
		for ( int i = 72; i < 80; i++ )
		{
			assertFalse(dstSet.isSet(i));
		}
		assertTrue(dstSet.isSet(80));
		for ( int i = 81; i < 100; i++ )
		{
			assertFalse(dstSet.isSet(i));
		}
	}
	
	@Test
	public void testIsSet()
	{
		BitSet set = new BitSet();
		set.add(5);
		assertTrue(set.isSet(5));
	}

	@Test
	public void testIsEmpty()
	{
		BitSet set = new BitSet();
		assertTrue(set.isEmpty());
		set.add(10);
		assertFalse(set.isEmpty());
	}

	@Test
	public void testSize()
	{
		BitSet set = new BitSet();
		assertEquals(0, set.size());
		set.add(10);
		assertEquals(1, set.size());
		set.add(20);
		assertEquals(2, set.size());
		set.add(15, 25);
		assertEquals(11, set.size());
		set.clear();
		assertEquals(0, set.size());
	}

	@Test
	public void testCapacity()
	{
		BitSet set = new BitSet(32);
		assertEquals(32, set.capacity());
		set.add(0);
		set.add(32);
		assertTrue(set.capacity() > 32);
	}

	@Test
	public void testForEachBitAccept()
	{
		BitSet set = new BitSet();
		set.add(5);
		set.add(10);
		set.add(15);
		final BitSet res = new BitSet();
		set.forEachBitAccept(new BitSet.BitVisitor()
		{
			public void visit(int bit)
            {
				res.add(bit);
            }
		});
		assertEquals(3, res.size());
		assertTrue(set.isSet(5));
		assertTrue(set.isSet(10));
		assertTrue(set.isSet(15));
	}

	@Test
	public void testCountBits()
	{
		assertEquals(11, BitSet.countBits(0x7FC3));
		assertEquals(1, BitSet.countBits(0x8000));
	}

}
