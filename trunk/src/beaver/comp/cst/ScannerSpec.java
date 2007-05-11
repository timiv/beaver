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
public class ScannerSpec extends beaver.util.Node
{
	public MacroDeclList    macros;
	public TermDeclList     terminals;
	public ScannerStateList states;

	public ScannerSpec(TermDeclList terminals, ScannerStateList states)
	{
		this.terminals = terminals;
		this.states    = states;
	}

	public ScannerSpec(MacroDeclList macros, TermDeclList terminals, ScannerStateList states)
	{
		this.macros    = macros;
		this.terminals = terminals;
		this.states    = states;
	}

	public void accept(NodeVisitor visitor)
	{
		visitor.visit(this);
	}
}
