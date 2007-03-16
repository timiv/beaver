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
public class CharExprRange extends CharExpr
{
	public RangeExpr rangeExpr;

	public CharExprRange(RangeExpr rangeExpr)
	{
		this.rangeExpr = rangeExpr;
	}

	public void accept(NodeVisitor visitor)
	{
		visitor.visit(this);
	}
}
