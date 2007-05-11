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
 */
public class TermDecl extends beaver.util.Node
{
	public Term   name;
	public RegExp regExp;
	public RegExp ctx;
	public Term   event;

	public TermDecl(Term name, RegExp regExp)
	{
		this.name   = name;
		this.regExp = regExp;
	}

	public TermDecl(Term name, RegExp regExp, RegExp ctx)
	{
		this.name   = name;
		this.regExp = regExp;
		this.ctx    = ctx;
	}

	public TermDecl(Term name, RegExp regExp, Term event)
	{
		this.name   = name;
		this.regExp = regExp;
		this.event  = event;
	}

	public TermDecl(Term name, RegExp regExp, RegExp ctx, Term event)
	{
		this.name   = name;
		this.regExp = regExp;
		this.ctx    = ctx;
		this.event  = event;
	}

	public void accept(NodeVisitor visitor)
	{
		visitor.visit(this);
	}
}
