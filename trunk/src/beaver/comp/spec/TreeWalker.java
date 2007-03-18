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

	public void visit(ParserSpec node)
	{
		if ( node.ruleList       != null ) node.ruleList       .accept(this);
		if ( node.precedenceDecl != null ) node.precedenceDecl .accept(this);
	}

	public void visit(RangeExprRange node)
	{
		if ( node.range != null ) node.range .accept(this);
	}

	public void visit(ItemStatic node)
	{
		if ( node.text != null ) node.text .accept(this);
	}

	public void visit(RegExpItem node)
	{
		if ( node.charExpr != null ) node.charExpr .accept(this);
	}

	public void visit(Alt node)
	{
		if ( node.name     != null ) node.name     .accept(this);
		if ( node.itemList != null ) node.itemList .accept(this);
	}

	public void visit(TermDecl node)
	{
		if ( node.regExp  != null ) node.regExp  .accept(this);
		if ( node.name    != null ) node.name    .accept(this);
		if ( node.context != null ) node.context .accept(this);
	}

	public void visit(Spec node)
	{
		if ( node.scannerSpec != null ) node.scannerSpec .accept(this);
		if ( node.parserSpec  != null ) node.parserSpec  .accept(this);
	}

	public void visit(ScannerSpec node)
	{
		if ( node.macros    != null ) node.macros    .accept(this);
		if ( node.terminals != null ) node.terminals .accept(this);
	}

	public void visit(Rule node)
	{
		if ( node.name    != null ) node.name    .accept(this);
		if ( node.altList != null ) node.altList .accept(this);
	}

	public void visit(RegExpItemQuant node)
	{
		if ( node.quantifier != null ) node.quantifier .accept(this);
		if ( node.charExpr   != null ) node.charExpr   .accept(this);
	}

	public void visit(PrecItemRule node)
	{
		if ( node.name != null ) node.name .accept(this);
	}

	public void visit(CharExprRange node)
	{
		if ( node.rangeExpr != null ) node.rangeExpr .accept(this);
	}

	public void visit(ItemSymbol node)
	{
		if ( node.ref  != null ) node.ref  .accept(this);
		if ( node.oper != null ) node.oper .accept(this);
		if ( node.name != null ) node.name .accept(this);
	}

	public void visit(CharExprNested node)
	{
		if ( node.regExp != null ) node.regExp .accept(this);
	}

	public void visit(Context node)
	{
		if ( node.regExp != null ) node.regExp .accept(this);
	}

	public void visit(Precedence node)
	{
		if ( node.assoc        != null ) node.assoc        .accept(this);
		if ( node.precItemList != null ) node.precItemList .accept(this);
	}

	public void visit(MacroDecl node)
	{
		if ( node.regExp != null ) node.regExp .accept(this);
		if ( node.name   != null ) node.name   .accept(this);
	}

	public void visit(PrecItemTerm node)
	{
		if ( node.text != null ) node.text .accept(this);
	}

	public void visit(RangeExprMinus node)
	{
		if ( node.range != null ) node.range .accept(this);
		if ( node.diff  != null ) node.diff  .accept(this);
	}

	public void visit(RangeExprMacro node)
	{
		if ( node.macro != null ) node.macro .accept(this);
	}

	public void visit(CharExprText node)
	{
		if ( node.text != null ) node.text .accept(this);
	}

	public void visit(ItemInline node)
	{
		if ( node.oper     != null ) node.oper     .accept(this);
		if ( node.itemList != null ) node.itemList .accept(this);
		if ( node.name     != null ) node.name     .accept(this);
	}

	public void visit(Quantifier node)
	{
		if ( node.min != null ) node.min .accept(this);
		if ( node.max != null ) node.max .accept(this);
	}

	public void visit(RegExpItemClose node)
	{
		if ( node.oper     != null ) node.oper     .accept(this);
		if ( node.charExpr != null ) node.charExpr .accept(this);
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

	public void visit(MacroDeclList list)
	{
		for ( MacroDecl item = (MacroDecl) list.first(); item != null; item = (MacroDecl) item.next() )
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

	public void visit(RegExp list)
	{
		for ( RegExpItemList item = (RegExpItemList) list.first(); item != null; item = (RegExpItemList) item.next() )
		{
			item.accept(this);
		}
	}

	public void visit(RegExpItemList list)
	{
		for ( RegExpItem item = (RegExpItem) list.first(); item != null; item = (RegExpItem) item.next() )
		{
			item.accept(this);
		}
	}

	public void visit(TermDeclList list)
	{
		for ( TermDecl item = (TermDecl) list.first(); item != null; item = (TermDecl) item.next() )
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

	public void visit(NumTerm node)
	{
		// leaf node
	}

	public void visit(Term node)
	{
		// leaf node
	}

}
