/***
 * Beaver: compiler builder framework for Java                       
 * Copyright (c) 2003-2006 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */
package beaver.util;

import java.util.Arrays;

public class BitSet
{
	int[] bit_bags;
	int   num_bits;
	int   lb, ub;   // [lb, ub)
	
	public BitSet(int capacity)
	{
		bit_bags = new int[(capacity + 31) / 32];
	}
	
	public BitSet()
	{
		this(256);
	}
	
	/**
	 * Sets a single bit to 1.
	 *
	 * @param i element to add to the set
	 * @return true if a bit has been added and false if it was already in the set
	 */
	public boolean add(int i)
	{
		int bag_index = getBitBagIndex(i);
		int bit_index = i & 31;
		int bit_mask = 1 << bit_index;
		boolean bit_not_set = (bit_bags[bag_index] & bit_mask) == 0;
		if (bit_not_set)
		{
			bit_bags[bag_index] |= bit_mask;
			num_bits += 1;
		}
		return bit_not_set;
	}
	
	/**
	 * Sets a single bit in the set to 0 
	 * 
	 * @param i bit number to be removed from the set
	 */
	public void erase(int i)
	{
		if (i < lb || ub <= i)
			throw new IndexOutOfBoundsException("Bit "+i+" is out of bounds ["+lb+","+ub+")");
		
		int bag_index = (i - lb) / 32;
		int bit_index = i & 31;
		int bit_mask = 1 << bit_index;
		
		if ((bit_bags[bag_index] & bit_mask) != 0)
		{
			bit_bags[bag_index] &= ~bit_mask;
			num_bits--;
		}
	}
	
	/**
	 * Resets bit set into initial set - no bits are set, and bounds are undefined
	 */
	public void clear()
	{
		num_bits = 0;
		lb = ub = 0;
		Arrays.fill(bit_bags, 0);
	}

	/**
	 * Adds every element of another set to this set.
	 *
	 * @param another_set set of elements to be added to this set
	 * @return true if this set has new bits added
	 */
	public boolean add(BitSet another_set)
	{
		boolean new_bits_added = false;
		if (another_set.num_bits > 0)
		{
			if (another_set.lb < this.lb || this.ub < another_set.ub)
			{
				int new_lb = Math.min(another_set.lb, lb);
				int new_ub = Math.max(another_set.ub, ub);
				int n_new_l_bags = (lb - new_lb) / 32;
				int n_new_u_bags = (new_ub - ub) / 32;
				int[] new_bags = new int[n_new_l_bags + bit_bags.length + n_new_u_bags];
				System.arraycopy(bit_bags, 0, new_bags, n_new_l_bags, bit_bags.length);
				bit_bags = new_bags;
				lb = new_lb;
				ub = new_ub;
			}
			int into_bag_index = getBitBagIndex(Math.max(another_set.lb, lb));
			for (int from_bag_index = 0; from_bag_index < another_set.bit_bags.length; from_bag_index++, into_bag_index++) 
			{
				int diff = another_set.bit_bags[from_bag_index] & ~bit_bags[into_bag_index];
				if (diff != 0)
				{
					bit_bags[into_bag_index] |= diff;
					num_bits += countBits(diff);
					new_bits_added = true;
				}
			}
		}
		return new_bits_added;
	}
	
	/**
	 * Checks whether the element is in the set
	 *
	 * @param i element to check
	 * @return true if the element is present in the set
	 */
	public boolean isSet(int i)
	{
		if (isEmpty() || i < lb || ub <= i)
			return false;
		return (bit_bags[(i - lb) >> 5] & (1 << (i & 31))) != 0;
	}

	/**
	 * Checks whether the set has no set bits.
	 *
	 * @return true if all the bits of the set are cleared
	 */
	public boolean isEmpty()
	{
		return num_bits == 0;
	}
	
	/**
	 * Returns the size of the set.
	 * 
	 * @return number of bits in the set
	 */
	public int size()
	{
		return num_bits;
	}
	
	/**
	 * Invokes a bit processor for each set bit in the set.
	 *
	 * @param proc an action implmentation to be called
	 */
	public void forEachBitAccept(BitVisitor proc)
	{
		if (num_bits == 0)
			return;

		for (int bag_index = 0; bag_index < bit_bags.length; bag_index++)
		{
			for (int bit_index = lb + bag_index << 5, bag = bit_bags[bag_index]; bag != 0; bag >>>= 1, bit_index++)
			{
				if ((bag & 0x0001) == 0)
				{
					if ((bag & 0xFFFF) == 0)
					{
						bit_index += 16;
						bag >>>= 16;
					}
					if ((bag & 0x00FF) == 0)
					{
						bit_index += 8;
						bag >>>= 8;
					}
					if ((bag & 0x000F) == 0)
					{
						bit_index += 4;
						bag >>>= 4;
					}
					if ((bag & 0x0003) == 0)
					{
						bit_index += 2;
						bag >>>= 2;
					}
					if ((bag & 0x0001) == 0)
					{
						bit_index += 1;
						bag >>>= 1;
					}
				}
				proc.visit(bit_index);
			}
		}
	}

	/**
	 * Returns an index of a bag where new bit is going to be placed. Reallocates
	 * bags in case new bit is outside current boundaries.
	 * 
	 * @param i element to be added to the set
	 * @return index of the bit bag where the bit should be set
	 */
	int getBitBagIndex(int i)
	{
		if (num_bits == 0)
		{
			lb = i & ~31;
			ub = lb + bit_bags.length * 32;
			
			return 0;
		}
		else if (i < lb)
		{
			int new_lb = i & ~31;
			int n_new_bags = (lb - new_lb) / 32;
			int[] new_bags = new int[bit_bags.length + n_new_bags];
			System.arraycopy(bit_bags, 0, new_bags, n_new_bags, bit_bags.length);
			bit_bags = new_bags;
			lb = new_lb;
			
			return 0;
		}
		else if (ub <= i)
		{
			int new_ub = (i + 32) & ~31;
			int n_new_bags = (new_ub - ub) / 32;
			int[] new_bags = new int[bit_bags.length + n_new_bags];
			System.arraycopy(bit_bags, 0, new_bags, 0, bit_bags.length);
			bit_bags = new_bags;
			ub = new_ub;
			
			return bit_bags.length - 1; 
		}
		else // i E [lb,ub)
		{
			return (i - lb) / 32; 
		}
	}
	
	/**
	 * Counts set bits in a 32-bit bag
	 * 
	 * @param bits bag of bits
	 * @return number of bits in a bag
	 */
	static int countBits(int bits)
	{
		bits = (bits & 0x55555555) + ((bits >>> 1) & 0x55555555);
		bits = (bits & 0x33333333) + ((bits >>> 2) & 0x33333333);
		bits = (bits & 0x0F0F0F0F) + ((bits >>> 4) & 0x0F0F0F0F);
		bits = (bits & 0x00FF00FF) + ((bits >>> 8) & 0x00FF00FF);
		return (bits & 0x0000FFFF) + (bits >>> 16);
	}

	/**
	 * Base class for an entity that does something with each set bit 
	 */
	public static interface BitVisitor
	{
		/**
		 * This function is called for each non-zero bit in a set
		 * 
		 * @param bit bit number
		 */
		void visit(int bit);
	}
}
