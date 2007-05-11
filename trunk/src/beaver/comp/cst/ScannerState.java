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
public class ScannerState extends beaver.util.Node
{
	public Term         selector;
	public Term         name;
	public TermDeclList terminals;

	public ScannerState(Term selector, Term name, TermDeclList terminals)
	{
		this.selector  = selector;
		this.name      = name;
		this.terminals = terminals;
	}

	public void accept(NodeVisitor visitor)
	{
		visitor.visit(this);
	}
}
