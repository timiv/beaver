package beaver.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import beaver.util.BitSet;

class Item implements Comparable
{
	Production production;
	int        dot;
	BitSet     lookaheads;

	/**
	 * Items may initially be missing some symbols from their lookahead sets.
	 * <p>
	 * Acceptors are the items that would need to be updated if new symbols are added to this item
	 * lookahead sets.
	 */
	Collection acceptors;

	/**
	 * Items may initially be missing some symbols from their lookahead sets.
	 * <p>
	 * Emitters are the items that might contribute new lookahead symbols to this item.
	 */
	Collection emitters;

	/**
	 * A flag to mark items that have already contributed to acceptors.
	 */
	boolean    hasContributed;

	/**
	 * Items in a set a linked
	 */
	Item       next;

	Item(Production rule, int dot)
	{
		this.production = rule;
		this.dot = dot;
	}

	void become(Production rule, int dot)
	{
		this.production = rule;
		this.dot = dot;
	}
	
	boolean isDotAfterLastSymbol()
	{
		return dot == production.rhs.length;
	}

	Symbol getSymbolAfterDot()
	{
		return production.rhs[dot].symbol;
	}

	void addLookahead(Terminal term)
	{
		if (lookaheads == null)
			lookaheads = new BitSet(production.lhs.firstSet.capacity());

		lookaheads.add(term.id);
	}

	boolean addLookaheads(BitSet terms)
	{
		if (lookaheads == null)
			lookaheads = new BitSet(production.lhs.firstSet.capacity());

		return lookaheads.add(terms);
	}

	/**
	 * Adds lookahead symbols from a given item.
	 * 
	 * @return true if all RHS parts were null-able nonterminals and hence the lookahead set needs
	 *         to be expanded by propagating terminals.
	 */
	boolean addLookaheadsFrom(Item item)
	{
		if (lookaheads == null)
			lookaheads = new BitSet(production.lhs.firstSet.capacity());

		Production.RHSElement[] rhs = item.production.rhs;
		for (int i = item.dot + 1; i < rhs.length; i++)
		{
			Symbol rhsSymbol = rhs[i].symbol;

			if (rhsSymbol instanceof Terminal)
				lookaheads.add(rhsSymbol.id);
			else
				lookaheads.add(((Nonterminal) rhsSymbol).firstSet);

			if (!rhsSymbol.isNullable())
				return false;
		}
		return true;
	}

	void addAcceptor(Item item)
	{
		if (acceptors == null)
			acceptors = new ArrayList();

		acceptors.add(item);
	}

	void addEmitter(Item item)
	{
		if (emitters == null)
			emitters = new ArrayList();

		emitters.add(item);
	}
	
	void copyEmittersOf(Item item)
	{
		if (item.emitters != null)
		{
			if (emitters == null)
				emitters = new ArrayList(item.emitters);
			else
				emitters.addAll(item.emitters);
		}
	}
	
	void reverseEmitters()
	{
		if (emitters != null)
		{
			for (Iterator iter = emitters.iterator(); iter.hasNext();)
	        {
		        Item item = (Item) iter.next();
		        item.addAcceptor(this);
	        }
			emitters = null;
		}
	}

	/**
	 * Propagates this item lookaheads to acceptor items
	 * 
	 * @return true if this item lookaheads were propagated to acceptor items
	 */
	boolean propagateLookaheads()
	{
		boolean propagated = false;
		if (acceptors != null)
		{
			for (Iterator i = acceptors.iterator(); i.hasNext();)
			{
				Item item = (Item) i.next();
				if (item.addLookaheads(lookaheads))
				{
					propagated = true;
					/*
					 * the item that accepted new lookaheads may need to contribute them to other
					 * items
					 */
					item.hasContributed = false;
				}
			}
		}
		return propagated;
	}

	public int hashCode()
	{
		return production.id * 37 + dot;
	}

	public boolean equals(Object o)
	{
		if (o == this)
		{
			return true;
		}
		else if (o instanceof Item)
		{
			Item c = (Item) o;
			return this.production == c.production && this.dot == c.dot;
		}
		else
		{
			return false;
		}
	}

	public int compareTo(Object o)
	{
		if (o == this)
			return 0;

		Item item = (Item) o;

		int cmp = this.production.id - item.production.id;
		if (cmp == 0)
		{
			cmp = this.dot - item.dot;
		}
		return cmp;
	}
	
	public String toString()
	{
		String repr = production.lhs + " = ";
		for (int i = 0; i < production.rhs.length; i++)
		{
			if (i == dot)
			{
				repr += "* ";
			}
			repr += production.rhs[i] + " ";
		}
		if (dot == production.rhs.length)
		{
			repr += "* ";		
		}
		return repr + ";";
	}
}
