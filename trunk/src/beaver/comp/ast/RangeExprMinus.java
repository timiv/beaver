/**
* Beaver: compiler front-end construction toolkit
* Copyright (c) 2007 Alexander Demenchuk <alder@softanvil.com>
* All rights reserved.
*
* See the file "LICENSE" for the terms and conditions for copying,
* distribution and modification of Beaver.
*/
package beaver.comp.ast;

/**
 * @author <a href="http://beaver.sourceforge.net">Beaver</a> parser generator
 * @author Alexander Demenchuk
 */
public class RangeExprMinus extends RangeExpr
{
	public RangeExpr range;
	public RangeExpr diff;

	public RangeExprMinus(RangeExpr diff, RangeExpr range)
	{
		this.diff  = diff;
		this.range = range;
	}

	public void accept(NodeVisitor visitor)
	{
		visitor.visit(this);
	}
	
	public beaver.comp.lexer.RegExp accept(RegExpCompiler compiler)
	{
		return compiler.compile(this);
	}
}
