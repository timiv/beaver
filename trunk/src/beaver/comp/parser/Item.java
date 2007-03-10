/***
 * Beaver: compiler front-end construction toolkit                       
 * Copyright (c) 2003-2007 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */

package beaver.comp.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import beaver.util.BitSet;

/**
 * This class represents an LALR item or a state configuration item.
 * Each configuration consists of a production, a "dot" that marks a
 * position in a production and a set of lookahead symbols.
 *
 * @author Alexander Demenchuk
 */
class Item implements Comparable
{
	Item next;
	
	private Production rule;
	private int        dot;
	private BitSet     lookaheads;
	
	/**
	 * Items may initially be missing some symbols from their lookahead sets.
	 * Acceptors are the items that would need to be updated if new symbols
	 * are added to this item lookahead sets.
	 */ 
	transient Collection acceptors;

	/**
	 * Items may initially be missing some symbols from their lookahead sets.
	 * Emitters are the items that might contribute new lookahead symbols to
	 * this item.
	 */
	transient Collection emitters;
	
	/**
	 * A flags that is used to mark items that have already
	 * contributed to acceptor states.
	 */
	transient boolean hasContributed;

	
	Item(Production rule, int dot)
	{
		this.rule = rule;
		this.dot  = dot;
	}
	
	Item as(Production rule, int dot)
	{
		this.rule = rule;
		this.dot  = dot;
		
		return this;
	}
	
	boolean isDotAfterLastSymbol()
	{
		return dot == rule.rhs.length;
	}
	
	Symbol getSymbolAfterDot()
	{
		return rule.rhs[dot].symbol;
	}
	
	Item makeItemForShiftedDot()
	{
		return new Item(rule, dot + 1);
	}
	
	int getNumberOfLookaheads()
	{
		return lookaheads == null ? 0 : lookaheads.size();
	}
	
	void addLookahead(Terminal term)
	{
		if (lookaheads == null)
			lookaheads = new BitSet(rule.lhs.firstSet.capacity());
		
		lookaheads.add(term.id);
	}
	
	boolean addLookaheads(BitSet terms)
	{
		if (lookaheads == null)
			lookaheads = new BitSet(rule.lhs.firstSet.capacity());
		
		return lookaheads.add(terms);
	}

	/**
	 * Adds lookahead symbols from a given item.
	 *
	 * @return true if all rhs parts were nullable nonterminals and hence the lookahead set
	 *         needs to be expanded by propagating terminals.
	 */
	boolean addLookaheads(Item item)
	{		
		if (lookaheads == null)
			lookaheads = new BitSet(rule.lhs.firstSet.capacity());

		for (int i = item.dot + 1; i < item.rule.rhs.length; i++)
		{
			Symbol sym = item.rule.rhs[i].symbol;
			
			if (sym instanceof Terminal)
				lookaheads.add(sym.id);
			else
				lookaheads.add(((NonTerminal) sym).firstSet);

			if (!sym.matchesEmptyString())
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
	
	void copyEmitters(Item item)
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
	
	boolean findLookaheads()
	{
		boolean found = false;
		if (acceptors != null)
		{
			for (Iterator iter = acceptors.iterator(); iter.hasNext();)
	        {
	            Item item = (Item) iter.next();
	            if (item.addLookaheads(this.lookaheads))
	            {
	            	found = true;
	            	item.hasContributed = false;
	            }
	        }
		}
		return found;
	}
	
	void accept(Action.Reduce.Builder reduceActionsBuilder)
	{
		if (lookaheads != null)
		{
			reduceActionsBuilder.set(rule);
			lookaheads.forEachBitAccept(reduceActionsBuilder);
		}
	}
	
	public int hashCode()
	{
		return rule.id * 37 + dot;
	}
	
	public boolean equals(Object o)
	{
		if (o instanceof Item)
		{
			Item c = (Item) o;
			return this.rule == c.rule && this.dot == c.dot;
		}
		return false;
	}
	
	public String toString()
	{
		StringBuffer text = new StringBuffer();
		text.append(rule.lhs.name)
			.append(" =");
		for (int i = 0; i < rule.rhs.length; i++)
        {
			text.append(' ');
	        if (dot == i)
	        	text.append('*');
	        text.append(rule.rhs[i].name);
        }
        if (dot == rule.rhs.length)
        	text.append('*');
		
		return text.toString();
	}
	
	/**
	 * Defines ordering for state configuration items.
	 */
	public int compareTo(Object o)
	{
		if (o == this)
			return 0;
		
		Item c = (Item) o;

		int cmp = this.rule.id - c.rule.id;
		if (cmp == 0)
		{
			cmp = this.dot - c.dot;
		}
		return cmp;
	}

}
