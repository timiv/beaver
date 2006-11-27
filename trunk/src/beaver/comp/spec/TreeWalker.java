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
		if (node.rules != null)
		{
			for (Rule i = (Rule) node.rules.first(); i != null; i = (Rule) i.next())
			{
				i.accept(this);
			}
		}
	}

	public void visit(Rule node)
	{
		if (node.alts != null)
		{
			for (Alt i = (Alt) node.alts.first(); i != null; i = (Alt) i.next())
			{
				i.accept(this);
			}
		}
	}

	public void visit(Alt node)
	{
		if (node.def != null)
		{
			for (Item i = (Item) node.def.first(); i != null; i = (Item) i.next())
			{
				i.accept(this);
			}
		}
	}

	public void visit(ItemInline node)
	{
		if (node.def != null)
		{
			for (Item i = (Item) node.def.first(); i != null; i = (Item) i.next())
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
