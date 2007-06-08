/**
* Beaver: compiler front-end construction toolkit
* Copyright (c) 2007 Alexander Demenchuk <alder@softanvil.com>
* All rights reserved.
*
* See the file "LICENSE" for the terms and conditions for copying,
* distribution and modification of Beaver.
*/
package beaver.comp.cst;

/**
 * @author <a href="http://beaver.sourceforge.net">Beaver</a> parser generator
 * @author Alexander Demenchuk
 */
public class RegExpItemMulti extends RegExpItem
{
	public Multiplicity multiplicity;

	public RegExpItemMulti(CharExpr charExpr, Multiplicity multiplicity)
	{
		super(charExpr);
		this.multiplicity = multiplicity;
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
