/***
 * Beaver: compiler builder framework for Java                       
 * Copyright (c) 2003-2006 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */
package beaver.comp.spec;

/**
 * @author Alexander Demenchuk
 *
 */
public class TreeWalker extends NodeVisitor
{

	public void visit(Spec node)
	{
		RuleList list = node.rules;
		if (list != null)
		{
			for (Rule i = list.first(); i != null; i = list.next(i))
			{
				i.accept(this);
			}
		}
	}

	public void visit(Rule node)
	{
		AltList list = node.alts;
		if (list != null)
		{
			for (Alt i = list.first(); i != null; i = list.next(i))
			{
				i.accept(this);
			}
		}
	}

	public void visit(Alt node)
	{
		ItemList list = node.def;
		if (list != null)
		{
			for (Item i = list.first(); i != null; i = list.next(i))
			{
				i.accept(this);
			}
		}
	}

	public void visit(ItemInline node)
	{
		ItemList list = node.def;
		if (list != null)
		{
			for (Item i = list.first(); i != null; i = list.next(i))
			{
				i.accept(this);
			}
		}
	}

	public void visit(ItemString node)
	{
		// leaf
	}

	public void visit(ItemSymbol node)
	{
		// leaf
	}

}
