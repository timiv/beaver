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
public class RegExpItem extends beaver.util.Node
{
	public Quantifier quantifier;
	public Term       oper;
	public CharExpr   charExpr;

	public RegExpItem(CharExpr charExpr)
	{
		this.charExpr = charExpr;
	}

	public RegExpItem(CharExpr charExpr, Term oper)
	{
		this.charExpr = charExpr;
		this.oper     = oper;
	}

	public RegExpItem(CharExpr charExpr, Quantifier quantifier)
	{
		this.charExpr   = charExpr;
		this.quantifier = quantifier;
	}

	public void accept(NodeVisitor visitor)
	{
		visitor.visit(this);
	}
}
