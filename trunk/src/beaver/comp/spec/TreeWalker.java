/**
 * Beaver: compiler front-end construction toolkit
 * Copyright (c) 2007 Alexander Demenchuk <alder@softanvil.com>
 * All rights reserved.
 *
 * See the file "LICENSE" for the terms and conditions for copying,
 * distribution and modification of Beaver.
 */
package beaver.comp.spec;

/**
 * @author <a href="http://beaver.sourceforge.net">Beaver</a> parser generator
 */
public class TreeWalker implements NodeVisitor
{

	public void visit(ItemSymbol node)
	{
		if ( node.ref  != null ) node.ref  .accept(this);
		if ( node.oper != null ) node.oper .accept(this);
		if ( node.name != null ) node.name .accept(this);
	}

	public void visit(ParserSpec node)
	{
		if ( node.ruleList       != null ) node.ruleList       .accept(this);
		if ( node.precedenceDecl != null ) node.precedenceDecl .accept(this);
	}

	public void visit(ItemStatic node)
	{
		if ( node.text != null ) node.text .accept(this);
	}

	public void visit(Alt node)
	{
		if ( node.name     != null ) node.name     .accept(this);
		if ( node.itemList != null ) node.itemList .accept(this);
	}

	public void visit(Precedence node)
	{
		if ( node.assoc        != null ) node.assoc        .accept(this);
		if ( node.precItemList != null ) node.precItemList .accept(this);
	}

	public void visit(Spec node)
	{
		if ( node.parserSpec != null ) node.parserSpec .accept(this);
	}

	public void visit(PrecItemTerm node)
	{
		if ( node.text != null ) node.text .accept(this);
	}

	public void visit(Rule node)
	{
		if ( node.name    != null ) node.name    .accept(this);
		if ( node.altList != null ) node.altList .accept(this);
	}

	public void visit(ItemInline node)
	{
		if ( node.oper     != null ) node.oper     .accept(this);
		if ( node.itemList != null ) node.itemList .accept(this);
		if ( node.name     != null ) node.name     .accept(this);
	}

	public void visit(PrecItemRule node)
	{
		if ( node.name != null ) node.name .accept(this);
	}

	public void visit(RuleList list)
	{
		for ( Rule item = (Rule) list.first(); item != null; item = (Rule) item.next() )
		{
			item.accept(this);
		}
	}

	public void visit(ItemList list)
	{
		for ( Item item = (Item) list.first(); item != null; item = (Item) item.next() )
		{
			item.accept(this);
		}
	}

	public void visit(AltList list)
	{
		for ( Alt item = (Alt) list.first(); item != null; item = (Alt) item.next() )
		{
			item.accept(this);
		}
	}

	public void visit(PrecedenceList list)
	{
		for ( Precedence item = (Precedence) list.first(); item != null; item = (Precedence) item.next() )
		{
			item.accept(this);
		}
	}

	public void visit(PrecItemList list)
	{
		for ( PrecItem item = (PrecItem) list.first(); item != null; item = (PrecItem) item.next() )
		{
			item.accept(this);
		}
	}

	public void visit(Term node)
	{
		// leaf node
	}

}
