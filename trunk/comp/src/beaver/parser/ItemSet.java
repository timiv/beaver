package beaver.parser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

class ItemSet
{
	private Map  items = new HashMap();
	private Item probe = new Item(null, 0);
	private Item firstItem;
	private Item lastItem;
	private int  kernelSize;
	private int  kernelHash;

	Item getItem(Production rule, int dot)
	{
		probe.become(rule, dot);
		Item item = (Item) items.get(probe);
		if (item == null)
		{
			add(item = new Item(rule, dot));
		}
		return item;
	}
	
	void link()
	{
		Item[] kernel = (Item[]) items.values().toArray(new Item[items.size()]);
		Arrays.sort(kernel);
		/*
		 * Link all items and calculate kernel hash code, while we link kernel items
		 */
		firstItem = lastItem = kernel[0];
		kernelHash = firstItem.hashCode();
		for (int i = 1; i < kernel.length; i++)
		{
			lastItem = lastItem.next = kernel[i];
			kernelHash = kernelHash * 571 + lastItem.hashCode();
		}
		lastItem.next = null;
		kernelSize = kernel.length;
	}

	void buildClosure()
	{
		for (Item item = firstItem; item != null; item = item.next)
		{
			if (!item.isDotAfterLastSymbol())
			{
				Symbol symbol = item.getSymbolAfterDot();
				if (symbol instanceof Nonterminal)
				{
					Production[] ntRules = ((Nonterminal) symbol).rules;
					for (int i = 0; i < ntRules.length; i++)
					{
						Item newItem = getItem(ntRules[i], 0);
						if (newItem.addLookaheadsFrom(item))
						{
							item.addAcceptor(newItem);
						}
					}
				}
			}
		}
	}

	void resetContributions()
	{
		for (Item item = firstItem; item != null; item = item.next)
		{
			item.hasContributed = false;
		}
	}

	void copyEmitters(ItemSet set)
	{
		if (set.kernelSize != kernelSize)
			throw new IllegalArgumentException("unequal kernels");

		Item src = set.firstItem;
		Item dst = firstItem;
		for (int n = kernelSize; n > 0; n--)
		{
			dst.copyEmittersOf(src);

			src = src.next;
			dst = dst.next;
		}
	}

	void reverseEmitters()
	{
		for (Item item = firstItem; item != null; item = item.next)
		{
			item.reverseEmitters();
		}
	}

	boolean propagateLookaheads()
	{
		boolean propagated = false;
		for (Item item = firstItem; item != null; item = item.next)
		{
			if (!item.hasContributed)
			{
				if (item.propagateLookaheads())
				{
					propagated = true;
				}
				item.hasContributed = true;
			}
		}
		return propagated;
	}

	Item getFirstItem()
	{
		return firstItem;
	}

	private void add(Item item)
	{
		items.put(item, item);
		if (lastItem != null) // there is no need to link items while we build the kernel
		{
			lastItem = lastItem.next = item;
		}
	}

	public int hashCode()
	{
		return kernelHash;
	}

	public boolean equals(Object o)
	{
		return o == this || o instanceof ItemSet && this.equals((ItemSet) o);
	}

	boolean equals(ItemSet other)
	{
		if (kernelSize != other.kernelSize)
			return false;

		Item myItem = firstItem, otherItem = other.firstItem;
		for (int n = kernelSize; n > 0; n--, myItem = myItem.next, otherItem = otherItem.next)
		{
			if (!myItem.equals(otherItem))
				return false;
		}
		return true;
	}

	public String toString()
	{
		String repr = "";
		if (firstItem != null)
		{
			repr = "  " + firstItem.toString();
			int n = 1;
			for (Item item = firstItem.next; item != null; item = item.next)
			{
				repr += "\n";
				if (++n <= kernelSize)
				{
					repr += "  ";
				}
				else
				{
					repr += "+ ";
				}
				repr += item;
			}
		}
		return repr;
	}
}
